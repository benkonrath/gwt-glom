/*
 * Copyright (C) 2012 Openismus GmbH
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

import org.glom.libglom.Field;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.Relationship;
import org.glom.libglom.SortClause;
import org.glom.libglom.SqlBuilder;
import org.glom.libglom.SqlExpr;
import org.glom.libglom.Value;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class SqlUtils {

	// TODO: Change to final ArrayList<LayoutItem_Field> fieldsToGet
	public static String build_sql_select_with_key(final String tableName, final LayoutFieldVector fieldsToGet,
			final Field primaryKey, final Value gdaPrimaryKeyValue) {
		final SqlBuilder builder = Glom.build_sql_select_with_key(tableName, fieldsToGet, primaryKey,
				gdaPrimaryKeyValue);
		return Glom.sqlbuilder_get_full_query(builder);
	}

	public static String build_sql_select_with_where_clause(final String tableName,
			final LayoutFieldVector fieldsToGet, final SqlExpr whereClause, final SortClause sortClause) {
		final Relationship extraJoin = new Relationship(); // Ignored.
		final SqlBuilder builder = Glom.build_sql_select_with_where_clause(tableName, fieldsToGet, whereClause,
				extraJoin, sortClause);
		return Glom.sqlbuilder_get_full_query(builder);
	}

	public static String build_sql_count_select_with_where_clause(final String tableName,
			final LayoutFieldVector fieldsToGet) {
		final SqlBuilder builder_inner = Glom.build_sql_select_with_where_clause(tableName, fieldsToGet);
		final SqlBuilder builder = Glom.build_sql_select_count_rows(builder_inner);
		return Glom.sqlbuilder_get_full_query(builder);
	}

	public static String build_sql_count_select_with_where_clause(final String tableName,
			final LayoutFieldVector fieldsToGet, final SqlExpr whereClause) {
		final SqlBuilder builder_inner = Glom.build_sql_select_with_where_clause(tableName, fieldsToGet, whereClause);
		final SqlBuilder builder = Glom.build_sql_select_count_rows(builder_inner);
		return Glom.sqlbuilder_get_full_query(builder);
	}

}
