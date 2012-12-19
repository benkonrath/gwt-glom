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

import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.glom.web.client.OnlineGlomLoginService;
import org.glom.web.server.libglom.Document;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * This is the servlet class for setting up the server side of Online Glom. The client side can call the public methods
 * in this class via OnlineGlom
 * 
 * For instance, it loads all the available documents and provide a list - see getDocuments(). It then provides
 * information from each document. For instance, see getListViewLayout().
 * 
 * TODO: Watch for changes to the .glom files, to reload new versions and to load newly-added files. TODO: Watch for
 * changes to the properties (configuration)?
 */
@SuppressWarnings("serial")
public class OnlineGlomLoginServlet extends OnlineGlomServlet implements OnlineGlomLoginService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#checkAuthentication(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean checkAuthentication(final String documentID, final String username, final String password) {

		final HttpServletRequest request = getRequest(null);
		if (request != null && !request.isSecure()) {
			//TODO: Warn the user, instead of just asking for the login details again.
			Log.error("The servlet's checkAuthentication method was called via an insecure protocol. Refusing to continue.");
			return false;
		}
	
		final ConfiguredDocumentSet configuredDocumentSet = getConfiguredDocumentSet();
		if(configuredDocumentSet == null) {
			Log.error(documentID, "The document set could not be found.");
			return false;
		}

		final ConfiguredDocument configuredDoc = configuredDocumentSet.getDocument(documentID);
		if (configuredDoc == null) {
			Log.error(documentID, "The document could not be found for this ID: " + documentID);
			return false;
		}
	    
		final Document document = configuredDoc.getDocument();
		ComboPooledDataSource authenticatedConnection = null;
		try {
			authenticatedConnection = SqlUtils.tryUsernameAndPassword(document, username, password);
		} catch (final SQLException e) {
			Log.error(documentID, "Unknown SQL Error checking the database authentication.", e);
			return false;
		}
		
		if(authenticatedConnection != null) {
			final HttpSession session = request.getSession();
			final String sessionID = session.getId();
			
			// This GWT page suggests doing this on the client-side,
			// after returning the session ID to the client,
			// but it seems cleaner to do it here on the server side:
			final Cookie cookie = new Cookie(COOKIE_NAME, sessionID);
			cookie.setMaxAge(-1);
			cookie.setPath("/");
			//cookie.setSecure(true);
			cookie.setMaxAge(30 * 24 * 60 * 60); //30 days
			//TODO: How can we do this? cookie.setHttpOnly(true); //Avoid its use from client-side javascript.
			final HttpServletResponse response = this.getThreadLocalResponse();
			response.addCookie(cookie);


			// Let us retrieve the login details later,
			// based on the cookie's sessionID which we retrieve later:
			final UserStore userStore = getUserStore();
			if(userStore == null) {
				Log.error(documentID, "Could not retrieve the userStore");
				return false;
			}

			final Credentials credentials = new Credentials(document, username, password, authenticatedConnection);
			userStore.setCredentials(sessionID, credentials);
		}
		
		return (authenticatedConnection != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.OnlineGlomService#isAuthenticated(java.lang.String)
	 */
	@Override
	public boolean isAuthenticated(final String documentID) {
		/* We do not refuse to reply to this via HTTP (not HTTPS),
		 * because we need to return true even if there is no
		 * need to call checkAuthentication(), because everything
		 * is specified in the configuration settings, to
		 * make everything public.
		 */
		/*
		final HttpServletRequest request = getRequest(null);
		if (request != null && !request.isSecure()) {
			//TODO: Warn the user, instead of just asking for the login details again.
			Log.error("The servlet was called via an insecure protocol. Refusing to continue.");
			return false;
		}
		*/

		return super.isAuthenticated(documentID);
	}

}
