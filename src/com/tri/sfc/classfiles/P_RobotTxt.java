package com.tri.sfc.classfiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tri.sfc.robotLogParsing.MoveFile;

public class P_RobotTxt {

	private static ResourceBundle config = ResourceBundle.getBundle("Config");
	private static String strDBurl = config.getString("dburl");
	private static String strDBusername = config.getString("dbusername");
	private static String strDBpassword = config.getString("dbpassword");
	private static String strKeyStation = config.getString("KeyStationName");
	// private static String strKeyNameRobot = config.getString("KeyNameRobot");
	private static String strSQLforDataInsert = config.getString("SQLforDataInsert");
	private static String strSQLforUploadtimes1 = config.getString("SQLforUploadtimes1");
	private static String strSQLforUploadtimes2 = config.getString("SQLforUploadtimes2");
	private static Logger log = Logger.getLogger(P_RobotTxt.class);
	private static String strFailedFolder = config.getString("FailedFolder");

	@SuppressWarnings("resource")
	public void parsingContent(String strFolderName, String strFileType, File fileSrcFile, String strKeySeparator,
			String strValueSeparator) throws Exception {
		PropertyConfigurator.configure("src/log4j.properties");
		Scanner scanner = null;
		String strStation = null;
		String strSerialNumber = null;
		Integer intUploadtimes = null;
		Connection cnn = null;
		FileTime ftFileCreateDate = null;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDataTableName = null;
		String strSQLforTable = config.getString("SQLforTable");
		String strKeyName = null;
		PreparedStatement PS = null;
		ResultSet RS = null;
		Boolean blnDebugMode = false;

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			cnn = DriverManager.getConnection(strDBurl, strDBusername, strDBpassword);
			cnn.setAutoCommit(false);
		} catch (Exception e) {
			MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
			cnn.close();
			log.error("KNY-009: Connect DB Failed. " + strDBurl + "; " + e.getMessage());
			throw e;
		}

		try { // get insert table name and key name of log file
			Statement stmt = cnn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String strSQLexecute2 = strSQLforTable + "'" + strFolderName + "'";
			ResultSet rs2;
			rs2 = stmt.executeQuery(strSQLexecute2);
			if (rs2.next()) {
				strDataTableName = rs2.getString(1);
				strKeyName = rs2.getString(2);
			}
			rs2.close();
			stmt.close();
		} catch (SQLException e1) {
			MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
			cnn.close();
			// TODO Auto-generated catch block
			log.error("KNY-010: Cannot get insert table name. ");
			throw e1;
		}
		
		
		try {
			log.info("File parsing start. " + fileSrcFile.getName());
			scanner = new Scanner(fileSrcFile);
		} catch (FileNotFoundException e) {
			MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
			cnn.close();
			log.error("KNY-011: File not found. " + e.getMessage());
			throw e;
		}

		while (scanner.hasNext()) { // get table key value from txt
			String strCurrentLine = scanner.nextLine();
			if (strCurrentLine.contains(strKeyStation)) {
				String[] strPKAryLine = strCurrentLine.split(strKeySeparator);
				strStation = strPKAryLine[1];
			}
			if (strCurrentLine.contains(strKeyName)) {
				String[] strPKAryLine = strCurrentLine.split(strKeySeparator);
				strSerialNumber = strPKAryLine[1];
			}

		}
		scanner.close();

		try {// get upload times
			String strSQLexecute = strSQLforUploadtimes1 + " " + strDataTableName + " " + strSQLforUploadtimes2 + "'"
					+ strSerialNumber + "'" + " and keyname='" + strKeyName + "'";
			PS = cnn.prepareStatement(strSQLexecute);
			ResultSet rs = PS.executeQuery();
			if(blnDebugMode) System.out.println(strSQLexecute);
			while (rs.next()) {
				intUploadtimes = rs.getInt(1) + 1;
			}
			rs.close();
			PS.close();
		} catch (SQLException e) {
			MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
			cnn.close();
			log.error("KNY-012: Cannot get upload times. " + e.getMessage());
			throw e;
		}

		try {
			scanner = new Scanner(fileSrcFile); // scanner reset
			Path path = Paths.get(fileSrcFile.getPath());
			BasicFileAttributes attr;
			try { // get file create date for inserting table date column
				attr = Files.readAttributes(path, BasicFileAttributes.class);
				ftFileCreateDate = attr.creationTime();
//				System.out.println(attr.lastAccessTime());
//				System.out.println(path);
//				System.out.println(attr.creationTime());
//				System.out.println(attr.lastModifiedTime());
//				System.out.println(ftFileCreateDate);
//				System.out.println(df.format(ftFileCreateDate));

			} catch (IOException e) {
				scanner.close();
				MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
				cnn.close();
				log.error("KNY-013: Cannot get create date from file. " + e.getMessage());
				throw e;
			}

		} catch (FileNotFoundException e) {
			scanner.close();
			MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
			cnn.close();
			log.error("KNY-011: File not found. " + e.getMessage());
			throw e;
		}
		
//		System.out.println(scanner.hasNextLine());
		
		while (scanner.hasNext()) {
			String strKey = null;
			String strValue = null;
			String[] strAryLine = scanner.nextLine().split(strKeySeparator);
//			 System.out.println(strAryLine[0]);
			if (strAryLine.length > 0) {
				strKey = strAryLine[0].trim(); // Key
//				System.out.println(strKey);
				if(strAryLine.length > 1) {
					if (strValueSeparator != null) {
						if (strAryLine[1].contains(strValueSeparator)) { // check if value separator exist in config table
																			// and value
							String[] strAryValue = strAryLine[1].split(strValueSeparator);
							for (int i = 0; i < strAryValue.length; i++) { // insert all value separated by value separator
								strValue = strAryValue[i].trim(); // value
								// value index = i + 1
								String strSQLdataInsert = "INSERT INTO " + strDataTableName + " " + strSQLforDataInsert
										+ "'" + strFolderName + "','" + strStation + "','" + strSerialNumber + "','"
										+ strKey + "','" + strValue + "'," + (i + 1) + ", to_date('"
										+ df.format(ftFileCreateDate.toMillis()) + "','yyyy-mm-dd hh24:mi:ss'),"
										+ intUploadtimes + ")";
								if(blnDebugMode)  System.out.println(strSQLdataInsert);
								try {
									PS = cnn.prepareStatement(strSQLdataInsert);// insert data
									RS = PS.executeQuery();
								} catch (SQLException e) {
									scanner.close();
									MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
									cnn.rollback();
									cnn.close();
									log.error("KNY-014: Insert table failed. " + e.getMessage());
									throw e;
								}finally {
									RS.close();
									PS.close();
								}
	
							}
						} else {
	//						
							strValue = strAryLine[1]; // insert value separated by key separator
							// value index = 1
							String strSQLdataInsert = "INSERT INTO " + strDataTableName + " " + strSQLforDataInsert + "'"
									+ strFolderName + "','" + strStation + "','" + strSerialNumber + "','" + strKey + "','"
									+ strValue + "'," + 1 + ", to_date('" + df.format(ftFileCreateDate.toMillis())
									+ "','yyyy-MM-dd hh24:mi:ss')," + intUploadtimes + ")";
							if(blnDebugMode) System.out.println(strSQLdataInsert);
							try {
								PS = cnn.prepareStatement(strSQLdataInsert);// insert data
								RS = PS.executeQuery();
							} catch (SQLException e) {
								scanner.close();
								MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
								cnn.rollback();
								cnn.close();
								log.error("KNY-014: Insert table failed. " + e.getMessage());
								throw e;
							}finally {
								RS.close();
								PS.close();
							}
	//						
						}
					} else {
						strValue = strAryLine[1].trim(); // insert value separated by key separator
						// value index = 1
						String strSQLdataInsert = "INSERT INTO " + strDataTableName + " " + strSQLforDataInsert + "'"
								+ strFolderName + "','" + strStation + "','" + strSerialNumber + "','" + strKey + "','"
								+ strValue + "'," + 1 + ", to_date('" + df.format(ftFileCreateDate.toMillis())
								+ "','yyyy-MM-dd hh24:mi:ss')," + intUploadtimes + ")";
						if(blnDebugMode) System.out.println(strSQLdataInsert);
						try {
							PS = cnn.prepareStatement(strSQLdataInsert);// insert data
							RS = PS.executeQuery();
						} catch (SQLException e) {
							scanner.close();
							MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
							cnn.rollback();
							cnn.close();
							log.error("KNY-014: Insert table failed. " + e.getMessage());
							throw e;
						}finally {
							RS.close();
							PS.close();
						}
					}
				
				}else { // if no value, insert empty in column
					String strSQLdataInsert = "INSERT INTO " + strDataTableName + " " + strSQLforDataInsert + "'"
							+ strFolderName + "','" + strStation + "','" + strSerialNumber + "','" + strKey + "','"
							+ " " + "'," + 1 + ", to_date('" + df.format(ftFileCreateDate.toMillis())
							+ "','yyyy-MM-dd hh24:mi:ss')," + intUploadtimes + ")";
					if(blnDebugMode) System.out.println(strSQLdataInsert);
					try {
						MoveFile.moveFile(fileSrcFile, strFailedFolder); //move file to failed folder
						PS = cnn.prepareStatement(strSQLdataInsert);// insert data
						RS = PS.executeQuery();
						
					} catch (SQLException e) {
						scanner.close();
						cnn.rollback();
						cnn.close();
						log.error("KNY-014: Insert table failed. " + e.getMessage());
						throw e;
					}finally {
						RS.close();
						PS.close();
					}
				}
			}
		}
		cnn.commit();
		try { if (scanner != null) scanner.close(); } catch (Exception e) {log.error(e.getMessage());};
		try { if (RS != null) RS.close(); } catch (Exception e) {log.error(e.getMessage());};
		try { if (PS != null) PS.close(); } catch (Exception e) {log.error(e.getMessage());};
		try { if (cnn != null) cnn.close(); } catch (Exception e) {log.error(e.getMessage());};
	}
}
