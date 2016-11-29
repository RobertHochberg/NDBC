package ndbc;

import java.util.HashMap;

public class Constants {

	static String[] stocks = {"ONC", "EUP", "ONA", "MID", "NIG",
			"HTD", "REA", "RYA", "SIP", "OND", 
			"ERE", "DWE", "AKA", "NDW", "EAR",
			"YOV", "ERM", "ANY", "AQU", "AIN",
			"TAN", "DCU", "RIO", "OUS", "VOL",
			"UME", "OFF", "ORG", "OTT", "ENL"};
	
	static String[] companies = {"OncoGenius", "European Polo", "", "", "",
			"", "", "", "", "",
			"", "", "", "", "",
			"", "", "", "", "",
			"", "", "", "", "",
			"", "", "", "", ""};
	
	static String[] users = {"jbaumann", "bmccutchon", "jyamauchi", "mbolot", 
		"jwilson", "rgrosso", "bholdridge", "kbeine", "asokol", "aborse", "root"};
	
	static HashMap<String, String[]> manages;
	
	static int GAME_PERIOD = 20;
}
