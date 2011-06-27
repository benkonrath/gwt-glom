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

import org.glom.web.client.ui.DetailsView;
import org.glom.web.shared.Documents;
import org.glom.web.shared.GlomDocument;
import org.glom.web.shared.GlomField;
import org.glom.web.shared.layout.LayoutGroup;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("libGlom")
public interface OnlineGlomService extends RemoteService {

	GlomDocument getGlomDocument(String documentID);

	/**
	 * Gets a {@link LayoutGroup} for the given Glom document and table name.
	 * 
	 * @param documentID
	 *            identifier for the Glom document
	 * @param tableName
	 *            name of the table in the Glom document
	 * @return filled in {@link LayoutGroup}
	 */
	LayoutGroup getListLayout(String documentID, String tableName);

	/**
	 * Gets a {@link LayoutGroup} for the default table of the given Glom document.
	 * 
	 * @param documentID
	 *            identifier for the Glom document
	 * @return filled in {@link LayoutGroup} for the default table
	 */
	LayoutGroup getDefaultListLayout(String documentID);

	ArrayList<GlomField[]> getListData(String documentID, String tableName, int start, int length);

	ArrayList<GlomField[]> getSortedListData(String documentID, String tableName, int start, int length,
			int sortColumnIndex, boolean isAscending);

	/**
	 * Gets a list of Glom documents found in the configured directory.
	 * 
	 * @return an {@link ArrayList<String>} of Glom document titles. If the list is empty, no glom documents were found
	 *         in the configured directory because it's empty or the directory is not configured correctly.
	 */
	Documents getDocuments();

	/**
	 * Checks if the PostgreSQL authentication has been set for this document.
	 * 
	 * @param documentID
	 *            identifier for the Glom document
	 * @return true if the authentication has been set, false if it hasn't
	 */
	boolean isAuthenticated(String documentID);

	/**
	 * Checks if the provided PostgreSQL username and password are correct for the specified glom document. If the
	 * information is correct it is saved for future access.
	 * 
	 * @param documentID
	 *            identifier for the Glom document
	 * @param username
	 *            the PostgreSQL username
	 * @param password
	 *            the POstgreSQL password
	 * @return true if username and password are correct, false otherwise
	 */
	boolean checkAuthentication(String documentID, String username, String password);

	/**
	 * Gets a {@link LayoutGroup} that represents the layout of the {@link DetailsView} for the provided Glom document
	 * and table name.
	 * 
	 * @param documentID
	 *            identifier for the Glom document
	 * @param tableName
	 *            name of the table in the Glom document
	 * @return a {@link LayoutGroup} the represents the layout of the {@link DetailsView}
	 */
	ArrayList<LayoutGroup> getDetailsLayout(String documentID, String tableName);

	/**
	 * Gets a {@link LayoutGroup} that represents the layout of the {@link DetailsView} for the default table in the
	 * specified Glom document.
	 * 
	 * @param documentID
	 *            identifier for the Glom document
	 * @return a {@link LayoutGroup} the represents the layout of the {@link DetailsView}
	 */
	ArrayList<LayoutGroup> getDefaultDetailsLayout(String documentID);

	/**
	 * Gets data for the Details View.
	 * 
	 * @param documentID
	 *            identifier for the Glom document
	 * @param tableName
	 *            name of the table in the Glom document
	 * @param primaryKeyValue
	 *            value of the primary key in the specified Glom table to use in the query
	 * @return the result of the SQL query as an array of {@link GlomField}s
	 */
	GlomField[] getDetailsData(String documentID, String tableName, String primaryKeyValue);

}