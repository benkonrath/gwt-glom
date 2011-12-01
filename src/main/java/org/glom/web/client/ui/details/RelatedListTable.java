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
import org.glom.web.client.ui.cell.OpenButtonCell;
import org.glom.web.client.ui.list.ListTable;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.layout.LayoutItemPortal;

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
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class RelatedListTable extends ListTable {

	private static final int NUM_VISIBLE_ROWS = 5;
	private static final int MIN_NUM_VISIBLE_ROWS = NUM_VISIBLE_ROWS;

	private TypedDataItem foreignKeyValue;
	private String relationshipName;
	private int numNonEmptyRows = MIN_NUM_VISIBLE_ROWS;

	public RelatedListTable(String documentID, LayoutItemPortal layoutItemPortal, TypedDataItem foreignKeyValue,
			String openButtonLabel, OpenButtonCell openButtonCell) {

		super(documentID);

		// These variables need to be set before the createCellTable() method is called so that the data provider can
		// use them.
		this.foreignKeyValue = foreignKeyValue;
		this.relationshipName = layoutItemPortal.getName();

		createCellTable(layoutItemPortal, NUM_VISIBLE_ROWS, openButtonLabel, openButtonCell);

		// The FixedLayout property tells the browser that we want to manually specify the column widths and don't want
		// the table to overflow it's container. Browsers will make columns with equal widths since we're currently not
		// manally specifying the column widths.
		cellTable.setWidth("100%", true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.list.ListTable#getDataProvider()
	 */
	@Override
	protected AsyncDataProvider<DataItem[]> getDataProvider() {
		AsyncDataProvider<DataItem[]> dataProvider = new AsyncDataProvider<DataItem[]>() {

			@Override
			@SuppressWarnings("unchecked")
			protected void onRangeChanged(final HasData<DataItem[]> display) {
				// setup the callback object
				final Range range = display.getVisibleRange();
				final int start = range.getStart();
				AsyncCallback<ArrayList<DataItem[]>> callback = new AsyncCallback<ArrayList<DataItem[]>>() {
					public void onFailure(Throwable caught) {
						// TODO: create a way to notify users of asynchronous callback failures
						GWT.log("AsyncCallback Failed: OnlineGlomService.get(Sorted)RelatedListData()");
					}

					public void onSuccess(ArrayList<DataItem[]> result) {
						// keep track of the number of non-empty rows (rows with data)
						numNonEmptyRows = result.size();
						// Add empty rows if required.
						int numEmptyRows = MIN_NUM_VISIBLE_ROWS - numNonEmptyRows;
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
				ColumnSortList colSortList = cellTable.getColumnSortList();
				if (colSortList.size() > 0) {
					// ColumnSortEvent has been requested by the user
					ColumnSortInfo info = colSortList.get(0);

					OnlineGlomServiceAsync.Util.getInstance().getSortedRelatedListData(documentID, tableName,
							relationshipName, foreignKeyValue, start, range.getLength(),
							cellTable.getColumnIndex((Column<DataItem[], ?>) info.getColumn()), info.isAscending(),
							callback);

				} else {
					OnlineGlomServiceAsync.Util.getInstance().getRelatedListData(documentID, tableName,
							relationshipName, foreignKeyValue, start, range.getLength(), callback);

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
		return MIN_NUM_VISIBLE_ROWS;
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
