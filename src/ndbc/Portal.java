package ndbc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Portal extends JFrame{
	String[] stocks = Constants.stocks;

	// Panel to display the current and next stock holdings
	JPanel holdingsPanel;
	public HashMap<String, HoldingPanel> stockOrders; // Map from symbol to panel

	// Top strip showing current status
	JPanel statusPanel;
	double currentCash;
	double currentNetWorth;
	JLabel currentCashLabel;
	JLabel currentNetWorthLabel;
	public int secondsLeft;
	JLabel secondsLeftLabel;
	String nullpw = "";

	// Message panel
	MessagePanel messagePanel;

	// Schedules the background updater
	GameTimer gameTimer;

	// Schedules the administration tasks
	AdminTasks adminTasks;

	// Player panel. This is customizable by y'all
	JPanel playerPanel;


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
		stockOrders = new HashMap<>();
		for(String s : stocks){
			HoldingPanel hp = new HoldingPanel(s, 0, centsPrices.get(s));
			holdingsPanel.add(hp);
			stockOrders.put(s, hp);	// Add map entry from symbol to the panel
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

		// Set up the messaging panel
		messagePanel = new MessagePanel();
		this.add(messagePanel, BorderLayout.CENTER);

		// Set up the player panel
		playerPanel = new JPanel();
		UTeamPanel uteamPanel = new UTeamPanel();
		uteamPanel.setBackground(Color.white);
		DTeamPanel dteamPanel = new DTeamPanel();
		dteamPanel.setBackground(new Color(0, 35, 102));
		playerPanel.setPreferredSize(new Dimension(300, 800));
		playerPanel.setLayout(new GridLayout(2, 1));
		playerPanel.add(uteamPanel);
		playerPanel.add(dteamPanel);
		this.add(playerPanel, BorderLayout.EAST);

		this.pack();
		this.setVisible(true);
		messagePanel.update();

		//UserData.initializeHoldingsInDatabase();
		UserData.populateHoldingsFromDatabase();
		startGame();
		startAdminTasks();
	}

	/*
	 * Queries the database for the starting prices of our stocks.
	 * Also, a model for sending sql queries.
	 */
	HashMap<String, Integer> getCentsPrices(){
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

		HashMap<String, Integer> rv = new HashMap<>();
		try (Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery("SELECT symbol, currentPrice FROM stocks;");
			while (resultSet.next()) {
				rv.put(resultSet.getString(1), resultSet.getInt(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rv;
	}


	void startGame(){
		gameTimer = new GameTimer(this);
		gameTimer.start();
	}


	void startAdminTasks(){
		adminTasks = new AdminTasks();
		adminTasks.start();
	}

	void updateStatusPanel(){
		this.currentCashLabel.setText(String.valueOf(currentCash));
		this.currentNetWorthLabel.setText(String.valueOf(currentNetWorth));
		this.secondsLeftLabel.setText(String.valueOf(secondsLeft));
		this.statusPanel.repaint();
	}

	/*
	 * Looks at the holdings hash map in UserData and updates the panels
	 * accordingly.
	 */
	public void updateHoldingsPanel(){
		HashMap<String, Integer> centsPrices = getCentsPrices();
		for(String stock : Constants.stocks){
			stockOrders.get(stock).setNumShares(UserData.holdings.get(stock));
			stockOrders.get(stock).setPrice(centsPrices.get(stock));
		}
	}
	
	
	/*
	 * Make all the HoldingPanels on the panel manipulable.
	 */
	void setOrderable(boolean b){
		for(String s : Constants.stocks){
			this.stockOrders.get(s).enable(b);
		}
	}
}