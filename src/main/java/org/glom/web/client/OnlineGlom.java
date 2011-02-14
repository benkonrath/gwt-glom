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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class OnlineGlom implements EntryPoint {

	private VerticalPanel mainVPanel = new VerticalPanel();
	private HorizontalPanel hPanel = new HorizontalPanel();
	private static ListBox dropBox = new ListBox();
	private LayoutList table = null;
	private String documentName = "";

	public void onModuleLoad() {
		dropBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				updateTable();
			}
		});

		hPanel.add(new Label("Table:"));
		hPanel.add(dropBox);

		mainVPanel.add(hPanel);

		// associate the main panel with the HTML host page
		RootPanel.get().add(mainVPanel);

		// set up the callback object.
		AsyncCallback<GlomDocument> callback = new AsyncCallback<GlomDocument>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: LibGlomService");
			}

			public void onSuccess(GlomDocument result) {
				GlomTable[] tables = result.getTables();
				for (int i = 0; i < tables.length; i++) {
					dropBox.addItem(tables[i].getTitle(), tables[i].getName());
				}
				dropBox.setSelectedIndex(result.getDefaultTableIndex());
				documentName = result.getTitle();
				updateTable();
			}
		};

		// make the call to get the filled in GlomDocument
		LibGlomServiceAsync.Util.getInstance().getGlomDocument(callback);
	}

	private void updateTable() {

		// set up the callback object.
		AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: LibGlomService.updateTable()");
			}

			public void onSuccess(String[] result) {
				if (table != null)
					mainVPanel.remove(table);
				table = new LayoutList(result);
				mainVPanel.add(table);
				Window.setTitle("OnlineGlom - " + documentName + ": " + dropBox.getItemText(dropBox.getSelectedIndex()));
			}
		};

		String selectedTable = dropBox.getValue(dropBox.getSelectedIndex());
		LibGlomServiceAsync.Util.getInstance().getLayoutListHeaders(selectedTable, callback);

	}

	// FIXME find a better way to do this
	public static String getCurrentTableName() {
		return dropBox.getValue(dropBox.getSelectedIndex());
	}
}
