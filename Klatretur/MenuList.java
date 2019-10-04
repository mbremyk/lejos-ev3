/**
 *	MenuList.java
 *	Har ei liste over menyinnhold og holder styr på hvor vi er i menyen.
 *	Gir tabell med menyteksten formatert for oppgitt skjerm- eller generell listehøyde.
 *	Immutabel bortsett fra currentEntry.
 *
 *	Vurderer dyp kopiering som unødvendig og heller ganske uheldig i vårt tilfelle,
 *	siden 
 *	Dette gjør hver relasjon til og fra klassene som er koblet direkte til denne til
 *	aggregeringer, ikke komposisjoner - unntatt for emptyEntry,
 *										den er en komponent i alle listeobjekt.
 *	
 *	@author Tore Bergebakken
 */

public class MenuList {
	private final String title;
	private static final MenuEntry emptyEntry = new MenuEntry(); // for displaying empty text
	private final MenuEntry[] entries;
	private final byte numEntries;
	private byte currentEntry = 0;


	/* Full constructor */
	public MenuList(String title, MenuEntry[] entries) {
		/*
		byte entryCounter = 0;
		for (int i = 0; i < entries.length; i++) {
			if (entries[i] != null) {
				tempEntries[i] = entries[i];
				entryCounter++;
			}
		} // should I copy deeply tho? It takes resources... INDEED.
		numEntries = entryCounter
		this.entries = new MenuEntry[numEntries];
		int j = 0;
		for (int i = 0; i < tempEntries.length; i++) {
			if (tempEntries[i] != null && j < numEntries) {
				this.entries[j] = tempEntries[i];
				j++;
			}
		}
		*/
		// constructing the simple, cheap way
		this.entries = entries;
		numEntries = (byte)entries.length;
		this.title = title;
	}

	/* Tiny test constructor */
	public MenuList(MenuEntry[] entries) {
		this.entries = entries;
		numEntries = (byte)entries.length;
		title = "--Generic Menu";
	}

	/* Get methods for fetching */
	public boolean doesBranch() { // assumes current entry is selected - it must be.
		return entries[currentEntry].doesBranch();
	}

	public MenuList getBranch() { // same
		return entries[currentEntry].getBranch();
	}

	public byte getAction() {
		return entries[currentEntry].getAction();
	}

	/* Få ut tekst på indeks, hvis ugyldig gis en standardtekst. */
	private String getEntryText(int index) {
		if (index < 0 || index >= numEntries) {
			return emptyEntry.getText(); // or just "" but that's costly
		} else {
			return entries[index].getText(); //  the parent doesn't have a clue if the entry is valid
		}
	}
	/*
	 * y limit and y pos of where we want our target to be
	 * screenLim er skjermens maksimale y-posisjon
	 * selPos er y-verdi --> skal skrive ">" på nettopp den indeksen
	 */
	public String[] getEntryList(byte screenLimit, byte selectedPosition) {
		// retter opp evt gal selPos
		if (selectedPosition < 0) selectedPosition = 0;
		if (selectedPosition > screenLimit) selectedPosition = screenLimit;
		
		byte start = (byte) (currentEntry - selectedPosition);
		screenLimit++; // otherwise we'd have to manipulate two times.
		//System.out.println(title + "\n" + selectedPosition + " " + currentEntry + " " + start);

		String[] res = new String[screenLimit];
		res[0] = title; // derfor starter vi på i=1

		for (int i = 1; i < screenLimit; i++) {
			if (i == selectedPosition) { // at that exact y pos
				res[i] = "> " + getEntryText(start + i); // in initial case: i=3, index -3+3=0
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

	/* Alternativ input, trengs ikke for øyeblikket men hadde vært gode å ha */
	public String[] getEntryList(byte screenLimit, int selectedPosition) {
		return getEntryList(screenLimit, (byte) selectedPosition);
	}

	public String[] getEntryList(int screenLimit, int selectedPosition) {
		return getEntryList((byte) screenLimit, (byte) selectedPosition);
	}

	/*
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