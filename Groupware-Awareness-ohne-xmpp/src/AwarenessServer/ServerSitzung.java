package AwarenessServer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Bei dieser Klasse handelt es sich um einen Thread, der eröffnet wird, wenn der Client sich mit dem Server verbunden hat.
 * Über dieses Nebenprogramm werden die einzelnen Anfragen des Clients bearbeitet
 * @author Benedikt Brüntrup
 */
public class ServerSitzung implements Runnable{
	
	//Instanz-Variable, womit die Anfragen des Clients entgegebngenommen und beantwortet werden
	private Socket sitzung;
	
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
						
						//stellt eine Verbindung zur Datenbank her
						Datenbankzugriff dbZugriff = new Datenbankzugriff();
						
						//überprüft aus den ersten Feld den Typ der Anfrage
						String feld1 = arrClientAnfrage[0];
						
						/*
						 * Client möchte die Verbindung schließen
						 */
						if (feld1.equals("quit")){
							quit = true;
						}
						
						/*
						 * übertragen der Kontaktliste
						 */
						else if(feld1.equals("get-kontaktlist")){
							if ( arrClientAnfrage.length == 2){
								System.out.printf("Der Client %s fragt die Kontaktliste ab.%n", sitzung.getInetAddress().getHostAddress());
								
								//fragt von der Datenbank die Kontaktliste ab
								ResultSet kontaktliste = dbZugriff.get_kontaktliste(arrClientAnfrage[1]);
								String kontaktliste_str ="";
								
								//geht jede Zeile der Kontaktliste durch
								while(kontaktliste.next()){
									//erstellt aus der Kontaktliste einen String, der via TCP übermittelt werden kann
									kontaktliste_str = "(" + kontaktliste.getString(0) + "," + kontaktliste.getBoolean(1) + "," + kontaktliste.getString(2) + "," + kontaktliste.getString(3) + ")";
									
									//sendet die Kontaktlistenzeile an den Client
									serverAntwort.println(kontaktliste_str);
								}
								
								//teilt den Client mit, dass die Kontaktliste zu Ende ist
								serverAntwort.println("§Ende$");
							};
						}
						
						//Schließt die Datenbankverbindung
						dbZugriff.verbindungSchliessen();
					}
				}
			}while(!quit);
			
			sitzung.close();
		
		//Exception, die aufgerufen wird, wenn ein Fehler aufgetreten ist
		} catch (IOException | SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			
		//Gibt eine Meldung aus, dass die Verbindung zum Client getrennt wurde
		}finally{
			System.out.printf("Die Verbindung zum Client %s wurde getrennt.%n", sitzung.getInetAddress().getHostAddress());
		}
	}

}
