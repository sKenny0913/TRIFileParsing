package com.tri.sfc.robotLogParsing;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class LoadConfigDB {
	private static Logger log = Logger.getLogger(LoadConfigDB.class); //TODO
	private static ResourceBundle config = ResourceBundle.getBundle("Config");
	private static String strDBurl = config.getString("dburl");
	private static String strDBusername = config.getString("dbusername");
	private static String strDBpassword = config.getString("dbpassword");
	private static String strSQLforFolderPath = config.getString("SQLforFolderPath");
	private static String strSQLforClassSelect = config.getString("SQLforClassSelect");
	private static String strSQLforClassWhere1 = config.getString("SQLforClassWhere1");
	private static String strSQLforClassWhere2 = config.getString("SQLforClassWhere2");
	private static String strSQLforSeparatorSelect = config.getString("SQLforSeparatorSelect");
	private static String strSQLforSeparatorWhere1 = config.getString("SQLforSeparatorWhere1");
	private static String strSQLforSeparatorWhere2 = config.getString("SQLforSeparatorWhere2");
	private static Connection conn = ConnectionUtil.getConnectionUtil().getOracleDBConnection(strDBurl, strDBusername, strDBpassword);
	
	public static ResultSet getFolderPath() {
		ResultSet rsFolderPath = null;
		try {
			rsFolderPath = conn.prepareStatement(strSQLforFolderPath).executeQuery();
		} catch (SQLException e) {
			log.error("KNY-001: Cannot get folder path from config table. " + e.getMessage());
		}
		return rsFolderPath;
	}
	public static ResultSet getClass(String strFolderName, String strFileType) {
		ResultSet rsClass = null;
		try {
			String strSQLforDLL = strSQLforClassSelect + " " + strSQLforClassWhere1 + "'" + strFolderName + "'"  + " " + strSQLforClassWhere2 + "'" + strFileType + "'";
			rsClass = conn.prepareStatement(strSQLforDLL).executeQuery();
		} catch (SQLException e) {
			log.error("KNY-002: Cannot get class name from config table. " + e.getMessage());
		}
		return rsClass;
	}
	public static ResultSet getSeparator(String strFolderName, String strFileType) {
		ResultSet rsSeparator = null;
		try {
			String strSQLforSeparator = strSQLforSeparatorSelect + " " + strSQLforSeparatorWhere1 + "'" + strFolderName + "'"  + " " + strSQLforSeparatorWhere2 + "'" + strFileType + "'";
			rsSeparator = conn.prepareStatement(strSQLforSeparator).executeQuery();
		} catch (SQLException e) {
			log.error("KNY-003: Cannot get separator from config table. " + e.getMessage());
		}
		return rsSeparator;
	}
}
