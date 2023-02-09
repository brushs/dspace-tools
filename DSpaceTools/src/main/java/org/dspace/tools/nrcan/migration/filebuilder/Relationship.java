package org.dspace.tools.nrcan.migration.filebuilder;

public class Relationship {

	private String name;
	private String attribute;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public Relationship(String name, String attribute) {
		this.name = name;
		this.attribute = attribute;
	}
}
