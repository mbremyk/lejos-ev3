import lejos.hardware.BrickFinder; // for finding the ev3 object if the empty constructor is used
import lejos.hardware.ev3.EV3; // for finding necessary objects to control
import lejos.hardware.lcd.TextLCD; // for displaying text on the EV3's screen
import lejos.hardware.Button; // for handling keypresses and setting LED pattern
import lejos.utility.Delay; // to make the botto wait a little w/o needing exception handling
import static lejos.hardware.Button.waitForAnyPress;

/**
 *	EV3GUI.java
 *	Enkel GUI-klasse for å vise tekst på skjermen til en EV3,
 *	med noen ekstra ikke-grafiske metoder for å lese knappetrykk og vente en stund.
 *
 *	@author Tore Bergebakken
 */

public class EV3GUI {

    private TextLCD lcd;

    /** Max amounts, to avoid writing at invalid coordinates */
    public static final byte X_LIMIT = 16;
    public static final byte Y_LIMIT = 7;

    /** LED pattern codes and a variable for storing the current one */
    public static final byte NONE = 0;
    public static final byte GREEN = 1;
    public static final byte RED = 2;
    public static final byte ORANGE = 3;
    public static final byte FLICKER_GREEN = 4;
    public static final byte FLICKER_RED = 5;
    public static final byte FLICKER_ORANGE = 6;
    public static final byte BLINK_GREEN = 7;
    public static final byte BLINK_RED = 8;
    public static final byte BLINK_ORANGE = 9;
    private byte currentLED = 0;

    public EV3GUI() {
        EV3 ev3 = (EV3)BrickFinder.getLocal();
        lcd = ev3.getTextLCD();
    }

    public EV3GUI(EV3API api) {
        EV3 ev3 = api.ev3;
        lcd = ev3.getTextLCD();
    }

    /**
     * Endre farge og blinkemodus til LED-lyset på knappene;
     * parameteren er en av de statiske kodene definert ovenfor.
     */
    public void setLight(byte pattern) {
        Button.LEDPattern(pattern);
        currentLED = pattern; // saves current pattern
    }

    public void setLight(byte pattern, int msec) {
        Button.LEDPattern(pattern);
        wait(msec);
        Button.LEDPattern(currentLED); // restores previous pattern
    }

    /** Rense grums på skjermen */
    public void clear() {
        lcd.clear();
    }

    /** Skrive på spesifikk posisjon */
    public void write(String str, int x, int y) {
        if(x > X_LIMIT) x = X_LIMIT;
        if(y > Y_LIMIT) y = Y_LIMIT;
        lcd.drawString(str, x, y);
    }

    /** Skrive på spesifikk linje */
    public void write(String str, int y) {
        if(y > Y_LIMIT) y = Y_LIMIT;
        lcd.drawString(str, 0, y);
    }

    /** Vise masse tekst, mottas som ferdig liste */
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

    /** metode for å vente på knappetrykk for å gå videre */
    public int waitForKeyPress() {
        int opt = waitForAnyPress();
        wait(200);
        return opt;
    }

    public void wait(int msec) {
        Delay.msDelay(msec);
    }

    /** metode for å lese knappetrykk */
    public int readButtons() {
        return Button.readButtons();
    }

    /** metoder for å sjekke hvilken spesifikk knapp som ble trykt */
    public boolean isUp(int keypress) {
        return (keypress == Button.ID_UP);
    }

    public boolean isDown(int keypress) {
        return (keypress == Button.ID_DOWN);
    }

    public boolean isEnter(int keypress) {
        return (keypress == Button.ID_ENTER);
    }

    public boolean isEscape(int keypress) {
        return (keypress == Button.ID_ESCAPE);
    }
}