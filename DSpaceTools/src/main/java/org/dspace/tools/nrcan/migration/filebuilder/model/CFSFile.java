package org.dspace.tools.nrcan.migration.filebuilder.model;

import lombok.Getter;
import lombok.Setter;

public class CFSFile {

	@Getter
	@Setter
	CFSItem[] data;
	
	@Getter
	@Setter
	Meta meta;
}
