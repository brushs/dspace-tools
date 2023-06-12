package org.dspace.tools.nrcan.migration.filebuilder.model;

import lombok.Getter;
import lombok.Setter;

public class AuthorData {

	@Getter
	@Setter
	String uid;
	
	@Getter
	@Setter
	String name;
	
	@Getter
	@Setter
	Link[] links;
}
