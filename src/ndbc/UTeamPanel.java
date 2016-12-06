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
import javax.swing.JPanel;

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

	public UTeamPanel() {
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
	}

}
