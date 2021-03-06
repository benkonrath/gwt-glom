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

import java.util.Date;
import java.util.HashMap;

import org.glom.web.client.StringUtils;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Field.GlomFieldType;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DetailsPlace extends HasTablePlace {
	private final TypedDataItem primaryKeyValue;

	public DetailsPlace(final String documentID, final String tableName, final TypedDataItem primarykeyValue) {
		super(documentID, tableName);
		this.primaryKeyValue = primarykeyValue;
	}

	public TypedDataItem getPrimaryKeyValue() {
		return primaryKeyValue;
	}

	@Prefix("details")
	public static class Tokenizer extends HasTablePlace.Tokenizer implements PlaceTokenizer<DetailsPlace> {

		private final String primaryKeyValueKey = "value";

		/**
		 * Creates the URL string that is shown in the browser. This is the bookmarked URL.
		 *
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
		 * @see org.glom.web.server.Utils.getGlomTypeGdaValueForTypedDataItem(String, String, glom_field_type,
		 *      TypedDataItem)
		 */
		@Override
		public String getToken(final DetailsPlace place) {
			// create the URL string:
			final String primaryKeyValueString = getStringForData(place.getPrimaryKeyValue());

			final HashMap<String, String> params = new HashMap<>();
			params.put(documentKey, place.getDocumentID());
			params.put(tableKey, place.getTableName());
			params.put(primaryKeyValueKey, primaryKeyValueString);
			return buildParamsToken(params);
		}

		/**
		 * Create a string representation of a database value, for use in a URL.
		 * This return string should be parseable by Utils.transformUnknownToActualType()
		 * on the server.
		 *
		 * @param value
		 * @param glomFieldType
		 * @return
		 */
		private String getStringForData(final TypedDataItem value) {
			final GlomFieldType glomFieldType = value.getType();

			String primaryKeyValueString = "";
			switch (glomFieldType) {
			case TYPE_NUMERIC:
				// non-locale specific number-to-string conversion:
				// http://docs.oracle.com/javase/6/docs/api/java/lang/Double.html#toString%28double%29
				primaryKeyValueString = Double.toString(value.getNumber());
				// Remove the trailing point and zero on integers. This just makes URL string look nicer.
				if (primaryKeyValueString.endsWith(".0")) {
					primaryKeyValueString = primaryKeyValueString.substring(0, primaryKeyValueString.length() - 2);
				}
				break;

			case TYPE_TEXT:
				primaryKeyValueString = value.getText();
				break;

			case TYPE_BOOLEAN:
				primaryKeyValueString = (value.getBoolean() ? "true" : "false");
				break;

			case TYPE_DATE:
				final Date date = value.getDate();
				if(date == null) {
					primaryKeyValueString = "";
				} else {
					//Note that almost any use of GWT's DateTimeFormat on the server causes an exception
					//because it is not implemented.
					//See http://code.google.com/p/google-web-toolkit/issues/detail?id=7671

					final DateTimeFormat dateTimeFomat = DateTimeFormat.getFormat("yyyy-MM-dd");
					primaryKeyValueString = dateTimeFomat.format(date);

					//We could this manually, but date.getYear(), etc, are not available in client-side GWT code.
					//
					//primaryKeyValueString = String.format("%04d-%02d-%02d",
					//		date.getYear() + 1900, date.getMonth() + 1, date.getDate());
				}

				break;

			case TYPE_INVALID:
				final String urlText = value.getUnknown();
				if (!value.isEmpty() && urlText != null) {
					// An invalid type that's not empty indicates that the primary key value has been created from a URL
					// string. Use the same string to represent the primary key value on the URL.
					primaryKeyValueString = urlText;
					// TODO: Update the primary key value string with the actual Gda Value that was created. The primary
					// key value could be different if the string-to-number conversion doesn't work.
				}
				break;

			default:
				// Unknown types are represented in the URL by an empty string. When loading a page with an unknown from
				// a URL (bookmark or link), the details view will run the query with an empty Value item based on the
				// type from the Glom document. The first result from this query will be shown. This means that the
				// specific record for unknown types are not bookmarkmarkable.

				// Support for bookmarking a new type can be added by adding a case statement for the new type here
				// and in this method:
				// org.glom.web.server.Utils.getGlomTypeGdaValueForTypedDataItem()

				// primaryKeyValueString remains empty
				break;
			}

			return primaryKeyValueString;
		}

		/**
		 * Create a DetailPlace that should be loaded from a URL string. This is called when users load the details view
		 * directly with the URL string (a bookmark or link).
		 *
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
		 * @see org.glom.web.server.Utils.getGlomTypeGdaValueForTypedDataItem(String, String, glom_field_type,
		 *      TypedDataItem)
		 */
		@Override
		public DetailsPlace getPlace(final String token) {
			// default empty values
			String documentID = "";
			String tableName = ""; // an empty value represents the default table

			final TypedDataItem primaryKeyValue = new TypedDataItem();

			final HashMap<String, String> params = getTokenParams(token);

			if (params == null) {
				return new DetailsPlace("", "", primaryKeyValue);
			}

			if (params.get(documentKey) != null) {
				documentID = params.get(documentKey);
			}

			if (params.get(tableKey) != null) {
				tableName = params.get(tableKey);
			}

			if (params.get(primaryKeyValueKey) != null) {
				final String primaryKeyValueString = params.get(primaryKeyValueKey);
				// Set as unknown because the type of the primary key is not known at this point. A proper primary key
				// value will be created using the type from the Glom document in the servlet.
				primaryKeyValue.setUnknown(primaryKeyValueString);
			}

			if (StringUtils.isEmpty(documentID)) {
				// The documentID was not retrieved from the URL. Use empty values for the details place.
				return new DetailsPlace("", "", primaryKeyValue);
			}

			return new DetailsPlace(documentID, tableName, primaryKeyValue);
		}
	}

}
