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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.server.libglom.DocumentTest;
import org.glom.web.server.libglom.Document.HostingMode;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class SelfHostConfiguredDocumentTest {

	private static SelfHoster selfHoster = null;
	private static ConfiguredDocument configuredDoc;
	private static Document document;
	private static String defaultLocale = "";
	private static String germanLocale = "de";
	
	@BeforeClass
	static public void setUp() throws PropertyVetoException, SQLException {
		URL url = DocumentTest.class.getResource("example_music_collection.glom");
		assertTrue(url != null);
		final String testUriMusicCollection = url.toString();
		assertTrue(!StringUtils.isEmpty(testUriMusicCollection));

		document = new Document();
		document.setFileURI(testUriMusicCollection);
		final boolean retval = document.load();
		assertTrue(retval);

		selfHoster = new SelfHoster(document);
		final boolean hosted = selfHoster.createAndSelfHostFromExample(HostingMode.HOSTING_MODE_POSTGRES_SELF);
		assertTrue(hosted);
		
		configuredDoc = new ConfiguredDocument(document);
		configuredDoc.setUsernameAndPassword(selfHoster.getUsername(), selfHoster.getPassword());
		assertTrue(configuredDoc.isAuthenticated());
	}

	@AfterClass
	static public void tearDown() {
		if (selfHoster != null) {
			selfHoster.cleanup();
		}
	}
	

	@Test
	public void testGetListViewData() {
		ArrayList<DataItem[]> list = configuredDoc.getListViewData("albums", defaultLocale, 0, 10, false, 0, false);
		Assert.assertNotNull(list);
		Assert.assertEquals(5, list.size());
		
		DataItem[] data = list.get(2);
		Assert.assertNotNull(data);
		Assert.assertEquals(8, data.length);
		
		DataItem dataItem = data[0];
		Assert.assertNotNull(dataItem);
		Assert.assertEquals("The Wild, the Innocent, & the E-Street Shuffle", dataItem.getText());
		
		dataItem = data[1];
		Assert.assertEquals(1973.0, dataItem.getNumber());
		
		dataItem = data[2];
		Assert.assertEquals(0.0, dataItem.getNumber());
		
		dataItem = data[3];
		Assert.assertEquals("Bruce Springsteen", dataItem.getText());
		
		dataItem = data[4];
		Assert.assertEquals(0.0, dataItem.getNumber());
		
		dataItem = data[5];
		Assert.assertEquals("Sony", dataItem.getText());
		
		dataItem = data[6];
		Assert.assertEquals("", dataItem.getText());
		
		dataItem = data[7];
		Assert.assertEquals(2.0, dataItem.getNumber());
	}

	@Test
	public void testGetDetailsData() {
		final TypedDataItem primaryKeyValue = new TypedDataItem();
		primaryKeyValue.setNumber(1);
		final DataItem[] data = configuredDoc.getDetailsData("albums", primaryKeyValue);
		Assert.assertNotNull(data);
		Assert.assertEquals(8, data.length);
		
		DataItem dataItem = data[0];
		Assert.assertNotNull(dataItem);
		Assert.assertEquals(1.0, dataItem.getNumber());
		
		dataItem = data[1];
		Assert.assertEquals("True Blue", dataItem.getText());
		
		dataItem = data[2];
		Assert.assertEquals(1.0, dataItem.getNumber());
		
		dataItem = data[3];
		Assert.assertEquals("Madonna", dataItem.getText());
		
		dataItem = data[4];
		Assert.assertEquals(1.0, dataItem.getNumber());
		
		dataItem = data[5];
		Assert.assertEquals("Warner Bros", dataItem.getText());
		
		dataItem = data[6];
		Assert.assertEquals(1987.0, dataItem.getNumber());
		
		dataItem = data[7];
		Assert.assertEquals("", dataItem.getText());
	}

	@Test
	public void testGetRelatedListData() {
		//TODO: final ArrayList<DataItem[]> list = configuredDoc.getRelatedListData(tableName, portal, foreignKeyValue, start, length, sortColumnIndex, isAscending)
	}

	public void testGetDetailsLayoutGroup(final String locale, final String detailsTitle) {;
		final List<LayoutGroup> list = configuredDoc.getDetailsLayoutGroup("albums", locale);
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
		Assert.assertNotNull(list.get(0));
		
		LayoutGroup layoutGroup = list.get(1);
		Assert.assertNotNull(layoutGroup);
		assertEquals("details", layoutGroup.getName());
		assertEquals("Details", layoutGroup.getTitle()); //We don't need to specify locale again.
		//assertEquals("Details", layoutGroup.getTitle(germanLocale));
	}
	
	@Test
	public void testGetDetailsLayoutGroup() {;
		testGetDetailsLayoutGroup(defaultLocale, "Details");
		testGetDetailsLayoutGroup(germanLocale, "Details");
	}

	@Test
	public void testGetRelatedListRowCount() {
		//final TypedDataItem foreignKeyValue = new TypedDataItem();
		//foreignKeyValue.setNumber(1);
		//TODO: configuredDoc.getRelatedListRowCount(tableName, portal, foreignKeyValue);
	}
}
