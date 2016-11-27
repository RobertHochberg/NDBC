package ndbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class UserData {
	static String USER = "root";
	static String PW   = "ndbcAdmin()";

	static HashMap<String, Integer> holdings; 	// maps stock symbol to #shares

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
}
