package AwarenessClient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import AwarenessListFenster.ClientFenster;



public class ClientMain extends ClientFenster{
	
	private boolean verbindungGeschlossen;
	private Socket client;
	private PrintStream anfragen;
	private Scanner antwort;
	
	public ClientMain(){
		super("Awareness-Liste ohne XMPP");
		
		
		//TCP-Verbindung zum Server wird aufgebaut
		try {
			client = new Socket("127.0.0.1", 12345);
			verbindungGeschlossen = false;
			
			//Streams für den Datenin- und -output werden definiert
			anfragen = new PrintStream(client.getOutputStream());
			antwort = new Scanner(client.getInputStream());
			
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
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		
		
		//Listener zur Combo-Box hinzufügen, der reagiert, wenn eine anderer Status ausgewählt wurde
		comStatusSymbol.addItemListener(new statusSymbolListener());
		
		//Listener, der beim Schließen des Fenster die TCP-Verbindung trennt
		addWindowListener(new fensterSchliessenListener());	
	}
		

	/**
	 * Programmeinstiegspunkt
	 * @param args
	 */
	public static void main(String[] args) {
		ClientMain clientFenster = new ClientMain();
	}
	
	/**
	 * Dieser Listener warte, bis eine anderen Statussymbol ausgewählt wird
	 * @author Benedikt Brüntrup
	 */
	private class statusSymbolListener implements ItemListener{
		
		/**
		 * Bestimmt welches Statussymbol ausgewählt wurde
		 */
		public void itemStateChanged(ItemEvent e) {
			if ( e.getStateChange() == ItemEvent.SELECTED){
				JLabel auswahl = (JLabel)e.getItem();
				System.out.println(auswahl.getText());
			}
		}
	}
	
	/**
	 * Listener, der beim Schließen des Fenster die TCP-Verbindung trennt
	 * @author Benedikt Brüntrup
	 */
	private class fensterSchliessenListener extends WindowAdapter{
		public void windowClosed(WindowEvent e) {
			super.windowClosed(e);
			if (client.isClosed() == false){
				try {
					anfragen.close();
					antwort.close();
					client.close();
				} catch (IOException e1) {
				}
			}
		}
	}
	
	/**
	 * Listener, der darauf wartet, dass ein Button geklickt wird	
	 */
	private class buttonKlickListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
		}
		
	}
	
	
}
