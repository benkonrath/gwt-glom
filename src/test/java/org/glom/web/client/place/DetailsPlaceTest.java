package org.glom.web.client.place;

import static org.junit.Assert.*;
import org.junit.Test;

public class DetailsPlaceTest {

	public DetailsPlaceTest() {
	}

	@Test
	public void testGetPlaceNoParameters() {
		checkTokenWithoutParameters("");
		checkTokenWithoutParameters("something");
		checkTokenWithoutParameters("list:a=1");
		checkTokenWithoutParameters("value1=123");
	}

	@Test
	public void testGetPlaceParameters() {
		// Create a DetailsPlace, testing getPlace():
		final String documentId = "somedocument";
		final String tableName = "sometable";
		final String primaryKeyValue = "123";
		DetailsPlace place = getDetailsPlaceFromToken("document=" + documentId + "&table=" + tableName + "&value="
				+ primaryKeyValue);
		checkParameters(place, documentId, tableName, primaryKeyValue);

		// Recreate it, testing getToken(),
		// checking that the same parameters are read back:
		final DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
		final String token = tokenizer.getToken(place);
		place = getDetailsPlaceFromToken(token);
		checkParameters(place, documentId, tableName, primaryKeyValue);
	}

	private void checkParameters(final DetailsPlace place, final String documentID, final String tableName,
			final String primaryKeyValue) {
		assertNotNull(place);

		assertEquals(documentID, place.getDocumentID());
		assertEquals(tableName, place.getTableName());
		assertEquals(primaryKeyValue, place.getPrimaryKeyValue().getUnknown());
	}

	private DetailsPlace getDetailsPlaceFromToken(final String token) {
		final DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
		final DetailsPlace place = tokenizer.getPlace(token);
		assertNotNull(place);
		return place;
	}

	private void checkTokenWithoutParameters(final String token) {
		final DetailsPlace place = getDetailsPlaceFromToken(token);

		assertNotNull(place.getDocumentID());
		assertTrue(place.getDocumentID().isEmpty());

		assertNotNull(place.getTableName());
		assertTrue(place.getTableName().isEmpty());

		assertNotNull(place.getPrimaryKeyValue());
		assertTrue(place.getPrimaryKeyValue().isEmpty());
		assertEquals(null, place.getPrimaryKeyValue().getUnknown());
		assertEquals(0.0, place.getPrimaryKeyValue().getNumber(), 0.0); //TODO: Handle other types.
		assertEquals(null, place.getPrimaryKeyValue().getText());
	}

}
