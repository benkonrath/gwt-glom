package org.glom.web.client.place;

import org.junit.Test;
import org.junit.Assert;

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
		ListPlace.Tokenizer tokenizer = new ListPlace.Tokenizer();
		final String token = tokenizer.getToken(place);
		place = getListPlaceFromToken(token);
		checkParameters(place, documentId, tableName);
	}

	private void checkParameters(ListPlace place, final String documentID, final String tableName) {
		Assert.assertTrue(place != null);

		Assert.assertEquals(documentID, place.getDocumentID());
		Assert.assertEquals(tableName, place.getTableName());
	}

	private ListPlace getListPlaceFromToken(final String token) {
		ListPlace.Tokenizer tokenizer = new ListPlace.Tokenizer();
		ListPlace place = tokenizer.getPlace(token);
		Assert.assertTrue(place != null);
		return place;
	}

	private void checkTokenWithoutParameters(final String token) {
		ListPlace place = getListPlaceFromToken(token);

		Assert.assertTrue(place.getDocumentID() != null);
		Assert.assertTrue(place.getDocumentID().isEmpty());

		Assert.assertTrue(place.getTableName() != null);
		Assert.assertTrue(place.getTableName().isEmpty());
	}

}
