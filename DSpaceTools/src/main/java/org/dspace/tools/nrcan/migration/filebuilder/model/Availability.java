package org.dspace.tools.nrcan.migration.filebuilder.model;

import lombok.Getter;
import lombok.Setter;

public class Availability {

	@Getter
	@Setter
	String pdf_email;
	
	@Getter
	@Setter
	String pdf_download;
	
	@Getter
	@Setter
	String print;
	
	@Getter
	@Setter
	String epub;
}
