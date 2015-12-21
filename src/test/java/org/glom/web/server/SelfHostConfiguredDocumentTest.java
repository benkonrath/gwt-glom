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

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.server.libglom.DocumentTest;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class SelfHostConfiguredDocumentTest {

	private static SelfHosterPostgreSQL selfHosterPostgreSQL = null;
	private static ConfiguredDocument configuredDoc;
	private static ComboPooledDataSource authenticatedConnection;
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

		selfHosterPostgreSQL = new SelfHosterPostgreSQL(document);
		final boolean hosted = selfHosterPostgreSQL.createAndSelfHostFromExample();
		assertTrue(hosted);
		
		configuredDoc = new ConfiguredDocument(document);
		authenticatedConnection = SqlUtils.tryUsernameAndPassword(document, selfHosterPostgreSQL.getUsername(), selfHosterPostgreSQL.getPassword());
		assertNotNull(authenticatedConnection);
	}

	@AfterClass
	static public void tearDown() {
		if (selfHosterPostgreSQL != null) {
			selfHosterPostgreSQL.cleanup();
		}
	}
	
	private void testGetListViewLayoutGroup(final String locale, final String field0Title, final String field1Title) {
		final LayoutGroup group = configuredDoc.getListViewLayoutGroup("albums", defaultLocale);
		assertNotNull(group);
		
		List<LayoutItem> items = group.getItems();
		assertNotNull(items);
		assertEquals(8, items.size());
		
		LayoutItem item = items.get(0);
		assertTrue(item instanceof LayoutItemField);
		
		assertEquals("name", item.getName());
		assertEquals(field0Title, item.getTitle());
		
		item = items.get(1);
		assertTrue(item instanceof LayoutItemField);
		
		assertEquals("year", item.getName());
		assertEquals(field1Title, item.getTitle());
	}
	
	/**
	 * Test method for {@link org.glom.web.server.ConfiguredDocument#getListViewLayoutGroup(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetListViewLayoutGroup() {
		testGetListViewLayoutGroup(defaultLocale, "Name", "Year");
		testGetListViewLayoutGroup(germanLocale, "Name", "Year");
	}

	@Test
	public void testGetListViewData() {
		ArrayList<DataItem[]> list = configuredDoc.getListViewData(authenticatedConnection, "albums", defaultLocale, 0, 10, false, 0, false);
		assertNotNull(list);
		assertEquals(5, list.size());
		
		DataItem[] data = list.get(2);
		assertNotNull(data);
		assertEquals(8, data.length);
		
		DataItem dataItem = data[0];
		assertNotNull(dataItem);
		assertEquals("The Wild, the Innocent, & the E-Street Shuffle", dataItem.getText());
		
		dataItem = data[1];
		assertEquals(1973.0, dataItem.getNumber(), 0);
		
		dataItem = data[2];
		assertEquals(0.0, dataItem.getNumber(), 0);
		
		dataItem = data[3];
		assertEquals("Bruce Springsteen", dataItem.getText());
		
		dataItem = data[4];
		assertEquals(0.0, dataItem.getNumber(), 0);
		
		dataItem = data[5];
		assertEquals("Sony", dataItem.getText());
		
		dataItem = data[6];
		assertEquals("", dataItem.getText());
		
		dataItem = data[7];
		assertEquals(2.0, dataItem.getNumber(), 0);
	}

	@Test
	public void testGetDetailsData() {
		final TypedDataItem primaryKeyValue = new TypedDataItem();
		primaryKeyValue.setNumber(1);
		final DataItem[] data = configuredDoc.getDetailsData(authenticatedConnection, "albums", primaryKeyValue);
		assertNotNull(data);
		assertEquals(8, data.length);
		
		DataItem dataItem = data[0];
		assertNotNull(dataItem);
		assertEquals(1.0, dataItem.getNumber(), 0);
		
		dataItem = data[1];
		assertEquals("True Blue", dataItem.getText());
		
		dataItem = data[2];
		assertEquals(1.0, dataItem.getNumber(), 0);
		
		dataItem = data[3];
		assertEquals("Madonna", dataItem.getText());
		
		dataItem = data[4];
		assertEquals(1.0, dataItem.getNumber(), 0);
		
		dataItem = data[5];
		assertEquals("Warner Bros", dataItem.getText());
		
		dataItem = data[6];
		assertEquals(1987.0, dataItem.getNumber(), 0);
		
		dataItem = data[7];
		assertEquals("", dataItem.getText());
	}

	@Test
	public void testGetRelatedListData() {
		//TODO: final ArrayList<DataItem[]> list = configuredDoc.getRelatedListData(tableName, portal, foreignKeyValue, start, length, sortColumnIndex, isAscending)
	}

	private void testGetDetailsLayoutGroup(final String locale, final String detailsTitle) {;
		final List<LayoutGroup> list = configuredDoc.getDetailsLayoutGroup("albums", locale);
		assertNotNull(list);
		assertEquals(2, list.size());
		assertNotNull(list.get(0));
		
		LayoutGroup layoutGroup = list.get(1);
		assertNotNull(layoutGroup);
		assertEquals(Document.LAYOUT_NAME_DETAILS, layoutGroup.getName());
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
