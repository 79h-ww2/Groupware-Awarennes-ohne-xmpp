package AwarenessClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import AwarenessListFenster.ClientFenster;



public class ClientMain extends ClientFenster{
	
	private boolean verbindungGeschlossen;
	
	public ClientMain(){
		super("Awareness-Liste ohne XMPP");
		verbindungGeschlossen = false;
	}

	/**
	 * Programmeinstiegspunkt
	 * @param args
	 */
	public static void main(String[] args) {
		
		ClientMain clientFenster = new ClientMain();
		
		try {
			Socket client = new Socket("127.0.0.1", 12345);
			PrintStream anfragen = new PrintStream(client.getOutputStream());
			
			
			anfragen.println("get-kontaktlist#§xdagox");
			//nimmt die Zeilen vom Server entgegen
			ArrayList<String> zeilen = clientFenster.antwortVomServerEntgegennehmen(client.getInputStream());
			for(String zeile : zeilen){
				System.out.println(zeile);
			}
						
			anfragen.println("quit");
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * nimmt die Antworten des Server entgegen
	 * @param in InputStream des Servers
	 * @return Die Antworten des Servers in einer Arraylist
	 */
	public ArrayList<String> antwortVomServerEntgegennehmen(InputStream in){
		Scanner serverAntworten = new Scanner(in);
		ArrayList<String> zeilenServerAntwort = new ArrayList<>();
		try {
			//Überprüft solange, bis der Server die Anweisung "§Ende§" sendet, ob der Server etwas sendet
			String wert = "";
			do{
				Thread.sleep(1000);
				
				//schreibt die übergebenen Zeilen in eine ArrayList
				while(serverAntworten.hasNext() && !verbindungGeschlossen){
					wert = serverAntworten.nextLine();
					zeilenServerAntwort.add(wert);
				}
			}while(wert.equals("§Ende§") == false && !verbindungGeschlossen );
		} catch (InterruptedException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		
		//gibt die ArrayList aus
		return zeilenServerAntwort;
	}
	
}
