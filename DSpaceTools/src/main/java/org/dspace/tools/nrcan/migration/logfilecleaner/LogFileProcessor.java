package org.dspace.tools.nrcan.migration.logfilecleaner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.dspace.tools.nrcan.FileProcessor;

public class LogFileProcessor implements FileProcessor {

	private PrintStream outputStream;
	private FileInputStream inputStream;
	private BufferedReader streamReader;
	private String inPath;
	private String outPath;
	private Pattern pattern;
	private Matcher matcher;

	public LogFileProcessor(String inPath, String outPath, CommandLine cmd) {
		this.inPath = inPath;
		this.outPath = outPath;		
	}
	/*
	public void process() {
		try {
			inputStream = new FileInputStream(inPath);
			outputStream = getPrintStream(outPath);
			streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	
			pattern = Pattern.compile("Item:([a-f0-9-]+).*Path:.*?/([^/]+)$");
			
			String line = streamReader.readLine();
	
			while(line != null) {
				processLine(line);
				line = streamReader.readLine();
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		
		close();
	}
	
	public void processLine(String input) {
		boolean success = true;
		
		//success &=  input.contains("MIG_ERR");
		//success &=  input.contains(".author");
		//success &=  !(input.contains("geoscan") || input.contains("null"));
		//success &=  !(input.contains("null"));
		
		matcher = pattern.matcher(input);
		
        if (matcher.find()) {
            String itemGuid = matcher.group(1);
            String fileNameSuffix = matcher.group(2);
            outputStream.println(itemGuid + "," + fileNameSuffix);
            //System.out.println("Item GUID: " + itemGuid);
            //System.out.println("File Name Suffix: " + fileNameSuffix);
        } else {
        	
        }
		
		if(success) {
			//outputStream.println(input.substring(input.indexOf("@")+2));
		}
	}
	*/
	
	public void process() {
		try {
			inputStream = new FileInputStream(inPath);
			outputStream = getPrintStream(outPath);
			streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	
			pattern = Pattern.compile("Item:([a-f0-9-]+).*Path:.*?/([^/]+)$");
			
			String line = streamReader.readLine();
			String response = null;
			String englishTerm = null;
			String frenchTerm = null;
			
			while(line != null) {
				response = processLine(line);
				if (response != null) {
					if (englishTerm == null) {
						englishTerm = response;
					} else {
						frenchTerm = response;
						//outputStream.println(englishTerm + "," + frenchTerm);
					}
				} else {
					if (englishTerm != null && frenchTerm == null) {
						outputStream.println(englishTerm);
					}
					englishTerm = null;
					frenchTerm = null;
				}
				
				line = streamReader.readLine();
			}
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		
		close();
	}
	
	public String processLine(String input) {
		
		if (input.startsWith("English")) {
			input = input.replace("English", "").trim();
			return input;
		} else if (input.startsWith("Français")) {
			input = input.replace("Français", "").trim();
			return input;
		}
		
		return null;
	}
	
	public void close() {
		try {
			streamReader.close();
			inputStream.close();
			outputStream.close();
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
}
