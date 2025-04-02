package org.dspace.tools.nrcan.migration.filebuilder.model;

import lombok.Getter;
import lombok.Setter;

public class Author {

	@Getter
	@Setter
	String uuid;
	
	@Getter
	@Setter
	String name;
	
	@Getter
	@Setter
	String dpsid;
}
