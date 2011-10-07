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

package org.glom.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class TableChangeEvent extends GwtEvent<TableChangeEventHandler> {
	public static Type<TableChangeEventHandler> TYPE = new Type<TableChangeEventHandler>();
	private final String newTableName;

	public TableChangeEvent(String newTableName) {
		this.newTableName = newTableName;
	}

	public String getNewTableName() {
		return newTableName;
	}

	@Override
	public Type<TableChangeEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(TableChangeEventHandler handler) {
		handler.onTableChange(this);
	}

	@Override
	public String toDebugString() {
		String name = this.getClass().getName();
		name = name.substring(name.lastIndexOf(".") + 1);
		return "event: " + name + ": " + newTableName;
	}
}
