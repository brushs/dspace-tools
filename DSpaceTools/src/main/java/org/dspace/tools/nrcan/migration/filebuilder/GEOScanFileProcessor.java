package org.dspace.tools.nrcan.migration.filebuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

public class GEOScanFileProcessor implements FileProcessor {

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
	private PrintStream contentsFileStream;
	private PrintStream relationshipsFileStream;
	private PrintStream dublinCoreFileStream;
	private PrintStream dspaceFileStream;
	private PrintStream nrcanFileStream;
	private PrintStream geospatialFileStream;
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
	
	private static final String VALUE = "##VALUE##";
	private static final String LANGUAGE = "##LANG##";
	private static final String QUALIFIER = "##QUAL##";
	
	private static final String ELEMENT_TYPE = "dc:type";
	private static final String ELEMENT_DOCTYPE = "doctype";
	private static final String ELEMENT_CONTENT = "dc:content";
	private static final String ELEMENT_TITLE_M = "dc:titlem";
	private static final String ELEMENT_TITLE_A = "dc:titlea";
	private static final String ELEMENT_VOLUME = "volume";
	private static final String ELEMENT_ISSUE = "issue";
	private static final String ELEMENT_ARTICLE_NUMBER = "articlenumber";
	private static final String ELEMENT_OPEN_ACCESS = "openaccess";
	private static final String ELEMENT_OPEN_ACCESS_TYPE = "openaccesstype";
	private static final String ELEMENT_SERIAL_CODE = "serialcode";
	private static final String ELEMENT_JOURNAL_CODE = "journalcode";
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
	private static final String ELEMENT_AREA = "areat";
	private static final String ELEMENT_AREA_TEXT = "area";
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
	private static final String ELEMENT_MEETING_DATE = "meetingdate";
	private static final String ELEMENT_MEETING_START = "meetingstart";
	private static final String ELEMENT_MEETING_END = "meetingend";
	private static final String ELEMENT_MEETING_CITY = "meetingcity";
	private static final String ELEMENT_MEETING_COUNTRY = "meetingcountry";
	private static final String ELEMENT_ALTERNATE_FORMAT = "altenateformat";
	private static final String ELEMENT_PRINT_DATE = "printdate";
	private static final String ELEMENT_RELATION_URL = "dc:relationurl";
	private static final String ELEMENT_RELATION = "dc:relation";
	private static final String ELEMENT_RELATION_PHOTO = "dc:relationphoto";
	private static final String ELEMENT_DATE = "dc:date";
	private static final String ELEMENT_DATE_SUBMITTED = "dc:datesubmitted";
	private static final String ELEMENT_DATE_ISSUED = "dc:dateissued";
	private static final String ELEMENT_DATE_AVAILABLE = "dc:dateavailable";
	private static final String ELEMENT_DATE_UPDATED = "dc:dateupdated";
	private static final String ELEMENT_DIGITAL = "digital";
	private static final String ELEMENT_IS_OR_HAS_MAP = "isorhasmap";
	private static final String ELEMENT_CONTAINS_MAP = "containsmap";
	private static final String ELEMENT_REPORT_NUMBER = "reportnumber";
	private static final String ELEMENT_SEC_SERIAL_CODE = "secserialcode";
	private static final String ELEMENT_SEC_SERIAL_NUMBER = "secserialnumber";
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
	private static final String ELEMENT_COUNTRY_CANADA = "countryCAN";
	
	private static final String RELATIONSHIP_SERIAL = "isSerialOfPublication";
	private static final String RELATIONSHIP_JOURNAL = "isJournalOfPublication";
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
	
	private static final String ATTRIBUTE_TITLE = "dc.title";
	private static final String ATTRIBUTE_MIGRATION_ID = "nrcan.author.migrationid";
	private static final String ATTRIBUTE_PUB_MIGRATION_ID = "nrcan.publisher.migrationid";
	private static final String ATTRIBUTE_CA_MIGRATION_ID = "nrcan.corpauthor.migrationid";
	private static final String ATTRIBUTE_AREA_MIGRATION_ID = "nrcan.area.migrationid";
	private static final String ATTRIBUTE_DIVISION_CODE = "nrcan.division.code";
	private static final String ATTRIBUTE_SPONSOR_CODE = "nrcan.sponsor.code";
	private static final String ATTRIBUTE_SERIAL_CODE = "nrcan.serial.code";
	private static final String ATTRIBUTE_JOURNAL_CODE = "nrcan.journal.code";
	private static final String ATTRIBUTE_PROVINCE_CODE = "nrcan.province.code";
	private static final String ATTRIBUTE_COUNTRY_CODE = "nrcan.country.code";
	
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
			String nextLine;
			
			while(line != null) {
				nextLine = streamReader.readLine();
				if (nextLine == null) {
					processLine(line);
				} else {
					if (!nextLine.startsWith("<")) {
						while (nextLine != null && !nextLine.startsWith("<")) {
							line = line + nextLine;
							nextLine = streamReader.readLine();
						}
						if (nextLine != null && nextLine.startsWith("</") && !nextLine.startsWith("</item")) {
							line = line + nextLine;
							nextLine = streamReader.readLine();
						}
						processLine(line);
					} else if (nextLine.startsWith("</item>")) {
						processLine(line);
					} else if (nextLine.startsWith("</")) {
						line = line + nextLine;
						processLine(line);
						nextLine = streamReader.readLine();
					} else {
						processLine(line);
					}
				}
				line = nextLine;
			}
			
			if (itemCount != archiveSize) {
				String directory = outPath + "\\" + currentArchivePath + "\\";
				String filename = outPath + "\\" + "archive_" + String.format("%03d" , archiveCount -1) + ".zip";
				ZipDirectory.zipDirectory(directory, filename);			
			}
			
			for (String element : unknownElements) {
				System.out.println("UNKNOWN ELEMENT: " + element);
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
			
			for (String s : valueSet) {
				System.out.println(s);
			}
			
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
	
	public void processLine(String input) throws Exception {
		try {		
			if (StringUtils.isEmpty(input.trim())) {
				return;
			}
			
			if (StringUtils.trim(input).equals("<item>")) {
				bibLevel = "";
				firstDateSubmitted = true;
				dateIssued = "";
				lastDateUpdated = "";
				secSerials = new HashMap<Integer, Integer>();
				secSerialCount = 0;
				uniqueSecSerialCount = 0;
				secSerialNumberCount = 0;
				geoScanId= "";
				existingFundingCodes = new HashSet<String>();
				existingProvinceCodes = new HashSet<String>();
				existingDivisionCodes = new HashSet<String>();
				existingSecSerialCodes = new HashSet<String>();
				existingCountryCodes = new HashSet<String>();
				existingAuthorCodes = new HashSet<String>();
				existingAuthorACodes = new HashSet<String>();
				existingMonoCorpAuthorCodes = new HashSet<String>();
				existingCorpAuthorCodes = new HashSet<String>();
				existingPublisherCodes = new HashSet<String>();
				existingAreaCodes = new HashSet<String>();
				bBoxes = new ArrayList<String>();
				
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
				printBBox();
				printDateIssued();
				closeOutputFiles();
				filesOpen = false;
				
				if (itemCount == archiveSize) {
					String directory = outPath + "\\" + currentArchivePath + "\\";
					String filename = outPath + "\\" + "archive_" + String.format("%03d" , archiveCount -1) + ".zip";
					ZipDirectory.zipDirectory(directory, filename);			
				}
				
				return;
			}
			
			if (!beforeFirstItem) {
				processMetadata(input);
			}	
		} catch (Exception e) {
			throw e;
		}
	}

	private void processMetadata(String line) throws Exception {
		Integer indexOfGT = line.indexOf(">");
		Integer indexOfSpace = line.indexOf(" ");
		
		String element = line.substring(1, 
				Math.min(indexOfGT < 0 ? line.length()-1 : indexOfGT, indexOfSpace < 0 ? line.length()-1 : indexOfSpace));
		
		processElement(element.toLowerCase(), line);
	}
	
	private void processElement(String element, String line) throws Exception {
		
		if (element.contentEquals(ELEMENT_CONTENT)) {
			processContent(element, line);
			return;
		}
		
		if (relationshipElements.containsKey(element)) {
			processRelationship(element, line);
			if (!element.contentEquals(ELEMENT_FUNDING)) {
				return;
			}		
		}
		
		String value = "";
		String language = "";
		String qualifier = "";
		
		switch (element) {
			case ELEMENT_TYPE :
				value = getElementGenericCapitalize(line);
				break;
			case ELEMENT_TITLE_M :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = getElementLanguageGeneric(line);
				if (bibLevel.toLowerCase().contentEquals("m")) {
					element = ELEMENT_TITLE_A;
				}
				break;
			case ELEMENT_TITLE_A :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = getElementLanguageGeneric(line);
				break;
			case ELEMENT_ABSTRACT :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = getElementLanguageGeneric(line);
				break;
			case ELEMENT_SUMMARY :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = getElementLanguageGeneric(line);
				break;
			case ELEMENT_VOLUME :
				value = getElementGeneric(line);
				break;
			case ELEMENT_ISSUE :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_OPEN_ACCESS :
				value = getElementOpenAccess(line);
				break;
			case ELEMENT_OPEN_ACCESS_TYPE :
				value = getElementGeneric(line);
				break;
			case ELEMENT_STATUS :
				value = getElementGeneric(line);
				break;
			case ELEMENT_DATE_RECORD_SENT :
				value = getElementDateRecordTouched(line);
				break;
			case ELEMENT_PREVIOUS_FILENAME :
				value = getElementGeneric(line);
				break;
			case ELEMENT_RECORD_MODIFIED :
				value = getElementDateRecordTouched(line);
				break;
			case ELEMENT_ARCHIVAL_FILE :
				value = getElementArchivalFile(line);
				break;
			case ELEMENT_IDENTIFIER :
				value = getElementIdentifier(line);
				qualifier = getElementIdentifierQualifier(line).toLowerCase();
				if (line.contains("GID")) {
					geoScanId = value;
				}
				break;
			case ELEMENT_CONTRIBUTOR :
				value = getElementGeneric(line);
				break;
			case ELEMENT_NTS :
				value = getElementGeneric(line);
				break;
			case ELEMENT_PAGE_RANGE :
				value = getElementGeneric(line);
				break;	
			case ELEMENT_TOTAL_PAGES :
				value = getElementGeneric(line);
				break;	
			case ELEMENT_LANGUAGE :
				value = getElementLanguage(line);
				break;
			case ELEMENT_FILE_TYPE :
				value = getElementGeneric(line);
				break;
			case ELEMENT_MEDIA :
				value = getElementGeneric(line);
				break;
			case ELEMENT_LANGUAGE_ABSTRACT :
				value = getElementLanguage(line);
				break;
			case ELEMENT_ONLINE_URL :
				value = getElementGeneric(line);
				value = StringEscapeUtils.escapeXml(value);
				break;
			case ELEMENT_PLAIN_LANGUAGE_SUMMARY_E :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = "en";
				break;
			case ELEMENT_PLAIN_LANGUAGE_SUMMARY_F :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = "fr";
				break;
			case ELEMENT_NOTES :
				line = line.replace("\n", " - ").replace("\r", " - ");
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_DOCTYPE :
				value = getElementGeneric(line);
				break;
			case ELEMENT_AREA_TEXT :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_POLYGON_WENS :
				value = getElementPolygonWENS(line);
				if (containsNumber(value)) {
					bBoxes.add(value);
				}		
				return;
			case ELEMENT_POLYGON_DEG :
				value = getElementGeneric(line);
				break;
			case ELEMENT_COVERAGE :
				value = getElementGeneric(line);
				break;
			case ELEMENT_POLICY_PAAE :
				value = getElementGeneric(line);
				break;
			case ELEMENT_POLICY_IMPLICATION_E :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = "en";
				break;
			case ELEMENT_POLICY_IMPLICATION_F :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = "fr";
				break;
			case ELEMENT_POLICY_RELEVANCE_E :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = "en";
				break;
			case ELEMENT_POLICY_RELEVANCE_F :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				language = "fr";
				break;
			case ELEMENT_GEOP_SURVEY :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_GEOGRAPHY :
				value = getElementGeneric(line);
				break;
			case ELEMENT_WEB_ACCESSIBLE :
				value = getElementGeneric(line);
				break;
			case ELEMENT_EDITION :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_RELATION_REF :
				value = getElementGeneric(line);
				element = getRelationElement(value);
				value = "GID:" + value.substring(value.indexOf("-") + 1);
				break;
			case ELEMENT_BIBLIOGRAPHIC_LEVEL :
				value = getElementGeneric(line);
				bibLevel = value;
				return;
			case ELEMENT_SUBJECT_DESCRIPTOR :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_IMAGE :
				value = getElementGeneric(line);
				value = StringEscapeUtils.escapeXml(value);
				processThumbnail(element, line);
				return;
			case ELEMENT_SUBJECT_GEOSCAN :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_SUBJECT_BROAD :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_SUBJECT_GC :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_SUBJECT_OTHER :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_ARTICLE_NUMBER :
				value = getElementGeneric(line);
				break;
			case ELEMENT_DOWNLOAD :
				value = getElementDownload(line);
				break;
			case ELEMENT_RECORD_CREATED :
				value = getElementRecordCreated(line);
				language = "en";
				break;
			case ELEMENT_README_E :
				value = getElementGeneric(line);
				language = "en";
				break;
			case ELEMENT_README_F :
				value = getElementGeneric(line);
				language = "fr";
				break;
			case ELEMENT_THESIS :
				value = getElementThesis(line);
				break;
			case ELEMENT_TOTAL_SHEETS :
				value = getElementGeneric(line);
				break;
			case ELEMENT_CONT_DESCR :
				value = getElementGeneric(line);
				break;
			case ELEMENT_MAP :
				value = getElementMap(line);
				break;
			case ELEMENT_FUNDING :
				value = getElementFundingCode(line);
				if (value == null) {
					value = getElementFundingLegacy(line);
					if (value == null) {
						return;
					}
					value = replaceAmp(value);
					value = replaceLTGT(value);
					language = getElementFundingLegacyLang(line);
					element = ELEMENT_FUNDING_LEGACY;
				} else {
					return;
				}
				break;
			case ELEMENT_MEETING_NAME :
				value = getElementGeneric(line);
				value = replaceLTGT(value);
				value = StringEscapeUtils.escapeXml(value);
				break;
			case ELEMENT_MEETING_DATE :
				value = getElementGeneric(line);
				handleMeetingDate(value);
				return;
			case ELEMENT_MEETING_CITY :
				value = getElementGeneric(line);
				break;
			case ELEMENT_MEETING_COUNTRY :
				value = getElementGeneric(line);
				break;
			case ELEMENT_PRINT_DATE :
				value = getElementGeneric(line);
				break;
			case ELEMENT_RELATION :
				value = getElementGeneric(line);
				break;
			case ELEMENT_RELATION_URL :
				value = getElementRelationUrl(line);
				value = StringEscapeUtils.escapeXml(value);
				break;
			case ELEMENT_ALTERNATE_FORMAT :
				value = getElementGeneric(line);
				break;
			case ELEMENT_DATE :
				dateIssued = getElementGeneric(line);
				return;
			case ELEMENT_DATE_SUBMITTED :			
				if (firstDateSubmitted) {
					firstDateSubmitted = false;
										
					if (dateIssued != null && dateIssued.contentEquals(getElementGeneric(line).substring(0,4))) {
						dateIssued = getElementGeneric(line);
					}

					element = ELEMENT_DATE_AVAILABLE;
					value = getElementGeneric(line);
					break;
				}
				value = getElementRecordUpdated(line);
				lastDateUpdated = value;
				break;
			case ELEMENT_NUMBER_OF_MAPS :
				value = getElementGeneric(line);
				break;
			case ELEMENT_DIGITAL :
				value = getElementGeneric(line);
				break;
			case ELEMENT_CONTAINS_MAP :
				value = getElementGeneric(line);
				break;
			case ELEMENT_IS_OR_HAS_MAP :
				value = getElementGeneric(line);
				break;
			case ELEMENT_RELATION_PHOTO :
				handleRelationPhotoElement(getElementGeneric(line));
				return;
			case ELEMENT_REPORT_NUMBER :
				value = getElementGeneric(line);
				value = replaceAmp(value);
				break;
			case ELEMENT_RELATION_ERRATUM :
				value = getElementGeneric(line);
				break;
			case ELEMENT_CLASSIFICATION :
				value = getElementGeneric(line);
				break;
			case ELEMENT_DURATION :
				value = getElementGeneric(line);
				break;
			case ELEMENT_SEC_SERIAL_NUMBER :
				value = getElementGeneric(line);
				element = ELEMENT_SEC_SERIAL_NUMBER + secSerials.get(++secSerialNumberCount);
				if (element.contentEquals("secserialnumbernull")) {
					System.out.println("GID: " + geoScanId + " - Too many serials or no serial code");
					return;
				}
				break;
			default :
				unknownElements.add(element);
				return;
		};
		
		boolean isDCElement = true;
		boolean isGeospatialElement = true;
		String template = dcElementTemplates.get(element);
		if (StringUtils.isEmpty(template)) {
			isDCElement = false;
			template = geospatialElementTemplates.get(element);
			if (StringUtils.isEmpty(template)) {
				isGeospatialElement = false;
				template = nrcanElementTemplates.get(element);
			}
		}
		
		try {
			template = template.replace(VALUE, value);
		} catch (Exception e) {
			unknownElements.add(element);
			if (element.contentEquals("fundinglegacy")) {
				unknownElements.add(element);
			}
			return;
		}
		
		template = template.replace(LANGUAGE, language);
		template = template.replace(QUALIFIER, qualifier);
		
		if (isDCElement) {
			dublinCoreFileStream.println(template);
		} else if (isGeospatialElement) {
			geospatialFileStream.println(template);
		} else {
			nrcanFileStream.println(template);
		}
		
	}
	
	private void processContent(String element, String line) {
		
		String value = getElementGeneric(line);

		value = "STPublications_PublicationsST/" + value;
		
		value = value.replace("//","/");
		
		String output = "-r -s 0 -f " + value;
		
		contentsFileStream.println(output);
	}
	
	private void processThumbnail(String element, String line) {
		
		String value = getElementGeneric(line);
		
		value = "thumbnails" + value.substring(value.lastIndexOf("/"));

		String output = "-r -s 2 -f " + value;
		
		output = output + "\tbundle:THUMBNAIL";
		
		contentsFileStream.println(output);
	}
	
	private void processRelationship(String element, String line) {
		
		String value = "";
		switch (element) {
		case ELEMENT_SERIAL_CODE :
			value = getElementGeneric(line);
			break;
		case ELEMENT_JOURNAL_CODE :
			value = getElementGeneric(line);
			break;
		case ELEMENT_PUBLISHER :
			value = getElementGeneric(line);
			value = value.toUpperCase();
			if (existingPublisherCodes.contains(value)) {
				System.out.println("GID: " + geoScanId + " - Duplicate Publishers?");
				return;
			} else {
				existingPublisherCodes.add(value);
			}
			break;
		case ELEMENT_AUTHOR_A :
			value = getAuthorMigrationId(line);
			if (existingAuthorACodes.contains(value)) {
				System.out.println("GID: " + geoScanId + " - Duplicate Authors?");
				return;
			} else {
				existingAuthorACodes.add(value);
			}
			break;
		case ELEMENT_AUTHOR_M :
			value = getAuthorMigrationId(line);
			if (existingAuthorCodes.contains(value)) {
				System.out.println("GID: " + geoScanId + " - Duplicate Authors?");
				return;
			} else {
				existingAuthorCodes.add(value);
			}
			if (bibLevel.toLowerCase().contentEquals("m")) {
				element = ELEMENT_AUTHOR_A;
			}
			break;
		case ELEMENT_COUNTRY :
			value = getElementGeneric(line);
			if (existingCountryCodes.contains(value)) {
				return;
			} else {
				existingCountryCodes.add(value);
			}
			if (value.contentEquals("Canada")) {
				return;
			}
			break;
		case ELEMENT_PROVINCE :
			value = getElementGeneric(line);
			if (value.contentEquals("can")) {
				return;
			}
			if (existingProvinceCodes.contains(value)) {
				return;
			} else {
				existingProvinceCodes.add(value);
			}
			break;
		case ELEMENT_AREA :
			value = getElementGeneric(line);
			if (existingAreaCodes.contains(value)) {
				return;
			} else {
				existingAreaCodes.add(value);
			}
			break;
		case ELEMENT_DIVISION :
			value = getElementDivision(line);
			if (existingDivisionCodes.contains(value)) {
				return;
			} else {
				existingDivisionCodes.add(value);
			}
			break;
		case ELEMENT_CORP_AUTHOR_A :
			value = getElementGeneric(line);
			value = replaceAmp(value);
			if (existingCorpAuthorCodes.contains(value)) {
				System.out.println("GID: " + geoScanId + " - Duplicate Corp Authors?");
				return;
			} else {
				existingCorpAuthorCodes.add(value);
			}
			break;
		case ELEMENT_CORP_AUTHOR_M :
			value = getElementGeneric(line);
			value = replaceAmp(value);
			if (bibLevel.toLowerCase().contentEquals("m")) {
				element = ELEMENT_CORP_AUTHOR_A;
				if (existingCorpAuthorCodes.contains(value)) {
					System.out.println("GID: " + geoScanId + " - Duplicate Corp Authors?");
					return;
				} else {
					existingCorpAuthorCodes.add(value);
				}
			} else {							
				if (existingMonoCorpAuthorCodes.contains(value)) {
					System.out.println("GID: " + geoScanId + " - Duplicate Mono Corp Authors?");
					return;
				} else {
					existingMonoCorpAuthorCodes.add(value);
				}
			}
			break;
		case ELEMENT_FUNDING :
			value = getElementFundingCode(line);
			if (value == null || existingFundingCodes.contains(value)) {
				return;
			} else {
				existingFundingCodes.add(value);
			}
			break;
		case ELEMENT_SEC_SERIAL_CODE :
			secSerialCount++;
			value = getElementGeneric(line);
			if (existingSecSerialCodes.size() == 0 || !existingSecSerialCodes.contains(value)) {
				existingSecSerialCodes.add(value);
				secSerials.put(secSerialCount, ++uniqueSecSerialCount);			
			} else {
				secSerials.put(secSerialCount, uniqueSecSerialCount);
				return;
			}
			break;
		default :
			//
		};
		
		if (element.contentEquals(ELEMENT_AUTHOR_A)) {
			return;
		}
		
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
		dcElementTemplates.put(ELEMENT_TYPE, "<dcvalue element=\"type\" qualifier=\"\">" + VALUE + "</dcvalue>");
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
		
		nrcanElementTemplates = new HashMap<String, String>();
		
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
		nrcanElementTemplates.put(ELEMENT_AREA_TEXT, "<dcvalue element=\"area\" qualifier=\"\">" + VALUE + "</dcvalue>");
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
					
		relationshipElements = new HashMap<String, Relationship>();
		
		relationshipElements.put(ELEMENT_SERIAL_CODE, new Relationship(RELATIONSHIP_SERIAL, ATTRIBUTE_SERIAL_CODE));
		relationshipElements.put(ELEMENT_JOURNAL_CODE, new Relationship(RELATIONSHIP_JOURNAL, ATTRIBUTE_JOURNAL_CODE));
		relationshipElements.put(ELEMENT_AUTHOR_A, new Relationship(RELATIONSHIP_AUTHOR, ATTRIBUTE_MIGRATION_ID));
		relationshipElements.put(ELEMENT_AUTHOR_M, new Relationship(RELATIONSHIP_MONOGRAPHIC_AUTHOR, ATTRIBUTE_MIGRATION_ID));
		relationshipElements.put(ELEMENT_PUBLISHER, new Relationship(RELATIONSHIP_PUBLISHER, ATTRIBUTE_PUB_MIGRATION_ID));
		relationshipElements.put(ELEMENT_CORP_AUTHOR_A, new Relationship(RELATIONSHIP_CORP_AUTHOR, ATTRIBUTE_CA_MIGRATION_ID));
		relationshipElements.put(ELEMENT_CORP_AUTHOR_M, new Relationship(RELATIONSHIP_MONOGRAPHIC_CORP_AUTHOR, ATTRIBUTE_CA_MIGRATION_ID));
		relationshipElements.put(ELEMENT_COUNTRY, new Relationship(RELATIONSHIP_COUNTRY, ATTRIBUTE_TITLE));
		relationshipElements.put(ELEMENT_COUNTRY_CANADA, new Relationship(RELATIONSHIP_COUNTRY, ATTRIBUTE_COUNTRY_CODE));
		relationshipElements.put(ELEMENT_PROVINCE, new Relationship(RELATIONSHIP_PROVINCE, ATTRIBUTE_PROVINCE_CODE));
		relationshipElements.put(ELEMENT_AREA, new Relationship(RELATIONSHIP_AREA, ATTRIBUTE_AREA_MIGRATION_ID));
		relationshipElements.put(ELEMENT_DIVISION, new Relationship(RELATIONSHIP_DIVISION, ATTRIBUTE_DIVISION_CODE));
		relationshipElements.put(ELEMENT_FUNDING, new Relationship(RELATIONSHIP_SPONSOR, ATTRIBUTE_SPONSOR_CODE));
		relationshipElements.put(ELEMENT_SEC_SERIAL_CODE, new Relationship(RELATIONSHIP_SEC_SERIAL, ATTRIBUTE_SERIAL_CODE));
		
		geospatialElementTemplates = new HashMap<String, String>();
		geospatialElementTemplates.put(ELEMENT_POLYGON_DEG, "<dcvalue element=\"polygon\" qualifier=\"degrees\">" + VALUE + "</dcvalue>");
		geospatialElementTemplates.put(ELEMENT_COVERAGE, "<dcvalue element=\"polygon\" qualifier=\"\">" + VALUE + "</dcvalue>");
		geospatialElementTemplates.put(ELEMENT_BBOX, "<dcvalue element=\"bbox\" qualifier=\"\">" + VALUE + "</dcvalue>");
		
		ignoredElements.add(ELEMENT_BIBLIOGRAPHIC_LEVEL);
		
	}
	
	private void printDateIssued() throws Exception {
		String template = dcElementTemplates.get(ELEMENT_DATE_ISSUED);
		if (StringUtils.isEmpty(dateIssued)) {
			System.out.println("GID: " + geoScanId + " - No Date Issued");
		}
		template = template.replace(VALUE, dateIssued);
		
		dublinCoreFileStream.println(template);
		
		if (!StringUtils.isEmpty(lastDateUpdated)) {
			template = dcElementTemplates.get(ELEMENT_DATE_UPDATED);

			template = template.replace(VALUE, lastDateUpdated);
			
			dublinCoreFileStream.println(template);
		}
		
	}
	
	private void printBBox() {	
		
		if (bBoxes.size() > 0) {
			String value = combineBBoxes();
			
			if (bBoxes.size() > 1) {
				for (String val : bBoxes) {
					String template = nrcanElementTemplates.get(ELEMENT_POLYGON_WENS);
	
					template = template.replace(VALUE, val);
					
					nrcanFileStream.println(template);
				}
			}
			
			String template = geospatialElementTemplates.get(ELEMENT_BBOX);

			template = template.replace(VALUE, value);
			
			geospatialFileStream.println(template);
		}
				
	}
	
	private String combineBBoxes() {
		if (bBoxes.size() == 1) {
			return bBoxes.get(0);
		} else {
			Float maxW = null;
			Float maxE = null;
			Float maxN = null;
			Float maxS = null;
			float bBoxW, bBoxE, bBoxN, bBoxS;
			
			for (String bBox : bBoxes) {
				try {
					bBox = bBox.substring(8);
					bBox = bBox.replace("(", "");
					bBox = bBox.replace(")", "");
					
					List<String> tokens = new ArrayList<String>();
					StringTokenizer tokenizer = new StringTokenizer(bBox, ",");
				    while (tokenizer.hasMoreElements()) {
				        tokens.add(tokenizer.nextToken());
				    }
				    
				    bBoxW = Float.parseFloat(tokens.get(0));
				    bBoxE = Float.parseFloat(tokens.get(1));
				    bBoxN = Float.parseFloat(tokens.get(2));
				    bBoxS = Float.parseFloat(tokens.get(3));
				    
				    if (maxW == null || bBoxW < maxW) {
				    	maxW = bBoxW;
				    }
				    if (maxE == null || bBoxE > maxE) {
				    	maxE = bBoxE;
				    }
				    if (maxN == null || bBoxN > maxN) {
				    	maxN = bBoxN;
				    }
				    if (maxS == null || bBoxS < maxS) {
				    	maxS = bBoxS;
				    }
				} catch (Exception e) {
					throw e;
				}
			}
			return "ENVELOPE(" + maxW + ", " + maxE + ", " + maxN + ", " + maxS + ")";
		}		
	}
	
	private String getElementGeneric(String line) {
		try {
			return line.substring(line.indexOf(">") + 1, line.substring(1).indexOf("<") + 1);
		} catch (Exception e) {
			System.out.println("GID: " + geoScanId + " - Line: " + line);
			return "";
		}		
	}
	
	@SuppressWarnings("deprecation")
	private String getElementGenericCapitalize(String line) {
		line = getElementGeneric(line);
		WordUtils.capitalize(line);
		return line;
	}
	
	private String getElementLanguageGeneric(String line) {
		int pos = line.indexOf("xml:lang");
		return line.substring(pos + 10, pos + 12);
	}
	
	private String getElementLanguage(String line) {
		line = getElementGeneric(line).trim();
		if (line.contentEquals("eng")) {
			line = "en";
		} else if (line.contentEquals("fre")) {
			line = "fr";
		}
		return line;
	}
	
	private String getElementIdentifier(String line) {
		int pos = line.indexOf("/");
		return line.substring(pos + 1, line.indexOf("</"));
	}
	
	private String getElementIdentifierQualifier(String line) {
		int pos = line.indexOf("info:");
		return line.substring(pos + 5, line.indexOf("/"));
	}
	
	private String getElementOpenAccess(String line) {
		line = getElementGeneric(line).trim();
		return line.substring(0, line.indexOf(" "));
	}
	
	private String getElementPolygonWENS(String line) {
		line = getElementGeneric(line);
		if (StringUtils.isEmpty(line) || line.charAt(1) == ',') {
			return "";
		}
		return "ENVELOPE" + line;
	}
	
	private String getElementDivision(String line) {
		int pos = line.indexOf("code");
		return line.substring(pos + 5, line.indexOf("</"));
	}
	
	private String getElementRecordCreated(String line) {
		int pos = line.indexOf("<date>");
		line = line.substring(pos + 6, line.indexOf("</date"));
		return "Record added to GEOScan on " + line;
	}
	
	private String getElementRecordUpdated(String line) {
		line = getElementGeneric(line);
		return "Contents updated on " + line;
	}
	
	private String getElementThesis(String line) {
		int pos = line.indexOf("<degree>");
		String degree = line.substring(pos + 8, line.indexOf("</degree"));
		pos = line.indexOf("<univ>");
		String univ = line.substring(pos + 6, line.indexOf("</univ"));
		pos = line.indexOf("<city>");
		String city = line.substring(pos + 6, line.indexOf("</city"));
		pos = line.indexOf("<country>");
		String country = line.substring(pos + 9, line.indexOf("</country"));
		return degree + " - " + univ + " - " + city + " - " + country;
	}
	
	private String getElementDownload(String line) {
		int pos = line.indexOf("<service>");
		String service = line.substring(pos + 8, line.indexOf("</service"));
		pos = line.indexOf("<filename>");
		if (pos < 0) {
			return "";
		}
		String filename = line.substring(pos + 9, line.indexOf("</filename"));
		return filename;
	}
	
	private String getElementArchivalFile(String line) {
		int pos = line.indexOf("<name>");
		String name = line.substring(pos + 5, line.indexOf("</name"));
		pos = line.indexOf("<folder>");
		String folder = line.substring(pos + 7, line.indexOf("</folder"));
		pos = line.indexOf("<size>");
		String size = line.substring(pos + 5, line.indexOf("</size"));
		return folder + " - " + name + " - " + size;
	}
	
	private String getElementDateRecordTouched(String line) {
		int pos = line.indexOf("<name>");
		String name = line.substring(pos + 6, line.indexOf("</name"));
		pos = line.indexOf("<date>");
		String date = line.substring(pos + 6, line.indexOf("</date"));
		return date + " - " + name;
	}
	
	private String getElementMap(String line) {
		int pos = line.indexOf("<general>");
		String type = line.substring(pos + 8, line.indexOf("</general"));
		pos = line.indexOf("<scale>");
		if (pos > 0) {
			String scale = line.substring(pos + 7, line.indexOf("</scale"));
			return type + " - " + scale;
		}
		return type;
	}
	
	private String getElementFundingCode(String line) {
		int pos = line.indexOf("Code>");
		if (pos < 0) {
			return null;
		}
		String code = line.substring(pos + 5, line.indexOf("</Project Code"));
		return code;
	}
	
	private String getElementFundingLegacy(String line) {
		try {
			if (line.contentEquals("<Funding></Funding>")) {
				return null;
			}
			
			int pos = line.indexOf("Program En>");
			String lang = "En";
			if (pos < 0) {
				pos = line.indexOf("Program Fr>");
				lang = "Fr";
			}
			String program = line.substring(pos + 10, line.indexOf("</Program " + lang));
			
			pos = line.indexOf("Project En>");
			if (pos < 0) {
				pos = line.indexOf("Project Fr>");
			}
			String project = "";
			if (pos > 0) {
				project = line.substring(pos + 10, line.indexOf("</Project " + lang));
			}
			
			pos = line.indexOf("URL En>");
			if (pos < 0) {
				pos = line.indexOf("URL Fr>");
			}
			String url = "";
			if (pos > 0) {
				project = line.substring(pos + 10, line.indexOf("</URL " + lang));
			}
			return program + " - " + project + " - " + url;
		} catch (Exception e) {
			System.out.println("GID: " + geoScanId + " - FundingLegacy: " + line);
			return null;
		}
	}
	
	private String getElementFundingLegacyLang(String line) {
		int pos = line.indexOf("Program En>");
		if (pos < 0) {
			return "fr";
		}
		return "en";
	}
	
	private String getElementRelationUrl(String line) {
		int pos = line.indexOf("<label>");
		String label = line.substring(pos + 7, line.indexOf("</label"));
		pos = line.indexOf("<URL>");
		String url = line.substring(pos + 5, line.indexOf("</URL"));
		return label + " - " + url;
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
				orcId = line.substring(pos + 45, line.indexOf("</ORCID"));
			} catch (Exception e) {
				//
			}
			return lastName.toUpperCase() + "_" + firstName.toUpperCase() + "_" + deptId + "_" + orcId;
		} catch (Exception e) {
			return null;
		}		
	}
	
	private void handleRelationPhotoElement(String line) {
		try {
			List<String> tokens = getTokensWithCollection(line);
			for (String token : tokens) {
				if (token.contains(":")) {
					String start = token.substring(0, token.indexOf(":"));
					String end = token.substring(token.indexOf(":") + 1);
					
					int startNum = Integer.parseInt(start.substring(start.indexOf("-") + 1));
					int endNum = Integer.parseInt(end.substring(end.indexOf("-") + 1));
					
					String value = "";
					for (int i=startNum;i<=endNum;i++) {
						
							value = start.substring(0, start.indexOf("-"));
							value = value + "-" + i;
							printRelationPhotoElement(value);
						
					}
					
				} else {
					printRelationPhotoElement(token);
				}
			}
		} catch (Exception e) {
			System.out.println("GID: " + geoScanId + " - Relation Photo: " + line);
		}
	}
	
	private void printRelationPhotoElement(String value) {
		String template = nrcanElementTemplates.get(ELEMENT_RELATION_PHOTO);

		template = template.replace(VALUE, value);
		
		nrcanFileStream.println(template);
	}
	
	public List<String> getTokensWithCollection(String str) {
	    return Collections.list(new StringTokenizer(str, ";")).stream()
	      .map(token -> (String) token)
	      .collect(Collectors.toList());
	}
	
	private String getRelationElement(String value) throws Exception {
		Integer val = Integer.parseInt(value.substring(0, value.indexOf("-")));
		String element = null;
		switch (val) {
			case 1 :
				element = ELEMENT_RELATION_REPLACES;
				break;
			case 2 :
				element = ELEMENT_RELATION_ACCOMPANIES;
				break;
			case 3 :
				element = ELEMENT_RELATION_ISREPLACEDBY;
				break;
			case 4 :
				element = ELEMENT_RELATION_ISRELATEDTO;
				break;
			case 5 :
				element = ELEMENT_RELATION_ISPARTOF;
				break;
			case 6 :
				element = ELEMENT_RELATION_ISACCOMPANIEDBY;
				break;
			case 7 :
				element = ELEMENT_RELATION_CONTAINS;
				break;
			case 8 :
				element = ELEMENT_RELATION_ISENLARGEDFROM;
				break;
			case 9 :
				element = ELEMENT_RELATION_ISREDUCEDFROM;
				break;
			case 10 :
				element = ELEMENT_RELATION_ISTRANSLATIONOF;
				break;
			case 11 :
				element = ELEMENT_RELATION_ISREPRINTEDFROM;
				break;
			case 12 :
				element = ELEMENT_RELATION_TBD;
				break;
			case 13 :
				element = ELEMENT_RELATION_ISREPRINTEDIN;
				break;
			default :
				throw new Exception("No match");
		}
		
		return element;
	}
	
	private void handleMeetingDate(String value) throws Exception {
		try {	
			LocalDate startDate = null;
			LocalDate endDate = null;
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d uuuu");
			DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MMMM d, uuuu");
			
			if (value.contains("-")) {
				long spaceCount = value.chars().filter(ch -> ch == ' ').count();
				value = value.replace(",", "");		
				
				// July 8-12, 2002
				if (spaceCount == 2) {
					
					String endDateString = value.substring(value.indexOf("-") + 1);
					endDateString = value.substring(0, value.indexOf(" ")) + " " + endDateString;
					endDate = LocalDate.parse(endDateString, formatter);
					String startDateString = value.substring(0, value.indexOf("-")) + " " + endDate.getYear();
					startDate = LocalDate.parse(startDateString, formatter);
									
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
			System.out.println("GID: " + geoScanId + " - Meeting Date: " + value);
		}
	}
	
	private boolean containsNumber(String value) {
		// Test to see if this contains a numeric value, otherwise junk
		char[] ch = value.toCharArray();
		for(char c : ch) {
			if(Character.isDigit(c)){
				return true;
			}
		}
		return false;
	}
	
	private String replaceLTGT(String value) {
		value = value.replace("&amp;lt;", "&lt;");
		value = value.replace("&amp;gt;", "&gt;");
		return value;
	}
	
	private String replaceAmp(String value) {
		value = value.replace("&","&amp;");
		return value;
	}
}
