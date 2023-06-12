package org.dspace.tools.nrcan.migration.logfilecleaner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.dspace.tools.nrcan.FileProcessor;

public class LogFileProcessor implements FileProcessor {

	private PrintStream outputStream;
	private FileInputStream inputStream;
	private BufferedReader streamReader;
	private String inPath;
	private String outPath;

	public LogFileProcessor(String inPath, String outPath, CommandLine cmd) {
		this.inPath = inPath;
		this.outPath = outPath;		
	}
	
	public void process() {
		try {
			inputStream = new FileInputStream(inPath);
			outputStream = getPrintStream(outPath);
			streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	
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
		
		success &=  input.contains("MIG_ERR");

		if(success) {
			outputStream.println(input.substring(input.indexOf("@")+2));
		}
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
