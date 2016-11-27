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
	
	JSpinner orderSpinner;  // The amount the user wants to own
	JLabel numSharesLabel;	// The # of shares the user currently owns
	JLabel priceLabel;		// Current price of the stock
	
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
		orderSpinner.setEditor(new JSpinner.DefaultEditor(orderSpinner));
		
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
		this.orderSpinner.setValue(n);
		this.repaint();
	}
	
	/*
	 * Sets the price to n cents
	 */
	public void setPrice(int n){
		this.price = n;
		this.priceLabel.setText(String.valueOf(price/100.0));
		this.repaint();
	}
	
	/*
	 * Returns the number of shares indicated in the spinner
	 */
	public int getDesiredNumShares(){
		return (Integer)orderSpinner.getValue();
	}
	
	/*
	 * Returns the number of shares indicated in the spinner
	 */
	public void setDesiredNumShares(Integer n){
		orderSpinner.setValue(n);
	}

	/*
	 * Getter for the price
	 */
	public double getPrice() {
		return price;
	}
	
	@Override
	public void enable(boolean b){
		orderSpinner.setEnabled(b);
	}


}
