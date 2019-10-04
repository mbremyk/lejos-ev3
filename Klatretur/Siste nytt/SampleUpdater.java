/**
 * SampleUpdater.java
 *
 * Oppdaterer arrays ***USSample for ev3 med nye verdier
 * fra de ultrasoniske sensorene
 *
 * @author Aleksander Skogan
 */


//Create only one instance of this thread!
public class SampleUpdater extends Thread {

    //latskap v1.1
    private void err(String s) {
        System.err.print(s);
    }

    private void errln(String s) {
        System.err.println(s);
    }

    private void out(String s) {
        System.out.print(s);
    }

    private void outln(String s) {
        System.out.println(s);
    }


    /**
     * tråd-spesifik sovemetode
     * prøver å opprettholde en oppdateringsfrekvens på 33Hz
     * @param ms = input ms
     */

    private void sleep(int ms) {
        if (ms < 0) ms = 0;
        if (ms > 30) ms = 30;
        try {
            Thread.sleep(30 - ms);
        } catch (Exception threadSleepException) {
            err(threadSleepException.toString());
        }
    }

    /**
     * evig oppdatering av samples så lenge interrupted() returnerer false
     * hvis ev3 sine sensorer ikke fungerer vil ikke denne gjøre noe
     */

    public void run() {
        long millisStart;
        while (!interrupted()) {
            try {
                millisStart = System.currentTimeMillis();

                Main.ev3api.updateSamples();

                sleep(System.currentTimeMillis() - millisStart);
            } catch (Exception e) {
                //skrive feilmeldinger men fortsett å kjøre programmet
                e.printStackTrace();
                //outln(e.toString());
            }
        }
    }
}