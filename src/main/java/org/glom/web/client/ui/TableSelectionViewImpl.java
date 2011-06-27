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

import org.glom.web.client.place.ListPlace;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Ben Konrath <ben@bagu.org>
 */
public class TableSelectionViewImpl extends Composite implements TableSelectionView {

	ListBox listBox = new ListBox();
	Anchor backLink = new Anchor("Back to List");
	private Presenter presenter;
	private HandlerRegistration backLinkHandlerReg;

	public TableSelectionViewImpl() {
		// the table chooser widget
		HorizontalPanel tableChooser = new HorizontalPanel();
		tableChooser.setStyleName("tablechooser");
		tableChooser.add(new Label("Table:"));
		tableChooser.add(listBox);

		// the back link widget
		backLink.setStyleName("backlink");
		// empty click handler to avoid having to check for if the HandlerRegistration is null in setBackLink()
		backLinkHandlerReg = backLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			}
		});

		// the main container widget
		// TODO will probably need to change to different panel type to match the mockups
		HorizontalPanel panel = new HorizontalPanel();
		DOM.setElementAttribute(panel.getElement(), "id", "headbox");
		panel.add(backLink);
		panel.add(tableChooser);

		initWidget(panel);
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

	public void setBackLink(final String documentID) {
		backLinkHandlerReg.removeHandler();
		backLinkHandlerReg = backLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new ListPlace(documentID));
			}
		});
	}

	@Override
	public void clear() {
		listBox.clear();
	}

	@Override
	public void setBackLinkVisible(boolean visible) {
		backLink.setVisible(visible);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
