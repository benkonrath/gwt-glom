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

package org.glom.web.client;

import java.util.ArrayList;

import org.glom.web.shared.ColumnInfo;
import org.glom.web.shared.GlomField;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class LayoutListView extends Composite {

	private class GlomFieldCell extends AbstractCell<GlomField> {

		// The SafeHtml class is used to escape strings to avoid XSS attacks. This is not strictly
		// necessary because the values aren't coming from a user but I'm using it anyway as a reminder.
		@Override
		public void render(Context context, GlomField value, SafeHtmlBuilder sb) {
			if (value == null) {
				return;
			}

			// get the foreground and background colours if they're set
			SafeHtml fgcolour = null, bgcolour = null;
			String colour = value.getFGColour();
			if (colour == null) {
				fgcolour = SafeHtmlUtils.fromSafeConstant("");
			} else {
				fgcolour = SafeHtmlUtils.fromString("color:" + colour + ";");
			}
			colour = value.getBGColour();
			if (colour == null) {
				bgcolour = SafeHtmlUtils.fromSafeConstant("");
			} else {
				bgcolour = SafeHtmlUtils.fromString("background-color:" + colour + ";");
			}

			// set the text and colours
			sb.appendHtmlConstant("<div style=\"" + fgcolour.asString() + bgcolour.asString() + "\">");
			sb.append(SafeHtmlUtils.fromString(value.getText()));
			sb.appendHtmlConstant("</div>");
		}
	}

	public LayoutListView(ColumnInfo[] columns, int numRows) {
		final CellTable<GlomField[]> table = new CellTable<GlomField[]>(20);

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
				ColumnSortList colSortList = table.getColumnSortList();
				if (colSortList.size() > 0) {
					// ColumnSortEvent has been requested by the user
					ColumnSortInfo info = colSortList.get(0);
					OnlineGlomServiceAsync.Util.getInstance().getSortedTableData(OnlineGlom.getCurrentTableName(),
							start, range.getLength(), table.getColumnIndex((Column<GlomField[], ?>) info.getColumn()),
							info.isAscending(), callback);
				} else {
					OnlineGlomServiceAsync.Util.getInstance().getTableData(OnlineGlom.getCurrentTableName(), start,
							range.getLength(), callback);
				}
			}
		};

		dataProvider.addDataDisplay(table);

		HorizontalPanel hPanel = new HorizontalPanel();
		// setting the alignment through the object method doesn't work, I need to set the alignment manually ...
		// hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		hPanel.getElement().setPropertyString("align", HasHorizontalAlignment.ALIGN_RIGHT.getTextAlignString());
		System.out.println(hPanel.getElement().getInnerHTML());
		// I'm setting text location right but the text is actually at the left of the buttons ...
		SimplePager pager = new SimplePager(SimplePager.TextLocation.RIGHT);
		pager.setDisplay(table);
		hPanel.add(pager);

		// a panel to hold our widgets
		VerticalPanel panel = new VerticalPanel();
		panel.add(table);
		panel.add(hPanel);

		// create instances of GlomFieldColumn to retrieve the GlomField objects
		for (int i = 0; i < columns.length; i++) {
			// create a new column
			final int j = new Integer(i);
			Column<GlomField[], GlomField> column = new Column<GlomField[], GlomField>(new GlomFieldCell()) {
				@Override
				public GlomField getValue(GlomField[] object) {
					return object[j];
				}
			};

			// set column properties and add to cell table
			switch (columns[i].getAlignment()) {
			case HORIZONTAL_ALIGNMENT_LEFT:
				column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
				break;
			case HORIZONTAL_ALIGNMENT_RIGHT:
				column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
				break;
			case HORIZONTAL_ALIGNMENT_AUTO:
			default:
				// TODO: log warning, this shouldn't happen
				column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_DEFAULT);
				break;
			}
			column.setSortable(true);
			table.addColumn(column, new SafeHtmlHeader(SafeHtmlUtils.fromString(columns[i].getHeader())));
		}

		// set row count which is needed for paging
		table.setRowCount(numRows);

		// add an AsyncHandler to activate sorting for the AsyncDataProvider that was created above
		table.addColumnSortHandler(new AsyncHandler(table));

		// take care of the stuff required for composite widgets
		initWidget(panel);
		setStyleName("glom-LayoutListView");
	}
}
