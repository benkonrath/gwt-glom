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

import java.util.ArrayList;

import org.glom.libglom.Document;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.Relationship;
import org.glom.libglom.SortClause;
import org.glom.libglom.SqlBuilder;
import org.glom.libglom.SqlExpr;
import org.glom.web.shared.DataItem;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class ListViewDBAccess extends ListDBAccess {

	public ListViewDBAccess(Document document, String documentID, ComboPooledDataSource cpds, String tableName,
			org.glom.libglom.LayoutGroup libglomLayoutGroup) {
		super(document, documentID, cpds, tableName);

		// Convert the libglom LayoutGroup object into a LayoutFieldVector suitable for SQL queries.
		LayoutGroupVector tempLayoutGroupVec = new LayoutGroupVector();
		tempLayoutGroupVec.add(libglomLayoutGroup);
		fieldsToGet = getFieldsToShowForSQLQuery(tempLayoutGroupVec);
	}

	public ArrayList<DataItem[]> getData(int start, int length, boolean useSortClause, int sortColumnIndex,
			boolean isAscending) {

		return getListData(start, length, useSortClause, sortColumnIndex, isAscending);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.server.ListDBAccess#getExpectedResultSize()
	 */
	public int getExpectedResultSize() {

		if (fieldsToGet == null || fieldsToGet.size() <= 0)
			return -1;
		return getResultSizeOfSQLQuery();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.server.ListDBAccess#getSQLQuery(org.glom.libglom.LayoutFieldVector,
	 * org.glom.libglom.SortClause)
	 */
	@Override
	protected String getSelectQuery(SortClause sortClause) {
		SqlExpr whereClause = new SqlExpr();// Ignored.
		Relationship extraJoin = new Relationship(); // Ignored.
		SqlBuilder builder = Glom.build_sql_select_with_where_clause(tableName, fieldsToGet, whereClause, extraJoin,
				sortClause);
		return Glom.sqlbuilder_get_full_query(builder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.server.ListDBAccess#getCountQuery(org.glom.libglom.LayoutFieldVector)
	 */
	@Override
	protected String getCountQuery() {
		SqlBuilder builder = Glom.build_sql_select_with_where_clause(tableName, fieldsToGet);
		SqlBuilder countBuilder = Glom.build_sql_select_count_rows(builder);
		return Glom.sqlbuilder_get_full_query(countBuilder);
	}
}
