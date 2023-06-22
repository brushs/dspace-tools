package org.dspace.tools.nrcan.migration.filebuilder.model;

import lombok.Getter;
import lombok.Setter;

public class Meta {

	@Getter
	@Setter
	String total;
	
	@Getter
	@Setter
	String returned;
	
	@Getter
	@Setter
	String page;
	
	@Getter
	@Setter
	String pages;
	
	@Getter
	@Setter
	Pagination[] pagination;
	
}
