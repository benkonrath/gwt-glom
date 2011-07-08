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

package org.glom.web.shared.layout;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Ben Konrath <ben@bagu.org>
 */
@SuppressWarnings("serial")
public class LayoutGroup extends LayoutItem implements Serializable {

	// TODO Investigate if it's worthwhile creating ListLayoutGroup and DetailsLayoutGroup subclasses.
	// columnCount is only used for the details layout
	private int columnCount = 1;
	// expectedResultSize and tableName are only used for the list layout
	private int expectedResultSize = -1;
	private String tableName = "";

	// This is the primary key index of the LayoutFieldVector that is used for getting the SQL query. It's being used
	// here to avoid having to set an isPrimaryKey boolean with every LayoutItemField. This also has the advantage of
	// not having to iterate through all of the LayoutItemFields to find the primary key index on the client side.
	private int primaryKeyIndex = -1;

	// indicates if the primary key is hidden and has been added to the end of the LayoutListFields list and the
	// database data list (GlomField).
	private boolean hiddenPrimaryKey = false;

	private ArrayList<LayoutItem> items = new ArrayList<LayoutItem>();

	/**
	 * Add the item to the end of the list.
	 * 
	 * @param item
	 *            The item to add.
	 */
	public final boolean addItem(LayoutItem item) {
		return items.add(item);
	}

	public final ArrayList<LayoutItem> getItems() {
		return items;
	}

	public final int getColumnCount() {
		return columnCount;
	}

	public final void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public int getExpectedResultSize() {
		return expectedResultSize;
	}

	public void setExpectedResultSize(int expectedResultSize) {
		this.expectedResultSize = expectedResultSize;
	}

	public void setTableName(String defaultTableName) {
		this.tableName = defaultTableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setPrimaryKeyIndex(int primaryKeyIndex) {
		this.primaryKeyIndex = primaryKeyIndex;
	}

	public int getPrimaryKeyIndex() {
		return primaryKeyIndex;
	}

	public void setHiddenPrimaryKey(boolean hiddenPrimaryKey) {
		this.hiddenPrimaryKey = hiddenPrimaryKey;
	}

	public boolean hasHiddenPrimaryKey() {
		return hiddenPrimaryKey;
	}

}