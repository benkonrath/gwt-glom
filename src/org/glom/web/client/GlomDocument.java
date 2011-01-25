package org.glom.web.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GlomDocument implements Serializable {
	private String title;
	private int defaultTable;
	private GlomTable[] tables;

	public GlomDocument() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	public int getDefaultTableIndex() {
		return defaultTable;
	}

	public void setDefaultTableIndex(int defaultTable) {
		this.defaultTable = defaultTable;
	}

	public void setTables(GlomTable[] tables) {
		this.tables = tables;
	}

	public GlomTable[] getTables() {
		return tables;
	}
}
