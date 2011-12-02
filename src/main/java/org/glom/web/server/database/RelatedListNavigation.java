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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.glom.libglom.Document;
import org.glom.libglom.Field;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.LayoutItem_Portal;
import org.glom.libglom.SqlBuilder;
import org.glom.libglom.Value;
import org.glom.web.server.Log;
import org.glom.web.server.Utils;
import org.glom.web.shared.NavigationRecord;
import org.glom.web.shared.TypedDataItem;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class RelatedListNavigation extends DBAccess {

	private LayoutItem_Portal portal;

	public RelatedListNavigation(Document document, String documentID, ComboPooledDataSource cpds, String tableName,
			String relationshipName) {
		super(document, documentID, cpds, tableName);

		LayoutItem_Portal portal = getPortal(relationshipName);
		if (portal == null) {
			Log.error(documentID, tableName, "Couldn't find LayoutItem_Portal \"" + relationshipName + "\" in table \""
					+ tableName + "\". " + "Cannot retrive data for the related list.");
			return;
		}

		this.portal = portal;
	}

	/*
	 * Gets a NavigationRecord for the related list given the primaryKeyValue.
	 * 
	 * This code was ported from Glom: Box_Data_Portal::get_suitable_record_to_view_details()
	 */
	public NavigationRecord getNavigationRecord(TypedDataItem primaryKeyValue) {

		if (portal == null) {
			Log.error(documentID, tableName,
					"The related list navigation cannot be determined because the LayoutItem_Portal has not been found.");
			return null;
		}

		StringBuffer navigationTableNameSB = new StringBuffer();
		LayoutItem_Field navigationRelationshipItem = new LayoutItem_Field();
		portal.get_suitable_table_to_view_details(navigationTableNameSB, navigationRelationshipItem, document);

		String navigationTableName = navigationTableNameSB.toString();
		if (navigationTableName.isEmpty()) {
			Log.error(documentID, tableName,
					"The related list navigation cannot cannot be determined because the navigation table name is empty.");
			return null;
		}

		// Get the primary key of that table:
		Field navigationTablePrimaryKey = getPrimaryKeyFieldForTable(navigationTableName);

		// Build a layout item to get the field's value:
		navigationRelationshipItem.set_full_field_details(navigationTablePrimaryKey);

		// Get the value of the navigation related primary key:
		LayoutFieldVector fieldsToGet = new LayoutFieldVector();
		fieldsToGet.add(navigationRelationshipItem);

		// For instance "invoice_line_id" if this is a portal to an "invoice_lines" table:
		String relatedTableName = portal.get_table_used("" /* not relevant */);
		Field relatedPrimaryKey = getPrimaryKeyFieldForTable(relatedTableName);

		NavigationRecord navigationRecord = new NavigationRecord();
		String query = null;
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			// Setup the JDBC driver and get the query.
			conn = cpds.getConnection();
			st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			Value gdaRelatedPrimaryKeyValue = Utils.getGlomTypeGdaValueForTypedDataItem(documentID, tableName,
					relatedPrimaryKey.get_glom_type(), primaryKeyValue);

			// Only create the query if we've created a Gda Value from the DataItem.
			if (gdaRelatedPrimaryKeyValue != null) {

				SqlBuilder builder = Glom.build_sql_select_with_key(relatedTableName, fieldsToGet, relatedPrimaryKey,
						gdaRelatedPrimaryKeyValue);
				query = Glom.sqlbuilder_get_full_query(builder);

				rs = st.executeQuery(query);

				// Set the output parameters:
				navigationRecord.setTableName(navigationTableName);

				rs.next();
				TypedDataItem tablePrimaryKeyValue = new TypedDataItem();
				ResultSetMetaData rsMetaData = rs.getMetaData();
				switch (rsMetaData.getColumnType(1)) {
				case java.sql.Types.NUMERIC:
					tablePrimaryKeyValue.setNumber(rs.getDouble(1));
					break;
				case java.sql.Types.VARCHAR:
					tablePrimaryKeyValue.setText(rs.getString(1));
					break;
				default:
					Log.warn(documentID, tableName, "Unsupported java.sql.Type: " + rsMetaData.getColumnTypeName(1));
					break;
				}

				// The value is empty when there there is no record to match the key in the related table:
				// For instance, if an invoice lines record mentions a product id, but the product does not exist in the
				// products table.
				if (tablePrimaryKeyValue.isEmpty()) {
					Log.info(documentID, tableName, "SQL query returned empty primary key for navigation to the "
							+ navigationTableName + "table. Navigation may not work correctly");
					navigationRecord.setPrimaryKeyValue(null);
				} else {
					navigationRecord.setPrimaryKeyValue(tablePrimaryKeyValue);
				}
			}
		} catch (SQLException e) {
			Log.error(documentID, tableName, "Error executing database query: " + query, e);
			// TODO: somehow notify user of problem
			return null;
		} finally {
			// cleanup everything that has been used
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				Log.error(documentID, tableName,
						"Error closing database resources. Subsequent database queries may not work.", e);
			}
		}

		return navigationRecord;
	}
}
