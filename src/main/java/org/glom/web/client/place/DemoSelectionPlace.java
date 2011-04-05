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
