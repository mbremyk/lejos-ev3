/**
* SampleUpdater.java
*
* Oppdaterer arrays ***USSample for ev3 med nye verdier
* fra de ultrasoniske sensorene
*
* @author Aleksander Skogan
*/


//Create only one instance of this thread!
class SampleUpdater extends Thread {

	//latskap v1.1
	void err(String s) {
		System.err.print(s);
	}

	void errln(String s) {
		System.err.println(s);
	}

	void out(String s) {
		System.out.print(s);
	}

	void outln(String s) {
		System.out.println(s);
	}

	//tråd-spesifik sovemetode, prøver å opprettholde en oppdateringsfrekvens på 33Hz
	void sleep(int ms) {
		if (ms < 0) ms = 0;
		if (ms > 30) ms = 30;
		try {
			Thread.sleep(30 - ms);
		} catch (Exception threadSleepException) {
			err(threadSleepException.toString());
			outln(threadSleepException.toString());
		}
	}

	//evig oppdatering av samples så lenge interrupted() returnerer false
	public void run() {
		long millisStart;
		while (!interrupted()) {
			try {
				millisStart = System.currentTimeMillis();

				Main.ev3.updateSamples();

				sleep(System.currentTimeMillis() - millisStart);
			} catch (Exception e) {
				//skrive feilmeldinger men fortsett å kjøre programmet
				errln(e.toString());
				outln(e.toString());
			}
		}
	}
}