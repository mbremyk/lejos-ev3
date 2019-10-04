import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.Keys;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound.*;
import java.util.ConcurrentModificationException;

/**
 * EV3API.java
 * Inneholder alt for registrering av EV3-er, bricks, sensorporter osv osv.
 * Inneholder også alle metoder som innebærer bevegelse og/eller manipulering av motorer og sensorer
 * Unntak gjøres for oppdatering av ultralydsensorene, se egen tråd for dette.
 *
 * @author Magnus Brevik, Aleksander Skogan
 */

public class EV3API {
    //Registrere hovedenhet
    private Brick brick = BrickFinder.getDefault();
    private EV3 ev3 = (EV3)BrickFinder.getLocal();

    public EV3 getEV3()
    {
        return ev3;
    }

    //praktiske ting å ha
    private TextLCD lcd = ev3.getTextLCD();
    private Keys keys = ev3.getKeys();

    /*
     * Initiere sensorporter, sensorer,
     * sampleproviders og arrays tilknyttet disse
     */

    private Port s1 = brick.getPort("S1");
    private Port s2 = brick.getPort("S2");
    private Port s3 = brick.getPort("S3");
    private Port s4 = brick.getPort("S4");

    private NXTUltrasonicSensor topUS;
    private EV3UltrasonicSensor leftUS;
    private EV3UltrasonicSensor rightUS;

    private SampleProvider topUSSP;
    private SampleProvider leftUSSP;
    private SampleProvider rightUSSP;

    /*
     * Disse skal være tilgjengelige for andre klasser
     */

    public float[] topUSSample;
    public float[] leftUSSample;
    public float[] rightUSSample;

    public float lastTopUSSample = Float.MAX_VALUE;
    public float lastLeftUSSample = Float.MAX_VALUE;
    public float lastRightUSSample = Float.MAX_VALUE;

    /*
     * startverdier for nye EV3 - enheter
     * tilgjengelige for andre klasser
     */

    public Boolean near = false;
    public Boolean far = false;
    public Boolean climbable = false; //er objektet foran oss en trapp som kan klatres?
    public Boolean corner = false;
    public Boolean climbing = false; //klatrer vi akkurat nå?
    public Boolean sensorsWorking = true; //fungerte alle sensorene?
    public Boolean kalvskinnet = true; //har vi en kalvskinnet - spesifik trapp foran oss? 

    private int speed = 375; //starthastighet for motorene

    final private float SENSOR_DIFF = 0.13f; //avstand mellom de nedre sensorene og den øvre
    final private float BOT_LENGTH = 0.25f; //lengde på bot
    final private int MAX_TACHO_COUNT = 4160; //maksimal tachometer-verdi for motor D
    final private int STAIR_KALVSKINNET_TACHO_COUNT = 3200; //tachometer-verdi som angir høyden på en kalvskinnet-trapp
    final private float SENSOR_SLACK = 0.005f;
    final private int TASK_TIMEOUT_MS = 4000;

    /*
     * Lag et SampleUpdater - objekt
     * Denne startes i Main.java
     */

    private static SampleUpdater sampleUpdater;

    public EV3API() {

        /*
         * reset tachometer for motorene
         */

        Motor.A.resetTachoCount();	//A: høyre motor
        Motor.B.resetTachoCount();	//B: venstre motor
        Motor.C.resetTachoCount();	//C: bakmotor
        Motor.D.resetTachoCount();	//D: heismotor

        /*
         * Ferdiginitialisere sensorer
         * en av sensorene har dårlig tilkobling så sett flagget
         * sensorsWorking til false når de ikke er skikkelig tilkoblet
         */

        try {
            topUS = new NXTUltrasonicSensor(s1);
            leftUS = new EV3UltrasonicSensor(s2);
            rightUS = new EV3UltrasonicSensor(s3);

            topUSSP = topUS.getDistanceMode();
            leftUSSP = leftUS.getDistanceMode();
            rightUSSP = rightUS.getDistanceMode();

            topUSSample = new float[topUSSP.sampleSize()];
            leftUSSample = new float[leftUSSP.sampleSize()];
            rightUSSample = new float[rightUSSP.sampleSize()];
        } catch (Exception e) {
            write(e.toString());
            sensorsWorking = false;
        }

        /*
         * oppdatering av sensorer, del 2 :)
         */
        sampleUpdater = new SampleUpdater();
    }

    /**
     * set speed -.-
     * @param speed = hastighet
     */

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * get speed
     * @return speed
     */

    public int getSpeed() {
        return this.speed;
    }

    /**
     * metode for å vente på keypress før vi går videre
     * @throws Exception SleepInterruptedException
     */

    public void waitForKeyPress() throws Exception {
        keys.waitForAnyPress();
        Thread.sleep(200);
    }

    /**
     * les knapper
     * @return knapper :)
     */

    public int readButtons() {
        return keys.readButtons();
    }

    /**
     * svinge til venstre
     */

    private void turnLeft() {
        Motor.A.forward();
        //Motor.B.backward();
    }

    /**
     * svinge til venstre
     */

    private void turnLeft(int speed) {
        Motor.A.setSpeed(speed);
        //Motor.B.setSpeed(speed);
        Motor.A.forward();
        //Motor.B.backward();
    }

    /**
     * svinge til høyre
     */

    private void turnRight() {
        //Motor.A.backward();
        Motor.B.forward();
    }

    /**
     * svinge til høyre
     */

    private void turnRight(int speed) {
        //Motor.A.setSpeed(speed);
        Motor.B.setSpeed(speed);
        //Motor.A.backward();
        Motor.B.forward();
    }

    /**
     * gå fremover
     */

    private void goForward() {
        Motor.A.forward();
        Motor.B.forward();
        Motor.C.forward();
    }

    /**
     * gå fremover
     */

    private void goForward(int speed) {
        Motor.A.setSpeed(speed);
        Motor.B.setSpeed(speed);
        Motor.C.setSpeed(speed);
        Motor.A.forward();
        Motor.B.forward();
        Motor.C.forward();
    }

    /**
     * gå fremover som ledd i klatringen
     * Denne metoden prøver å løfte fremsiden av EV3en opp veggen
     * motor C kjører hær tregere
     * @param speed = speed
     */

    private void goClimbForward(int speed) {
        Motor.A.setSpeed(speed);
        Motor.B.setSpeed(speed);
        Motor.C.setSpeed(speed / 8);
        Motor.A.forward();
        Motor.B.forward();
        Motor.C.forward();
    }

    /**
     * full, umiddelbar stopp
     */

    public void stop() {
        Motor.A.stop(true);
        Motor.B.stop(true);
        Motor.C.stop(true);
        Motor.D.stop(true);
    }

    /**
     * Låse alle motorer
     */

    private void lock() {
        Motor.A.lock(100);
        Motor.B.lock(100);
        Motor.C.lock(100);
        Motor.D.lock(100);
    }

    /**
     * løfte med heismotores
     */

    private void goUp() {
        Motor.D.forward();
    }

    /**
     * løfte med heismotores
     */

    private void goUp(int speed) {
        Motor.D.setSpeed(speed);
        Motor.D.forward();
    }

    /**
     * Senke med heismotoren
     */

    private void goDown() {
        Motor.D.backward();
    }

    /**
     * Senke med heismotoren
     */

    private void goDown(int speed) {
        Motor.D.setSpeed(speed);
        Motor.D.backward();
    }

    /**
     * Gå fremover med bakhjulene
     */

    private void backForward() {
        Motor.C.forward();
    }

    /**
     * Gå fremover med bakhjulene
     */

    private void backForward(int speed) {
        Motor.C.setSpeed(speed);
        Motor.C.forward();
    }

    /**
     * Gå bakover med bakhjulene
     */

    private void backBackward() {
        Motor.C.backward();
    }

    /**
     * Gå bakover med bakhjulene
     */

    private void backBackward(int speed) {
        Motor.C.setSpeed(speed);
        Motor.C.backward();
    }

    /**
     * sett global hastighet
     * @param speed = nyspeed
     */

    public void setSpeedAll(int speed) {
        Motor.A.setSpeed(speed);
        Motor.B.setSpeed(speed);
        Motor.C.setSpeed(speed);
        Motor.D.setSpeed(speed);
    }

    /**
     * rensk LCD-display
     */

    public void clear() {
        LCD.clear();
    }

    /**
     * roter motor D tilbake til 0
     * Denne brukes istedet for goDown()
     */

    private void rotateDTo0() {
        Motor.D.rotateTo(0);
        Motor.D.getTachoCount();    //oppdaterer tachocounten til motor D. Fungerer ikke uten!! Vet ikke hvorfor det fungerer med
        Motor.D.stop(true);
    }

    /**
     * gå inn, som ledd i klatringen
     * @param speed = speed
     */

    private void goIn(int speed) {
        Motor.A.setSpeed(speed / 3);
        Motor.B.setSpeed(speed / 3);
        Motor.C.setSpeed(speed);
        Motor.A.forward();
        Motor.B.forward();
        Motor.C.forward();
    }

    /**
     * samlemetode for å klatre opp ett steg
     * @param kalvskinnet bestemmer om vi bruker en spesifik og
     *                    optimalisert metode, eller en generell metode
     */

    public boolean climbUpStep(Boolean kalvskinnet) {
        long startMillis = System.currentTimeMillis();
        long millis;
        boolean success = true;

        /*
         * sjekk gyldig hastighet
         */

        if (speed < 0) {
            speed = 0;
        }

        if (speed > 900) {
            speed = 900;
        }

        if (kalvskinnet) {
            climbing = true;
            while(readButtons() == 0 && climbing) {
                millis = System.currentTimeMillis() - startMillis;
                success = kalvskinnetClimb(this.speed, millis);
            }
        } else if (readButtons() == 0 && climbing) {
            millis = System.currentTimeMillis();

            success = stairClimb(speed, millis);
        }

        return success;
    }

    /**
     * skriv streng til LCD-display, gitt koordinater
     * @param str streng
     * @param x koordinat
     * @param y koordinat
     */

    public void write(String str, int x, int y) {
        if(x > 16) x = 16;
        if(y > 6) y = 6;
        LCD.drawString(str, x, y);
        //System.out.print(str);
    }

    /**
     * skriv streng til LCD-display
     * @param str streng
     */

    public void write(String str)
    {
        System.out.println(str);
    }

    /**
     * starte SampleUpdater - tråden
     */

    public static void sampleUpdaterStart() {
        sampleUpdater.start();
    }

    /**
     * stoppe SampleUpdater - tråden
     */

    public static void sampleUpdaterStop() {
        try {
            sampleUpdater.interrupt();
        } catch (Exception e) {
            //throws exception if thread is already interrupted
            //do nothing
            //sampleUpdaterStop();
        }
    }

    /**
     * Oppdatere samples fra ultralydsensorene, denne metoden skal KUN kjøres fra SampleUpdater.java
     */

    public void updateSamples() {
        try {
            //tar vare på siste ikke-uendelige verdi fra sensorene
            if (!(topUSSample[0] == Float.POSITIVE_INFINITY)) {
                lastTopUSSample = topUSSample[0];
            }

            if (!(leftUSSample[0] == Float.POSITIVE_INFINITY)) {
                lastLeftUSSample = leftUSSample[0];
            }

            if (!(rightUSSample[0] == Float.POSITIVE_INFINITY)) {
                lastRightUSSample = rightUSSample[0];
            }

            //henter samples fra sensorene
            topUSSP.fetchSample(topUSSample, 0);
            leftUSSP.fetchSample(leftUSSample, 0);
            rightUSSP.fetchSample(rightUSSample, 0);

            //finner 
            if (((lastLeftUSSample < 0.10f && lastRightUSSample < 0.10f) && (leftUSSample[0] == Float.POSITIVE_INFINITY || rightUSSample[0] == Float.POSITIVE_INFINITY)) || (leftUSSample[0] < 0.04f && rightUSSample[0] < 0.04f)) {
                near = true;
                far = false;
                corner = false;
            } else if ((lastLeftUSSample > 0.50f && lastRightUSSample > 0.50f) && (leftUSSample[0] == Float.POSITIVE_INFINITY || rightUSSample[0] == Float.POSITIVE_INFINITY)) {
                near = false;
                far = true;
                corner = false;
            } else if (((lastLeftUSSample > 0.50f && lastRightUSSample < 0.10f) || (lastLeftUSSample < 0.10f && lastRightUSSample > 0.50f)) && (leftUSSample[0] == Float.POSITIVE_INFINITY && rightUSSample[0] == Float.POSITIVE_INFINITY)) {
                near = false;
                far = false;
                corner = true;
            } else {
                near = false;
                far = false;
                corner = false;
            }

            if (topUSSample[0] > BOT_LENGTH + SENSOR_DIFF || topUSSample[0] == Float.POSITIVE_INFINITY) {
                climbable = true;
            } else {
                climbable = false;
            }
        } catch (ConcurrentModificationException v) {
            //do nothing and continue to next iteration
        }
    }

    /**
     * spesifik metode for å klatre opp en trapp ved kalvskinnet, med statisk hastighet
     * @param speed ignorert for øyeblikket
     * @param millis setter tidligere, men vanligvis ca 0 når vi kommer hit
     */

    private boolean kalvskinnetClimb(int speed, long millis) {
        int oldSpeed = speed;
        speed = 375;

        if (millis < 3000) {	//løfte front
            goForward(speed / 5);
        } else if (millis < 11500) { 	//klatre opp
            goClimbForward(speed / 5);
            goUp(speed);
        } else if (millis < 11600) {
            stop();
        } else if (millis < 15500) {		//kjøre inn
            goIn(speed / 5);
        } else if (millis < 15600) {        //vente litt
            stop();
        } else if (millis < 26500) {	    //løfte bak
            //System.out.println("Før rotateDTo0");
            rotateDTo0();
            //System.out.println("Etter rotateDTo0");
        } else if (millis < 26600) {
            //System.out.println("Før stopp");
            stop();
            //System.out.println("Etter stopp");
        } else if (millis < 31500) {		//kjøre mer inn
            //System.out.println("Før inn");
            goForward(speed / 5);
            //System.out.println("Etter inn");
        } else {
            stop();
            climbing = false;
        }
        return true;
    }

    /**
     * bruk sensorene til å lete etter et trappetrinn
     * @param millis millis
     */

    public void searchForStep(long millis) {
        if (climbable && near) {    //hvis det er plasse til roboten og den står inntil noe settes "climbing"-flagget til true
            stop();
            climbing = true;
        } else if (near) {          //hvis roboten er nær noe, men det ikke er plass til den, rygger den litt og svinger
            long startMillis = millis;
            long millisTurning = System.currentTimeMillis() - startMillis;
            stop();
            while (millisTurning < 1000) {
                millisTurning = System.currentTimeMillis() - startMillis;
                Motor.A.backward();
                Motor.C.backward();
            }
            Motor.A.stop(true);
        } else if (corner) {        //hvis robotoen leser stor forskjell i de to sensorene vil den prøve å komme unna hjørnet
            long startMillis = millis;
            long millisTurning = System.currentTimeMillis() - startMillis;
            if (lastLeftUSSample > lastRightUSSample + SENSOR_SLACK) {
                stop();
                while (millisTurning < 1000) {
                    millisTurning = System.currentTimeMillis() - startMillis;
                    Motor.B.backward();
                }
                Motor.B.stop(true);
            } else if (lastRightUSSample > lastLeftUSSample + SENSOR_SLACK) {
                stop();
                while (millisTurning < 1000) {
                    millisTurning = System.currentTimeMillis() - startMillis;
                    Motor.A.backward();
                }
                Motor.A.stop(true);
            }
        } else if (far) {           //hvis roboten registrerer er langt unna vil den kjøre litt tilfeldig rundt
            if ((millis / 1000) % 5 == 0) {
                turnLeft();
            } else if ((millis / 1000) % 7 == 0) {
                turnRight();
            } else {
                goForward();
            }
        } else {                    //hvis roboten ser noe rett foran seg, vil den gå rett fram
            if (Math.abs(leftUSSample[0] - rightUSSample[0]) < SENSOR_SLACK) {
                goForward();
            }
        }
    }

    /**
     * Første (virkelige) forsøk på en generell klatremetode
     */

     private boolean stairClimb(int speed, long millis) {

        //ignore input speed for now
        speed = 375;

        float leftSample = 0;
        float rightSample = 0;
        try {
            leftSample = leftUSSample[0];
            rightSample = rightUSSample[0];
        } catch (ConcurrentModificationException e) {
            //do nothing and continue
        }
        boolean noError = true;

        //løfte front
        stop();
        long localMillis = System.currentTimeMillis();
        while(noError && (leftSample < 4.3f && rightSample < 4.3f)) {
            try {
                leftSample = leftUSSample[0];
                rightSample = rightUSSample[0];
            } catch (ConcurrentModificationException e) {
                //do nothing and continue
            }
            goForward(speed / 5);
            if ((localMillis < System.currentTimeMillis() - TASK_TIMEOUT_MS) || readButtons() > 0) {
                noError = false;
                write("no grip on wall");
            }
        }

        //klatre til sensorene har passert toppen av trappen
        stop();
        localMillis = System.currentTimeMillis();
        while(noError && (leftSample < 7f && rightSample < 7f)) {
            try {
                leftSample = leftUSSample[0];
                rightSample = rightUSSample[0];
            } catch (ConcurrentModificationException e) {
                //do nothing and continue
            }
            goClimbForward(speed / 5);
            goUp(speed);
            if ((localMillis < System.currentTimeMillis() - (TASK_TIMEOUT_MS)) || readButtons() > 0) {
                noError = false;
                write("didn't find top of step");
            }
        }

        //klatre til toppen av trappen; denne funksjonen er basert på en timer
        stop();
        localMillis = System.currentTimeMillis();
        while(noError && (localMillis > System.currentTimeMillis() - (TASK_TIMEOUT_MS * 1.9)) && (Motor.D.getTachoCount() < (MAX_TACHO_COUNT - 40))) {
            goClimbForward(speed / 5);
            goUp(speed);
            if ((localMillis < (System.currentTimeMillis() - (TASK_TIMEOUT_MS * 2))) || readButtons() > 0) {
                noError = false;
                write("couldn't climb to top of step");
            }
        }

        //anta at toppen av trappen er nådd, og kjør inn mot kanten, også basert på timer
        stop();
        localMillis = System.currentTimeMillis();
        boolean fortsett = true;
        while(fortsett && noError) {
            goIn(speed / 5);
            if ((localMillis < System.currentTimeMillis() - (TASK_TIMEOUT_MS / 2)) || readButtons() > 0) {
                fortsett = false;
                write("couldn't enter step");
            }
        }

        //løfte bakende
        stop();
        localMillis = System.currentTimeMillis();
        while(noError && Motor.D.getTachoCount() > 10) {
            rotateDTo0();
            if ((localMillis < System.currentTimeMillis() - (TASK_TIMEOUT_MS * 3)) || readButtons() > 0) {
                noError = false;
                write("couldn't return belt to 0");
            }
        }

        //kjøre mer inn
        stop();
        localMillis = System.currentTimeMillis();
        while(noError && (leftSample > 3.5 && rightSample < 3.5)) {
            try {
                leftSample = leftUSSample[0];
                rightSample = rightUSSample[0];
            } catch (ConcurrentModificationException e) {
                //do nothing and continue
            }
            goForward(speed / 3);
            if ((localMillis < System.currentTimeMillis() - (TASK_TIMEOUT_MS)) || readButtons() > 0) {
                noError = false;
                write("couldn't get belt onto step");
            }
        }
        //reset beltet
        rotateDTo0();
        climbing = false;
        //stopper maskinen
        stop();

        return noError;
    }

    // public void angryBeeps() {
    //     Sound.twoBeeps();
    // }
}


//TODO: Ynkelig.exe