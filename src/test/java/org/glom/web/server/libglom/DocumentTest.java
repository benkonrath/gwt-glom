/*
 * Copyright (C) 2009, 2010, 2011 Openismus GmbH
 *
 * This file is part of gwt-glom
 *
 * gwt-glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * gwt-glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gwt-glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.web.server.libglom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.NumericFormat;
import org.glom.web.shared.libglom.Relationship;
import org.glom.web.shared.libglom.Report;
import org.glom.web.shared.libglom.Translatable;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemNotebook;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;
import org.glom.web.shared.libglom.layout.LayoutItemText;
import org.glom.web.shared.libglom.layout.SortClause;
import org.glom.web.shared.libglom.layout.StaticText;
import org.glom.web.shared.libglom.layout.TableToViewDetails;
import org.glom.web.shared.libglom.layout.reportparts.LayoutItemGroupBy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Simple test to ensure that the generated bindings are working.
 */
public class DocumentTest {

	private static Document document;
	private static String defaultLocale = "";
	private static String germanLocale = "de";
	private static String testUriMusicCollection = "";
	private static String testUriFilmManager = "";

	@BeforeClass
	static public void setUp() {
		URL url = DocumentTest.class.getResource("example_music_collection.glom");
		assertTrue(url != null);
		testUriMusicCollection = url.toString();
		assertTrue(!StringUtils.isEmpty(testUriMusicCollection));

		url = DocumentTest.class.getResource("example_film_manager.glom");
		assertTrue(url != null);
		testUriFilmManager = url.toString();
		assertTrue(!StringUtils.isEmpty(testUriFilmManager));

		document = new Document();
		document.setFileURI(testUriMusicCollection);
		final boolean retval = document.load();
		assertTrue(retval);
	}

	@AfterClass
	static public void tearDown() {
	}

	@Test
	public void testDocumentInfo() {
		assertThat(document.getDatabaseTitleOriginal(), is("Music Collection"));
		assertThat(document.getDatabaseTitle(defaultLocale), is("Music Collection"));
		assertThat(document.getDatabaseTitle(germanLocale), is("Musiksammlung"));
		assertThat(document.getDefaultTable(), is("artists"));
	}

	@Test
	public void testLocales() {
		final List<String> localeIDs = document.getTranslationAvailableLocales();
		assertEquals(11, localeIDs.size());

		String tables = localeIDs.get(0);
		for (int i = 1; i < localeIDs.size(); i++) {
			tables += ", " + localeIDs.get(i);
		}
		assertThat(tables, is("cs, de, el, es, fr, gl, id, lv, pt_BR, sl, en"));
	}

	@Test
	public void testReadTableNames() {
		final List<String> tableNames = document.getTableNames();
		assertEquals(4, tableNames.size());

		String tables = tableNames.get(0);
		for (int i = 1; i < tableNames.size(); i++) {
			tables += ", " + tableNames.get(i);
		}
		assertThat(tables, is("artists, albums, songs, publishers"));
	}
	
	private String getTitles(final List<Field> list, final String locale) {
		String result = "";
		for (int i = 0; i < list.size(); i++) {
			final Translatable item = list.get(i);
			
			if(i != 0) {
				result += ", ";
			}
			
			result += item.getTitleOrName(locale);
		}

		return result;
	}

	@Test
	public void testReadTableFieldSizes() {

		List<Field> fields = document.getTableFields("albums");
		assertEquals(6, fields.size());

		// TODO: The sequence is not important. It's only important that they are all there.
		assertThat(getTitles(fields, defaultLocale), is("Publisher ID, Artist ID, Album ID, Name, Year, Comments"));
		assertThat(getTitles(fields, germanLocale), is("Herausgeber-Kennung, Künstlerkennung, Albenkennung, Name, Jahr, Kommentare"));
		
		fields = document.getTableFields("artists");
		assertEquals(4, fields.size());

		// TODO: The sequence is not important. It's only important that they are all there.
		assertThat(getTitles(fields, defaultLocale), is("Artist ID, Name, Description, Comments"));
		assertThat(getTitles(fields, germanLocale), is("Künstlerkennung, Name, Beschreibung, Kommentare"));

		fields = document.getTableFields("publishers");
		assertEquals(3, fields.size());

		// TODO: The sequence is not important. It's only important that they are all there.
		assertThat(getTitles(fields, defaultLocale), is("Name, Publisher ID, Comments"));
		assertThat(getTitles(fields, germanLocale), is("Name, Herausgeber-Kennung, Kommentare"));


		fields = document.getTableFields("songs");
		assertEquals(4, fields.size());

		// TODO: The sequence is not important. It's only important that they are all there.
		assertThat(getTitles(fields, defaultLocale), is("Song ID, Album ID, Name, Comments"));
		assertThat(getTitles(fields, germanLocale), is("Lied-Kennung, Albenkennung, Name, Kommentare"));
	}

	@Test
	public void testReadTableExampleRows() {
		final List<Map<String, DataItem>> exampleRows = document.getExampleRows("albums");
		assertFalse(exampleRows.isEmpty());

		final Map<String, DataItem> row = exampleRows.get(0);
		assertFalse(row.isEmpty());
	}

	@Test
	public void testReadLayoutListInfo() {
		final String[] tables = { "albums", "artists", "publishers", "songs" };
		final int[] sortClauseSizes = { 0, 1, 1, 1 };
		final int[] layoutFieldSizes = { 7, 4, 3, 4 };

		for (int i = 0; i < tables.length; i++) {
			final List<LayoutGroup> layoutList = document.getDataLayoutGroups(Document.LAYOUT_NAME_LIST, tables[i]);
			assertTrue(!layoutList.isEmpty());
			final List<LayoutItem> layoutItems = layoutList.get(0).getItems();
			final List<LayoutItemField> layoutFields = new ArrayList<>();
			final SortClause sortClause = new SortClause(); // TODO: Why use a SortClause instead of a List?
			final int numItems = safeLongToInt(layoutItems.size());
			for (int j = 0; j < numItems; j++) {
				final LayoutItem item = layoutItems.get(j);

				if (item instanceof LayoutItemField) {
					final LayoutItemField field = (LayoutItemField) item;
					layoutFields.add(field);
					final Field details = field.getFullFieldDetails();
					if (details != null && details.getPrimaryKey()) {
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
		final List<String> tableNames = document.getTableNames();

		for (final String table : tableNames) {
			final List<LayoutGroup> layoutList = document.getDataLayoutGroups(Document.LAYOUT_NAME_LIST, table);
			assertTrue(!layoutList.isEmpty());
			final LayoutGroup firstgroup = layoutList.get(0);
			assertTrue(firstgroup != null);
			final List<LayoutItem> layoutItems = firstgroup.getItems();
			final int numItems = safeLongToInt(layoutItems.size());
			for (int j = 0; j < numItems; j++) {
				final LayoutItem item = layoutItems.get(j);
				assertTrue(item != null);

				if (item instanceof LayoutItemField) {
					final LayoutItemField itemField = (LayoutItemField) item;
					// don't keep a reference to the FeildFormatting object
					final NumericFormat numFormat = itemField.getFormattingUsed().getNumericFormat();
					assertTrue(numFormat != null);

					// get the values
					final boolean altForegroundColorForNegatives = numFormat.getUseAltForegroundColorForNegatives();
					final String currencySymbol = numFormat.getCurrencySymbol();
					final long decimalPlaces = numFormat.getDecimalPlaces();
					final boolean decimalPlacesRestricted = numFormat.getDecimalPlacesRestricted();
					final boolean useThousandsSepator = numFormat.getUseThousandsSeparator();
					final String alternativeColorForNegatives = NumericFormat
							.getAlternativeColorForNegativesAsHTMLColor();
					final long defaultPrecision = NumericFormat.getDefaultPrecision();

					// Simulate a garbage collection
					System.gc();
					System.runFinalization();

					// re-get the values and test
					assertEquals(altForegroundColorForNegatives, numFormat.getUseAltForegroundColorForNegatives());
					assertEquals(currencySymbol, numFormat.getCurrencySymbol());
					assertEquals(decimalPlaces, numFormat.getDecimalPlaces());
					assertEquals(decimalPlacesRestricted, numFormat.getDecimalPlacesRestricted());
					assertEquals(useThousandsSepator, numFormat.getUseThousandsSeparator());
					assertEquals(alternativeColorForNegatives,
							NumericFormat.getAlternativeColorForNegativesAsHTMLColor());
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
		final String table = "albums";
		final List<LayoutGroup> layoutList = document.getDataLayoutGroups(Document.LAYOUT_NAME_LIST, table);
		final List<LayoutItem> layoutItems = layoutList.get(0).getItems();

		String names = null, hasRelationshipNames = null, tablesUsed = null;
		final LayoutItem firstItem = layoutItems.get(0);

		if (firstItem instanceof LayoutItemField) {
			final LayoutItemField firstItemField = (LayoutItemField) firstItem;
			names = firstItemField.getName();
			hasRelationshipNames = "" + firstItemField.getHasRelationshipName();
			tablesUsed = firstItemField.getTableUsed(table);
		}
		final int numItems = safeLongToInt(layoutItems.size());
		for (int j = 1; j < numItems; j++) {
			final LayoutItem item = layoutItems.get(j);

			if (item instanceof LayoutItemField) {
				final LayoutItemField itemField = (LayoutItemField) item;
				names += ", " + itemField.getName();
				hasRelationshipNames += ", " + itemField.getHasRelationshipName();
				tablesUsed += ", " + itemField.getTableUsed(table);
			}
		}
		assertEquals("name, year, artist_id, name, publisher_id, name, comments", names);
		assertEquals("false, false, false, true, false, true, false", hasRelationshipNames);
		assertEquals("albums, albums, albums, artists, albums, publishers, albums", tablesUsed);
	}

	@Test
	public void testLayoutItemText() {

		// Create a new document for the film manager
		final Document filmManagerDocument = new Document();
		filmManagerDocument.setFileURI(testUriFilmManager);
		final boolean retval = filmManagerDocument.load();
		assertTrue(retval);

		// This relies on specific details of the film manager details
		// view layout. I've included safety checks that will fail if the layout changes.
		final List<LayoutGroup> detailsLayout = filmManagerDocument.getDataLayoutGroups(Document.LAYOUT_NAME_DETAILS, "scenes");
		assertEquals(3, detailsLayout.size());

		LayoutGroup layoutGroup = detailsLayout.get(1);
		assertEquals(Document.LAYOUT_NAME_DETAILS, layoutGroup.getName());

		final List<LayoutItem> items = layoutGroup.getItems();
		
		final LayoutItem item = items.get(1);
		assertTrue(item instanceof LayoutItemText);
		
		LayoutItemText itemText = (LayoutItemText)item;
		StaticText text = itemText.getText();
		assertEquals(text.getTitle(), "The location name will be used if the name is empty.");
	}
		
		
		
	@Test
	public void testGetSuitableTableToViewDetails() {

		// Create a new document for the film manager
		final Document filmManagerDocument = new Document();
		filmManagerDocument.setFileURI(testUriFilmManager);
		final boolean retval = filmManagerDocument.load();
		assertTrue(retval);

		// Get the "Scene Cast" related list portal. This relies on specific details of the film manager details
		// view layout. I've included safety checks that will fail if the layout changes.
		final List<LayoutGroup> detailsLayout = filmManagerDocument.getDataLayoutGroups(Document.LAYOUT_NAME_DETAILS, "scenes");
		assertEquals(3, detailsLayout.size());

		LayoutGroup layoutGroup = detailsLayout.get(1);
		assertEquals(Document.LAYOUT_NAME_DETAILS, layoutGroup.getName());
		assertEquals("Details", layoutGroup.getTitle(defaultLocale));
		assertEquals("Details", layoutGroup.getTitle(germanLocale));

		layoutGroup = detailsLayout.get(2);
		assertEquals("details_lower", layoutGroup.getName());

		List<LayoutItem> items = layoutGroup.getItems();
		assertEquals(2, items.size());

		final LayoutItem notebookItem = items.get(0);
		assertEquals("notebook", notebookItem.getName());
		assertTrue(notebookItem instanceof LayoutItemNotebook);
		final LayoutItemNotebook notebook = (LayoutItemNotebook) notebookItem;
		items = notebook.getItems();
		assertEquals(7, items.size());
		final LayoutItem portalItem = items.get(0);
		assertTrue(portalItem instanceof LayoutItemPortal);
		final LayoutItemPortal portal = (LayoutItemPortal) portalItem;
		assertTrue(portal != null);

		assertEquals("scene_cast", portal.getRelationshipNameUsed());
		assertEquals("Cast", portal.getTitle(defaultLocale));
		assertEquals("Szene Besetzung", portal.getTitle(germanLocale));

		// call getSuitableTableToViewDetails
		final TableToViewDetails viewDetails = filmManagerDocument.getPortalSuitableTableToViewDetails(portal);
		assertTrue(viewDetails != null);

		// Simulate a garbage collection
		System.gc();
		System.runFinalization();

		// Check if things are working like we expect
		assertEquals("characters", viewDetails.tableName);
		assertTrue(viewDetails.usesRelationship != null);
		final Relationship relationship = viewDetails.usesRelationship.getRelationship();
		assertTrue(relationship != null);
		assertEquals("cast", relationship.getName());
		assertTrue(viewDetails.usesRelationship.getRelatedRelationship() == null);

	}

	@Test
	public void testReadReportNames() {
		final List<String> reportNames = document.getReportNames("albums");
		assertEquals(1, reportNames.size()); // TODO: Test something with more reports.

		String reports = reportNames.get(0);
		for (int i = 1; i < reportNames.size(); i++) {
			reports += ", " + reportNames.get(i);
		}
		assertThat(reports, is("albums_by_artist"));
	}

	@Test
	public void testReadReportStructure() {
		final Report report = document.getReport("albums", "albums_by_artist");
		assertTrue(report != null);
		
		assertThat(report.getTitle(defaultLocale), is("Albums By Artist"));
		assertThat(report.getTitle(germanLocale), is("Alben nach Künstler"));	
		
		final LayoutGroup layoutGroup = report.getLayoutGroup();
		assertTrue(layoutGroup != null);
		final List<LayoutItem> layoutItems = layoutGroup.getItems();
		final int numItems = safeLongToInt(layoutItems.size());
		assertEquals(1, numItems);

		LayoutItem layoutItem = layoutItems.get(0);
		assertTrue(layoutItem != null);
		final LayoutGroup asGroup = (LayoutGroup) layoutItem;
		assertTrue(asGroup != null);
		final LayoutItemGroupBy groupby = (LayoutItemGroupBy) layoutItem;
		assertTrue(groupby != null);

		assertTrue(groupby.getHasFieldGroupBy());
		final LayoutItemField fieldGroupBy = groupby.getFieldGroupBy();
		assertTrue(fieldGroupBy != null);
		assertThat(fieldGroupBy.getName(), is("artist_id"));

		final LayoutGroup groupSecondaries = groupby.getSecondaryFields();
		assertTrue(groupSecondaries != null);

		final List<LayoutItem> innerItems = groupby.getItems();
		assertTrue(innerItems != null);
		final int numInnerItems = safeLongToInt(innerItems.size());
		assertEquals(2, numInnerItems);

		layoutItem = innerItems.get(0);
		assertTrue(layoutItem != null);
		assertTrue(layoutItem instanceof LayoutItemField);
		LayoutItemField field = (LayoutItemField) layoutItem;
		assertTrue(field != null);
		assertThat(field.getName(), is("name"));
		assertThat(field.getGlomType(), is(Field.GlomFieldType.TYPE_TEXT));

		layoutItem = innerItems.get(1);
		assertTrue(layoutItem != null);
		assertTrue(layoutItem instanceof LayoutItemField);
		field = (LayoutItemField) layoutItem;
		assertTrue(field != null);
		assertThat(field.getName(), is("year"));
		assertThat(field.getGlomType(), is(Field.GlomFieldType.TYPE_NUMERIC));
	}

	// Test thread class that runs all the tests.
	private class TestThread implements Runnable {

		@Override
		public void run() {
			for (int i = 0; i < 20; i++) {
				testDocumentInfo();
				testGetNumericFormat();
				testLayoutItemText();
				//TODO: testLayoutItemImage(), also testing that it has the expected layout path.
				testGetSuitableTableToViewDetails();
				testReadLayoutListInfo();
				testReadTableFieldSizes();
				testReadTableNames();
				testUsesRelationshipMethods();
			}
		}
	}

	/*
	 * Tests threaded access.
	 */
	@Test
	public void testThreadedAccess() throws InterruptedException {
		// create the threads
		final Thread thread1 = new Thread(new TestThread());
		final Thread thread2 = new Thread(new TestThread());
		final Thread thread3 = new Thread(new TestThread());
		final Thread thread4 = new Thread(new TestThread());

		// start the threads
		thread1.start();
		thread2.start();
		thread3.start();
		thread4.start();

		// wait for the treads to finish
		try {
			thread1.join();
		} catch (final InterruptedException e) {
			System.out.println("Thread 1 had a problem finishing. " + e);
			throw e;
		}

		try {
			thread2.join();
		} catch (final InterruptedException e) {
			System.out.println("Thread 2 had a problem finishing. " + e);
			throw e;
		}

		try {
			thread3.join();
		} catch (final InterruptedException e) {
			System.out.println("Thread 3 had a problem finishing. " + e);
			throw e;
		}

		try {
			thread4.join();
		} catch (final InterruptedException e) {
			System.out.println("Thread 4 had a problem finishing. " + e);
			throw e;
		}
	}

	/*
	 * This method safely converts longs from libglom into ints. This method was taken from stackoverflow:
	 * 
	 * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
	 */
	private static int safeLongToInt(final long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

}
