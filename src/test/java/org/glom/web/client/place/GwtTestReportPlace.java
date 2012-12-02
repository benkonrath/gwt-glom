package org.glom.web.client.place;

import static org.junit.Assert.*;
import org.junit.Test;

import com.googlecode.gwt.test.GwtModule;
import com.googlecode.gwt.test.GwtTest;

@GwtModule("org.glom.web.OnlineGlom")
public class GwtTestReportPlace extends GwtTest {

	public GwtTestReportPlace() {
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
		// Create a ReportPlace, testing getPlace():
		final String documentId = "somedocument";
		final String tableName = "sometable";
		final String reportName = "somereport";
		ReportPlace place = getReportPlaceFromToken("document=" + documentId + "&table=" + tableName + "&report=" + reportName);
		checkParameters(place, documentId, tableName, reportName);

		// Recreate it, testing getToken(),
		// checking that the same parameters are read back:
		final ReportPlace.Tokenizer tokenizer = new ReportPlace.Tokenizer();
		final String token = tokenizer.getToken(place);
		place = getReportPlaceFromToken(token);
		checkParameters(place, documentId, tableName, reportName);
	}

	private void checkParameters(final ReportPlace place, final String documentID, final String tableName, final String reportName) {
		assertTrue(place != null);

		assertEquals(documentID, place.getDocumentID());
		assertEquals(tableName, place.getTableName());
		assertEquals(reportName, place.getReportName());
	}

	private ReportPlace getReportPlaceFromToken(final String token) {
		final ReportPlace.Tokenizer tokenizer = new ReportPlace.Tokenizer();
		final ReportPlace place = tokenizer.getPlace(token);
		assertTrue(place != null);
		return place;
	}

	private void checkTokenWithoutParameters(final String token) {
		final ReportPlace place = getReportPlaceFromToken(token);

		assertTrue(place.getDocumentID() != null);
		assertTrue(place.getDocumentID().isEmpty());

		assertTrue(place.getTableName() != null);
		assertTrue(place.getTableName().isEmpty());
		
		assertTrue(place.getReportName() != null);
		assertTrue(place.getReportName().isEmpty());
				
	}

}
