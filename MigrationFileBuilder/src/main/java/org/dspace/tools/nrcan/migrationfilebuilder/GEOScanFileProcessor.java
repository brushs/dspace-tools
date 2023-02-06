package org.dspace.tools.nrcan.migrationfilebuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

public class GEOScanFileProcessor implements MetadataFileProcessor {

	//private PrintStream outputStream;
	private FileInputStream inputStream;
	private BufferedReader streamReader;
	private GEOScanDataProcessor unitedToteFilter;
	private String inPath;
	private String outPath;
	private int itemCount = 0;
	private int archiveCount = 0;
	private int archiveSize = 5;
	private String currentArchivePath;
	private String currentItemPath;

	public GEOScanFileProcessor(String inPath, String outPath, CommandLine cmd) {
		this.inPath = inPath;
		this.outPath = outPath;
		
		unitedToteFilter = new GEOScanDataProcessor(cmd);	
	}
	
	public void process() {
		try {
			inputStream = new FileInputStream(inPath);
			//outputStream = getPrintStream(outPath);
			streamReader = new BufferedReader(new InputStreamReader(inputStream));
	
			String line = streamReader.readLine();
	
			while(line != null) {
				processLine(line);
				line = streamReader.readLine();
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		finally {
			close();
		}		
	}
	
	@Override
	public void close() {
		try {
			streamReader.close();
			inputStream.close();
			//outputStream.close();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	private PrintStream getPrintStream(String filename) {
		FileOutputStream fileOutputStream;
		try {fileOutputStream = new FileOutputStream(filename);}
		catch(IOException ex) {throw new RuntimeException(ex.getMessage(), ex);}
		return new PrintStream(fileOutputStream);
	}
	
	public void processLine(String input) {
		if (StringUtils.trim(input).equals("<item>")) {
			if (itemCount == archiveSize || StringUtils.isEmpty(currentArchivePath)) {
				currentArchivePath = "archive_" + String.format("%03d" , archiveCount++);
				itemCount = 0;
			}
			
			currentItemPath = "item_" + String.format("%03d" , itemCount++);
			
			String path = outPath + "\\" + currentArchivePath + "\\" + currentItemPath;
			//System.out.println("PATH: " + path);
			createDirectory(path);
		}

//		if(success) {
//			outputStream.println(input);
//		}
	}

	private void createDirectory(String path) {
		File theDir = new File(path);
		if (!theDir.exists()){
		    theDir.mkdirs();
		}
	}
}
