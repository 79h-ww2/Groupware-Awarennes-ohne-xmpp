package AwarenessServer;

import java.sql.*;
/**
 * Diese Klasse ist für den Datenbankzugriff zuständig.
 * In der Datenbank sind die awareness-Informationen der einzelnen Clients gespeichert
 * @author Benedikt Brüntrup
 *
 */
public class Datenbankzugriff {
	
	private Connection con;

	/**
	 * Konstruktor der Klasse.
	 * Hier wird eine Verbindung zur Datenbank aufgebaut, falls noch nicht vorhanden die benötigten Tabellen erstellt.
	 */
	public Datenbankzugriff() {
		try{
			Class.forName("org.sqlite.JDBC");
			
			//Versucht eine Verbindung zur SQLite-Datenbank aufzubauen
			con = DriverManager.getConnection("jdbc:sqlite:awarenessList.db");
			
			//erstellt die Tabellen der Datenbank, wenn diese noch nicht exisiteren
			erstelleTabellen();
		
		//wirft eine Fehlermeldung, wenn bei der Datenbankverbindung ein Fehler aufgetreten ist
		}catch(Exception e){
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * erstellt die nötigen Tabellen in der Datenbank, falls sie noch nicht exisiteren
	 */
	private void erstelleTabellen(){		
		try {
			//erstellt die Benutzer-Tabelle
			String dbQuery= "CREATE TABLE IF NOT EXISTS benutzer ("
					+ "bNr INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "benutzername VARCHAR(45) NOT NULL,"
					+ "statusnachricht text,"
					+ "statussymbol VARCHAR(4),"
					+ "status_aktuell_seit INTEGER," 
					+ "passwort VARCHAR(50) NOT NULL,"
					+ "online TINYINT(1));";
			
			con.createStatement().executeUpdate(dbQuery);
			
			//erstellt die Kontaklisten-Tabelle
			dbQuery= "CREATE TABLE IF NOT EXISTS kontaktliste ("
					+ "kId INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "besitzer INTEGER,"
					+ "kontakt INTEGER,"
					+ "foreign key (besitzer) references benutzer(bNr),"
					+ "foreign key (kontakt) references benutzer(bNr))";
			
			con.createStatement().executeUpdate(dbQuery);
			
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * bestimmt dies Id eines Benutzers
	 * @param benutzername Der zu überprüfene Benutzername
	 * @return Die ID des Benutzer, wenn nichts gefunden -1
	 */
	public int bestimmeBNrBenutzer(String benutzername){
		int id = -1;
		ResultSet rueckgabewert = null;
		try{
			//sendet eine Datenbnankabfrage zur Ermittelung, ob es den Benutzer schon gibt
			String query ="select bNr from benutzer where benutzername = ?";
			PreparedStatement anweisung = con.prepareStatement(query);
			anweisung.setString(1, benutzername);
			rueckgabewert = anweisung.executeQuery();
			
			//werdet den Rückgabewert aus
			while(rueckgabewert.next()){
				id = rueckgabewert.getInt(1);
			}
				
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
		return id;
	}
	
	/**
	 * Fragt vom Server die Kontaktliste des übergebenen Benutzers ab
	 * @param benutzer Der Benutzer von den die Kontaktliste geladen werden soll
	 * @return Die Kontaktliste
	 */
	public ResultSet get_kontaktliste(String benutzer){
		
		ResultSet rueckgabewert = null;
		try{
			String query = "select  k.benutzername, k.online, k.statusnachricht, k.statussymbol "
					+ "from "
					+ "kontaktliste, benutzer AS k, benutzer AS b "
					+ "where b.benutzername = ? and b.bNr = kontaktliste.besitzer and k.bNr = kontaktliste.kontakt;";
			
			PreparedStatement anweisung = con.prepareStatement(query);
			anweisung.setString(1, benutzer);
			rueckgabewert = anweisung.executeQuery();
			
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
		return rueckgabewert;
	}
	
	/**
	 * Lädt die Kontaktlistenänderung seid einer Uhrzeit
	 * @param benutzer Der Benutzer von den die Kontaktliste geladen werden soll
	 * @param zeitpunkt Zeitpunkt, der letzten Änderung des Clients
	 * @return Die Kontaktliste
	 */
	public ResultSet get_kontaktliste_update(String benutzer, long zeitpunkt){
		
		ResultSet rueckgabewert = null;
		try{
			String query = "select  k.benutzername, k.online, k.statusnachricht, k.statussymbol "
					+ "from "
					+ "kontaktliste, benutzer AS k, benutzer AS b "
					+ "where b.benutzername = ? and b.bNr = kontaktliste.besitzer and k.bNr = kontaktliste.kontakt and k.status_aktuell_seit > ?;";
			
			PreparedStatement anweisung = con.prepareStatement(query);
			anweisung.setString(1, benutzer);
			anweisung.setLong(2, zeitpunkt);
			rueckgabewert = anweisung.executeQuery();
			
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
		return rueckgabewert;
	}
	
	/**
	 * legt einen neuen Benutzer in der Datenbank an
	 * @param benutzername Benutzername des neuen Benutzers
	 * @param passwort Passwort des neuen Benutzers
	 */
	public void benutzerAnlegen(String benutzername, String passwort){
		try{
			String query ="insert into benutzer (benutzername, statussymbol, status_aktuell_seit, passwort, online)"
					+ "values(?, ?, ?, ?, ?);";
			PreparedStatement anweisung = con.prepareStatement(query);
			anweisung.setString(1, benutzername);
			anweisung.setString(2, "chat");
			anweisung.setLong(3, System.currentTimeMillis());
			anweisung.setString(4, passwort);
			anweisung.setBoolean(5, true);
			anweisung.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Schließt die Datenbankverbindung
	 */
	public void verbindungSchliessen(){
		try {
			if (con != null) con.close();
		} catch (SQLException e) {
		}
	}
	
	/**
	 * Überprüft die Login-Daten und gibt, wenn sie richtig sind die ID des Benutzers aus
	 * @param benutzername Der übergebene Benutzername
	 * @param passwort Das übergebene Passwort
	 * @return Die bestimmte Benutzer ID
	 */
	public int loginCheck (String benutzername, String passwort){
		int id = -1;
		ResultSet rueckgabewert = null;
		try{
			//sendet eine Datenbnankabfrage zur Ermittelung, ob es den Benutzer schon gibt
			String query ="select bNr from benutzer where benutzername = ? and passwort = ?";
			PreparedStatement anweisung = con.prepareStatement(query);
			anweisung.setString(1, benutzername);
			anweisung.setString(2, passwort);
			rueckgabewert = anweisung.executeQuery();
			
			//werdet den Rückgabewert aus
			while(rueckgabewert.next()){
				id = rueckgabewert.getInt(1);
			}
				
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
		return id;
	}
	
	/**
	 * fügt einen neuen Kontakt zur Kontaktliste hinzu
	 * @param besitzer Der Besitzer der Kontaktliste
	 * @param kontakt Der Kontakt, der hinzugefügt werden soll
	 */
	public void kontaktZurKontaktListeHinzufuegen(String besitzer, String kontakt){
		int kNr = bestimmeBNrBenutzer(kontakt);
		int bNr = bestimmeBNrBenutzer(besitzer);

		try{
			//überprüft, ob der Konktakt noch nicht in der Kontaktliste ist
			boolean gefunden = false;
			ResultSet rueckgabewert = null;
			String query1 ="select count(*) from kontaktliste where besitzer = ? and kontakt = ?";
			PreparedStatement anweisung1 = con.prepareStatement(query1);
			
			anweisung1.setString(1, besitzer);
			anweisung1.setString(1, kontakt);
			rueckgabewert = anweisung1.executeQuery();
			
			//werdet den Rückgabewert aus
			while(rueckgabewert.next()){
				gefunden = rueckgabewert.getBoolean(1);
			}
			
			if(!gefunden){
				//fügt den Kontakt zur Kontakliste hinzu
				String query ="insert into kontaktliste (besitzer, kontakt) values(?, ?);";
				PreparedStatement anweisung = con.prepareStatement(query);
				
				anweisung.setInt(1, bNr);
				anweisung.setInt(2, kNr);		
				anweisung.executeUpdate();
			}
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Änder den Online-Status eines Benutzers
	 * @param benutzer Der Benutzer
	 * @param online Sein nuer Online-Status
	 */
	public void aendereOnlineStatus(String benutzer, boolean online){

		try{
			String query ="update benutzer set online = ?, status_aktuell_seit = ?, statussymbol = ? where benutzername = ?";
			PreparedStatement anweisung = con.prepareStatement(query);
			
			anweisung.setBoolean(1, online);
			anweisung.setLong(2, System.currentTimeMillis());
			anweisung.setString(3, "chat");
			anweisung.setString(4, benutzer);
	
			anweisung.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Änder das Status-Symbol des Benutzers
	 * @param benutzer Der Benutzer
	 * @param symbol Das neue Statussymbol
	 */
	public void aendereStatusSymbol(String benutzer, String symbol){

		try{
			String query ="update benutzer set status_aktuell_seit = ?, statussymbol = ? where benutzername = ?";
			PreparedStatement anweisung = con.prepareStatement(query);
			
			anweisung.setLong(1, System.currentTimeMillis());
			anweisung.setString(2, symbol);
			anweisung.setString(3, benutzer);
			anweisung.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Änder die Statusnachricht des Benutzers
	 * @param benutzer Der Benutzer
	 * @param nachricht Die neue Statusnachricht
	 */
	public void aendereStatusnachricht(String benutzer, String nachricht){
		
		try{
			String query ="update benutzer set status_aktuell_seit = ?, statusnachricht = ? where benutzername = ?";
			PreparedStatement anweisung = con.prepareStatement(query);
			
			anweisung.setLong(1, System.currentTimeMillis());
			anweisung.setString(2, nachricht);
			anweisung.setString(3, benutzer);
			anweisung.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Lädt die Statusnachricht des Benutzers
	 * @param benutzer Der Benutzer
	 * @param nachricht Die neue Statusnachricht
	 */
	public String getStatusnachricht(String benutzer){

		String status = "";
		ResultSet rueckgabewert = null;
		try{
			String query ="select statusnachricht from benutzer where benutzername = ?";
			PreparedStatement anweisung = con.prepareStatement(query);
			
			anweisung.setString(1, benutzer);
			rueckgabewert = anweisung.executeQuery();
			
			//werdet den Rückgabewert aus
			while(rueckgabewert.next()){
				status = rueckgabewert.getString(1);
			}
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
		
		return status;
	}

}
