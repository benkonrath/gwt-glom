package org.glom.web.client.place;

import junit.framework.TestCase;
import junit.framework.Assert;

public class DetailsPlaceTest extends TestCase {
 
	public DetailsPlaceTest() {
	}
 
	public void testGetPlaceNoParameters()  {
		checkTokenWithoutParameters("");
		checkTokenWithoutParameters("something");
		checkTokenWithoutParameters("list:a=1");
		checkTokenWithoutParameters("value1=123");
	}

	public void testGetPlaceParameters()  {
		DetailsPlace place = getDetailsPlaceFromToken("document=somedocument&table=sometable&value=123");

		Assert.assertEquals("somedocument", place.getDocumentID() );
		Assert.assertEquals("sometable", place.getTableName() );
		Assert.assertEquals("123", place.getPrimaryKeyValue().getUnknown() );
	}

        private DetailsPlace getDetailsPlaceFromToken(final String token) {
		DetailsPlace.Tokenizer tokenizer = new DetailsPlace.Tokenizer();
		DetailsPlace place = tokenizer.getPlace(token);
		Assert.assertTrue( place != null );
		return place;
	}

	private void checkTokenWithoutParameters(final String token) {
		DetailsPlace place = getDetailsPlaceFromToken(token);

		Assert.assertTrue( place.getDocumentID() != null );
		Assert.assertTrue( place.getDocumentID().isEmpty() );

		Assert.assertTrue( place.getTableName() != null );
		Assert.assertTrue( place.getTableName().isEmpty() );

		Assert.assertTrue( place.getPrimaryKeyValue() != null );
		Assert.assertTrue( place.getPrimaryKeyValue().isEmpty() );
		Assert.assertEquals(null, place.getPrimaryKeyValue().getUnknown() );
		Assert.assertEquals(0.0, place.getPrimaryKeyValue().getNumber() );
		Assert.assertEquals(null, place.getPrimaryKeyValue().getText() );
	}

}
