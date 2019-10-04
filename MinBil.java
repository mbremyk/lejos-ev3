/*    MinBil.java G.S 2011 - 08 - 24
* Program som styrer en bil med 2 motorer. Bilen oppfører seg slik:
* 1. kjør framover
* 2. Rygg
* 3. Sving høyre
* 4. Endre hastighet på motorene
* 5. Kjør framover igjen
*/

import lejos.hardware.motor.*;
import lejos.hardware.lcd.*;
import lejos.hardware.sensor.*;

import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class MinBil
{
    private static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S1);

    public static void main (String[] args)  throws Exception
    {
        float[] f = new float[1];

        System.out.println("Hello World!");
        Thread.sleep(500);

        Motor.A.setSpeed(450);  
        Motor.C.setSpeed(450);

        Motor.A.forward();  
        Motor.C.forward();  
        Thread.sleep(2000); 

        LCD.clear();
        gyroSensor.reset();
        Motor.A.forward();
        Motor.C.stop();
        gyroSensor.getAngleMode().fetchSample(f, 0);
        while(Math.abs(f[0]) < 90) 
        {
            gyroSensor.getAngleMode().fetchSample(f, 0);
            System.out.println(f[0]);
            Thread.yield();
        }

        Motor.A.forward();  
        Motor.C.forward();  
        Thread.sleep(2000); 

        gyroSensor.reset();
        Motor.A.forward();
        Motor.C.stop();
        gyroSensor.getAngleMode().fetchSample(f, 0);
        while(Math.abs(f[0]) < 90) 
        {
            gyroSensor.getAngleMode().fetchSample(f, 0);
            System.out.println(f[0]);
            Thread.yield();
        }

        Motor.A.forward();
        Motor.C.forward();
        Thread.sleep(2000);

        gyroSensor.reset();
        Motor.A.forward();
        Motor.C.stop();
        gyroSensor.getAngleMode().fetchSample(f, 0);
        while(Math.abs(f[0]) < 90) 
        {
            gyroSensor.getAngleMode().fetchSample(f, 0);
            System.out.println(f[0]);
            Thread.yield();
        }

        Motor.A.forward();  
        Motor.C.forward();  
        Thread.sleep(2000); 

        System.out.println("Goodbye World!");
        Thread.sleep(1000);
        Motor.A.stop();
        Motor.C.stop();
    }
}
