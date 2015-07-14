package AwarenessServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Ein TCP-Server, der den Clients eine Kontaktliste mit Awareness-Informationen zur Verfügung stellt.
 * @author Benedikt Brüntrup
 *
 */
public class ServerMain {
		
	/**
	 * Programmeinstiegspunkt
	 * @param args Parameter für den Port
	 */
	public static void main(String[] args) {	
		new ServerMain(12345);
	}
	
	/**
	 * Konstruktor der ServerMain-Klasse
	 */
	public ServerMain(int port){
		try {
			//Der TCP-Server lauscht auf eine Anfrage eines Clients auf den übergebenen Port
			ServerSocket server = new ServerSocket(port);
			
			//wartet, bis ein Client die Verbindung zum Server aufbaut
			while(true){
				Socket sitzung = server.accept();
				
				//eröffnet einen Thread, der die Sitzung abarbeitet
				new ServerSitzung(sitzung);
			}
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
