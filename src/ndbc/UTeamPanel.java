package ndbc;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
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

	public UTeamPanel(Portal portal) {
	      super();
	      this.portal = portal;
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