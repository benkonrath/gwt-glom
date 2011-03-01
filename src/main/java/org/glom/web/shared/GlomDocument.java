/*
 * Copyright (C) 2010, 2011 Openismus GmbH
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
public class GlomDocument implements Serializable {
	private String title;
	// could consider a LinkedHashMap if we need to support adding or removing tables
	// order must be consistent between these two arrays
	private String[] tableNames;
	private String[] tableTitles;
	private int defaultTableIndex;

	public GlomDocument() {
	}

	public GlomDocument(String title, String[] tableNames, String[] tableTitles, int defaultTableIndex) {
		this.title = title;
		this.tableNames = tableNames;
		this.tableTitles = tableTitles;
		this.defaultTableIndex = defaultTableIndex;
	}

	// @formatter:off
	public String getTitle() { return title; }
	public void setTitle(String name) { this.title = name; }
	public int getDefaultTableIndex() { return defaultTableIndex; }
	public void setDefaultTableIndex(int defaultTable) { this.defaultTableIndex = defaultTable; }
	public String[] getTableNames() { return tableNames; }
	public void setTableNames(String[] tableNames) { this.tableNames = tableNames; }
	public void setTableTitles(String[] tableTitles) { this.tableTitles = tableTitles; }
	public String[] getTableTitles() { return tableTitles; }
	// @formatter:on
}
