package com.tri.sfc.robotLogParsing;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class ConnectionUtil {
	
	public static ConnectionUtil Instance = null;
	private static  Logger log = Logger.getLogger(ConnectionUtil.class);
	
	private ConnectionUtil() {
	}

	public static ConnectionUtil getConnectionUtil() {
		if (Instance == null)
			Instance = new ConnectionUtil();
		return Instance;
	}


	public Connection getOracleDBConnection(String strDBurl, String strDBusername, String strDBpassword) {
		PropertyConfigurator.configure("src/log4j.properties");
		
		Connection cnn = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			cnn = DriverManager.getConnection(strDBurl, strDBusername, strDBpassword);
		} catch (Exception e) {
			log.error("KNY-009: Connect DB Failed. " + strDBurl + " " + e.getMessage());
		}
		return cnn;
	}

}
