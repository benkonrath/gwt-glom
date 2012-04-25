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

import java.util.List;

import org.glom.web.shared.libglom.Document;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.LayoutGroup;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.LayoutItem;
import org.glom.libglom.LayoutItem_GroupBy;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.LayoutItem_Notebook;
import org.glom.libglom.LayoutItem_Portal;
import org.glom.libglom.NumericFormat;
import org.glom.libglom.Relationship;
import org.glom.libglom.SortClause;
import org.glom.libglom.SortFieldPair;
import org.glom.libglom.StringVector;
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
		Glom.libglom_deinit();
	}

	@Test
	public void testDocumentInfo() {
		assertThat(document.get_database_title_original(), is("Music Collection"));
		assertThat(document.get_default_table(), is("artists"));
	}

	@Test
	public void testReadTableNames() {
		StringVector tableNames = document.get_table_names();
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
		assertThat(titles, is("Album ID, Comments, Name, Artist ID, Publisher ID, Year"));

		fields = document.get_table_fields("artists");
		assertEquals(4, fields.size());

		field = fields.get(0);
		titles = field.get_title_or_name(locale);
		for (int i = 1; i < fields.size(); i++) {
			field = fields.get(i);
			titles += ", " + field.get_title_or_name(locale);
		}
		assertThat(titles, is("Artist ID, Description, Comments, Name"));

		fields = document.get_table_fields("publishers");
		assertEquals(3, fields.size());

		field = fields.get(0);
		titles = field.get_title_or_name(locale);
		for (int i = 1; i < fields.size(); i++) {
			field = fields.get(i);
			titles += ", " + field.get_title_or_name(locale);
		}
		assertThat(titles, is("Publisher ID, Comments, Name"));

		fields = document.get_table_fields("songs");
		assertEquals(4, fields.size());

		field = fields.get(0);
		titles = field.get_title_or_name(locale);
		for (int i = 1; i < fields.size(); i++) {
			field = fields.get(i);
			titles += ", " + field.get_title_or_name(locale);
		}
		assertThat(titles, is("Song ID, Comments, Album ID, Name"));
	}

	@Test
	public void testReadLayoutListInfo() {
		String[] tables = { "albums", "artists", "publishers", "songs" };
		int[] sortClauseSizes = { 0, 1, 1, 1 };
		int[] layoutFieldSizes = { 7, 4, 3, 4 };

		for (int i = 0; i < tables.length; i++) {
			LayoutGroupVector layoutList = document.get_data_layout_groups("list", tables[i]);
			LayoutItemVector layoutItems = layoutList.get(0).get_items();
			LayoutFieldVector layoutFields = new LayoutFieldVector();
			SortClause sortClause = new SortClause();
			int numItems = safeLongToInt(layoutItems.size());
			for (int j = 0; j < numItems; j++) {
				LayoutItem item = layoutItems.get(j);
				LayoutItem_Field field = LayoutItem_Field.cast_dynamic(item);
				if (field != null) {
					layoutFields.add(field);
					Field details = field.get_full_field_details();
					if (details != null && details.get_primary_key()) {
						sortClause.add(new SortFieldPair(field, true)); // ascending
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
		StringVector tableNames = document.get_table_names();

		for (int i = 0; i < tableNames.size(); i++) {
			String table = tableNames.get(i);
			LayoutGroupVector layoutList = document.get_data_layout_groups("list", table);
			LayoutItemVector layoutItems = layoutList.get(0).get_items();
			int numItems = safeLongToInt(layoutItems.size());
			for (int j = 0; j < numItems; j++) {
				LayoutItem item = layoutItems.get(j);
				LayoutItem_Field item_field = LayoutItem_Field.cast_dynamic(item);
				if (item_field != null) {
					// don't keep a reference to the FeildFormatting object
					NumericFormat numFormat = item_field.get_formatting_used().get_numeric_format();

					// get the values
					boolean altForegroundColorForNegatives = numFormat.get_alt_foreground_color_for_negatives();
					String currencySymbol = numFormat.get_currency_symbol();
					long decimalPlaces = numFormat.get_decimal_places();
					boolean decimalPlacesRestricted = numFormat.get_decimal_places_restricted();
					boolean useThousandsSepator = numFormat.get_use_thousands_separator();
					String alternativeColorForNegatives = NumericFormat.get_alternative_color_for_negatives();
					long defaultPrecision = NumericFormat.get_default_precision();

					// Simulate a garbage collection
					System.gc();
					System.runFinalization();

					// re-get the values and test
					assertEquals(altForegroundColorForNegatives, numFormat.get_alt_foreground_color_for_negatives());
					assertEquals(currencySymbol, numFormat.get_currency_symbol());
					assertEquals(decimalPlaces, numFormat.get_decimal_places());
					assertEquals(decimalPlacesRestricted, numFormat.get_decimal_places_restricted());
					assertEquals(useThousandsSepator, numFormat.get_use_thousands_separator());
					assertEquals(alternativeColorForNegatives, NumericFormat.get_alternative_color_for_negatives());
					assertEquals(defaultPrecision, NumericFormat.get_default_precision());

				}
			}
		}
	}

	/*
	 * A smoke test for the methods added to LayoutItem_Field for accessing methods in Glom::UsesRelationship.
	 */
	@Test
	public void testUsesRelationshipMethods() {
		String table = "albums";
		LayoutGroupVector layoutList = document.get_data_layout_groups("list", table);
		LayoutItemVector layoutItems = layoutList.get(0).get_items();

		String names = null, hasRelationshipNames = null, tablesUsed = null;
		LayoutItem firstItem = layoutItems.get(0);
		LayoutItem_Field firstItemField = LayoutItem_Field.cast_dynamic(firstItem);
		if (firstItemField != null) {
			names = firstItemField.get_name();
			hasRelationshipNames = "" + firstItemField.get_has_relationship_name();
			tablesUsed = firstItemField.get_table_used(table);
		}
		int numItems = safeLongToInt(layoutItems.size());
		for (int j = 1; j < numItems; j++) {
			LayoutItem item = layoutItems.get(j);
			LayoutItem_Field itemField = LayoutItem_Field.cast_dynamic(item);
			if (itemField != null) {
				names += ", " + itemField.get_name();
				hasRelationshipNames += ", " + itemField.get_has_relationship_name();
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
		LayoutGroupVector detailsLayout = filmManagerDocument.get_data_layout_groups("details", "scenes");
		assertEquals(3, detailsLayout.size());

		LayoutGroup layoutGroup = detailsLayout.get(1);
		assertEquals("details", layoutGroup.get_name());

		layoutGroup = detailsLayout.get(2);
		assertEquals("details_lower", layoutGroup.get_name());

		LayoutItemVector items = layoutGroup.get_items();
		assertEquals(2, items.size());

		LayoutItem notebookItem = items.get(0);
		assertEquals("notebook", notebookItem.get_name());
		LayoutItem_Notebook notebook = LayoutItem_Notebook.cast_dynamic(notebookItem);
		items = notebook.get_items();
		assertEquals(7, items.size());
		LayoutItem portalItem = items.get(0);
		LayoutItem_Portal portal = LayoutItem_Portal.cast_dynamic(portalItem);
		assertTrue(portal != null);

		// call get_suitable_table_to_view_details
		StringBuffer navigation_table_name = new StringBuffer();
		LayoutItem_Field navigation_relationship = new LayoutItem_Field(); // LayoutItem_Field is being used in place of
																			// UsesRelationship
		//TODO: portal.get_suitable_table_to_view_details(navigation_table_name, navigation_relationship, filmManagerDocument);

		// Simulate a garbage collection
		System.gc();
		System.runFinalization();

		// Check if things are working like we expect
		assertEquals("characters", navigation_table_name.toString());
		assertTrue(navigation_relationship != null);
		Relationship relationship = navigation_relationship.get_relationship();
		assertTrue(relationship != null);
		assertEquals("cast", relationship.get_name());
		assertTrue(navigation_relationship.get_related_relationship() == null);

	}

	@Test
	public void testReadReportNames() {
		StringVector reportNames = document.get_report_names("albums");
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
		LayoutItemVector layoutItems = layoutGroup.get_items();
		final int numItems = safeLongToInt(layoutItems.size());
		assertEquals(1, numItems);

		LayoutItem layoutItem = layoutItems.get(0);
		assertTrue(layoutItem != null);
		LayoutGroup asGroup = LayoutGroup.cast_dynamic(layoutItem);
		assertTrue(asGroup != null);
		LayoutItem_GroupBy groupby = LayoutItem_GroupBy.cast_dynamic(layoutItem);
		assertTrue(groupby != null);

		assertTrue(groupby.get_has_field_group_by());
		LayoutItem_Field fieldGroupBy = groupby.get_field_group_by();
		assertTrue(fieldGroupBy != null);
		assertThat(fieldGroupBy.get_name(), is("artist_id"));

		LayoutGroup groupSecondaries = groupby.get_secondary_fields();
		assertTrue(groupSecondaries != null);

		LayoutItemVector innerItems = groupby.get_items();
		assertTrue(innerItems != null);
		final int numInnerItems = safeLongToInt(innerItems.size());
		assertEquals(2, numInnerItems);

		layoutItem = layoutItems.get(0);
		assertTrue(layoutItem != null);
		LayoutItem_Field field = LayoutItem_Field.cast_dynamic(layoutItem);
		assertTrue(field != null);
		assertThat(field.get_name(), is("name"));
		assertThat(field.get_glom_type(), is(Field.glom_field_type.TYPE_TEXT));

		layoutItem = layoutItems.get(1);
		assertTrue(layoutItem != null);
		field = LayoutItem_Field.cast_dynamic(layoutItem);
		assertTrue(field != null);
		assertThat(field.get_name(), is("year"));
		assertThat(field.get_glom_type(), is(Field.glom_field_type.TYPE_NUMERIC));
	}

	// Test thread class that runs all the tests.
	private class TestThread implements Runnable {

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
