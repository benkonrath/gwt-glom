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

package org.glom.web.client.ui.list;

import java.util.ArrayList;

import org.glom.web.client.OnlineGlomServiceAsync;
import org.glom.web.client.ui.OnlineGlomConstants;
import org.glom.web.client.ui.cell.NavigationButtonCell;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.libglom.layout.LayoutGroup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RowCountChangeEvent;

/**
 *
 */
public class ListViewTable extends ListTable {

	// OnlineGlomConstants.java is generated in the target/ directory,
	// from OnlineGlomConstants.properties
	// by the gwt-maven-plugin's i18n (mvn:i18n) goal.
	private OnlineGlomConstants constants = GWT.create(OnlineGlomConstants.class);

	// These represent the minimum and maximum number of rows in the cell table not the number of rows with data.
	private static final int MAX_TABLE_ROWS = 15;
	private static final int MIN_TABLE_ROWS = 10;

	private int numNonEmptyRows = 0;

	public ListViewTable(final String documentID, final LayoutGroup layoutGroup,
			final NavigationButtonCell navigationButtonCell, final String quickFind) {
		super(documentID);
		this.quickFind = quickFind;
		//TODO: Us tableName set here?
		createCellTable(layoutGroup, tableName, MAX_TABLE_ROWS, constants.details(), navigationButtonCell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.list.ListTable#getDataProvider()
	 */
	@Override
	protected AsyncDataProvider<DataItem[]> getDataProvider() {
		final AsyncDataProvider<DataItem[]> dataProvider = new AsyncDataProvider<DataItem[]>() {

			@SuppressWarnings("unchecked")
			@Override
			protected void onRangeChanged(final HasData<DataItem[]> display) {
				// setup the callback object
				final Range range = display.getVisibleRange();
				final int start = range.getStart();
				final AsyncCallback<ArrayList<DataItem[]>> callback = new AsyncCallback<ArrayList<DataItem[]>>() {
					@Override
					public void onFailure(final Throwable caught) {
						// TODO: create a way to notify users of asynchronous callback failures
						GWT.log("AsyncCallback Failed: OnlineGlomService.get(Sorted)ListViewData()");
					}

					@Override
					public void onSuccess(final ArrayList<DataItem[]> result) {
						// keep track of the number of non-empty rows (rows with data)
						numNonEmptyRows = result.size();

						// Add empty rows if required.
						final int numEmptyRows = MIN_TABLE_ROWS - numNonEmptyRows;
						for (int i = 0; i < numEmptyRows; i++) {
							// A row that has one null item will be rendered as an empty row.
							result.add(new DataItem[1]);
						}
						updateRowData(start, result);

						// Note: numNonEmptyRows is not used by the RowCountChangeEvent handler but we need to fire the
						// event to get ListTable.ListTablePage.createText() to run and update the pager text using
						// getNumNonEmptyRows().
						RowCountChangeEvent.fire(display, numNonEmptyRows, true);
					}
				};

				// get data from the server
				final ColumnSortList colSortList = cellTable.getColumnSortList();
				if (colSortList.size() > 0) {
					// ColumnSortEvent has been requested by the user
					final ColumnSortInfo info = colSortList.get(0);

					// TODO: Just make the sort field an optional parameter instead of having two methods?
					OnlineGlomServiceAsync.Util.getInstance().getSortedListViewData(documentID, tableName, quickFind,
							start, range.getLength(),
							cellTable.getColumnIndex((Column<DataItem[], ?>) info.getColumn()), info.isAscending(),
							callback);

				} else {
					OnlineGlomServiceAsync.Util.getInstance().getListViewData(documentID, tableName, quickFind, start,
							range.getLength(), callback);

				}

			}
		};

		return dataProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.list.ListTable#getMinNumVisibleRows()
	 */
	@Override
	public int getMinNumVisibleRows() {
		return MIN_TABLE_ROWS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.list.ListTable#getNumNonEmptyRows()
	 */
	@Override
	public int getNumNonEmptyRows() {
		return numNonEmptyRows;
	}

}
