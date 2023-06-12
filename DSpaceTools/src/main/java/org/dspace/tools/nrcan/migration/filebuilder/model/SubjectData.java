package org.dspace.tools.nrcan.migration.filebuilder.model;

import lombok.Getter;
import lombok.Setter;

public class SubjectData {

	@Getter
	@Setter
	String uid;
	
	@Getter
	@Setter
	LangValue subject;
	
	@Getter
	@Setter
	Link[] links;
}
