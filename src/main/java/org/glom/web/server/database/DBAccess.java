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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.glom.web.server.Log;
import org.glom.web.server.Utils;
import org.glom.web.server.libglom.Document;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
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
	final protected ArrayList<DataItem[]> convertResultSetToDTO(int length, List<LayoutItemField> layoutFields,
			ResultSet rs) throws SQLException {
		
	    final ResultSetMetaData rsMetaData = rs.getMetaData();
	    final int rsColumnscount = rsMetaData.getColumnCount();

		// get the data we've been asked for
		int rowCount = 0;
		ArrayList<DataItem[]> rowsList = new ArrayList<DataItem[]>();
		while (rs.next() && rowCount <= length) {
			int layoutFieldsSize = Utils.safeLongToInt(layoutFields.size());
			DataItem[] rowArray = new DataItem[layoutFieldsSize];
			for (int i = 0; i < layoutFieldsSize; i++) {
				// make a new DataItem to set the text and colors
				rowArray[i] = new DataItem(); //TODO: Performance: Set it in the rowArray at the end, to avoid repeated lookup via [].
				
				LayoutItemField field = layoutFields.get(i);

				if(i >= rsColumnscount) {
					Log.error("convertResultSetToDTO(): index i=" + i + "+1 (field=" + field.getName() + " is out of range for the ResultSet. Using empty string for value.");
					rowArray[i].setText("");
					continue;
				}

				final int rsIndex = i + 1; //Because java.sql.ResultSet is 1-indexed, for some reason.

				// Convert the field value to a string based on the glom type. We're doing the formatting on the
				// server side for now but it might be useful to move this to the client side.
				switch (field.getGlomType()) {
				case TYPE_TEXT:
					final String text = rs.getString(rsIndex);
					rowArray[i].setText(text != null ? text : "");
					break;
				case TYPE_BOOLEAN:
					rowArray[i].setBoolean(rs.getBoolean(rsIndex));
					break;
				case TYPE_NUMERIC:
					rowArray[i].setNumber(rs.getDouble(rsIndex));
					break;
				case TYPE_DATE:
					final Date date = rs.getDate(rsIndex);
					if (date != null) {
						// TODO: Pass Date and Time types instead of converting to text here?
						// TODO: Use a 4-digit-year short form, somehow.
						DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ROOT);
						rowArray[i].setText(dateFormat.format(date));
					} else {
						rowArray[i].setText("");
					}
					break;
				case TYPE_TIME:
					final Time time = rs.getTime(rsIndex);
					if (time != null) {
						DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.ROOT);
						rowArray[i].setText(timeFormat.format(time));
					} else {
						rowArray[i].setText("");
					}
					break;
				case TYPE_IMAGE:
					final byte[] image = rs.getBytes(rsIndex);
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
	 * Gets a list to use when generating an SQL query.
	 */
	protected List<LayoutItemField> getFieldsToShowForSQLQuery(List<LayoutGroup> layoutGroupVec) {
		List<LayoutItemField> listLayoutFIelds = new ArrayList<LayoutItemField>();

		// We will show the fields that the document says we should:
		for (int i = 0; i < layoutGroupVec.size(); i++) {
			LayoutGroup layoutGroup = layoutGroupVec.get(i);

			// satisfy the precondition of getDetailsLayoutGroup(String tableName, LayoutGroup
			// libglomLayoutGroup)
			if (layoutGroup == null)
				continue;

			// Get the fields:
			ArrayList<LayoutItemField> layoutItemFields = getFieldsToShowForSQLQueryAddGroup(layoutGroup);
			for (LayoutItemField layoutItem_Field : layoutItemFields) {
				listLayoutFIelds.add(layoutItem_Field);
			}
		}
		return listLayoutFIelds;
	}

	/*
	 * Gets an ArrayList of LayoutItem_Field objects to use when generating an SQL query.
	 * 
	 * @precondition libglomLayoutGroup must not be null
	 */
	private ArrayList<LayoutItemField> getFieldsToShowForSQLQueryAddGroup(final LayoutGroup libglomLayoutGroup) {

		final ArrayList<LayoutItemField> layoutItemFields = new ArrayList<LayoutItemField>();
		final List<LayoutItem> items = libglomLayoutGroup.getItems();
		final int numItems = Utils.safeLongToInt(items.size());
		for (int i = 0; i < numItems; i++) {
			LayoutItem layoutItem = items.get(i);

			if (layoutItem instanceof LayoutItemField) {
				final LayoutItemField layoutItemField = (LayoutItemField) layoutItem;
				// the layoutItem is a LayoutItem_Field
				
				//Make sure that it has full field details:
				//TODO: Is this necessary?
				String tableNameToUse = tableName;
				if (layoutItemField.getHasRelationshipName()) {
					tableNameToUse = layoutItemField.getTableUsed(tableName);
				}
				
				final Field field = document.getField(tableNameToUse, layoutItemField.getName());
				if (field != null) {
					layoutItemField.setFullFieldDetails(field);
				} else {
					Log.warn(document.getDatabaseTitleOriginal(), tableName, "LayoutItem_Field "
								+ layoutItemField.getLayoutDisplayName() + " not found in document field list.");
				}
		
				//Add it to the list:
				layoutItemFields.add(layoutItemField);
			} else if (layoutItem instanceof LayoutGroup) {
				LayoutGroup subLayoutGroup = (LayoutGroup) layoutItem;

				if (!(subLayoutGroup instanceof LayoutItemPortal)) {
					// The subGroup is not a LayoutItemPortal.
					// We're ignoring portals because they are filled by means of a separate SQL query.
					layoutItemFields.addAll(getFieldsToShowForSQLQueryAddGroup(subLayoutGroup));
				}
			}
		}
		return layoutItemFields;
	}

	/**
	 * Gets the primary key Field for the specified table name.
	 * 
	 * @param tableName
	 *            name of table to search for the primary key field
	 * @return primary key Field
	 */
	protected Field getPrimaryKeyField(String tableName) {
		Field primaryKey = null;
		List<Field> fieldsVec = document.getTableFields(tableName);
		for (int i = 0; i < Utils.safeLongToInt(fieldsVec.size()); i++) {
			Field field = fieldsVec.get(i);
			if (field.getPrimaryKey()) {
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
	protected LayoutItemField getPrimaryKeyLayoutItemField(String tableName) {
		Field primaryKey = getPrimaryKeyField(tableName);

		LayoutItemField libglomLayoutItemField = new LayoutItemField();

		if (primaryKey != null) {
			libglomLayoutItemField.setName(primaryKey.getName());
			libglomLayoutItemField.setFullFieldDetails(primaryKey);
		} else {
			Log.error(document.getDatabaseTitleOriginal(), this.tableName,
					"A primary key was not found in the FieldVector for this table.");
		}

		return libglomLayoutItemField;
	}

}
