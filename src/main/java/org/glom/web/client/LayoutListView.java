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

import org.glom.web.shared.GlomField;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

class LayoutListDataProvider extends AsyncDataProvider<GlomField[]> {
	@Override
	protected void onRangeChanged(HasData<GlomField[]> display) {
		final Range range = display.getVisibleRange();

		final int start = range.getStart();
		final int length = range.getLength();

		AsyncCallback<ArrayList<GlomField[]>> callback = new AsyncCallback<ArrayList<GlomField[]>>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getTableData()");
			}

			public void onSuccess(ArrayList<GlomField[]> result) {
				updateRowData(start, result);
			}
		};

		OnlineGlomServiceAsync.Util.getInstance().getTableData(start, length, OnlineGlom.getCurrentTableName(),
				callback);
	}
}

public class LayoutListView extends Composite {

	private CellTable<GlomField[]> table;

	private class GlomFieldCell extends AbstractCell<GlomField> {

		// The SafeHtml class is used to escape strings to avoid XSS attacks. This is not strictly
		// neccessary because the values aren't coming from a user but I'm using it anyway as a reminder.
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
			SafeHtml text = SafeHtmlUtils.fromString(value.getText());
			sb.append(text);
			sb.appendHtmlConstant("</div>");
		}
	}

	private abstract class GlomFieldColumn extends Column<GlomField[], GlomField> {
		public GlomFieldColumn() {
			super(new GlomFieldCell());
		}
	}

	public LayoutListView(String[] headers, int numRows) {
		table = new CellTable<GlomField[]>(20);
		LayoutListDataProvider dataProvider = new LayoutListDataProvider();
		dataProvider.addDataDisplay(table);

		SimplePager pager = new SimplePager(SimplePager.TextLocation.CENTER);
		pager.setDisplay(table);

		// a panel to hold our widgets
		VerticalPanel panel = new VerticalPanel();
		panel.add(table);
		panel.add(pager);

		for (int i = 0; i < headers.length; i++) {
			// create a new column
			final int j = new Integer(i);
			GlomFieldColumn column = new GlomFieldColumn() {
				@Override
				public GlomField getValue(GlomField[] object) {
					return object[j];
				}
			};

			// add the column to the list
			table.addColumn(column, headers[i]);

		}

		table.setRowCount(numRows);

		// take care of the necessary stuff required for composite widgets
		initWidget(panel);
		setStyleName("glom-ListLayoutTable");
	}

}
