/*
 * Copyright (C) 2011 Openismus GmbH
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OnlineGlomLoginServiceAsync {

	/**
	 * Utility class to get the RPC Async interface from client-side code
	 */
	final class Util {
		private static OnlineGlomLoginServiceAsync instance;

		public static final OnlineGlomLoginServiceAsync getInstance() {
			if (instance == null) {
				instance = (OnlineGlomLoginServiceAsync) GWT.create(OnlineGlomLoginService.class);
			}
			return instance;
		}

		private Util() {
			// Utility class should not be instantiated
		}
	}

	void checkAuthentication(String documentID, String username, String password, AsyncCallback<Boolean> callback);

	void isAuthenticated(String documentID, AsyncCallback<Boolean> callback);
}
