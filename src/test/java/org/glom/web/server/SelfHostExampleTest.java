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

import java.net.URL;
import java.sql.SQLException;

import org.glom.web.server.libglom.Document;
import org.glom.web.server.libglom.Document.HostingMode;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 * 
 */
public class SelfHostExampleTest {

	private static SelfHosterPostgreSQL selfHosterPostgreSQL = null;

	@Test
	public void test() throws SQLException {
		final URL url = SelfHostExampleTest.class.getResource("example_music_collection.glom");
		assertTrue(url != null);
		final String strUri = url.toString();

		final Document document = new Document();
		document.setFileURI(strUri);
		assertTrue(document.load());

		selfHosterPostgreSQL = new SelfHosterPostgreSQL(document);
		final boolean hosted = selfHosterPostgreSQL.createAndSelfHostFromExample(HostingMode.HOSTING_MODE_POSTGRES_SELF);
		assertTrue(hosted);
		
		SelfHostTestUtils.testExampleMusiccollectionData(selfHosterPostgreSQL, document);
	}

	@AfterClass
	public static void tearDown() {
		if (selfHosterPostgreSQL != null) {
			selfHosterPostgreSQL.cleanup();
		}
	}
}
