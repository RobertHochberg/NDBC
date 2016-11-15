package ndbc;

import java.util.TimerTask;

public class GameTimer extends Thread {
	Portal portal;
	int timeLeft;
	long startTime;

	public GameTimer(Portal portal) {
		this.portal = portal;
		this.timeLeft = 60;
		portal.secondsLeft = timeLeft;
		startTime = System.currentTimeMillis();
	}

	@Override
	public void run() {
		while(true){
			long elapsedTime = System.currentTimeMillis() - startTime;
			timeLeft = (int)(60 - elapsedTime/1000.0);
			portal.secondsLeft = timeLeft;
			portal.updateStatusPanel();

			// Check to see if we have to do work
			if(timeLeft < 0){
				
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
