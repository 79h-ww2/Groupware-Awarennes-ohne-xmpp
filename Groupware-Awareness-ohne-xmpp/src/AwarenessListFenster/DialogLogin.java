package AwarenessListFenster;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * In diesem Dialog kann sich der Benutzer anmelden oder registieren
 * @author Benedikt Brüntrup
 *
 */
public class DialogLogin extends JDialog {
	
	protected JTextField txtBenutzername;
	protected JPasswordField txtPasswort;
	protected JButton btnAbbrechen, btnLogin, btnRegistieren;
	private boolean geschlossendurchButton;
	
	public DialogLogin(Window parent){
		super(parent,"Login");
		
		geschlossendurchButton = false;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel content = new JPanel();
		add(content);
		content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		content.setLayout(new BorderLayout(5,5));
		
		//Textbeschriftung
		JPanel panelBeschriftung = new JPanel(new GridLayout(2, 1));
		content.add(panelBeschriftung, BorderLayout.WEST);
		panelBeschriftung.add(new JLabel("Benutzername:"));
		panelBeschriftung.add(new JLabel("Passwort:"));
		
		//Textfelder
		JPanel panelTextfelder = new JPanel(new GridLayout(2, 1,5,5));
		content.add(panelTextfelder, BorderLayout.CENTER);
		panelTextfelder.add( txtBenutzername = new JTextField());
		panelTextfelder.add( txtPasswort = new JPasswordField());
		
		//Buttonbereich
		JPanel panelButtonRechtsplatzieren = new JPanel(new BorderLayout(5, 5));
		content.add(panelButtonRechtsplatzieren, BorderLayout.SOUTH);
		JPanel panelButtonBereich = new JPanel(new GridLayout(1, 3,5,5));
		panelButtonRechtsplatzieren.add(panelButtonBereich, BorderLayout.EAST);
		panelButtonBereich.add(btnLogin = new JButton("Login"));
		panelButtonBereich.add(btnRegistieren = new JButton("Registrieren"));
		panelButtonBereich.add(btnRegistieren = new JButton("Abbrechen"));
		panelButtonBereich.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
				
		
		panelBeschriftung.setVisible(true);
		setSize(450, 150);
		setResizable(false);
		content.setVisible(true);
		setVisible(true);
		
		addWindowListener(new DialogSchliessListener());
	}
	
	/**
	 * fügt zum Login-Button einen ActionListener hinzu
	 * @param l Der ActionListener
	 */
	public void addActionListenerLoginButton(ActionListener l){
		btnLogin.addActionListener(l);
	}
	
	/**
	 * fügt zum Abbrechen-Button einen ActionListener hinzu
	 * @param l Der ActionListener
	 */
	public void addActionListenerAbbrechenButton(ActionListener l){
		btnAbbrechen.addActionListener(l);
	}
	
	/**
	 * fügt zum Registierungs-Button einen ActionListener hinzu
	 * @param l Der ActionListener
	 */
	public void addActionListenerRegButton(ActionListener l){
		btnRegistieren.addActionListener(l);
	}
	
	/**
	 * Dieser Listener schließt das Komplette Programm, wenn das Dialog mit den Kreuz geschlossen wurde
	 * @author Benedikt Brüntrup
	 *
	 */
	private class DialogSchliessListener extends WindowAdapter{

		public void windowClosed(WindowEvent e) {
			super.windowClosed(e);
			
			//Wenn das Dialog mit dem Kreuz geschlossen wurde, wird das komplette Programm beendet
			if (!geschlossendurchButton) System.exit(0);
		}
		
	};
}
