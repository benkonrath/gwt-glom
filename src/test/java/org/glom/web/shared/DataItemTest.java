package org.glom.web.shared;

import org.junit.Assert;
import org.junit.Test;

public class DataItemTest {

	public DataItemTest() {
	}

	@Test
	public void testBoolean() {
		final DataItem item = new DataItem();
		item.setBoolean(true);
		Assert.assertTrue(item.getBoolean());
	}

	@Test
	public void testNumber() {
		final DataItem item = new DataItem();
		final double val = 123.456;
		item.setNumber(val);
		Assert.assertTrue(item.getNumber() == val);
	}

	@Test
	public void testText() {
		final DataItem item = new DataItem();
		final String val = "abc";
		item.setText(val);
		Assert.assertTrue(item.getText() == val);
	}

}
