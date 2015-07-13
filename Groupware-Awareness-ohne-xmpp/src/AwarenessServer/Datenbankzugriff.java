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
	 * Konstruktor der Klasse
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
					+ "bNr INTEGER PRIMARY KEY NOT NULL,"
					+ "benutzername VARCHAR(45) NOT NULL,"
					+ "statusnachricht VARCHAR(50),"
					+ "statussymbol VARCHAR(4),"
					+ "status_aktuell_seit INTEGER," 
					+ "passwort VARCHAR(50) NOT NULL,"
					+ "online TINYINT(1));";
			
			con.createStatement().executeUpdate(dbQuery);
			
			//erstellt die Kontaklisten-Tabelle
			dbQuery= "CREATE TABLE IF NOT EXISTS kontaktliste ("
					+ "kId INTEGER PRIMARY KEY NOT NULL,"
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
	
	
	public ResultSet get_kontaktliste(String benutzer){
		
		try{
			String query = "select"
					+ "		k.benutzername,"
					+ "		k.online,"
					+ "		k.statusnachricht,"
					+ "		k.statussymbol"
					+ "from"
					+ "		kontaktliste "
					+ "			inner join benutzer as k on k.bNr = kontaktliste.kontakt"
					+ "			inner join benutzer as b on b.Bnr = kontaktliste.besitzer"
					+ "where"
					+ "		b.benutzername = ?";
			
			PreparedStatement anweisung = con.prepareStatement(query);
			anweisung.setString(1, benutzer);			
			
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(1);
		}
		
		return null;
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
