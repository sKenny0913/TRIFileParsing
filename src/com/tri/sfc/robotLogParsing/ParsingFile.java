package com.tri.sfc.robotLogParsing;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ParsingFile {
	private static ResourceBundle config = ResourceBundle.getBundle("Config");
	private static String strPackageName = config.getString("PackageName");
	private static Logger log = Logger.getLogger(ParsingFile.class); 
	private static String strDBurl = config.getString("dburl");
	private static String strDBusername = config.getString("dbusername");
	private static String strDBpassword = config.getString("dbpassword");
	private static String strSQLforClassSelect = config.getString("SQLforClassSelect");
	private static String strSQLforClassWhere1 = config.getString("SQLforClassWhere1");
	private static String strSQLforClassWhere2 = config.getString("SQLforClassWhere2");
	private static String strSQLforSeparatorSelect = config.getString("SQLforSeparatorSelect");
	private static String strSQLforSeparatorWhere1 = config.getString("SQLforSeparatorWhere1");
	private static String strSQLforSeparatorWhere2 = config.getString("SQLforSeparatorWhere2");
	
	
	
	public static void parsingFile(String strFolderName, String strFileType, File fileProcess) throws ClassNotFoundException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, SQLException {
		 Connection conn = ConnectionUtil.getConnectionUtil().getOracleDBConnection(strDBurl, strDBusername, strDBpassword);
		PropertyConfigurator.configure("src/log4j.properties");
		
		ResultSet rsClass = null;
		PreparedStatement psClass = null;
		try {
			String strSQLforDLL = strSQLforClassSelect + " " + strSQLforClassWhere1 + "'" + strFolderName + "'"  + " " + strSQLforClassWhere2 + "'" + strFileType + "'";
			psClass = conn.prepareStatement(strSQLforDLL);
			rsClass = psClass.executeQuery();  // get class name from config table where by folder name and filetype 
		} catch (SQLException e) {
			log.error("KNY-002: Cannot get class name from config table. " + e.getMessage());
		}
		
		ResultSet rsSeparator = null;
		PreparedStatement psSeparator = null;
		try {
			String strSQLforSeparator = strSQLforSeparatorSelect + " " + strSQLforSeparatorWhere1 + "'" + strFolderName + "'"  + " " + strSQLforSeparatorWhere2 + "'" + strFileType + "'";
			psSeparator = conn.prepareStatement(strSQLforSeparator);
			rsSeparator = psSeparator.executeQuery(); // get separator from config table where by folder name and filetype
		} catch (SQLException e) {
			log.error("KNY-003: Cannot get separator from config table. " + e.getMessage());
		}
		
		
		String strKeySeparator = null;
		String strValueSeparator = null;
//		System.out.println(fileProcess.getName());
			//parsing file by class
			if(rsClass.next()) {
//				System.out.println(rsDLL.getString("dll"));
				String strClassName = strPackageName + rsClass.getString("dll");//TODO change table column name from dll to class
				if(rsSeparator.next()) {
					strKeySeparator = rsSeparator.getString("keyseparator");
					strValueSeparator = rsSeparator.getString("valueseparator");
					Class<?> c = Class.forName(strClassName);
					Method [] method = c.getDeclaredMethods() ;
					method[0].invoke(c.newInstance(), new Object[]{strFolderName,strFileType,fileProcess, strKeySeparator, strValueSeparator}); //invoke�I�sobj����mehtod�A�ñN�n�ǵ�method���Ѽƫŧi��Object�}�C�ǤJmethod
				}
			}
			
			try { if (rsSeparator != null) rsSeparator.close(); } catch (Exception e) {log.error(e.getMessage());};
			try { if (psSeparator != null) psSeparator.close(); } catch (Exception e) {log.error(e.getMessage());};
			try { if (rsSeparator != null) rsSeparator.close(); } catch (Exception e) {log.error(e.getMessage());};
			try { if (psSeparator != null) psSeparator.close(); } catch (Exception e) {log.error(e.getMessage());};
			try { if (conn != null) conn.close(); } catch (Exception e) {log.error(e.getMessage());};
	}
	
}
