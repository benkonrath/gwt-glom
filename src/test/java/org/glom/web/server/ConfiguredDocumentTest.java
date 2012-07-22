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

import java.beans.PropertyVetoException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.server.libglom.DocumentTest;
import org.glom.web.shared.DocumentInfo;
import org.glom.web.shared.Reports;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class ConfiguredDocumentTest {

	private static ConfiguredDocument configuredDoc;
	private static Document document;
	private static String defaultLocale = "";
	private static String germanLocale = "de";
	
	@BeforeClass
	static public void setUp() throws PropertyVetoException {
		URL url = DocumentTest.class.getResource("example_music_collection.glom");
		assertNotNull(url);
		final String testUriMusicCollection = url.toString();
		assertTrue(!StringUtils.isEmpty(testUriMusicCollection));

		document = new Document();
		document.setFileURI(testUriMusicCollection);
		final boolean retval = document.load();
		assertTrue(retval);
		
		configuredDoc = new ConfiguredDocument(document);
	}

	@AfterClass
	static public void tearDown() {
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getDocument()}.
	 */
	@Test
	public void testGetDocument() {
		assertNotNull(configuredDoc.getDocument());
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#isAuthenticated()}.
	 */
	@Test
	public void testIsAuthenticated() {
		assertFalse(configuredDoc.isAuthenticated());
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getDocumentID()}.
	 */
	@Test
	public void testGetDocumentID() {
		//This is a simple setter/getter:
		final String docID = "somethingOrOther";
		configuredDoc.setDocumentID(docID);
		assertEquals(docID, configuredDoc.getDocumentID());
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getDefaultLocaleID()}.
	 */
	@Test
	public void testGetDefaultLocaleID() {
		assertEquals("", configuredDoc.getDefaultLocaleID());
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getDocumentInfo(java.lang.String)}.
	 */
	@Test
	public void testGetDocumentInfo() {
		//Default locale:
		DocumentInfo docInfo = configuredDoc.getDocumentInfo(defaultLocale);
		assertNotNull(docInfo);
		assertEquals(docInfo.getTitle(), "Music Collection");
		
		//Other locale:
		docInfo = configuredDoc.getDocumentInfo(germanLocale);
		assertNotNull(docInfo);
		assertEquals(docInfo.getTitle(), "Musiksammlung");
		
		//Invalid locale, which should use the default one:
		docInfo = configuredDoc.getDocumentInfo("someinvalidlocale");
		assertNotNull(docInfo);
		assertEquals(docInfo.getTitle(), "Music Collection");
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getSuitableRecordToViewDetails(java.lang.String, org.glom.web.shared.libglom.layout.LayoutItemPortal, org.glom.web.shared.TypedDataItem)}.
	 */
	@Test
	public void testGetSuitableRecordToViewDetails() {
		//TODO: final NavigationRecord navRecord = configuredDoc.getSuitableRecordToViewDetails(tableName, portal, primaryKeyValue);
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getReports(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetReports() {
		final Reports reports = configuredDoc.getReports("albums", defaultLocale);
		assertNotNull(reports);
		//TODO: test more details.
	}

}
