package org.glom.web.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class GlomTable implements Serializable {
	private String name;
	private String title;
	public GlomTable() {
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
}