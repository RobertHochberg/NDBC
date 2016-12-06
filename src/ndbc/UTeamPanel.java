package ndbc;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import java.awt.Font;

public class UTeamPanel extends JPanel {

	public UTeamPanel(Portal portal) {
	      super();
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