/**
 *	EV3Menu.java
 *	Håndterer menyklassene og setter i gang det brukeren vil ha gjort.
 *	Går nå opp og ned i et klient-spesifisert tre av menyer.
 *	
 *	@author Tore Bergebakken
 */

public class EV3Menu {
	//private MenuList currentMenu;
	private byte currentMenu = 0; // there are no menus beyond this index
	private MenuList[] branchTree = new MenuList[5]; // at the moment
	private final EV3GUI gui;
	private final EV3API api;
	//private final byte SCREEN_LIMIT;
	private static final byte SELECTED_AT = 3;

	public static final byte EXIT = 0;
	public static final byte SPEED_MENU = 11;
	public static final byte CLIMB_DYNAMIC = 22;
	public static final byte CLIMB_KALVSKINNET = 23;
	public static final byte DESCEND_DYNAMIC = 32;
	public static final byte DESCEND_KALVSKINNET = 33;
	public static final byte BULLSHIT = 110;

	/* Constructors */
	public EV3Menu(MenuList topMenu, EV3API api) {
		branchTree[currentMenu] = topMenu; // no deep copy - clone constructor?
		this.api = api;
		gui = new EV3GUI(api);
		//SCREEN_LIMIT = gui.getScreenLimit();
	}

	/*
	 * Temporary placeholder for more complex branching
	 * You can't return to the previous menu atm.
	 */
	private void setMenu(MenuList nextMenu) {
		if (currentMenu + 1 < branchTree.length) {
			currentMenu++;
		} else { // since we don't need to extend our array in this usage
			throw new IllegalArgumentException("Can't branch further, mate. (placeholder exception)");
		}
		branchTree[currentMenu] = nextMenu; // again, no deep copy. saving resources.
	}

	/*
	 * implemented in buttonResponse()
	 * private boolean goBack() { if (currentMenu >= 0) {currentMenu--;} else {//exit program} } 
	*/

	// Displaying the array formatted in MenuList
	public void showMenu() {
		gui.displayList(branchTree[currentMenu].getEntryList(EV3GUI.Y_LIMIT, SELECTED_AT));
	}

	/*
	 * Show a message on the EV3 screen, accepting newline markers
	 * You'll have to use "\n \n" to get an empty line, MAYBE?
	 */
	private void showMessage(String msg) {
		String[] msgSplit = msg.split("\n");
		gui.displayList(msgSplit);
		gui.waitForKeyPress();
	}

	/* Private helper method used in askForValue() */
	private void valuePrompt(String[] msgSplit, int value) {
		int index = gui.displayList(msgSplit);
		gui.write("Current value: " + value, index);
	}

	/* Ask user to set a value */
	private int askForValue(String msg, int value, int interval, int min, int max) {
		String[] msgSplit = msg.split("\n");

		valuePrompt(msgSplit, value);
		int opt = gui.waitForKeyPress();
		while (!(gui.isEscape(opt) || gui.isEnter(opt))) {
			if (gui.isUp(opt)) {
				if (value + interval <= max) {
					value += interval;
				} else {
					value = min;
				}
			} else if (gui.isDown(opt)) {
				if (value - interval >= min) {
					value -= interval;
				} else {
					value = max;
				}
			}
			valuePrompt(msgSplit, value);
			opt = gui.waitForKeyPress();
		}
		showMessage(" \n \nValue set to:\n" + value);
		return value;
	}

	/* Simple thing for gooding the bye */
	public void sayGoodbye() {
		for (int i = 0; i < 5; i++) {
			gui.clear();
			String disp = "Goodbye";
			for (int j = 0; j < i + 1; j++) {
				disp += ".";
			}
			gui.write(disp, 3);
			gui.wait(350);
		}
	}

	/* Check buttons and do things, return false if escape or 0 somehow */
	public boolean buttonResponse() {
		int opt = gui.waitForKeyPress();
		if (opt > 0) {
			if (gui.isUp(opt)) {
				branchTree[currentMenu].browseUp();
			} else if (gui.isDown(opt)) {
				branchTree[currentMenu].browseDown();
			} else if (gui.isEnter(opt)) {
				activate();
			} else if (gui.isEscape(opt)) {
				if (currentMenu <= 0) { // at top level (avoiding errors)
					return false;
				} else { // at lower level, go up one level
					currentMenu--;
				}
			}
			return true;
		}
		return false;
	}

	/* Do what the MenuEntry holds */
	private void activate() {
		if (branchTree[currentMenu].doesBranch()) {
			setMenu(branchTree[currentMenu].getBranch());
		} else {
			doAction(branchTree[currentMenu].getAction());
			//testActions(currentMenu.getAction());
		}
	}

	/* The actual action happens here */
	private void doAction(byte opcode) {
		switch (opcode) {
			case EXIT:
				// Exiting somehow
				break;
			case SPEED_MENU:
				api.setSpeed(
					askForValue("Welcome to our\n--SPEED MENU--\nWould you kindly\nSET MY SPEEED",
								 api.getSpeed(), 5, 100, 900));
				break;
			case CLIMB_KALVSKINNET:
					api.kalvskinnet = true;
				break;
			case CLIMB_DYNAMIC:
				if (api.sensorsWorking) {
					// dynamic climbing
					api.kalvskinnet = false; // placeholder
				} else {
					showMessage("\n\nMALFUNCTIONING\nSENSORS");
					api.kalvskinnet = true; // default behaviour
				}
			case BULLSHIT:
				showMessage(" \n \nComplete, utter\n \nB U L L S H I T\nTESTING DISPLAY\n cpabullityces\nDOES THIS SHOW?");
				break;
			default:
				showMessage(" \n \nInvalid opcode\nMajor flaw");
				break;
		}
	}

	// Simple thing for testing purposes
	/*
	private void testActions(byte action) {
		switch (action) {
			case 0:
				gui.clear();
				gui.write("ZEROOO", 0, 2);
				gui.waitForKeyPress();
				break;
			case 1:
				gui.clear();
				gui.write("WOOOO", 0, 2);
				gui.waitForKeyPress();
				break;
			case 2:
				gui.clear();
				gui.write("Buzz Aldrin,", 0, 2);
				gui.write("hvor ble det av deg i alt mylderet?", 0, 3);
				gui.waitForKeyPress();
				break;
			default:
				gui.clear();
				gui.write("Oh noes", 0, 2);
				gui.waitForKeyPress();
				break;
		}
	}

	public static void main(String[] args) {
		MenuList menu0; // MUST BE INITIALIZED SHIT OH GOD
		MenuEntry[] menu1Array = {new MenuEntry("Hahaha", 0), new MenuEntry("4ever Second Place", 2), new MenuEntry("Back to previous menu", 1)};
		MenuList menu1 = new MenuList("--Undermeny", menu1Array);
		MenuEntry[] menu0Array = {new MenuEntry("Next menu", menu1), new MenuEntry("That. Is. Okay.", 1), new MenuEntry("trap", 23)};
		menu0 = new MenuList("--TEST MEG", menu0Array);

		EV3Menu mainMenu = new EV3Menu(menu0);

		mainMenu.showMenu();
		while(mainMenu.buttonResponse()) {
			mainMenu.showMenu();
		}

		mainMenu.sayGoodbye();
	}
	*/
}