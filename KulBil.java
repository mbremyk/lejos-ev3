import lejos.hardware.motor.*;
import lejos.hardware.lcd.*;
import lejos.hardware.sensor.*;

import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

class KulBil
{
	private static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);

	public static void main(String[] args) throws Exception 
	{
		float[] f = new float[1];
		gyroSensor.reset();
		Motor.A.setSpeed(450);  
        Motor.C.setSpeed(450);

        Motor.A.forward();  
        Motor.C.forward();  
        gyroSensor.getAngleMode().fetchSample(f, 0);
        while(Math.abs(f[0]) < 5) 
        {
            gyroSensor.getAngleMode().fetchSample(f, 0);
            System.out.println(f[0]);
            Thread.yield();
        }

        Motor.A.stop();
        Motor.B.stop();
	}
}