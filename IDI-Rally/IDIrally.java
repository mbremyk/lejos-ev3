import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.Keys;
import lejos.hardware.lcd.*;
import lejos.hardware.motor.*;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

//http://www.legoengineering.com/nxt-sensors/ for mer info på hvordan noen av sensorene funker

class IDIrally
{
	//Registrere ev3 brick
	private static final Brick brick = BrickFinder.getDefault();
	private static final EV3 ev3 = (EV3)BrickFinder.getLocal();

	//praktiske ting å ha for å samhandle med ev3'en mens den kjører et program
	private static final TextLCD lcd = ev3.getTextLCD();
	private static final Keys keys = ev3.getKeys();

	//registrering av sensorporter
	private static final Port s1 = brick.getPort("S1");
	private static final Port s2 = brick.getPort("S2");
	private static final Port s3 = brick.getPort("S3");
	private static final Port s4 = brick.getPort("S4");

	//registrere lyssensorer
	private static SampleProvider lightSensor0 = new NXTLightSensor(s1).getRedMode();	//venstre
	private static SampleProvider lightSensor1 = new NXTLightSensor(s2).getRedMode();	//høyre
	private static float[][] lightSamples = { new flo-at[lightSensor0.sampleSize()], new float[lightSensor1.sampleSize()] };

	//metode for å lese antall knapper trykket inn
	private static int readButtons()
	{
		return keys.readButtons();
	}

	//metode for å svinge til venstre
	private static void turnLeft()
	{
		Motor.A.backward();
		Motor.B.forward();
	}

	//metode for å svinge til høyre
	private static void turnRight()
	{
		Motor.B.backward();
		Motor.A.forward();
	}

	//metode for å kjøre fremover
	private static void goForward()
	{
		Motor.A.backward();
		Motor.B.backward();
	}

	//metode for å stoppe
	private static void stop()
	{
		Motor.A.stop(true);
		Motor.B.stop(true);
	}

	//metode for å sette global motorhastighet
	private static void setSpeed(int speed)
	{
		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
	}

	//metode for å hente informasjon fra lyssensorene
	private static void fetchLightSample()
	{
		lightSensor0.fetchSample(lightSamples[0], 0);
		lightSensor1.fetchSample(lightSamples[1], 0);
	}

	public static void main(String[] args)
	{
		final int speed = 900;
		setSpeed(speed);
		int lastBlack = -1;
		int black = 37;

		while(readButtons() < 1)
		{
			fetchLightSample();

			if((int)(lightSamples[0][0] * 100) < black && (int)(lightSamples[1][0] * 100) < black) //hvis begge sensorer ser svart
			{
				setSpeed(speed);
				goForward();
			}
			else if ((int)(lightSamples[0][0] * 100) < black) //hvis venstre sensor ser svart
			{
				setSpeed(speed / 2);
				lastBlack = 0;
				turnLeft();
			}
			else if ((int)(lightSamples[1][0] * 100) < black) //hvis høyre sensor ser svart
			{
				setSpeed(speed / 2);
				lastBlack = 1;
				turnRight();
			}
			else if(lastBlack == 0) //hvis sist sett svarte side var venstre
			{
				setSpeed(speed / 2);
				turnLeft();
			}
			else
			{
				setSpeed(speed);
				goForward();
			}
		}

		stop();
	}
}