package org.dspace.tools.nrcan.migrationfilebuilder;

import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

public class GEOScanDataProcessor {

	private final static String OPT_POOL_CODE = "poolCode";
	private final static String OPT_EVENT_CODE = "event";
	private final static String OPT_RACE_NUMBER = "raceNumber";
	private final static String OPT_SOURCE_CODE = "sourceCode";
	private final static String OPT_MESSAGE_TYPE = "messageType";
	private final static String OPT_CUSTOMER_CODE = "customerCode";
	
	private String eventCode;
	private String poolCodeStr;
	private String poolCodes[];
	private Integer raceNumber;
	private String sourceCode;
	private String messageType;
	private String customerCode;
	
	public GEOScanDataProcessor(CommandLine cmd) {
		eventCode = cmd.getOptionValue(OPT_EVENT_CODE);
		eventCode = StringUtils.isEmpty(eventCode) ? null : eventCode;
		sourceCode = cmd.getOptionValue(OPT_SOURCE_CODE);
		sourceCode = StringUtils.isEmpty(sourceCode) ? null : sourceCode;
		customerCode = cmd.getOptionValue(OPT_CUSTOMER_CODE);
		customerCode = StringUtils.isEmpty(customerCode) ? null : customerCode;
		
		String messageTypeStr = cmd.getOptionValue(OPT_MESSAGE_TYPE);
		if(!StringUtils.isEmpty(messageTypeStr)){
			messageType = messageTypeStr;
		}
		
		String raceStr = cmd.getOptionValue(OPT_RACE_NUMBER);
		if(!StringUtils.isEmpty(raceStr)){
			raceNumber = Integer.parseInt(raceStr);
		}
		
		poolCodeStr = cmd.getOptionValue(OPT_POOL_CODE);
		if(!StringUtils.isEmpty(poolCodeStr)) {
			StringTokenizer tokenizer = new StringTokenizer(poolCodeStr, ",");
			poolCodes = new String[tokenizer.countTokens()];
			int i = 0;
			while (tokenizer.hasMoreTokens()) {
				poolCodes[i++] = tokenizer.nextToken();
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public static void appendClioptions(Options options) {
		options.addOption(
				OptionBuilder.withArgName("EVENT-CODE")
				.withDescription("Event Code")
				.hasArg()
				.create(OPT_EVENT_CODE));
		
		options.addOption(
				OptionBuilder.withArgName("SOURCE-CODE")
				.withDescription("SOURCE CODE")
				.hasArg()
				.create(OPT_SOURCE_CODE));
		
		options.addOption(
				OptionBuilder.withArgName("RACE-NUMBER")
				.withDescription("Race Number")
				.hasArg()
				.create(OPT_RACE_NUMBER));
		options.addOption(
				OptionBuilder.withArgName("POOL-CODE")
				.withDescription("ITSP Pool Code")
				.hasArg()
				.create(OPT_POOL_CODE));
		
		options.addOption(
				OptionBuilder.withArgName("DATA-TYPE")
				.withDescription("Data Type")
				.hasArg()
				.create(OPT_MESSAGE_TYPE));
		
		options.addOption(
				OptionBuilder.withArgName("CUSTOMER-TYPE")
				.withDescription("Customer Type")
				.hasArg()
				.create(OPT_CUSTOMER_CODE));
	}
	
	public boolean passes(String record) {
		boolean success = true;
		
		if (eventCode != null) {
			String eventSearchStr = "Event=\"" + eventCode + "\"";
			if (!record.contains(eventSearchStr)) {
				success = false;
					}
			}
		
		if (raceNumber != null){
			String raceSearchStr = "Race=\"" + raceNumber + "\"";
			if (!record.contains(raceSearchStr)) {
				success = false;
					}
			}
		
		if (messageType != null) {
			String messageTypeSearchStr = "|<" + messageType;
			if (!record.contains(messageTypeSearchStr)){
				success = false;
			}
		}
		
		if (poolCodes != null) {
			boolean isOneOfThesePools = false;
			for (String poolCode : Arrays.asList(poolCodes)) {
				String poolSearchStr = "Pool=\"" + poolCode +"\"";
				if (record.contains(poolSearchStr)){
					isOneOfThesePools = true;
				}
			}
			if (!isOneOfThesePools) {
				success = false;
			}
		}
		
		if (sourceCode != null) {
			String sourceSearchStr = "Source=\"" + sourceCode +"\"";
			if (!record.contains(sourceSearchStr)){
				success = false;
			}
		}
		
		if (customerCode != null) {
			String customerSearchStr = "Customer=\"" + customerCode +"\"";
			if (!record.contains(customerSearchStr)){
				success = false;
			}
		}
		
		if (record.contains("<Connect")){
				success = true;
		}
		
		return success;
	
	}

	public final String[] getPoolCodes() {
		return poolCodes;
	}

	public final void setPoolCodes(String[] poolCodes) {
		this.poolCodes = poolCodes;
	}
	
	public final String getEventCode() {
		return eventCode;
	}

	public final void setEventCode(String eventCode) {
		this.eventCode = eventCode;
	}

	public final Integer getRaceNumber() {
		return raceNumber;
	}

	public final void setRaceNumber(Integer raceNumber) {
		this.raceNumber = raceNumber;
	}

	public final String getSourceCode() {
		return sourceCode;
	}

	public final void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public final String getMessageType() {
		return messageType;
	}

	public final void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	public final String getCustomerCode(String customerCode) {
		return customerCode;
	}
	
	public final void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

}
