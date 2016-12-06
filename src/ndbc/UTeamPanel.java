package ndbc;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;
import java.sql.Connection;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import java.awt.Font;

public class UTeamPanel extends JPanel {

	String instanceConnectionName = "mineral-brand-148217:us-central1:first";
	String databaseName = "ndbc";
	String jdbcUrl = String.format(
			"jdbc:mysql://google/%s?cloudSqlInstance=%s&"
					+ "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
					databaseName,
					instanceConnectionName);
	private final BigInteger p = BigInteger.valueOf(2).pow(128)
			.subtract(BigInteger.valueOf(73));
	private final BigInteger g = new BigInteger(
			"298269132914567127986791922278827852061");
	private final BigInteger a = new BigInteger(128, new Random());

	public UTeamPanel(Portal portal) {
		super();
		this.setBackground(Color.WHITE);

		JButton startDhe = new JButton();
		startDhe.setText("Initiate DHE");
		startDhe.addActionListener(e -> {
			String query = "INSERT INTO u2 (username, gToTheAModP) values (?, ?);";
			try (Connection connection = DriverManager.getConnection(
							jdbcUrl, UserData.USER, UserData.PW);
					PreparedStatement statement = connection.prepareStatement(query)) {
				statement.setString(1, UserData.USER);
				statement.setBigDecimal(2, new BigDecimal(g.modPow(a, p)));
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});
		this.add(startDhe);

	      this.setBackground(Color.WHITE);
	      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	      AutoTrader autoTrader = new AutoTrader(portal);
	      
	      JButton logBtn = new JButton("PrintLog");
	      
	      


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

}