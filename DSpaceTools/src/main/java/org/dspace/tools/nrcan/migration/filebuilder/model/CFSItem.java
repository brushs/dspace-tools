package org.dspace.tools.nrcan.migration.filebuilder.model;

import lombok.Getter;
import lombok.Setter;

public class CFSItem {

	@Getter
	@Setter
	String uid;
	
	@Getter
	@Setter
	String year;
	
	@Getter
	@Setter
	String title;
	
	@Getter
	@Setter
	String citation_author;
	
	@Getter
	@Setter
	String citation_title;
	
	@Getter
	@Setter
	String keywords;
	
	@Getter
	@Setter
	String doi;
	
	@Getter
	@Setter
	Availability availability;
	
	@Getter
	@Setter
	String date_added;
	
	@Getter
	@Setter
	String date_updated;
	
	@Getter
	@Setter
	Link[] links;
	
	@Getter
	@Setter
	LangValue itemAbstract;
	
	@Getter
	@Setter
	LangValue pls;
	
	@Getter
	@Setter
	LangValue language;
	
	@Getter
	@Setter
	Stats stats;
	
	@Getter
	@Setter
	Programs programs;
	
	@Getter
	@Setter
	Centre centre;
	
	@Getter
	@Setter
	Authors authors;
	
	@Getter
	@Setter
	Subjects subjects;
	
	@Getter
	@Setter
	Type type;
	
	@Getter
	@Setter
	Series series;
	
	@Getter
	@Setter
	Outputs outputs;
}
