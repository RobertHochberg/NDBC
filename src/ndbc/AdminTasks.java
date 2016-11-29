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
	
	// Hashmap to hold all future price values
	HashMap<Integer, HashMap<String, Integer>> prices;
	
	@Override
	public void run() {
		int UPDATE_DELAY = 5; // in seconds
		int round = 0;
		long lastUpdateTime = -1;
		getHistoricalValues(); // fill the prices hashmap
		
		while(true){
			// Check for new transactions and update if necessary
			System.out.println("Checking Snapshot");
			boolean needToUpdate = checkSnapshot();
			if(needToUpdate){
				System.out.println("Need to update");
				updateSnapshot();
			}

			// Set stock prices from the history table
			if((System.currentTimeMillis() - lastUpdateTime) / 1000 > Constants.GAME_PERIOD){
				lastUpdateTime = System.currentTimeMillis();
				setPricesFromHistory(round);
				round++;
			}

			// Generate and send secret messages
			sendSecretMessages();

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
	static void updateSnapshot(){
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

		// Update net cash of users in users table
		String upUsers = "UPDATE users SET cash = cash + ? WHERE username = ?";
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
		int lastTransactionId = -1;
		for(Transaction t : transactions){
			System.out.println("Transaction ID: " + t.tranasactionId);
			if(t.tranasactionId > lastTransactionId)
				lastTransactionId = t.tranasactionId;
		}
		if(lastTransactionId >= 0){
			try (Statement statement = connection.createStatement()) {
				statement.execute("UPDATE snapshots SET lastTransactionId = " + lastTransactionId + ";");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}


	private void setPricesFromHistory(int round){
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

		// Read all the recent transactions
		HashMap<String, Integer> newPrice = new HashMap<>();
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT stock, price FROM history WHERE round = " + round + ";");
			while(resultSet.next()){
				newPrice.put(resultSet.getString(1), resultSet.getInt(2));	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Set them as the new prices
		String updatePrices  = "UPDATE stocks SET currentPrice = ? WHERE symbol = ?";
		try (PreparedStatement statement = connection.prepareStatement(updatePrices)) {
			for(String s : Constants.stocks){
				statement.setInt(1, newPrice.get(s));
				statement.setString(2, s);
				statement.addBatch();
			}
			int[] x = statement.executeBatch();
			System.out.println("Update prices: " + Arrays.toString(x));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Update the round in the currentRound table
		try (Statement statement = connection.createStatement()) {
			boolean x = statement.execute("UPDATE currentRound SET round = " + round + ";");
			if(!x) System.out.println("Could not update current round in currentRound table");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	/*
	 * Sends hints to managers of stocks by placing messages
	 * into the secretMessages table
	 */
	private void sendSecretMessages(){
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
		
		// First find the current round, according to the database
		int currentRound = 0;
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT round FROM currentRound;";
			ResultSet resultSet = statement.executeQuery(queryString);
			resultSet.next();
			currentRound = resultSet.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Set them as the new prices
		String updatePrices  = "INSERT INTO secretMessages(recipient, body) VALUES(?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(updatePrices)) {
			for(String m : Constants.manages.keySet()){
				String msg = "";
				for(String s : Constants.manages.get(m)){
					HashMap<String, Integer> n0 = prices.get(currentRound);
					HashMap<String, Integer> n1 = prices.get(currentRound + 1);
					HashMap<String, Integer> n5 = prices.get(currentRound + 5);
					HashMap<String, Integer> n20 = prices.get(currentRound + 20);
					msg += String.format("%s:%2f-%2f-%2f ",
						s, 100*((double)n1.get(s))/n0.get(s)-1, 100*((double)n5.get(s))/n0.get(s)-1, 
						100*((double)n20.get(s))/n0.get(s)-1);
				}
				statement.setString(1, m);
				statement.setString(2, msg);
				statement.addBatch();
			}
			int[] x = statement.executeBatch();
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
	 * Populate a "history" of prices. Really the future of prices.
	 * Intended to be run only whenever a new game is starting
	 */
	static void putHistoricalValues(){
		System.out.println("Start History");
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

		double minValue = 0.10;
		double maxValue = 1000.0;
		double drift = 0.485; // 0.5-drift = average growth rate
		double historyDivisor = 20.0;
		double randDivisor = 11.0;

		HashMap<String, Double[]> DPrices = new HashMap<>();
		HashMap<String, Integer[]> IPrices = new HashMap<>();
		for(String s : Constants.stocks){
			DPrices.put(s, new Double[1000]); 
			IPrices.put(s, new Integer[1000]); 
			DPrices.get(s)[0] = 40.0; // Stock all start at $40
			IPrices.get(s)[0] = 4000; // Stock all start at $40
		}
		// Create the next six days
		for(int i = 1; i <= 6; i++){
			for(String stock : Constants.stocks){
				DPrices.get(stock)[i] = DPrices.get(stock)[i-1]*(1 + (Math.random() - 0.5)/10);
				IPrices.get(stock)[i] = (int)(100 * (DPrices.get(stock)[i]) + 0.5);
				//System.out.printf("%4d ", IPrices.get(stock)[i]);
			}
			//System.out.println("");
		}
		// Now the next 993 days.
		for(int i = 7; i <= 999; i++){
			for(String stock : Constants.stocks){
				// Update the prices
				Double[] p = DPrices.get(stock);
				p[i] = Math.min(maxValue, Math.max(minValue, 
						(p[i-1] + (p[i-1]-p[i-5])/historyDivisor) *
						(1 + (Math.random() - drift)/randDivisor)));
				IPrices.get(stock)[i] = (int)(100 * p[i] + 0.5);
				//System.out.printf("%.0f ", p[i]);
			}
			//System.out.println("");
		}
		System.out.println("Built Arrays, putting into DB");

		// Put it into the database
		String insert = "INSERT INTO history(round, stock, price) VALUES(?, ?, ?)";
		int round = 0;
		try (PreparedStatement statement = connection.prepareStatement(insert)) {
			// Create the insert line
			for(int i = 0; i <= 999; i++){
				System.out.print(i + ": ");
				for(String stock : Constants.stocks){
					statement.setInt(1, round);
					statement.setString(2, stock);
					statement.setInt(3, IPrices.get(stock)[i]);
					statement.addBatch();
				}
				round++;
			}
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("DB full.");
	}

	/*
	 * For retrieving future prices from history table
	 * Fills the prices hash map.
	 */
	void getHistoricalValues(){
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

		// Get stock holdings
		prices = new HashMap<>();
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT round, stock, price FROM history;";
			ResultSet resultSet = statement.executeQuery(queryString);
			while (resultSet.next()) {
				int round = resultSet.getInt(1);
				if(!prices.containsKey(round))
					prices.put(round, new HashMap<String, Integer>());
				prices.get(round).put(resultSet.getString(2), resultSet.getInt(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
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


	/*
	 * Written to be used once, to set the quantities in our quantities table.
	 * This is used solely to enforce a constraint on the # of shares that may
	 * be owned to be in the range 0-100.
	 */
	static void populateQuantities(){
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

		// Populate the database
		String insert = "INSERT INTO quantities values(?)";
		int round = 0;
		try (PreparedStatement statement = connection.prepareStatement(insert)) {
			// Create the insert line
			for(int i = 0; i <= 100; i++){
				statement.setInt(1, i);
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
