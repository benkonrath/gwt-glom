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

/*
 * The DemoSelectionPlace doesn't hold state so we just have to make a basic implementation.
 */
public class DemoSelectionPlace extends Place {
	public static class Tokenizer implements PlaceTokenizer<DemoSelectionPlace> {
		@Override
		public DemoSelectionPlace getPlace(String token) {
			return new DemoSelectionPlace();
		}

		@Override
		public String getToken(DemoSelectionPlace place) {
			return null;
		}
	}
}
