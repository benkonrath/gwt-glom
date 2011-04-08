/*
 * Copyright (C) 2011 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

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
