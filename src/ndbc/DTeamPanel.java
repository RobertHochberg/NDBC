package ndbc;

import java.awt.Color;
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
import javax.swing.JTextField;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

public class DTeamPanel extends JPanel {
	private boolean boolOfPower = false;
	private Portal portal;
	private Basics basics;
	private JTextField keyField;
	private JLabel indicatorOfPower;
	private HashMap<String, DStack> prices;
	
	
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

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel keyPanel = new JPanel();
		//		encryptPanel.setLayout(new BoxLayout(encryptPanel, BoxLayout.Y_AXIS));

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
				gPower.setText(new BigInteger(gField.getText())
						.modPow(new BigInteger(privateLabel.getText()), new BigInteger(pField.getText())).toString());

			}
		});
		keyPanel.add(raiseGPower);
		keyPanel.add(gPower);



		JPanel encryptPanel = new JPanel();

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
		this.add(keyPanel);
		this.add(encryptPanel);


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
						basics.start();
						indicatorOfPower.setText("Bot of Power!");
					}
					
					boolOfPower = !boolOfPower;

				}
			});
			botOfPower.add(buttonOfPower);
			botOfPower.add(indicatorOfPower);
			botOfPower.setBackground(this.getBackground());
			this.add(botOfPower);
		} catch (SQLException e) {
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
		Message secret = receiveSecretMessage(connection);
		postSecretMessage(secret, connection);
		
		try(Statement statement = connection.createStatement()){
			String queryString = "select t1.message from d1 t1 where t1.id = (select t2.id from d1 t2 where t2.user = t1.user order by t2.id desc limit 1);";
			ResultSet resultSet = statement.executeQuery(queryString);
			
			for(DStack st : prices.values())
				st.pop();
			
			String[] stocks;
			HoldingPanel stock;
			Float[] future;
			String stockMessage = "";
			while(resultSet.next()){
				stocks = decrypt(resultSet.getString(1), keyField.getText()).split(" ");
				for(String s : stocks){
					String stockName = s.split(":")[0];
					stock = portal.stockOrders.get(stockName);
					final double price = stock.getPrice();
					future = (Float[])Arrays.stream(s.split(":")[1].split("-")).map((x) -> Float.parseFloat(x)*price).toArray(size -> new Float[size]);
					
					if(!prices.containsKey(stockName))
						prices.put(stockName, new DStack());
					
					prices.get(stockName).addMessage(future);
					
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


	private Message receiveSecretMessage(Connection connection){
		Message secretMessage = null;

		// Retrieve my most recent secret message
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT messageId, recipient, " + UserData.USER + " FROM secretMessages " +
					"WHERE recipient='" + UserData.USER + "' AND " +
					"messageId = (SELECT MAX(messageId) from secretMessages where recipient = '" + UserData.USER + "');";
			ResultSet resultSet = statement.executeQuery(queryString);
			if(resultSet.next())
				secretMessage = new Message(Integer.parseInt(resultSet.getString(1)), null, resultSet.getString(2), resultSet.getString(3));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return secretMessage;
	}

	private void postSecretMessage(Message secret, Connection connection) {
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
}
