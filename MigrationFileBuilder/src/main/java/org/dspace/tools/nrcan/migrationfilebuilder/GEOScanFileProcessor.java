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

	private FileInputStream inputStream;
	private BufferedReader streamReader;
	private String inPath;
	private String outPath;
	private int itemCount = 0;
	private int archiveCount = 0;
	private int archiveSize = 5;
	private String currentArchivePath;
	private String currentItemPath;
	private boolean filesOpen = false;
	private PrintStream contentsFileStream;
	private PrintStream relationshipsFileStream;
	private PrintStream dublinCoreFileStream;
	private PrintStream dspaceFileStream;
	private PrintStream nrcanFileStream;

	public GEOScanFileProcessor(String inPath, String outPath, CommandLine cmd) {
		this.inPath = inPath;
		this.outPath = outPath;
	}
	
	public void process() {
		try {
			inputStream = new FileInputStream(inPath);
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
			
			if (filesOpen) {
				closeOutputFiles();
			}
			
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
			openNewOutputFiles(path);
			filesOpen = true;
		}
		
		if (StringUtils.trim(input).equals("</item>")) {
			closeOutputFiles();
			filesOpen = false;
		}
	}

	private void createDirectory(String path) {
		File theDir = new File(path);
		if (!theDir.exists()){
		    theDir.mkdirs();
		}
	}
	
	private void openNewOutputFiles(String path) {
		contentsFileStream = getPrintStream(path + "\\contents");
		relationshipsFileStream = getPrintStream(path + "\\relationships");
		dublinCoreFileStream = getPrintStream(path + "\\dublin_core.xml");
		dspaceFileStream = getPrintStream(path + "\\metadata_dspace.xml");
		nrcanFileStream = getPrintStream(path + "\\metadata_nrcan.xml");
		
		initializeDSpaceFile();
	}
	
	private void closeOutputFiles() {
		contentsFileStream.close();
		relationshipsFileStream.close();
		dublinCoreFileStream.close();
		dspaceFileStream.close();
		nrcanFileStream.close();
	}
	
	private void initializeDSpaceFile() {
		dspaceFileStream.println("<dublin_core schema=\"dspace\">");
		dspaceFileStream.println("<dcvalue element=\"entity\" qualifier=\"type\">Publication</dcvalue>");
		dspaceFileStream.println("</dublin_core>");
	}
}
