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

package org.glom.web.client.ui.cell;

import org.glom.web.client.place.DetailsPlace;
import org.glom.web.client.ui.ListView.Presenter;
import org.glom.web.client.ui.list.ListTable;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell renderer for {@link ListTable} open buttons.
 * 
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class OpenButtonCell extends ButtonCell {
	private String documentID;
	private String tableName;
	private Presenter presenter;

	public OpenButtonCell(String documentID, String tableName, Presenter presenter) {
		this.documentID = documentID;
		this.tableName = tableName;
		this.presenter = presenter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.cell.client.ButtonCell#onEnterKeyDown(com.google.gwt.cell.client.Cell.Context,
	 * com.google.gwt.dom.client.Element, java.lang.String, com.google.gwt.dom.client.NativeEvent,
	 * com.google.gwt.cell.client.ValueUpdater)
	 */
	@Override
	protected void onEnterKeyDown(Context context, Element parent, String value, NativeEvent event,
			ValueUpdater<String> valueUpdater) {
		super.onEnterKeyDown(context, parent, value, event, valueUpdater);
		presenter.goTo(new DetailsPlace(documentID, tableName, (String) context.getKey()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.cell.client.ButtonCell#render(com.google.gwt.cell.client.Cell.Context,
	 * com.google.gwt.safehtml.shared.SafeHtml, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
	 */
	@Override
	public void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
		if (data == null)
			// The value is from an empty row.
			sb.appendHtmlConstant("&nbsp;");
		else
			super.render(context, data, sb);
	}

}
