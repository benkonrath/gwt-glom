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

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/** A convenience class for dealing with the Online Glom configuration file
 * TODO: test this.
 */
class OnlineGlomProperties extends Properties {

	private static final long serialVersionUID = 4290997725469072758L;
	

	/** Get the credentials for a specific file, ignoring the global username and password.
	 * 
	 * @param filename
	 * @return
	 */
	public Credentials getCredentials(final String filename) {
		Credentials result = null;

		final String key = getKey(filename);
		if (key == null) {
			return result;
		}

		//Split the line at the . separators,
		final String[] keyArray = key.split("\\.");
		
		//Check that the third item is "filename", as expected:
		if (keyArray.length == 3 && "filename".equals(keyArray[2])) {
			//Get the username and password for this file:
			final String usernameKey = key.replaceAll(keyArray[2], "username");
			final String passwordKey = key.replaceAll(keyArray[2], "password");
			final String username = getPropertyNonNull(usernameKey).trim();
			final String password = getPropertyNonNull(passwordKey);
			result = new Credentials(null, username, password, null);
		}
		
		return result;
	}

	public String getGlobalUsername() {
		return getPropertyNonNull("glom.document.username").trim();
	}

	public String getGlobalPassword() {
		return getPropertyNonNull("glom.document.password");
	}

	public String getGlobalLocale() {
		return getPropertyNonNull("glom.document.locale");
	}

	public String getDocumentsDirectory() {
		return getPropertyNonNull("glom.document.directory");
	}
	
	/** Get the key for any *.*.filename = thefilename line.
	 *
	 * @param value
	 * @return
	 */
	private String getKey(final String filename) {
		
		for (final String key : stringPropertyNames()) {
			
			//Split the line at the . separators,
			final String[] keyArray = key.split("\\.");
			if (keyArray.length != 3)
				continue;
			if(!("filename".equals(keyArray[2]))) {
				continue;
			}

			if (getPropertyNonNull(key).trim().equals(filename)) {
				return key;
			}
		}

		return null;
	}
	
	private String getPropertyNonNull(final String key) {
		return StringUtils.defaultString(getProperty(key));
	}
}