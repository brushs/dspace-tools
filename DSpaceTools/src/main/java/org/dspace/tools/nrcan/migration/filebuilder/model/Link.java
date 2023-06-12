package org.dspace.tools.nrcan.migration.filebuilder.model;

import lombok.Getter;
import lombok.Setter;

public class Link {

	@Getter
	@Setter
	String rel;
	
	@Getter
	@Setter
	String uri;
	
	@Getter
	@Setter
	String lang;
}
