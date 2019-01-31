package com.tri.sfc.robotLogParsing;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class RenameFile {
	
	private static  Logger log = Logger.getLogger(RenameFile.class);
	
	public static File renameFile(File fileSrcFile, String strDate) { // add date time in file name
		
		PropertyConfigurator.configure("src/log4j.properties");
		File file = null;
//		File file = fileSrcFile;
		try {
		String strFilename = fileSrcFile.getName().split("\\.")[0] + "_" + strDate + "." + fileSrcFile.getName().split("\\.")[1];
		fileSrcFile.renameTo(new File(fileSrcFile.getParentFile() + "\\" + strFilename));
		file = new File(fileSrcFile.getParentFile() + "\\" + strFilename);
		log.info("File renamed. " + fileSrcFile.getName() + " rename to " + file.getName());
		}catch(Exception e){
			log.error("KNY-005: File rename failed. " + fileSrcFile);
		}
		
		return file;
		
	}
	
}
