package ndbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class AdminTasks extends Thread{
	static long startTime = 0;
	static int timeLeft = 0;

	@Override
	public void run() {
		int UPDATE_DELAY = 0; 
		while(true){
			System.out.println("Checking Snapshot");
			boolean needToUpdate = checkSnapshot();
			if(needToUpdate){
				System.out.println("Need to update");
				updateSnapshot();
			}

			try {
				Thread.sleep(1000 * UPDATE_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Returns true if there are transactions that have not yet been
	 * taken into account in the latest snapshots online.
	 */
	boolean checkSnapshot(){
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

		// find the latest transaction ID and latest snapshot ID
		int lastTransactionId = 0, lastSnapshot = 0;
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT MAX(transactionId) FROM transactions;");
			resultSet.next();
			lastTransactionId = resultSet.getInt(1);
			resultSet = statement.executeQuery("SELECT lastTransactionId FROM snapshots;");
			resultSet.next();
			lastSnapshot = resultSet.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return (lastSnapshot < lastTransactionId);
	}


	/*
	 * Reads all transactions committed since the last snapshot, and 
	 * incorporates them into:
	 *   owns
	 *   snapshots
	 *   
	 */
	void updateSnapshot(){
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

		// find the latest snapshot ID
		int lastSnapshot = 0;
		try (Statement statement = connection.createStatement()) {
			// Get the last snapshot time
			ResultSet resultSet = statement.executeQuery("SELECT lastTransactionId FROM snapshots;");
			resultSet.next();
			lastSnapshot = resultSet.getInt(1); 
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Get the stock holdings of all users
		/*
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM owns;" + lastSnapshot + ";");
			HashMap<String, HashMap<String, Integer>> owns = new HashMap<>();
			HashMap<String, HashMap<String, Integer>> toUpdate = new HashMap<>();
			while(resultSet.next()){
				String user = resultSet.getString(1);
				if(!owns.containsKey(user)){
					owns.put(user, new HashMap<String, Integer>());
					toUpdate.put(user, new HashMap<String, Integer>());
				}
				owns.get(user).put(resultSet.getString(2), resultSet.getInt(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}*/

		// Read all the recent transactions
		ArrayList<Transaction> transactions = new ArrayList<>();
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT * FROM transactions WHERE transactionId > " + lastSnapshot + ";");
			while(resultSet.next()){
				transactions.add(new Transaction(resultSet.getInt(1), resultSet.getDate(2),
						resultSet.getInt(3), resultSet.getInt(4), resultSet.getString(5),
						resultSet.getString(6), resultSet.getString(7)));		
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("Updating " + transactions.size() + " transactions.");

		// Get current stock prices
		HashMap<String, Integer> prices = new HashMap<>();
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT symbol, currentPrice FROM stocks;");
			while(resultSet.next()){
				prices.put(resultSet.getString(1), resultSet.getInt(2));		
			}
			System.out.println(prices);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Update owns table to reflect these transactions
		String upOwns  = "UPDATE owns SET quantity = quantity + ? WHERE username = ? AND stock = ?";
		try (PreparedStatement statement = connection.prepareStatement(upOwns)) {
			for(Transaction t : transactions){
				int multiplier = t.buySell.equals("buy") ? +1 : -1;
				statement.setInt(1, multiplier * t.quantity);
				statement.setString(2, t.username);
				statement.setString(3, t.symbol);
				System.out.println(statement.toString());
				statement.addBatch();
			}
			int[] x = statement.executeBatch();
			System.out.println(Arrays.toString(x));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Update net worth of users in users table
		String upUsers = "UPDATE users SET worth = worth + ? WHERE username = ?";
		try (PreparedStatement statement = connection.prepareStatement(upUsers)) {
			for(Transaction t : transactions){
				int multiplier = t.buySell.equals("buy") ? -1 : +1;
				statement.setInt(1, multiplier * t.quantity * prices.get(t.symbol));
				statement.setString(2, t.username);
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Finally, update the lastTransactionId in our snapshot table
		int lastTransactionId = 0;
		for(Transaction t : transactions){
			if(t.tranasactionId > lastTransactionId)
				lastTransactionId = t.tranasactionId;
		}
		try (Statement statement = connection.createStatement()) {
			statement.execute("UPDATE snapshots SET lastTransactionId = " + lastTransactionId + ";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Populates the stocks table
	 * Intended to be run only once.
	 */
	static void populateStocks(){
		System.out.print("Populating Stocks... ");
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

		// Update owns table to reflect these transactions
		String makeStocks  = "INSERT INTO stocks VALUES (?, ?, ?, ?);";
		try (PreparedStatement statement = connection.prepareStatement(makeStocks)) {
			for(int i = 0; i < 30; i++){
				statement.setString(1, Constants.stocks[i]);
				statement.setInt(2, 100);
				statement.setString(3, Constants.companies[i]);
				statement.setString(4, Constants.users[i/3]); // Each user manages 3 companies
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Populated.");
	}



	/*
	 * Designed to be used once, to populate users with 
	 * initial values.UserData
	 */
	public static void initializeHoldingsInDatabase(){
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


		try (Statement statement = connection.createStatement()) {
			statement.clearBatch();
			for(String user : Constants.users){
				statement.addBatch("INSERT INTO users VALUES('" + user + "', 1000);");
				for(String stock : Constants.stocks){
					String queryString = "INSERT INTO owns VALUES('" + user + "', '" + stock + "', 0);";
					statement.addBatch(queryString);
				}
			}
			int[] results = statement.executeBatch();
			System.out.println(results);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/*
	 * Written to be used once, to set the starting prices of our 30 stocks.
	 */
	void setCentsPrices(){
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

		try (Statement statement = connection.createStatement()) {
			for(String s : Constants.stocks){
				int price = ((int)(Math.random()*1000));
				statement.execute("INSERT INTO ndbc.startPrices VALUES('" + s + "','" + price + "');");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
