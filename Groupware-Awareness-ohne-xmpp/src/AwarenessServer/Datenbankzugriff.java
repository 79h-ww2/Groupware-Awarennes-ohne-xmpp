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
					+ "statusnachricht VARCHAR(50),"
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

}
