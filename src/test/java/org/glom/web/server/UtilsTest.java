/*
 * Copyright (C) 2012 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.web.server;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@murrayc.com>
 *
 */
public class UtilsTest {

	/**
	 * Test method for {@link org.glom.web.server.Utils#buildImageDataUrl(java.lang.String, java.lang.String, java.lang.String, int[])}.
	 */
	@Test
	public void testBuildImageDataUrl() {
		final int path[] = {0, 1};
		final String url = Utils.buildImageDataUrl("some-document-id", "some-table-name", "details", path);
		assertNotNull(url);
		assertFalse(StringUtils.isEmpty(url));

	}

	/**
	 * Test method for {@link org.glom.web.server.Utils#buildImageDataUrl(org.glom.web.shared.TypedDataItem, java.lang.String, java.lang.String, org.glom.web.shared.libglom.layout.LayoutItemField)}.
	 */
	@Test
	public void testBuildImageDataUrlTypedDataItem() {
		final TypedDataItem primaryKey = new TypedDataItem();
		primaryKey.setNumber(1);
		final LayoutItemField field = new LayoutItemField();
		field.setName("some-image-field");
		final String url = Utils.buildImageDataUrl(primaryKey, "some-document-id", "some-table-name", field);
		assertNotNull(url);
		assertFalse(StringUtils.isEmpty(url));
	}
}
