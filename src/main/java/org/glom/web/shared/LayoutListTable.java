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
public class LayoutListTable implements Serializable {
	private String name;
	private String title;
	private ColumnInfo[] columns;
	private int numRows;
	private String tableName;

	public LayoutListTable() {
	}

	public LayoutListTable(String name, String title, ColumnInfo[] columns, int numRows) {
		this.name = name;
		this.title = title;
		this.columns = columns;
		this.numRows = numRows;
	}

	// @formatter:off
	public String getName() { return name;}
	public void setName(String name) { this.name = name;}
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public ColumnInfo[] getColumns() { return columns; }
	public void setColumns(ColumnInfo[] columns) { this.columns = columns; }
	public int getNumRows() { return numRows; }
	public void setNumRows(int numRows) { this.numRows = numRows; }
	public String getTableName() { return tableName; }
	public void setTableName(String tableName) { this.tableName = tableName; }
	// @formatter:on
}