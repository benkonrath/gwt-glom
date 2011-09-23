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

import org.glom.libglom.Document;
import org.glom.libglom.Field;
import org.glom.libglom.FieldFormatting;
import org.glom.libglom.FieldVector;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.LayoutItem_Portal;
import org.glom.libglom.Relationship;
import org.glom.libglom.StringVector;
import org.glom.web.server.database.DetailsDBAccess;
import org.glom.web.server.database.ListDBAccess;
import org.glom.web.server.database.ListViewDBAccess;
import org.glom.web.server.database.RelatedListDBAccess;
import org.glom.web.shared.DocumentInfo;
import org.glom.web.shared.GlomField;
import org.glom.web.shared.layout.Formatting;
import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItemField;
import org.glom.web.shared.layout.LayoutItemPortal;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * A class to hold configuration information for a given Glom document. This class is used to retrieve layout
 * information from libglom and data from the underlying PostgreSQL database.
 * 
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
final class ConfiguredDocument {

	private Document document;
	private ComboPooledDataSource cpds;
	private boolean authenticated = false;
	private String documentID;

	@SuppressWarnings("unused")
	private ConfiguredDocument() {
		// disable default constructor
	}

	ConfiguredDocument(Document document) throws PropertyVetoException {

		// load the jdbc driver
		cpds = new ComboPooledDataSource();

		// We don't support sqlite or self-hosting yet.
		if (document.get_hosting_mode() != Document.HostingMode.HOSTING_MODE_POSTGRES_CENTRAL) {
			Log.fatal("Error configuring the database connection." + " Only central PostgreSQL hosting is supported.");
			// FIXME: Throw exception?
		}

		try {
			cpds.setDriverClass("org.postgresql.Driver");
		} catch (PropertyVetoException e) {
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
	boolean setUsernameAndPassword(String username, String password) throws SQLException {
		cpds.setUser(username);
		cpds.setPassword(password);

		int acquireRetryAttempts = cpds.getAcquireRetryAttempts();
		cpds.setAcquireRetryAttempts(1);
		Connection conn = null;
		try {
			// FIXME find a better way to check authentication
			// it's possible that the connection could be failing for another reason
			conn = cpds.getConnection();
			authenticated = true;
		} catch (SQLException e) {
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

	void setDocumentID(String documentID) {
		this.documentID = documentID;
	}

	/**
	 * @return
	 */
	DocumentInfo getDocumentInfo() {
		DocumentInfo documentInfo = new DocumentInfo();

		// get arrays of table names and titles, and find the default table index
		StringVector tablesVec = document.get_table_names();

		int numTables = Utils.safeLongToInt(tablesVec.size());
		// we don't know how many tables will be hidden so we'll use half of the number of tables for the default size
		// of the ArrayList
		ArrayList<String> tableNames = new ArrayList<String>(numTables / 2);
		ArrayList<String> tableTitles = new ArrayList<String>(numTables / 2);
		boolean foundDefaultTable = false;
		int visibleIndex = 0;
		for (int i = 0; i < numTables; i++) {
			String tableName = tablesVec.get(i);
			if (!document.get_table_is_hidden(tableName)) {
				tableNames.add(tableName);
				// JNI is "expensive", the comparison will only be called if we haven't already found the default table
				if (!foundDefaultTable && tableName.equals(document.get_default_table())) {
					documentInfo.setDefaultTableIndex(visibleIndex);
					foundDefaultTable = true;
				}
				tableTitles.add(document.get_table_title(tableName));
				visibleIndex++;
			}
		}

		// set everything we need
		documentInfo.setTableNames(tableNames);
		documentInfo.setTableTitles(tableTitles);
		documentInfo.setTitle(document.get_database_title());

		return documentInfo;
	}

	/*
	 * Gets the layout group for the list view using the defined layout list in the document or the table fields if
	 * there's no defined layout group for the list view.
	 */
	private org.glom.libglom.LayoutGroup getValidListViewLayoutGroup(String tableName) {

		LayoutGroupVector layoutGroupVec = document.get_data_layout_groups("list", tableName);

		int listViewLayoutGroupSize = Utils.safeLongToInt(layoutGroupVec.size());
		org.glom.libglom.LayoutGroup libglomLayoutGroup = null;
		if (listViewLayoutGroupSize > 0) {
			// a list layout group is defined; we can use the first group as the list
			if (listViewLayoutGroupSize > 1)
				Log.warn(documentID, tableName, "The size of the list layout group is greater than 1. "
						+ "Attempting to use the first item for the layout list view.");

			libglomLayoutGroup = layoutGroupVec.get(0);
		} else {
			// a list layout group is *not* defined; we are going make a libglom layout group from the list of fields
			Log.info(documentID, tableName,
					"A list layout is not defined for this table. Displaying a list layout based on the field list.");

			FieldVector fieldsVec = document.get_table_fields(tableName);
			libglomLayoutGroup = new org.glom.libglom.LayoutGroup();
			for (int i = 0; i < fieldsVec.size(); i++) {
				Field field = fieldsVec.get(i);
				LayoutItem_Field layoutItemField = new LayoutItem_Field();
				layoutItemField.set_full_field_details(field);
				libglomLayoutGroup.add_item(layoutItemField);
			}
		}

		return libglomLayoutGroup;
	}

	ArrayList<GlomField[]> getListViewData(String tableName, int start, int length, boolean useSortClause,
			int sortColumnIndex, boolean isAscending) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		// Get the libglom LayoutGroup that represents the list view.
		org.glom.libglom.LayoutGroup libglomLayoutGroup = getValidListViewLayoutGroup(tableName);

		// Create a database access object for the list view.
		ListViewDBAccess listViewDBAccess = new ListViewDBAccess(document, documentID, cpds, tableName,
				libglomLayoutGroup);

		// Return the data.
		return listViewDBAccess.getData(start, length, useSortClause, sortColumnIndex, isAscending);
	}

	GlomField[] getDetailsData(String tableName, String primaryKeyValue) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		DetailsDBAccess detailsDBAccess = new DetailsDBAccess(document, documentID, cpds, tableName);

		return detailsDBAccess.getData(primaryKeyValue);
	}

	ArrayList<GlomField[]> getRelatedListData(String tableName, String relationshipName, String foreignKeyValue,
			int start, int length, boolean useSortClause, int sortColumnIndex, boolean isAscending) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		// Create a database access object for the related list
		RelatedListDBAccess relatedListDBAccess = new RelatedListDBAccess(document, documentID, cpds, tableName,
				relationshipName);

		// Return the data
		return relatedListDBAccess.getData(start, length, foreignKeyValue, useSortClause, sortColumnIndex, isAscending);
	}

	ArrayList<LayoutGroup> getDetailsLayoutGroup(String tableName) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		// Get the details layout group information for each LayoutGroup in the LayoutGroupVector
		LayoutGroupVector layoutGroupVec = document.get_data_layout_groups("details", tableName);
		ArrayList<LayoutGroup> layoutGroups = new ArrayList<LayoutGroup>();
		for (int i = 0; i < layoutGroupVec.size(); i++) {
			org.glom.libglom.LayoutGroup libglomLayoutGroup = layoutGroupVec.get(i);

			// satisfy the precondition of getDetailsLayoutGroup(String, org.glom.libglom.LayoutGroup)
			if (libglomLayoutGroup == null)
				continue;

			layoutGroups.add(getDetailsLayoutGroup(tableName, libglomLayoutGroup));
		}

		return layoutGroups;
	}

	LayoutGroup getListViewLayoutGroup(String tableName) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		org.glom.libglom.LayoutGroup libglomLayoutGroup = getValidListViewLayoutGroup(tableName);

		return getListLayoutGroup(tableName, libglomLayoutGroup);
	}

	/*
	 * Gets the expected row count for a related list.
	 */
	int getRelatedListRowCount(String tableName, String relationshipName, String foreignKeyValue) {
		// Validate the table name.
		tableName = getTableNameToUse(tableName);

		// Create a database access object for the related list
		RelatedListDBAccess relatedListDBAccess = new RelatedListDBAccess(document, documentID, cpds, tableName,
				relationshipName);

		// Return the row count
		return relatedListDBAccess.getExpectedResultSize(foreignKeyValue);
	}

	/*
	 * Gets a LayoutGroup DTO for the given table name and libglom LayoutGroup. This method can be used for the main
	 * list view table and for the related list table.
	 */
	private LayoutGroup getListLayoutGroup(String tableName, org.glom.libglom.LayoutGroup libglomLayoutGroup) {
		LayoutGroup layoutGroup = new LayoutGroup();

		// look at each child item
		LayoutItemVector layoutItemsVec = libglomLayoutGroup.get_items();
		int numItems = Utils.safeLongToInt(layoutItemsVec.size());
		for (int i = 0; i < numItems; i++) {
			org.glom.libglom.LayoutItem libglomLayoutItem = layoutItemsVec.get(i);

			// TODO add support for other LayoutItems (Text, Image, Button etc.)
			LayoutItem_Field libglomLayoutField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
			if (libglomLayoutField != null) {
				layoutGroup.addItem(convertToGWTGlomLayoutItemField(libglomLayoutField));
			} else {
				Log.info(documentID, tableName,
						"Ignoring unknown LayoutItem of type " + libglomLayoutItem.get_part_type_name() + ".");
				continue;
			}
		}

		ListDBAccess listDBAccess = null;
		LayoutItem_Portal libglomLayoutItemPortal = LayoutItem_Portal.cast_dynamic(libglomLayoutGroup);
		if (libglomLayoutItemPortal != null) {
			// libglomLayoutGroup is a related view
			listDBAccess = new RelatedListDBAccess(document, documentID, cpds, tableName,
					libglomLayoutItemPortal.get_relationship_name_used());
			layoutGroup.setExpectedResultSize(listDBAccess.getExpectedResultSize());
		} else {
			// libglomLayoutGroup is a list view
			listDBAccess = new ListViewDBAccess(document, documentID, cpds, tableName, libglomLayoutGroup);
			layoutGroup.setExpectedResultSize(listDBAccess.getExpectedResultSize());
		}

		// Set the primary key index for the table
		int primaryKeyIndex = listDBAccess.getPrimaryKeyIndex();
		if (primaryKeyIndex < 0) {
			// Add a LayoutItemField for the primary key to the end of the item list in the LayoutGroup because it
			// doesn't already contain a primary key.
			LayoutItem_Field libglomLayoutItemField = listDBAccess.getPrimaryKeyLayoutItemField();
			layoutGroup.addItem(convertToGWTGlomLayoutItemField(libglomLayoutItemField));
			layoutGroup.setPrimaryKeyIndex(layoutGroup.getItems().size() - 1);
			layoutGroup.setHiddenPrimaryKey(true);

		} else {
			layoutGroup.setPrimaryKeyIndex(primaryKeyIndex);
		}

		layoutGroup.setTableName(tableName);

		return layoutGroup;
	}

	/*
	 * Gets a recursively defined Details LayoutGroup DTO for the specified libglom LayoutGroup object. This is used for
	 * getting layout information for the details view.
	 * 
	 * @param documentID Glom document identifier
	 * 
	 * @param tableName table name in the specified Glom document
	 * 
	 * @param libglomLayoutGroup libglom LayoutGroup to convert
	 * 
	 * @precondition libglomLayoutGroup must not be null
	 * 
	 * @return {@link LayoutGroup} object that represents the layout for the specified {@link
	 * org.glom.libglom.LayoutGroup}
	 */
	private LayoutGroup getDetailsLayoutGroup(String tableName, org.glom.libglom.LayoutGroup libglomLayoutGroup) {
		LayoutGroup layoutGroup = new LayoutGroup();
		layoutGroup.setColumnCount(Utils.safeLongToInt(libglomLayoutGroup.get_columns_count()));
		layoutGroup.setTitle(libglomLayoutGroup.get_title());

		// look at each child item
		LayoutItemVector layoutItemsVec = libglomLayoutGroup.get_items();
		for (int i = 0; i < layoutItemsVec.size(); i++) {
			org.glom.libglom.LayoutItem libglomLayoutItem = layoutItemsVec.get(i);

			// just a safety check
			if (libglomLayoutItem == null)
				continue;

			org.glom.web.shared.layout.LayoutItem layoutItem = null;
			org.glom.libglom.LayoutGroup group = org.glom.libglom.LayoutGroup.cast_dynamic(libglomLayoutItem);
			if (group != null) {
				// libglomLayoutItem is a LayoutGroup
				LayoutItem_Portal libglomLayoutItemPortal = LayoutItem_Portal.cast_dynamic(group);
				if (libglomLayoutItemPortal != null) {
					// group is a LayoutItemPortal
					LayoutItemPortal layoutItemPortal = new LayoutItemPortal();
					Relationship relationship = libglomLayoutItemPortal.get_relationship();
					if (relationship != null) {
						layoutItemPortal.setNavigationType(convertToGWTGlomNavigationType(libglomLayoutItemPortal
								.get_navigation_type()));

						layoutItemPortal.setTitle(libglomLayoutItemPortal.get_title_used("")); // parent title not
																								// relevant
						LayoutGroup tempLayoutGroup = getListLayoutGroup(tableName, libglomLayoutItemPortal);
						for (org.glom.web.shared.layout.LayoutItem item : tempLayoutGroup.getItems()) {
							// TODO EDITING If the relationship does not allow editing, then mark all these fields as
							// non-editable. Check relationship.get_allow_edit() to see if it's editable.
							layoutItemPortal.addItem(item);
						}
						layoutItemPortal.setPrimaryKeyIndex(tempLayoutGroup.getPrimaryKeyIndex());
						layoutItemPortal.setHiddenPrimaryKey(tempLayoutGroup.hasHiddenPrimaryKey());
						layoutItemPortal.setName(libglomLayoutItemPortal.get_relationship_name_used());
						layoutItemPortal.setTableName(relationship.get_from_table());
						layoutItemPortal.setFromField(relationship.get_from_field());
					}

					// Note: empty layoutItemPortal used if relationship is null
					layoutItem = layoutItemPortal;

				} else {
					// group is *not* a LayoutItemPortal//
					// recurse into child groups
					layoutItem = getDetailsLayoutGroup(tableName, group);
				}
			} else {
				// libglomLayoutItem is *not* a LayoutGroup
				// create LayoutItem DTOs based on the the libglom type
				// TODO add support for other LayoutItems (Text, Image, Button etc.)
				LayoutItem_Field libglomLayoutField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
				if (libglomLayoutField != null) {
					layoutItem = convertToGWTGlomLayoutItemField(libglomLayoutField);
				} else {
					Log.info(documentID, tableName,
							"Ignoring unknown LayoutItem of type " + libglomLayoutItem.get_part_type_name() + ".");
					continue;
				}
			}

			layoutGroup.addItem(layoutItem);
		}

		return layoutGroup;
	}

	private LayoutItemField convertToGWTGlomLayoutItemField(LayoutItem_Field libglomLayoutItemField) {
		LayoutItemField layoutItemField = new LayoutItemField();

		// set type
		layoutItemField.setType(convertToGWTGlomFieldType(libglomLayoutItemField.get_glom_type()));

		// set formatting
		Formatting formatting = new Formatting();
		formatting.setHorizontalAlignment(convertToGWTGlomHorizonalAlignment(libglomLayoutItemField
				.get_formatting_used_horizontal_alignment()));
		FieldFormatting libglomFormatting = libglomLayoutItemField.get_formatting_used();
		if (libglomFormatting.get_text_format_multiline()) {
			formatting.setTextFormatMultilineHeightLines(Utils.safeLongToInt(libglomFormatting
					.get_text_format_multiline_height_lines()));
		}
		layoutItemField.setFormatting(formatting);

		// set title and name
		layoutItemField.setTitle(libglomLayoutItemField.get_title_or_name());
		layoutItemField.setName(libglomLayoutItemField.get_name());

		return layoutItemField;
	}

	/*
	 * This method converts a Field.glom_field_type to the equivalent ColumnInfo.FieldType. The need for this comes from
	 * the fact that the GWT FieldType classes can't be used with RPC and there's no easy way to use the java-libglom
	 * Field.glom_field_type enum with RPC. An enum identical to FieldFormatting.glom_field_type is included in the
	 * ColumnInfo class.
	 */
	private LayoutItemField.GlomFieldType convertToGWTGlomFieldType(Field.glom_field_type type) {
		switch (type) {
		case TYPE_BOOLEAN:
			return LayoutItemField.GlomFieldType.TYPE_BOOLEAN;
		case TYPE_DATE:
			return LayoutItemField.GlomFieldType.TYPE_DATE;
		case TYPE_IMAGE:
			return LayoutItemField.GlomFieldType.TYPE_IMAGE;
		case TYPE_NUMERIC:
			return LayoutItemField.GlomFieldType.TYPE_NUMERIC;
		case TYPE_TEXT:
			return LayoutItemField.GlomFieldType.TYPE_TEXT;
		case TYPE_TIME:
			return LayoutItemField.GlomFieldType.TYPE_TIME;
		case TYPE_INVALID:
			Log.info("Returning TYPE_INVALID.");
			return LayoutItemField.GlomFieldType.TYPE_INVALID;
		default:
			Log.error("Recieved a type that I don't know about: " + Field.glom_field_type.class.getName() + "."
					+ type.toString() + ". Returning " + LayoutItemField.GlomFieldType.TYPE_INVALID.toString() + ".");
			return LayoutItemField.GlomFieldType.TYPE_INVALID;
		}
	}

	/*
	 * This method converts a FieldFormatting.HorizontalAlignment to the equivalent Formatting.HorizontalAlignment. The
	 * need for this comes from the fact that the GWT HorizontalAlignment classes can't be used with RPC and there's no
	 * easy way to use the java-libglom FieldFormatting.HorizontalAlignment enum with RPC. An enum identical to
	 * FieldFormatting.HorizontalAlignment is included in the Formatting class.
	 */
	private Formatting.HorizontalAlignment convertToGWTGlomHorizonalAlignment(
			FieldFormatting.HorizontalAlignment alignment) {
		switch (alignment) {
		case HORIZONTAL_ALIGNMENT_AUTO:
			return Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_AUTO;
		case HORIZONTAL_ALIGNMENT_LEFT:
			return Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_LEFT;
		case HORIZONTAL_ALIGNMENT_RIGHT:
			return Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_RIGHT;
		default:
			Log.error("Recieved an alignment that I don't know about: "
					+ FieldFormatting.HorizontalAlignment.class.getName() + "." + alignment.toString() + ". Returning "
					+ Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_RIGHT.toString() + ".");
			return Formatting.HorizontalAlignment.HORIZONTAL_ALIGNMENT_RIGHT;
		}
	}

	/*
	 * This method converts a LayoutItem_Portal.navigation_type from java-libglom to the equivalent
	 * LayoutItemPortal.NavigationType from Online Glom. This conversion is required because the LayoutItem_Portal class
	 * from java-libglom can't be used with GWT-RPC. An enum identical to LayoutItem_Portal.navigation_type from
	 * java-libglom is included in the LayoutItemPortal data transfer object.
	 */
	private LayoutItemPortal.NavigationType convertToGWTGlomNavigationType(
			LayoutItem_Portal.navigation_type navigationType) {
		switch (navigationType) {
		case NAVIGATION_NONE:
			return LayoutItemPortal.NavigationType.NAVIGATION_NONE;
		case NAVIGATION_AUTOMATIC:
			return LayoutItemPortal.NavigationType.NAVIGATION_AUTOMATIC;
		case NAVIGATION_SPECIFIC:
			return LayoutItemPortal.NavigationType.NAVIGATION_SPECIFIC;
		default:
			Log.error("Recieved an unknown NavigationType: " + LayoutItem_Portal.navigation_type.class.getName() + "."
					+ navigationType.toString() + ". Returning " + LayoutItemPortal.NavigationType.NAVIGATION_AUTOMATIC
					+ ".");
			return LayoutItemPortal.NavigationType.NAVIGATION_AUTOMATIC;
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
	private String getTableNameToUse(String tableName) {
		if (tableName == null || tableName.isEmpty() || !document.get_table_is_known(tableName)) {
			return document.get_default_table();
		}
		return tableName;
	}

}
