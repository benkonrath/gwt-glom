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

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.glom.web.server.Log;
import org.glom.web.server.SqlUtils;
import org.glom.web.server.Utils;
import org.glom.web.server.libglom.Document;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.SortClause;
import org.glom.web.shared.libglom.layout.UsesRelationship;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 *
 */
abstract class ListDBAccess extends DBAccess {
	List<LayoutItemField> fieldsToGet;

	ListDBAccess(final Document document, final String documentID, final ComboPooledDataSource cpds,
				 final String tableName) {
		super(document, documentID, cpds, tableName);
	}

	protected abstract String getSelectQuery(String quickFind, SortClause sortClause);

	protected abstract String getCountQuery();

	/**
	 *
	 * @param quickFind
	 * @param start
	 * @param length
	 * @param sortColumnIndex
	 *            The index of the column to sort by, or -1 for none.
	 * @param isAscending
	 * @return
	 */
	ArrayList<DataItem[]> getListData(final String quickFind, final int start, final int length,
									  final int sortColumnIndex, final boolean isAscending) {

		// create a sort clause for the column we've been asked to sort
		final SortClause sortClause = new SortClause();
		if (sortColumnIndex != -1) {
			final LayoutItem item = fieldsToGet.get(sortColumnIndex);
			if (item instanceof LayoutItemField) {
				final UsesRelationship layoutItemField = (UsesRelationship) item;
				sortClause.add(new SortClause.SortField(layoutItemField, isAscending));
			} else {
				Log.error(documentID, tableName, "Error getting LayoutItemField for column index " + sortColumnIndex
						+ ". Cannot create a sort clause for this column.");
			}
		} else {
			// create a sort clause for the primary key if we're not asked to sort a specific column
			int numItems = 0;
			if (fieldsToGet != null) {
				numItems = Utils.safeLongToInt(fieldsToGet.size());
			}

			for (int i = 0; i < numItems; i++) {
				final LayoutItemField layoutItem = fieldsToGet.get(i);
				final Field details = layoutItem.getFullFieldDetails();
				if (details != null && details.getPrimaryKey()) {
					sortClause.add(new SortClause.SortField(layoutItem, true)); // ascending
					break;
				}
			}
		}

		ArrayList<DataItem[]> rowsList = new ArrayList<>();
		ResultSet rs = null;
		try {

			//Change the timeout, because it otherwise takes ages to fail sometimes when the details are not setup.
			//This is more than enough.
			DriverManager.setLoginTimeout(5);

			// Setup the JDBC driver and get the query. Special care needs to be taken to ensure that the results will be
			// based on a cursor so that large amounts of memory are not consumed when the query retrieve a large amount
			// of data. Here's the relevant PostgreSQL documentation:
			// http://jdbc.postgresql.org/documentation/83/query.html#query-with-cursor

			final String query = getSelectQuery(quickFind, sortClause) + " OFFSET " + start;
			// TODO Test memory usage before and after we execute the query that would result in a large ResultSet.
			// We need to ensure that the JDBC driver is in fact returning a cursor based result set that has a low
			// memory footprint. Check the difference between this value before and after the query:
			// Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
			// Test the execution time at the same time (see the todo item in getLayoutListTable()).
			rs = SqlUtils.executeQuery(cpds, query, length);

			// get the results from the ResultSet
			final TypedDataItem primaryKeyValue = null; //TODO: Discover it for each row instead.
			rowsList = convertResultSetToDTO(length, fieldsToGet, primaryKeyValue, rs);
		} catch (final SQLException e) {
			Log.error(documentID, tableName, "Error executing database query.", e);
			// TODO: somehow notify user of problem
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
		return rowsList;
	}

	/*
	 * Get the number of rows a query with the table name and layout fields would return. This is needed for the /* list
	 * view pager.
	 */
	int getResultSizeOfSQLQuery() {

		ResultSet rs = null;
		try {
			//Change the timeout, because it otherwise takes ages to fail sometimes when the details are not setup.
			//This is more than enough.
			DriverManager.setLoginTimeout(5);

			// Setup and execute the count query. Special care needs to be take to ensure that the results will be based
			// on a cursor so that large amounts of memory are not consumed when the query retrieve a large amount of
			// data. Here's the relevant PostgreSQL documentation:
			// http://jdbc.postgresql.org/documentation/83/query.html#query-with-cursor
			final String query = getCountQuery();

			// TODO Test execution time of this query with when the number of rows in the table is large (say >
			// 1,000,000). Test memory usage at the same time (see the todo item in getTableData()).
			rs = SqlUtils.executeQuery(cpds, query);

			// get the number of rows in the query
			rs.next();
			return rs.getInt(1);

		} catch (final SQLException e) {
			Log.error(documentID, tableName, "Error calculating number of rows in the query.", e);
			return -1;
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
	}

}
