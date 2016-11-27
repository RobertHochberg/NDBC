/*
 * Mirror class for an entry in our transaction table
 */
package ndbc;

import java.sql.Date;

public class Transaction {
	int tranasactionId;
	Date timestamp;
	int salePrice;
	int quantity;
	String buySell;
	String symbol;
	String username;
	
	public Transaction(int tranasactionId, Date timestamp, int salePrice, int quantity, String buySell, String symbol,
			String username) {
		this.tranasactionId = tranasactionId;
		this.timestamp = timestamp;
		this.salePrice = salePrice;
		this.quantity = quantity;
		this.buySell = buySell;
		this.symbol = symbol;
		this.username = username;
	}
	
	
}
