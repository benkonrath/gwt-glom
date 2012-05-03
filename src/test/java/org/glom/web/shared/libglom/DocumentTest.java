/*
 * Copyright (C) 2009, 2010, 2011 Openismus GmbH
 *
 * This file is part of Java-libglom.
 *
 * Java-libglom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Java-libglom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java-libglom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.web.shared.libglom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.glom.web.shared.libglom.Document;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemNotebook;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;
import org.glom.web.shared.libglom.layout.SortClause;
import org.glom.web.shared.libglom.layout.reportparts.LayoutItemGroupBy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Simple test to ensure that the generated bindings are working.
 */
public class DocumentTest {

	private static Document document;
	private static String locale = ""; //This means the original locale.
	final static String testUriMusicCollection = "src/test/java/org/glom/web/shared/libglom/example_music_collection.glom";
	final static String testUriFilmManager = "src/test/java/org/glom/web/shared/libglom/example_film_manager.glom";

	@BeforeClass
	static public void setUp() {
		document = new Document();
		document.set_file_uri(testUriMusicCollection);
		int error = 0;
		boolean retval = document.load(error);
		assertTrue(retval);
		assertEquals(error, 0);
	}

	@AfterClass
	static public void tearDown() {
	}

	@Test
	public void testDocumentInfo() {
		assertThat(document.get_database_title_original(), is("Music Collection"));
		assertThat(document.get_default_table(), is("artists"));
	}

	@Test
	public void testReadTableNames() {
		List<String> tableNames = document.get_table_names();
		assertEquals(4, tableNames.size());

		String tables = tableNames.get(0);
		for (int i = 1; i < tableNames.size(); i++) {
			tables += ", " + tableNames.get(i);
		}
		assertThat(tables, is("artists, albums, songs, publishers"));
	}

	@Test
	public void testReadTableFieldSizes() {

		List<Field> fields = document.get_table_fields("albums");
		assertEquals(6, fields.size());

		Field field = fields.get(0);
		String titles = field.get_title_or_name(locale);
		for (int i = 1; i < fields.size(); i++) {
			field = fields.get(i);
			titles += ", " + field.get_title_or_name(locale);
		}

		//TODO: The sequence is not important. It's only important that they are all there.
		assertThat(titles, is("Publisher ID, Artist ID, Album ID, Name, Year, Comments"));

		fields = document.get_table_fields("artists");
		assertEquals(4, fields.size());

		field = fields.get(0);
		titles = field.get_title_or_name(locale);
		for (int i = 1; i < fields.size(); i++) {
			field = fields.get(i);
			titles += ", " + field.get_title_or_name(locale);
		}

		//TODO: The sequence is not important. It's only important that they are all there.
		assertThat(titles, is("Artist ID, Name, Description, Comments"));

		fields = document.get_table_fields("publishers");
		assertEquals(3, fields.size());

		field = fields.get(0);
		titles = field.get_title_or_name(locale);
		for (int i = 1; i < fields.size(); i++) {
			field = fields.get(i);
			titles += ", " + field.get_title_or_name(locale);
		}

		//TODO: The sequence is not important. It's only important that they are all there.
		assertThat(titles, is("Name, Publisher ID, Comments"));

		fields = document.get_table_fields("songs");
		assertEquals(4, fields.size());

		field = fields.get(0);
		titles = field.get_title_or_name(locale);
		for (int i = 1; i < fields.size(); i++) {
			field = fields.get(i);
			titles += ", " + field.get_title_or_name(locale);
		}

		//TODO: The sequence is not important. It's only important that they are all there.
		assertThat(titles, is("Song ID, Album ID, Name, Comments"));
	}

	@Test
	public void testReadLayoutListInfo() {
		String[] tables = { "albums", "artists", "publishers", "songs" };
		int[] sortClauseSizes = { 0, 1, 1, 1 };
		int[] layoutFieldSizes = { 7, 4, 3, 4 };

		for (int i = 0; i < tables.length; i++) {
			List<LayoutGroup> layoutList = document.get_data_layout_groups("list", tables[i]);
			assertTrue(!layoutList.isEmpty());
			List<LayoutItem> layoutItems = layoutList.get(0).get_items();
			List<LayoutItemField> layoutFields = new ArrayList<LayoutItemField>();
			SortClause sortClause = new SortClause(); //TODO: Why use a SortClause instead of a List?
			int numItems = safeLongToInt(layoutItems.size());
			for (int j = 0; j < numItems; j++) {
				LayoutItem item = layoutItems.get(j);
				
				if(item instanceof LayoutItemField ) {
					LayoutItemField field = (LayoutItemField)item;
					layoutFields.add(field);
					Field details = field.get_full_field_details();
					if (details != null && details.get_primary_key()) { 
						sortClause.add(new SortClause.SortField(field, true)); // ascending
					}
				}
			}
			assertEquals(sortClauseSizes[i], sortClause.size());
			assertEquals(layoutFieldSizes[i], safeLongToInt(layoutFields.size()));
		}
	}

	/*
	 * This tests if getting values from a NumericFormat object is working. This test was failing with a JVM crash when
	 * using the glom_sharedptr macro with Glom::UsesRelationship and Glom::Formatting.
	 */
	@Test
	public void testGetNumericFormat() {
		List<String> tableNames = document.get_table_names();

		for (int i = 0; i < tableNames.size(); i++) {
			String table = tableNames.get(i);
			List<LayoutGroup> layoutList = document.get_data_layout_groups("list", table);
			assertTrue(!layoutList.isEmpty());
			List<LayoutItem> layoutItems = layoutList.get(0).get_items();
			int numItems = safeLongToInt(layoutItems.size());
			for (int j = 0; j < numItems; j++) {
				LayoutItem item = layoutItems.get(j);
				assertTrue(item != null);
				
				if(item instanceof LayoutItemField) {
					final LayoutItemField item_field = (LayoutItemField)item;
					// don't keep a reference to the FeildFormatting object
					NumericFormat numFormat = item_field.get_formatting_used().getNumericFormat();
					assertTrue(numFormat != null);

					// get the values
					boolean altForegroundColorForNegatives = numFormat.getUseAltForegroundColorForNegatives();
					String currencySymbol = numFormat.getCurrencySymbol();
					long decimalPlaces = numFormat.getDecimalPlaces();
					boolean decimalPlacesRestricted = numFormat.getDecimalPlacesRestricted();
					boolean useThousandsSepator = numFormat.getUseThousandsSeparator();
					String alternativeColorForNegatives = NumericFormat.getAlternativeColorForNegatives();
					long defaultPrecision = NumericFormat.getDefaultPrecision();

					// Simulate a garbage collection
					System.gc();
					System.runFinalization();

					// re-get the values and test
					assertEquals(altForegroundColorForNegatives, numFormat.getUseAltForegroundColorForNegatives());
					assertEquals(currencySymbol, numFormat.getCurrencySymbol());
					assertEquals(decimalPlaces, numFormat.getDecimalPlaces());
					assertEquals(decimalPlacesRestricted, numFormat.getDecimalPlacesRestricted());
					assertEquals(useThousandsSepator, numFormat.getUseThousandsSeparator());
					assertEquals(alternativeColorForNegatives, NumericFormat.getAlternativeColorForNegatives());
					assertEquals(defaultPrecision, NumericFormat.getDefaultPrecision());

				}
			}
		}
	}

	/*
	 * A smoke test for the methods added to LayoutItemField for accessing methods in Glom::UsesRelationship.
	 */
	@Test
	public void testUsesRelationshipMethods() {
		String table = "albums";
		List<LayoutGroup> layoutList = document.get_data_layout_groups("list", table);
		List<LayoutItem> layoutItems = layoutList.get(0).get_items();

		String names = null, hasRelationshipNames = null, tablesUsed = null;
		final LayoutItem firstItem = layoutItems.get(0);
		
		if(firstItem instanceof LayoutItemField) {
			LayoutItemField firstItemField = (LayoutItemField)firstItem;
			names = firstItemField.get_name();
			hasRelationshipNames = "" + firstItemField.getHasRelationshipName();
			tablesUsed = firstItemField.get_table_used(table);
		}
		int numItems = safeLongToInt(layoutItems.size());
		for (int j = 1; j < numItems; j++) {
			LayoutItem item = layoutItems.get(j);

			if(item instanceof LayoutItemField) {
				LayoutItemField itemField = (LayoutItemField)item;
				names += ", " + itemField.get_name();
				hasRelationshipNames += ", " + itemField.getHasRelationshipName();
				tablesUsed += ", " + itemField.get_table_used(table);
			}
		}
		assertEquals("name, year, artist_id, name, publisher_id, name, comments", names);
		assertEquals("false, false, false, true, false, true, false", hasRelationshipNames);
		assertEquals("albums, albums, albums, artists, albums, publishers, albums", tablesUsed);
	}

	@Test
	public void testGetSuitableTableToViewDetails() {

		// Create a new document for the film manager
		Document filmManagerDocument = new Document();
		filmManagerDocument.set_file_uri(testUriFilmManager);
		int error = 0;
		boolean retval = filmManagerDocument.load(error);
		assertTrue(retval);
		assertEquals(error, 0);

		// Get the "Scene Cast" related list portal. This relies on specific details of the film manager details
		// view layout. I've included safety checks that will fail if the layout changes.
		List<LayoutGroup> detailsLayout = filmManagerDocument.get_data_layout_groups("details", "scenes");
		assertEquals(3, detailsLayout.size());

		LayoutGroup layoutGroup = detailsLayout.get(1);
		assertEquals("details", layoutGroup.get_name());

		layoutGroup = detailsLayout.get(2);
		assertEquals("details_lower", layoutGroup.get_name());

		List<LayoutItem> items = layoutGroup.get_items();
		assertEquals(2, items.size());

		LayoutItem notebookItem = items.get(0);
		assertEquals("notebook", notebookItem.get_name());
		assertTrue(notebookItem instanceof LayoutItemNotebook);
		LayoutItemNotebook notebook = (LayoutItemNotebook)notebookItem;
		items = notebook.get_items();
		assertEquals(7, items.size());
		LayoutItem portalItem = items.get(0);
		assertTrue(portalItem instanceof LayoutItemPortal);
		LayoutItemPortal portal = (LayoutItemPortal)portalItem;
		assertTrue(portal != null);
		
		assertEquals(portal.getRelationshipNameUsed(), "scene_cast");

		// call get_suitable_table_to_view_details
		final LayoutItemPortal.TableToViewDetails viewDetails = portal.get_suitable_table_to_view_details(filmManagerDocument);
		assertTrue(viewDetails != null);

		// Simulate a garbage collection
		System.gc();
		System.runFinalization();

		// Check if things are working like we expect
		assertEquals("characters", viewDetails.tableName);
		assertTrue(viewDetails.usesRelationship != null);
		Relationship relationship = viewDetails.usesRelationship.getRelationship();
		assertTrue(relationship != null);
		assertEquals("cast", relationship.get_name());
		assertTrue(viewDetails.usesRelationship.getRelatedRelationship() == null);

	}

	@Test
	public void testReadReportNames() {
		List<String> reportNames = document.get_report_names("albums");
		assertEquals(1, reportNames.size()); //TODO: Test something with more reports.

		String reports = reportNames.get(0);
		for (int i = 1; i < reportNames.size(); i++) {
			reports += ", " + reportNames.get(i);
		}
		assertThat(reports, is("albums_by_artist"));
	}

	@Test
	public void testReadReportStructure() {
		Report report = document.get_report("albums", "albums_by_artist");
		assertTrue(report != null);
		LayoutGroup layoutGroup = report.get_layout_group();
		assertTrue(layoutGroup != null);
		List<LayoutItem> layoutItems = layoutGroup.get_items();
		final int numItems = safeLongToInt(layoutItems.size());
		assertEquals(1, numItems);

		LayoutItem layoutItem = layoutItems.get(0);
		assertTrue(layoutItem != null);
		LayoutGroup asGroup = (LayoutGroup)layoutItem;
		assertTrue(asGroup != null);
		LayoutItemGroupBy groupby = (LayoutItemGroupBy)layoutItem;
		assertTrue(groupby != null);

		assertTrue(groupby.get_has_field_group_by());
		LayoutItemField fieldGroupBy = groupby.get_field_group_by();
		assertTrue(fieldGroupBy != null);
		assertThat(fieldGroupBy.get_name(), is("artist_id"));

		LayoutGroup groupSecondaries = groupby.get_secondary_fields();
		assertTrue(groupSecondaries != null);

		List<LayoutItem> innerItems = groupby.get_items();
		assertTrue(innerItems != null);
		final int numInnerItems = safeLongToInt(innerItems.size());
		assertEquals(2, numInnerItems);

		layoutItem = innerItems.get(0);
		assertTrue(layoutItem != null);
		assertTrue(layoutItem instanceof LayoutItemField);
		LayoutItemField field = (LayoutItemField)layoutItem;
		assertTrue(field != null);
		assertThat(field.get_name(), is("name"));
		assertThat(field.get_glom_type(), is(Field.GlomFieldType.TYPE_TEXT));

		layoutItem = innerItems.get(1);
		assertTrue(layoutItem != null);
		assertTrue(layoutItem instanceof LayoutItemField);
		field = (LayoutItemField)layoutItem;
		assertTrue(field != null);
		assertThat(field.get_name(), is("year"));
		assertThat(field.get_glom_type(), is(Field.GlomFieldType.TYPE_NUMERIC));
	}

	// Test thread class that runs all the tests.
	private class TestThread implements Runnable {

		@Override
		public void run() {
			for (int i = 0; i < 20; i++) {
				testDocumentInfo();
				testGetNumericFormat();
				testGetSuitableTableToViewDetails();
				testReadLayoutListInfo();
				testReadTableFieldSizes();
				testReadTableNames();
				testUsesRelationshipMethods();
			}
		}
	}

	/*
	 * Tests threaded access to java-libglom.
	 */
	@Test
	public void testThreadedAccess() throws InterruptedException {
		// create the threads
		Thread thread1 = new Thread(new TestThread());
		Thread thread2 = new Thread(new TestThread());
		Thread thread3 = new Thread(new TestThread());
		Thread thread4 = new Thread(new TestThread());

		// start the threads
		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();

		// wait for the treads to finish
		try {
			thread1.join();
		} catch (InterruptedException e) {
			System.out.println("Thread 1 had a problem finishing. " + e);
			throw e;
		}

		try {
			thread2.join();
		} catch (InterruptedException e) {
			System.out.println("Thread 2 had a problem finishing. " + e);
			throw e;
		}

		try {
			thread3.join();
		} catch (InterruptedException e) {
			System.out.println("Thread 3 had a problem finishing. " + e);
			throw e;
		}

		try {
			thread4.join();
		} catch (InterruptedException e) {
			System.out.println("Thread 4 had a problem finishing. " + e);
			throw e;
		}
	}

	/*
	 * This method safely converts longs from libglom into ints. This method was taken from stackoverflow:
	 * 
	 * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
	 */
	private static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

}
