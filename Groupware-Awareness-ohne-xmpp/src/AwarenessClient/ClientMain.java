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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.security.auth.login.LoginContext;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import AwarenessListFenster.ClientFenster;



public class ClientMain extends ClientFenster{
	
	private boolean verbindungGeschlossen;
	private Socket client;
	private PrintStream anfragen;
	private Scanner antwort;
	
	/**
	 * Konstruktor des Clients
	 */
	public ClientMain(){
		super("Awareness-Liste ohne XMPP");
		
		
		//TCP-Verbindung zum Server wird aufgebaut
		try {
			client = new Socket("127.0.0.1", 12345);
			verbindungGeschlossen = false;
			
			//Streams für den Datenin- und -output werden definiert
			anfragen = new PrintStream(client.getOutputStream());
			antwort = new Scanner(client.getInputStream());
			
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage());
			System.exit(1);
		}
		
		
		//Listener zur Combo-Box hinzufügen, der reagiert, wenn eine anderer Status ausgewählt wurde
		comStatusSymbol.addItemListener(new statusSymbolListener());
		
		//Listener, der beim Schließen des Fenster die TCP-Verbindung trennt
		addWindowListener(new fensterSchliessenListener());	
		
		//Listener, die einen Button-Klick beim Loginfenster entgegennehmen
		login.addActionListenerButtons(new buttonKlickListener());
		
		//Listener, der darauf lauscht, ob beim Menü ausgewählt wurde, dass eine neuer Kontakt zur Kontaktliste hinzugefügt werden soll
		kontaktHinzufuegen.addActionListener(new buttonKlickListener());
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

		public void windowClosing(WindowEvent e) {
			super.windowClosing(e);
			try {
				anfragen.println("quit");
				anfragen.close();
				antwort.close();
				client.close();
			} catch (Exception e1) {
			}
		}
	}
	
	/**
	 * Listener, der darauf wartet, dass ein Button geklickt wird	
	 */
	private class buttonKlickListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			
			/*
			 * Benutzer hat den Registieren-Button geklickt
			 */
			if(e.getActionCommand().equals("Registrieren")){
				
				//sendet zum Server eine Anfrage, zum Anlegen eines neuen Benutzers
				if (neuenBenutzerAnlegen(login.getBenutzername(), login.getPasswort())){
					//wenn das Anlegen erfolgreich war
					
					//Kennzeichnet, dass das Login-Dialog mit einen Button geschlossen wurde
					login.setGeschlossendurchButton(true);
					login.dispose(); //Schließt das Anmeldedialog
					setVisible(true); //zeigt das Awareness-Dialog an
				}
				//wenn der Benutzer schon exisiterte
				else{
					JOptionPane.showMessageDialog(null, "Der Benutzername ist schon bereits belegt", "Benutzeranlegen fehlgeschlagen", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			
			/*
			 * Benutzer hat den Login-Button geklickt, um sich anzumelden
			 */
			else if (e.getActionCommand().equals("Login")){
				
				if (loginCheck(login.getBenutzername(), login.getPasswort()) != -1){
					//wenn die Login-Daten richtig waren
					
					//Kennzeichnet, dass das Login-Dialog mit einen Button geschlossen wurde
					login.setGeschlossendurchButton(true);
					login.dispose(); //Schließt das Anmeldedialog
					setVisible(true); //zeigt das Awareness-Dialog an
				}
				//Fehlermeldung, wenn die Login-Daten falsch waren
				else{
					JOptionPane.showMessageDialog(null, "Die Login-Daten sind falsch. Bitte versuchen Sie es nocheinmal", "Login fehlgeschlagen", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			
			/*
			 * Benutzer möchte einen neuen Kontakt zur Kontaktliste hinzufügen
			 */
			else if (e.getActionCommand().equals("Kontakt hinzufügen")){
				if (kontaktZurKontaktlisteHinzufuegen()){
					JOptionPane.showMessageDialog(null, "Der Kontakt wurde erfolgreich zur Kontaktliste hinzugefügt.", "Hinzufügen erfolgreich", JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(null, "Der kontakt exisiert nicht.", "Hinzufügen Fehlgeschlagen", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
	}
	
	/**
	 * Dieses Methode sendet eine Anfrage an den Server zum Anlegen eines neuen Benutzers
	 * @param benutzer Benutzername
	 * @param passwort Passowort
	 */
	public boolean neuenBenutzerAnlegen(String benutzer, char[] passwort){
		
		//#§ ist das Trennzeichen zwischen den Parametern
		String server_antwort_text = "";
		try {
			//Verschlüsseln des Passworts durch ein MD5-Hash
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			String passwort_verschluesselt =  new String(md5.digest(String.valueOf(passwort).getBytes()));
			
			//erstellt die Serveranfrage
			String anfrage_text = "set-neuer-benutzer#§" + benutzer +"#§" + passwort_verschluesselt;
			
			//sendet die Anfrage an den Server
			anfragen.println(anfrage_text);
			
			//nimmt die Serverantwort entgegen
			if (antwort.hasNextLine()){
				String wert = "";
				do{
					wert = antwort.nextLine();
					if (wert.equals("§Ende§") == false) server_antwort_text = wert;
				}while(wert.equals("§Ende§") == false);
			}
							
		} catch (NoSuchAlgorithmException e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
		
		return server_antwort_text.equals("ok");
	}
	
	/**
	 * überprüft die Logindaten und wenn sie richtig sind die Id des Benutzers aus
	 * @param benutzername Der eingegeben Benutzername
	 * @param passwort Das eingegeben Passwort
	 * @return Die ID des Benutzers, bei falscher Eingabe -1
	 */
	public int loginCheck(String benutzername, char[] passwort){
		//#§ ist das Trennzeichen zwischen den Parametern
				String server_antwort_text = "";
				try {
					//Verschlüsseln des Passworts durch ein MD5-Hash
					MessageDigest md5 = MessageDigest.getInstance("MD5");
					String passwort_verschluesselt =  new String(md5.digest(String.valueOf(passwort).getBytes()));
					
					//erstellt die Serveranfrage
					String anfrage_text = "check-login#§" + benutzername +"#§" + passwort_verschluesselt;
					
					//sendet die Anfrage an den Server
					anfragen.println(anfrage_text);
					
					//nimmt die Serverantwort entgegen
					if (antwort.hasNextLine()){
						String wert = "";
						do{
							wert = antwort.nextLine();
							if (wert.equals("§Ende§") == false) server_antwort_text = wert;
						}while(wert.equals("§Ende§") == false);
					}
									
				} catch (NoSuchAlgorithmException e) {
					JOptionPane.showMessageDialog(this, e.getMessage());
				}
				
				int id = Integer.valueOf(server_antwort_text);
				return id;
	}
	
	/**
	 * Zeigt ein Input-Feld an, womit der Benutzer einen neuen Kontakt zur Kontaktliste hinzufügen kann.
	 * Sendet, die Hinzufügeanfrage an den Server
	 * @return Gibt aus, opb das Hinzufügen erfolgreich war
	 */
	public boolean kontaktZurKontaktlisteHinzufuegen(){
		String benutzer = login.getBenutzername();
		String kontakt = JOptionPane.showInputDialog(this, "Bitte geben Sie den Kontaktnamen ein, der zur Kontaktliste hinzugefügt werden soll.");
		
		String server_antwort_text = "";
		
		//erstellt die Serveranfrage
		String anfrage_text = "add-kontakt#§" + benutzer +"#§" + kontakt;
		
		//sendet die Anfrage an den Server
		anfragen.println(anfrage_text);
		
		//nimmt die Serverantwort entgegen
		if (antwort.hasNextLine()){
			String wert = "";
			do{
				wert = antwort.nextLine();
				if (wert.equals("§Ende§") == false) server_antwort_text = wert;
			}while(wert.equals("§Ende§") == false);
		}
		
		return server_antwort_text.equals("ok");
	}
	
}
