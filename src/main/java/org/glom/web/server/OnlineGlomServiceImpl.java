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

package org.glom.web.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.client.OnlineGlomService;
import org.glom.web.server.libglom.Document;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.DetailsLayoutAndData;
import org.glom.web.shared.DocumentInfo;
import org.glom.web.shared.Documents;
import org.glom.web.shared.NavigationRecord;
import org.glom.web.shared.Reports;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Report;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This is the servlet class for setting up the server side of Online Glom. The client side can call the public methods
 * in this class via OnlineGlom
 * 
 * For instance, it loads all the available documents and provide a list - see (). It then provides
 * information from each document. For instance, see getListViewLayout().
 * 
 * TODO: Watch for changes to the .glom files, to reload new versions and to load newly-added files. TODO: Watch for
 * changes to the properties (configuration)?
 */
@SuppressWarnings("serial")
public class OnlineGlomServiceImpl extends OnlineGlomServlet implements OnlineGlomService {

	
	/*
	 * This is called when the servlet is stopped or restarted.
	 * 
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		//TODO: This is an arbitrary place to do this,
		//because this is shared by all servlets.
		//It is even necessary?
		final ConfiguredDocumentSet configuredDocumentSet = getConfiguredDocumentSet();
		if(configuredDocumentSet != null) {
			configuredDocumentSet.forgetDocuments();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getConfigurationErrorMessage()
	 */
	@Override
	public String getConfigurationErrorMessage() {
		final ConfiguredDocumentSet configuredDocumentSet = getConfiguredDocumentSet();
		if(configuredDocumentSet == null) {
			Log.error("Could not get the configuredDocumentSet.");
			return "";
		}	
			
		final Exception configurationException = configuredDocumentSet.getConfigurationException();
		if (configurationException == null) {
			return "No configuration errors to report.";
		} else if (configurationException.getMessage() == null) {
			return configurationException.toString();
		} else {
			return configurationException.getMessage();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getDetailsData(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public DataItem[] getDetailsData(final String documentID, final String tableName,
			final TypedDataItem primaryKeyValue) {
		final ComboPooledDataSource authenticatedConnection = getConnection(documentID);
		if(authenticatedConnection == null) {
			return new DataItem[0];
		}

		// An empty tableName is OK, because that means the default table.

		final ConfiguredDocument configuredDoc = getDocument(documentID);
		if (configuredDoc == null) {
			return new DataItem[0];
		}

		return configuredDoc.getDetailsData(authenticatedConnection, tableName, primaryKeyValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getDetailsLayoutAndData(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public DetailsLayoutAndData getDetailsLayoutAndData(final String documentID, final String tableName,
			final TypedDataItem primaryKeyValue, final String localeID) {
		final ComboPooledDataSource authenticatedConnection = getConnection(documentID);
		if(authenticatedConnection == null) {
			return null;
		}

		// An empty tableName is OK, because that means the default table.

		final ConfiguredDocument configuredDoc = getDocument(documentID);
		if (configuredDoc == null) {
			return null;
		}

		final DetailsLayoutAndData initalDetailsView = new DetailsLayoutAndData();
		initalDetailsView
				.setLayout(configuredDoc.getDetailsLayoutGroup(tableName, StringUtils.defaultString(localeID)));
		initalDetailsView.setData(configuredDoc.getDetailsData(authenticatedConnection, tableName, primaryKeyValue));

		return initalDetailsView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getDocumentInfo(java.lang.String)
	 */
	@Override
	public DocumentInfo getDocumentInfo(final String documentID, final String localeID) {

		if (!isAuthenticated(documentID)) {
			return new DocumentInfo();
		}

		final ConfiguredDocument configuredDoc = getDocument(documentID);

		// Avoid dereferencing a null object:
		if (configuredDoc == null) {
			return new DocumentInfo();
		}

		return configuredDoc.getDocumentInfo(StringUtils.defaultString(localeID));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getDocuments()
	 */
	@Override
	public Documents getDocuments() {
		final ConfiguredDocumentSet configuredDocumentSet = getConfiguredDocumentSet();
		if(configuredDocumentSet == null) {
			Log.error("Could not get the configuredDocumentSet.");
			return null;
		}

		return configuredDocumentSet.getDocuments();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getListViewData(java.lang.String, java.lang.String, int, int, int,
	 * boolean)
	 */
	@Override
	public ArrayList<DataItem[]> getListViewData(final String documentID, final String tableName,
			final String quickFind, final int start, final int length, final int sortColumnIndex,
			final boolean isAscending) {
		if (!isAuthenticated(documentID)) {
			return new ArrayList<>();
		}

		final ConfiguredDocument configuredDoc = getDocument(documentID);
		if (configuredDoc == null) {
			return new ArrayList<>();
		}
		
		final ComboPooledDataSource authenticatedConnection = getConnection(documentID);
		if(authenticatedConnection == null) {
			return new ArrayList<>();
		}

		return configuredDoc.getListViewData(authenticatedConnection, tableName, quickFind, start, length, true, sortColumnIndex, isAscending);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getListViewLayout(java.lang.String, java.lang.String)
	 */
	@Override
	public LayoutGroup getListViewLayout(final String documentID, final String tableName, final String localeID) {
		if (!isAuthenticated(documentID)) {
			return new LayoutGroup();
		}

		final ConfiguredDocument configuredDoc = getDocument(documentID);
		if (configuredDoc == null) {
			return new LayoutGroup();
		}

		return configuredDoc.getListViewLayoutGroup(tableName, StringUtils.defaultString(localeID));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getRelatedListData(java.lang.String, java.lang.String, int, int, int,
	 * boolean)
	 */
	@Override
	public ArrayList<DataItem[]> getRelatedListData(final String documentID, final String tableName,
			final LayoutItemPortal portal, final TypedDataItem foreignKeyValue, final int start, final int length,
			final int sortColumnIndex, final boolean ascending) {
		if (!isAuthenticated(documentID)) {
			return new ArrayList<>();
		}

		// An empty tableName is OK, because that means the default table.

		if (portal == null) {
			Log.error("getRelatedListData(): portal is null.");
			return null;
		}

		final ConfiguredDocument configuredDoc = getDocument(documentID);
		if (configuredDoc == null) {
			return new ArrayList<>();
		}
		
		final ComboPooledDataSource authenticatedConnection = getConnection(documentID);
		if(authenticatedConnection == null) {
			return null;
		}

		return configuredDoc.getRelatedListData(authenticatedConnection, tableName, portal, foreignKeyValue, start, length, sortColumnIndex,
				ascending);
	}

	@Override
	public int getRelatedListRowCount(final String documentID, final String tableName, final LayoutItemPortal portal,
			final TypedDataItem foreignKeyValue) {
		if (!isAuthenticated(documentID)) {
			return 0;
		}

		// An empty tableName is OK, because that means the default table.

		if (portal == null) {
			Log.error("getRelatedListRowCount(): portal is null");
			return 0;
		}

		final ConfiguredDocument configuredDoc = getDocument(documentID);
		if (configuredDoc == null) {
			return 0;
		}
		
		final ComboPooledDataSource authenticatedConnection = getConnection(documentID);
		if(authenticatedConnection == null) {
			return 0;
		}

		return configuredDoc.getRelatedListRowCount(authenticatedConnection, tableName, portal, foreignKeyValue);
	}

	// TODO: Specify the foundset (via a where clause) and maybe a default sort order.
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getReportLayout(java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String getReportHTML(final String documentID, final String tableName, final String reportName,
			final String quickFind, final String localeID) {
		if (!isAuthenticated(documentID)) {
			return "";
		}

		final ConfiguredDocument configuredDoc = getDocument(documentID);
		if (configuredDoc == null) {
			return "";
		}

		final Document glomDocument = configuredDoc.getDocument();
		if (glomDocument == null) {
			final String errorMessage = "getReportHTML(): getDocument() failed.";
			Log.fatal(errorMessage);
			// TODO: throw new Exception(errorMessage);
			return "";
		}

		final Report report = glomDocument.getReport(tableName, reportName);
		if (report == null) {
			Log.info(documentID, tableName, "The report layout is not defined for this table:" + reportName);
			return "";
		}
		
		final ComboPooledDataSource authenticatedConnection = getConnection(documentID);
		if(authenticatedConnection == null) {
			return "";
		}

		Connection connection;
		try {
			connection = authenticatedConnection.getConnection();
		} catch (final SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return "Connection Failed";
		}

		// TODO: Use quickFind
		final ReportGenerator generator = new ReportGenerator(StringUtils.defaultString(localeID));
		return generator.generateReport(glomDocument, tableName, report, connection, quickFind);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getReportsList(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Reports getReportsList(final String documentID, final String tableName, final String localeID) {
		final ConfiguredDocument configuredDoc = getDocument(documentID);
		if(configuredDoc != null) {
			return configuredDoc.getReports(tableName, localeID);
		} else {
			return null;
		}
	}

	// TODO: It would be more efficient to get the extra related (or related related) column value along with the other
	// values,
	// instead of doing a separate SQL query to get it now for a specific row.
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#getSuitableRecordToViewDetails(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public NavigationRecord getSuitableRecordToViewDetails(final String documentID, final String tableName,
			final LayoutItemPortal portal, final TypedDataItem primaryKeyValue) {
		final ComboPooledDataSource authenticatedConnection = getConnection(documentID);
		if(authenticatedConnection == null) {
			return null;
		}

		// An empty tableName is OK, because that means the default table.

		if (portal == null) {
			Log.error("getSuitableRecordToViewDetails(): portal is null");
			return null;
		}

		final ConfiguredDocument configuredDoc = getDocument(documentID);
		if (configuredDoc == null) {
			return null;
		}

		return configuredDoc.getSuitableRecordToViewDetails(authenticatedConnection, tableName, portal, primaryKeyValue);
	}

	/*
	 * This is called when the servlet is started or restarted.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
	}

}
