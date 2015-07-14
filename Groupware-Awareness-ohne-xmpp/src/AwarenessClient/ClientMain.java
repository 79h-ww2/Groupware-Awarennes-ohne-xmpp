package AwarenessClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

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
			Scanner antwort = new Scanner(client.getInputStream());
			
			//nimmt die Kontaktliste vom Server entgegen
			anfragen.println("get-kontaktlist#§xdagox");
			if (antwort.hasNextLine()){
				String wert = "";
				do{
					wert = antwort.nextLine();
				}while(wert.equals("§Ende§") == false);
			}	
			anfragen.println("quit");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(clientFenster, e.getMessage());
		}
	}
	
	
	
}
