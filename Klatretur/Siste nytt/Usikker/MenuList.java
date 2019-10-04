/**
 *	MenuList.java
 *	Har ei liste over menyinnhold og holder styr på hvor vi er i menyen.
 *	Gir tabell med menyteksten formatert for oppgitt skjermhøyde eller generell listelengde.
 *	Immutabel bortsett fra currentEntry.
 *
 *	@author Tore Bergebakken
 */

public class MenuList {

    private final MenuEntry[] entries;
    private final byte numEntries;
    private byte currentEntry = 0;

    private final String title;
    private final String marker;
    private final String emptyString;

    /** Full constructors, the first one is never used since we use the defaults for those two */
    public MenuList(String title, MenuEntry[] entries, String marker, String emptyString) {
        // copying the simple, cheap way
        this.entries = entries;
        numEntries = (byte)entries.length;
        this.title = title;
        this.marker = marker;
        this.emptyString = emptyString;
    }

    public MenuList(String title, MenuEntry[] entries) {
        // copying the simple, cheap way
        this.entries = entries;
        numEntries = (byte)entries.length;
        this.title = title;
        marker = "> ";
        emptyString = "...";
    }

    /** Tiny test constructor */
    public MenuList(MenuEntry[] entries) {
        this.entries = entries;
        numEntries = (byte)entries.length;
        title = "--Generic Menu";
        marker = "> ";
        emptyString = "...";
    }

    /** Get methods for fetching */
    public boolean doesBranch() { // assumes current entry is selected - it must be.
        return entries[currentEntry].doesBranch();
    }

    public MenuList getBranch() { // same
        return entries[currentEntry].getBranch();
    }

    public byte getAction() { // indeed
        return entries[currentEntry].getAction();
    }

    /** Få ut tekst på indeks, hvis ugyldig gis en standardtekst. */
    private String getEntryText(int index) {
        if (index < 0 || index >= numEntries) {
            return emptyString;
        } else {
            return entries[index].getText(); // the parent doesn't have a clue if the entry is valid
        }
    }

    /**
     * y limit and y pos of where we want our target to be
     * screenLim er skjermens maksimale y-posisjon
     * selPos er y-verdi --> skal sette markøren på nettopp den indeksen
     */
    public String[] getEntryList(byte screenLimit, byte selectedPosition) {
        // retter opp evt gal selPos
        if (selectedPosition < 0) selectedPosition = 0;
        if (selectedPosition > screenLimit) selectedPosition = screenLimit;

        byte start = (byte) (currentEntry - selectedPosition);
        screenLimit++; // otherwise we'd have to manipulate two times.

        String[] res = new String[screenLimit];
        res[0] = title; // derfor starter vi på i=1

        for (int i = 1; i < screenLimit; i++) {
            if (i == selectedPosition) { // at that exact y pos
                res[i] = marker + getEntryText(start + i); // in initial case: i=3, index -3+3=0
            } else {
                res[i] = getEntryText(start + i);
                /*
                 * i=1, curEntry=0, selPos=3 (before start is set) gives index -2
                 * i=2 gives -1
                 * i=3 gives 0 --> first entry @ y pos 3 which is selPos
                 */
            }
        }
        return res;
    }

    /** Alternativ input, trengs ikke for øyeblikket men hadde vært gode å ha */
    public String[] getEntryList(byte screenLimit, int selectedPosition) {
        return getEntryList(screenLimit, (byte) selectedPosition);
    }

    public String[] getEntryList(int screenLimit, int selectedPosition) {
        return getEntryList((byte) screenLimit, (byte) selectedPosition);
    }

    /**
     * Methods for changing currentEntry
     * boolean for eye-/earcandy purposes
     */
    public boolean browseUp() { // decrement
        if (currentEntry > 0) {
            currentEntry--;
            return true;
        }
        return false;
    }

    public boolean browseDown() { // increment
        if (currentEntry + 1 < numEntries) {
            currentEntry++;
            return true;
        }
        return false;
    }
}