package ndbc;

import java.awt.Color;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

import javax.swing.*;

import java.awt.Font;

public class UTeamPanel extends JPanel {
	Portal portal;
	
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

	
	private final BigInteger p = BigInteger.valueOf(2).pow(128)
			.subtract(BigInteger.valueOf(73));
	private final BigInteger g = new BigInteger(
			"298269132914567127986791922278827852061");
	private final BigInteger a = new BigInteger(128, new Random());
	private BigInteger key = null;
	private BigInteger dheKey;
	private boolean leader = false;
	Timer listenForDHE;
	List<String> usernames = Arrays.asList("bholdridge", "mbolot", "bmccutchon", "asokol", "rgrosso");

	public UTeamPanel(Portal portal) {

	      super();
	      this.portal = portal;

		this.setBackground(Color.WHITE);

		JButton startDhe = new JButton();
		startDhe.setText("Initiate DHE");
		startDhe.addActionListener(e -> {
			leader = true;
			key = new BigInteger(128, new Random());
			System.out.println(key);
			String query = "INSERT INTO u2 (username, gToTheAModP) values (?, ?);";
			try (Connection connection = DriverManager.getConnection(
							jdbcUrl, UserData.USER, UserData.PW);
					PreparedStatement statement = connection.prepareStatement(query)) {
				for (String user : usernames) {
					statement.setString(1, user);
					statement.setString(2, g.modPow(a, p).toString(16));
					statement.execute();
				}

				listenForDHE = new Timer(1000, e2 -> {
					String selQuery = "SELECT gToTheBModP FROM u2 WHERE username = ?";
					String insQuery = "UPDATE u2 SET `key` = AES_ENCRYPT(?, UNHEX(?)) WHERE username = ?";
					try (Connection con = DriverManager.getConnection(
									jdbcUrl, UserData.USER, UserData.PW);
							PreparedStatement selStmt = con.prepareStatement(selQuery);
							PreparedStatement insStmt = con.prepareStatement(insQuery)) {
						for (String user : usernames) {
							selStmt.setString(1, user);
							ResultSet rs = selStmt.executeQuery();
							rs.next();
							String res = rs.getString(1);
							if (res != null) {
								BigInteger dheKey = new BigInteger(res, 16).modPow(a, p);
								insStmt.setString(1, key.toString(16));
								insStmt.setString(2, dheKey.toString(16));
								insStmt.setString(3, user);
								insStmt.executeUpdate();
							}
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				});
				listenForDHE.setRepeats(true);
				listenForDHE.start();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});
		this.add(startDhe);

		JButton joinDhe = new JButton();
		joinDhe.setText("Join DHE");
		joinDhe.addActionListener(e -> {
			String selQuery = "SELECT gToTheAModP FROM u2 WHERE username = ? LIMIT 1";
			String insQuery = "UPDATE u2 SET gToTheBModP = ? WHERE username = ?";
			try (Connection connection = DriverManager.getConnection(
							jdbcUrl, UserData.USER, UserData.PW);
					PreparedStatement insStmt = connection.prepareStatement(insQuery);
					PreparedStatement selStmt = connection.prepareStatement(selQuery)) {
				insStmt.setString(2, UserData.USER);
				insStmt.setString(1, g.modPow(a, p).toString(16));
				insStmt.executeUpdate();

				selStmt.setString(1, UserData.USER);
				ResultSet rs = selStmt.executeQuery();
				rs.next();
				dheKey = new BigInteger(rs.getString(1), 16).modPow(a, p);

				listenForDHE = new Timer(1000, e2 -> {
					String selQuery1 = "SELECT AES_DECRYPT(`key`, UNHEX(?)) FROM u2 WHERE username = ?";
					try (Connection con = DriverManager.getConnection(
									jdbcUrl, UserData.USER, UserData.PW);
							PreparedStatement selStmt1 = con.prepareStatement(selQuery1)) {
						selStmt1.setString(1, dheKey.toString(16));
						selStmt1.setString(2, UserData.USER);
						ResultSet rs1 = selStmt1.executeQuery();
						rs1.next();
						String res = rs1.getString(1);
						if (res != null) {
							key = new BigInteger(res, 16);
							System.out.println(key);
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				});
				listenForDHE.setRepeats(true);
				listenForDHE.start();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});
		this.add(joinDhe);


	      this.setBackground(Color.WHITE);
	      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	      AutoTrader autoTrader = new AutoTrader(portal);
	      
	      JButton logBtn = new JButton("PrintLog");
	      
	      JButton shareSecrets = new JButton("Share stocks");
	      
	      

	      JLabel lblAutoTrade = new JLabel("AutoTrade");
	      JRadioButton runAutoTrade = new JRadioButton("On");
	      JRadioButton stopAutoTrade = new JRadioButton("Off");

	      //Group RadioButtons
	      ButtonGroup autoGroup = new ButtonGroup();
	      autoGroup.add(stopAutoTrade);
	      autoGroup.add(runAutoTrade);

	      stopAutoTrade.setSelected(true);
	      ActionListener tradeListener = new ActionListener(){

	         @Override
	         public void actionPerformed(ActionEvent e) {
	            // TODO Auto-generated method stub
	            if(runAutoTrade.isSelected() && e.getSource() != logBtn){
	               autoTrader.STOP = false;
	               runAutoTrade.setEnabled(false);
	               autoTrader.start();
	            }
	            if(stopAutoTrade.isSelected()&& e.getSource() != logBtn){
	               autoTrader.STOP = true;
	               runAutoTrade.setEnabled(true);
	            }
	            if(e.getSource() == logBtn){
	            	autoTrader.logPredictionsAIN();
	            	//System.out.println(autoTrader.log);
	            }
	            if(e.getSource() == shareSecrets){
	            	shareSecrets();
	            }


	         }

	      };
	      
		JPanel jamPanel = new JPanel();
        Jammer x = new Jammer(false);
        JButton sj = new JButton("Start Jammer");
        JButton alter = new JButton("Alter");
        JTextArea alterspace;
        JTextArea incomingMessageArea;
        
        
        alterspace = new JTextArea(3,30);
        alterspace.setEditable(true);
        alterspace.setBackground(Color.LIGHT_GRAY);
        alterspace.setFont(new Font("Sans", Font.BOLD, 12));
        jamPanel.add(alterspace);
        sj.addActionListener(new ActionListener() {
        
            @Override
            public void actionPerformed(ActionEvent e) {
                x.setState(true);
                x.start();
            }
        });
    
        jamPanel.add(sj);
        this.add(jamPanel);
        /**
        alter.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                UserData.USER = alterspace.getText();
            }
        });
        jamPanel.add(alter);
    }
    */
    
        logBtn.addActionListener(tradeListener);
		runAutoTrade.addActionListener(tradeListener);
		stopAutoTrade.addActionListener(tradeListener);
		
		this.add(logBtn);
		this.add(lblAutoTrade);
		this.add(stopAutoTrade);
		this.add(runAutoTrade);
		
		
		
		
		
		
		
		
	}

	
	public void shareSecrets(){
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String insert = "INSERT INTO u1 (username, secretmessage) " +
				"VALUES(?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(insert)) {

				statement.setString(1, UserData.USER);
				statement.setString(2, getNewSecretMessage());
				statement.addBatch();
				statement.execute();
				
			}
			catch (Exception e) {
				// TODO: handle exception
			}

		
	}
		public String getNewSecretMessage(){
			portal.messagePanel.update();
			String message = portal.messagePanel.secretMessage.body;
			return message;
		}


}