import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.Keys;
import lejos.hardware.lcd.*;
import lejos.hardware.motor.*;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.hardware.Sound;

import lejos.hardware.*;

import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

import java.lang.System.*;

public class EV3API {
	//Registrere hovedenhet
	Brick brick = BrickFinder.getDefault();
	EV3 ev3 = (EV3)BrickFinder.getLocal();

	//praktiske ting å ha
	TextLCD lcd = ev3.getTextLCD();
	Keys keys = ev3.getKeys();

	//registrering av sensorporter
	private Port s1 = brick.getPort("S1");
	private Port s2 = brick.getPort("S2");
	private Port s3 = brick.getPort("S3");
	private Port s4 = brick.getPort("S4");

	private NXTUltrasonicSensor topUS;
	private EV3UltrasonicSensor leftUS;
	private EV3UltrasonicSensor rightUS;

	SampleProvider topUSSP;
	SampleProvider leftUSSP;
	SampleProvider rightUSSP;

	float[] topUSSample;
	float[] leftUSSample;
	float[] rightUSSample;

	float lastTopUSSample = Float.MAX_VALUE;
	float lastLeftUSSample = Float.MAX_VALUE;
	float lastRightUSSample = Float.MAX_VALUE;

	public Boolean near = false;
	public Boolean far = false;
	public Boolean climbable = false;
	public Boolean corner = false;
	public Boolean climbing = false;
	public Boolean sensorsWorking = true;
	public Boolean kalvskinnet = false;

	private int speed = 375;

	final private float SENSOR_DIFF = 0.13f;
	final private float BOT_LENGTH = 0.25f;
	final private int MAX_TACHO_COUNT = 4160;
	final private int STAIR_KALVSKINNET_TACHO_COUNT = 3200;
	final private float SENSOR_SLACK = 0.005f;

	static SampleUpdater sampleUpdater;

	public EV3API() {
		Motor.A.resetTachoCount();	//A: høyre motor
		Motor.B.resetTachoCount();	//B: venstre motor
		Motor.C.resetTachoCount();	//C: bakmotor
		Motor.D.resetTachoCount();	//D: heismotor

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

		sampleUpdater = new SampleUpdater();
	}

	//set og get speed
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getSpeed() {
		return this.speed;
	}

	//metode for å vente på keypress for å gå videre
	public void waitForKeyPress() throws Exception {
		keys.waitForAnyPress();
		Thread.sleep(200);
	}

	//metode for å lese antall knapper trykket inn
	public int readButtons() {
		return keys.readButtons();
	}

	public void turnLeft() {
		Motor.A.forward();
		//Motor.B.backward();
	}

	public void turnLeft(int speed) {
		Motor.A.setSpeed(speed);
		//Motor.B.setSpeed(speed);
		Motor.A.forward();
		//Motor.B.backward();
	}

	public void turnRight() {
		//Motor.A.backward();
		Motor.B.forward();
	}

	public void turnRight(int speed) {
		//Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
		//Motor.A.backward();
		Motor.B.forward();
	}

	public void goForward() {
		Motor.A.forward();
		Motor.B.forward();
		Motor.C.forward();
	}

	public void goForward(int speed) {
		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed);
		Motor.A.forward();
		Motor.B.forward();
		Motor.C.forward();
	}

	public void goClimbForward(int speed) {
		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed / 8);
		Motor.A.forward();
		Motor.B.forward();
		Motor.C.forward();
	}

	public void stop() {
		Motor.A.stop(true);
		Motor.B.stop(true);
		Motor.C.stop(true);
		Motor.D.stop(true);
	}

	public void lock() {
		Motor.A.lock(100);
		Motor.B.lock(100);
		Motor.C.lock(100);
		Motor.D.lock(100);
	}

	public void goUp() {
		Motor.D.forward();
	}

	public void goUp(int speed) {
		Motor.D.setSpeed(speed);
		Motor.D.forward();
	}

	public void goDown() {
		Motor.D.backward();
	}

	public void goDown(int speed) {
		Motor.D.setSpeed(speed);
		Motor.D.backward();
	}

	public void backForward() {
		Motor.C.forward();
	}

	public void backForward(int speed) {
		Motor.C.setSpeed(speed);
		Motor.C.forward();
	}

	public void backBackward() {
		Motor.C.backward();
	}

	public void backBackward(int speed) {
		Motor.C.setSpeed(speed);
		Motor.C.backward();
	}

	public void setSpeedAll(int speed) {
		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed);
		Motor.D.setSpeed(speed);
	}

	public void clear() {
		LCD.clear();
	}

	public void rotateDTo0() {
		Motor.D.rotateTo(0);
	}

	public void goIn(int speed) {
		Motor.A.setSpeed(speed / 3);
		Motor.B.setSpeed(speed / 3);
		Motor.C.setSpeed(speed);
		Motor.A.forward();
		Motor.B.forward();
		Motor.C.forward();
	}

	public void climbUpStep(Boolean kalvskinnet) {
		long startMillis = System.currentTimeMillis();
		long millis = System.currentTimeMillis() - startMillis;

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
				kalvskinnetClimb(this.speed, millis);
			}
		} else if (readButtons() == 0 && climbing) {
			while (readButtons() == 0) {
				millis = System.currentTimeMillis() - startMillis;


				// if (Motor.D.getTachoCount() >= MAX_TACHO_COUNT) {
				// 	Motor.D.rotateTo(MAX_TACHO_COUNT);
				// 	Motor.D.stop();
				// } else if (!(leftUSSample[0] == Float.POSITIVE_INFINITY && rightUSSample[0] == Float.POSITIVE_INFINITY)) {
				// 	goForward();
				// 	backForward();
				// } else if (Motor.D.getTachoCount() < MAX_TACHO_COUNT) {
				// 	goUp();
				// }
			}
		}
	}


	public void write(String str, int x, int y) {
		if(x > 16) x = 16;
		if(y > 6) y = 6;
		LCD.drawString(str, x, y);
		//System.out.print(str);
	}

	public void write(String str)
	{
		System.out.println(str);
	}

	// public void writeMid(String str) {
	// 	int l = str.length();
	// 	String[] strArr;

	// 	if(l > 17) {
	// 		strArr = new String[l / 17 + 1];
	// 		for(int i = 0; i < )
	// 	}
	// }

	// public int speedMenu() {
	// 	clear();
	// 	int mySpeed = this.speed;
	// 	write("Specify speed: " + mySpeed, 0, 0);
	// 	int buttons = keys.waitForAnyPress(200);

	// 	while (true) {
	// 		switch (buttons) {
	// 			case Keys.ID_DOWN:
	// 			mySpeed -= 5;
	// 			break;
	// 			case Keys.ID_UP:
	// 			mySpeed += 5;
	// 			break;
	// 			case Keys.ID_ENTER:
	// 			Delay.msDelay(200);
	// 			clear();
	// 			return mySpeed;
	// 			default:
	// 			break;
	// 		}
	// 		clear();
	// 		write("Specify speed: " + mySpeed, 0,0);
	// 		buttons = keys.waitForAnyPress(200);
	// 	}
	// }

	static void sampleUpdaterStart() {
		sampleUpdater.start();
	}

	static void sampleUpdaterStop() {
		try{
			sampleUpdater.interrupt();
		} catch (Exception e) {}
	}

	//Sample updating method
	void updateSamples() {
		if(!(topUSSample[0] == Float.POSITIVE_INFINITY)) {
			lastTopUSSample = topUSSample[0];
		}

		if(!(leftUSSample[0] == Float.POSITIVE_INFINITY)) {
			lastLeftUSSample = leftUSSample[0];
		}

		if(!(rightUSSample[0] == Float.POSITIVE_INFINITY)) {
			lastRightUSSample = rightUSSample[0];
		}

		topUSSP.fetchSample(topUSSample, 0);
		leftUSSP.fetchSample(leftUSSample, 0);
		rightUSSP.fetchSample(rightUSSample, 0);

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
	}

	private void kalvskinnetClimb(int speed, long millis) {
		int oldSpeed = speed;
		speed = 375;

		if (millis < 3000) {	//løfte front
			goForward(speed / 5);
		} else if (millis < 11500) { 	//klatre opp
			goClimbForward(speed / 5);
			goUp(speed);
		} else if (millis < 11600) {
			stop();
		} else if (millis < 12500) {		//kjøre inn
			goIn(speed / 5);
		} else if (millis < 12600) { //vente litt
			stop();
		} else if (millis < 23500) {	   //løfte bak
			rotateDTo0();
		} else if (millis < 23600) {
			stop();
		} else if (millis < 28500) {		//kjøre mer inn
			goForward(speed / 5);
		} else {
			stop();
			climbing = false;
		}
	}

	public void searchForStep(long millis) {
		if (climbable && near) {
			stop();
			climbing = true;
		} else if (near) {
			long startMillis = millis;
			long millisTurning = System.currentTimeMillis() - startMillis;
			stop();
			while (millisTurning < 1000) {
				millisTurning = System.currentTimeMillis() - startMillis;
				Motor.A.backward();
				Motor.C.backward();
			}
			Motor.A.stop(true);
		} else if (corner) {
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
		} else if (far) {
			if ((millis / 1000) % 5 == 0) {
				turnLeft();
			} else if ((millis / 1000) % 7 == 0) {
				turnRight();
			} else {
				goForward();
			}
		} else {
			if (Math.abs(leftUSSample[0] - rightUSSample[0]) < SENSOR_SLACK) {
				goForward();
			}
		}
	}
}