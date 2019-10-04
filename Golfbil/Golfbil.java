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

public class Golfbil
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

		Motor.C.forward();

        // Beregn verdi for svart
		int svart = 0;
		for (int i = 0; i < 100; i++){
			fargeLeser.fetchSample(fargeSample, 0);
			svart += fargeSample[0]* 100;
		}
		svart = svart / 100 + 5;
		//System.out.println("Svart: " + svart);

		while(true)
		{
			trykksensor1.fetchSample(trykkSample1, 0);
			trykksensor2.fetchSample(trykkSample2, 0);

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
				Thread.sleep(1000);
			}
			else
			{
				Motor.A.forward();
				Motor.B.forward();
			}

			if(keys.readButtons() > 0)
			{
				break;
			}
		}
	}
}