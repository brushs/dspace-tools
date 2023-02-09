package org.dspace.tools.nrcan.migration.filebuilder;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dspace.tools.nrcan.FileProcessor;

public final class MigrationFileBuilder {
	private final static char OPT_INPUT_FILE = 'f';
	private final static char OPT_OUTPUT_FILE = 'o';
	
	public static final void main(String[] args) {
		CommandLineParser parser = new PosixParser();
		Options options = getCliOptions();
		CommandLine cmd;
		
		try {
			cmd = parser.parse(options, args);
		}
		catch(ParseException ex) {
			System.out.println("ERROR: " + ex.getMessage());
			new HelpFormatter().printUsage(new PrintWriter(System.out, true), 80, MigrationFileBuilder.class.getName(), options);
			new HelpFormatter().printHelp(MigrationFileBuilder.class.getName(), "", options, "");
			return;
		}

		processFile(cmd);
	}
	
	private static void processFile(CommandLine cmd) {
		String inPath = cmd.getOptionValue(OPT_INPUT_FILE);
		String outPath = cmd.getOptionValue(OPT_OUTPUT_FILE);

		FileProcessor processor = new GEOScanFileProcessor(inPath, outPath, cmd);
		
		try {
			processor.process();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		return options;
	}
}
