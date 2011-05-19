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

import org.glom.web.shared.GlomDocument;
import org.glom.web.shared.GlomField;
import org.glom.web.shared.LayoutListTable;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("libGlom")
public interface OnlineGlomService extends RemoteService {

	GlomDocument getGlomDocument(String documentTitle);

	/**
	 * Gets a {@link LayoutListTable} Object for the given Glom document title and table name.
	 * 
	 * @param documentTitle
	 *            title of the Glom document
	 * @param tableName
	 *            name of the table in the Glom document
	 * @return filled in {@link LayoutListTable}
	 */
	LayoutListTable getLayoutListTable(String documentTitle, String tableName);

	ArrayList<GlomField[]> getTableData(String documentTitle, String tableName, int start, int length);

	ArrayList<GlomField[]> getSortedTableData(String documentTitle, String tableName, int start, int length,
			int sortColumnIndex, boolean isAscending);

	/**
	 * Gets a list of Glom document titles found in the configured directory.
	 * 
	 * @return an {@link ArrayList<String>} of Glom document titles. If the list is empty, no glom documents were found
	 *         in the configured directory because it's empty or the directory is not configured correctly.
	 */
	ArrayList<String> getDocumentTitles();

	/**
	 * Checks if the PostgreSQL authentication has been set for this document.
	 * 
	 * @param documentTitle
	 *            title of Glom document to check
	 * @return true if the authentication has been set, false if it hasn't
	 */
	boolean isAuthenticated(String documentTitle);

	/**
	 * Checks if the provided PostgreSQL username and password are correct for the specified glom document. If the
	 * information is correct it is saved for future access.
	 * 
	 * @param documentTitle
	 *            title of the Glom document to check
	 * @param username
	 *            the PostgreSQL username
	 * @param password
	 *            the POstgreSQL password
	 * @return true if username and password are correct, false otherwise
	 */
	boolean checkAuthentication(String documentTitle, String username, String password);

	/**
	 * Gets a {@link LayoutListTable} Object for the default table of the given Glom document title.
	 * 
	 * @param documentTitle
	 *            title of the Glom document
	 * @return filled in {@link LayoutListTable} for the default table
	 */
	LayoutListTable getDefaultLayoutListTable(String documentTitle);

}