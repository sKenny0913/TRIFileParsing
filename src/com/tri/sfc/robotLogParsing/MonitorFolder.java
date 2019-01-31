package com.tri.sfc.robotLogParsing;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MonitorFolder {
	private static ResourceBundle config = ResourceBundle.getBundle("Config");
	private static Logger log = Logger.getLogger(ParsingFile.class); 
	
	
	private static String strProcessFolder = config.getString("ProcessFolder");
	private static String strCompleteFolder = config.getString("CompleteFolder");
	private static String strFailedFolder = config.getString("FailedFolder");
	private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmsss");
	private static Date date = new Date();
	
	private static String strDBurl = config.getString("dburl");
	private static String strDBusername = config.getString("dbusername");
	private static String strDBpassword = config.getString("dbpassword");
	private static String strSQLforFolderPath = config.getString("SQLforFolderPath");
	
	
	
	public static void main(String[] args) {
		Connection conn = ConnectionUtil.getConnectionUtil().getOracleDBConnection(strDBurl, strDBusername, strDBpassword);
		PropertyConfigurator.configure("src/log4j.properties");
		
		ResultSet rsFolderPath = null;
		PreparedStatement psFolderPath = null;
		try {
			psFolderPath = conn.prepareStatement(strSQLforFolderPath);
			rsFolderPath = psFolderPath.executeQuery(); //get Folder Path from config table
			while(rsFolderPath.next()) {
				loadFile(rsFolderPath.getString("folderpath")); // load file from folder
			}
			rsFolderPath.close();
		} catch (SQLException e) {
			log.error("KNY-001: Cannot get folder path from config table. " + e.getMessage());
		}finally {
				try { if (rsFolderPath != null) rsFolderPath.close(); } catch (Exception e) {log.error(e.getMessage());};
			    try { if (psFolderPath != null) psFolderPath.close(); } catch (Exception e) {log.error(e.getMessage());};
			    try { if (conn != null) conn.close(); } catch (Exception e) {log.error(e.getMessage());};
		}
		
		
	}
	
	public static void loadFile(String strSrcFolder) {
		
		File folder = new File(strSrcFolder);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			  File file = listOfFiles[i];
			  if (file.isFile()) {
				  File fileMove = MoveFile.moveFile(file, strProcessFolder); //move file to process folder
				  
				  String strFolderName = file.getParentFile().getName(); // get folder name
				  String strFileType = FilenameUtils.getExtension(file.getName()); // get file extension
				  
				  File fileRename = RenameFile.renameFile(fileMove, dateFormat.format(date)); //add date time in file name
				  
				  try {
					ParsingFile.parsingFile(strFolderName, strFileType, fileRename); //parsingFile by calling Parsing_RobotTxt class
					log.info("insert data successfully. " + fileRename.getName());
					MoveFile.moveFile(fileRename, strCompleteFolder); //move file to complete folder
				} catch (ClassNotFoundException e) {
					MoveFile.moveFile(fileRename, strFailedFolder); //move file to failed folder
					log.error(e.getMessage());
				} catch (SecurityException e) {
					MoveFile.moveFile(fileRename, strFailedFolder); //move file to failed folder
					log.error(e.getMessage());
				} catch (IllegalAccessException e) {
					MoveFile.moveFile(fileRename, strFailedFolder); //move file to failed folder
					log.error(e.getMessage());
				} catch (IllegalArgumentException e) {
					MoveFile.moveFile(fileRename, strFailedFolder); //move file to failed folder
					log.error(e.getMessage());
				} catch (InvocationTargetException e) {
					MoveFile.moveFile(fileRename, strFailedFolder); //move file to failed folder
					log.error(e.getMessage());
				} catch (InstantiationException e) {
					MoveFile.moveFile(fileRename, strFailedFolder); //move file to failed folder
					log.error(e.getMessage());
				} catch (SQLException e) {
					MoveFile.moveFile(fileRename, strFailedFolder); //move file to failed folder
					log.error(e.getMessage());
				} 
				  
			  } 
			}
	}
}
