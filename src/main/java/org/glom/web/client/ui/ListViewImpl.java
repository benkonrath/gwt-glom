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

package org.glom.web.client.ui;

import org.glom.web.client.ui.list.ListViewTable;
import org.glom.web.shared.layout.LayoutGroup;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class ListViewImpl extends Composite implements ListView {

	final private FlowPanel mainPanel = new FlowPanel();
	private Presenter presenter;

	public ListViewImpl() {
		initWidget(mainPanel);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setCellTable(final String documentID, LayoutGroup layoutGroup) {
		// This is not really in the MVP style because we're creating a new ListTable (really just a configured
		// CellTable) for every document and table name change. The issue with creating a re-usable CellTable with
		// methods like setColumnTitles() and setNumRows() is that the column objects (new Column<DataItem[],
		// DataItem>(new GlomTextCell())) aren't destroyed when the column is removed from the CellTable and
		// IndexOutOfBounds exceptions are encountered with invalid array indexes trying access the data in this line:
		// return object[j]. There's probably a workaround that could be done to fix this but I'm leaving it until
		// there's a reason to fix it (performance, ease of testing, alternate implementation or otherwise).
		// Note: This comment refers to code that is now in the ListTable class.

		mainPanel.clear();

		ListViewTable listViewTable = new ListViewTable(documentID, layoutGroup);
		listViewTable.addOpenButtonColumnn(presenter, "Details");

		mainPanel.add(listViewTable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.ListView#clear()
	 */
	@Override
	public void clear() {
		mainPanel.clear();
	}

}
