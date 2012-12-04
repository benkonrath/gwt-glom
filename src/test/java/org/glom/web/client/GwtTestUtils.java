package org.glom.web.client;

import static org.junit.Assert.*;

import org.glom.web.shared.libglom.NumericFormat;
import org.junit.Test;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.test.GwtModule;
import com.googlecode.gwt.test.GwtTest;

@GwtModule("org.glom.web.OnlineGlom")
public class GwtTestUtils extends GwtTest {

	public GwtTestUtils() {
	}

	@Test
	public void testGetCurrentLocaleID() {
		assertNotNull(Utils.getCurrentLocaleID());
	}

	@Test
	public void testGetNumberFormat() {
		final NumericFormat numericFormat = new NumericFormat();
		final NumberFormat format = Utils.getNumberFormat(numericFormat);
		assertNotNull(format);
		assertNotNull(format.getPattern());
		assertFalse(StringUtils.isEmpty(format.getPattern()));
	}

	@Test
	public void testGetWidgetHeight() {
		final Widget widget = new Button();
		final int height = Utils.getWidgetHeight(widget);
		assertTrue(height > 0);
	}

}
