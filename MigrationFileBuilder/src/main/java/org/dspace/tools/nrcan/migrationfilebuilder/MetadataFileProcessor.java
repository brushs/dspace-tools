package org.dspace.tools.nrcan.migrationfilebuilder;

import java.io.IOException;

public interface MetadataFileProcessor {
	public void process() throws IOException;
	
	public void close();
}
