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
import java.util.ArrayList;

/**
 * DTO to hold basic information about tables of a Glom document.
 * 
 * @author Ben Konrath <ben@bagu.org>
 */
@SuppressWarnings("serial")
public class DocumentInfo implements Serializable {
	private String title;
	// could consider a LinkedHashMap if we need to support adding or removing tables
	// order must be consistent between these two arrays
	private ArrayList<String> tableNames;
	private ArrayList<String> tableTitles;
	private int defaultTableIndex;

	public DocumentInfo() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ArrayList<String> getTableNames() {
		return tableNames;
	}

	public void setTableNames(ArrayList<String> tableNames) {
		this.tableNames = tableNames;
	}

	public ArrayList<String> getTableTitles() {
		return tableTitles;
	}

	public void setTableTitles(ArrayList<String> tableTitles) {
		this.tableTitles = tableTitles;
	}

	public int getDefaultTableIndex() {
		return defaultTableIndex;
	}

	public void setDefaultTableIndex(int defaultTable) {
		this.defaultTableIndex = defaultTable;
	}

}
