/*
 * Copyright (C) 2011 Openismus GmbH
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

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.database.DetailsDBAccess;
import org.glom.web.server.database.ListViewDBAccess;
import org.glom.web.server.database.RelatedListDBAccess;
import org.glom.web.server.database.RelatedListNavigation;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.DocumentInfo;
import org.glom.web.shared.NavigationRecord;
import org.glom.web.shared.Reports;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Document;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.Relationship;
import org.glom.web.shared.libglom.Report;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemCalendarPortal;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * A class to hold configuration information for a given Glom document. This class retrieves layout information from
 * libglom and data from the underlying PostgreSQL database.
 */
final class ConfiguredDocument {

	private Document document;
	private ComboPooledDataSource cpds;
	private boolean authenticated = false;
	private String documentID = "";
	private String defaultLocaleID = "";

	@SuppressWarnings("unused")
	private ConfiguredDocument() {
		// disable default constructor
	}

	ConfiguredDocument(final Document document) throws PropertyVetoException {

		// load the jdbc driver
		cpds = new ComboPooledDataSource();

		// We don't support sqlite or self-hosting yet.
		if (document.get_hosting_mode() != Document.HostingMode.HOSTING_MODE_POSTGRES_CENTRAL) {
			Log.fatal("Error configuring the database connection." + " Only central PostgreSQL hosting is supported.");
			// FIXME: Throw exception?
		}

		try {
			cpds.setDriverClass("org.postgresql.Driver");
		} catch (final PropertyVetoException e) {
			Log.fatal("Error loading the PostgreSQL JDBC driver."
					+ " Is the PostgreSQL JDBC jar available to the servlet?", e);
			throw e;
		}

		// setup the JDBC driver for the current glom document
		cpds.setJdbcUrl("jdbc:postgresql://" + document.get_connection_server() + ":" + document.get_connection_port()
				+ "/" + document.get_connection_database());

		this.document = document;
	}

	/**
	 * Sets the username and password for the database associated with the Glom document.
	 * 
	 * @return true if the username and password works, false otherwise
	 */
	boolean setUsernameAndPassword(final String username, final String password) throws SQLException {
		cpds.setUser(username);
		cpds.setPassword(password);

		final int acquireRetryAttempts = cpds.getAcquireRetryAttempts();
		cpds.setAcquireRetryAttempts(1);
		Connection conn = null;
		try {
			// FIXME find a better way to check authentication
			// it's possible that the connection could be failing for another reason
			conn = cpds.getConnection();
			authenticated = true;
		} catch (final SQLException e) {
			Log.info(Utils.getFileName(document.get_file_uri()), e.getMessage());
			Log.info(Utils.getFileName(document.get_file_uri()),
					"Connection Failed. Maybe the username or password is not correct.");
			authenticated = false;
		} finally {
			if (conn != null)
				conn.close();
			cpds.setAcquireRetryAttempts(acquireRetryAttempts);
		}
		return authenticated;
	}

	Document getDocument() {
		return document;
	}

	ComboPooledDataSource getCpds() {
		return cpds;
	}

	boolean isAuthenticated() {
		return authenticated;
	}

	String getDocumentID() {
		return documentID;
	}

	void setDocumentID(final String documentID) {
		this.documentID = documentID;
	}

	String getDefaultLocaleID() {
		return defaultLocaleID;
	}

	void setDefaultLocaleID(final String localeID) {
		this.defaultLocaleID = localeID;
	}

	/**
	 * @return
	 */
	DocumentInfo getDocumentInfo(final String localeID) {
		final DocumentInfo documentInfo = new DocumentInfo();

		// get arrays of table names and titles, and find the default table index
		final List<String> tablesVec = document.get_table_names();

		final int numTables = Utils.safeLongToInt(tablesVec.size());
		// we don't know how many tables will be hidden so we'll use half of the number of tables for the default size
		// of the ArrayList
		final ArrayList<String> tableNames = new ArrayList<String>(numTables / 2);
		final ArrayList<String> tableTitles = new ArrayList<String>(numTables / 2);
		boolean foundDefaultTable = false;
		int visibleIndex = 0;
		for (int i = 0; i < numTables; i++) {
			final String tableName = tablesVec.get(i);
			if (!document.get_table_is_hidden(tableName)) {
				tableNames.add(tableName);
				// JNI is "expensive", the comparison will only be called if we haven't already found the default table
				if (!foundDefaultTable && tableName.equals(document.get_default_table())) {
					documentInfo.setDefaultTableIndex(visibleIndex);
					foundDefaultTable = true;
				}
				tableTitles.add(document.get_table_title(tableName, localeID));
				visibleIndex++;
			}
		}

		// set everything we need
		documentInfo.setTableNames(tableNames);
		documentInfo.setTableTitles(tableTitles);
		documentInfo.setTitle(document.get_database_title(localeID));

		// Fetch arrays of locale IDs and titles:
		final List<String> localesVec = document.get_translation_available_locales();
		final int numLocales = Utils.safeLongToInt(localesVec.size());
		final ArrayList<String> localeIDs = new ArrayList<String>(numLocales);
		final ArrayList<String> localeTitles = new ArrayList<String>(numLocales);
		for (int i = 0; i < numLocales; i++) {
			final String this_localeID = localesVec.get(i);
			localeIDs.add(this_localeID);

			// Use java.util.Locale to get a title for the locale:
			final String[] locale_parts = this_localeID.split("_");
			String locale_lang = this_localeID;
			if (locale_parts.length > 0)
				locale_lang = locale_parts[0];
			String locale_country = "";
			if (locale_parts.length > 1)
				locale_country = locale_parts[1];

			final Locale locale = new Locale(locale_lang, locale_country);
			final String title = locale.getDisplayName(locale);
			localeTitles.add(title);
		}
		documentInfo.setLocaleIDs(localeIDs);
		documentInfo.setLocaleTitles(localeTitles);

		return documentInfo;
	}

	/*
	 * Gets the layout group for the list view using the defined layout list in the document or the table fields if
	 * there's no defined layout group for the list view.
	 */
	private LayoutGroup getValidListViewLayoutGroup(final String tableName, final String localeID) {

		final List<LayoutGroup> layoutGroupVec = document.get_data_layout_groups("list", tableName);

		final int listViewLayoutGroupSize = Utils.safeLongToInt(layoutGroupVec.size());
		LayoutGroup libglomLayoutGroup = null;
		if (listViewLayoutGroupSize > 0) {
			// A list layout group is defined.
			// We use the first group as the list.
			if (listViewLayoutGroupSize > 1)
				Log.warn(documentID, tableName, "The size of the list layout group is greater than 1. "
						+ "Attempting to use the first item for the layout list view.");

			libglomLayoutGroup = layoutGroupVec.get(0);
		} else {
			// A list layout group is *not* defined; we are going make a LayoutGroup from the list of fields.
			// This is unusual.
			Log.info(documentID, tableName,
					"A list layout is not defined for this table. Displaying a list layout based on the field list.");

			final List<Field> fieldsVec = document.get_table_fields(tableName);
			libglomLayoutGroup = new LayoutGroup();
			for (int i = 0; i < fieldsVec.size(); i++) {
				final Field field = fieldsVec.get(i);
				final LayoutItemField layoutItemField = new LayoutItemField();
				layoutItemField.set_full_field_details(field);
				libglomLayoutGroup.add_item(layoutItemField);
			}
		}
		
		//Clone the group and change the clone, to discard unwanted informatin (such as translations),
		//and to store some information that we do not want to calculate on the client side:
		final LayoutGroup cloned = (LayoutGroup)libglomLayoutGroup.clone();
		updateLayoutGroup(cloned, tableName, localeID);

		return libglomLayoutGroup;
	}

	/**
	 * @param libglomLayoutGroup
	 */
	private void updateLayoutGroup(final LayoutGroup layoutGroup, final String tableName, final String localeID) {
		final List<LayoutItem> layoutItemsVec = layoutGroup.get_items();
		
		int primaryKeyIndex = -1;
		
		final int numItems = Utils.safeLongToInt(layoutItemsVec.size());
		for (int i = 0; i < numItems; i++) {
			final LayoutItem layoutItem = layoutItemsVec.get(i);
			
			if(layoutItem instanceof LayoutItemField) {
				LayoutItemField layoutItemField = (LayoutItemField)layoutItem;
				final Field field = layoutItemField.get_full_field_details();
				if (field.get_primary_key())
					primaryKeyIndex = i;
				
			} else if(layoutItem instanceof LayoutGroup) {
				LayoutGroup childGroup = (LayoutGroup)layoutItem;
				updateLayoutGroup(childGroup, tableName, localeID);
			}
		}
		
		final ListViewDBAccess listViewDBAccess = new ListViewDBAccess(document, documentID, cpds, tableName, layoutGroup);
		layoutGroup.setExpectedResultSize(listViewDBAccess.getExpectedResultSize());
		
		// Set the primary key index for the table
		if (primaryKeyIndex < 0) {
			// Add a LayoutItemField for the primary key to the end of the item list in the LayoutGroup because it
			// doesn't already contain a primary key.
			Field primaryKey = null;
			final List<Field> fieldsVec = document.get_table_fields(tableName);
			for (int i = 0; i < Utils.safeLongToInt(fieldsVec.size()); i++) {
				final Field field = fieldsVec.get(i);
				if (field.get_primary_key()) {
					primaryKey = field;
					break;
				}
			}
			
			if (primaryKey != null) {
				final LayoutItemField layoutItemField = new LayoutItemField();
				layoutItemField.set_full_field_details(primaryKey);
				layoutGroup.add_item(layoutItemField); //TODO: Update the field to show just one locale?
				layoutGroup.setPrimaryKeyIndex(layoutGroup.get_items().size() - 1);
				layoutGroup.setHiddenPrimaryKey(true);
			} else {
				Log.error(document.get_database_title_original(), tableName,
						"A primary key was not found in the FieldVector for this table. Navigation buttons will not work.");
			}
		} else {
			layoutGroup.setPrimaryKeyIndex(primaryKeyIndex);
		}

		
		
		if(layoutGroup instanceof LayoutItemPortal) {
			LayoutItemPortal portal = (LayoutItemPortal)layoutGroup;
			updateLayoutItemPortalDTO(tableName, portal, localeID);
		}
	}

	ArrayList<DataItem[]> getListViewData(String tableName, final String quickFind, final int start, final int length,
			final boolean useSortClause, final int sortColumnIndex, final boolean isAscending) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		// Get the LayoutGroup that represents the list view.
		// TODO: Performance: Avoid calling this again:
		final LayoutGroup libglomLayoutGroup = getValidListViewLayoutGroup(tableName, "" /* irrelevant locale */);

		// Create a database access object for the list view.
		final ListViewDBAccess listViewDBAccess = new ListViewDBAccess(document, documentID, cpds, tableName,
				libglomLayoutGroup);

		// Return the data.
		return listViewDBAccess.getData(quickFind, start, length, useSortClause, sortColumnIndex, isAscending);
	}

	DataItem[] getDetailsData(String tableName, final TypedDataItem primaryKeyValue) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		final DetailsDBAccess detailsDBAccess = new DetailsDBAccess(document, documentID, cpds, tableName);

		return detailsDBAccess.getData(primaryKeyValue);
	}

	ArrayList<DataItem[]> getRelatedListData(String tableName, final String relationshipName,
			final TypedDataItem foreignKeyValue, final int start, final int length, final boolean useSortClause,
			final int sortColumnIndex, final boolean isAscending) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		// Create a database access object for the related list
		final RelatedListDBAccess relatedListDBAccess = new RelatedListDBAccess(document, documentID, cpds, tableName,
				relationshipName);

		// Return the data
		return relatedListDBAccess.getData(start, length, foreignKeyValue, useSortClause, sortColumnIndex, isAscending);
	}

	List<LayoutGroup> getDetailsLayoutGroup(String tableName, final String localeID) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);
		return document.get_data_layout_groups("details", tableName);
	}

	/*
	 * Gets the expected row count for a related list.
	 */
	int getRelatedListRowCount(String tableName, final String relationshipName, final TypedDataItem foreignKeyValue) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		// Create a database access object for the related list
		final RelatedListDBAccess relatedListDBAccess = new RelatedListDBAccess(document, documentID, cpds, tableName,
				relationshipName);

		// Return the row count
		return relatedListDBAccess.getExpectedResultSize(foreignKeyValue);
	}

	NavigationRecord getSuitableRecordToViewDetails(String tableName, final String relationshipName,
			final TypedDataItem primaryKeyValue) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		final RelatedListNavigation relatedListNavigation = new RelatedListNavigation(document, documentID, cpds,
				tableName, relationshipName);

		return relatedListNavigation.getNavigationRecord(primaryKeyValue);
	}

	LayoutGroup getListViewLayoutGroup(String tableName, final String localeID) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);
		return getValidListViewLayoutGroup(tableName, localeID);
	}

	/** Store some cache values in the LayoutItemPortal.
	 * 
	 * @param tableName
	 * @param layoutItemPortal
	 * @param localeID
	 * @return
	 */
	private void updateLayoutItemPortalDTO(final String tableName,
			final LayoutItemPortal layoutItemPortal, final String localeID) {

		// Ignore LayoutItem_CalendarPortals for now:
		// https://bugzilla.gnome.org/show_bug.cgi?id=664273
		if(layoutItemPortal instanceof LayoutItemCalendarPortal) {
			return;
		}

		final Relationship relationship = layoutItemPortal.getRelationship();
		if (relationship != null) {
			//layoutItemPortal.set_name(libglomLayoutItemPortal.get_relationship_name_used());
			//layoutItemPortal.setTableName(relationship.get_from_table());
			//layoutItemPortal.setFromField(relationship.get_from_field());

			// Set whether or not the related list will need to show the navigation buttons.
			// This was ported from Glom: Box_Data_Portal::get_has_suitable_record_to_view_details()
			final LayoutItemPortal.TableToViewDetails viewDetails = layoutItemPortal.get_suitable_table_to_view_details(document);
			boolean addNavigation = false;
			if(viewDetails != null) {
				addNavigation = !StringUtils.isEmpty(viewDetails.tableName);
			}
			layoutItemPortal.setAddNavigation(addNavigation);
		}
	}

	/*
	 * Converts a Gdk::Color (16-bits per channel) to an HTML colour (8-bits per channel) by discarding the least
	 * significant 8-bits in each channel.
	 */
	private String convertGdkColorToHtmlColour(final String gdkColor) {
		if (gdkColor.length() == 13)
			return gdkColor.substring(0, 3) + gdkColor.substring(5, 7) + gdkColor.substring(9, 11);
		else if (gdkColor.length() == 7) {
			// This shouldn't happen but let's deal with it if it does.
			Log.warn(documentID,
					"Expected a 13 character string but received a 7 character string. Returning received string.");
			return gdkColor;
		} else {
			Log.error("Did not receive a 13 or 7 character string. Returning black HTML colour code.");
			return "#000000";
		}
	}

	/**
	 * Gets the table name to use when accessing the database and the document. This method guards against SQL injection
	 * attacks by returning the default table if the requested table is not in the database or if the table name has not
	 * been set.
	 * 
	 * @param tableName
	 *            The table name to validate.
	 * @return The table name to use.
	 */
	private String getTableNameToUse(final String tableName) {
		if (StringUtils.isEmpty(tableName) || !document.get_table_is_known(tableName)) {
			return document.get_default_table();
		}
		return tableName;
	}

	/**
	 * @param tableName
	 * @param localeID
	 * @return
	 */
	public Reports getReports(final String tableName, final String localeID) {
		final Reports result = new Reports();

		final List<String> names = document.get_report_names(tableName);

		final int count = Utils.safeLongToInt(names.size());
		for (int i = 0; i < count; i++) {
			final String name = names.get(i);
			final Report report = document.get_report(tableName, name);
			if (report == null)
				continue;

			final String title = report.get_title(localeID);
			result.addReport(name, title);
		}

		return result;
	}
}
