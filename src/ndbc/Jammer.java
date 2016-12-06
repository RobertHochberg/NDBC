package ndbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class Jammer extends Thread {
    boolean active;
    String[] activeUsers = {"aborse","asokol","bholdridge","bmccutchon","jbaumann", "jwilson","jyamauchi", "kbeine", "mbolot", "rgrosso"};
    public Jammer(boolean activationState) {
        active = activationState;
    }
    
    public void setState(boolean state){
        active = state;
    }
    
    public void run(){
        String instanceConnectionName = "mineral-brand-148217:us-central1:first";
        String databaseName = "ndbc";
        String username = UserData.USER;
        String password = UserData.PW;
        String jdbcUrl = String.format(
                "jdbc:mysql://google/%s?cloudSqlInstance=%s&"
                        + "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
                        databaseName,
                        instanceConnectionName);
        boolean go = true;
        int incrementer = 0;
        while(go){
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(jdbcUrl, username, password);
            } catch (SQLException z) {
                z.printStackTrace();
            }
            try (Statement statement = connection.createStatement()) {
                String queryString = "INSERT INTO messages(body, sender) VALUES('";
                queryString += "I think this game will go badly";
                queryString += "', '";
                queryString += activeUsers[incrementer];
                incrementer += 1;
                incrementer = incrementer % activeUsers.length;
                queryString += "');";
                statement.execute(queryString);
            } catch (SQLException z) {
                z.printStackTrace();
            }
        }
    }
    
}