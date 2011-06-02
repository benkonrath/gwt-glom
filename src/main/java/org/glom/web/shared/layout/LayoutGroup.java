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
 * 
 */
@SuppressWarnings("serial")
public class LayoutGroup extends LayoutItem implements Serializable {

	private int columnCount = 1;
	private ArrayList<LayoutItem> items = new ArrayList<LayoutItem>();

	/**
	 * Add the item to the end of the list.
	 * 
	 * @param item
	 *            The item to add.
	 */
	public final void addItem(LayoutItem item) {
		items.add(item);
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

}
