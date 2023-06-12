package org.dspace.tools.nrcan.migration.filebuilder.model;

import lombok.Getter;
import lombok.Setter;

public class Stats {

	@Getter
	@Setter
	String views;
	
	@Getter
	@Setter
	String downloads;
	
	@Getter
	@Setter
	String email;
	
	@Getter
	@Setter
	String orders;
}
