package AwarenessServer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import org.sqlite.core.DB;

/**
 * Bei dieser Klasse handelt es sich um einen Thread, der eröffnet wird, wenn der Client sich mit dem Server verbunden hat.
 * Über dieses Nebenprogramm werden die einzelnen Anfragen des Clients bearbeitet
 * @author Benedikt Brüntrup
 */
public class ServerSitzung implements Runnable{
	
	//Instanz-Variable, womit die Anfragen des Clients entgegebngenommen und beantwortet werden
	private Socket sitzung;
	private String benutzer_ = null;
	
	/**
	 * Konstruktor der Server-Sitzung
	 * Arbeitet die Anfragen des Clients ab, bis der Client eine quit-Anweisung sendet
	 * @param sitzung Die Sitzungsvariable. Sie dient dazu die Anfragen des Clients entgegen zu nehmen und diese zu beantworten.
	 */
	public ServerSitzung(Socket sitzung){
		
		System.out.printf ("Der Client: %s hat eine Verbindung zum Server aufgebaut.%n", sitzung.getInetAddress().getHostAddress());
		this.sitzung = sitzung;
		
		//erstellt einen neue Thread-Instanz und startet diese
		Thread t1 = new Thread(this);
		t1.start();
	}


	/**
	 * Thread-Methode, die den Sitzungsprozess abarbeitet
	 */
	public void run() {
		try {			
			//definert einen Scanner, der auf Clientanfragen lauscht
			Scanner clientAnfrage = new Scanner(sitzung.getInputStream());
			
			//PrintStream, womit auf die Anfrage des Clients geantwortet wird
			PrintStream serverAntwort = new PrintStream(sitzung.getOutputStream());
			
			//die Schleife läuft solange, bis der Client ein quit sendet.
			boolean quit = false;
			do{
				//wenn der Scanner eine Clientanfrage entdeckt hat
				if(clientAnfrage.hasNext()){
					
					//splittet die Anfrage in ein Array
					String anfrage = clientAnfrage.nextLine();
					
					String[] arrClientAnfrage;
					arrClientAnfrage = anfrage.split("#§");

					//wenn das Array ein Feld besitzt
					if(arrClientAnfrage.length > 0){
												
						//überprüft aus den ersten Feld den Typ der Anfrage
						String feld1 = arrClientAnfrage[0];
						
						/*
						 * Anfrage zur Verbindungsschließung
						 */
						if (feld1.equals("quit") || sitzung.isClosed()){
							quit = true;
						}
						
						/*
						 * Anfrage auf eine Kontaktliste
						 */
						else if(feld1.equals("get-kontaktlist")){
							kontaktlisteSenden(serverAntwort, arrClientAnfrage, false);
						}
						
						/*
						 * Lädt die Kontaktlisteänderungen nach einer bestimmten Zeit
						 */
						else if(feld1.equals("get-aenderung-kontaktlist")){
							kontaktlisteSenden(serverAntwort, arrClientAnfrage, true);
						}
						
						/*
						 * Anfrage zum Hinzufügen eines neuen Benutzers
						 */
						else if (feld1.equals("set-neuer-benutzer")){
							benutzerHinzufuegen(arrClientAnfrage[1], arrClientAnfrage[2], serverAntwort);
						}
						
						/*
						 * überprüft die Login-Daten
						 */
						else if (feld1.equals("check-login")){
							ueberprüfeLoginDaten(arrClientAnfrage[1], arrClientAnfrage[2], serverAntwort);
						}
						
						/*
						 * fügt einen Benutzer zur Kontaktliste hinzu
						 */
						else if (feld1.equals("add-kontakt")){
							kontaktZurKontaklisteHinzufuegen(arrClientAnfrage[1], arrClientAnfrage[2], serverAntwort);
						}
						
						/*
						 * lädt vom Client das aktuelle Status-Symbol
						 */
						else if (feld1.equals("set-symbol")){
							Datenbankzugriff dbZugriff = new Datenbankzugriff();
							dbZugriff.aendereStatusSymbol(arrClientAnfrage[1], arrClientAnfrage[2]);
							dbZugriff.verbindungSchliessen();
						}
						
						/*
						 * lädt vom Client die aktuelle Statusnachricht
						 */
						else if (feld1.equals("set-statusnachricht")){
							Datenbankzugriff dbZugriff = new Datenbankzugriff();
							dbZugriff.aendereStatusnachricht(arrClientAnfrage[1], arrClientAnfrage[2]);
							dbZugriff.verbindungSchliessen();
						}
						
						/*
						 * sendet an den Client die aktuelle Statusnachricht
						 */
						else if (feld1.equals("get-statusnachricht")){
							Datenbankzugriff dbZugriff = new Datenbankzugriff();
							serverAntwort.println(dbZugriff.getStatusnachricht(arrClientAnfrage[1]));
							dbZugriff.verbindungSchliessen();
							serverAntwort.println("§Ende§");
						}
					}
				}
			}while(!quit);
			
			//ändert den Online-Status des Benutzers auf offline
			if (benutzer_ != null){
				Datenbankzugriff dbZugriff = new Datenbankzugriff();
				dbZugriff.aendereOnlineStatus(benutzer_, false);
				dbZugriff.verbindungSchliessen();
			}
			
			sitzung.close();
		
		//Exception, die aufgerufen wird, wenn ein Fehler aufgetreten ist
		} catch (IOException | SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			
		//Gibt eine Meldung aus, dass die Verbindung zum Client getrennt wurde
		}finally{
			System.out.printf("Die Verbindung zum Client %s wurde getrennt.%n", sitzung.getInetAddress().getHostAddress());
		}
	}
		
	
	/**
	 * Sendet die Kontaktliste an den Client
	 * @throws SQLException 
	 */
	public void kontaktlisteSenden(PrintStream ausgabeServer, String[] clientAnfrage, boolean update) throws SQLException{
		
		//stellt eine Verbindung zur Datenbank her
		Datenbankzugriff dbZugriff = new Datenbankzugriff();
		
		ResultSet kontaktliste = null;
		//lädt die komplette Kontaktliste aus der Datenbank
		if (!update){
			System.out.printf("Der Client %s fragt die Kontaktliste ab.%n", sitzung.getInetAddress().getHostAddress());
			//fragt von der Datenbank die Kontaktliste ab
			kontaktliste = dbZugriff.get_kontaktliste(clientAnfrage[1]);
		}
		//lädt nur die Änderungen aus der Datenbank
		else{
			//System.out.printf("Der Client %s lädt Kontaktlistenupdates.%n", sitzung.getInetAddress().getHostAddress());
			kontaktliste = dbZugriff.get_kontaktliste_update(clientAnfrage[1], Long.valueOf(clientAnfrage[2]));
		}
		String kontaktliste_str ="";
		
		//geht jede Zeile der Kontaktliste durch
		while(kontaktliste.next()){
			//erstellt aus der Kontaktliste einen String, der via TCP übermittelt werden kann
			kontaktliste_str = kontaktliste.getString(1) + "#§" + kontaktliste.getBoolean(2) + "#§" + kontaktliste.getString(3) + "#§" + kontaktliste.getString(4);
			
			//sendet die Kontaktlistenzeile an den Client
			ausgabeServer.println(kontaktliste_str);
		}
		
		dbZugriff.verbindungSchliessen();
		
		//teilt den Client mit, dass die Kontaktliste zu Ende ist
		ausgabeServer.println("§Ende§");
	}
	
	/**
	 * Fügt einen Benutzer zur Datenbank hinzu und überprüft vorher, ob er nicht schon existiert
	 * @param benutzername Benutzername des neuen Benutzers
	 * @param passwort Passwort des neuen Benutzers
	 * @param ausgabeServer Ausgabe-Stream des Server zum Client
	 */
	public void benutzerHinzufuegen(String benutzername, String passwort, PrintStream ausgabeServer){
		Datenbankzugriff dbZugriff = new Datenbankzugriff();
		
		//Benutzer wird nur angelegt, wenn er noch nicht exiswitert
		if(dbZugriff.bestimmeBNrBenutzer(benutzername) == -1){
			
			//Der Benutzer wird angelegt
			dbZugriff.benutzerAnlegen(benutzername, passwort);
			
			//bestätigt das Anlegen des Benutzers
			ausgabeServer.println("ok");
			ausgabeServer.println("§Ende§"); //Nachrichten Ende wird übertragen	
			
			System.out.printf ("Der Client: %s hat einen neuen Benutzer angelegt.%n", sitzung.getInetAddress().getHostAddress());
		}
		
		//wenn er schon existiert,wird der Client informatiert
		else{
			//informatiert den Client, dass der Benutzername schon belegt ist
			ausgabeServer.println("schon vorhanden");
			ausgabeServer.println("§Ende§"); //Nachrichten Ende wird übertragen
			
			System.out.printf ("Der Client: %s von Client genannt Benutzer, der angelegt werden soll, exisiert schon.%n", sitzung.getInetAddress().getHostAddress());
		}
		
		//trennt die Datenbankverbindung
		dbZugriff.verbindungSchliessen();
	}
	
	/**
	 * Überprüft die Login-Daten und gibt die ID des Benutzers aus, wenn sie gültig sind.
	 * Sonst wird -1 ausgegeben
	 * @param benutzer Der übergebene Benutzername
	 * @param passwort Das übergeben Passwort
	 * @param ausgabeServer Ausgabe-Stream des Server zum Client
	 */
	public void ueberprüfeLoginDaten(String benutzer, String passwort, PrintStream ausgabeServer){
		Datenbankzugriff dbZugriff = new Datenbankzugriff();
		//übermittelt die bestimmte Benutzer-ID
		int wert = dbZugriff.loginCheck(benutzer, passwort);
		ausgabeServer.println(wert);
		ausgabeServer.println("§Ende§"); //Nachrichten Ende wird übertragen
		
		if ( wert != -1) {
			System.out.printf ("Der Client %s hat sich erfolgreich angemeldet.%n", sitzung.getInetAddress().getHostAddress());
			
			//ändert den online-Status des Benutzers aus online
			dbZugriff.aendereOnlineStatus(benutzer, true);
			
			benutzer_ = benutzer;
		}
		
		//trennt die Datenbankverbindung
		dbZugriff.verbindungSchliessen();
	}
	
	/**
	 * fügt einen Kontakt zur Kontaktliste hinzu
	 * @param besitzer Besitzer der kontaktliste
	 * @param kontakt Kontakt, der hinzugefügt werden soll
	 * @param ausgabeServer Ausgabe-Stream des Server zum Client
	 */
	public void kontaktZurKontaklisteHinzufuegen(String besitzer, String kontakt, PrintStream ausgabeServer){
		Datenbankzugriff dbZugriff = new Datenbankzugriff();
		
		//überprüft, ob der Kontakt exisiert
		if (dbZugriff.bestimmeBNrBenutzer(kontakt) != -1 && kontakt.equals(besitzer) == false){
			//fügt den Kontakt zur Kontaktliste hinzu
			dbZugriff.kontaktZurKontaktListeHinzufuegen(besitzer, kontakt);
			ausgabeServer.println("ok"); //Hinzufügen wurde bestätigt
			ausgabeServer.println("§Ende§"); //Nachrichten Ende wird übertragen
		}
		//wenn der Kontakt nicht exisiert
		else{
			//Eine Fehlermeldung wird an den Client gesendet
			ausgabeServer.println("Kontakt exisiert nicht");
			ausgabeServer.println("§Ende§"); //Nachrichten Ende wird übertragen
		}
			
		//trennt die Datenbankverbindung
		dbZugriff.verbindungSchliessen();
	}
}
