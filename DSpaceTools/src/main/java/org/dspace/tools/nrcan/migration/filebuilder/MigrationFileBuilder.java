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
import org.apache.commons.lang3.StringUtils;
import org.dspace.tools.nrcan.FileProcessor;

public final class MigrationFileBuilder {
	
	private final static char OPT_MAP_FILE = 'm';
	private final static char OPT_REL_FILE = 'r';
	private final static char OPT_PLACE_FILE = 'p';
	private final static char OPT_INPUT_FILE = 'f';
	private final static char OPT_INPUT_FOLDER = 'd';
	private final static char OPT_OUTPUT_FILE = 'o';
	private final static char OPT_TYPE = 't';
	private final static char OPT_INPUT_CFSID_FILE = 'c';
	
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
		String mapPath = cmd.getOptionValue(OPT_MAP_FILE);
		String relPath = cmd.getOptionValue(OPT_REL_FILE);
		String placePath = cmd.getOptionValue(OPT_PLACE_FILE);
		String inPath = cmd.getOptionValue(OPT_INPUT_FILE);
		String inFolderPath = cmd.getOptionValue(OPT_INPUT_FOLDER);
		String outPath = cmd.getOptionValue(OPT_OUTPUT_FILE);
		String type = cmd.getOptionValue(OPT_TYPE);
		String inCFSIDPath = cmd.getOptionValue(OPT_INPUT_CFSID_FILE);
		
		FileProcessor processor;
		if (!StringUtils.isEmpty(type) && type.contentEquals("cfs")) {
			processor = new CFSFileProcessor(inFolderPath, outPath, inCFSIDPath, cmd);
		} else if (!StringUtils.isEmpty(type) && type.contentEquals("map")) {
			processor = new GEOScanCleanupProcessor(mapPath, inPath, outPath, cmd);
		} else if (!StringUtils.isEmpty(type) && type.contentEquals("rel")) {
			processor = new GEOScanRelationshipCleanupProcessor(mapPath, relPath, placePath, inPath, outPath, cmd);
		} else if (!StringUtils.isEmpty(type) && type.contentEquals("fil")) {
			processor = new GEOScanFilteredFileProcessor(inPath, outPath, cmd);
		} else if (!StringUtils.isEmpty(type) && type.contentEquals("photo")) {
			processor = new PhotoDBFileProcessor(inPath, outPath, cmd);
		}  else {
			processor = new GEOScanFileProcessor(inPath, outPath, cmd);
		}
		
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
				.withDescription("Map file from DSpace")
				.hasArg()
				.create(OPT_MAP_FILE));
		
		options.addOption(
				OptionBuilder.withLongOpt("file")
				.withArgName("FILE")
				.withDescription("Rel file from DSpace")
				.hasArg()
				.create(OPT_REL_FILE));
		
		options.addOption(
				OptionBuilder.withLongOpt("file")
				.withArgName("FILE")
				.withDescription("Place file from DSpace")
				.hasArg()
				.create(OPT_PLACE_FILE));
		
		options.addOption(
				OptionBuilder.withLongOpt("file")
				.withArgName("FILE")
				.withDescription("GEOScan export file")
				.hasArg()
				.create(OPT_INPUT_FILE));
		
		options.addOption(
				OptionBuilder.withLongOpt("folder")
				.withArgName("FOLDER")
				.withDescription("GEOScan export file")
				.hasArg()
				.create(OPT_INPUT_FOLDER));

		options.addOption(
				OptionBuilder.withLongOpt("output")
				.withArgName("FILE")
				.withDescription("Output file (default, STDOUT)")
				.hasArg()
				.create(OPT_OUTPUT_FILE));
		
		options.addOption(
				OptionBuilder.withLongOpt("type")
				.withArgName("Type")
				.withDescription("Type of migration (default, STDOUT)")
				.hasArg()
				.create(OPT_TYPE));
		
		options.addOption(
				OptionBuilder.withLongOpt("cfsids")
				.withArgName("FILE")
				.withDescription("CFSID File")
				.hasArg()
				.create(OPT_INPUT_CFSID_FILE));

		return options;
	}
}
