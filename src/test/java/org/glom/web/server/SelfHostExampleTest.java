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
import org.jooq.SQLDialect;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class SelfHostExampleTest {

	private static SelfHoster selfHoster = null;

	@Test
	public void testPostgreSQL() throws SQLException {
		doTest(Document.HostingMode.HOSTING_MODE_POSTGRES_SELF);
	}

	@Test
	public void testMySQL() throws SQLException {
		doTest(Document.HostingMode.HOSTING_MODE_MYSQL_SELF);
	}

	/* This is really a test of our test utility code. */
	@Test
	public void testSelfHosterEscapeIDSame() {
		final String id = "something";
		assertEquals("\"" + id + "\"", SelfHosterMySQL.quoteAndEscapeSqlId(id, SQLDialect.POSTGRES));
		assertEquals("`" + id + "`", SelfHosterMySQL.quoteAndEscapeSqlId(id, SQLDialect.MYSQL));
	}

	/* This is really a test of our test utility code. */
	@Test
	public void testSelfHosterEscapeIDNotSame() {
		final String id = "something with a \" and a ` char";
		assertNotEquals("\"" + id + "\"", SelfHosterMySQL.quoteAndEscapeSqlId(id, SQLDialect.POSTGRES));
		assertNotEquals("`" + id + "`", SelfHosterMySQL.quoteAndEscapeSqlId(id, SQLDialect.MYSQL));
	}

	/**
	 * @param hostingMode
	 * @throws SQLException
	 */
	private void doTest(Document.HostingMode hostingMode) throws SQLException {
		final URL url = SelfHostExampleTest.class.getResource("example_music_collection.glom");
		assertTrue(url != null);
		final String strUri = url.toString();

		final Document document = new Document();
		document.setFileURI(strUri);
		assertTrue(document.load());

		if (hostingMode == Document.HostingMode.HOSTING_MODE_POSTGRES_SELF) {
			selfHoster = new SelfHosterPostgreSQL(document);
		} else if (hostingMode == Document.HostingMode.HOSTING_MODE_MYSQL_SELF) {
			selfHoster = new SelfHosterMySQL(document);
		} else {
			// TODO: std::cerr << G_STRFUNC << ": This test function does not support the specified hosting_mode: " <<
			// hosting_mode << std::endl;
			assert false;
		}

		final boolean hosted = selfHoster.createAndSelfHostFromExample();
		assertTrue(hosted);

		SelfHostTestUtils.testExampleMusiccollectionData(selfHoster, document);

		if (selfHoster != null) {
			selfHoster.cleanup();
		}
	}

	@AfterClass
	public static void tearDown() {
		if (selfHoster != null) {
			selfHoster.cleanup();
		}
	}
}
