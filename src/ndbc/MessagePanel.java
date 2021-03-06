package ndbc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MessagePanel extends JPanel implements ActionListener{
	JTextArea incomingMessageArea;
	JTextArea myMessageArea;
	JTextArea secretMessageArea;
	HashMap<Integer, Message> messages; // Maps ids to messages
	Message secretMessage; 				// abuse of this class...s
	JButton sendMessageButton;
	JButton getMessagesButton;
	
	int mostRecentMessageId;

	public MessagePanel(){
		mostRecentMessageId = 0;
		// Incoming area
		this.setLayout(new BorderLayout());
		incomingMessageArea = new JTextArea(20, 40);
		JScrollPane scrollPane = new JScrollPane(incomingMessageArea); 
		incomingMessageArea.setEditable(false);
		incomingMessageArea.setBackground(Color.lightGray);
		incomingMessageArea.setFont(new Font("Serif", Font.PLAIN, 12));
		this.add(scrollPane, BorderLayout.CENTER);
		
		// South Panel to hold buttons and send message area
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(3, 1));
		
		// Buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		sendMessageButton = new JButton("Send Message");
		sendMessageButton.addActionListener(this);
		buttonPanel.add(sendMessageButton);
		getMessagesButton = new JButton("Get Messages");
		getMessagesButton.addActionListener(this);
		buttonPanel.add(getMessagesButton);
		southPanel.add(buttonPanel);
		
		// Send message area
		myMessageArea = new JTextArea(3, 50);
		myMessageArea.setEditable(true);
		myMessageArea.setBackground(Color.CYAN);
		myMessageArea.setFont(new Font("Sans", Font.BOLD, 12));
		southPanel.add(myMessageArea);
		
		// Secret message area
		secretMessageArea = new JTextArea(3, 50);
		secretMessageArea.setEditable(true);
		secretMessageArea.setBackground(Color.LIGHT_GRAY);
		secretMessageArea.setFont(new Font("Sans", Font.ITALIC, 11));
		southPanel.add(secretMessageArea);
		
		this.add(southPanel, BorderLayout.SOUTH);
		
		messages = new HashMap<>();
	}

	
	/*
	 * Grab messages from the database and display them
	 */
	public void update(){
		this.getMessagesFromDatabase();
		this.updateIncomingMessageArea();
		this.updateSecretMessageArea();
	}


	/*
	 * Grab new messages from the database
	 */
	private void getMessagesFromDatabase(){
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
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Update public messages
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT messageId, timestamp, body, sender " +
					"FROM messages WHERE messageId > " + mostRecentMessageId + ";";
			ResultSet resultSet = statement.executeQuery(queryString);
			while (resultSet.next()) {
				Message m = new Message(resultSet.getInt(1), resultSet.getTimestamp(2),
						resultSet.getString(3), resultSet.getString(4));
				messages.put(resultSet.getInt(1), m);
				if(resultSet.getInt(1) > mostRecentMessageId)
					mostRecentMessageId = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Retrieve my most recent secret message
		try (Statement statement = connection.createStatement()) {
			String queryString = "SELECT recipient, " + UserData.USER + " FROM secretMessages " +
					"WHERE recipient='" + UserData.USER + "' AND " +
					"messageId = (SELECT MAX(messageId) from secretMessages where recipient = '" + UserData.USER + "');";
			ResultSet resultSet = statement.executeQuery(queryString);
			if(resultSet.next())
				secretMessage = new Message(0, null, resultSet.getString(2), resultSet.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Write messages from our messages HashMap to the incoming message area
	 */
	private void updateIncomingMessageArea(){
		if(messages.size() < 1)
			return;
		String msgs = "";
		Integer[] ids = messages.keySet().toArray(new Integer[1]);
		Arrays.sort(ids);
		for(Integer i : ids){
			msgs = msgs + messages.get(i).sender + " ";
			msgs = msgs + messages.get(i).timestamp + "\n";
			msgs = msgs + messages.get(i).body + "\n\n";
		}
		this.incomingMessageArea.setText(msgs);
	}
	
	/*
	 * Write messages from our secret messages HashMap to the incoming message area
	 */
	private void updateSecretMessageArea(){
		if(secretMessage != null)
			this.secretMessageArea.setText("To " + secretMessage.sender + ": " + secretMessage.body);
	}
	
	/*
	 * Send this user's currently-typed message to the database
	 */
	private void sendMessage(){
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
		try {
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try (Statement statement = connection.createStatement()) {
			String queryString = "INSERT INTO messages(body, sender) VALUES('";
			queryString += this.myMessageArea.getText();
			queryString += "', '";
			queryString += username;
			queryString += "');";
			statement.execute(queryString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		this.myMessageArea.setText("");
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.getMessagesButton){
			this.update();
		}
		
		if(e.getSource() == this.sendMessageButton){
			this.sendMessage();
		}
		
	}
}
