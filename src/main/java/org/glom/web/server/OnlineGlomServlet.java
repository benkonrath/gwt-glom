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
	protected static final String COOKIE_NAME = "OnlineGlomSessionID";
	protected UserStore userStore = new UserStore();

	/**
	 * 
	 */
	public OnlineGlomServlet() {
		super();
	}

	/**
	 * @param delegate
	 */
	public OnlineGlomServlet(Object delegate) {
		super(delegate);
	}

	protected ComboPooledDataSource getConnectionForCookie() {
		final String sessionID = getSessionIdFromCookie();
		if(StringUtils.isEmpty(sessionID)) {
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

}