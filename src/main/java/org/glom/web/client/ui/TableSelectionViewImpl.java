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

import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Ben Konrath <ben@bagu.org>
 */
public class TableSelectionViewImpl extends Composite implements TableSelectionView {

	private final HorizontalPanel hPanel = new HorizontalPanel();
	ListBox listBox = new ListBox();

	public TableSelectionViewImpl() {
		hPanel.add(new Label("Table:"));
		hPanel.add(listBox);
		initWidget(hPanel);
	}

	@Override
	public void setTableSelection(ArrayList<String> names, ArrayList<String> titles) {
		listBox.clear();
		for (int i = 0; i < names.size(); i++) {
			listBox.addItem(titles.get(i), names.get(i));
		}
	}

	@Override
	public void setTableSelectedIndex(int index) {
		listBox.setSelectedIndex(index);
	}

	@Override
	public HasChangeHandlers getTableSelector() {
		return listBox;
	}

	@Override
	public String getSelectedTable() {
		int selectedIndex = listBox.getSelectedIndex();
		return selectedIndex < 0 ? "" : listBox.getValue(selectedIndex);
	}

	@Override
	public void clear() {
		listBox.clear();
	}

}
