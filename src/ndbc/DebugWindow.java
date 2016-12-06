package ndbc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.List;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ndbc.AutoTrader.StockandPred;

public class DebugWindow extends JFrame {
	JPanel myPanel = new JPanel();
	ArrayList<JLabel> Onelabels = new ArrayList<JLabel>();
	ArrayList<JLabel> Twolabels = new ArrayList<JLabel>();
	ArrayList<JLabel> Threelabels = new ArrayList<JLabel>();
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


	public DebugWindow(AutoTrader at) {
				
		// Set up the JFrame
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(300, 1200));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(1,3));
		
		
		myPanel.setBackground(Color.WHITE);
		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));

		ArrayList<StockandPred> myStocks = at.myStocks;
		for(StockandPred stock: myStocks){
			JPanel p = new JPanel();
			JLabel name = new JLabel(stock.name);
			p.setLayout(new GridLayout(21,2));
			p.add(name);
			p.add(new JLabel());
			if(stock.name=="AQU"){
			JLabel[] predLabels = makeOne(stock.predictions);
			JLabel[] futureLabels = makeOne(stock.futurePrices);
			
			for(int i=0;i<20;i++){
				p.add(predLabels[i]);
				p.add(futureLabels[i]);
			}
			myPanel.add(p);}
			if(stock.name=="AIN"){
				JLabel[] predLabels = makeTwo(stock.predictions);
				JLabel[] futureLabels = makeTwo(stock.futurePrices);
				
				for(int i=0;i<20;i++){
					p.add(predLabels[i]);
					p.add(futureLabels[i]);
				}
				myPanel.add(p);}
			if(stock.name=="TAN"){
				JLabel[] predLabels = makeThree(stock.predictions);
				JLabel[] futureLabels = makeThree(stock.futurePrices);
				
				for(int i=0;i<20;i++){
					p.add(predLabels[i]);
					p.add(futureLabels[i]);
				}
				myPanel.add(p);}
			
			JLabel futurePrices = new JLabel(stock.futurePrices.toString());
			//myPanel.add(p);
		}

		this.add(myPanel);
		this.pack();
		this.setVisible(true);

	}
	
	JLabel[] makeOne(double[] predictions){
		JLabel[] jls = new JLabel[20];
		for(int i=0;i<20;i++){
			JLabel a = new JLabel(i + ": " + predictions[i]);
			jls[i] = a;
			Onelabels.add(a);
		}
		return jls;
	}
	JLabel[] makeTwo(double[] predictions){
		JLabel[] jls = new JLabel[20];
		for(int i=0;i<20;i++){
			JLabel a = new JLabel(i + ": " + predictions[i]);
			jls[i] = a;
			Twolabels.add(a);
		}
		return jls;
	}
	JLabel[] makeThree(double[] predictions){
		JLabel[] jls = new JLabel[20];
		for(int i=0;i<20;i++){
			JLabel a = new JLabel(i + ": " + predictions[i]);
			jls[i] = a;
			Threelabels.add(a);
		}
		return jls;
	}

	public void repaint(StockandPred stock, int offSet){
		String name = stock.name;
		ArrayList<JLabel> list = Onelabels;
		switch(name){
			case "AQU":
				list = Onelabels;
				break;
			case "AIN":
				list = Twolabels;
				break;
			case "TAN":
				list = Threelabels;
				break;
				
		}
		
		for(int i=0;i<offSet+20;i++){	
			JLabel l = list.get(i);
			l.setText("" +stock.predictions[i-offSet]);
			int g = i+20;
			l = list.get(g);
			l.setText(""+stock.futurePrices[i-offSet]);
		
		}
		super.repaint();
	}
	
}