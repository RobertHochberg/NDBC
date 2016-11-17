package ndbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class UserData {
	static String USER = "root";
	static String PW   = "";

	static HashMap<String, Integer> holdings; // maps stock symbol to #shares

	public static void populateHoldingsFromDatabase(){
		holdings = new HashMap<>();
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
			String queryString = "SELECT stock, quantity FROM owns WHERE username='" + 
					username + "';";
			ResultSet resultSet = statement.executeQuery(queryString);
			while (resultSet.next()) {
				holdings.put(resultSet.getString(1), resultSet.getInt(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateHoldingsPanel(){
		
	}

	
	
	/*
	 * Designed to be used once, to populate the database with 
	 * initial values.
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
}
