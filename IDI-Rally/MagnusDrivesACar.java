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

//bare importere hele lejos.hardware.* istedet?
//MB - enig, men det funker ikke
import lejos.hardware.*;

import lejos.robotics.SampleProvider;
import java.util.Random; //brukes ikke, fjern
import java.util.Arrays; //brukes ikke, fjern
import java.io.File;

//http://www.legoengineering.com/nxt-sensors/ for mer info på sensorer funker

class MagnusDrivesACar
{
	//Registrere hovedenhet
	public static Brick brick = BrickFinder.getDefault();
	public static EV3 ev3 = (EV3)BrickFinder.getLocal();

	//praktiske ting å ha
	public static TextLCD lcd = ev3.getTextLCD();
	public static Keys keys = ev3.getKeys();

	//registrering av sensorporter
	public static Port s1 = brick.getPort("S1");
	public static Port s2 = brick.getPort("S2");
	public static Port s3 = brick.getPort("S3");
	public static Port s4 = brick.getPort("S4");

	public static SampleProvider lightSensor0 = new NXTLightSensor(s1).getRedMode();	//venstre
	public static SampleProvider lightSensor1 = new NXTLightSensor(s2).getRedMode();	//høyre
	public static float[][] lightSamples = { new float[lightSensor0.sampleSize()], new float[lightSensor1.sampleSize()] };

	//public static File music = new File("-Yakety Sax- Music.wav");

	public static void waitForKeyPress() throws Exception 
	{
		keys.waitForAnyPress();
		Thread.sleep(200);
	}

	//metode for å lese antall knapper trykket inn
	public static int readButtons() 
	{
		return keys.readButtons();
	}

	public static void turnLeft()
	{
		Motor.A.backward();
		Motor.B.forward();
	}

	public static void turnLeft(int msec) throws Exception
	{
		Motor.A.backward();
		Motor.B.forward();
		Thread.sleep(msec);
	}

	public static void turnRight()
	{
		Motor.B.backward();
		Motor.A.forward();
	}

	public static void turnRight(int msec) throws Exception
	{
		Motor.B.backward();
		Motor.A.forward();
		Thread.sleep(msec);
	}

	public static void goForward() 
	{
		Motor.A.backward();
		Motor.B.backward();
	}

	public static void goForward(int msec) throws Exception
	{
		Motor.A.backward();
		Motor.B.backward();
		Thread.sleep(msec);
	}

	public static void stop()
	{
		Motor.A.stop(true);
		Motor.B.stop(true);
	}

	public static void setSpeed(int speed)
	{
		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
	}

	public static void fetchLightSample()
	{
		lightSensor0.fetchSample(lightSamples[0], 0);
		lightSensor1.fetchSample(lightSamples[1], 0);
	}

	public static void main(String[] args) 
	{
		Sound.beep();
		setSpeed(900);
		int lastBlack = -1;
		int black = 37;
		final int speed = 900;
		//Sound.playSample(music, 100);

		while(readButtons() < 1)
		{
			fetchLightSample();
			//System.out.println((int)(lightSamples[0][0] * 100) + " " + (int)(lightSamples[1][0] * 100) + " ");

			if((int)(lightSamples[0][0] * 100) < black && (int)(lightSamples[1][0] * 100) < black)
			{
				setSpeed(speed);
				goForward();
			}
			else if ((int)(lightSamples[0][0] * 100) < black) 
			{
				setSpeed(speed);
				lastBlack = 0;
				turnLeft();
			}
			else if ((int)(lightSamples[1][0] * 100) < black) 
			{
				setSpeed(speed);
				lastBlack = 1;
				turnRight();
			}
			else if(lastBlack == 0)
			{
				setSpeed(speed);
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

//Legge spillkort i svingen. Gyrosensor? Stor sving = 