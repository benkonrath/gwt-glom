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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.Log;
import org.glom.web.server.SqlUtils;
import org.glom.web.server.Utils;
import org.glom.web.server.libglom.Document;
import org.glom.web.shared.NavigationRecord;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.Field.GlomFieldType;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;
import org.glom.web.shared.libglom.layout.TableToViewDetails;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 *
 */
public class RelatedListNavigation extends DBAccess {

	private LayoutItemPortal portal;

	public RelatedListNavigation(final Document document, final String documentID, final ComboPooledDataSource cpds,
			final String tableName, final LayoutItemPortal portal) {
		super(document, documentID, cpds, tableName);

		if (portal == null) {
			Log.error(documentID, tableName, "portal is null in table \"" + tableName + "\". "
					+ "Cannot retrieve data for the related list.");
			return;
		}

		this.portal = portal;
	}

	/*
	 * Gets a NavigationRecord for the related list given the primaryKeyValue.
	 * 
	 * This code was ported from Glom: Box_Data_Portal::get_suitable_record_to_view_details()
	 */
	public NavigationRecord getNavigationRecord(final TypedDataItem primaryKeyValue) {

		if (portal == null) {
			Log.error(documentID, tableName,
					"The related list navigation cannot be determined because the LayoutItemPortal has not been found.");
			return null;
		}

		if (primaryKeyValue == null) {
			Log.error(documentID, tableName,
					"The related list navigation cannot be determined because the primaryKeyValue is null.");
			return null;
		}

		final TableToViewDetails navigationTable = document.getPortalSuitableTableToViewDetails(portal);
		if (navigationTable == null) {
			Log.error(documentID, tableName,
					"The related list navigation cannot cannot be determined because the navigation table details are empty.");
			return null;
		}

		if (StringUtils.isEmpty(navigationTable.tableName)) {
			Log.error(documentID, tableName,
					"The related list navigation cannot cannot be determined because the navigation table name is empty.");
			return null;
		}

		// Get the primary key of that table:
		final Field navigationTablePrimaryKey = document.getTablePrimaryKeyField(navigationTable.tableName);

		// Build a layout item to get the field's value:
		final LayoutItemField navigationRelationshipItem = new LayoutItemField();
		navigationRelationshipItem.setName(navigationTablePrimaryKey.getName());
		navigationRelationshipItem.setFullFieldDetails(navigationTablePrimaryKey);
		if (navigationTable.usesRelationship != null) {
			navigationRelationshipItem.setRelationship(navigationTable.usesRelationship.getRelationship());
			navigationRelationshipItem
					.setRelatedRelationship(navigationTable.usesRelationship.getRelatedRelationship());
		}

		// Get the value of the navigation related primary key:
		final List<LayoutItemField> fieldsToGet = new ArrayList<LayoutItemField>();
		fieldsToGet.add(navigationRelationshipItem);

		// For instance "invoice_line_id" if this is a portal to an "invoice_lines" table:
		final String relatedTableName = portal.getTableUsed("" /* not relevant */);
		final Field primaryKeyField = document.getTablePrimaryKeyField(relatedTableName);
		if (primaryKeyField == null) {
			Log.error(documentID, tableName,
					"The related table's primary key field could not be found, for related table " + relatedTableName);
			return null;
		}

		final NavigationRecord navigationRecord = new NavigationRecord();
		String query = null;
		ResultSet rs = null;
		try {
			if (primaryKeyValue != null) {
				
				// Make sure that the value knows its actual type,
				// in case it was received via a URL parameter as a string representation:
				Utils.transformUnknownToActualType(primaryKeyValue, primaryKeyField.getGlomType());

				query = SqlUtils.buildSqlSelectWithKey(relatedTableName, fieldsToGet, primaryKeyField, primaryKeyValue);

				rs = SqlUtils.executeQuery(cpds, query);

				// Set the output parameters:
				navigationRecord.setTableName(navigationTable.tableName);

				rs.next();
				final TypedDataItem navigationTablePrimaryKeyValue = new TypedDataItem();
				final ResultSetMetaData rsMetaData = rs.getMetaData();
				final int queryReturnValueType = rsMetaData.getColumnType(1);
				switch (navigationTablePrimaryKey.getGlomType()) {
				case TYPE_NUMERIC:
					if (queryReturnValueType == java.sql.Types.NUMERIC) {
						navigationTablePrimaryKeyValue.setNumber(rs.getDouble(1));
					} else {
						logNavigationTablePrimaryKeyTypeMismatchError(Field.GlomFieldType.TYPE_NUMERIC,
								rsMetaData.getColumnTypeName(1));
					}
					break;
				case TYPE_TEXT:
					if (queryReturnValueType == java.sql.Types.VARCHAR) {
						navigationTablePrimaryKeyValue.setText(rs.getString(1));
					} else {
						logNavigationTablePrimaryKeyTypeMismatchError(Field.GlomFieldType.TYPE_TEXT,
								rsMetaData.getColumnTypeName(1));
					}
					break;
				default:
					Log.error(documentID, tableName, "Unsupported java.sql.Type: " + rsMetaData.getColumnTypeName(1));
					Log.error(documentID, tableName,
							"The navigation table primary key value will not be created. This is a bug.");
					break;
				}

				// The value is empty when there there is no record to match the key in the related table:
				// For instance, if an invoice lines record mentions a product id, but the product does not exist in the
				// products table.
				if (navigationTablePrimaryKeyValue.isEmpty()) {
					Log.info(documentID, tableName, "SQL query returned empty primary key for navigation to the "
							+ navigationTable.tableName + "table. Navigation may not work correctly");
					navigationRecord.setPrimaryKeyValue(null);
				} else {
					navigationRecord.setPrimaryKeyValue(navigationTablePrimaryKeyValue);
				}
			}
		} catch (final SQLException e) {
			Log.error(documentID, tableName, "Error executing database query: " + query, e);
			// TODO: somehow notify user of problem
			return null;
		} finally {
			// cleanup everything that has been used
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (final Exception e) {
				Log.error(documentID, tableName,
						"Error closing database resources. Subsequent database queries may not work.", e);
			}
		}

		return navigationRecord;
	}

	private void logNavigationTablePrimaryKeyTypeMismatchError(final GlomFieldType glomType,
			final String queryReturnValueTypeName) {
		Log.error(documentID, tableName, "The expected type from the Glom document: " + glomType
				+ " doesn't match the type returned by the SQL query: " + queryReturnValueTypeName + ".");
		Log.error(documentID, tableName, "The navigation table primary key value will not be created. This is a bug.");

	}
}
