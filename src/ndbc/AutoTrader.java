package ndbc;

import jdk.nashorn.internal.ir.annotations.Ignore;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.jar.JarInputStream;

import javax.print.attribute.standard.JobMessageFromOperator;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class AutoTrader extends Thread {
	//XXXX Maybe rework the entre thing. make simpler.  should be predicting based on 
	int turn = 0;
	int lastTurn = -1;
	Portal portal;
	MessagePanel messagePanel;
	JPanel holdingPanel;
	GameTimer timer;
	ArrayList<StockandPred> myStocks;
	String oldSecretMessage = "";
	boolean STOP = false;
	int AMOUNT = 10; /// Amount that will be bought automatically.
	String log = "";
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

	volatile boolean wait;
	
	double oldcash;


	public static void main(String[] args) {
		// TODO Auto-generated method stub


	}

	//@Override
	synchronized public void run(){

		System.out.println("RUN");
		String password = getPassword();
		if(verifyPassword(password) == false)
			return;
		wait = false;
		
		//String[] stocks = getStocksString();

		messagePanel.update();
		String secretMessage = getNewSecretMessage();
		populateMyStocks(secretMessage);
		int time = portal.gameTimer.timeLeft;
		//DebugWindow d = new DebugWindow(this);

		while(!STOP){
			time = portal.gameTimer.timeLeft;
			/*
			if(portal.gameTimer.timeLeft % 4 == 0){
				//for(StockandPred s : myStocks)
					//s.updatePrices();
				portal.messagePanel.update();

				System.out.println("PPP");
				portal.messagePanel.myMessageArea.setText("AQU = " + myStocks.get(0).currentPrice + " \nnext: " + myStocks.get(0).nextPrice +"\nthird: "+
						myStocks.get(0).thirdPrice);
				portal.messagePanel.update();

				System.out.println("TURN: " + turn + " last: " + lastTurn);

			}
			 */
			//portal.updateStatusPanel();
			//System.out.println(String.format("UPDATING %d %d %b %d", lastTurn, turn, wait, portal.gameTimer.timeLeft));
			if(!wait){

				wait = true;
				String message = getNewSecretMessage();
				if(!message.contentEquals(oldSecretMessage)){
					updateStocks(message);
					oldSecretMessage = message;
					UserData.populateHoldingsFromDatabase();
					turn+=1;
					if(turn>=4){
						makeTransactions();
						printStats();
						oldcash = UserData.currentCash;
					}

					/*
                	portal.messagePanel.myMessageArea.setText("AQU = " + myStocks.get(0).currentPrice + " next: " + myStocks.get(0).nextPrice +"\n"+
                	"Turn 5: " + myStocks.get(0).currentPrice * myStocks.get(0).predictions[4] +
                	"\n Turn 5: " + myStocks.get(0).currentPrice * myStocks.get(0).predictions[19]);
                	portal.repaint();
					 */

					//portal.updateHoldingsPanel();
					/*

					int count = 0;
					for(StockandPred stock : myStocks){
						//d.repaint(stock, count);
						count += 20;
					}
					 */
				}

			}else{
				//	if(print == true){
				//	System.out.print("PRINT ");
				//}
				if(time >=12 && time<18 && wait){
					wait = false;
				}
				if(time <= 2 || time >18){
					wait = true;

				}
			}
		}
		System.out.println("Stop");
		this.interrupt();


	}
	
	public void printStats(){
		String AQN, AIN, TAN;
		double cashChange = UserData.currentCash - oldcash;  
		System.out.println("Cash Change: " + cashChange);
		System.out.println(String.format("%s : %f %f %f", myStocks.get(0).name,myStocks.get(0).currentPrice ,myStocks.get(0).nextPrice,myStocks.get(0).thirdPrice));
		System.out.println(String.format("%s : %f %f %f", myStocks.get(1).name,myStocks.get(1).currentPrice ,myStocks.get(1).nextPrice,myStocks.get(1).thirdPrice));
		System.out.println(String.format("%s : %f %f %f", myStocks.get(2).name,myStocks.get(2).currentPrice ,myStocks.get(2).nextPrice,myStocks.get(2).thirdPrice));
		System.out.println("AQN OWN: " + UserData.holdings.get("AQU"));
		System.out.println("AIN OWN: " + UserData.holdings.get("AIN"));
		System.out.println("TAN OWN: " + UserData.holdings.get("TAN"));
		
		
	}


	public AutoTrader(Portal portal){
		this.messagePanel = portal.messagePanel;
		this.holdingPanel = portal.holdingsPanel;
		this.portal = portal;
		this.timer = portal.gameTimer;
	}


	public String getPassword(){
		String pw = (String) JOptionPane.showInputDialog("Enter the password:");
		return pw;

	}

	public String[] getStocksString(){
		String stocksString = (String) JOptionPane.showInputDialog("Enter your stocks that get secrect messages seperated by a space (ex. UAT JUN HYN)");
		String delim = " ";
		String Stocks[] = stocksString.split(delim);

		return Stocks;

	}


	//To bholdridge: AQU:101.71-103.28-91.40 AIN:99.97-90.64-106.26 TAN:99.56-85.81-92.27
	public void populateMyStocks(String message){
		if(myStocks == null){
			myStocks = new ArrayList<StockandPred>();
			String[] pieces = message.split(" ");
			for(int i=0;i<pieces.length;i+=1){
				String stock = pieces[i].trim().substring(0, 3);
				String[] pricesStr = pieces[i].substring(4, pieces[i].length()).split("-");
				ArrayList<Double> vals = new ArrayList<Double>();
				for(String p :pricesStr){
					double val = Double.parseDouble(p);
					vals.add(val);
				}
				StockandPred stockandPred = new StockandPred(stock);
				stockandPred.predictions[0] = vals.get(0);
				stockandPred.predictions[4] = vals.get(1);
				stockandPred.predictions[19] = vals.get(2);
				myStocks.add(stockandPred);
			}

		}
	}

	public boolean verifyPassword(String userPassword){
		String dbPassword = "testing";

		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		// Update public messages
		/*
      try (Statement statement = connection.createStatement()) {
         String queryString = "SELECT password " +
               "FROM u3 WHERE CHAR_LENGHT(password) > " + 8 + ";";
         ResultSet resultSet = statement.executeQuery(queryString);
         while (resultSet.next()) {
            dbPassword = resultSet.getString(1);
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }

		 String mypw = JOptionPane.showInputDialog("Enter the AES Encryption key");
		 
		 */

		if(!userPassword.contentEquals(dbPassword) || userPassword == null)
			return false;
		if(userPassword.contentEquals(dbPassword)){
			JOptionPane.showMessageDialog(null, "Logged in!");
			return true;
		}

		return false;

	}
	//can only be called once per turn or errors occur
	public void updateStocks(String message){
		ArrayList<String> stocks = new ArrayList<String>();
		for(StockandPred ms : myStocks){
			for(int i=0;i<19;i++){
				ms.predictions[i] = ms.predictions[i+1];
				ms.futurePrices[i] = ms.futurePrices[i+1];
			}
		}

		if(myStocks != null){
			String[] pieces = message.split(" ");
			for(int i=0;i<pieces.length;i+=1){
				String stock = pieces[i].trim().substring(0, 3);
				stocks.add(stock);
				String[] pricesStr = pieces[i].substring(4, pieces[i].length()).split("-");
				ArrayList<Double> vals = new ArrayList<Double>();
				for(String p :pricesStr){
					double val = Double.parseDouble(p);
					vals.add(val);
				}
				String nname = stocks.get(0);
				for(StockandPred ms : myStocks){
					for(String name : stocks){
						if(name.contentEquals(ms.name)){
							ms.predictions[0] = vals.get(0);
							ms.predictions[4] = vals.get(1);
							ms.predictions[19] = vals.get(2);
							ms.getCurrentPrice();
							ms.futurePrices[0] = vals.get(0) * ms.currentPrice /100.0;
							ms.futurePrices[4] = vals.get(1) * ms.currentPrice /100.0;
							ms.futurePrices[19] = vals.get(2) * ms.currentPrice /100.0;

						}
					}
					//ms.updateFuturePrices();
					ms.updatePrices();
				}

				stocks.remove(nname);

			}

		}

	}

	public void makeTransactions(){
		for(StockandPred stock: myStocks){
			/*if(stock.nextPrice == stock.maxPrice)
                sellStocks(stock);
            if(stock.nextPrice != stock.maxPrice)
                buyStocks(stock);

        	ArrayList<Double> filter = new ArrayList<Double>();
        	filter.add(0.0);
        	ArrayList<double[]> sorted = new ArrayList<double[]>(Arrays.asList(stock.futurePrices));
        	sorted.removeAll(filter);
			 */

			if(stock.currentPrice > stock.nextPrice){
				System.out.println("SELL: " + stock.currentPrice + " " + stock.nextPrice + " " + stock.thirdPrice);
				sellStocks(stock);
			}
			if(stock.currentPrice <= stock.nextPrice){
				
				buyStocks(stock);
				
				System.out.println("BUY: " + stock.name + " " +stock.currentPrice + " " + stock.nextPrice + " " + stock.thirdPrice);
			}



		}

	}

	public void buyStocks(StockandPred myStock){
		/*
        Container container = portal.holdingsPanel;
        Component[] components = container.getComponents();
        for(int i=0;i<components.length;i++){
            if(components[i] instanceof HoldingPanel){
                HoldingPanel hp = (HoldingPanel) components[i];
                if(hp.name.contentEquals(stock.name)){
                    int val = (int) hp.orderSpinner.getValue();
                    hp.orderSpinner.setValue(val + AMOUNT);

                }
             }

            }*/
		System.out.println("BUY");

		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String insert = "INSERT INTO transactions(salePrice, quantity, buySell, symbol, username) " +
				"VALUES(?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(insert)) {

			String stock = myStock.name;
			int currentAmount = UserData.holdings.get(stock);
			int newAmount = AMOUNT + currentAmount;
			int salePrice = (int)(100 * portal.stockOrders.get(stock).getPrice()); // price in cents
			if(newAmount > UserData.holdings.get(stock)){
				statement.setInt(1, salePrice);
				statement.setInt(2, newAmount - currentAmount);
				statement.setString(3, "buy");
				statement.setString(4, stock);
				statement.setString(5,  UserData.USER);
				statement.addBatch();
				//JOptionPane.showMessageDialog(null,"Bought " + stock + " " + (newAmount) + " at "  + salePrice/10000.0);
				log += "Bought " + stock + " " + (newAmount) + " at "  + salePrice/10000.0;
			}
			System.out.println(statement.toString());

			int[] rowsChanged = statement.executeBatch();
			for(int i : rowsChanged) if(i == 0) System.out.println("Error Occurred in making buys.");

			//

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}   	



	public void sellStocks(StockandPred myStock){
		/*
        Container container = portal.holdingsPanel;
        Component[] components = container.getComponents();
        for(int i=0;i<components.length;i++){
            if(components[i] instanceof HoldingPanel){
                HoldingPanel hp = (HoldingPanel) components[i];
                if(hp.name.contentEquals(stock.name)){
                    int val = (int) hp.orderSpinner.getValue();
                    hp.orderSpinner.setValue(0);

                }
            }
        }
		 */
		System.out.println("SELL");
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}


		String insert = "INSERT INTO transactions(salePrice, quantity, buySell, symbol, username) " +
				"VALUES(?, ?, ?, ?, ?)";
		String stock = myStock.name;
		try (PreparedStatement statement = connection.prepareStatement(insert)) {
			int newAmount = 0;
			int currentAmount = UserData.holdings.get(stock);
			int salePrice = (int)(100 * portal.stockOrders.get(stock).getPrice()); // price in cents
			
				statement.setInt(1, salePrice);
				statement.setInt(2, currentAmount);
				statement.setString(3, "sell");
				statement.setString(4, stock);
				statement.setString(5,  UserData.USER);
				statement.addBatch();
				//JOptionPane.showMessageDialog(null,"Sold " + stock + " at "  + salePrice/10000.0);
				log += "\nSold " + stock + " at "  + salePrice/10000.0 +"\n";
			
			System.out.println(statement.toString());


			statement.executeBatch();
		} catch (SQLException e) {
			String s = e.getStackTrace().toString();
			e.printStackTrace();
		}
	}


	public String getNewSecretMessage(){
		messagePanel.update();
		String message = messagePanel.secretMessage.body;
		return message;
	}

	public void logPredictionsAIN(){
		log += "\n FUTURE PRICES: ";
		for(StockandPred stock :myStocks){
			if(stock.name.contentEquals("AQU")){
				for(int i=0;i<stock.futurePrices.length;i++){
					log += stock.futurePrices[i] + ", ";
				}
				log +="\nPREDICTIONS: ";
				for(int i=0;i<stock.predictions.length;i++){
					log += stock.predictions[i] + ", ";
				}
				log+="\n";
			}
		}
		System.out.println(log);

	}

	//
	//
	//
	//
	//
	//
	static class StockandPred{
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

		String name;
		double predictions[];
		double maxPrice;
		double nextPrice;
		double thirdPrice;
		double[] futurePrices;
		double currentPrice = 0.0;


		public StockandPred(String name){
			this.name = name;
			this.predictions = new double[20];
			this.futurePrices = new double[20];
			this.currentPrice = getCurrentPrice();

		}

		public void updatePrices(){
			getCurrentPrice();
			//updateFuturePrices();
			updateNextPrice();
			updateMaxPrice();
		}

		private void updateNextPrice(){
			nextPrice = futurePrices[0];
			thirdPrice = futurePrices[1];
		}

		private void updateMaxPrice(){
			for(double i : futurePrices){
				if(i>maxPrice){
					maxPrice = i;
				}
			}
		}
		/////HERE? XXX 
		private void updateFuturePrices(){
			for(int i=0;i<19;i++){
				futurePrices[i] = futurePrices[i+1];
			}
		}
		//Gets current price of current stock in dollars
		private double getCurrentPrice(){
			String priceStr= Double.toString(currentPrice);

			try {
				connection = DriverManager.getConnection(jdbcUrl, username, password);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			try (Statement statement = connection.createStatement()) {

				String queryString = "SELECT * " +
						"FROM stocks WHERE symbol = '" + this.name + "' " +"limit 1;";
				System.out.println(queryString);
				ResultSet resultSet = statement.executeQuery(queryString);
				while (resultSet.next()) {
					priceStr = resultSet.getString(2);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			this.currentPrice = Double.parseDouble(priceStr) / 100.0;

			return currentPrice;
		}

	}

}