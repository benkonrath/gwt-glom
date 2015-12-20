package org.glom.web.client;

import static org.junit.Assert.*;

import org.junit.Test;

import com.googlecode.gwt.test.GwtModule;
import com.googlecode.gwt.test.GwtTest;

@GwtModule("org.glom.web.OnlineGlom")
public class GwtTestStringUtils extends GwtTest {

	public GwtTestStringUtils() {
	}

	@Test
	public void testIsEmpty() {
		assertTrue(StringUtils.isEmpty(""));
		assertTrue(StringUtils.isEmpty(null));
		//noinspection RedundantStringConstructorCall
		assertTrue(StringUtils.isEmpty(new String()));
		assertFalse(StringUtils.isEmpty("something"));
	}

	@Test
	public void testDefaultString() {
		assertEquals("", StringUtils.defaultString(""));
		assertEquals("", StringUtils.defaultString(null));
		
		assertEquals("something", StringUtils.defaultString("something"));
	}

	@SuppressWarnings("RedundantStringConstructorCall")
	@Test
	public void testEquals() {
		assertTrue(StringUtils.equals(null, null));
		assertTrue(StringUtils.equals("", ""));
		assertTrue(StringUtils.equals(new String(), new String()));
		assertTrue(StringUtils.equals(null, ""));
		assertTrue(StringUtils.equals("", null));
		assertTrue(StringUtils.equals(null, new String()));
		assertTrue(StringUtils.equals(new String(), null));
		assertTrue(StringUtils.equals(null, new String()));
		assertTrue(StringUtils.equals(new String(), null));
		
		assertFalse(StringUtils.equals("something", null));
		assertFalse(StringUtils.equals("something", ""));
		assertFalse(StringUtils.equals("something", new String()));
		assertFalse(StringUtils.equals(null, "something"));
		assertFalse(StringUtils.equals("", "something"));
		assertFalse(StringUtils.equals(new String(), "something"));
	}

}
