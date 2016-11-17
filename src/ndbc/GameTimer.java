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
		int ORDER_PERIOD = 20; // Must be at least 20
		int state, ORDER=1, CHECK=2;
		state = 2;
		while(true){
			long elapsedTime = System.currentTimeMillis() - startTime;
			timeLeft = (int)(ORDER_PERIOD - elapsedTime/1000.0);
			portal.secondsLeft = timeLeft;
			portal.updateStatusPanel();

			if(timeLeft < ORDER_PERIOD-10 && state == CHECK){
				state = ORDER;
				UserData.populateHoldingsFromDatabase();
				UserData.updateHoldingsPanel();
			}
			// Check to see if we have to do work
			if(timeLeft < 0){
				makeBuys();
				makeSells();

				startTime = System.currentTimeMillis();
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
			statement.executeBatch();
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