package AwarenessListFenster;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;

/**
 * Listener, der aufgerufen wird, wenn das Statussymbol geändert wird
 * @author benedikt
 *
 */
public class StatusSymbolListerner implements ItemListener {

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
