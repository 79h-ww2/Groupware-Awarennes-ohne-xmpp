package AwarenessServer;

import java.io.IOException;
import java.net.Socket;
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
						
						//Client möchte die Verbindung schließen
						if (feld1.equals("quit")){
							quit = true;
						}
						else if(feld1.equals("get-kontaktlist")){
							
						}
						
						//Schließt die Datenbankverbindung
						dbZugriff.verbindungSchliessen();
					}
				}
			}while(!quit);
		
		//Exception, die aufgerufen wird, wenn ein Fehler aufgetreten ist
		} catch (IOException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			
		//Gibt eine Meldung aus, dass die Verbindung zum Client getrennt wurde
		}finally{
			System.out.printf("Die Verbindung zum Client %s wurde getrennt.%n", sitzung.getInetAddress().getHostAddress());
		}
	}

}
