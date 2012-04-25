package org.glom.web.shared;

import org.glom.web.shared.DataItem;
import org.junit.Test;
import org.junit.Assert;

public class DataItemTest {

	public DataItemTest() {
	}

	@Test
	public void testBoolean() {
		DataItem item = new DataItem();
		item.setBoolean(true);
		Assert.assertTrue(item.getBoolean());
	}

	@Test
	public void testNumber() {
		DataItem item = new DataItem();
		final double val = 123.456;
		item.setNumber(val);
		Assert.assertTrue(item.getNumber() == val);
	}

	@Test
	public void testText() {
		DataItem item = new DataItem();
		final String val = "abc";
		item.setText(val);
		Assert.assertTrue(item.getText() == val);
	}

}
