package org.glom.web.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GlomTable implements Serializable {
	private String name;
	
	public GlomTable() {
	}
	
	public GlomTable(String name) {
		this.setName(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
