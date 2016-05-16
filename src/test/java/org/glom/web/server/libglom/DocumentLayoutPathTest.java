/*
 * Copyright (C) 2009, 2010, 2011 Openismus GmbH
 *
 * This file is part of gwt-glom
 *
 * gwt-glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * gwt-glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gwt-glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.web.server.libglom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Simple test to ensure that the generated bindings are working.
 */
public class DocumentLayoutPathTest {

	private static Document document;

	@BeforeClass
	static public void setUp() {
		final URL url = DocumentLayoutPathTest.class.getResource("example_music_collection.glom");
		assertTrue(url != null);
		final String documentUri= url.toString();
		assertTrue(!StringUtils.isEmpty(documentUri));

		document = new Document();
		document.setFileURI(documentUri);
		final boolean retval = document.load();
		assertTrue(retval);
	}

	@AfterClass
	static public void tearDown() {
	}

	@Test
	public void testNormal() throws IOException {
		// Just an initial sanity check:
		assertThat(document.getDatabaseTitleOriginal(), is("Music Collection"));

		final String layoutPath = "1:2";
		final LayoutItem item = document.getLayoutItemByPath("artists", Document.LAYOUT_NAME_DETAILS, layoutPath);
		assertNotNull(item);
		assertTrue(item instanceof LayoutItemField);

		assertEquals(item.getName(), "comments");
	}

	@Test
	public void testOutOfBounds() throws IOException {
		final String layoutPath = "1:200"; //Check that it does not crash.
		final LayoutItem item = document.getLayoutItemByPath("artists", Document.LAYOUT_NAME_DETAILS, layoutPath);
		assertNull(item);
	}

	@Test
	public void testOutOfBoundsNegative() throws IOException {
		final String layoutPath = "-1:-50"; //Check that it does not crash.
		final LayoutItem item = document.getLayoutItemByPath("artists", Document.LAYOUT_NAME_DETAILS, layoutPath);
		assertNull(item);
	}

}
