package AwarenessListFenster;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JViewport;

public class ClientFenster  extends JFrame{

	private JTextField txtStatusnachricht;
	private JComboBox<JLabel> comStatusSymbol;
	
	/**
	 * Konstruktor
	 */
	public ClientFenster(String Fenstertitel){
		super(Fenstertitel);	
		//designen des Fensters
		
		//Fensterabmaße angeben
		setSize(290, 450);
				
		//Festlegen, dass beim Schließen des Fensters das Programm beendet werden soll
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel cont = new JPanel();
		add(cont);
		cont.setLayout(new BorderLayout());
		cont.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		//Bereich, wo der Benutzer seine Awareness auswählen kann
		JPanel panelBereichAwarenessSetzung = new JPanel();
		
		cont.add(panelBereichAwarenessSetzung, BorderLayout.SOUTH);
		panelBereichAwarenessSetzung.setLayout(new BorderLayout(10,5));
		panelBereichAwarenessSetzung.setVisible(true);
		
		//Bereich: Textfeld für die Statusnachricht
		JPanel panelStatusMeldung = new JPanel();
		panelBereichAwarenessSetzung.add(panelStatusMeldung, BorderLayout.NORTH);
		panelStatusMeldung.setLayout(new BorderLayout(10,10));
		panelStatusMeldung.add(new JLabel("Statusmeldung:"), BorderLayout.WEST);
		txtStatusnachricht = new JTextField();
		panelStatusMeldung.add(txtStatusnachricht, BorderLayout.CENTER);
		txtStatusnachricht.setVisible(true);
		panelStatusMeldung.setVisible(true);
		
		//Vektor, der die Zeilen der Combo-Box enthält, wo der Status ausgewählt werden kann
		Vector<JLabel> comboEintraegeStatus =  new Vector<JLabel>();
		String[] zeileIcons = {"online.png", "abwsend.png", "laenger_abwesend.png", "beschaeftigt.png"};
		String[] zeileBeschriftung = {"Online", "Abwesend", "Länger Abwesend", "Beschäftigt"};
		
		for( int i = 0; i < zeileIcons.length; i++){
			//bestimmt die Adresse des Icons
			String adresseIcon = System.getProperty("user.dir") + "/bilder/" + zeileIcons[i];
			
			//System.out.println(adresseIcon.getPath());
			JLabel tempLabel = new JLabel();
			
			//lädt das Icon in die Combobox-Zeile
			try{
				File f = new File(adresseIcon);
				System.out.println(f.getAbsolutePath());
				ImageIcon icon = new ImageIcon(ImageIO.read(f));
				tempLabel.setIcon(icon);
			}catch (IOException e) {
				System.out.println(e);
			}
			//fügt zur Combobox-Zeile einen Text hinzu
			tempLabel.setText(zeileBeschriftung[i]);
			
			//fügt die Zeile zum Vektor hinzu
			comboEintraegeStatus.add(tempLabel);
		}
		
		//ComboBox, wo das Statussymbol ausgewählt wird
		comStatusSymbol = new JComboBox<>(comboEintraegeStatus);
		panelBereichAwarenessSetzung.add(comStatusSymbol,BorderLayout.SOUTH);
		comStatusSymbol.setRenderer(new ZeileComboBox<JLabel>());
		comStatusSymbol.setVisible(true);
		
		
		
		//Fenster anzeigen
		cont.setVisible(true);
		setVisible(true);
	}

}
