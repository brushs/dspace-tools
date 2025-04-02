package org.dspace.tools.nrcan.migration.filebuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.dspace.tools.nrcan.FileProcessor;

public class CSVRelProcessor implements FileProcessor {

	private FileInputStream inputStream;
	private BufferedReader streamReader;
	private String inPath;
	private String outPath;
	private int itemCount = 0;
	private int archiveCount = 0;
	private int archiveSize = 100;
	private String currentArchivePath;
	private String currentItemPath;
	private String bibLevel;
	private boolean filesOpen = false;
	private boolean beforeFirstItem = true;
	private PrintStream outputStream;
	private Map<String, String> dcElementTemplates;
	private Map<String, String> nrcanElementTemplates;
	private Map<String, String> geospatialElementTemplates;
	private Map<String, Relationship> relationshipElements;
	private Set<String> unknownElements = new HashSet<String>();
	private Set<String> ignoredElements = new HashSet<String>();
	private Set<String> valueSet = new HashSet<String>();
	private boolean firstDateSubmitted = true;
	private String dateIssued = "";
	private String lastDateUpdated = "";
	private Map<Integer, Integer> secSerials;
	private int secSerialCount;
	private int uniqueSecSerialCount;
	private int secSerialNumberCount;
	private String geoScanId;
	private Set<String> existingFundingCodes = new HashSet<String>();
	private Set<String> existingProvinceCodes = new HashSet<String>();
	private Set<String> existingDivisionCodes = new HashSet<String>();
	private Set<String> existingSecSerialCodes = new HashSet<String>();
	private Set<String> existingCountryCodes = new HashSet<String>();
	private Set<String> existingAuthorCodes = new HashSet<String>();
	private Set<String> existingAuthorACodes = new HashSet<String>();
	private Set<String> existingMonoCorpAuthorCodes = new HashSet<String>();
	private Set<String> existingCorpAuthorCodes = new HashSet<String>();
	private Set<String> existingPublisherCodes = new HashSet<String>();
	private Set<String> existingAreaCodes = new HashSet<String>();
	private List<String> bBoxes = new ArrayList<String>();
	private int startId = 0;
	
	private Map<String, String> relMap = new HashMap<String, String>();	

	public CSVRelProcessor(String inPath, String outPath, CommandLine cmd) {
		this.inPath = inPath;
		this.outPath = outPath;
	}
	
	public void process() {
		try {
			System.out.println("CSV Processor initialized");
			
			startId = 15822388;
			
			inputStream = new FileInputStream(inPath);
			streamReader = new BufferedReader(new InputStreamReader(inputStream));
	
			outputStream = getPrintStream(outPath + "/output.csv");
			
			String line;
			
            while ((line = streamReader.readLine()) != null) {
            	processLine(line);
            }
			
//			while(StringUtils.isNotEmpty(line)) {
//				processLine(line);
//				line = streamReader.readLine();
//			}	
			
		}
		catch(Exception ex) {
			System.out.println(ex);
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
			outputStream.close();
		}
		catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	private PrintStream getPrintStream(String filename) throws UnsupportedEncodingException {
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(filename);
		} catch(IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
		return new PrintStream(fileOutputStream, true, StandardCharsets.UTF_8.toString());
	}

	private void processLine(String line) throws Exception {
		if (StringUtils.isEmpty(line) || "COMMIT;".contentEquals(line)) {
			return;
		}
		
		outputStream.println(startId++ + "," + line);
//		String[] fields = line.split(",");
//        
//		String metadataFieldId = fields[0];
//		String dso = fields[1];
//		String gids = fields[2].replace("\"","");
//		String titleEn = fields[3];
//		String titleFr = fields[4];
//		String handle = fields[5];
//		
//		if (StringUtils.isEmpty(titleEn)) {
//			titleEn = titleFr;
//		}
//		if (StringUtils.isEmpty(titleFr)) {
//			titleFr = titleEn;
//		}
//			
//		List<Integer> results = new ArrayList<>();
//        String[] segments = gids.split(";");
//
//        for (String segment : segments) {
//            if (segment.contains(":")) {
//                // This segment is a range
//                String[] rangeBounds = segment.split(":");
//                int start = Integer.parseInt(rangeBounds[0].replace(" ", ""));
//                int end = Integer.parseInt(rangeBounds[1].replace(" ", ""));
//
//                for (int i = start; i <= end; i++) {
//                    results.add(i);
//                }
//            } else {
//                // This segment is a single number
//                results.add(Integer.parseInt(segment.replace(" ", "")));
//            }
//        }
//        
//        for (Integer result : results) {
//        	outputStream.println(metadataFieldId + "," + dso + "," + result);
//        }
        
	}
}
