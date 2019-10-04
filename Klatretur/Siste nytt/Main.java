/**
 * Main.java
 * Is main, very yes
 * Gjør ting
 * Det er her safta ligger
 *
 * @author Joakim Moe Adolfsen, Tore Bergebakken, Jon Åby Bergquist, Magnus Brevik, Aleksander Skogan
 *
 */

public class Main {

    public static final EV3API ev3api = new EV3API();

    //Lage poengløs undermeny
    private static final MenuEntry bullshit = new MenuEntry("Take some bullshit and go", EV3Menu.BULLSHIT);
    private static final MenuEntry stay = new MenuEntry("Don't stay", -45);
    private static final MenuEntry walk = new MenuEntry("Walk in the hay", -16);
    private static final MenuEntry[] array1_1 = {bullshit, stay, walk};
    private static final MenuList list1_1 = new MenuList("**POINTLESSNESS**", array1_1);

    //Lage hovedmeny
    private static final MenuEntry speedMenu = new MenuEntry("Set my speed", EV3Menu.SPEED_MENU);
    private static final MenuEntry climbCalf = new MenuEntry("Klatre opp kalven", EV3Menu.CLIMB_KALVSKINNET);
    private static final MenuEntry climbDefault = new MenuEntry("Klatre hvor som helst", EV3Menu.CLIMB_DYNAMIC);
    private static final MenuEntry pointer1_1 = new MenuEntry("Witness the truth", list1_1);
    private static final MenuEntry[] array0 = {climbCalf, climbDefault, speedMenu, pointer1_1};
    private static final MenuList list0 = new MenuList("**MENU IN MAIN**", array0);

    private static final EV3Menu menu = new EV3Menu(list0, ev3api);

    public static void main(String[] args) {

        long startMillis = System.currentTimeMillis();
        long millis;
        EV3API.sampleUpdaterStart(); //start sample updater thread

        menu.showMenu();
        while (menu.buttonResponse()) {
            menu.showMenu();
        }

        while (ev3api.readButtons() == 0) {
            millis = System.currentTimeMillis() - startMillis;
            if (!ev3api.climbing && ev3api.sensorsWorking) {
                try {
                    ev3api.searchForStep(millis);
                } catch (java.util.ConcurrentModificationException e) {
                    //Do nothing and continue
                } catch (Exception e) {
                            
                }
            } else {
                if(!ev3api.climbUpStep(ev3api.kalvskinnet)) {
                    //ev3api.angryBeeps();
                    break;
                }
            }
        }

        ev3api.stop();
        menu.sayGoodbye();
        try {
            EV3API.sampleUpdaterStop();
        } catch (Exception e) {

        }
        //ev3api.write(ev3api.topUSSample[0] + "\n" + ev3api.leftUSSample[0] + "\n" + ev3api.rightUSSample[0]);
        //ev3api.climbUpStep(speed, true);
    }
}