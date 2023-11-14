package org.dspace.tools.nrcan.migration.filebuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.dspace.tools.nrcan.FileProcessor;
import org.dspace.tools.nrcan.migration.filebuilder.model.AuthorData;
import org.dspace.tools.nrcan.migration.filebuilder.model.CFSFile;
import org.dspace.tools.nrcan.migration.filebuilder.model.CFSItem;
import org.dspace.tools.nrcan.migration.filebuilder.model.ProgramData;
import org.dspace.tools.nrcan.migration.filebuilder.model.ProgramDetailData;
import org.dspace.tools.nrcan.migration.filebuilder.model.SubjectData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import static com.github.pemistahl.lingua.api.Language.*;

public class CFSFileProcessor implements FileProcessor {

	private String inPath;
	private String outPath;
	private int itemCount = 0;
	private int archiveCount = 0;
	private int archiveSize = 100;
	private String currentArchivePath;
	private String currentItemPath;
	private boolean filesOpen = false;
	private PrintStream contentsFileStream;
	private PrintStream relationshipsFileStream;
	private PrintStream dublinCoreFileStream;
	private PrintStream dspaceFileStream;
	private PrintStream nrcanFileStream;
	private PrintStream geospatialFileStream;
	private PrintStream cfsidFileStream;
	private Map<String, String> dcElementTemplates;
	private Map<String, String> nrcanElementTemplates;
	private Map<String, String> geospatialElementTemplates;
	private Map<String, Relationship> relationshipElements;
	private Set<String> ignoredElements = new HashSet<String>();
	private Set<String> valueSet = new HashSet<String>();
	
	private final LanguageDetector detector = LanguageDetectorBuilder.fromLanguages(ENGLISH, FRENCH).build();
	
	private static final String VALUE = "##VALUE##";
	private static final String LANGUAGE = "##LANG##";
	private static final String QUALIFIER = "##QUAL##";
	
	private static final String ELEMENT_TYPE = "dc:type";
	private static final String ELEMENT_DOCTYPE = "doctype";
	private static final String ELEMENT_TITLE_M = "dc:titlem";
	private static final String ELEMENT_TITLE_A = "dc:titlea";
	private static final String ELEMENT_VOLUME = "volume";
	private static final String ELEMENT_ISSUE = "issue";
	private static final String ELEMENT_ARTICLE_NUMBER = "articlenumber";
	private static final String ELEMENT_OPEN_ACCESS = "openaccess";
	private static final String ELEMENT_OPEN_ACCESS_TYPE = "openaccesstype";
	private static final String ELEMENT_SERIAL_CODE = "serialcode";
	private static final String ELEMENT_AUTHOR_A = "dc:creatora";
	private static final String ELEMENT_AUTHOR_M = "dc:creatorm";
	private static final String ELEMENT_IDENTIFIER = "dc:identifier";
	private static final String ELEMENT_PUBLISHER = "dc:publisher";
	private static final String ELEMENT_CORP_AUTHOR_A = "corpcreatora";
	private static final String ELEMENT_CORP_AUTHOR_M = "corpcreatorm";
	private static final String ELEMENT_CONTRIBUTOR = "dc:contributor";
	private static final String ELEMENT_LANGUAGE = "dc:language";
	private static final String ELEMENT_LANGUAGE_ABSTRACT = "dc:languageabst";
	private static final String ELEMENT_NTS = "nts";
	private static final String ELEMENT_PAGE_RANGE = "pagerange";
	private static final String ELEMENT_TOTAL_PAGES = "totalpages";
	private static final String ELEMENT_TOTAL_SHEETS = "totalsheets";
	private static final String ELEMENT_FILE_TYPE = "filetype";
	private static final String ELEMENT_MEDIA = "media";
	private static final String ELEMENT_STATUS = "status";
	private static final String ELEMENT_ONLINE_URL = "onlineurl";
	private static final String ELEMENT_IMAGE = "image";
	private static final String ELEMENT_PLAIN_LANGUAGE_SUMMARY_E = "plainlanguagesummarye";
	private static final String ELEMENT_PLAIN_LANGUAGE_SUMMARY_F = "plainlanguagesummaryf";
	private static final String ELEMENT_ABSTRACT = "dc:abstract";
	private static final String ELEMENT_SUMMARY = "dc:summary";
	private static final String ELEMENT_NOTES = "notes";
	private static final String ELEMENT_POLYGON_WENS = "polygonwens";
	private static final String ELEMENT_POLYGON_DEG = "polygondeg";
	private static final String ELEMENT_COVERAGE = "dc:coverage";
	private static final String ELEMENT_POLICY_PAAE = "policypaae";
	private static final String ELEMENT_POLICY_IMPLICATION_E = "policyimplicatione";
	private static final String ELEMENT_POLICY_IMPLICATION_F = "policyimplicationf";
	private static final String ELEMENT_POLICY_RELEVANCE_E = "policyrelevancee";
	private static final String ELEMENT_POLICY_RELEVANCE_F = "policyrelevancef";
	private static final String ELEMENT_GEOP_SURVEY = "geopsurvey";
	private static final String ELEMENT_GEOGRAPHY = "geography";
	private static final String ELEMENT_WEB_ACCESSIBLE = "webaccessible";
	private static final String ELEMENT_DATE_RECORD_SENT = "daterecordsent";
	private static final String ELEMENT_PREVIOUS_FILENAME = "previousfilename";
	private static final String ELEMENT_COUNTRY = "country";
	private static final String ELEMENT_AREA = "area";
	private static final String ELEMENT_DIVISION = "division";
	private static final String ELEMENT_EDITION = "edition";
	private static final String ELEMENT_RELATION_REF = "dc:relationref";
	private static final String ELEMENT_BIBLIOGRAPHIC_LEVEL = "biblevel";
	private static final String ELEMENT_SUBJECT_GEOSCAN = "dc:subjectgeoscan";
	private static final String ELEMENT_SUBJECT_DESCRIPTOR = "dc:subjectdescriptor";
	private static final String ELEMENT_RECORD_CREATED = "daterecordcr";
	private static final String ELEMENT_README_E = "readmee";
	private static final String ELEMENT_README_F = "readmef";
	private static final String ELEMENT_ARCHIVAL_FILE = "archivalfile";
	private static final String ELEMENT_THESIS = "thesis";
	private static final String ELEMENT_NUMBER_OF_MAPS = "numbermaps";
	private static final String ELEMENT_SUBJECT_OTHER = "dc:subjectother";
	private static final String ELEMENT_SUBJECT_GC = "dc:subjectgoc";
	private static final String ELEMENT_SUBJECT_BROAD = "dc:subjectbroad";
	private static final String ELEMENT_RECORD_MODIFIED = "daterecordmod";
	private static final String ELEMENT_CONT_DESCR = "contdescr";
	private static final String ELEMENT_DOWNLOAD = "download";
	private static final String ELEMENT_MAP = "map";
	private static final String ELEMENT_FUNDING = "funding";
	private static final String ELEMENT_MEETING_NAME = "meetingname";
	private static final String ELEMENT_MEETING_START = "meetingstart";
	private static final String ELEMENT_MEETING_END = "meetingend";
	private static final String ELEMENT_MEETING_CITY = "meetingcity";
	private static final String ELEMENT_MEETING_COUNTRY = "meetingcountry";
	private static final String ELEMENT_ALTERNATE_FORMAT = "altenateformat";
	private static final String ELEMENT_PRINT_DATE = "printdate";
	private static final String ELEMENT_RELATION_URL = "dc:relationurl";
	private static final String ELEMENT_RELATION = "dc:relation";
	private static final String ELEMENT_RELATION_PHOTO = "dc:relationphoto";
	private static final String ELEMENT_DATE_SUBMITTED = "dc:datesubmitted";
	private static final String ELEMENT_DATE_ISSUED = "dc:dateissued";
	private static final String ELEMENT_DATE_AVAILABLE = "dc:dateavailable";
	private static final String ELEMENT_DATE_UPDATED = "dc:dateupdated";
	private static final String ELEMENT_DIGITAL = "digital";
	private static final String ELEMENT_IS_OR_HAS_MAP = "isorhasmap";
	private static final String ELEMENT_CONTAINS_MAP = "containsmap";
	private static final String ELEMENT_REPORT_SERIAL_CODE = "reportserialcode";
	private static final String ELEMENT_REPORT_NUMBER = "reportnumber";
	private static final String ELEMENT_SEC_SERIAL_CODE = "secserialcode";
	private static final String ELEMENT_SEC_SERIAL_NUMBER1 = "secserialnumber1";
	private static final String ELEMENT_SEC_SERIAL_NUMBER2 = "secserialnumber2";
	private static final String ELEMENT_SEC_SERIAL_NUMBER3 = "secserialnumber3";
	private static final String ELEMENT_SEC_SERIAL_NUMBER4 = "secserialnumber4";
	private static final String ELEMENT_SEC_SERIAL_NUMBER5 = "secserialnumber5";
	private static final String ELEMENT_FUNDING_LEGACY = "fundinglegacy";
	private static final String ELEMENT_PROVINCE = "province";
	private static final String ELEMENT_RELATION_ERRATUM = "dc:relationerratum";
	private static final String ELEMENT_CLASSIFICATION = "classification";
	private static final String ELEMENT_DURATION = "dc:duration";
	private static final String ELEMENT_BBOX = "bbox";
	private static final String ELEMENT_RELATION_REPLACES = "replaces";
	private static final String ELEMENT_RELATION_ACCOMPANIES = "accompanies";
	private static final String ELEMENT_RELATION_ISREPLACEDBY = "isreplacedby";
	private static final String ELEMENT_RELATION_ISRELATEDTO = "isrelatedto";
	private static final String ELEMENT_RELATION_ISPARTOF = "ispartof";
	private static final String ELEMENT_RELATION_ISACCOMPANIEDBY = "isaccompaniedby";
	private static final String ELEMENT_RELATION_CONTAINS = "contains";
	private static final String ELEMENT_RELATION_ISENLARGEDFROM = "isnenlargedfrom";
	private static final String ELEMENT_RELATION_ISREDUCEDFROM = "isreducedfrom";
	private static final String ELEMENT_RELATION_ISTRANSLATIONOF = "istranslationof";
	private static final String ELEMENT_RELATION_ISREPRINTEDFROM = "isreprintedfrom";
	private static final String ELEMENT_RELATION_ISREPRINTEDIN = "isreprintedin";
	private static final String ELEMENT_RELATION_TBD = "tbd";
	private static final String ELEMENT_AUTHOR_CFS = "authorCFS";
	private static final String ELEMENT_SERIAL_CFS = "serialCFS";
	private static final String ELEMENT_FUNDING_CFS = "fundingCFS";
	private static final String ELEMENT_DIVISION_CFS = "divisionCFS";
	private static final String ELEMENT_JOURNAL_CFS = "journalCFS";
	private static final String ELEMENT_IDENTIFIER_CFS = "identifierCFS";
	private static final String ELEMENT_CFS = "cfs";
	private static final String ELEMENT_EDITOR_COMPILER = "editor_compiler";
	private static final String ELEMENT_MEETING_PLACE = "meeting_place";
	
	private static final String RELATIONSHIP_SERIAL = "isSerialOfPublication";
	private static final String RELATIONSHIP_SEC_SERIAL = "isSecondarySerialOfPublication";
	private static final String RELATIONSHIP_AUTHOR = "isAuthorOfPublication";
	private static final String RELATIONSHIP_MONOGRAPHIC_AUTHOR = "isMonographicAuthorOfPublication";
	private static final String RELATIONSHIP_PUBLISHER = "isPublisherOfPublication";
	private static final String RELATIONSHIP_CORP_AUTHOR = "isCorporateAuthorOfPublication";
	private static final String RELATIONSHIP_MONOGRAPHIC_CORP_AUTHOR = "isMonoCorpAuthorOfPublication";
	private static final String RELATIONSHIP_COUNTRY = "isCountryOfPublication";
	private static final String RELATIONSHIP_PROVINCE = "isProvinceOfPublication";
	private static final String RELATIONSHIP_AREA = "isAreaOfPublication";
	private static final String RELATIONSHIP_DIVISION = "isDivisionOfPublication";
	private static final String RELATIONSHIP_SPONSOR = "isSponsorOfPublication";
	private static final String RELATIONSHIP_JOURNAL = "isJournalOfPublication";
	
	private static final String ATTRIBUTE_TITLE = "dc.title";
	private static final String ATTRIBUTE_MIGRATION_ID = "nrcan.author.migrationid";
	private static final String ATTRIBUTE_PUB_MIGRATION_ID = "nrcan.publisher.migrationid";
	private static final String ATTRIBUTE_DIV_MIGRATION_ID = "nrcan.division.migrationid";
	private static final String ATTRIBUTE_AREA_MIGRATION_ID = "nrcan.area.migrationid";
	private static final String ATTRIBUTE_DIVISION_CODE = "nrcan.division.code";
	private static final String ATTRIBUTE_SPONSOR_CODE = "nrcan.sponsor.code";
	private static final String ATTRIBUTE_SERIAL_CODE = "nrcan.serial.code";
	private static final String ATTRIBUTE_PROVINCE_CODE = "nrcan.province.code";
	private static final String ATTRIBUTE_CFS_MIGRATION_ID = "nrcan.author.cfsmigrationid";
	private static final String ATTRIBUTE_JOURNAL_MIGRATION_ID = "nrcan.journal.migrationid";
	
	public CFSFileProcessor(String inPath, String outPath, CommandLine cmd) {
		this.inPath = inPath;
		this.outPath = outPath;
	}
	
	public void process() {
		try {
			cfsidFileStream = getPrintStream("C:\\dspace\\csfids");
			File dir = new File(inPath);
			File[] directoryListing = dir.listFiles();
			if (directoryListing != null) {
				for (File child : directoryListing) {
					System.out.println("Processing File: " + child.getPath());
					CFSFile file = get(CFSFile.class, child.getPath());
					
					System.out.println(file.toString());
					
					initializeElementTemplates();
					
					for (CFSItem item : file.getData()) {
						if (isValidItem(item)) {
							processItem(item);
						}
					}
				}
			}
			cfsidFileStream.close();
		}
		catch(Exception ex) {
			System.out.println(ex);
			throw new RuntimeException(ex.getMessage(), ex);
		} finally {
			close();
		}
	}
	
	private <T> T get(Class<T> type, String inPath) {
		ObjectMapper mapper = new ObjectMapper();
	    try {
			return mapper.readValue(new File(inPath), type);
		} catch (IOException ex) {
			System.out.println(ex);
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
	
	@Override
	public void close() {
		try {			
			if (filesOpen) {
				closeOutputFiles();
			}
			
			for (String s : valueSet) {
				System.out.println(s);
			}
			
		}
		catch(Exception ex) {
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
	
	public void processItem(CFSItem input) throws Exception {
		try {					
			if (itemCount == archiveSize || StringUtils.isEmpty(currentArchivePath)) {
				currentArchivePath = "archive_" + String.format("%03d" , archiveCount++);
				itemCount = 0;
			}
			
			currentItemPath = "item_" + String.format("%03d" , itemCount++);
			
			String path = outPath + "\\" + currentArchivePath + "\\" + currentItemPath;
			//System.out.println("PATH: " + path);
			
			createDirectory(path);
			openNewOutputFiles(path);

			processMetadata(input);
			
			closeOutputFiles();
			
			if (itemCount == archiveSize) {
				String directory = outPath + "\\" + currentArchivePath + "\\";
				String filename = outPath + "\\" + "archive_" + String.format("%03d" , archiveCount -1) + ".zip";
				ZipDirectory.zipDirectory(directory, filename);			
			}
			
		} catch (Exception e) {
			throw e;
		}
	}

	private void processMetadata(CFSItem input) throws Exception {

		// ID
		if (StringUtils.isEmpty(input.getUid())) {
			throw new Exception("UID should not be null");
		} else {
			printElement(nrcanFileStream, nrcanElementTemplates.get(ELEMENT_IDENTIFIER_CFS), input.getUid(), "cfsid", null);
			cfsidFileStream.println(input.getUid());
		}
		
		// Title
		if (StringUtils.isEmpty(input.getTitle())) {
			throw new Exception("Title should not be null");
		} else {
			Language detectedLanguage = detector.detectLanguageOf(input.getTitle());
			if (detectedLanguage.equals(ENGLISH)) {
				printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_TITLE_A), input.getTitle(), "en");
			} else {
				printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_TITLE_A), input.getTitle(), "fr");
			}	
		}
		
		// Content
		if (input.getAvailability().getPdf_download().contentEquals("1")) {
			processContent(input.getUid());
		}
		
		// Email download
		if (input.getAvailability().getPdf_email().contentEquals("1")) {
			printElement(nrcanFileStream, nrcanElementTemplates.get(ELEMENT_CFS), "Y", null, null);
		}
		
		// Thumbnail
		if (!input.getCover().contentEquals("false")) {
			processThumbnail(input.getCover());
		}
				
		// Issue Date
		if (StringUtils.isEmpty(input.getYear())) {
			System.out.println("UID:" + input.getUid() + " - Year should not be null");
		} else {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_DATE_ISSUED), input.getYear(), null);
		}
		
		// Authors
		if (input.getAuthors() == null) {
			throw new Exception("Authors should not be null");
		} else {
			for (AuthorData author : input.getAuthors().getData()) {
				printRelationship(ELEMENT_AUTHOR_CFS, author.getUid());
			}	
		}
		
		// Editors
		if (!StringUtils.isEmpty(input.getEditor_compiler())) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_EDITOR_COMPILER), input.getEditor_compiler(), null);	
		}
		
		// Meeting Date
		if (!StringUtils.isEmpty(input.getMeeting_date())) {
			handleMeetingDate(input.getUid(), input.getMeeting_date(), input.getYear());	
		}
		
		// Meeting Place
		if (!StringUtils.isEmpty(input.getPlace())) {
			printElement(nrcanFileStream, nrcanElementTemplates.get(ELEMENT_MEETING_PLACE), input.getEditor_compiler(), null);	
		}
		
		// Abstract
		if (!StringUtils.isEmpty(input.getItemAbstract().getEn())) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_ABSTRACT), input.getItemAbstract().getEn(), "en");
		}
		if (!StringUtils.isEmpty(input.getItemAbstract().getFr())) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_ABSTRACT), input.getItemAbstract().getFr(), "fr");
		}
		
		// Plain Language Summary
		if (!StringUtils.isEmpty(input.getPls().getEn())) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_PLAIN_LANGUAGE_SUMMARY_E), input.getPls().getEn(), "en");
		}
		if (!StringUtils.isEmpty(input.getPls().getFr())) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_PLAIN_LANGUAGE_SUMMARY_F), input.getPls().getFr(), "fr");
		}
		
		// Language
		if (input.getLanguage().getEn().contentEquals("French")) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_LANGUAGE), "fr", null);
		} else {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_LANGUAGE), "en", null);
		}
		
		// DOI
		if (!StringUtils.isEmpty(input.getDoi())) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_IDENTIFIER), input.getDoi(), "doi", null);
		}
		
		// ISSN
		if (!StringUtils.isEmpty(input.getIssn())) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_IDENTIFIER), input.getIssn(), "issn", null);
		}
				
		// ISBN
		if (!StringUtils.isEmpty(input.getIsbn())) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_IDENTIFIER), input.getIsbn(), "isbn", null);
		}
		
		// Volume
		if (!StringUtils.isEmpty(input.getVolume())) {
			printElement(nrcanFileStream, nrcanElementTemplates.get(ELEMENT_VOLUME), input.getVolume(), null, null);
		}
		
		// Issue
		if (!StringUtils.isEmpty(input.getIssue())) {
			printElement(nrcanFileStream, nrcanElementTemplates.get(ELEMENT_ISSUE), replaceAmp(input.getIssue()), null, null);
		}
				
		// Type
		if (input.getType() != null) {
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_TYPE), input.getType().getData().getName().getEn(), "en");
			printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_TYPE), input.getType().getData().getName().getFr(), "fr");
		}
		
		// Journal
		if (!StringUtils.isEmpty(input.getPublication_name())) {
			printRelationship(ELEMENT_JOURNAL_CFS, input.getPublication_name());	
		}
		
		// Subjects
		if (input.getSubjects() != null) {
			for (SubjectData subject : input.getSubjects().getData()) {
				printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_SUBJECT_BROAD), replaceAmp(subject.getSubject().getEn()), "en");
				printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_SUBJECT_BROAD), replaceAmp(subject.getSubject().getFr()), "fr");
			}
		}
		
		// Series
		if (input.getSeries() != null && StringUtils.isNotEmpty(input.getSeries().getData().getName().getEn())) {
			printRelationship(ELEMENT_SERIAL_CFS, input.getSeries().getData().getName().getEn());	
		}
		
		// Division (Centre)
		if (input.getCentre() != null && StringUtils.isNotEmpty(input.getCentre().getData().getUid())) {
			printRelationship(ELEMENT_DIVISION_CFS, input.getCentre().getData().getUid());	
		}
		
		// Sponsor (Program - promis)
		if (input.getPrograms().getData() != null) {
			for (ProgramData program : input.getPrograms().getData()) {
				printRelationship(ELEMENT_FUNDING_CFS, program.getProgram().getEn());
			}		
		}
		
		// Keywords
		if (StringUtils.isNotEmpty(input.getKeywords())) {
			String keywords = input.getKeywords().replace("\r\n", ", ");
			List<String> keywordList = Arrays.asList(keywords.split(","));
			String lang = "en";
			for (String keyword : keywordList) {
				Language detectedLanguage = detector.detectLanguageOf(input.getTitle());
				if (detectedLanguage.equals(FRENCH)) {
					lang = "fr";
				}
				printElement(dublinCoreFileStream, dcElementTemplates.get(ELEMENT_SUBJECT_OTHER), replaceAmp(keyword.trim()), lang);				
			}
		}
		
		/*
		final LanguageDetector detector = LanguageDetectorBuilder.fromLanguages(ENGLISH, FRENCH).build();
		final Language detectedLanguage = detector.detectLanguageOf("languages are awesome");
		System.out.println(input.getType().getData().getName().getEn() + " - " + detector.detectLanguageOf(input.getType().getData().getName().getEn()));
		System.out.println(input.getType().getData().getName().getFr() + " - " + detector.detectLanguageOf(input.getType().getData().getName().getFr()));
		
		if (input.getSeries().getData().getName().getEn() != null) {
			System.out.println(input.getSeries().getData().getName().getEn() + " - " + detector.detectLanguageOf(input.getSeries().getData().getName().getEn()));
			System.out.println(input.getSeries().getData().getName().getFr() + " - " + detector.detectLanguageOf(input.getSeries().getData().getName().getFr()));
		}
		
		if (input.getCentre().getData().getName().getEn() != null) {
			System.out.println(input.getCentre().getData().getName().getEn() + " - " + detector.detectLanguageOf(input.getCentre().getData().getName().getEn()));
			System.out.println(input.getCentre().getData().getName().getFr() + " - " + detector.detectLanguageOf(input.getCentre().getData().getName().getFr()));
		}
		*/
	}
	
	private void printElement(PrintStream stream, String template, String value, String lang) throws Exception {			
		printElement(stream, template, value, null, lang);					
	}
	
	private void printElement(PrintStream stream, String template, String value, String qualifier, String lang) throws Exception {			
		if (template == null) {
			throw new Exception("Element not found");
		}
		
		template = template.replace(VALUE, value);
		
		if (qualifier != null) {
			template = template.replace(QUALIFIER, qualifier);
		}
		
		if (lang != null) {
			template = template.replace(LANGUAGE, lang);
		}
		
		stream.println(template);					
}
	
	private void printRelationship(String element, String value) {			
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
	
	private void openNewOutputFiles(String path) throws UnsupportedEncodingException {
		contentsFileStream = getPrintStream(path + "\\contents");
		relationshipsFileStream = getPrintStream(path + "\\relationships");
		dublinCoreFileStream = getPrintStream(path + "\\dublin_core.xml");
		dspaceFileStream = getPrintStream(path + "\\metadata_dspace.xml");
		nrcanFileStream = getPrintStream(path + "\\metadata_nrcan.xml");
		geospatialFileStream = getPrintStream(path + "\\metadata_geospatial.xml");
		
		initializeDSpaceFile();
		initializeDublinCoreFile();
		initializeNRCanFile();
		initializeGeospatialFile();
	}
	
	private void closeOutputFiles() {
		finalizeXmlFile(dublinCoreFileStream);
		finalizeXmlFile(nrcanFileStream);
		finalizeXmlFile(geospatialFileStream);
		
		contentsFileStream.close();
		relationshipsFileStream.close();
		dublinCoreFileStream.close();
		dspaceFileStream.close();
		nrcanFileStream.close();
		geospatialFileStream.close();
	}
	
	private void finalizeXmlFile(PrintStream printStream) {
		printStream.println("</dublin_core>");
	}
	
	private void initializeDSpaceFile() {
		dspaceFileStream.println("<dublin_core schema=\"dspace\">");
		dspaceFileStream.println("<dcvalue element=\"entity\" qualifier=\"type\">Publication</dcvalue>");
		dspaceFileStream.println("</dublin_core>");
	}
	
	private void initializeDublinCoreFile() {
		dublinCoreFileStream.println("<dublin_core>");
	}

	private void initializeNRCanFile() {
		nrcanFileStream.println("<dublin_core schema=\"nrcan\">");
	}
	
	private void initializeGeospatialFile() {
		geospatialFileStream.println("<dublin_core schema=\"geospatial\">");
	}
	
	private void initializeElementTemplates() {
		dcElementTemplates = new HashMap<String, String>();
		
		dcElementTemplates.put(ELEMENT_TITLE_A, "<dcvalue element=\"title\" qualifier=\"\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_IDENTIFIER, "<dcvalue element=\"identifier\" qualifier=\"" + "##QUAL##" + "\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_LANGUAGE, "<dcvalue element=\"language\" qualifier=\"\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_PLAIN_LANGUAGE_SUMMARY_E, "<dcvalue element=\"description\" qualifier=\"\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_PLAIN_LANGUAGE_SUMMARY_F, "<dcvalue element=\"description\" qualifier=\"\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_ABSTRACT, "<dcvalue element=\"description\" qualifier=\"abstract\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_TYPE, "<dcvalue element=\"type\" qualifier=\"\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_SUBJECT_DESCRIPTOR, "<dcvalue element=\"subject\" qualifier=\"descriptor\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_SUBJECT_GEOSCAN, "<dcvalue element=\"subject\" qualifier=\"geoscan\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_SUBJECT_BROAD, "<dcvalue element=\"subject\" qualifier=\"broad\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_SUBJECT_GC, "<dcvalue element=\"subject\" qualifier=\"gc\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_SUBJECT_OTHER, "<dcvalue element=\"subject\" qualifier=\"other\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RECORD_CREATED, "<dcvalue element=\"description\" qualifier=\"provenance\"  language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_DATE_ISSUED, "<dcvalue element=\"date\" qualifier=\"issued\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_DATE_AVAILABLE, "<dcvalue element=\"date\" qualifier=\"available\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_DATE_UPDATED, "<dcvalue element=\"date\" qualifier=\"updated\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_DATE_SUBMITTED, "<dcvalue element=\"description\" qualifier=\"provenance\"  language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_REPLACES, "<dcvalue element=\"relation\" qualifier=\"replaces\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ACCOMPANIES, "<dcvalue element=\"relation\" qualifier=\"accompanies\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ISREPLACEDBY, "<dcvalue element=\"relation\" qualifier=\"isreplacedby\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ISRELATEDTO, "<dcvalue element=\"relation\" qualifier=\"isrelatedto\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ISPARTOF, "<dcvalue element=\"relation\" qualifier=\"ispartof\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ISACCOMPANIEDBY, "<dcvalue element=\"relation\" qualifier=\"isaccompaniedby\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_CONTAINS, "<dcvalue element=\"relation\" qualifier=\"contains\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ISENLARGEDFROM, "<dcvalue element=\"relation\" qualifier=\"isenlargedfrom\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ISREDUCEDFROM, "<dcvalue element=\"relation\" qualifier=\"isreducedfrom\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ISTRANSLATIONOF, "<dcvalue element=\"relation\" qualifier=\"istranslationof\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ISREPRINTEDFROM, "<dcvalue element=\"relation\" qualifier=\"isreprintedfrom\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_ISREPRINTEDIN, "<dcvalue element=\"relation\" qualifier=\"isreprintedin\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_RELATION_TBD, "<dcvalue element=\"relation\" qualifier=\"\">" + VALUE + "</dcvalue>");
		dcElementTemplates.put(ELEMENT_EDITOR_COMPILER, "<dcvalue element=\"contributor\" qualifier=\"editor\">" + VALUE + "</dcvalue>");
		
		nrcanElementTemplates = new HashMap<String, String>();
		
		nrcanElementTemplates.put(ELEMENT_IDENTIFIER_CFS, "<dcvalue element=\"identifier\" qualifier=\"" + "##QUAL##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_VOLUME, "<dcvalue element=\"volume\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_ISSUE, "<dcvalue element=\"issue\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_OPEN_ACCESS, "<dcvalue element=\"openaccess\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_CONTRIBUTOR, "<dcvalue element=\"sourcesystem\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_NTS, "<dcvalue element=\"nts\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_PAGE_RANGE, "<dcvalue element=\"pagination\" qualifier=\"pagerange\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_TOTAL_PAGES, "<dcvalue element=\"pagination\" qualifier=\"totalpages\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_TOTAL_SHEETS, "<dcvalue element=\"pagination\" qualifier=\"totalsheets\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_FILE_TYPE, "<dcvalue element=\"filetype\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_MEDIA, "<dcvalue element=\"media\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_LANGUAGE_ABSTRACT, "<dcvalue element=\"abstract\" qualifier=\"language\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_ONLINE_URL, "<dcvalue element=\"publication\" qualifier=\"externalurl\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_SUMMARY, "<dcvalue element=\"summary\" qualifier=\"\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_NOTES, "<dcvalue element=\"notes\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_DOCTYPE, "<dcvalue element=\"legacy\" qualifier=\"doctype\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_POLICY_PAAE, "<dcvalue element=\"policy\" qualifier=\"paae\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_POLICY_IMPLICATION_E, "<dcvalue element=\"policy\" qualifier=\"implication\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_POLICY_IMPLICATION_F, "<dcvalue element=\"policy\" qualifier=\"implication\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_POLICY_RELEVANCE_E, "<dcvalue element=\"policy\" qualifier=\"relevance\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_POLICY_RELEVANCE_F, "<dcvalue element=\"policy\" qualifier=\"relevance\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_GEOP_SURVEY, "<dcvalue element=\"geopsurvey\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_GEOGRAPHY, "<dcvalue element=\"geography\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_WEB_ACCESSIBLE, "<dcvalue element=\"legacy\" qualifier=\"webaccessible\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_EDITION, "<dcvalue element=\"edition\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_RELATION_REF, "<dcvalue element=\"legacy\" qualifier=\"relationref\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_ARTICLE_NUMBER, "<dcvalue element=\"articlenumber\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_README_E, "<dcvalue element=\"readme\" qualifier=\"\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_README_F, "<dcvalue element=\"readme\" qualifier=\"\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_THESIS, "<dcvalue element=\"legacy\" qualifier=\"thesis\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_NUMBER_OF_MAPS, "<dcvalue element=\"map\" qualifier=\"numberofmaps\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_TITLE_M, "<dcvalue element=\"monographic\" qualifier=\"title\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_CONT_DESCR, "<dcvalue element=\"legacy\" qualifier=\"contdescr\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_DOWNLOAD, "<dcvalue element=\"legacy\" qualifier=\"download\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_ARCHIVAL_FILE, "<dcvalue element=\"legacy\" qualifier=\"archivalfile\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_MAP, "<dcvalue element=\"map\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_FUNDING_LEGACY, "<dcvalue element=\"legacy\" qualifier=\"sponsor\" language=\"" + "##LANG##" + "\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_MEETING_NAME, "<dcvalue element=\"meeting\" qualifier=\"name\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_MEETING_CITY, "<dcvalue element=\"meeting\" qualifier=\"city\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_MEETING_COUNTRY, "<dcvalue element=\"meeting\" qualifier=\"country\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_ALTERNATE_FORMAT, "<dcvalue element=\"legacy\" qualifier=\"alternateformat\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_PRINT_DATE, "<dcvalue element=\"legacy\" qualifier=\"printdate\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_RELATION_URL, "<dcvalue element=\"legacy\" qualifier=\"relationurl\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_RELATION, "<dcvalue element=\"legacy\" qualifier=\"relation\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_RELATION_PHOTO, "<dcvalue element=\"legacy\" qualifier=\"relationphoto\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_MEETING_START, "<dcvalue element=\"meeting\" qualifier=\"startdate\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_MEETING_END, "<dcvalue element=\"meeting\" qualifier=\"enddate\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_DIGITAL, "<dcvalue element=\"legacy\" qualifier=\"mapdigital\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_IS_OR_HAS_MAP, "<dcvalue element=\"legacy\" qualifier=\"isorhasmap\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_CONTAINS_MAP, "<dcvalue element=\"legacy\" qualifier=\"map\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_REPORT_NUMBER, "<dcvalue element=\"reportnumber\" qualifier=\"\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_SEC_SERIAL_NUMBER1, "<dcvalue element=\"secserial\" qualifier=\"0number\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_SEC_SERIAL_NUMBER2, "<dcvalue element=\"secserial\" qualifier=\"1number\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_SEC_SERIAL_NUMBER3, "<dcvalue element=\"secserial\" qualifier=\"2number\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_SEC_SERIAL_NUMBER4, "<dcvalue element=\"secserial\" qualifier=\"3number\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_SEC_SERIAL_NUMBER5, "<dcvalue element=\"secserial\" qualifier=\"4number\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_DATE_RECORD_SENT, "<dcvalue element=\"legacy\" qualifier=\"daterecordsent\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_RECORD_MODIFIED, "<dcvalue element=\"legacy\" qualifier=\"daterecordmod\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_PREVIOUS_FILENAME, "<dcvalue element=\"legacy\" qualifier=\"previousfilename\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_OPEN_ACCESS_TYPE, "<dcvalue element=\"legacy\" qualifier=\"openaccesstype\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_STATUS, "<dcvalue element=\"legacy\" qualifier=\"status\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_IMAGE, "<dcvalue element=\"legacy\" qualifier=\"thumbnail\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_RELATION_ERRATUM, "<dcvalue element=\"legacy\" qualifier=\"relationerratum\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_CLASSIFICATION, "<dcvalue element=\"legacy\" qualifier=\"classification\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_DURATION, "<dcvalue element=\"legacy\" qualifier=\"duration\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_POLYGON_WENS, "<dcvalue element=\"legacy\" qualifier=\"bbox\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_CFS, "<dcvalue element=\"cfs\" qualifier=\"emailpdf\">" + VALUE + "</dcvalue>");
		nrcanElementTemplates.put(ELEMENT_MEETING_PLACE, "<dcvalue element=\"meeting\" qualifier=\"place\">" + VALUE + "</dcvalue>");
					
		relationshipElements = new HashMap<String, Relationship>();
		
		relationshipElements.put(ELEMENT_SERIAL_CFS, new Relationship(RELATIONSHIP_SERIAL, ATTRIBUTE_TITLE));
		relationshipElements.put(ELEMENT_SERIAL_CODE, new Relationship(RELATIONSHIP_SERIAL, ATTRIBUTE_SERIAL_CODE));
		relationshipElements.put(ELEMENT_AUTHOR_A, new Relationship(RELATIONSHIP_AUTHOR, ATTRIBUTE_MIGRATION_ID));
		relationshipElements.put(ELEMENT_AUTHOR_M, new Relationship(RELATIONSHIP_MONOGRAPHIC_AUTHOR, ATTRIBUTE_MIGRATION_ID));
		relationshipElements.put(ELEMENT_AUTHOR_CFS, new Relationship(RELATIONSHIP_AUTHOR, ATTRIBUTE_CFS_MIGRATION_ID));
		relationshipElements.put(ELEMENT_PUBLISHER, new Relationship(RELATIONSHIP_PUBLISHER, ATTRIBUTE_PUB_MIGRATION_ID));
		relationshipElements.put(ELEMENT_CORP_AUTHOR_A, new Relationship(RELATIONSHIP_CORP_AUTHOR, ATTRIBUTE_PUB_MIGRATION_ID));
		relationshipElements.put(ELEMENT_CORP_AUTHOR_M, new Relationship(RELATIONSHIP_MONOGRAPHIC_CORP_AUTHOR, ATTRIBUTE_PUB_MIGRATION_ID));
		relationshipElements.put(ELEMENT_COUNTRY, new Relationship(RELATIONSHIP_COUNTRY, ATTRIBUTE_TITLE));
		relationshipElements.put(ELEMENT_PROVINCE, new Relationship(RELATIONSHIP_PROVINCE, ATTRIBUTE_PROVINCE_CODE));
		relationshipElements.put(ELEMENT_AREA, new Relationship(RELATIONSHIP_AREA, ATTRIBUTE_AREA_MIGRATION_ID));
		relationshipElements.put(ELEMENT_DIVISION, new Relationship(RELATIONSHIP_DIVISION, ATTRIBUTE_DIVISION_CODE));
		relationshipElements.put(ELEMENT_DIVISION_CFS, new Relationship(RELATIONSHIP_DIVISION, ATTRIBUTE_DIV_MIGRATION_ID));
		relationshipElements.put(ELEMENT_FUNDING, new Relationship(RELATIONSHIP_SPONSOR, ATTRIBUTE_SPONSOR_CODE));
		relationshipElements.put(ELEMENT_FUNDING_CFS, new Relationship(RELATIONSHIP_SPONSOR, ATTRIBUTE_TITLE));
		relationshipElements.put(ELEMENT_REPORT_SERIAL_CODE, new Relationship(RELATIONSHIP_SERIAL, ATTRIBUTE_SERIAL_CODE));
		relationshipElements.put(ELEMENT_SEC_SERIAL_CODE, new Relationship(RELATIONSHIP_SEC_SERIAL, ATTRIBUTE_SERIAL_CODE));
		relationshipElements.put(ELEMENT_JOURNAL_CFS, new Relationship(RELATIONSHIP_JOURNAL, ATTRIBUTE_JOURNAL_MIGRATION_ID));
		
		geospatialElementTemplates = new HashMap<String, String>();
		geospatialElementTemplates.put(ELEMENT_POLYGON_DEG, "<dcvalue element=\"polygon\" qualifier=\"degrees\">" + VALUE + "</dcvalue>");
		geospatialElementTemplates.put(ELEMENT_COVERAGE, "<dcvalue element=\"polygon\" qualifier=\"\">" + VALUE + "</dcvalue>");
		geospatialElementTemplates.put(ELEMENT_BBOX, "<dcvalue element=\"bbox\" qualifier=\"\">" + VALUE + "</dcvalue>");
		
		ignoredElements.add(ELEMENT_BIBLIOGRAPHIC_LEVEL);
		
	}
	
	public List<String> getTokensWithCollection(String str) {
	    return Collections.list(new StringTokenizer(str, ";")).stream()
	      .map(token -> (String) token)
	      .collect(Collectors.toList());
	}
	
	private String replaceAmp(String value) {
		if (value != null) {
			value = value.replace("&","&amp;");
		}
		return value;
	}

	private void processContent(String id) {
		
		String value = "pdfs/" + id + ".pdf";
		
		String output = "-r -s 3 -f " + value;
		
		contentsFileStream.println(output);
	}
	
	private void processThumbnail(String value) {
		
		value = "covers" + value.substring(value.lastIndexOf("/"));

		String output = "-r -s 3 -f " + value;
		
		output = output + "\tbundle:THUMBNAIL";
		
		contentsFileStream.println(output);
	}
	
	private boolean isValidItem(CFSItem input) {
		if (input.getType() == null) {
			return false;
		}

		/*
		if (input.getType().getData().getName().getEn().contentEquals("Journal Article") ||
				input.getType().getData().getName().getEn().contentEquals("Series Item") ||
				input.getType().getData().getName().getEn().contentEquals("Monographs")) {
			return true;
		}
		
		return false;
		*/
		return true;
	}
	
	private void handleMeetingDate(String uid, String value, String year) throws Exception {
		try {	
			LocalDate startDate = null;
			LocalDate endDate = null;
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d uuuu");
			DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MMMM d, uuuu");
			DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("d-MMM-uuuu");
			DateTimeFormatter formatter4 = DateTimeFormatter.ofPattern("dd MMMM uuuu");
			
			value = value.trim();
			
			if (value.contains("-")) {
				long spaceCount = value.chars().filter(ch -> ch == ' ').count();
				value = value.replace(",", "");		
				
				
				
				if (spaceCount == 0) {
					
					// 17-Sep
					try {
						String endDateString = value + "-" + year;
						endDate = LocalDate.parse(endDateString, formatter3);
						startDate = LocalDate.parse(endDateString, formatter3);
					
					// 22-Nov-94
					} catch (Exception e) {
						String yearString = value.substring(value.lastIndexOf("-") + 1);
						if (Integer.parseInt(yearString) > 23) {
							yearString = "19" + yearString;
						} else {
							yearString = "20" + yearString;
						}
						String endDateString = value.substring(0, value.lastIndexOf("-")) + "-" + yearString;
						endDate = LocalDate.parse(endDateString, formatter3);
						startDate = LocalDate.parse(endDateString, formatter3);
					}
					
				}
				
				
				else if (spaceCount == 1) {
					
					// May 16-19
					try {
						String endDateString = value.substring(value.indexOf("-") + 1);
						endDateString = value.substring(0, value.indexOf(" ")) + " " + endDateString + " " + year;
						endDate = LocalDate.parse(endDateString, formatter);
						String startDateString = value.substring(0, value.indexOf("-")) + " " + endDate.getYear();
						startDate = LocalDate.parse(startDateString, formatter);
					
					// 14-16 June
					} catch (Exception e) {
						String endDateString = value.substring(value.indexOf("-") + 1) + " " + year;
						endDate = LocalDate.parse(endDateString, formatter4);
						String startDateString = value.substring(0, value.indexOf("-")) + value.substring(value.indexOf(" ")) + " " + endDate.getYear();
						startDate = LocalDate.parse(startDateString, formatter4);
					}				
				}
				
				
				else if (spaceCount == 2) {
					
					// July 8-12, 2002
					try {
						String endDateString = value.substring(value.indexOf("-") + 1);
						endDateString = value.substring(0, value.indexOf(" ")) + " " + endDateString;
						endDate = LocalDate.parse(endDateString, formatter);
						String startDateString = value.substring(0, value.indexOf("-")) + " " + endDate.getYear();
						startDate = LocalDate.parse(startDateString, formatter);						
					
					} catch (Exception e) {
						
						// October 28-November 1
						try {
							String endDateString = value.substring(value.indexOf("-") + 1);
							endDateString = endDateString + " " + year;
							endDate = LocalDate.parse(endDateString, formatter);
							String startDateString = value.substring(0, value.indexOf("-")) + " " + endDate.getYear();
							startDate = LocalDate.parse(startDateString, formatter);
						
						// 23-27 September 1980
						} catch (Exception f) {
							String endDateString = value.substring(value.indexOf("-") + 1);
							endDate = LocalDate.parse(endDateString, formatter4);
							String startDateString = value.substring(0, value.indexOf("-")) + value.substring(value.indexOf(" "));
							startDate = LocalDate.parse(startDateString, formatter4);
						
						}
						
					}
									
				//April 29-May 11, 1974
				} else if (spaceCount == 3) {
				
					endDate = LocalDate.parse(value.substring(value.indexOf("-") + 1), formatter);
					String startDateString = value.substring(0, value.indexOf("-")) + " " + endDate.getYear();
					startDate = LocalDate.parse(startDateString, formatter);				
					
				// Dec 28, 2011-Jan 2, 2012
				} else if (spaceCount == 4) {
					
					endDate = LocalDate.parse(value.substring(value.indexOf("-") + 1), formatter);
					startDate = LocalDate.parse(value.substring(0, value.indexOf("-")), formatter);
					
					
				// September 28 - October 1, 1987
				} else if (spaceCount == 5) {
					
					endDate = LocalDate.parse(value.substring(value.indexOf("-") + 1).trim(), formatter);
					String startDateString = value.substring(0, value.indexOf("-")).trim() + " " + endDate.getYear();
					startDate = LocalDate.parse(startDateString, formatter);				
								
				} else {
					throw new Exception("Unhandled date format: " + value);
				}
				
			// May 19, 2020
			} else if (value.contains(",")) {
				startDate = LocalDate.parse(value, formatter2);
				endDate = startDate;
			
			// 1970
			} else {
				String template = nrcanElementTemplates.get(ELEMENT_MEETING_START);
				template = template.replace(VALUE, value);
				nrcanFileStream.println(template);
				
				template = nrcanElementTemplates.get(ELEMENT_MEETING_END);
				template = template.replace(VALUE, value);
				nrcanFileStream.println(template);
				
				return;
			}
			
			String template = nrcanElementTemplates.get(ELEMENT_MEETING_START);
			template = template.replace(VALUE, startDate.format(DateTimeFormatter.ISO_DATE));
			nrcanFileStream.println(template);
			
			template = nrcanElementTemplates.get(ELEMENT_MEETING_END);
			template = template.replace(VALUE, endDate.format(DateTimeFormatter.ISO_DATE));
			nrcanFileStream.println(template);
		
		} catch (Exception e) {
			System.out.println("CFS ID: " + uid + " - Meeting Date: " + value);
		}
	}
	
}
