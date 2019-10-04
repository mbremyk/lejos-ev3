import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.Keys;
import lejos.utility.Delay;

/**
 *	EV3GUI.java
 *	Enkel GUI-klasse for å vise tekst på skjermen til en EV3,
 *	med noen ekstra ikke-grafiske metoder for å lese knappetrykk og vente en stund.
 *	
 *	@author Tore Bergebakken
 */

public class EV3GUI {

	private TextLCD lcd;
	private Keys keys;
	public static final byte X_LIMIT = 16;
	public static final byte Y_LIMIT = 7;

	public EV3GUI() {
		EV3 ev3 = (EV3)BrickFinder.getLocal();
		lcd = ev3.getTextLCD();
		keys = ev3.getKeys();
	}

	public EV3GUI(EV3API api) {
		EV3 ev3 = api.ev3;
		lcd = ev3.getTextLCD();
		keys = ev3.getKeys();
	}

	/* Rense grums på skjermen */
	public void clear() {
		lcd.clear();
	}

	/* Skrive på spesifikk posisjon */
	public void write(String str, int x, int y) {
		if(x > X_LIMIT) x = X_LIMIT;
		if(y > Y_LIMIT) y = Y_LIMIT;
		lcd.drawString(str, x, y);
	}

	/* Skrive på spesifikk linje */
	public void write(String str, int y) {
		if(y > Y_LIMIT) y = Y_LIMIT;
		lcd.drawString(str, 0, y);
	}

	/* Vise masse tekst, mottas som ferdig liste */
	public int displayList(String[] list) {
		clear();
		for (int i = 0; i < list.length; i++) { // letting the actual length of the array go in
			if (list[i] != null) {
				write(list[i], i);
			} else {
				write("FAULT", X_LIMIT - 5, i); // reporting about erraur
			}
		}

		if (list.length > Y_LIMIT + 1) {
			return Y_LIMIT;
		} else {
			return list.length; // next index
		}
	}

	/* metode for å vente på knappetrykk for å gå videre */
	public int waitForKeyPress() {
		int opt = keys.waitForAnyPress();
		wait(200);
		return opt;
	}

	public void wait(int msec) {
		Delay.msDelay(msec);
	}

	/* metode for å lese knappetrykk */
	public int readButtons() {
		return keys.readButtons();
	}

	/* check what key is pressed */
	public boolean isUp(int keypress) {
		return (keypress == Keys.ID_UP);
	}

	public boolean isDown(int keypress) {
		return (keypress == Keys.ID_DOWN);
	}

	public boolean isEnter(int keypress) {
		return (keypress == Keys.ID_ENTER);
	}

	public boolean isEscape(int keypress) {
		return (keypress == Keys.ID_ESCAPE);
	}
}