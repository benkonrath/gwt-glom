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

import org.glom.web.client.StringUtils;
import org.glom.web.client.place.ListPlace;
import org.glom.web.shared.Reports;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 */
public class TableSelectionViewImpl extends Composite implements TableSelectionView {

	// OnlineGlomConstants.java is generated in the target/ directory,
	// from OnlineGlomConstants.properties
	// by the gwt-maven-plugin's i18n (mvn:i18n) goal.
	private final OnlineGlomConstants constants = GWT.create(OnlineGlomConstants.class);

	private Label documentTitleLabel = new Label();
	private ListBox tablesChooser = new ListBox();

	private Label searchLabel = new Label(constants.search());
	private TextBox searchTextBox = new TextBox();

	private Label reportsLabel = new Label(constants.reports());
	private ListBox reportsChooser = new ListBox();

	private ListBox localesChooser = new ListBox();

	private Anchor backLink = new Anchor(constants.backToList());
	private Presenter presenter;
	private HandlerRegistration backLinkHandlerReg;

	public TableSelectionViewImpl() {
		tablesChooser.setStyleName("tableschooser");
		searchLabel.setStyleName("searchlabel"); // TODO: This is tedious.
		searchTextBox.setStyleName("searchtextbox"); // TODO: This is tedious.
		backLink.setStyleName("backlink");

		// empty click handler to avoid having to check for if the HandlerRegistration is null in setBackLink()
		backLinkHandlerReg = backLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
			}
		});

		final FlowPanel titlebox = new FlowPanel();
		titlebox.getElement().setAttribute("id", "titlebox");
		titlebox.add(documentTitleLabel);

		titlebox.add(localesChooser);
		titlebox.add(reportsChooser);
		titlebox.add(reportsLabel);

		// document title
		// Set a default value for the document title label with the opacity set to 0. The headbox will bounce up and
		// down when retrieving the document title from the server if an empty string is used.
		documentTitleLabel.getElement().getStyle().setOpacity(0);
		documentTitleLabel.setText("A");
		documentTitleLabel.addStyleName("document-title");
		documentTitleLabel.getElement().setAttribute("id", "document-title");

		reportsLabel.setStyleName("reportslabel"); // TODO: This is tedious.
		reportsChooser.setStyleName("reportschooser"); // TODO: This is tedious.

		localesChooser.setStyleName("localeschooser"); // TODO: This is tedious.

		// headbox with the table selector
		final FlowPanel headbox = new FlowPanel();
		headbox.getElement().setAttribute("id", "headbox");
		headbox.add(tablesChooser);

		final FlowPanel searchbox = new FlowPanel();
		searchbox.getElement().setAttribute("id", "searchbox");
		searchbox.add(searchLabel);
		searchbox.add(searchTextBox);
		headbox.add(searchbox);

		headbox.add(backLink);

		// the main container widget
		final FlowPanel mainPanel = new FlowPanel();
		mainPanel.add(titlebox);
		mainPanel.add(headbox);

		initWidget(mainPanel);
	}

	@Override
	public void setTableSelection(final ArrayList<String> names, final ArrayList<String> titles) {
		tablesChooser.clear();

		if (names == null) {
			return;
		}

		for (int i = 0; i < names.size(); i++) {
			tablesChooser.addItem(titles.get(i), names.get(i));
		}
	}

	@Override
	public void setSelectedTableName(final String tableName) {
		if (StringUtils.isEmpty(tableName)) {
			// Make sure none are selected:
			// TODO: Find a better way to do this.
			for (int i = 0; i < tablesChooser.getItemCount(); i++) {
				tablesChooser.setItemSelected(i, false);
			}

			return;
		}

		for (int i = 0; i < tablesChooser.getItemCount(); i++) {
			if (tableName.equals(tablesChooser.getValue(i))) {
				tablesChooser.setSelectedIndex(i);
				break;
			}
		}

	}

	@Override
	public HasChangeHandlers getTableSelector() {
		return tablesChooser;
	}

	@Override
	public String getSelectedTableName() {
		final int selectedIndex = tablesChooser.getSelectedIndex();
		return selectedIndex < 0 ? "" : tablesChooser.getValue(selectedIndex);
	}

	@Override
	public void setBackLink(final String documentID, final String tableName, final String quickFind) {
		backLinkHandlerReg.removeHandler();
		backLinkHandlerReg = backLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				presenter.goTo(new ListPlace(documentID, tableName, ""));
			}
		});
	}

	@Override
	public void setDocumentTitle(final String documentTitle) {
		documentTitleLabel.setText(documentTitle);
		documentTitleLabel.getElement().getStyle().setOpacity(100);
	}

	@Override
	public void clear() {
		tablesChooser.clear();
		// Set a default value for the document title label with the opacity set to 0. The headbox will bounce up and
		// down when retrieving the document title from the server if an empty string is used.
		documentTitleLabel.getElement().getStyle().setOpacity(0);
		documentTitleLabel.setText("A");
	}

	@Override
	public void setBackLinkVisible(final boolean visible) {
		backLink.setVisible(visible);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public String getSelectedTableTitle() {
		final int selectedIndex = tablesChooser.getSelectedIndex();
		return selectedIndex < 0 ? "" : tablesChooser.getItemText(selectedIndex);
	}

	@Override
	public HasChangeHandlers getQuickFindBox() {
		return searchTextBox;
	}

	@Override
	public String getQuickFindText() {
		return searchTextBox.getText();
	}

	@Override
	public void setQuickFindText(final String quickFind) {
		searchTextBox.setText(quickFind);
	}

	@Override
	public HasChangeHandlers getLocaleSelector() {
		return localesChooser;
	}

	@Override
	public void setLocaleList(final ArrayList<String> ids, final ArrayList<String> titles) {
		localesChooser.clear();

		if(ids == null) {
			return;
		}

		for (int i = 0; i < ids.size(); i++) {
			localesChooser.addItem(titles.get(i), ids.get(i));
		}
	}

	@Override
	public String getSelectedLocale() {
		final int selectedIndex = localesChooser.getSelectedIndex();
		return selectedIndex < 0 ? "" : localesChooser.getValue(selectedIndex);
	}

	@Override
	public void setSelectedLocale(final String localeID) {
		if (StringUtils.isEmpty(localeID)) {
			// Make sure none are selected:
			// TODO: Find a better way to do this.
			for (int i = 0; i < localesChooser.getItemCount(); i++) {
				localesChooser.setItemSelected(i, false);
			}

			return;
		}

		for (int i = 0; i < localesChooser.getItemCount(); i++) {
			if (localeID.equals(localesChooser.getValue(i))) {
				localesChooser.setSelectedIndex(i);
				break;
			}
		}

	}

	@Override
	public HasChangeHandlers getReportSelector() {
		return reportsChooser;
	}

	@Override
	public String getSelectedReport() {
		final int selectedIndex = reportsChooser.getSelectedIndex();
		return selectedIndex < 0 ? "" : reportsChooser.getValue(selectedIndex);
	}

	@Override
	public void setSelectedReport(final String reportName) {
		if (StringUtils.isEmpty(reportName)) {
			// Make sure none are selected:
			// TODO: Find a better way to do this.
			for (int i = 0; i < reportsChooser.getItemCount(); i++) {
				reportsChooser.setItemSelected(i, false);
			}

			return;
		}

		for (int i = 0; i < reportsChooser.getItemCount(); i++) {
			if (reportName.equals(reportsChooser.getValue(i))) {
				reportsChooser.setSelectedIndex(i);
				break;
			}
		}

	}

	@Override
	public void setReportList(final Reports reports) {
		reportsChooser.clear();

		// Add a first item as a default for the combobox.
		// Otherwise a report will be chosen already,
		// so the user will not be able to choose that report to go to that page.
		// TODO: Think of a better UI for this.
		reportsChooser.addItem("-", "");

		if(reports == null)
			return;

		for (int i = 0; i < reports.getCount(); i++) {
			reportsChooser.addItem(reports.getTitle(i), reports.getName(i));
		}
	}
}
