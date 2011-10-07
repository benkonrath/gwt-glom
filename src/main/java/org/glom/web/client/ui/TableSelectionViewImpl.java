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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Ben Konrath <ben@bagu.org>
 */
public class TableSelectionViewImpl extends Composite implements TableSelectionView {

	Label documentTitleLabel = new Label();
	ListBox tableChooser = new ListBox();
	Anchor backLink = new Anchor("Back to List");
	private Presenter presenter;
	private HandlerRegistration backLinkHandlerReg;

	public TableSelectionViewImpl() {
		tableChooser.setStyleName("tablechooser");
		backLink.setStyleName("backlink");

		// empty click handler to avoid having to check for if the HandlerRegistration is null in setBackLink()
		backLinkHandlerReg = backLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			}
		});

		// headbox with the table selector
		FlowPanel headbox = new FlowPanel();
		DOM.setElementAttribute(headbox.getElement(), "id", "headbox");
		headbox.add(backLink);
		headbox.add(tableChooser);

		// document title
		// Set a default value for the document title label with the opacity set to 0. The headbox will bounce up and
		// down when retrieving the document title from the server if an empty string is used.
		documentTitleLabel.getElement().getStyle().setOpacity(0);
		documentTitleLabel.setText("A");
		documentTitleLabel.addStyleName("document-title");
		DOM.setElementAttribute(documentTitleLabel.getElement(), "id", "document-title");

		// the main container widget
		FlowPanel mainPanel = new FlowPanel();
		mainPanel.add(documentTitleLabel);
		mainPanel.add(headbox);

		initWidget(mainPanel);
	}

	@Override
	public void setTableSelection(ArrayList<String> names, ArrayList<String> titles) {
		tableChooser.clear();
		for (int i = 0; i < names.size(); i++) {
			tableChooser.addItem(titles.get(i), names.get(i));
		}
	}

	@Override
	public void setSelectedTableName(String tableName) {
		for (int i = 0; i < tableChooser.getItemCount(); i++) {
			if (tableName.equals(tableChooser.getValue(i))) {
				tableChooser.setSelectedIndex(i);
				break;
			}
		}

	}

	@Override
	public HasChangeHandlers getTableSelector() {
		return tableChooser;
	}

	@Override
	public String getSelectedTableName() {
		int selectedIndex = tableChooser.getSelectedIndex();
		return selectedIndex < 0 ? "" : tableChooser.getValue(selectedIndex);
	}

	public void setBackLink(final String documentID, final String tableName) {
		backLinkHandlerReg.removeHandler();
		backLinkHandlerReg = backLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new ListPlace(documentID, tableName));
			}
		});
	}

	public void setDocumentTitle(String documentTitle) {
		documentTitleLabel.setText(documentTitle);
		documentTitleLabel.getElement().getStyle().setOpacity(100);
	}

	@Override
	public void clear() {
		tableChooser.clear();
		// Set a default value for the document title label with the opacity set to 0. The headbox will bounce up and
		// down when retrieving the document title from the server if an empty string is used.
		documentTitleLabel.getElement().getStyle().setOpacity(0);
		documentTitleLabel.setText("A");
	}

	@Override
	public void setBackLinkVisible(boolean visible) {
		backLink.setVisible(visible);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public String getSelectedTableTitle() {
		int selectedIndex = tableChooser.getSelectedIndex();
		return selectedIndex < 0 ? "" : tableChooser.getItemText(selectedIndex);
	}
}
