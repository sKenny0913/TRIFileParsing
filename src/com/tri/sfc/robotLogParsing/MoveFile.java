package com.tri.sfc.robotLogParsing;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MoveFile {
	
	private static  Logger log = Logger.getLogger(MoveFile.class); //TODO
	
	public static File moveFile(File fileSrcFile, String strDestFilePath) { //move to process folder
		
		PropertyConfigurator.configure("src/log4j.properties");
		File file = null;
//		File file = fileSrcFile;
		try {
		fileSrcFile.renameTo(new File(strDestFilePath + "\\" + fileSrcFile.getName()));
		file = new File(strDestFilePath + "\\" + fileSrcFile.getName());
		log.info("File moved. " + fileSrcFile + " move to " + file);
		}catch(Exception e){
			log.error("KNY-004: File move failed. " + fileSrcFile);
		}
		return file;
	}
	
}

