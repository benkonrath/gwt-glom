package org.glom.web.client.place;

import junit.framework.TestCase;
import junit.framework.Assert;

public class DetailsPlaceTest extends TestCase {

	public DetailsPlaceTest() {
	}

	public void testGetPlaceNoParameters() {
		checkTokenWithoutParameters("");
		checkTokenWithoutParameters("something");
		checkTokenWithoutParameters("list:a=1");
		checkTokenWithoutParameters("value1=123");
	}

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
		DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
		final String token = tokenizer.getToken(place);
		place = getDetailsPlaceFromToken(token);
		checkParameters(place, documentId, tableName, primaryKeyValue);
	}

	private void checkParameters(DetailsPlace place, final String documentID, final String tableName,
			final String primaryKeyValue) {
		Assert.assertTrue(place != null);

		Assert.assertEquals(documentID, place.getDocumentID());
		Assert.assertEquals(tableName, place.getTableName());
		Assert.assertEquals(primaryKeyValue, place.getPrimaryKeyValue().getUnknown());
	}

	private DetailsPlace getDetailsPlaceFromToken(final String token) {
		DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
		DetailsPlace place = tokenizer.getPlace(token);
		Assert.assertTrue(place != null);
		return place;
	}

	private void checkTokenWithoutParameters(final String token) {
		DetailsPlace place = getDetailsPlaceFromToken(token);

		Assert.assertTrue(place.getDocumentID() != null);
		Assert.assertTrue(place.getDocumentID().isEmpty());

		Assert.assertTrue(place.getTableName() != null);
		Assert.assertTrue(place.getTableName().isEmpty());

		Assert.assertTrue(place.getPrimaryKeyValue() != null);
		Assert.assertTrue(place.getPrimaryKeyValue().isEmpty());
		Assert.assertEquals(null, place.getPrimaryKeyValue().getUnknown());
		Assert.assertEquals(0.0, place.getPrimaryKeyValue().getNumber());
		Assert.assertEquals(null, place.getPrimaryKeyValue().getText());
	}

}
