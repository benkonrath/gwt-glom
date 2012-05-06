/*
 * Copyright (C) 2012 Openismus GmbH
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.junit.Test;

public class LayoutItemFieldTest {

	private static String locale = ""; // This means the original locale.

	@Test
	public void test() {
		final LayoutItemField item = new LayoutItemField();
		assertTrue(StringUtils.isEmpty(item.getTitleOriginal()));
		assertTrue(StringUtils.isEmpty(item.getTitle(locale)));

		final String testFieldTitle = "somefieldtitle";
		final Field field = new Field();
		assertTrue(StringUtils.isEmpty(field.getTitleOriginal()));
		assertTrue(StringUtils.isEmpty(field.getTitle(locale)));
		field.setTitleOriginal(testFieldTitle);
		assertEquals(testFieldTitle, field.getTitleOriginal());
		assertEquals(testFieldTitle, field.getTitle(locale));

		// Check that the LayoutItemField's title is retrieved from the field:
		item.setFullFieldDetails(field);
		assertEquals(testFieldTitle, item.getTitleOriginal());
		assertEquals(testFieldTitle, item.getTitle(locale));

		// Check that a custom title is used:
		final String testItemTitle = "someitemtitle";
		item.getCustomTitle().setTitleOriginal(testItemTitle);
		item.getCustomTitle().setUseCustomTitle(true);
		assertEquals(testItemTitle, item.getTitleOriginal());
		assertEquals(testItemTitle, item.getTitle(locale));
	}
}
