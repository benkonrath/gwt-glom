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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.glom.libglom.Document;
import org.glom.libglom.Field;
import org.glom.libglom.FieldVector;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.Relationship;
import org.glom.libglom.SortClause;
import org.glom.libglom.SortFieldPair;
import org.glom.libglom.Value;
import org.jooq.Condition;
import org.jooq.SQLDialect;
import org.jooq.SelectFinalStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.Factory;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class SqlUtils {

	public static class UsesRelationship {

		private Relationship relationship;
		private Relationship relatedRelationship;

		public void setRelationship(final Relationship relationship) {
			this.relationship = relationship;
		}

		/**
		 * @param get_related_relationship
		 */
		public void setRelatedRelationship(final Relationship relationship) {
			this.relatedRelationship = relationship;
		}

		public Relationship getRelationship() {
			return relationship;
		}

		public Relationship getRelatedRelationship() {
			return relatedRelationship;
		}

		private boolean getHasRelationshipName() {
			if (relationship == null) {
				return false;
			}

			if (StringUtils.isEmpty(relationship.get_name())) {
				return false;
			}

			return true;
		}

		private boolean getHasRelatedRelationshipName() {
			if (relatedRelationship == null) {
				return false;
			}

			if (StringUtils.isEmpty(relatedRelationship.get_name())) {
				return false;
			}

			return true;
		}

		public String get_sql_join_alias_name() {
			String result = "";

			if (getHasRelationshipName() && relationship.get_has_fields()) // relationships that link to tables together
																			// via a field
			{
				// We use relationship_name.field_name instead of related_tableName.field_name,
				// because, in the JOIN below, will specify the relationship_name as an alias for the related table name
				result += ("relationship_" + relationship.get_name());

				if (getHasRelatedRelationshipName() && relatedRelationship.get_has_fields()) {
					result += ('_' + relatedRelationship.get_name());
				}
			}

			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		/*
		 * @Override public int hashCode() { final int prime = 31; int result = 1; result = prime * result +
		 * ((relatedRelationship == null) ? 0 : relatedRelationship.hashCode()); result = prime * result +
		 * ((relationship == null) ? 0 : relationship.hashCode()); return result; }
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 * 
		 * 
		 * TODO: This causes NullPointerExceptions when used from contains().
		 */
		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}

			if (obj == null) {
				return false;
			}

			if (!(obj instanceof UsesRelationship)) {
				return false;
			}

			final UsesRelationship other = (UsesRelationship) obj;
			if (relationship == null) {
				if (other.relationship != null) {
					return false;
				}
			} else if (!relationship_equals(relationship, other.relationship)) {
				return false;
			}

			if (relatedRelationship == null) {
				if (other.relatedRelationship != null) {
					return false;
				}
			} else if (!relationship_equals(relatedRelationship, other.relatedRelationship)) {
				return false;
			}

			return true;
		}

		/**
		 * We use this utility function because Relationship.equals() fails in the the generated SWIG C++ code with a
		 * NullPointerException.
		 */
		public static boolean relationship_equals(final Relationship a, final Relationship b) {
			if (a == null) {
				if (b == null) {
					return true;
				} else {
					return false;
				}
			}

			if (b == null) {
				return false;
			}

			final String a_name = a.get_name();
			final String b_name = b.get_name();

			if (!StringUtils.equals(a_name, b_name)) { // TODO: And the rest.
				return false;
			}

			return true;
		}
	}

	// TODO: Change to final ArrayList<LayoutItem_Field> fieldsToGet
	public static String build_sql_select_with_key(final Connection connection, final String tableName,
			final LayoutFieldVector fieldsToGet, final Field primaryKey, final Value gdaPrimaryKeyValue) {

		Condition whereClause = null; // Note that we ignore quickFind.
		if (gdaPrimaryKeyValue != null) {
			whereClause = build_simple_where_expression(tableName, primaryKey, gdaPrimaryKeyValue);
		}

		final SortClause sortClause = null; // Ignored.
		return build_sql_select_with_where_clause(connection, tableName, fieldsToGet, whereClause, sortClause);
	}

	public static Condition build_simple_where_expression(final String tableName, final Field primaryKey,
			final Value gdaPrimaryKeyValue) {

		Condition result = null;

		if (primaryKey == null) {
			return result;
		}

		final String fieldName = primaryKey.get_name();
		if (StringUtils.isEmpty(fieldName)) {
			return result;
		}

		// TODO: field() takes general SQL, not specifically a field name, so this is unsafe.
		final String sqlFieldName = get_sql_field_name(tableName, fieldName);
		final org.jooq.Field<Object> field = Factory.field(sqlFieldName);
		result = field.equal(gdaPrimaryKeyValue.get_double()); // TODO: Handle other types too.
		return result;
	}

	/*
	 * private static String build_sql_select_with_where_clause(final Connection connection, final String tableName,
	 * final LayoutFieldVector fieldsToGet) { final Condition whereClause = null; return
	 * build_sql_select_with_where_clause(connection, tableName, fieldsToGet, whereClause); }
	 */

	/*
	 * private static String build_sql_select_with_where_clause(final Connection connection, final String tableName,
	 * final LayoutFieldVector fieldsToGet, final Condition whereClause) { final SortClause sortClause = null; return
	 * build_sql_select_with_where_clause(connection, tableName, fieldsToGet, whereClause, sortClause); }
	 */

	public static String build_sql_select_with_where_clause(final Connection connection, final String tableName,
			final LayoutFieldVector fieldsToGet, final Condition whereClause, final SortClause sortClause) {
		final SelectFinalStep step = build_sql_select_step_with_where_clause(connection, tableName, fieldsToGet,
				whereClause, sortClause);
		if (step == null) {
			return "";
		}

		final String query = step.getQuery().getSQL(true);
		//Log.info("Query: " + query);
		return query;
	}

	private static SelectFinalStep build_sql_select_step_with_where_clause(final Connection connection,
			final String tableName, final LayoutFieldVector fieldsToGet, final Condition whereClause,
			final SortClause sortClause) {

		final Factory factory = new Factory(connection, SQLDialect.POSTGRES);
		final Settings settings = factory.getSettings();
		settings.setRenderNameStyle(RenderNameStyle.QUOTED); // TODO: This doesn't seem to have any effect.
		settings.setRenderKeywordStyle(RenderKeywordStyle.UPPER); // TODO: Just to make debugging nicer.

		// Add the fields, and any necessary joins:
		final SelectSelectStep selectStep = factory.select();
		final List<UsesRelationship> listRelationships = build_sql_select_add_fields_to_get(selectStep, tableName,
				fieldsToGet, sortClause, false /* extraJoin */);

		final SelectJoinStep joinStep = selectStep.from(tableName);

		// LEFT OUTER JOIN will get the field values from the other tables,
		// and give us our fields for this table even if there is no corresponding value in the other table.
		for (final UsesRelationship usesRelationship : listRelationships) {
			builder_add_join(joinStep, usesRelationship);
		}

		SelectFinalStep finalStep = joinStep;
		if (whereClause != null) {
			finalStep = joinStep.where(whereClause);
		}

		return finalStep;
	}

	public static String build_sql_count_select_with_where_clause(final Connection connection, final String tableName,
			final LayoutFieldVector fieldsToGet) {
		final SelectFinalStep selectInner = build_sql_select_step_with_where_clause(connection, tableName, fieldsToGet,
				null, null);
		return build_sql_select_count_rows(selectInner);
	}

	public static String build_sql_count_select_with_where_clause(final Connection connection, final String tableName,
			final LayoutFieldVector fieldsToGet, final Condition whereClause) {
		final SelectFinalStep selectInner = build_sql_select_step_with_where_clause(connection, tableName, fieldsToGet,
				whereClause, null);
		return build_sql_select_count_rows(selectInner);
	}

	private static String build_sql_select_count_rows(final SelectFinalStep selectInner) {
		// TODO: Find a way to do this with the jOOQ API:
		final String query = selectInner.getQuery().getSQL(true);
		return "SELECT COUNT(*) FROM (" + query + ") AS glomarbitraryalias";
	}

	private static List<UsesRelationship> build_sql_select_add_fields_to_get(SelectSelectStep step,
			final String tableName, final LayoutFieldVector fieldsToGet, final SortClause sortClause,
			final boolean extraJoin) {

		// Get all relationships used in the query:
		final List<UsesRelationship> listRelationships = new ArrayList<UsesRelationship>();

		final int layoutFieldsSize = Utils.safeLongToInt(fieldsToGet.size());
		for (int i = 0; i < layoutFieldsSize; i++) {
			final LayoutItem_Field layout_item = fieldsToGet.get(i);
			add_to_relationships_list(listRelationships, layout_item);
		}

		if (sortClause != null) {
			final int sortFieldsSize = Utils.safeLongToInt(sortClause.size());
			for (int i = 0; i < sortFieldsSize; i++) {
				final SortFieldPair pair = sortClause.get(i);
				final LayoutItem_Field layout_item = pair.getFirst();
				add_to_relationships_list(listRelationships, layout_item);
			}
		}

		boolean one_added = false;
		for (int i = 0; i < layoutFieldsSize; i++) {
			final LayoutItem_Field layout_item = fieldsToGet.get(i);

			if (layout_item == null) {
				// g_warn_if_reached();
				continue;
			}

			// Get the parent, such as the table name, or the alias name for the join:
			// final String parent = layout_item.get_sql_table_or_join_alias_name(tableName);

			/*
			 * TODO: const LayoutItem_FieldSummary* fieldsummary = dynamic_cast<const
			 * LayoutItem_FieldSummary*>(layout_item.obj()); if(fieldsummary) { const Gnome::Gda::SqlBuilder::Id
			 * id_function = builder->add_function( fieldsummary->get_summary_type_sql(),
			 * builder->add_field_id(layout_item->get_name(), tableName)); builder->add_field_value_id(id_function); }
			 * else {
			 */
			final String sql_field_name = get_sql_field_name(tableName, layout_item);
			if (!StringUtils.isEmpty(sql_field_name)) {
				// TODO Factory.field() takes SQL, which can be a field name,
				// but this does not interpret it as a field name, so this is unsafe.
				final org.jooq.Field<?> field = Factory.field(sql_field_name);
				step = step.select(field);

				// Avoid duplicate records with doubly-related fields:
				// TODO: if(extra_join)
				// builder->select_group_by(id);
			}
			// }

			one_added = true;
		}

		if (!one_added) {
			// TODO: std::cerr << G_STRFUNC << ": No fields added: fieldsToGet.size()=" << fieldsToGet.size() <<
			// std::endl;
			return listRelationships;
		}

		return listRelationships;
	}

	private static String get_sql_field_name(final String tableName, final String fieldName) {

		if (StringUtils.isEmpty(tableName)) {
			return "";
		}

		if (StringUtils.isEmpty(fieldName)) {
			return "";
		}

		// TODO: Quoting, escaping, etc:
		return tableName + "." + fieldName;
	}

	private static String get_sql_field_name(final String tableName, final LayoutItem_Field layoutItemField) {

		if (layoutItemField == null) {
			return "";
		}

		if (StringUtils.isEmpty(tableName)) {
			return "";
		}

		// TODO: Quoting, escaping, etc:
		return get_sql_field_name(layoutItemField.get_sql_table_or_join_alias_name(tableName),
				layoutItemField.get_name());
	}

	private static void add_to_relationships_list(final List<UsesRelationship> listRelationships,
			final LayoutItem_Field layout_item) {

		if (layout_item == null) {
			return;
		}

		if (!layout_item.get_has_relationship_name()) {
			return;
		}

		// If this is a related relationship, add the first-level relationship too, so that the related relationship can
		// be defined in terms of it:
		// TODO: //If the table is not yet in the list:
		if (layout_item.get_has_related_relationship_name()) {
			final UsesRelationship usesRel = new UsesRelationship();
			usesRel.setRelationship(layout_item.get_relationship());

			// Remove any UsesRelationship that has only the same relationship (not related relationship),
			// to avoid adding that part of the relationship to the SQL twice (two identical JOINS).
			// listRemoveIfUsesRelationship(listRelationships, usesRel.getRelationship());

			if (!listRelationships.contains(usesRel)) {
				// These need to be at the front, so that related relationships can use
				// them later in the SQL statement.
				listRelationships.add(usesRel);
			}

		}

		// Add the relationship to the list:
		final UsesRelationship usesRel = new UsesRelationship();
		usesRel.setRelationship(layout_item.get_relationship());
		usesRel.setRelatedRelationship(layout_item.get_related_relationship());
		if (!listRelationships.contains(usesRel)) {
			listRelationships.add(usesRel);
		}

	}

	/**
	 * @param listRelationships
	 * @param relationship
	 */
	/*
	 * private static void listRemoveIfUsesRelationship(final List<UsesRelationship> listRelationships, final
	 * Relationship relationship) { if (relationship == null) { return; }
	 * 
	 * final Iterator<UsesRelationship> i = listRelationships.iterator(); while (i.hasNext()) { final UsesRelationship
	 * eachUsesRel = i.next(); if (eachUsesRel == null) continue;
	 * 
	 * // Ignore these: if (eachUsesRel.getHasRelatedRelationshipName()) { continue; }
	 * 
	 * final Relationship eachRel = eachUsesRel.getRelationship(); if (eachRel == null) { continue; }
	 * 
	 * Log.info("Checking: rel name=" + relationship.get_name() + ", eachRel name=" + eachRel.get_name());
	 * 
	 * if (UsesRelationship.relationship_equals(relationship, eachRel)) { i.remove(); Log.info("  Removed"); } else {
	 * Log.info(" not equal"); }
	 * 
	 * } }
	 */

	private static void builder_add_join(SelectJoinStep step, final UsesRelationship uses_relationship) {
		final Relationship relationship = uses_relationship.getRelationship();
		if (!relationship.get_has_fields()) { // TODO: Handle related_record has_fields.
			if (relationship.get_has_to_table()) {
				// It is a relationship that only specifies the table, without specifying linking fields:

				// TODO: from() takes SQL, not specifically a table name, so this is unsafe.
				// TODO: stepResult = step.from(relationship.get_to_table());
			}

			return;
		}

		// Define the alias name as returned by get_sql_join_alias_name():

		// Specify an alias, to avoid ambiguity when using 2 relationships to the same table.
		final String alias_name = uses_relationship.get_sql_join_alias_name();

		// Add the JOIN:
		if (!uses_relationship.getHasRelatedRelationshipName()) {

			final String sql_field_name_from = get_sql_field_name(relationship.get_from_table(),
					relationship.get_from_field());
			final org.jooq.Field<Object> fieldFrom = Factory.field(sql_field_name_from);
			final String sql_field_name_to = get_sql_field_name(alias_name, relationship.get_to_field());
			final org.jooq.Field<Object> fieldTo = Factory.field(sql_field_name_to);
			final Condition condition = fieldFrom.equal(fieldTo);

			// TODO: join() takes SQL, not specifically an alias name, so this is unsafe.
			// Note that LEFT JOIN (used in libglom/GdaSqlBuilder) is apparently the same as LEFT OUTER JOIN.
			step = step.leftOuterJoin(relationship.get_to_table() + " AS " + alias_name).on(condition);
		} else {
			final UsesRelationship parent_relationship = new UsesRelationship();
			parent_relationship.setRelationship(relationship);
			final Relationship relatedRelationship = uses_relationship.getRelatedRelationship();

			final String sql_field_name_from = get_sql_field_name(parent_relationship.get_sql_join_alias_name(),
					relatedRelationship.get_from_field());
			final org.jooq.Field<Object> fieldFrom = Factory.field(sql_field_name_from);
			final String sql_field_name_to = get_sql_field_name(alias_name, relatedRelationship.get_to_field());
			final org.jooq.Field<Object> fieldTo = Factory.field(sql_field_name_to);
			final Condition condition = fieldFrom.equal(fieldTo);

			// TODO: join() takes SQL, not specifically an alias name, so this is unsafe.
			// Note that LEFT JOIN (used in libglom/GdaSqlBuilder) is apparently the same as LEFT OUTER JOIN.
			step = step.leftOuterJoin(relatedRelationship.get_to_table() + " AS " + alias_name).on(condition);
		}
	}

	public static Condition get_find_where_clause_quick(final Document document, final String tableName,
			final Value quickFindValue) {
		if (StringUtils.isEmpty(tableName)) {
			return null;
		}

		// TODO: if(Conversions::value_is_empty(quick_search))
		// return Gnome::Gda::SqlExpr();

		Condition condition = null;

		// TODO: Cache the list of all fields, as well as caching (m_Fields) the list of all visible fields:
		final FieldVector fields = document.get_table_fields(tableName);

		final int fieldsSize = Utils.safeLongToInt(fields.size());
		for (int i = 0; i < fieldsSize; i++) {
			final Field field = fields.get(i);
			if (field == null) {
				continue;
			}

			if (field.get_glom_type() != Field.glom_field_type.TYPE_TEXT) {
				continue;
			}

			final String sql_field_name = get_sql_field_name(tableName, field.get_name());
			final org.jooq.Field<Object> jooqField = Factory.field(sql_field_name);
			final Condition thisCondition = jooqField.equal(quickFindValue.get_string());

			if (condition == null) {
				condition = thisCondition;
			} else {
				condition = condition.or(thisCondition);
			}
		}

		return condition;
	}
}
