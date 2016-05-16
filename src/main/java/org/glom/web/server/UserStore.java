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

import java.util.Hashtable;

/**
 * A store of user credentials, details, etc,
 * retrievable based on the session ID,
 * which we store in a cookie in the client's browser.
 *
 * @author Murray Cumming <murrayc@murrayc.com>
 *
 */
public class UserStore {

	/**
	 * A map of the sessionID to the credentials.
	 */
	private Hashtable<String, Credentials> credentialsMap = new Hashtable<>();

	/**
	 *
	 */
	public UserStore() {
	}

	/**
	 * TODO: It would be far better to store a hash,
	 * if we can find a way to authenticate with PostgreSQL with a hash.
	 */
	public Credentials getCredentials(final String sessionID) {
		return credentialsMap.get(sessionID);
	}

	public void setCredentials(final String sessionID, final Credentials credentials) {
		//Note that HashTable.put() is synchronized,
		//but be careful if we ever change this to a container that does not
		//have synchronized methods.
		//TODO: Avoid races between checking and setting.
		credentialsMap.put(sessionID, credentials);
	}

}
