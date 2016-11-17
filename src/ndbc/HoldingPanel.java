package ndbc;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class HoldingPanel extends javax.swing.JPanel {
	int numShares;
	double price;
	String name;
	
	JSpinner orderSpinner;
	JLabel numSharesLabel;
	JLabel priceLabel;
	
	public HoldingPanel(String name, int value, double price){
		// Initialize Fields
		this.name = name;
		this.price = price;
		this.priceLabel = new JLabel(String.valueOf(price/100.0));
		this.numShares = value;
		this.numSharesLabel = new JLabel();
		this.numSharesLabel.setText(" " + String.valueOf(numShares));
		
		// Setup the price spinner
		// Users can own up to 100 shares
		SpinnerModel model = new SpinnerNumberModel(value, 0, 100, 1);
		orderSpinner = new JSpinner(model);
		
		// Build the component
		this.setPreferredSize(new Dimension(120, 30));
		this.setBorder(BorderFactory.createRaisedBevelBorder());
		this.setLayout(new GridLayout(2, 2));
		this.add(new JLabel("  " + name));
		this.add(priceLabel);
		this.add(numSharesLabel);
		this.add(orderSpinner);
		this.setVisible(true);
	}
	
	public void setNumShares(int n){
		this.numShares = n;
		this.numSharesLabel.setText(String.valueOf(numShares));
		this.repaint();
	}
	
	/*
	 * Sets the price to n cents
	 */
	public void setPrice(int n){
		this.price = n;
		this.numSharesLabel.setText(String.valueOf(price/100.0));
		this.repaint();
	}
	
	/*
	 * Returns the number of shares indicated in the spinner
	 */
	public int getDesiredNumShares(){
		return (Integer)orderSpinner.getValue();
	}

	/*
	 * Getter for the price
	 */
	public double getPrice() {
		return price;
	}


}
