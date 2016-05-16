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

import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@murrayc.com>
 *
 */
public class LayoutPathTest {

	@Test
	public void test() {
		final String layoutPathExpected = "1:2:3:4";
		final int[] indicesExpected = {1, 2, 3, 4};

		final int[] indices = Utils.parseLayoutPath(layoutPathExpected);
		assertArrayEquals(indicesExpected, indices);

		final String layoutPath = Utils.buildLayoutPath(indicesExpected);
		assertEquals(layoutPathExpected, layoutPath);
	}


}
