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

import java.util.ArrayList;

import org.glom.web.shared.Documents;
import org.glom.web.shared.GlomDocument;
import org.glom.web.shared.GlomField;
import org.glom.web.shared.layout.LayoutGroup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OnlineGlomServiceAsync {

	/**
	 * Utility class to get the RPC Async interface from client-side code
	 */
	public static final class Util {
		private static OnlineGlomServiceAsync instance;

		public static final OnlineGlomServiceAsync getInstance() {
			if (instance == null) {
				instance = (OnlineGlomServiceAsync) GWT.create(OnlineGlomService.class);
			}
			return instance;
		}

		private Util() {
			// Utility class should not be instantiated
		}
	}

	void getGlomDocument(String documentID, AsyncCallback<GlomDocument> callback);

	void getListLayout(String documentID, String tableName, AsyncCallback<LayoutGroup> callback);

	void getListData(String documentID, String tableName, int start, int length,
			AsyncCallback<ArrayList<GlomField[]>> callback);

	void getSortedListData(String documentID, String tableName, int start, int length, int columnIndex,
			boolean isAscending, AsyncCallback<ArrayList<GlomField[]>> callback);

	void getDocuments(AsyncCallback<Documents> callback);

	void isAuthenticated(String documentID, AsyncCallback<Boolean> callback);

	void checkAuthentication(String documentID, String username, String password, AsyncCallback<Boolean> callback);

	void getDefaultListLayout(String documentID, AsyncCallback<LayoutGroup> callback);

	void getDetailsLayout(String documentID, String tableName, AsyncCallback<ArrayList<LayoutGroup>> callback);

	void getDetailsData(String documentID, String tableName, String primaryKeyValue, AsyncCallback<GlomField[]> callback);

}
