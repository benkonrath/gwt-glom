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

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ListPlace extends HasSelectableTablePlace {

	public ListPlace(String documentID, String tableName) {
		super(documentID, tableName);
	}

	@Prefix("list")
	public static class Tokenizer extends HasSelectableTablePlace.Tokenizer implements PlaceTokenizer<ListPlace> {

		@Override
		public String getToken(ListPlace place) {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(documentKey, place.getDocumentID());
			params.put(tableKey, place.getTableName());
			return buildParamsToken(params);
		}

		@Override
		public ListPlace getPlace(String token) {
			String documentID = "", tableName = "";
			
			HashMap<String, String> params = getTokenParams(token);
			
			if (params == null) {
				return new ListPlace("", "");
			}
	                
			if (params.get(documentKey) != null) {
				documentID = params.get(documentKey);
			}
	        
			if (params.get(tableKey) != null) {
				tableName = params.get(tableKey);
			}
			
			if ((documentID == "") || (tableName == "")) {
				// The URL string doesn't match what we're expecting. Just use the initial values for the details place.
				// TODO Shouldn't this just go to the document selection place?
				return new ListPlace("", "");
			}

			return new ListPlace(documentID, tableName);
		}
	}

}
