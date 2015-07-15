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
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import javax.security.auth.login.LoginContext;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import AwarenessListFenster.AwarenessListZeile;
import AwarenessListFenster.ClientFenster;



public class ClientMain extends ClientFenster{
	
	private boolean verbindungGeschlossen;
	private Socket client;
	private PrintStream anfragen;
	private Scanner antwort;
	private boolean serverLock;
	private boolean geradeGestartet;
	
	/**
	 * Konstruktor des Clients
	 */
	public ClientMain(/*String adresse*/){
		super("Awareness-Liste ohne XMPP");
		
		//Variabel, die den Thread pausiert, wenn auf andere Anfragen gewartet wird
		serverLock = false;
		geradeGestartet = true;
		
		
		//TCP-Verbindung zum Server wird aufgebaut
		try {
			String adresse = JOptionPane.showInputDialog(this, "Bitte geben Sie IP-Adresse des Servers an.");
			client = new Socket(adresse, 12345);
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
		
		//Listener, der auf eine Änderung im Statustextfeld lauscht
		txtStatusnachricht.getDocument().addDocumentListener(new StatusTextAenderungListener());
	}
		

	/**
	 * Programmeinstiegspunkt
	 * @param args
	 */
	public static void main(String[] args) {

		ClientMain clientFenster = new ClientMain();

	}
	
	/**
	 * Listener, der die Änderung der Statusnachricht an den Server überträgt
	 * @author Benedikt Brüntrup
	 *
	 */
	private class StatusTextAenderungListener implements DocumentListener{

		public void insertUpdate(DocumentEvent e) {
			textAenderung(e);;	
		}

		public void removeUpdate(DocumentEvent e) {
			textAenderung(e);
		}

		public void changedUpdate(DocumentEvent e) {
			textAenderung(e);	
		}
		
		public void textAenderung(DocumentEvent e){
			String text = txtStatusnachricht.getText().equals("") ? "null" : txtStatusnachricht.getText();
			anfragen.println("set-statusnachricht#§" + login.getBenutzername() +"#§" + text );
		}
		
	}
	
	/**
	 * Dieser Listener warte, bis eine anderen Statussymbol ausgewählt wird
	 * @author Benedikt Brüntrup
	 */
	private class statusSymbolListener implements ItemListener{
		
		//
		/**
		 * Sendet zum Server das neue Statussymbol
		 */
		public void itemStateChanged(ItemEvent e) {
			if ( e.getStateChange() == ItemEvent.SELECTED){
				JLabel auswahl = (JLabel)e.getItem();
				String[] zeileBeschriftung = {"Online", "Abwesend", "Laenger Abwesend", "Beschaeftigt"};
				String[] wertAnfrage = {"chat", "away", "xa", "dnd"};
				
				for(int i = 0; i < zeileBeschriftung.length; i++){
					if (zeileBeschriftung[i].equals(auswahl.getText())){
						while(serverLock && !verbindungGeschlossen);
						anfragen.println("set-symbol#§" + login.getBenutzername() +"#§" + wertAnfrage[i] );
					}
				}
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
				verbindungGeschlossen = true;
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
					
					//startet einen Thread, der die Kontaktliste durchgehend lädt
					new AwarenessCheck();
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
					
					//empfängt vom Server die aktuelle Stautsnachricht
					
					//wartet, bis die Leitung frei ist
					while(serverLock && !verbindungGeschlossen);
					
					//empfängt vom Server die aktuelle Stautsnachricht
					serverLock = true;
					anfragen.println("get-statusnachricht#§" + login.getBenutzername());
					String server_antwort_text = "";
					//nimmt die Serverantwort entgegen
					if (antwort.hasNextLine()){
						String wert = "";
						do{
							wert = antwort.nextLine();
							if (wert.equals("§Ende§") == false) server_antwort_text = wert;
						}while(wert.equals("§Ende§") == false);
					}
					serverLock = false;
					
					//zeigt die Statusnachricht im Textfeld an
					server_antwort_text = server_antwort_text.equals("null") ? "" : server_antwort_text;
					txtStatusnachricht.setText(server_antwort_text);
					
					
					//startet einen Thread, der die Kontaktliste durchgehend lädt
					new AwarenessCheck();
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
			
			//wartet, bis die Leitung frei ist
			while(serverLock && !verbindungGeschlossen);
			
			//sendet die Anfrage an den Server
			serverLock = true;
			anfragen.println(anfrage_text);
			
			//nimmt die Serverantwort entgegen
			if (antwort.hasNextLine()){
				String wert = "";
				do{
					wert = antwort.nextLine();
					if (wert.equals("§Ende§") == false) server_antwort_text = wert;
				}while(wert.equals("§Ende§") == false);
			}
			serverLock = false;
							
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
					
					//wartet, bis die Leitung frei ist
					while(serverLock && !verbindungGeschlossen);
					
					//sendet die Anfrage an den Server
					serverLock = true;
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
				serverLock = false;
				
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
		
		//wartet, bis die Leitung frei ist
		while(serverLock && !verbindungGeschlossen);
		
		serverLock = true;
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
		geradeGestartet = true;
		serverLock = false;
		
		return server_antwort_text.equals("ok");
	}
	
	/**
	 * Dieses Klasse überprüft durchgehend, ob sich die Awareness-List geändert hat
	 * @author Benedikt Brüntrup
	 */
	private class AwarenessCheck implements Runnable{
		
		public AwarenessCheck(){
			Thread t1= new Thread(this);
			t1.start();
		}

		public void run() {
					
			//dieses Variable enthält die Uhrzeit, wann das letzte Mal die Kontaktliste neugeladen wurde
			long zeit_letzte_aenderung = 0;
			
			//Kontaktliste im Fenster angezeigt
			Vector<AwarenessListZeile> kontaktliste_fenster = new Vector<AwarenessListZeile>();
			
			while (!verbindungGeschlossen){
				
				while(serverLock && !verbindungGeschlossen);
								
				String anfrage_text = "";
				
				//wenn der Client gerade gestartet wurde, wird die komplette Kontaktliste geladen
				if (geradeGestartet == true ){
					//erstellt die Serveranfrage
					anfrage_text = "get-kontaktlist#§" + login.getBenutzername();
				}
				//sonst nur die Änderungen
				else{
					anfrage_text = "get-aenderung-kontaktlist#§" + login.getBenutzername() +"#§" + zeit_letzte_aenderung;
				}
				
				//sendet die Anfrage an den Server
				serverLock = true;
				anfragen.println(anfrage_text);
				
				//Vektor, der die Server-Antwort zwischenspeichert
				Vector<AwarenessListZeile> zeilenServerAntwort = new Vector<AwarenessListZeile>();
				
				//Namenliste mit den Icons der Status-Symbole
				HashMap<String, String> icons = new HashMap<>();
				icons.put("chat", "online.png");
				icons.put("null", "online.png");
				icons.put("away", "abwsend.png");
				icons.put("xa", "laenger_abwesend.png");
				icons.put("dnd", "beschaeftigt.png");
				icons.put("off", "offline.png");
				String pfadZumIcon = System.getProperty("user.dir") + "/bilder/";
				
				//nimmt die Serverantwort entgegen
				if (antwort.hasNextLine()){
					String wert = "";
					boolean farbwechsel = false;
					do{
						wert = antwort.nextLine();
						if (wert.equals("§Ende§") == false){
							
							//eine Zeile aus der Kontaktliste
							//fügt dieses zum Vektor hinzu
							String[] werte = wert.split("#§");
							
							//legt das angezeigte Icon fest
							String adresseSymbol = "";
							if (werte[1].equals("false")){
								//Benutzer wird als offline angezeigt
								adresseSymbol = pfadZumIcon + icons.get("off");
							}else{
								adresseSymbol = pfadZumIcon + icons.get(werte[3]);
							}
							
							String statusmeldung = "";
							//Wenn der Kontakt keine Statusnachricht angegeben hat wird einfach online angezeigt
							if ( werte[2].equals("null") && werte[1].equals("true")){
								statusmeldung = werte[2].equals("null") ? "online" : werte[2];
							}else if (werte[2].equals("null") && werte[1].equals("false")){
								statusmeldung = werte[2].equals("null") ? "offline" : werte[2];
							}
							
							statusmeldung = werte[2].equals("null") ? "online" : werte[2];
							
							//speichert die Zeile im Vektor
							AwarenessListZeile zeile = new AwarenessListZeile(werte[0], statusmeldung , new ImageIcon(adresseSymbol), farbwechsel);
							zeilenServerAntwort.add(zeile);
							
							farbwechsel = !farbwechsel;
						}
					}while(wert.equals("§Ende§") == false);
				}
				serverLock = false;
				
				//speichert die Uhrzeit, die letztw Kontaktliste geladen wurde
				zeit_letzte_aenderung = System.currentTimeMillis();
				
				//zeigt die neue Kontaktliste an
				
				//lädt die komplette Kontaktliste ins Fenster
				if (geradeGestartet == true ){
					kontaktliste_fenster = zeilenServerAntwort;
					awarenessListe.setListData(kontaktliste_fenster);
					awarenessListe.repaint();
					geradeGestartet = false;
				}
				//updatest die bestehende Liste
				else{
					boolean warenAenderungen = zeilenServerAntwort.size() > 0;
					
					for (int i = 0; i < kontaktliste_fenster.size() && zeilenServerAntwort.size() > 0; i++){
						AwarenessListZeile zeile_alt = kontaktliste_fenster.get(i);
						boolean schon_gefunden = false;
						for (int j = 0; j < zeilenServerAntwort.size() && !schon_gefunden; j++){
							AwarenessListZeile zeile_neu = zeilenServerAntwort.get(j);
							if (zeile_alt.getBenutzername().equals(zeile_neu.getBenutzername())){
								kontaktliste_fenster.set(j, zeile_neu);
								schon_gefunden = true;
								zeilenServerAntwort.remove(j);
							}
						}
					}
					//wenn die Liste sich verändert hat, wird sie angezeigt
					if (warenAenderungen){
						awarenessListe.setListData(kontaktliste_fenster);
						awarenessListe.repaint();
					}
				}
				
				
				//lädt die Kobtaktliste alle 1 Sekunden neu	
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
		
	}
	
	
}
