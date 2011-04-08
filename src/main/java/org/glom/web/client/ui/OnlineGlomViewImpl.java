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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OnlineGlomViewImpl extends Composite implements OnlineGlomView {

	private final VerticalPanel mainVPanel = new VerticalPanel();
	// FIXME remove static see FIXME below
	private final static ListBox listBox = new ListBox();
	private final HorizontalPanel hPanel = new HorizontalPanel();
	// FIXME make reusable and add a LayoutListActivity
	// this will get rid of the static listBox and static method below
	private final LayoutListView listTable = new LayoutListView();
	private final SimplePanel dataPanel = new SimplePanel();
	private Presenter presenter;
	private HandlerRegistration changeHandlerRegistration = null;

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

		for (int i = 0; i < names.size(); i++) {
			listBox.addItem(titles.get(i), names.get(i));
		}
	}

	@Override
	public void setTableSelectedIndex(int index) {
		listBox.setSelectedIndex(index);
	}

	@Override
	public void setTableChangeHandler(ChangeHandler changeHandler) {
		changeHandlerRegistration = listBox.addChangeHandler(changeHandler);
	}

	@Override
	public String getSelectedTable() {
		int selectedIndex = listBox.getSelectedIndex();
		return selectedIndex < 0 ? "" : listBox.getValue(selectedIndex);
	}

	@Override
	public void setListTable(LayoutListView listView) {
		// FIXME don't need to clear the dataPanel when LayoutListView is fixed
		dataPanel.clear();
		dataPanel.add(listView);
	}

	@Override
	public void clear() {
		if (changeHandlerRegistration != null)
			changeHandlerRegistration.removeHandler();
		listBox.clear();
		dataPanel.clear();
	}
}
