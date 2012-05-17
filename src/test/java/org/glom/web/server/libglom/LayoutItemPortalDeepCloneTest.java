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

package org.glom.web.server.libglom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.glom.web.server.Utils;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;
import org.glom.web.shared.libglom.layout.TableToViewDetails;
import org.glom.web.shared.libglom.layout.UsesRelationshipImpl;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 * 
 */
public class LayoutItemPortalDeepCloneTest {

	@Test
	public void test() {
		// This seems to fail when we use it in OnlineGlomServiceImpl,
		// but it works here:
		final LayoutItemPortal portal = new LayoutItemPortal();
		final TableToViewDetails viewDetails = new TableToViewDetails();
		final String testTableName = "sometable";
		viewDetails.tableName = testTableName;
		viewDetails.usesRelationship = new UsesRelationshipImpl();
		portal.setNavigationTable(viewDetails);

		final LayoutItemPortal clone = (LayoutItemPortal) Utils.deepCopy(portal);
		assertTrue(clone != null);
		final TableToViewDetails cloneViewDetails = clone.getNavigationTable();
		assertTrue(cloneViewDetails != null);
		assertEquals(cloneViewDetails.tableName, testTableName);
		assertTrue(cloneViewDetails.usesRelationship != null);
	}
}
