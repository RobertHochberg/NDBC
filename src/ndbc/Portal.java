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
	double currentOrder;
	JLabel currentCashLabel;
	JLabel currentNetWorthLabel;
	JLabel currentOrderLabel;
	public int secondsLeft;
	JLabel secondsLeftLabel;
	JLabel UWorthLabel;
	JLabel DWorthLabel;
	int UWorth; // Net worth of team
	int DWorth; // Net worth of team
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
		initialize();
		new Portal();

	}

	// Do some initialization tasks before creating the portal
	private static void initialize(){
		// Build the manages hashmap
		Constants.manages = new HashMap<>();
		for(int i = 0; i < 30; i++){
			if(!Constants.manages.containsKey(Constants.users[i/3]))
				Constants.manages.put(Constants.users[i/3], new String[3]);
			Constants.manages.get(Constants.users[i/3])[i%3] = Constants.stocks[i];
		}
	}

	public Portal(){
		// Set up the JFrame
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1200, 800));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set up the Holdings Panel
		holdingsPanel = new JPanel();
		holdingsPanel.setLayout(new GridLayout(10, 5));
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
		this.currentOrder = 0.0;
		this.currentCashLabel = new JLabel();
		this.currentCashLabel.setText(String.valueOf(UserData.currentCash));
		this.currentNetWorthLabel = new JLabel();
		this.currentNetWorthLabel.setText(String.valueOf(UserData.currentNetWorth));
		this.currentOrderLabel = new JLabel();
		this.currentOrderLabel.setText(String.valueOf(currentOrder));
		secondsLeft = 60;
		this.secondsLeftLabel = new JLabel();
		this.secondsLeftLabel.setText(String.valueOf(secondsLeft));
		this.UWorthLabel = new JLabel();
		UWorthLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		this.DWorthLabel = new JLabel();
		DWorthLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));

		statusPanel.setLayout(new GridLayout(1, 5));
		statusPanel.add(currentCashLabel);
		statusPanel.add(currentNetWorthLabel);
		statusPanel.add(secondsLeftLabel);
		statusPanel.add(UWorthLabel);
		statusPanel.add(DWorthLabel);
		this.add(statusPanel, BorderLayout.NORTH);

		// Set up the messaging panel
		messagePanel = new MessagePanel();
		this.add(messagePanel, BorderLayout.CENTER);

		// Set up the player panel
		playerPanel = new JPanel();
		UTeamPanel uteamPanel = new UTeamPanel(this);
		uteamPanel.setBackground(Color.white);
		DTeamPanel dteamPanel = new DTeamPanel(this);
		dteamPanel.setBackground(new Color(0, 35, 102));
		playerPanel.setPreferredSize(new Dimension(300, 800));
		playerPanel.setLayout(new GridLayout(2, 1));
		playerPanel.add(uteamPanel);
		playerPanel.add(dteamPanel);
		this.add(playerPanel, BorderLayout.EAST);

		this.pack();
		this.setVisible(true);
		messagePanel.update();

		//AdminTasks.managePrivileges();
		//AdminTasks.putHistoricalValues();
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
		this.currentCashLabel.setText("Cash: $" + String.valueOf(UserData.currentCash/100.0));
		this.currentOrderLabel.setText("Order Amount: " + String.valueOf(currentOrder));
		this.currentNetWorthLabel.setText("Net Worth: $" + String.valueOf(UserData.currentNetWorth/100.0));
		this.secondsLeftLabel.setText("Time until trade: " + String.valueOf(secondsLeft));
		this.UWorthLabel.setText("U: " + String.valueOf(UWorth/100.0));
		this.DWorthLabel.setText("D: " + String.valueOf(DWorth/100.0));
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

	/*
	 * Get the net worth of the two teams
	 */
	public void getNetWorths(){
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
			String queryString = "SELECT users.team, SUM(quantity*currentPrice) " +
					"FROM owns JOIN stocks ON owns.stock=stocks.symbol " +
					"JOIN users ON users.username = owns.username GROUP BY users.team;";
			ResultSet resultSet = statement.executeQuery(queryString);
			while (resultSet.next()) {
				System.out.println(resultSet.getString(1));
				System.out.println(resultSet.getInt(2));
				if(resultSet.getString(1).equals("U"))
					UWorth = resultSet.getInt(2);
				if(resultSet.getString(1).equals("D"))
					DWorth = resultSet.getInt(2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Get cash
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT team, SUM(cash) " +
					"FROM users GROUP BY team;";
			ResultSet resultSet = statement.executeQuery(queryString);
			while (resultSet.next()) {
				System.out.println(resultSet.getString(1));
				System.out.println(resultSet.getInt(2));
				if(resultSet.getString(1).equals("U"))
					UWorth += resultSet.getInt(2);
				if(resultSet.getString(1).equals("D"))
					DWorth += resultSet.getInt(2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}