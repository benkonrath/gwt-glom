package org.glom.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ColumnInfo implements Serializable {
	public enum HorizontalAlignment {
		HORIZONTAL_ALIGNMENT_AUTO, HORIZONTAL_ALIGNMENT_LEFT, HORIZONTAL_ALIGNMENT_RIGHT;
	}

	private String header;
	private HorizontalAlignment alignment;

	public ColumnInfo() {
	}

	public ColumnInfo(String header, HorizontalAlignment alignment) {
		this.header = header;
		this.alignment = alignment;
	}

	// @formatter:off
	public String getHeader() { return header;}
	public void setHeader(String header) { this.header = header; }
	public HorizontalAlignment getAlignment() { return alignment;}
	public void setAlignment(HorizontalAlignment alignment) { this.alignment = alignment; }
	// @formatter:on
}
