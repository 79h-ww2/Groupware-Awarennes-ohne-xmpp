package AwarenessServer;

import java.net.Socket;

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
		this.sitzung = sitzung;
	}


	public void run() {
	}

}
