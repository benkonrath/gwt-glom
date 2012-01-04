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

import org.glom.web.shared.layout.LayoutItemField.GlomFieldType;

/**
 * This specialization of DataItem can hold a primary key item.
 */
@SuppressWarnings("serial")
public class TypedDataItem extends DataItem {
	private boolean empty = true;
	private GlomFieldType type = GlomFieldType.TYPE_INVALID;
	private String unknown = null;

	public TypedDataItem() {
	}

	public boolean isEmpty() {
		return empty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.DataItem#setBoolean(boolean)
	 */
	@Override
	public void setBoolean(boolean bool) {
		this.empty = false;
		this.type = GlomFieldType.TYPE_BOOLEAN;
		super.setBoolean(bool);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.DataItem#setNumber(double)
	 */
	@Override
	public void setNumber(double number) {
		this.empty = false;
		this.type = GlomFieldType.TYPE_NUMERIC;
		super.setNumber(number);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.DataItem#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		this.empty = false;
		this.type = GlomFieldType.TYPE_TEXT;
		super.setText(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.shared.DataItem#setText(java.lang.String)
	 */
	public void setUnknown(String value) {
		this.empty = false;
		this.type = GlomFieldType.TYPE_INVALID;
		this.unknown = value;
	}

	public String getUnknown() {
		return unknown;
	}

	public GlomFieldType getType() {
		return type;
	}

}
