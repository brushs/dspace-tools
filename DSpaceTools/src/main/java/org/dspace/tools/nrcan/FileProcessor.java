package org.dspace.tools.nrcan;

import java.io.IOException;

public interface FileProcessor {
	public void process() throws IOException;
	
	public void close();
}
