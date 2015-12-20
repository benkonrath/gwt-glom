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

import org.glom.web.client.StringUtils;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DocumentLoginPlace extends HasDocumentPlace {

	public DocumentLoginPlace(final String documentID) {
		super(documentID);
	}

	@Prefix("documentlogin")
	public static class Tokenizer extends HasDocumentPlace.Tokenizer implements PlaceTokenizer<DocumentLoginPlace> {

		@Override
		public String getToken(final DocumentLoginPlace place) {
			final HashMap<String, String> params = new HashMap<>();
			params.put(documentKey, place.getDocumentID());
			return buildParamsToken(params);
		}

		@Override
		public DocumentLoginPlace getPlace(final String token) {
			// default empty values
			String documentID = "";

			final HashMap<String, String> params = getTokenParams(token);

			if (params == null) {
				return new DocumentLoginPlace("");
			}

			if (params.get(documentKey) != null) {
				documentID = params.get(documentKey);
			}

			if (StringUtils.isEmpty(documentID)) {
				// The documentID was not retrieved from the URL. Use empty values for the place.
				return new DocumentLoginPlace("");
			}

			return new DocumentLoginPlace(documentID);
		}
	}

}
