/*
 * Copyright (C) 2012 Openismus GmbH
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Ben Konrath <ben@bagu.org>
 *
 */
public class OnlineGlomServlet extends RemoteServiceServlet {

	private static final long serialVersionUID = 6173927100594792748L;
	
	private static final String CONFIGURED_DOCUMENT_SET = "configuredDocumentSet";
	private static final String USER_STORE = "userStore";
	protected static final String COOKIE_NAME = "OnlineGlomSessionID";
	
	/**
	 * 
	 */
	public OnlineGlomServlet() {
		super();
	
	}

	/**
	 * 
	 */

	/**
	 * @param delegate
	 */
	public OnlineGlomServlet(Object delegate) {
		super(delegate);
	}

	protected UserStore getUserStore() {
		//See if there is already a shared userstore
		final ServletConfig config = this.getServletConfig();
		if(config == null) {
			Log.error("getServletConfig() return null");
			return null;
		}
	
		final ServletContext context = config.getServletContext();
		if(context == null) {
			Log.error("getServletContext() return null");
			return null;
		}

		//Use the existing shared document set, if any:
		final Object object = context.getAttribute(USER_STORE);
		if((object != null) && !(object instanceof UserStore)) {
			Log.error("The configuredDocumentSet attribute is not of the expected type.");
			return null;
		}
		
		UserStore userStore = (UserStore)object;
		if(userStore != null) {
			return userStore;
		}


		//Create the shared userstore
		//TODO: Possible race condition between checking and creating+setting:
		userStore = new UserStore();

		//Store it in the Servlet Context,
		//so it is available to all servlets:
		context.setAttribute(USER_STORE, userStore);	

		return userStore;
	}

	protected ConfiguredDocumentSet getConfiguredDocumentSet() {
		//See if there is already a shared documentSet:
		final ServletConfig config = this.getServletConfig();
		if(config == null) {
			Log.error("getServletConfig() return null");
			return null;
		}
	
		final ServletContext context = config.getServletContext();
		if(context == null) {
			Log.error("getServletContext() return null");
			return null;
		}

		//Use the existing shared document set, if any:
		final Object object = context.getAttribute(CONFIGURED_DOCUMENT_SET);
		if((object != null) && !(object instanceof ConfiguredDocumentSet)) {
			Log.error("The configuredDocumentSet attribute is not of the expected type.");
			return null;
		}
		
		ConfiguredDocumentSet configuredDocumentSet = (ConfiguredDocumentSet)object;
		if(configuredDocumentSet != null) {
			return configuredDocumentSet;
		}


		//Create the shared document set:
		//TODO: Possible race condition between checking and creating+setting:
		configuredDocumentSet = new ConfiguredDocumentSet();
		try {
			configuredDocumentSet.readConfiguration();
		} catch (ServletException e) {
			Log.error("Configuration error", e);
			return null;
		}
		
		//Store it in the Servlet Context,
		//so it is available to all servlets:
		context.setAttribute(CONFIGURED_DOCUMENT_SET, configuredDocumentSet);	

		return configuredDocumentSet;
	}
	
	/**
	 * @param documentID
	 * @return
	 */
	protected ConfiguredDocument getDocument(final String documentID) {
		final ConfiguredDocumentSet configuredDocumentSet = getConfiguredDocumentSet();
		if(configuredDocumentSet == null) {
			Log.error("Could not get the configuredDocumentSet.");
			return null;
		}

		return configuredDocumentSet.getDocument(documentID);
	}
	
	protected ComboPooledDataSource getConnectionForCookie() {
		final String sessionID = getSessionIdFromCookie();
		if(StringUtils.isEmpty(sessionID)) {
			return null;
		}

		final UserStore userStore = getUserStore();
		if(userStore == null) {
			Log.error("Could not retrieve the userStore");
			return null;
		}
	
		final UserStore.Credentials credentials = userStore.getCredentials(sessionID);
		if(credentials == null) {
			return null;
		}
	
		return credentials.getConnection();
	}

	/**
	 * @return
	 */
	protected String getSessionIdFromCookie() {
		final HttpServletRequest request = this.getThreadLocalRequest();
		final Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}

		Cookie sessionCookie = null;
		for(Cookie cookie : cookies) {
			if (cookie == null)
				continue;
			
			if (StringUtils.equals(cookie.getName(), COOKIE_NAME)) {
				sessionCookie = cookie;
				break;
			}
		}
	
		if (sessionCookie == null) {
			return null;
		}
		
		final String sessionID = sessionCookie.getValue();
		Log.info("sessionID=" + sessionID);
		return sessionID;
	}
	
	/*
	 * This is called when the servlet is started or restarted.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException {
	}
	
	/*
	 * This is called when the servlet is stopped or restarted.
	 * 
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {
		//TODO: When should we do this for a shared set?: configuredDocumentSet.forgetDocuments();
	}
	
	//TODO: Rename this to avoid confusion with OnlineGlomLoginServlet.isAuthenticated()?
	protected boolean isAuthenticated(final String documentID) {
		//TODO: Use the document.
		final ComboPooledDataSource authenticatedConnection = getConnectionForCookie();
		return (authenticatedConnection != null);
	}


}