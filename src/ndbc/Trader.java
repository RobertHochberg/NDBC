package ndbc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.print.attribute.standard.Finishings;
import javax.swing.Timer;

public class Trader extends Thread{
	Portal portal;
	UTeamPanel upanel;
	String[] usernames = {"aborse", "asokol","bholdridge","bmccutchon","jbaumann","jwilson","jyamauchi","kbeine","mbolot","rgrosso"};
	//////Database Connection Info/////////
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
	//////////////////////////////////////	

	/*
	 * Change the syncing
	 * Check what the game timer is when a new post is recieved.
	 * if gametimer < 10 need to make new clock for selling
	 * that has a start time ten seconds earlier than the origional clock. (startTime += 10000) 
	 * check for new messages every 5 seconds from then on 
	 * 
	 */


	Map<String,Stock> Stocks  = new HashMap<String, Stock>();
	Integer AMOUNT = 100;
	volatile int time = 0;
	boolean STOP = false;
	volatile boolean wait = true;
	int turn =0;
	String myOldMessage = "";
	int localTime = 0;
	boolean synced = false;
	int numMessagesRecieved = 0;
	Timer syncTimer;
	volatile int syncTime = 0;
	volatile int base = 1000000000;
	int POSTTIME = 0;
	volatile boolean finishedTurn = true;


	@Override
	public synchronized void run(){

		time = portal.gameTimer.timeLeft;
		Timer localTimer = new Timer(1000, new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				time = (base) % 20;
				base -=1;
				
			}
			
		});
		
		
		while(!STOP){
			time = portal.gameTimer.timeLeft;

			/*
			if(!synced){
				Message m = getMyNewSecretMessage();
				String b = m.body;
				if(b != null){
					if(!myOldMessage.contentEquals(b)){
						System.out.println("QQQQQQQ:" + b + "-------"+ myOldMessage);
						postMySecretMessage();
						numMessagesRecieved += 1;
						myOldMessage = b;
						if(numMessagesRecieved == 2){
							if(portal.gameTimer.timeLeft < 16){
								System.out.println(portal.gameTimer.timeLeft);
								int A = 16 - portal.gameTimer.timeLeft;
								base += A;
								base += time;
								System.out.println(time);
								localTimer.start();
							}
							synced = true;
						}
					}
				}
				try{
					Thread.sleep(500);
				}catch(InterruptedException e){
					
				}
			}
*/


				
				//time = syncTime;
				if(!wait && !finishedTurn){
					wait = true;
					ArrayList<String> secretMessages = getTopSecretMessages();
					System.out.println(Stocks.size());
					//UserData.populateHoldingsFromDatabase();
					//portal.getNetWorths();
					//portal.updateHoldingsPanel();
					updateStocks(secretMessages);
					turn +=1;
					finishedTurn = true;
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			
			if(time % 1 == 0){
				String secretMessage = getMyNewSecretMessage().body;
				if(!secretMessage.contentEquals(myOldMessage)){
					postMySecretMessage();
					upanel.shareSecrets.setText(""+ time);	
					myOldMessage = secretMessage;
				}
			}
			try {
		
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();

			}
			if(time <= 2 && wait){
				wait = false;
			}
			if(time< 18 && time >10){
				finishedTurn = false;
			}

			//upanel.shareSecrets.setText(""+ time);	
		}
		

	}

	public Trader(Portal portal, UTeamPanel upanel){
		this.portal = portal;
		this.upanel = upanel;
	}

	public void syncGameTimer(){
		System.out.println("Synced at " + portal.gameTimer.timeLeft);
		base += 16 - portal.gameTimer.timeLeft;
		int sec = 0;
		do{
			Date date = new Date();
			sec = date.getSeconds();
			System.out.println(sec);

		}
		while ((sec %20) != 17 );
		syncTimer.start();


	}


	public void updateStocks(ArrayList<String> secretMessages){
		secretMessages = filterMessages(secretMessages);
		Collections.reverse(secretMessages);
		for(String message : secretMessages){
			String[] stocks = message.split(" ");
			for(String stock : stocks){
				String name = stock.split(":")[0];
				String prediction = stock.split(":")[1];
				String[] predictionStr = prediction.split("-");
				if(!Stocks.containsKey(name)){
					Stock createStock = new Stock(name);
					Stocks.put(name, createStock);
				}
				Stock myStock = Stocks.get(name);
				if(newInfo(myStock, predictionStr)){
					shiftPrices(myStock);
					getCurrentPrice(myStock);
					myStock.predictions[0] = Double.parseDouble(predictionStr[0]);
					myStock.predictions[4] = Double.parseDouble(predictionStr[1]);
					myStock.predictions[19] = Double.parseDouble(predictionStr[2]);
					myStock.futurePrices[0] = Double.parseDouble(predictionStr[0]) * myStock.currentPrice / 100.0;
					myStock.futurePrices[4] = Double.parseDouble(predictionStr[1]) * myStock.currentPrice /100.0;
					myStock.futurePrices[19] = Double.parseDouble(predictionStr[2]) * myStock.currentPrice / 100.0;
					if(turn>4){
						System.out.println("BUYING or Selling");
						makeTransaction(myStock);

					}
				}
			}


		}
	}
	private ArrayList<String> filterMessages(ArrayList<String> messages){
		Set<String> set = new LinkedHashSet<>(messages);
		ArrayList<String> rv = new ArrayList<String>(set);
		return rv;
	}

	private void sell(Stock myStock){
		String insert = "INSERT INTO transactions(salePrice, quantity, buySell, symbol, username) " +
				"VALUES(?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(insert)) {

			int salePrice = (int)(100 * portal.stockOrders.get(myStock.name).getPrice()); // price in cents

			statement.setInt(1, salePrice);
			statement.setInt(2, AMOUNT);
			statement.setString(3, "sell");
			statement.setString(4, myStock.name);
			statement.setString(5,  UserData.USER);
			statement.addBatch();
			statement.executeBatch();


		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void buy(Stock myStock){
		String insert = "INSERT INTO transactions(salePrice, quantity, buySell, symbol, username) " +
				"VALUES(?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(insert)) {
			int salePrice = (int)(100 * portal.stockOrders.get(myStock.name).getPrice()); // price in cents
			statement.setInt(1, salePrice);
			statement.setInt(2, AMOUNT);
			statement.setString(3, "buy");
			statement.setString(4, myStock.name);
			statement.setString(5,  UserData.USER);
			statement.addBatch();


			int[] rowsChanged = statement.executeBatch();
			for(int i : rowsChanged) if(i == 0) System.out.println("Error Occurred in making buys.");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void makeTransaction(Stock myStock){
		if(myStock.futurePrices[0] < myStock.futurePrices[1])
			buy(myStock);
		else if(myStock.futurePrices[0] > myStock.futurePrices[1])
			sell(myStock);

	}

	private void getCurrentPrice(Stock myStock){
		myStock.currentPrice = portal.stockOrders.get(myStock.name).getPrice() / 100.0;
		return;
	}

	private void shiftPrices(Stock myStock){
		for(int i=0;i<19;i++){
			myStock.futurePrices[i] = myStock.futurePrices[i+1];
			myStock.predictions[i] = myStock.predictions[i+1];
		}
	}


	public boolean newInfo(Stock stock, String[] predictions){
		if(stock.predictions[0] == Double.parseDouble(predictions[0]) &&
				stock.predictions[4] == Double.parseDouble(predictions[1]) &&
				stock.predictions[19] == Double.parseDouble(predictions[2])){
			return false;
		}
		return true;
	}

	public void postMySecretMessage(){
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String message = AES.encrypt(getMyNewSecretMessage().body.trim());
		String insert = "INSERT INTO u1 (username, secmessage) " +
				"VALUES(?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(insert)) {

			statement.setString(1, UserData.USER);
			statement.setString(2, message);
			statement.addBatch();
			statement.execute();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("POST " + message);

	}

	public Message getMyNewSecretMessage(){
		Message secretMessage = new Message(0,null,null,null);
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Retrieve my most recent secret message
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT recipient, " + UserData.USER + " FROM secretMessages " +
					"WHERE recipient='" + UserData.USER + "' AND " +
					"messageId = (SELECT MAX(messageId) from secretMessages where recipient = '" + UserData.USER + "');";
			ResultSet resultSet = statement.executeQuery(queryString);
			if(resultSet.next())
				secretMessage = new Message(0, null, resultSet.getString(2), resultSet.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//System.out.println("HERE IS MY SECMSG: " + secretMessage.body);
		return secretMessage;
	}

	public ArrayList<String> getTopSecretMessages(){
		ArrayList<String> secMessages = new ArrayList<String>();

		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//String message = AES.encrypt(getNewSecretMessage().body.trim());

		for(String user : usernames){
			try (Statement statement = connection.createStatement()) {
				//"SELECT username, secmessage, time from u1 where time >= DATE_SUB(NOW(), INTERVAL 60 MINUTE) ORDER BY time DESC;"
				String queryString = "SELECT username, secmessage, time from u1 where time >= DATE_SUB(NOW(), INTERVAL 1 MINUTE) and username = '" + user + "' ORDER BY time DESC limit 1;";

				ResultSet resultSet = statement.executeQuery(queryString);

				while (resultSet.next()) {
					String username = resultSet.getString(1);
					String str = resultSet.getString(2);
					str = AES.decrypt(str.trim());
					secMessages.add(str);
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		for(String s: secMessages)
			System.out.println(s.toString());

		return secMessages;
	}


	public class Stock {
		String name;
		double currentPrice;
		double[] futurePrices = new double[20];
		double[] predictions = new double[20];



		public Stock(String name){
			this.name = name;
		}


	}
}
