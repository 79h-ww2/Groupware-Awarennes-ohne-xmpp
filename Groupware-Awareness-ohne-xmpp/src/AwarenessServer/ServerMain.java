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
			
		//überprüft, ob ein Port übergeben wurde
		if (args.length < 1){
			System.out.println("Bitte übergeben Sie als Parameter einen Port.");
		}
		//wenn ein Port übergeben wurde wird überprüft, ob dieser im gültigen Bereich liegt
		else{
			try{
				long portTmp = Long.valueOf(args[0]);
				
				//Fehlermeldung, wenn der Port nicht im gültigen Bereich liegt
				if (portTmp < 0 || portTmp > 65535 ){
					System.out.println("Der Port ist nicht im definierten Bereich. Es muss ein Port zwischen 0 und 65535 gewählt werden.");
				}
				//wenn der Port gültig ist, wird aus der Klasse eine Instanz gebildet
				else{
					//Instanz der Klasse erstellen
					new ServerMain((int)portTmp);
				}
			}
			//wird aufgerufen, wenn der Port kein Zahlenwert ist
			catch(NumberFormatException e){
				System.out.println("Bitte übergeben Sie einen Zahlenwert als Port.");
			}
		}
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
