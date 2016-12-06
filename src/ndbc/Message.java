package ndbc;

import java.sql.Timestamp;

/*
 * Holds one message from the database
 */
public class Message {
	int messageId;
	Timestamp timestamp;
	String body;
	String sender;
	public Message(int messageId, Timestamp timestamp, String body, String sender) {
		super();
		this.messageId = messageId;
		this.timestamp = timestamp;
		this.body = body;
		this.sender = sender;
	}
	
	public boolean equals(Message obj) {
		// TODO Auto-generated method stub
		return (messageId == obj.messageId) && (timestamp.equals(obj.timestamp) && (body.equals(obj.body) && sender.equals(obj.sender)));
	}
	
	
}
