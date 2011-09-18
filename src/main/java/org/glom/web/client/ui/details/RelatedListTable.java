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

package org.glom.web.client.ui.details;

import java.util.ArrayList;

import org.glom.web.client.OnlineGlomServiceAsync;
import org.glom.web.client.ui.list.ListTable;
import org.glom.web.shared.GlomField;
import org.glom.web.shared.layout.LayoutGroup;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class RelatedListTable extends ListTable {

	private static final int NUM_VISIBLE_ROWS = 5;

	private String foreignKeyValue;
	private String relationshipName;

	public RelatedListTable(String documentID, LayoutGroup layoutGroup, String foreignKeyValue) {
		super(documentID);

		// These variables need to be set before the createCellTable() method is called so that the data provider can
		// use them.
		this.foreignKeyValue = foreignKeyValue;
		this.relationshipName = layoutGroup.getName();

		createCellTable(layoutGroup, NUM_VISIBLE_ROWS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.list.ListTable#getDataProvider()
	 */
	@Override
	protected AsyncDataProvider<GlomField[]> getDataProvider() {
		AsyncDataProvider<GlomField[]> dataProvider = new AsyncDataProvider<GlomField[]>() {

			@Override
			@SuppressWarnings("unchecked")
			protected void onRangeChanged(HasData<GlomField[]> display) {
				// setup the callback object
				final Range range = display.getVisibleRange();
				final int start = range.getStart();
				AsyncCallback<ArrayList<GlomField[]>> callback = new AsyncCallback<ArrayList<GlomField[]>>() {
					public void onFailure(Throwable caught) {
						// FIXME: need to deal with failure
						System.out.println("AsyncCallback Failed: OnlineGlomService.getTableData()");
					}

					public void onSuccess(ArrayList<GlomField[]> result) {
						updateRowData(start, result);
					}
				};

				// get data from the server
				ColumnSortList colSortList = cellTable.getColumnSortList();
				if (colSortList.size() > 0) {
					// ColumnSortEvent has been requested by the user
					ColumnSortInfo info = colSortList.get(0);

					OnlineGlomServiceAsync.Util.getInstance().getSortedRelatedListData(documentID, tableName,
							relationshipName, foreignKeyValue, start, range.getLength(),
							cellTable.getColumnIndex((Column<GlomField[], ?>) info.getColumn()), info.isAscending(),
							callback);

				} else {
					OnlineGlomServiceAsync.Util.getInstance().getRelatedListData(documentID, tableName,
							relationshipName, foreignKeyValue, start, range.getLength(), callback);

				}

			}
		};

		return dataProvider;
	}

}
