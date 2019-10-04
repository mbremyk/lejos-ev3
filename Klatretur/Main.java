import java.lang.System.*;

import java.util.ConcurrentModificationException;

public class Main {
	public static EV3API ev3 = new EV3API();

	private static MenuEntry bullshit = new MenuEntry("Take some bullshit and go", EV3Menu.BULLSHIT);
	private static MenuEntry stay = new MenuEntry("Don't stay", -45);
	private static MenuEntry walk = new MenuEntry("Walk in the hay", -16);
	private static MenuEntry[] array1_1 = {bullshit, stay, walk};
	private static MenuList list1_1 = new MenuList("**POINTLESSNESS**", array1_1);

	private static MenuEntry speedMenu = new MenuEntry("Set my speed", EV3Menu.SPEED_MENU);
	private static MenuEntry climbCalf = new MenuEntry("Klatre opp kalven", EV3Menu.CLIMB_KALVSKINNET);
	private static MenuEntry climbDefault = new MenuEntry("Klatre hvor som helst", EV3Menu.CLIMB_DYNAMIC);
	private static MenuEntry pointer1_1 = new MenuEntry("Witness the truth", list1_1);
	private static MenuEntry[] array0 = {climbCalf, climbDefault, speedMenu, pointer1_1};
	private static MenuList list0 = new MenuList("**MENU IN MAIN**", array0);

	private static EV3Menu menu = new EV3Menu(list0, ev3);

	public static void main(String[] args) {

		long startMillis = System.currentTimeMillis();
		long millis = System.currentTimeMillis() - startMillis;
		EV3API.sampleUpdaterStart();

		menu.showMenu();
		while (menu.buttonResponse()) {
			menu.showMenu();
			//ev3.write(ev3.topUSSample[0] + "\n" + ev3.leftUSSample[0] + "\n" + ev3.rightUSSample[0]);
		}

		while (ev3.readButtons() == 0) {
			millis = System.currentTimeMillis() - startMillis;
			//ev3.write(ev3.topUSSample[0] + "\n" + ev3.leftUSSample[0] + "\n" + ev3.rightUSSample[0]);
			if (!ev3.climbing) {
				try {
					ev3.searchForStep(millis);
				} catch (ConcurrentModificationException e) {} catch (Exception e) {} finally {}
			} else {
				ev3.climbUpStep(ev3.kalvskinnet);
			}
			//ev3.climbUpStep(true);
		}
		EV3API.sampleUpdaterStop();
		menu.sayGoodbye();
		//ev3.write(ev3.topUSSample[0] + "\n" + ev3.leftUSSample[0] + "\n" + ev3.rightUSSample[0]);
		//ev3.climbUpStep(speed, true);
	}
}