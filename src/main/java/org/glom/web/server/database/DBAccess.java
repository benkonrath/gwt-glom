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

package org.glom.web.server.database;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.glom.libglom.Document;
import org.glom.libglom.Field;
import org.glom.libglom.FieldVector;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.LayoutItem;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.LayoutItem_Portal;
import org.glom.web.server.Log;
import org.glom.web.server.Utils;
import org.glom.web.shared.DataItem;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
abstract class DBAccess {
	protected Document document;
	protected String documentID;
	protected String tableName;
	protected ComboPooledDataSource cpds;

	protected DBAccess(Document document, String documentID, ComboPooledDataSource cpds, String tableName) {
		this.document = document;
		this.documentID = documentID;
		this.cpds = cpds;
		this.tableName = tableName;
	}

	/*
	 * Converts data from a ResultSet to an ArrayList of DataItem array suitable for sending back to the client.
	 */
	final protected ArrayList<DataItem[]> convertResultSetToDTO(int length, LayoutFieldVector layoutFields, ResultSet rs)
			throws SQLException {

		// get the data we've been asked for
		int rowCount = 0;
		ArrayList<DataItem[]> rowsList = new ArrayList<DataItem[]>();
		while (rs.next() && rowCount <= length) {
			int layoutFieldsSize = Utils.safeLongToInt(layoutFields.size());
			DataItem[] rowArray = new DataItem[layoutFieldsSize];
			for (int i = 0; i < layoutFieldsSize; i++) {
				// make a new DataItem to set the text and colours
				rowArray[i] = new DataItem();

				// Convert the field value to a string based on the glom type. We're doing the formatting on the
				// server side for now but it might be useful to move this to the client side.
				LayoutItem_Field field = layoutFields.get(i);
				switch (field.get_glom_type()) {
				case TYPE_TEXT:
					String text = rs.getString(i + 1);
					rowArray[i].setText(text != null ? text : "");
					break;
				case TYPE_BOOLEAN:
					rowArray[i].setBoolean(rs.getBoolean(i + 1));
					break;
				case TYPE_NUMERIC:
					rowArray[i].setNumber(rs.getDouble(i + 1));
					break;
				case TYPE_DATE:
					Date date = rs.getDate(i + 1);
					if (date != null) {
						DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ROOT);
						rowArray[i].setText(dateFormat.format(date));
					} else {
						rowArray[i].setText("");
					}
					break;
				case TYPE_TIME:
					Time time = rs.getTime(i + 1);
					if (time != null) {
						DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.ROOT);
						rowArray[i].setText(timeFormat.format(time));
					} else {
						rowArray[i].setText("");
					}
					break;
				case TYPE_IMAGE:
					byte[] image = rs.getBytes(i + 1);
					if (image != null) {
						// TODO implement field TYPE_IMAGE
						rowArray[i].setText("Image (FIXME)");
					} else {
						rowArray[i].setText("");
					}
					break;
				case TYPE_INVALID:
				default:
					Log.warn(documentID, tableName, "Invalid LayoutItem Field type. Using empty string for value.");
					rowArray[i].setText("");
					break;
				}
			}

			// add the row of DataItems to the ArrayList we're going to return and update the row count
			rowsList.add(rowArray);
			rowCount++;
		}

		return rowsList;
	}

	/*
	 * Gets a LayoutFieldVector to use when generating an SQL query.
	 */
	protected LayoutFieldVector getFieldsToShowForSQLQuery(LayoutGroupVector layoutGroupVec) {
		LayoutFieldVector layoutFieldVector = new LayoutFieldVector();

		// We will show the fields that the document says we should:
		for (int i = 0; i < layoutGroupVec.size(); i++) {
			org.glom.libglom.LayoutGroup layoutGroup = layoutGroupVec.get(i);

			// satisfy the precondition of getDetailsLayoutGroup(String tableName, org.glom.libglom.LayoutGroup
			// libglomLayoutGroup)
			if (layoutGroup == null)
				continue;

			// Get the fields:
			ArrayList<LayoutItem_Field> layoutItemFields = getFieldsToShowForSQLQueryAddGroup(layoutGroup);
			for (LayoutItem_Field layoutItem_Field : layoutItemFields) {
				layoutFieldVector.add(layoutItem_Field);
			}
		}
		return layoutFieldVector;
	}

	/*
	 * Gets an ArrayList of LayoutItem_Field objects to use when generating an SQL query.
	 * 
	 * @precondition libglomLayoutGroup must not be null
	 */
	private ArrayList<LayoutItem_Field> getFieldsToShowForSQLQueryAddGroup(
			org.glom.libglom.LayoutGroup libglomLayoutGroup) {

		ArrayList<LayoutItem_Field> layoutItemFields = new ArrayList<LayoutItem_Field>();
		LayoutItemVector items = libglomLayoutGroup.get_items();
		int numItems = Utils.safeLongToInt(items.size());
		for (int i = 0; i < numItems; i++) {
			LayoutItem layoutItem = items.get(i);

			LayoutItem_Field layoutItemField = LayoutItem_Field.cast_dynamic(layoutItem);
			if (layoutItemField != null) {
				// the layoutItem is a LayoutItem_Field
				FieldVector fields;
				if (layoutItemField.get_has_relationship_name()) {
					// layoutItemField is a field in a related table
					fields = document.get_table_fields(layoutItemField.get_table_used(tableName));
				} else {
					// layoutItemField is a field in this table
					fields = document.get_table_fields(tableName);
				}

				// set the layoutItemFeild with details from its Field in the document and
				// add it to the list to be returned
				for (int j = 0; j < fields.size(); j++) {
					// check the names to see if they're the same
					// this works because we're using the field list from the related table if necessary
					if (layoutItemField.get_name().equals(fields.get(j).get_name())) {
						Field field = fields.get(j);
						if (field != null) {
							layoutItemField.set_full_field_details(field);
							layoutItemFields.add(layoutItemField);
						} else {
							Log.warn(document.get_database_title(), tableName,
									"LayoutItem_Field " + layoutItemField.get_layout_display_name()
											+ " not found in document field list.");
						}
						break;
					}
				}

			} else {
				// the layoutItem is not a LayoutItem_Field
				org.glom.libglom.LayoutGroup subLayoutGroup = org.glom.libglom.LayoutGroup.cast_dynamic(layoutItem);
				if (subLayoutGroup != null) {
					// the layoutItem is a LayoutGroup
					LayoutItem_Portal layoutItemPortal = LayoutItem_Portal.cast_dynamic(layoutItem);
					if (layoutItemPortal == null) {
						// The subGroup is not a LayoutItem_Portal.
						// We're ignoring portals because they are filled by means of a separate SQL query.
						layoutItemFields.addAll(getFieldsToShowForSQLQueryAddGroup(subLayoutGroup));
					}
				}
			}
		}
		return layoutItemFields;
	}

	/**
	 * Gets the primary key Field for the current table name.
	 * 
	 * @return primary key Field
	 */
	protected Field getPrimaryKeyField() {
		return getPrimaryKeyFieldForTable(this.tableName);
	}

	/**
	 * Gets the primary key Field for the specified table name.
	 * 
	 * @param tableName
	 *            name of table to search for the primary key field
	 * @return primary key Field
	 */
	protected Field getPrimaryKeyFieldForTable(String tableName) {
		Field primaryKey = null;
		FieldVector fieldsVec = document.get_table_fields(tableName);
		for (int i = 0; i < Utils.safeLongToInt(fieldsVec.size()); i++) {
			Field field = fieldsVec.get(i);
			if (field.get_primary_key()) {
				primaryKey = field;
				break;
			}
		}
		return primaryKey;
	}

	/**
	 * Gets the primary key LayoutItem_Field for the specified table.
	 * 
	 * @param tableName
	 *            name of table to search for the primary key LayoutItem_Field
	 * @return primary key LayoutItem_Field
	 */
	protected LayoutItem_Field getPrimaryKeyLayoutItemField(String tableName) {
		Field primaryKey = getPrimaryKeyFieldForTable(tableName);

		LayoutItem_Field libglomLayoutItemField = new LayoutItem_Field();

		if (primaryKey != null) {
			libglomLayoutItemField.set_full_field_details(primaryKey);
		} else {
			Log.error(document.get_database_title(), this.tableName,
					"A primary key was not found in the FieldVector for this table.");
		}

		return libglomLayoutItemField;
	}

	/*
	 * Find the LayoutItem_Portal for the related list name
	 */
	final protected LayoutItem_Portal getPortal(String relationshipName) {

		LayoutGroupVector layoutGroupVec = document.get_data_layout_groups("details", tableName);
		// LayoutItem_Portal portal = null;
		for (int i = 0; i < layoutGroupVec.size(); i++) {
			org.glom.libglom.LayoutGroup layoutGroup = layoutGroupVec.get(i);
			LayoutItem_Portal portal = getPortal(relationshipName, layoutGroup);
			if (portal != null) {
				return portal;
			}
		}

		// the LayoutItem_Portal with relationshipName was not found
		return null;
	}

	/*
	 * Recursive helper method.
	 */
	final private LayoutItem_Portal getPortal(String relationshipName, org.glom.libglom.LayoutGroup layoutGroup) {

		if (relationshipName == null)
			return null;

		LayoutItemVector items = layoutGroup.get_items();
		for (int i = 0; i < items.size(); i++) {
			LayoutItem layoutItem = items.get(i);

			LayoutItem_Field layoutItemField = LayoutItem_Field.cast_dynamic(layoutItem);
			if (layoutItemField != null) {
				// the layoutItem is a LayoutItem_Field
				continue;

			} else {
				// the layoutItem is not a LayoutItem_Field
				org.glom.libglom.LayoutGroup subLayoutGroup = org.glom.libglom.LayoutGroup.cast_dynamic(layoutItem);
				if (subLayoutGroup != null) {
					// the layoutItem is a LayoutGroup
					LayoutItem_Portal layoutItemPortal = LayoutItem_Portal.cast_dynamic(layoutItem);
					if (layoutItemPortal != null) {
						// The subGroup is a LayoutItem_Protal
						if (relationshipName.equals(layoutItemPortal.get_relationship_name_used())) {
							// yey, we found it!
							return layoutItemPortal;
						}
					} else {
						// The subGroup is not a LayoutItem_Portal.
						LayoutItem_Portal retval = getPortal(relationshipName, subLayoutGroup);
						if (retval != null) {
							return retval;
						}
					}
				}
			}
		}

		// the LayoutItem_Portal with relationshipName was not found
		return null;
	}

}
