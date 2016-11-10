package ndbc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Portal extends JFrame{
	String[] stocks = {"ONC", "EUP", "ONA", "MID", "NIG",
			"HTD", "REA", "RYA", "SIP", "OND", 
			"ERE", "DWE", "AKA", "NDW", "EAR",
			"YOV", "ERM", "ANY", "AQU", "AIN",
			"TAN", "DCU", "RIO", "OUS", "VOL",
			"UME", "OFF", "ORG", "OTT", "ENL"};

	// Panel to display the current and next stock holdings
	JPanel holdingsPanel;

	// Top strip showing current status
	JPanel statusPanel;
	double currentCash;
	double currentNetWorth;
	JLabel currentCashLabel;
	JLabel currentNetWorthLabel;
	int secondsLeft;
	JLabel secondsLeftLabel;
	String nullpw = "";

	public static void main(String[] args) {
		new Portal();

	}

	public Portal(){
		// Set up the JFrame
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1200, 800));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set up the Holdings Panel
		holdingsPanel = new JPanel();
		holdingsPanel.setLayout(new GridLayout(10, 3));
		HashMap<String, Integer> centsPrices = getCentsPrices();
		for(String s : stocks){
			HoldingPanel hp = new HoldingPanel(s, 0, centsPrices.get(s));
			holdingsPanel.add(hp);
		}
		this.add(holdingsPanel, BorderLayout.WEST);

		// Set up the Status Panel
		statusPanel = new JPanel();
		statusPanel.setPreferredSize(new Dimension(1100, 30));
		this.currentCash = 1000.0;
		this.currentNetWorth = 1000.0;
		this.currentCashLabel = new JLabel();
		this.currentCashLabel.setText(String.valueOf(currentCash));
		this.currentNetWorthLabel = new JLabel();
		this.currentNetWorthLabel.setText(String.valueOf(currentNetWorth));
		secondsLeft = 60;
		this.secondsLeftLabel = new JLabel();
		this.secondsLeftLabel.setText(String.valueOf(secondsLeft));

		statusPanel.add(new JLabel("Cash"));
		statusPanel.add(currentCashLabel);
		statusPanel.add(new JLabel("Net Worth"));
		statusPanel.add(currentNetWorthLabel);
		statusPanel.add(secondsLeftLabel);
		this.add(statusPanel, BorderLayout.NORTH);


		this.pack();
		this.setVisible(true);
		test();
	}
	
	/*
	 * Queries the database for the starting prices of our stocks.
	 * Also, a model for sending sql queries.
	 */
	HashMap<String, Integer> getCentsPrices(){
		String instanceConnectionName = "mineral-brand-148217:us-central1:first";
		String databaseName = "ndbc";
		String username = "jwilson";
		String password = "fake";
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

		HashMap<String, Integer> rv = new HashMap<>();
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("select symbol, price from ndbc.startPrices;");
			while (resultSet.next()) {
				rv.put(resultSet.getString(1), resultSet.getInt(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rv;
	}

	

	/*
	 * Written to be used once, to set the starting prices of our 30 stocks.
	 */
	void setCentsPrices(){
		String instanceConnectionName = "mineral-brand-148217:us-central1:first";
		String databaseName = "ndbc";
		String username = "jwilson";
		String password = "fake";
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
			for(String s : stocks){
				int price = ((int)(Math.random()*1000));
				statement.execute("INSERT INTO ndbc.startPrices VALUES('" + s + "','" + price + "');");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Delete soon...
	 */
	void test(){
		String instanceConnectionName = "mineral-brand-148217:us-central1:first";
		String databaseName = "ndbc";
		String username = "jwilson";
		String password = "fake";
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
			ResultSet resultSet = statement.executeQuery("show tables from ndbc;");
			while (resultSet.next()) {
				System.out.println(resultSet.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
