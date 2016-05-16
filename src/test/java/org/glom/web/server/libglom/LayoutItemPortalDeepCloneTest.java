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

import static org.junit.Assert.*;

import org.glom.web.server.Utils;
import org.glom.web.shared.libglom.Relationship;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class LayoutItemPortalDeepCloneTest {

	@Test
	public void test() {
		final String testTableFrom = "testTableFrom";
		final String testFieldFrom = "testFieldFrom";
		final String testTableTo = "testTableTo";
		final String testFieldTo = "testFieldTo";

		final Relationship relationship = new Relationship();
		relationship.setFromTable(testTableFrom);
		relationship.setFromField(testFieldFrom);
		relationship.setToTable(testTableTo);
		relationship.setToField(testFieldTo);

		final LayoutItemPortal portal = new LayoutItemPortal();
		portal.setRelationship(relationship);

		LayoutItemField item = new LayoutItemField();
		item.setName("testfield1");
		portal.addItem(item);
		item = new LayoutItemField();
		item.setName("testfield2");
		portal.addItem(item);

		final LayoutItemPortal clonePortal = (LayoutItemPortal) Utils.deepCopy(portal);
		assertTrue(clonePortal != null);
		final Relationship cloneRelationship = clonePortal.getRelationship();
		assertTrue(cloneRelationship != null);
		assertEquals(cloneRelationship.getFromTable(), testTableFrom);
		assertEquals(cloneRelationship.getFromField(), testFieldFrom);
		assertEquals(cloneRelationship.getToTable(), testTableTo);
		assertEquals(cloneRelationship.getToField(), testFieldTo);
	}
}
