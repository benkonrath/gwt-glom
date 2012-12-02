package org.glom.web.shared;

import static org.junit.Assert.*;

import java.util.Date;
import org.junit.Test;

import com.googlecode.gwt.test.GwtModule;
import com.googlecode.gwt.test.GwtTest;

@GwtModule("org.glom.web.OnlineGlom")
public class GwtTestDataItem extends GwtTest {

	public GwtTestDataItem() {
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
		//in the server-side test (GwtTestDataItem),
		//because this API is deprecated, but the alternative
		//(Calendar) is not available in GWT client code.
		final Date val = new Date(1973, 4, 11);			
		item.setDate(val);
		assertEquals(val, item.getDate());
	}

}
