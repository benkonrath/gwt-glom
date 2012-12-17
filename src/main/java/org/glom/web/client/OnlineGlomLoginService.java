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


import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("gwtGlomLogin")
public interface OnlineGlomLoginService extends RemoteService {

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
	 * Checks if the PostgreSQL authentication has been set for this document.
	 * 
	 * @param documentID
	 *            identifier for the Glom document
	 * @return true if the authentication has been set, false if it hasn't
	 */
	boolean isAuthenticated(String documentID);
}