package org.glom.web.client.place;

import static org.junit.Assert.*;
import org.junit.Test;

public class ListPlaceTest {

	public ListPlaceTest() {
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
		// Create a ListPlace, testing getPlace():
		final String documentId = "somedocument";
		final String tableName = "sometable";
		ListPlace place = getListPlaceFromToken("document=" + documentId + "&table=" + tableName);
		checkParameters(place, documentId, tableName);

		// Recreate it, testing getToken(),
		// checking that the same parameters are read back:
		final ListPlace.Tokenizer tokenizer = new ListPlace.Tokenizer();
		final String token = tokenizer.getToken(place);
		place = getListPlaceFromToken(token);
		checkParameters(place, documentId, tableName);
	}

	private void checkParameters(final ListPlace place, final String documentID, final String tableName) {
		assertTrue(place != null);

		assertEquals(documentID, place.getDocumentID());
		assertEquals(tableName, place.getTableName());
	}

	private ListPlace getListPlaceFromToken(final String token) {
		final ListPlace.Tokenizer tokenizer = new ListPlace.Tokenizer();
		final ListPlace place = tokenizer.getPlace(token);
		assertTrue(place != null);
		return place;
	}

	private void checkTokenWithoutParameters(final String token) {
		final ListPlace place = getListPlaceFromToken(token);

		assertTrue(place.getDocumentID() != null);
		assertTrue(place.getDocumentID().isEmpty());

		assertTrue(place.getTableName() != null);
		assertTrue(place.getTableName().isEmpty());
	}

}
