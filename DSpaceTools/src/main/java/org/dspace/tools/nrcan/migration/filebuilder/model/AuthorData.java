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
	String orcid;
	
	@Getter
	@Setter
	String nrn_userid;
	
	@Getter
	@Setter
	Link[] links;
}
