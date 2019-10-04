import lejos.hardware.motor.*;
import lejos.hardware.lcd.*;
import lejos.hardware.sensor.*;

import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;

import lejos.hardware.port.Port;
import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;

import lejos.hardware.Keys;
import lejos.hardware.ev3.EV3;

import java.util.Random;
import java.util.Arrays;

public class Hitratunell
{
	public static void main(String[] args) throws Exception
	{
		EV3 ev3 = (EV3)BrickFinder.getLocal();

		TextLCD lcd = ev3.getTextLCD();
		Keys keys = ev3.getKeys();

		Random rng = new Random();

		Brick brick = BrickFinder.getDefault();
		Port s1 = brick.getPort("S1");
		Port s2 = brick.getPort("S2");
		Port s3 = brick.getPort("S3");
		Port s4 = brick.getPort("S4");

 		//Fargesensor
		EV3ColorSensor fargesensor = new EV3ColorSensor(s1);
		SampleProvider fargeLeser = fargesensor.getMode("RGB");
		float[] fargeSample = new float[fargeLeser.sampleSize()];

		//Trykksensor
		SampleProvider trykksensor1 = new EV3TouchSensor(s2);
		SampleProvider trykksensor2 = new EV3TouchSensor(s3);
		float[] trykkSample1 = new float[trykksensor1.sampleSize()];
		float[] trykkSample2 = new float[trykksensor2.sampleSize()];

		Motor.A.setSpeed(900);  
		Motor.B.setSpeed(900);
		Motor.C.setSpeed(900);

		Motor.C.backward();

        // Beregn verdi for svart
		int svart = 0;
		for (int i = 0; i < 100; i++){
			fargeLeser.fetchSample(fargeSample, 0);
			//System.out.println(fargeSample[0] * 100);
			svart += fargeSample[0] * 1000;
			Thread.sleep(3);
		}
		svart = svart / 100 + 5;
		System.out.println("Colour sensor calibration complete: " + svart + " ");

		//Sound sensor calibration
		SampleProvider soundSensor = new NXTSoundSensor(s4).getDBMode();

		int sound = 0;
		float[] soundSample = new float[soundSensor.sampleSize()];

		for(int i = 0; i < 100; i++)
		{
			soundSensor.fetchSample(soundSample, 0);
			//System.out.println(soundSample[0]);
			sound += soundSample[0] * 100;
		}
		sound = sound / 100;
		System.out.println("Sound sensor calibration complete: " + sound);

		keys.waitForAnyPress();
		Thread.sleep(200);

		int soundCounter = 0;
		float avgSound = 0f;

		for (int i = 0; i < 100; i++) 
		{
			soundSensor.fetchSample(soundSample, 0);
			avgSound = averageSound(soundSample[0], i);
		}

		while(true)
		{
			trykksensor1.fetchSample(trykkSample1, 0);
			trykksensor2.fetchSample(trykkSample2, 0);
			fargeLeser.fetchSample(fargeSample, 0);
			soundSensor.fetchSample(soundSample, 0);

			if(trykkSample1[0] > 0 || trykkSample2[0] > 0)
			{
				Motor.A.stop();
				Motor.B.stop();

				Motor.A.backward();
				Motor.B.backward();
				Thread.sleep(500);

				int r = rng.nextInt(2);

				switch(r)
				{
					case 0:
					Motor.A.forward();
					Motor.B.backward();
					break;
					case 1:
					Motor.B.forward();
					Motor.A.backward();
					break;
					default:
					break;
				}
				System.out.println("I'm under pressure");
				Thread.sleep(1000);
			}
			else if (fargeSample[0] * 1000 < svart) 
			{
				Motor.A.stop();
				Motor.B.stop();
				Motor.C.stop();
				System.out.println("Stoppelopp! " + fargeSample[0] * 1000);
				//keys.waitForAnyPress();
				Thread.sleep(200);
			}
			else if(soundSample[0] * 100 > 45)//(soundSample[0] * 100 > avgSound + 200 / avgSound)
			{
				Motor.A.stop();
				Motor.B.stop();
				Motor.C.stop();
				System.out.println(soundSample[0] * 100);
				System.out.println("Lyd for faen!");
				Thread.sleep(2500);
			}
			else
			{
				Motor.A.forward();
				Motor.B.forward();
				Motor.C.backward();
			}

			if(keys.readButtons() > 0)
			{
				Motor.A.stop();
				Motor.B.stop();
				Motor.C.stop();
				System.out.println("You really push my buttons");
				Thread.sleep(10000);
				return;
			}

			avgSound = averageSound(soundSample[0], soundCounter);
			soundCounter++;
			soundCounter %= 100;		//soundCounter = soundCounter % 10;
		}
	}

	static float[] soundAverage = new float[100];

	static float averageSound(float sample, int i)
	{
		float avgSound = 0f;

		soundAverage[i] = sample * 100;

		for (int j = 0; j < soundAverage.length; j++) 
		{
			avgSound += soundAverage[j];
		}

		avgSound /= soundAverage.length;

		/*System.out.print("{ ");
		for(int j = 0; j < 100; j++)
		{
			System.out.print(soundAverage[j] + " ");
		}
		System.out.println("} " + avgSound);
		*/
		System.out.println(avgSound);
		return avgSound;
	}
}