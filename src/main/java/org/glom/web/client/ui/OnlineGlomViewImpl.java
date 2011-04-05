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

package org.glom.web.client.ui;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OnlineGlomViewImpl extends Composite implements OnlineGlomView {

	private final VerticalPanel mainVPanel = new VerticalPanel();
	// FIXME remove static
	private final static ListBox listBox = new ListBox();
	private final HorizontalPanel hPanel = new HorizontalPanel();
	// FIXME make reusable and add a LayoutListActivity
	// this will get rid of the static listBox and static method below
	private final LayoutListView listTable = new LayoutListView();
	private final SimplePanel dataPanel = new SimplePanel();
	private Presenter presenter;

	public OnlineGlomViewImpl() {
		hPanel.add(new Label("Table:"));
		hPanel.add(listBox);
		mainVPanel.add(hPanel);
		dataPanel.add(listTable);
		mainVPanel.add(dataPanel);
		initWidget(mainVPanel);
	}

	// FIXME this needs to go!!
	public static String getCurrentTableName() {
		int selectedIndex = listBox.getSelectedIndex();
		return selectedIndex < 0 ? "" : listBox.getValue(selectedIndex);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	public void setTableSelection(ArrayList<String> names, ArrayList<String> titles) {
		listBox.clear();
		for (int i = 0; i < names.size(); i++) {
			listBox.addItem(titles.get(i), names.get(i));
		}
	}

	@Override
	public void setTableSelectedIndex(int index) {
		listBox.setTabIndex(index);
	}

	@Override
	public void setDocumentTitle(String title) {
		String selectedTable = "";
		int selectedIndex = listBox.getSelectedIndex();
		if (selectedIndex >= 0 && selectedIndex < listBox.getItemCount())
			selectedTable = ": " + listBox.getItemText(selectedIndex);
		Window.setTitle("OnlineGlom - " + title + selectedTable);
	}

	@Override
	public void setTableChangeHandler(ChangeHandler changeHandler) {
		listBox.addChangeHandler(changeHandler);
	}

	@Override
	public String getSelectedTable() {
		int selectedIndex = listBox.getSelectedIndex();
		return selectedIndex < 0 ? "" : listBox.getValue(selectedIndex);
	}

	@Override
	public void setListTable(LayoutListView listView) {
		dataPanel.clear();
		dataPanel.add(listView);
	}
}
