package ndbc;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

public class DTeamPanel extends JPanel {
	private boolean boolOfPower = false;
	private Portal portal;
	private Basics basics;
	private JTextField keyField;
	private JLabel indicatorOfPower;
	private HashMap<String, DStack> prices;
//	private BigInteger raisedMod;
	private JTextField userPassOfPower;
	Message oldSecretMessage = new Message(0,null,null,null);
	
	
	String instanceConnectionName = "mineral-brand-148217:us-central1:first";
	String databaseName = "ndbc";
	String username = UserData.USER;
	String password = UserData.PW;
	String jdbcUrl = String.format(
			"jdbc:mysql://google/%s?cloudSqlInstance=%s&"
					+ "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
					databaseName,
					instanceConnectionName);


	public DTeamPanel(Portal portal) {
		super();
		this.setBackground(new Color(0, 35, 102));

		this.setLayout(new GridLayout(3,1));

		JPanel keyPanel = new JPanel();
				keyPanel.setLayout(new BoxLayout(keyPanel, BoxLayout.Y_AXIS));

		JTextField pField = new JTextField(20);
		JTextField gField = new JTextField(20);
		JButton generateGP = new JButton("Generate p and g");
		generateGP.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Random rnd = new Random();

				BigInteger q;
				BigInteger k = new BigInteger("2");
				BigInteger rv;
				do{
					q = new BigInteger(64, rnd);
					q = q.nextProbablePrime();


					System.out.println("Finding prime");
					rv = k.multiply(q).add(BigInteger.ONE);

				}while(!rv.isProbablePrime(7));

				pField.setText(rv.toString());

				BigInteger g = BigInteger.ONE;
				do{
					System.out.println("Finding Generator");
					g = g.add(BigInteger.ONE);
				}while((g.intValue() == 1) || (g.pow(k.intValue()).intValue() == 1) || (g.modPow(q, rv).intValue() == 1) );

				gField.setText(g.toString());
			}
		});
		keyPanel.add(generateGP);
		keyPanel.add(pField);
		keyPanel.add(gField);

		JLabel privateLabel = new JLabel();
		JButton generatePrivate = new JButton("Generate Secret Key");
		generatePrivate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(null, "Are you sure you want to replace your secret key?", 
						"Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					BigInteger p = new BigInteger(pField.getText());
					BigInteger rv;
					do{
						Random rnd = new Random();
						rv = new BigInteger(p.bitLength(), rnd);
					}while(p.compareTo(rv) < 0);

					privateLabel.setText(rv.toString());
				}

			}
		});
		keyPanel.add(generatePrivate);		
		keyPanel.add(privateLabel);

		JTextField gPower = new JTextField();
		gPower.setEditable(false);
		JButton raiseGPower = new JButton("Raise Mod");
		
		raiseGPower.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
//				if (JOptionPane.showConfirmDialog(null, "Are you sure you want to change your encryption key?", 
//						"Setting encryption key", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
//					raisedMod = new BigInteger(gField.getText())
//						.modPow(new BigInteger(privateLabel.getText()), new BigInteger(pField.getText()));
//				}
				gPower.setText(new BigInteger(gField.getText())
						.modPow(new BigInteger(privateLabel.getText()), new BigInteger(pField.getText())).toString());

			}
		});
		keyPanel.add(raiseGPower);
		keyPanel.add(gPower);



		JPanel encryptPanel = new JPanel();
		encryptPanel.setLayout(new BoxLayout(encryptPanel, BoxLayout.Y_AXIS));

		keyField = new JTextField(20);
		encryptPanel.add(keyField);

		JTextField messageField = new JTextField(20);
		encryptPanel.add(messageField);

		JTextField messageLabel = new JTextField();
		messageLabel.setEditable(false);
		JButton encrypt = new JButton("Encrypt Message");
		JButton decrypt = new JButton("Decrypt Message");
		encrypt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				messageLabel.setText(encrypt(messageField.getText(), keyField.getText()));
			}
		});
		decrypt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				messageLabel.setText(decrypt(messageField.getText(), keyField.getText()));
			}
		});
		encryptPanel.add(encrypt);
		encryptPanel.add(decrypt);
		encryptPanel.add(messageLabel);


		keyPanel.setBackground(this.getBackground());
		encryptPanel.setBackground(this.getBackground());
		
		JPanel scroll = new JPanel();
		scroll.setLayout(new GridLayout(2, 1));
		JScrollPane scrollPane = new JScrollPane(scroll);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.add(keyPanel);
		scroll.add(encryptPanel);
		this.add(scrollPane);


		Connection connection = null;
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		try (Statement statement = connection.createStatement()) {
			String queryString = "INSERT INTO d1(user, message) VALUES('";
			queryString += username;
			queryString += "', '');";
			statement.execute(queryString);
			this.portal = portal;
			basics = new Basics(this);
			DTeamPanel panel = this;
			JPanel botOfPower = new JPanel();
			JButton buttonOfPower = new JButton("Test");
			indicatorOfPower = new JLabel();
			buttonOfPower.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if(boolOfPower){
						basics.kill();
						indicatorOfPower.setText("");
						basics = new Basics(panel);
					}else{
						prices = new HashMap<>();
						killD1();
						basics.start();
						indicatorOfPower.setText("Bot of Power!");
					}
					
					boolOfPower = !boolOfPower;

				}
			});
			botOfPower.add(buttonOfPower);
			botOfPower.add(indicatorOfPower);
			
			JButton initiateDH = new JButton("Initiate");
			initiateDH.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					Connection connection = null;
					try {
						connection = DriverManager.getConnection(jdbcUrl, username, password);

					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					
					generateGP.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, null));
					try  (Statement statement = connection.createStatement())  {
						statement.execute("delete from d2");
						statement.execute("insert into d2(idx,sender,message) values(8,'" + username + "','" + pField.getText() + "');");
						statement.execute("insert into d2(idx,sender,message) values(9,'" + username + "','" + gField.getText() + "');");
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			});
			botOfPower.add(initiateDH);
			
			JButton joinDH = new JButton("Join");
			joinDH.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					
					String friend = "";
					String[] friends = new String[]{"jyamauchi", "jwilson", "kbeine", "jbaumann", "aborse"};
					for(int i=0; i<friends.length; i++)
						if(friends[i].equals(username))
							friend = friends[(i+1) % friends.length];
					
					boolean[] keyed = new boolean[friends.length];
					
					Connection connection = null;
					try {
						connection = DriverManager.getConnection(jdbcUrl, username, password);

					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					
					try(Statement statement = connection.createStatement()){
						ResultSet rs = statement.executeQuery("select message from d2 where (idx = 8) or (idx = 9) order by idx");
						rs.next();
						pField.setText(rs.getString(1));
						rs.next();
						gField.setText(rs.getString(1));
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					
					generatePrivate.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, null));
					raiseGPower.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, null));
					
					try  (Statement statement = connection.createStatement())  {
						statement.execute("insert into d2(idx,sender,message) values(0,'" + username + "','" + gPower.getText() + "');");
						keyed[0] = true;
						
						
						Statement inserts = connection.createStatement();
						outer:
						while(true){
							ResultSet rs = statement.executeQuery("select idx, message from d2 where sender='" + friend + "';");
							while(rs.next()){
								if(rs.getInt(1) > 7)
									continue;
								if(rs.getInt(1) == (friends.length - 2)){
									gField.setText(rs.getString(2));
									raiseGPower.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, null));
									keyField.setText(gPower.getText());
									keyed[friends.length-1] = true;
								} else if(!keyed[rs.getInt(1)+1]){
									gField.setText(rs.getString(2));
									raiseGPower.getActionListeners()[0].actionPerformed(new ActionEvent(this, 0, null));
									keyed[rs.getInt(1)+1] = true;
									inserts.execute("insert into d2(idx,sender,message) values(" + (rs.getInt(1) + 1) + ",'" + username + "','" + gPower.getText() + "');");
								}
							}
							
							rs.close();
							
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
							
							for(boolean k : keyed)
								if(!k)
									continue outer;
							break;
						}
						
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			});
			botOfPower.add(joinDH);
			
			
			JButton sellout = new JButton("Sellout");
			sellout.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					for(HoldingPanel h : portal.stockOrders.values())
						h.setDesiredNumShares(0);
				}
			});
			botOfPower.add(sellout);
			
			
			userPassOfPower = new JTextField(20);
			botOfPower.add(userPassOfPower);
			
			
			botOfPower.setBackground(this.getBackground());
			this.add(botOfPower);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try (Statement statement = connection.createStatement()){
			
			HashMap<Integer, Message> messages = new HashMap<>();
			JPanel prmsgs = new JPanel();
			prmsgs.setLayout(new GridLayout(3,1));
			JTextField sendmsg = new JTextField(20);
			JTextArea dispmsgs = new JTextArea(10, 1);
			JScrollPane scrollmsgs = new JScrollPane(dispmsgs);
			dispmsgs.setEditable(false);
			dispmsgs.setBackground(Color.WHITE);
			dispmsgs.setFont(new Font("Serif", Font.PLAIN, 12));
			
			prmsgs.add(sendmsg);
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(1, 2));
			
			JButton sendMessageButton = new JButton("Send");
			sendMessageButton.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					Connection connection = null;
					try {
						connection = DriverManager.getConnection(jdbcUrl, username, password);

					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					String newmsg = encrypt(sendmsg.getText(), keyField.getText());
					try  (Statement statement = connection.createStatement())  {
						statement.execute("insert into d3(user,message) values('" + username + "', '" + newmsg +"');");   
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					sendmsg.setText("");
				}
			});
			buttonPanel.add(sendMessageButton);
			
			JButton getMessagesButton = new JButton("Receive");
			getMessagesButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Connection connection = null;
					try {
						connection = DriverManager.getConnection(jdbcUrl, username, password);
					} catch(SQLException e1){
						e1.printStackTrace();
					}
					try(Statement statement = connection.createStatement()){
						ResultSet resultSet = statement.executeQuery("select * from d3");
						while (resultSet.next()) {
							Message m = new Message(resultSet.getString(3), resultSet.getString(2));
							messages.put(resultSet.getInt(1), m);
						}
							
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String msgs = "";
					Integer[] ids = messages.keySet().toArray(new Integer[1]);
					Arrays.sort(ids);
					for(Integer i : ids){
						msgs = msgs + messages.get(i).sender + "\n";
						msgs = msgs + decrypt(messages.get(i).body, keyField.getText()) + "\n\n";
					}
					dispmsgs.setText(msgs);
				}
			});
			buttonPanel.add(getMessagesButton);
			prmsgs.add(buttonPanel);
			prmsgs.add(scrollmsgs);
			this.add(prmsgs);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String encrypt(String message, String stringKey){
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			byte[] bytes = new byte[16];
			System.arraycopy(new BigInteger(stringKey).toByteArray(), 0, bytes, 0, Math.min(bytes.length,new BigInteger(stringKey).toByteArray().length));
			SecretKeySpec key = new SecretKeySpec(bytes, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return Base64.encodeBase64String(cipher.doFinal(message.getBytes()));
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (NoSuchPaddingException e1) {
			e1.printStackTrace();
		} catch (InvalidKeyException e1) {
			e1.printStackTrace();
		} catch (IllegalBlockSizeException e1) {
			e1.printStackTrace();
		} catch (BadPaddingException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private String decrypt(String message, String stringKey){
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			byte[] bytes = new byte[16];
			System.arraycopy(new BigInteger(stringKey).toByteArray(), 0, bytes, 0, Math.min(bytes.length,new BigInteger(stringKey).toByteArray().length));
			SecretKeySpec key = new SecretKeySpec(bytes, "AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			System.out.println(message);
			return new String(cipher.doFinal(Base64.decodeBase64(message)));
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (NoSuchPaddingException e1) {
			e1.printStackTrace();
		} catch (InvalidKeyException e1) {
			e1.printStackTrace();
		} catch (IllegalBlockSizeException e1) {
			e1.printStackTrace();
		} catch (BadPaddingException e1) {
			e1.printStackTrace();
		}

		return null;
	}

	void updateBotofPower(){
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Message secret = receiveSecretMessage(connection, username);
		postSecretMessage(secret, connection);
		
		if(!userPassOfPower.getText().equals("")){
			String[] enemy = userPassOfPower.getText().split(" ");
			for(String e : enemy){
				String[] userPass = e.split(":");
				Connection breach = null;
				try {
					breach = DriverManager.getConnection(jdbcUrl, userPass[0], userPass[1]);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				secret = receiveSecretMessage(breach, userPass[0]);
				postSecretMessage(secret, connection);
				try {
					breach.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		
		
		try(Statement statement = connection.createStatement()){
			String queryString = "select t1.message from d1 t1 where t1.id = (select t2.id from d1 t2 where t2.user = t1.user order by t2.id desc limit 1);";
			ResultSet resultSet = statement.executeQuery(queryString);
			
			System.out.println("HI there");
			for(Entry<String, DStack> st : prices.entrySet())
				System.out.println(st.getKey() + ":" + Arrays.toString(st.getValue().toArray()));
			
			
			
			String[] stocks;
			HoldingPanel stock;
			Float[] future;
			String stockMessage = "";
			while(resultSet.next()){
				
				try {
					if(resultSet.getString(1) == null)
						continue;
					
					stocks = decrypt(resultSet.getString(1), keyField.getText()).split(" ");
				} catch (Exception e) {
					System.err.println(resultSet.getString(1));
					continue;
				}
				for(String s : stocks){
					String stockName = s.split(":")[0];
					stock = portal.stockOrders.get(s.split(":")[0]);
					if(stock == null)
						continue;
					final double price = stock.getPrice();
					future = (Float[])Arrays.stream(s.split(":")[1].split("-")).map((x) -> Float.parseFloat(x)).toArray(size -> new Float[size]);
					
					if(!prices.containsKey(stockName))
						prices.put(stockName, new DStack());
					
					prices.get(stockName).addMessage((float)(future[0]*price), (float)(future[1]*price), (float)(future[2]*price));
					
					if(prices.get(s.split(":")[0]).buy()){
						stock.setDesiredNumShares(100);
						stockMessage += s.split(":")[0] + " at 100; ";
					}else{
						stock.setDesiredNumShares(0);
						stockMessage += s.split(":")[0] + " at 0; ";
					}
					
//					if((future[1] > 100 && future[1] > future[0]) || (future[2] > 100 && future[2] > future[0])){
//						stock.setDesiredNumShares(100);
//						stockMessage += s.split(":")[0] + " at 100; ";
//					}else if(future[1] < future[0] && future[2] < future[0]){
//						stock.setDesiredNumShares(0);
//					stockMessage += s.split(":")[0] + " at 0; ";
//					}
				}
				stockMessage += "\n";
			}
			indicatorOfPower.setText(stockMessage);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}


	private Message receiveSecretMessage(Connection connection, String username){
		Message secretMessage = null;

		// Retrieve my most recent secret message
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT messageId, recipient, " + username + " FROM secretMessages " +
					"WHERE recipient='" + username + "' AND " +
					"messageId = (SELECT MAX(messageId) from secretMessages where recipient = '" + username + "');";
			ResultSet resultSet = statement.executeQuery(queryString);
			if(resultSet.next())
				secretMessage = new Message(Integer.parseInt(resultSet.getString(1)), null, resultSet.getString(2), resultSet.getString(3));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
//		if (!secretMessage.equals(oldSecretMessage))
//			for(DStack st : prices.values())
//				st.pop();
		
		oldSecretMessage = secretMessage;

		return secretMessage;
	}

	private void postSecretMessage(Message secret, Connection connection) {
//		System.out.println("Will: " + secret.sender);
		System.out.println("Write: " + encrypt(secret.sender.trim(), keyField.getText()));
		try (Statement statement = connection.createStatement()) {
			String queryString = "INSERT INTO d1(id, user, message) VALUES('";
			queryString += secret.messageId;
			queryString += "', '";
			queryString += secret.body;
			queryString += "', '";
			queryString += encrypt(secret.sender, keyField.getText());
			queryString += "');";
			statement.execute(queryString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	void killD1(){
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try(Statement statement = connection.createStatement()){
			String queryString = "delete from d1 where user = '" + username + "';";
			statement.execute(queryString);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void killD3(){
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try(Statement statement = connection.createStatement()){
			String queryString = "delete from d3 where user = '" + username + "';";
			statement.execute(queryString);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
