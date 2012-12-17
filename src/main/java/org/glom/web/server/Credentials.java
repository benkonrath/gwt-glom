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

import java.sql.SQLException;

import org.glom.web.server.libglom.Document;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class Credentials {
	public Document document;
	public String username;
	public String password;
	private ComboPooledDataSource cpds;
	SessionListener connectionInvalidator = null;
	
	public Credentials(final Document document, final String username, final String password, final ComboPooledDataSource cpds) {
		this.document = document;
		this.username = username;
		this.password = password;
		
		setConnection(cpds);
	}
	
	private void setConnection(final ComboPooledDataSource cpds) {
		this.cpds = cpds;

		//Forget the connection when the user's browser session ends:
		this.connectionInvalidator = new SessionListener(this);
	}
	
	public void invalidateConnection() {
		try {
			DataSources.destroy(cpds);
		} catch (final SQLException e) {
			Log.error("Error cleaning up the ComboPooledDataSource.", e);
		}

		cpds = null;
		connectionInvalidator = null;
	}

	/**
	 * @return
	 */
	public ComboPooledDataSource getConnection() {
		if(cpds != null) {
			return cpds;
		}
		
		// Try to recreate the connection,
		// which might have been invalidated after some time:
		ComboPooledDataSource authenticatedConnection = null;
		try
		{
			authenticatedConnection = SqlUtils.tryUsernameAndPassword(document, username, password);
		} catch (final SQLException e) {
			Log.error("Unknown SQL Error checking the database authentication.", e);
			return null;
		}
		
		if(authenticatedConnection == null) {
			return null;
		}
		
		//Remember it for a while:
		setConnection(authenticatedConnection);
		return authenticatedConnection;
	}
	
}
