//MB (18.09.18) - endret variabel- og metodenavn fra norsk til engelsk. andre endringer i kommentarer p� relevant sted

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.ev3.EV3;
import lejos.hardware.Keys;
import lejos.hardware.lcd.*;
import lejos.hardware.motor.*;
import lejos.hardware.port.SensorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;

//bare importere hele lejos.hardware.* istedet?
//MB - enig, men det funker ikke
//import lejos.hardware.*;

import lejos.robotics.SampleProvider;
import java.util.Random; //brukes ikke, fjern
import java.util.Arrays; //brukes ikke, fjern

//http://www.legoengineering.com/nxt-sensors/ for mer info p� sensorer funker

public class ev3api {

	//Registrere hovedenhet
	public static Brick brick = BrickFinder.getDefault();
	public static EV3 ev3 = (EV3)BrickFinder.getLocal();

	//praktiske ting � ha
	public static TextLCD lcd = ev3.getTextLCD();
	public static Keys keys = ev3.getKeys();

	//registrering av sensorporter
	public static Port s1 = brick.getPort("S1");
	public static Port s2 = brick.getPort("S2");
	public static Port s3 = brick.getPort("S3");
	public static Port s4 = brick.getPort("S4");

	//registrering av fargesensor
	public static EV3ColorSensor colorSensor = new EV3ColorSensor(s1);
	public static SampleProvider colorReader = colorSensor.getMode("RGB");
	public static float[] colorSample = new float[colorReader.sampleSize()];

	//registrering av 2x trykksensorer'
	//MB - endret til to arrays istedenfor frittst�ende sensorer og samples for enklere interaksjon
	public static SampleProvider[] touchSensors = {new EV3TouchSensor(s2), new EV3TouchSensor(s3)};
	public static float[][] touchSamples = {new float[touchSensors[0].sampleSize()], new float[touchSensors[1].sampleSize()]};

	//registrering av soundsensor
	public static SampleProvider soundSensor = new NXTSoundSensor(s4).getDBMode();
	public static float[] soundSample = new float[soundSensor.sampleSize()];

	//Kalibrere int svartKalibrering etter fargesensor, Husk � bruke realistisk setting n�r dette gj�res!
	public static int blackCalibration() throws Exception 
	{
		int black = 0;
		for (int i = 0; i < 100; i++){
			colorReader.fetchSample(colorSample, 0);
			black += (colorSample[0] * 100); //sensor leser ca. 0.00 - 0.02 for en nesten helt sort nyanse.
			Thread.sleep(1); //Offisiel max sample rate er 1kHz, s�rg for at vi har minst ett millisekund  mellom samples.
		}
		black = (black / 100) + 5; //svart blir vanligvis mellom 5 og 7 en plass
		//System.out.println("Colour sensor calibration complete: " + svart + " "); //debug?
		return black;
	}

	//metode for kalibrering av soundsensor til int soundCalibration()
	public static int soundCalibration() 
	{
		int sound = 0;
		for(int i = 0; i < 100; i++){
			soundSensor.fetchSample(soundSample, 0);
			sound += soundSample[0] * 100;
		}
		sound = sound / 100; //rom med personer som prater med h�velig volum kan v�re nede p� 10-ish, klapp i n�rheten av sensor kan n� 55-ish
		//System.out.println("Sound sensor calibration complete: " + sound); //debug?
		return sound;
	}

	//metode for � lese data fra trykksensor. num kan v�re {0, 1}
	//MB - endret fra to forskjellige metoder til �n for � ha mer generelle metoder
	public static float readTouchSensor(int num) 
	{
		touchSensors[num].fetchSample(touchSamples[num], 0);
		return touchSamples[num][0];
	}

	//metode for � hente n�v�rende data fra fargesensor
	public static float readColorSensor() 
	{
		colorReader.fetchSample(colorSample, 0);
		return colorSample[0]; //MB - fjernet "* 100" fordi man skal ha uendrede tall ut av funksjonen, og heller endre dem n�r de skal brukes
	}

	//metode for � hente n�v�rende lydniv�
	public static float readSoundSensor() 
	{
		soundSensor.fetchSample(soundSample, 0);
		return soundSample[0];
	}

	//metode for � vente p� keypress for � g� videre
	public static void waitForKeyPress() throws Exception 
	{
		keys.waitForAnyPress();
		Thread.sleep(200);
	}

	//metode for � lese antall knapper trykket inn
	public static int readButtons() 
	{
		return keys.readButtons();
	}

	public static void main(String[] args) throws Exception 
	{
		//RNGeezus hvis guddomelig inntreden er n�dvendig
		Random rng = new Random();
	}
}