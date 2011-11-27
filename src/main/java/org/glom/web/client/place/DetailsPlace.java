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

import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.layout.LayoutItemField.GlomFieldType;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DetailsPlace extends HasSelectableTablePlace {
	private TypedDataItem primaryKeyValue;

	public DetailsPlace(String documentID, String tableName, TypedDataItem primarykeyValue) {
		super(documentID, tableName);
		this.primaryKeyValue = primarykeyValue;
	}

	public TypedDataItem getPrimaryKeyValue() {
		return primaryKeyValue;
	}

	@Prefix("details")
	public static class Tokenizer extends HasSelectableTablePlace.Tokenizer implements PlaceTokenizer<DetailsPlace> {

		private final String primaryKeyValueKey = "value=";

		/**
		 * Creates the URL string that is shown in the browser. This is the bookmarked URL.
		 * 
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
		 */
		@Override
		public String getToken(DetailsPlace place) {
			TypedDataItem primaryKeyValue = place.getPrimaryKeyValue();
			GlomFieldType glomFieldType = primaryKeyValue.getGlomFieldType();

			// create the URL string based on the
			String primaryKeyValueString = "";
			switch (glomFieldType) {
			case TYPE_NUMERIC:
				primaryKeyValueString = new Double(primaryKeyValue.getNumber()).toString();
				// Remove the trailing point and zero on integers.
				if (primaryKeyValueString.endsWith(".0"))
					primaryKeyValueString = primaryKeyValueString.substring(0, primaryKeyValueString.length() - 2);
				break;
			case TYPE_TEXT:
				primaryKeyValueString = primaryKeyValue.getText();
				break;
			default:
				// Unknown types are represented in the URL by an empty string. This means the details view with these
				// types will run the query with an empty Value item based on the Glom type from the Glom document. The
				// fir result will be shown.
				// TODO update the URL location string and the TypedDataItem when this happens
				break;
			}

			return documentKey + place.getDocumentID() + seperator + tableKey + place.getTableName() + seperator
					+ primaryKeyValueKey + primaryKeyValueString;
		}

		/**
		 * Create a DetailPlace that should be loaded from a URL string. This is called when users load the details view
		 * directly with the URL string (a bookmark or link).
		 * 
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
		 */
		@Override
		public DetailsPlace getPlace(String token) {
			String[] tokenArray = token.split(seperator);

			// default empty values
			String documentID = "", tableName = "";
			TypedDataItem primaryKeyValue = new TypedDataItem();

			if (tokenArray.length != 3) {
				// The URL string doesn't match what we're expecting. Just use the initial values for the details place.
				// TODO Shouldn't this just go to the document selection place?
				return new DetailsPlace(documentID, tableName, primaryKeyValue);
			}

			// Get the document ID string.
			// TODO else go to the document selection place?
			if (documentKey.equals(tokenArray[0].substring(0, documentKey.length()))) {
				documentID = tokenArray[0].substring(documentKey.length());
			}

			// Get the table name string.
			// An empty table name will load the default table so it's ok if this key isn't found
			if (tableKey.equals(tokenArray[1].substring(0, tableKey.length()))) {
				tableName = tokenArray[1].substring(tableKey.length());
			}

			// Get the primary key value.
			if (primaryKeyValueKey.equals(tokenArray[2].substring(0, primaryKeyValueKey.length()))) {
				// the text after the 'value='
				String primaryKeyValueString = tokenArray[2].substring(primaryKeyValueKey.length());

				// Try to create a TypedDataItem from the text. Check if it's a number first.
				try {
					primaryKeyValue.setNumber(new Double(primaryKeyValueString));
				} catch (NumberFormatException e) {
					// It's not a number, use the string.
					primaryKeyValue.setText(primaryKeyValueString);
				}
			}

			return new DetailsPlace(documentID, tableName, primaryKeyValue);
		}
	}

}
