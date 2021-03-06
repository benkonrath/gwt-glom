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

import org.glom.web.shared.Reports;

import com.google.gwt.event.dom.client.HasChangeHandlers;

/**
 *
 */
public interface TableSelectionView extends View {

	/**
	 * Allow the Activity to respond to changes to this widget.
	 */
	HasChangeHandlers getTableSelector();

	String getSelectedTableName();

	void setSelectedTableName(String tableName);

	String getSelectedTableTitle();

	void setTableSelection(ArrayList<String> tableNames, ArrayList<String> tableTitles);

	/**
	 * Allow the Activity to respond to changes to this widget.
	 */
	HasChangeHandlers getQuickFindBox();

	String getQuickFindText();

	void setQuickFindText(final String quickFind);

	HasChangeHandlers getLocaleSelector();

	void setLocaleList(ArrayList<String> names, ArrayList<String> titles);

	String getSelectedLocale();

	void setSelectedLocale(final String localeID);

	void setBackLinkVisible(boolean visible);

	void setBackLink(final String documentID, final String tableName, String quickFind);

	void setDocumentTitle(String documentTitle);

	HasChangeHandlers getReportSelector();

	void setReportList(Reports reports);

	String getSelectedReport();

	void setSelectedReport(String reportName);
}
