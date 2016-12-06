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
	//static String USER = "jwilson";
	//static String PW   = "";
	

	static HashMap<String, Integer> holdings; 	// maps stock symbol to #shares
	static int currentCash;						// To buy stocks
	static int currentNetWorth;					// cash + stocks

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

		// Get stock holdings
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT stock, quantity, currentPrice " +
					"FROM owns JOIN stocks ON owns.stock = stocks.symbol " +
					"WHERE username='" + username + "';";
			ResultSet resultSet = statement.executeQuery(queryString);
			currentNetWorth = 0;
			while (resultSet.next()) {
				holdings.put(resultSet.getString(1), resultSet.getInt(2));
				currentNetWorth += resultSet.getInt(2) * resultSet.getInt(3);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Get current amount of cash
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT cash FROM users WHERE username='" + 
					username + "';";
			ResultSet resultSet = statement.executeQuery(queryString);
			resultSet.next();
			currentCash = resultSet.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		currentNetWorth += currentCash;
	}
}
