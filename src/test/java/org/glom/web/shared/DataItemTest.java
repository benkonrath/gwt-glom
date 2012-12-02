package org.glom.web.shared;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class DataItemTest {

	public DataItemTest() {
	}

	@Test
	public void testBoolean() {
		final DataItem item = new DataItem();
		item.setBoolean(true);
		assertEquals(true, item.getBoolean());
	}

	@Test
	public void testNumber() {
		final DataItem item = new DataItem();
		final double val = 123.456;
		item.setNumber(val);
		assertEquals(val, item.getNumber(), 0.000001);
	}

	@Test
	public void testText() {
		final DataItem item = new DataItem();
		final String val = "abc";
		item.setText(val);
		assertEquals(val, item.getText());
	}
	
	@Test
	public void testDate() {
		final DataItem item = new DataItem();
		
		//Not that we use a different API to create a Date
		//in the client-side test (GwtTestDataItem),
		//because this API is not available in GWT client code.
		//TODO: Is there no easier (and non-deprecated) way to create
		//a Date instance?
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.YEAR, 1973);
		cal.set(Calendar.MONTH, 4); /* The month is 0 indexed. */
		cal.set(Calendar.DAY_OF_MONTH, 11);
		final Date val = cal.getTime();
				
		item.setDate(val);
		assertEquals(val, item.getDate());
	}

}
