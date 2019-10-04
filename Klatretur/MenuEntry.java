/**
 *	MenuEntry.java
 *	Immutabel klasse for ei enkelt linje i en meny.
 *	branchPointer peker til et MenuList-objekt.
 *	actionPointer peker i vårt tilfelle til en case i en switch-setning.
 *	
 *	@author Tore Bergebakken
 */

public class MenuEntry {
	private final String text;
	private final boolean doesBranch; // for checking if it points to list or action
	private final MenuList branchPointer;
	private final byte actionPointer;

	/* CONSTRUCTION AREA */
	public MenuEntry(String text, MenuList branchPointer) {
		this.text = text;
		doesBranch = true;
		this.branchPointer = branchPointer;
		actionPointer = -1;
	}

	public MenuEntry(String text, byte actionPointer) {
		this.text = text;
		doesBranch = false;
		this.actionPointer = actionPointer;
		branchPointer = null;
	}

	public MenuEntry(String text, int actionPointer) {
		this.text = text;
		doesBranch = false;
		this.actionPointer = (byte) actionPointer;
		branchPointer = null;
	}

	public MenuEntry() { // specific empty constructor
		text = "...";
		// none of the other variables are used, but 
		doesBranch = false;
		actionPointer = -1;
		branchPointer = null;
	}

	public String getText() {
		return text;
	}

	public boolean doesBranch() { // get method but more meaningful name
		return doesBranch;
	}

	public MenuList getBranch() {
		return branchPointer;
	}

	public byte getAction() {
		return actionPointer;
	}

	// no toString() needed yet, getText() handles that.
}