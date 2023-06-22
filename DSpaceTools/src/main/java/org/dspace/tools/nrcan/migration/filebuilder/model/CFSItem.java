package org.dspace.tools.nrcan.migration.filebuilder.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	String issn;
	
	@Getter
	@Setter
	String isbn;
	
	@Getter
	@Setter
	String publication_name;
	
	@Getter
	@Setter
	String volume;
	
	@Getter
	@Setter
	String issue;
	
	@Getter
	@Setter
	String page_first;
	
	@Getter
	@Setter
	String page_last;
	
	@Getter
	@Setter
	String goc_fo_num;
	
	@Getter
	@Setter
	String fiscal_year;
	
	@Getter
	@Setter
	String release_date;
	
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
	@JsonProperty(value="abstract")
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
