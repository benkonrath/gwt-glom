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

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DetailsPlace extends HasSelectableTablePlace {
	private String primaryKeyValue;

	public DetailsPlace(String documentID, String primarykey) {
		super(documentID);
		this.primaryKeyValue = primarykey;
	}

	public String getPrimaryKeyValue() {
		return primaryKeyValue;
	}

	@Prefix("details")
	public static class Tokenizer implements PlaceTokenizer<DetailsPlace> {

		final String documentKey = "document=";
		private final String primaryKeyValueKey = "value=";
		protected final String seperator = "&";

		@Override
		public String getToken(DetailsPlace place) {
			return documentKey + place.getDocumentID() + seperator + primaryKeyValueKey + place.getPrimaryKeyValue();
		}

		@Override
		public DetailsPlace getPlace(String token) {
			String[] tokenArray = token.split(seperator);
			String documentID = "", primaryKeyValue = "";
			if (tokenArray.length != 2)
				return new DetailsPlace(documentID, primaryKeyValue);

			if (documentKey.equals(tokenArray[0].substring(0, documentKey.length()))) {
				documentID = tokenArray[0].substring(documentKey.length());
			}

			if (primaryKeyValueKey.equals(tokenArray[1].substring(0, primaryKeyValueKey.length()))) {
				primaryKeyValue = tokenArray[1].substring(primaryKeyValueKey.length());
			}

			return new DetailsPlace(documentID, primaryKeyValue);
		}
	}

}
