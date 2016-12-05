package ndbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimerTask;

public class GameTimer extends Thread {
	Portal portal; 	// Reference to the parent Portal
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
		int state, PULL=1, WAIT=2, ORDER=3;
		state = PULL;
		portal.setOrderable(false);
		while(true){
			long elapsedTime = System.currentTimeMillis() - startTime;
			timeLeft = (int)(Constants.GAME_PERIOD - elapsedTime/1000.0);
			portal.secondsLeft = timeLeft;
			portal.updateStatusPanel();

			// If State == PULL, update from the database
			if(timeLeft > 10 && state == PULL){
				UserData.populateHoldingsFromDatabase();
				portal.updateHoldingsPanel();
				state = WAIT;
				portal.setOrderable(true);
			}
			
			// Check to see if there are orders to do
			if(timeLeft < 1 && state == WAIT){
				state = ORDER;
				portal.setOrderable(false);
				// Create transactions
				makeBuys();
				makeSells();
				 // Process transactions
				portal.updateHoldingsPanel();
				
				startTime = System.currentTimeMillis();
				state = PULL;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	void makeBuys(){
		// TODO: Don't connect if we don't need to. 
		String instanceConnectionName = "mineral-brand-148217:us-central1:first";
		String databaseName = "ndbc";
		String username = UserData.USER;
		String password = UserData.PW;
		String jdbcUrl = String.format(
				"jdbc:mysql://google/%s?cloudSqlInstance=%s&"
						+ "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
						databaseName,
						instanceConnectionName);

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String insert = "INSERT INTO transactions(salePrice, quantity, buySell, symbol, username) " +
				"VALUES(?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(insert)) {
			for(String stock : Constants.stocks){
				int newAmount = portal.stockOrders.get(stock).getDesiredNumShares();
				int currentAmount = UserData.holdings.get(stock);
				int salePrice = (int)(100 * portal.stockOrders.get(stock).getPrice()); // price in cents
				if(newAmount > UserData.holdings.get(stock)){
					statement.setInt(1, salePrice);
					statement.setInt(2, newAmount - currentAmount);
					statement.setString(3, "buy");
					statement.setString(4, stock);
					statement.setString(5,  UserData.USER);
					statement.addBatch();
				}
			}
			int[] rowsChanged = statement.executeBatch();
			for(int i : rowsChanged) if(i == 0) System.out.println("Error Occurred in making buys.");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	void makeSells(){
		// TODO: Don't connect if we don't need to. 
		String instanceConnectionName = "mineral-brand-148217:us-central1:first";
		String databaseName = "ndbc";
		String username = UserData.USER;
		String password = UserData.PW;
		String jdbcUrl = String.format(
				"jdbc:mysql://google/%s?cloudSqlInstance=%s&"
						+ "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
						databaseName,
						instanceConnectionName);

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String insert = "INSERT INTO transactions(salePrice, quantity, buySell, symbol, username) " +
				"VALUES(?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(insert)) {
			for(String stock : Constants.stocks){
				int newAmount = portal.stockOrders.get(stock).getDesiredNumShares();
				int currentAmount = UserData.holdings.get(stock);
				int salePrice = (int)(100 * portal.stockOrders.get(stock).getPrice()); // price in cents
				if(newAmount < UserData.holdings.get(stock)){
					statement.setInt(1, salePrice);
					statement.setInt(2, currentAmount - newAmount);
					statement.setString(3, "sell");
					statement.setString(4, stock);
					statement.setString(5,  UserData.USER);
					statement.addBatch();
				}
			}
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}