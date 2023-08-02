package org.dspace.tools.nrcan.migration.logfilecleaner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.dspace.tools.nrcan.FileProcessor;

public final class LogFileCleaner {
	private final static char OPT_INPUT_FILE = 'f';
	private final static char OPT_OUTPUT_FILE = 'o';
	private final static char OPT_COMBINE_FILES = 'c';
	private final static char OPT_COMBINE_CFS_FILES = 'z';
	
	public static final void main(String[] args) throws IOException {
		CommandLineParser parser = new PosixParser();
		Options options = getCliOptions();
		CommandLine cmd;
		
		try {
			cmd = parser.parse(options, args);
		}
		catch(ParseException ex) {
			System.out.println("ERROR: " + ex.getMessage());
			new HelpFormatter().printUsage(new PrintWriter(System.out, true), 80, LogFileCleaner.class.getName(), options);
			new HelpFormatter().printHelp(LogFileCleaner.class.getName(), "", options, "");
			return;
		}

		if (!StringUtils.isEmpty(cmd.getOptionValue(OPT_COMBINE_FILES))) {
			combine(cmd);
		} else if (!StringUtils.isEmpty(cmd.getOptionValue(OPT_COMBINE_CFS_FILES))) {
			combineCFS(cmd);
		} else {
			processFile(cmd);
		}
		
	}
	
	private static void processFile(CommandLine cmd) {
		String inPath = cmd.getOptionValue(OPT_INPUT_FILE);
		String outPath = cmd.getOptionValue(OPT_OUTPUT_FILE);

		FileProcessor processor = new LogFileProcessor(inPath, outPath, cmd);
		
		try {
			processor.process();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void combine(CommandLine cmd) throws IOException {
        // create instance of directory
        File dir = new File(cmd.getOptionValue(OPT_INPUT_FILE));
 
        // create object of PrintWriter for output file
        PrintWriter pw = new PrintWriter(cmd.getOptionValue(OPT_OUTPUT_FILE));
 
        // Get list of all the files in form of String Array
        String[] fileNames = dir.list();
 
        // loop for reading the contents of all the files
        // in the directory GeeksForGeeks
        for (String fileName : fileNames) {
            System.out.println("Reading from " + fileName);
 
            // create instance of file from Name of
            // the file stored in string Array
            File f = new File(dir, fileName);
 
            // create object of BufferedReader
            BufferedReader br = new BufferedReader(new FileReader(f));
            pw.println("Contents of file " + fileName);
 
            // Read from current file
            String line = br.readLine();
            while (line != null) {
 
                // write to the output file
                pw.println(line);
                line = br.readLine();
            }
            pw.flush();
        }
        System.out.println("Reading from all files" +
        " in directory " + dir.getName() + " Completed");
	}
	
	private static void combineCFS(CommandLine cmd) throws IOException {
        // create instance of directory
        File dir = new File(cmd.getOptionValue(OPT_INPUT_FILE));
 
        // create object of PrintWriter for output file
        PrintWriter pw = new PrintWriter(cmd.getOptionValue(OPT_OUTPUT_FILE));
 
        // Get list of all the files in form of String Array
        String[] fileNames = dir.list();
 
        pw.print("{\"data\":[");
        
        boolean first = true;
        
        // loop for reading the contents of all the files
        // in the directory GeeksForGeeks
        for (String fileName : fileNames) {
            System.out.println("Reading from " + fileName);
 
            // create instance of file from Name of
            // the file stored in string Array
            File f = new File(dir, fileName);
 
            // create object of BufferedReader
            BufferedReader br = new BufferedReader(new FileReader(f));
            
            // Read from current file
            String line = br.readLine();
            while (line != null) {
            	line = line.substring(9);
            	int index = line.indexOf("],\"meta\"");
            	line = line.substring(0,index);
            	if (!first) {
            		line =  "," + line;
            	}            	
                // write to the output file
                pw.print(line);
                line = br.readLine();
                first = false;
            }
            pw.flush();
        }

        pw.print("]}");
        pw.flush();
        System.out.println("Reading from all files" +
        " in directory " + dir.getName() + " Completed");
        
	}

	
	@SuppressWarnings("static-access")
	public static Options getCliOptions() {
		Options options = new Options();
		
		options.addOption(
				OptionBuilder.withLongOpt("file")
				.withArgName("FILE")
				.withDescription("GEOScan export file")
				.hasArg()
				.isRequired()
				.create(OPT_INPUT_FILE));

		options.addOption(
				OptionBuilder.withLongOpt("output")
				.withArgName("FILE")
				.withDescription("Output file (default, STDOUT)")
				.hasArg()
				.create(OPT_OUTPUT_FILE));
		
		options.addOption(
				OptionBuilder.withLongOpt("combine")
				.withArgName("FILE")
				.withDescription("Output file (default, STDOUT)")
				.hasArg()
				.create(OPT_COMBINE_FILES));
		
		options.addOption(
				OptionBuilder.withLongOpt("combine CFS")
				.withArgName("FILE")
				.withDescription("Output file (default, STDOUT)")
				.hasArg()
				.create(OPT_COMBINE_CFS_FILES));

		return options;
	}
}
