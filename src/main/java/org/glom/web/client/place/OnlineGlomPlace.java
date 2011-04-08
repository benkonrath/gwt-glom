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

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class OnlineGlomPlace extends Place {

	private final String documentTitle;

	public OnlineGlomPlace(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	@Prefix("Document")
	public static class Tokenizer implements PlaceTokenizer<OnlineGlomPlace> {

		@Override
		public String getToken(OnlineGlomPlace place) {
			return place.getDocumentTitle();
		}

		@Override
		public OnlineGlomPlace getPlace(String token) {
			return new OnlineGlomPlace(token);
		}
	}

}
