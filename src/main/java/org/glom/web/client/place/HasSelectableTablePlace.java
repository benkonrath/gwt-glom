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

package org.glom.web.client.place;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.place.shared.Place;

/**
 * Super type for Place objects (bookmarkable URLs) that display the TableSelectionView.
 * 
 */
public abstract class HasSelectableTablePlace extends Place {

	private final String documentID;
	private final String tableName;
	private final String localeID;

	public HasSelectableTablePlace(final String documentID, final String tableName, final String localeID) {
		this.documentID = documentID;
		this.tableName = tableName;
		this.localeID = localeID;
	}

	public String getDocumentID() {
		return documentID;
	}

	public String getTableName() {
		return tableName;
	}

	public String getLocaleID() {
		return localeID;
	}

	public static class Tokenizer {
		protected final String documentKey = "document";
		protected final String tableKey = "table";
		protected final String localeKey = "lang";
		private final String separator = "&";
		private final String equals = "=";

		/**
		 * Get a HashMap of parameter names and values from the history token. This can parse tokens built by
		 * buildParamsToken().
		 * 
		 * @param historyToken
		 *            The historyToken provided to getPlace().
		 * @return A HasMap of names to values.
		 */
		protected HashMap<String, String> getTokenParams(final String historyToken) {
			final String[] arStr = historyToken.substring(0, historyToken.length()).split(separator);
			final HashMap<String, String> params = new HashMap<String, String>();
			for (int i = 0; i < arStr.length; i++) {
				final String[] substr = arStr[i].split(equals);
				if (substr.length != 2) {
					continue;
				}

				String key = "";
				String value = "";
				if (!substr[0].isEmpty()) {
					key = substr[0];
				}

				if (!substr[1].isEmpty()) {
					value = substr[1];
				}

				if (!key.isEmpty() && !value.isEmpty()) {
					params.put(key, value);
				}
			}

			return params;
		}

		/**
		 * Build a history token based on a HashMap of parameter names and values. This can later be parsed by
		 * getTokenParams().
		 * 
		 * @param params
		 *            A HashMap of names and values.
		 * @return A history string for use by getToken() implementation.
		 */
		protected String buildParamsToken(final HashMap<String, String> params) {
			String token = "";
			for (final Iterator<Entry<String, String>> it = params.entrySet().iterator(); it.hasNext();) {
				final Map.Entry<String, String> entry = it.next();
				final String key = entry.getKey();
				final String value = entry.getValue();
				if (key.isEmpty() || value.isEmpty())
					continue;

				if (token != "") {
					token += separator;
				}

				token += key + equals + value;
			}

			return token;
		}
	}

}
