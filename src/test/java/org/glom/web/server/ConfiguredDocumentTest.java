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
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.server.libglom.DocumentTest;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.DocumentInfo;
import org.glom.web.shared.Reports;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Ben Konrath <ben@bagu.org>
 *
 */
public class ConfiguredDocumentTest {

	private static ConfiguredDocument configuredDoc;
	private static Document document;
	
	@BeforeClass
	static public void setUp() throws PropertyVetoException {
		URL url = DocumentTest.class.getResource("example_music_collection.glom");
		assertTrue(url != null);
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
		Assert.assertNotNull(configuredDoc.getDocument());
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#isAuthenticated()}.
	 */
	@Test
	public void testIsAuthenticated() {
		Assert.assertFalse(configuredDoc.isAuthenticated());
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getDocumentID()}.
	 */
	@Test
	public void testGetDocumentID() {
		//This is a simple setter/getter:
		final String docID = "somethingOrOther";
		configuredDoc.setDocumentID(docID);
		Assert.assertEquals(docID, configuredDoc.getDocumentID());
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getDefaultLocaleID()}.
	 */
	@Test
	public void testGetDefaultLocaleID() {
		Assert.assertEquals("", configuredDoc.getDefaultLocaleID());
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getDocumentInfo(java.lang.String)}.
	 */
	@Test
	public void testGetDocumentInfo() {
		final DocumentInfo docInfo = configuredDoc.getDocumentInfo("");
		Assert.assertNotNull(docInfo);
	}

	/* This requires a database connection:
	@Test
	public void testGetListViewData() {
		final ArrayList<DataItem[]> list = configuredDoc.getListViewData("albums", "", 0, 10, false, 0, false);
		Assert.assertNotNull(list);
		Assert.assertEquals(10, list.size());
		Assert.assertNotNull(list.get(0));
		//TODO: test more details.
	}
	*/

	/* This requires a database connection:
	@Test
	public void testGetDetailsData() {
		final TypedDataItem primaryKeyValue = new TypedDataItem();
		primaryKeyValue.setNumber(1);
		final DataItem[] data = configuredDoc.getDetailsData("albums", primaryKeyValue);
		Assert.assertNotNull(data);
		Assert.assertNotNull(data[0]);
		//TODO: test more details.
	}
	*/

	/* This requires a database connection:
	@Test
	public void testGetRelatedListData() {
		//TODO: final ArrayList<DataItem[]> list = configuredDoc.getRelatedListData(tableName, portal, foreignKeyValue, start, length, sortColumnIndex, isAscending)
	}
	*/

	/* This requires a database connection (to update the portals details):
	@Test
	public void testGetDetailsLayoutGroup() {;
		final List<LayoutGroup> list = configuredDoc.getDetailsLayoutGroup("albums", "");
		Assert.assertNotNull(list);
		Assert.assertNotNull(list.get(0));
		//TODO: test more details.
	}
	*/

	/* This requires a database connection:
	@Test
	public void testGetRelatedListRowCount() {
		final TypedDataItem foreignKeyValue = new TypedDataItem();
		primaryKeyValue.setNumber(1);
		configuredDoc.getRelatedListRowCount(tableName, portal, foreignKeyValue);
	}
	*/

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getSuitableRecordToViewDetails(java.lang.String, org.glom.web.shared.libglom.layout.LayoutItemPortal, org.glom.web.shared.TypedDataItem)}.
	 */
	@Test
	public void testGetSuitableRecordToViewDetails() {
		//TODO: final NavigationRecord navRecord = configuredDoc.getSuitableRecordToViewDetails(tableName, portal, primaryKeyValue);
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getListViewLayoutGroup(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetListViewLayoutGroup() {
		final LayoutGroup group = configuredDoc.getListViewLayoutGroup("albums", "");
		Assert.assertNotNull(group);
		Assert.assertNotNull(group.getItems());
		Assert.assertEquals(8, group.getItems().size());
		//TODO: test more details.
	}

	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getReports(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetReports() {
		final Reports reports = configuredDoc.getReports("albums", "");
		Assert.assertNotNull(reports);
		//TODO: test more details.
	}

}
