package org.glom.web.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ColumnInfo implements Serializable {
	// There's no way to make the org.glom.libglom.FieldFormatting.HorizontalAlignment enum available to GWT so it's
	// been copied from the FieldFormatting class.
	public enum HorizontalAlignment {
		HORIZONTAL_ALIGNMENT_AUTO, HORIZONTAL_ALIGNMENT_LEFT, HORIZONTAL_ALIGNMENT_RIGHT;
	}

	// There's no way to make the org.glom.libglom.Field.glom_field_type enum available to GWT so it's been copied from
	// the Field class.
	public enum GlomFieldType {
		TYPE_INVALID, TYPE_NUMERIC, TYPE_TEXT, TYPE_DATE, TYPE_TIME, TYPE_BOOLEAN, TYPE_IMAGE;
	}

	private String header;
	private HorizontalAlignment alignment;
	private GlomFieldType type;

	public ColumnInfo() {
	}

	public ColumnInfo(String header, HorizontalAlignment alignment, GlomFieldType type) {
		this.header = header;
		this.alignment = alignment;
		this.type = type;
	}

	// @formatter:off
	public String getHeader() { return header;}
	public void setHeader(String header) { this.header = header; }
	public HorizontalAlignment getAlignment() { return alignment;}
	public void setAlignment(HorizontalAlignment alignment) { this.alignment = alignment; }
	public GlomFieldType getType() { return type; }
	public void setType(GlomFieldType type) { this.type = type; }
	// @formatter:on
}
