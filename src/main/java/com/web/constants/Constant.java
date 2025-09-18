package com.web.constants;

import java.util.ArrayList;

import com.web.pojo.FutureList;

public class Constant {

	public static String DB_URL = "guptainvestmentservices@gmail.com";
	public static String USER = "Papa@1955";
	public static String PASS = "1658465";
	public static String btst_start = " 15:15";
	public static String btst_end = " 15:25";
	public static String publish_call = " 14:30";

	// Database credentials
	public static final String DBUSER = "root";
	public static final String DBPASS = "root";
	public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	public static final String DBURL = "jdbc:mysql://localhost:3306/tradedata";
	public static final String DBURLSCRENNER= "jdbc:mysql://localhost:3306/rms";
	public static final String DBURL_DATA= "jdbc:mysql://localhost:3306/trade_data";
	public static final String path = "F:\\D-Ankur\\workspace2\\IntraDay\\config.properties";
	public static final String batchNightFile =  "H://Project//logs//Performance\\Batch_night_script.xls";
	public static ArrayList<FutureList> futureDetails= new ArrayList<FutureList>();
	public static  ArrayList<String> clientData = new ArrayList<String> ();
	public static String url = "https://guptaeduhub.com/API/CheckLicence.php?userid=";
	public static String stopOrder = " 15:00";
	public static String stopstart = " 15:35";
	
	public static double maxLoss = 0;
	public static boolean mcxoption = true;
	
	public static String livePrice = "com.bt.app.scheduler.threads.LiveAlertFromSingleOrder";
	public static String trackSL = "com.bt.app.scheduler.threads.TrackSLFromSingleOrder";
	public static String trackTarget = "com.bt.app.scheduler.threads.TrackTargetFromSingleOrder'";
	public static String putSL = "com.bt.app.scheduler.threads.PutSLFromSIngleOrder";
	public static String exitOrder ="com.bt.app.scheduler.threads.ExitAllFromSingleOrder";
	public static String trackRobo = "com.bt.app.scheduler.threads.TrackRoboFromSingleOrder";
}
