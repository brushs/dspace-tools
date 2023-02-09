package org.dspace.tools.nrcan.migration.filebuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.dspace.tools.nrcan.FileProcessor;

public class GEOScanFileProcessor implements FileProcessor {

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
	private boolean beforeFirstItem = true;
	private PrintStream contentsFileStream;
	private PrintStream relationshipsFileStream;
	private PrintStream dublinCoreFileStream;
	private PrintStream dspaceFileStream;
	private PrintStream nrcanFileStream;
	private Map<String, String> dcElementTemplates;
	private Map<String, String> nrcanElementTemplates;
	private Map<String, Relationship> relationshipElements;

	private static final String VALUE = "##VALUE##";
	private static final String LANGUAGE = "##LANG##";
	
	private static final String ELEMENT_CONTENT = "dc:content";
	private static final String ELEMENT_TITLE = "dc:titlea";
	private static final String ELEMENT_VOLUME = "volume";
	private static final String ELEMENT_SERIAL = "serialtitle";
	private static final String ELEMENT_AUTHOR = "dc:creator";
	
	private static final String RELATIONSHIP_SERIAL = "isSerialOfPublication";
	private static final String RELATIONSHIP_AUTHOR = "isAuthorOfPublication";
	
	private static final String ATTRIBUTE_TITLE = "dc.title";
	private static final String ATTRIBUTE_MIGRATION_ID = "nrcan.author.migrationid";
	
	public GEOScanFileProcessor(String inPath, String outPath, CommandLine cmd) {
		this.inPath = inPath;
		this.outPath = outPath;
	}
	
	public void process() {
		try {
			initializeElementTemplates();
			
			inputStream = new FileInputStream(inPath);
			streamReader = new BufferedReader(new InputStreamReader(inputStream));
	
			String line = streamReader.readLine();
	
			while(line != null) {
				processLine(line);
				line = streamReader.readLine();
			}
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
		if (StringUtils.isEmpty(input.trim())) {
			return;
		}
		
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
			beforeFirstItem = false;
			return;
		}
		
		if (StringUtils.trim(input).substring(0, Math.min(7, input.length())).equals("</item>")) {
			closeOutputFiles();
			filesOpen = false;
			return;
		}
		
		if (!beforeFirstItem) {
			processMetadata(input);
		}	
	}

	private void processMetadata(String line) {
		Integer indexOfGT = line.indexOf(">");
		Integer indexOfSpace = line.indexOf(" ");
		
		String element = line.substring(1, 
				Math.min(indexOfGT < 0 ? line.length()-1 : indexOfGT, indexOfSpace < 0 ? line.length()-1 : indexOfSpace));
		
		processElement(element.toLowerCase(), line);
	}
	
	private void processElement(String element, String line) {
		
		if (element.contentEquals(ELEMENT_CONTENT)) {
			processContent(element, line);
			return;
		}
		
		if (relationshipElements.containsKey(element)) {
			processRelationship(element, line);
			return;
		}
		
		boolean isDCElement = true;
		String template = dcElementTemplates.get(element);
		if (StringUtils.isEmpty(template)) {
			isDCElement = false;
			template = nrcanElementTemplates.get(element);
		}
		
		if (StringUtils.isEmpty(template)) {
			System.out.println("UNKNOWN ELEMENT: " + element);
			return;
		}
		
		String value = "";
		String language = "";
		
		switch (element) {
			case ELEMENT_TITLE :
				value = getElementGeneric(line);
				language = getElementLanguageGeneric(line);
				break;
			case ELEMENT_VOLUME :
				value = getElementGeneric(line);
				break;
			default :
				//
		};
		
		template = template.replace(VALUE, value);
		template = template.replace(LANGUAGE, language);
		
		if (isDCElement) {
			dublinCoreFileStream.println(template);
		} else {
			nrcanFileStream.println(template);
		}
		
	}
	
	private void processContent(String element, String line) {
		
		String value = getElementGeneric(line);

		String output = "-r -s 0 -f " + value;
		
		contentsFileStream.println(output);
	}
	
	private void processRelationship(String element, String line) {
		
		String value = "";
		switch (element) {
		case ELEMENT_SERIAL :
			value = getElementGeneric(line);
			break;
		case ELEMENT_AUTHOR :
			value = getAuthorMigrationId(line);
			break;
		default :
			//
		};
		
		Relationship rel = relationshipElements.get(element);
		
		String output = "relationship." + rel.getName() + " " + rel.getAttribute() + ":" + value;
		
		relationshipsFileStream.println(output);
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
		initializeDublinCoreFile();
		initializeNRCanFile();
	}
	
	private void closeOutputFiles() {
		finalizeDublinCoreFile();
		finalizeNRCanFile();
		
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
	
	private void initializeDublinCoreFile() {
		dublinCoreFileStream.println("<dublin_core>");
	}
	
	private void finalizeDublinCoreFile() {
		dublinCoreFileStream.println("</dublin_core>");
	}
	
	private void initializeNRCanFile() {
		nrcanFileStream.println("<dublin_core schema=\"nrcan\">");
	}
	
	private void finalizeNRCanFile() {
		nrcanFileStream.println("</dublin_core>");
	}
	
	private void initializeElementTemplates() {
		dcElementTemplates = new HashMap<String, String>();
		
		//dcElementTemplates.put(ELEMENT_TITLE, "<dcvalue element=\"title\" qualifier=\"alternative\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_TITLE, "<dcvalue element=\"title\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		
		nrcanElementTemplates = new HashMap<String, String>();
		
		nrcanElementTemplates.put(ELEMENT_VOLUME, "<dcvalue element=\"volume\" qualifier=\"\">" + VALUE + "</dcvalue>");
		
		relationshipElements = new HashMap<String, Relationship>();
		
		relationshipElements.put(ELEMENT_SERIAL, new Relationship(RELATIONSHIP_SERIAL, ATTRIBUTE_TITLE));
		relationshipElements.put(ELEMENT_AUTHOR, new Relationship(RELATIONSHIP_AUTHOR, ATTRIBUTE_MIGRATION_ID));
		
	}
	
	private String getElementGeneric(String line) {
		return line.substring(line.indexOf(">") + 1, line.substring(1).indexOf("<") + 1);
	}
	
	private String getElementLanguageGeneric(String line) {
		int pos = line.indexOf("xml:lang");
		return line.substring(pos + 10, pos + 12);
	}
	
	private String getAuthorMigrationId(String line) {
		try {
			int pos = line.indexOf("given_name");
			String firstName = line.substring(pos + 11, line.indexOf("</given_name"));
			pos = line.indexOf("surname");
			String lastName = line.substring(pos + 8, line.indexOf("</surname"));
			String deptId = "";
			try {
				pos = line.indexOf("dpsid");
				deptId = line.substring(pos + 6, line.indexOf("</dpsid"));
			} catch (Exception e) {
				//
			}
			String orcId = "";
			try {
				pos = line.indexOf("ORCID");
				orcId = line.substring(pos + 44, line.indexOf("</ORCID"));
			} catch (Exception e) {
				//
			}
			return firstName + "_" + lastName + "_" + deptId + "_" + orcId;
		} catch (Exception e) {
			return null;
		}		
	}
}
