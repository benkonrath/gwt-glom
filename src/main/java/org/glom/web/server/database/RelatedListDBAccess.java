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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.Log;
import org.glom.web.server.SqlUtils;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Document;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.Relationship;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;
import org.glom.web.shared.libglom.layout.SortClause;
import org.jooq.Condition;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 *
 */
public class RelatedListDBAccess extends ListDBAccess {
	private TypedDataItem foreignKeyValue = null;
	private LayoutItemPortal portal = null;
	private String parentTable = null;
	private String whereClauseToTableName = null;
	private Field whereClauseToKeyField = null;

	public RelatedListDBAccess(final Document document, final String documentID, final ComboPooledDataSource cpds,
			final String tableName, final String relationshipName) {
		super(document, documentID, cpds, tableName);

		final LayoutItemPortal portal = getPortal(relationshipName);
		if (portal == null) {
			Log.error(documentID, tableName, "Couldn't find LayoutItem_Portal \"" + relationshipName + "\" in table \""
					+ tableName + "\". " + "Cannot retrive data for the related list.");
			return;
		}

		parentTable = tableName;
		// Reassign the tableName variable to table that is being used for the related list. This needs to be set before
		// getFieldsToShowForSQLQuery().
		this.tableName = portal.getTableUsed("" /* parent table - not relevant */);

		// Convert the libglom LayoutGroup object into a List<LayoutItem_Field> suitable for SQL queries.
		final List<LayoutGroup> tempLayoutGroupVec = new ArrayList<LayoutGroup>();
		tempLayoutGroupVec.add(portal);
		fieldsToGet = getFieldsToShowForSQLQuery(tempLayoutGroupVec);

		/*
		 * The code from the rest of this method was inspired by code from Glom:
		 * Base_DB::set_found_set_where_clause_for_portal()
		 */
		final Relationship relationship = portal.getRelationship();

		// Notice that, in the case that this is a portal to doubly-related records,
		// The WHERE clause mentions the first-related table (though by the alias defined in extra_join)
		// and we add an extra JOIN to mention the second-related table.

		whereClauseToTableName = relationship.getToTable();
		whereClauseToKeyField = getFieldInTable(relationship.getToField(), whereClauseToTableName);

		// Add primary key
		fieldsToGet.add(getPrimaryKeyLayoutItemField(this.tableName));

		final Relationship relationshipRelated = portal.getRelatedRelationship();
		if (relationshipRelated != null) {
			Log.error(documentID, tableName, "The related relationship " + relationshipRelated.getName()
					+ " is not empty but the related relationship code has not been implemented yet.");

			// FIXME port this Glom code to Java
			// @formatter:off
			/*
		    //Add the extra JOIN:
		    sharedptr<UsesRelationship> uses_rel_temp = sharedptr<UsesRelationship>::create();
		    uses_rel_temp->set_relationship(relationship);
		    found_set.m_extra_join = relationship;

		    //Adjust the WHERE clause appropriately for the extra JOIN:
		    whereClauseToTableName = uses_rel_temp->get_sql_join_alias_name();

		    const Glib::ustring to_field_name = uses_rel_temp->get_to_field_used();
		    where_clause_to_key_field = get_fields_for_table_one_field(relationship->get_to_table(), to_field_name);
		    std::cout << "extra_join=" << found_set.m_extra_join << std::endl;
		    std::cout << "extra_join where_clause_to_key_field=" << where_clause_to_key_field->get_name() << std::endl;
			 */
			// @formatter:on
		}

		// set portal field
		this.portal = portal;

	}

	public ArrayList<DataItem[]> getData(final int start, final int length, final TypedDataItem foreignKeyValue,
			final boolean useSortClause, final int sortColumnIndex, final boolean isAscending) {

		if (tableName == null || foreignKeyValue == null || foreignKeyValue.isEmpty()) {
			return null;
		}

		// Set the foreignKeyValue
		this.foreignKeyValue = foreignKeyValue;

		return getListData("" /* quickFind */, start, length, useSortClause, sortColumnIndex, isAscending);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.server.ListDBAccess#getExpectedResultSize()
	 */
	public int getExpectedResultSize(final TypedDataItem foreignKeyValue) {

		// Set the foreignKeyValue
		this.foreignKeyValue = foreignKeyValue;

		if (fieldsToGet == null || fieldsToGet.size() <= 0 || this.foreignKeyValue == null)
			return -1;

		return getResultSizeOfSQLQuery();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.server.ListDBAccess#getSelectQuery(org.glom.libglom.LayoutFieldVector,
	 * org.glom.libglom.SortClause)
	 */
	@Override
	protected String getSelectQuery(final Connection connection, final String quickFind, final SortClause sortClause) {
		// TODO: combine this method with getCountQuery() to remove duplicate code
		if (portal == null) {
			Log.error(documentID, parentTable,
					"The Portal has not been found. Cannot build query for the related list.");
			return "";
		}

		if (foreignKeyValue == null || foreignKeyValue.isEmpty()) {
			Log.error(documentID, parentTable,
					"The value for the foreign key has not been set. Cannot build query for the related list.");
			return "";
		}

		Condition whereClause = null; // Note that we ignore quickFind.
		// only attempt to make a where clause if it makes sense to do so
		if (!StringUtils.isEmpty(whereClauseToTableName)) {
			if (foreignKeyValue != null)
				whereClause = SqlUtils.build_simple_where_expression(whereClauseToTableName, whereClauseToKeyField,
						foreignKeyValue);
		}

		return SqlUtils.build_sql_select_with_where_clause(connection, tableName, fieldsToGet, whereClause, sortClause);

	}

	private Field getFieldInTable(final String fieldName, final String tableName) {

		if (StringUtils.isEmpty(tableName))
			return null;

		final List<Field> fields = document.getTableFields(tableName);
		for (int i = 0; i < fields.size(); i++) {
			final Field field = fields.get(i);
			if (fieldName.equals(field.getName())) {
				return field;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.server.ListDBAccess#getCountQuery()
	 */
	@Override
	protected String getCountQuery(final Connection connection) {
		// TODO: combine this method with getSelectQuery() to remove duplicate code
		if (portal == null) {
			Log.error(documentID, parentTable,
					"The Portal has not been found. Cannot build query for the related list.");
			return "";
		}

		if (foreignKeyValue == null || foreignKeyValue.isEmpty()) {
			Log.error(documentID, parentTable,
					"The value for the foreign key has not been set. Cannot build query for the related list.");
			return "";
		}

		Condition whereClause = null;
		// only attempt to make a where clause if it makes sense to do so
		if (!whereClauseToTableName.isEmpty() && whereClauseToKeyField != null) {
			if (foreignKeyValue != null)
				whereClause = SqlUtils.build_simple_where_expression(whereClauseToTableName, whereClauseToKeyField,
						foreignKeyValue);
		}

		return SqlUtils.build_sql_count_select_with_where_clause(connection, tableName, fieldsToGet, whereClause);
	}

}
