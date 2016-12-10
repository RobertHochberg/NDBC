package ndbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class Play {
	static String last[] = {"DIAZ", "HAYES", "MYERS", "FORD", "HAMILTON", "GRAHAM", "SULLIVAN", "WALLACE", "WOODS", 
			"COLE", "WEST", "JORDAN", "OWENS", "REYNOLDS", "FISHER", "ELLIS", "HARRISON", "GIBSON", "MCDONALD", 
			"CRUZ", "MARSHALL", "ORTIZ", "GOMEZ", "MURRAY", "FREEMAN", "WELLS", "WEBB", "SIMPSON", "STEVENS", 
			"TUCKER", "PORTER", "HUNTER", "HICKS", "CRAWFORD", "HENRY", "BOYD", "MASON", "MORALES", "KENNEDY", 
			"WARREN", "DIXON", "RAMOS", "REYES", "BURNS", "GORDON"};
	static int numLast = last.length;
	
	static String first[] = {"ANTHONY", "LISA", "KEVIN", "NANCY", "KAREN", "BETTY", "HELEN", "JASON", "MATTHEW", 
			"GARY", "TIMOTHY", "SANDRA", "JOSE", "LARRY", "JEFFREY", "FRANK", "DONNA", "CAROL", "RUTH", 
			"SCOTT", "ERIC", "STEPHEN", "ANDREW", "SHARON", "MICHELLE", "LAURA", "SARAH", "KIMBERLY", 
			"DEBORAH", "JESSICA", "RAYMOND", "SHIRLEY", "CYNTHIA", "ANGELA", "MELISSA", "BRENDA", "AMY", 
			"JERRY", "GREGORY", "ANNA", "JOSHUA", "VIRGINIA", "REBECCA", "KATHLEEN", "DENNIS", "PAMELA", 
			"MARTHA", "DEBRA", "AMANDA", "WALTER", "STEPHANIE", "WILLIE", "PATRICK", "TERRY", "CAROLYN", 
			"PETER", "CHRISTINE", "MARIE", "JANET", "FRANCES", "CATHERINE", "HAROLD", "HENRY", "DOUGLAS", 
			"JOYCE", "ANN", "DIANE", "ALICE", "JEAN", "JULIE", "CARL", "KELLY", "HEATHER", "ARTHUR", 
			"TERESA", "GLORIA", "DORIS", "RYAN", "JOE", "ROGER", "EVELYN", "JUAN", "ASHLEY", "JACK", "CHERYL", 
			"ALBERT", "JOAN", "MILDRED", "KATHERINE", "JUSTIN", "JONATHAN", "GERALD", "KEITH", "SAMUEL", 
			"JUDITH", "ROSE", "JANICE", "LAWRENCE", "RALPH", "NICOLE", "JUDY", "NICHOLAS", "CHRISTINA", "ROY", 
			"KATHY", "THERESA", "BENJAMIN", "BEVERLY", "DENISE", "BRUCE", "BRANDON", "ADAM", "TAMMY", "IRENE", 
			"FRED", "BILLY", "HARRY", "JANE", "WAYNE", "LOUIS", "LO"};
	static int numFirst = first.length;
	
	static int[] ids = new int[300];
	static String[] fn = new String[300];
	static String[] ln = new String[300];
	static int[] credits = new int[300];
	static int[] gpa = new int[300];
	
	
	
	public static void main(String[] args){
		populateStocks();
	}
	
	static void populateStocks(){
		Random r = new Random();
		System.out.print("Populating Students... ");
		// TODO: Don't connect if we don't need to. 
		String instanceConnectionName = "mineral-brand-148217:us-central1:first";
		String databaseName = "play";
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

		// Update owns table to reflect these transactions
		String makeStocks  = "INSERT INTO student(first, last, credits, gpa, id) VALUES (?, ?, ?, ?, ?);";
		try (PreparedStatement statement = connection.prepareStatement(makeStocks)) {
			for(int i = 0; i < 300; i++){
				ids[i] = 900000000 + r.nextInt(878473);
				fn[i] = first[r.nextInt(numFirst)];
				ln[i] = last[r.nextInt(numLast)];
				credits[i] = r.nextInt(130);
				gpa[i] = r.nextInt(401);
				statement.setString(1, fn[i]);
				statement.setString(2, ln[i]);
				statement.setInt(3, credits[i]);
				statement.setInt(4, gpa[i]);
				statement.setInt(5, ids[i]);
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Populated.");
	}
	
	static void populateProfs(){
		Random r = new Random();
		System.out.print("Populating Students... ");
		// TODO: Don't connect if we don't need to. 
		String instanceConnectionName = "mineral-brand-148217:us-central1:first";
		String databaseName = "play";
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

		// Update owns table to reflect these transactions
		String makeStocks  = "INSERT INTO student(first, last, credits, gpa, id) VALUES (?, ?, ?, ?, ?);";
		try (PreparedStatement statement = connection.prepareStatement(makeStocks)) {
			for(int i = 0; i < 300; i++){
				ids[i] = 900000000 + r.nextInt(878473);
				statement.setString(1, first[r.nextInt(numFirst)]);
				statement.setString(2, last[r.nextInt(numLast)]);
				statement.setInt(3, r.nextInt(130));
				statement.setInt(4, r.nextInt(401));
				statement.setInt(5, ids[i]);
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Populated.");
	}

}
