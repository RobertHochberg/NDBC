package ndbc;

public class Basics extends Thread {
	
	DTeamPanel panel;
	long startTime;
	boolean live;
	
	public Basics(DTeamPanel d){
		panel = d;
	}
	
	@Override
	public void run() {
		startTime = System.currentTimeMillis() + 5000;
		int timeleft;
		live = true;
		while(live){
			
			timeleft = (int)((System.currentTimeMillis() - startTime) / 1000);
			
			if(timeleft > 10){
				panel.updateBotofPower();
				startTime = System.currentTimeMillis();
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void kill(){
		live = false;
	}

}
