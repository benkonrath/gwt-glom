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

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.Relationship;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.SortClause;
import org.glom.web.shared.libglom.layout.UsesRelationship;
import org.glom.web.shared.libglom.layout.UsesRelationshipImpl;
import org.jooq.AggregateFunction;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectFinalStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.Factory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 * 
 */
public class SqlUtils {

	/**
	 * @param document
	 * @return
	 */
	private static ComboPooledDataSource createAndSetupDataSource(final Document document) {
		return createAndSetupDataSource(document.getHostingMode(), document.getConnectionServer(), document.getConnectionPort(), document.getConnectionDatabase());
	}
	
	static class JdbcConnectionDetails {
		String driverClass = null;
		String jdbcURL = null;
	};

	public static JdbcConnectionDetails getJdbcConnectionDetails(final Document document) {
		return getJdbcConnectionDetails(document.getHostingMode(), document.getConnectionServer(), document.getConnectionPort(), document.getConnectionDatabase());
	}

	public static JdbcConnectionDetails getJdbcConnectionDetails(final Document.HostingMode hostingMode, final String serverHost, int serverPort, final String database) {
		final JdbcConnectionDetails details = new JdbcConnectionDetails();

		String defaultDatabase = null;
		switch (hostingMode) {
			case HOSTING_MODE_POSTGRES_CENTRAL:
			case HOSTING_MODE_POSTGRES_SELF: {
				details.driverClass = "org.postgresql.Driver";
				details.jdbcURL = "jdbc:postgresql://";
				defaultDatabase = "template1";
				break;
			}
			case HOSTING_MODE_MYSQL_CENTRAL:
			case HOSTING_MODE_MYSQL_SELF: {
				details.driverClass = "com.mysql.jdbc.Driver";
				details.jdbcURL = "jdbc:mysql://";
				defaultDatabase = "INFORMATION_SCHEMA";
				break;
			}
			default: {
				// TODO: We allow self-hosting here, for testing,
				// but maybe the startup of self-hosting should happen here.
				Log.fatal("Error configuring the database connection." + " Only PostgreSQL and MYSQL hosting are supported.");
				// FIXME: Throw exception?
				return null;
			}
		}
			
		// setup the JDBC driver for the current glom document
		details.jdbcURL += serverHost + ":" + serverPort;

		String db = database;
		if (StringUtils.isEmpty(db)) {
			// Use the default PostgreSQL database, because ComboPooledDataSource.connect() fails otherwise.
			db = defaultDatabase;
		}
		details.jdbcURL += "/" + db; // TODO: Quote the database name?
		
		return details;
	}
	
	/**
	 * @param document
	 * @return
	 */
	private static ComboPooledDataSource createAndSetupDataSource(final Document.HostingMode hostingMode, final String serverHost, int serverPort, final String database) {
		final ComboPooledDataSource cpds = new ComboPooledDataSource();

		final JdbcConnectionDetails details = getJdbcConnectionDetails(hostingMode, serverHost, serverPort, database);
		if (details == null) {
			return null;
		}

		try {
			cpds.setDriverClass(details.driverClass);
		} catch (final PropertyVetoException e) {
			Log.fatal("Error loading the PostgreSQL JDBC driver."
					+ " Is the PostgreSQL JDBC jar available to the servlet?", e);
			return null;
		}
		
		cpds.setJdbcUrl(details.jdbcURL);

		return cpds;
	}

	/**
	 * Sets the username and password for the database associated with the Glom document.
	 * 
	 * @return true if the username and password works, false otherwise
	 */
	public static ComboPooledDataSource tryUsernameAndPassword(final Document document, final String username, final String password) throws SQLException {
		ComboPooledDataSource cpds = createAndSetupDataSource(document);
		if (cpds == null)
			return null;
		
		/* Do not bother trying if there are no credentials. */
		if(StringUtils.isEmpty(username) && StringUtils.isEmpty(password)) {
			return null;
		}

		cpds.setUser(username);
		cpds.setPassword(password);

		final int acquireRetryAttempts = cpds.getAcquireRetryAttempts();
		cpds.setAcquireRetryAttempts(1);
		Connection conn = null;
		try {
			// FIXME find a better way to check authentication
			// it's possible that the connection could be failing for another reason
			
			//Change the timeout, because it otherwise takes ages to fail sometimes when the details are not setup.
			//This is more than enough.
			DriverManager.setLoginTimeout(5); 
			
			conn = cpds.getConnection();
			return cpds;
		} catch (final SQLException e) {
			Log.info(Utils.getFileName(document.getFileURI()), e.getMessage());
			Log.info(Utils.getFileName(document.getFileURI()),
					"Connection Failed. Maybe the username or password is not correct.");
			return null;
		} finally {
			if (conn != null) {
				conn.close();
			}
			cpds.setAcquireRetryAttempts(acquireRetryAttempts);
		}
	}

	public static ResultSet executeQuery(final ComboPooledDataSource cpds, final String query) throws SQLException {
		return executeQuery(cpds, query, 0);
	}

	public static ResultSet executeQuery(final ComboPooledDataSource cpds, final String query, int expectedLength) throws SQLException {
		if(cpds == null) {
			Log.error("cpds is null.");
			return null;
		}

		// Setup the JDBC driver and run the query.
		final Connection conn = cpds.getConnection();
		if(conn == null) {
			Log.error("The connection is null.");
			return null;
		}
		
		return executeQuery(conn, query, expectedLength);
	}

	public static ResultSet executeQuery(final Connection conn, final String query) throws SQLException {
		return executeQuery(conn, query, 0);
	}

	public static ResultSet executeQuery(final Connection conn, final String query, int expectedLength) throws SQLException {
		// Setup and execute the query. Special care needs to be take to ensure that the results will be based
		// on a cursor so that large amounts of memory are not consumed when the query retrieve a large amount of
		// data. Here's the relevant PostgreSQL documentation:
		// http://jdbc.postgresql.org/documentation/83/query.html#query-with-cursor
		conn.setAutoCommit(false);
		final Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if(expectedLength > 0) {
			st.setFetchSize(expectedLength);
		}

		return st.executeQuery(query);
	}
	
	/**
	 * @param connection
	 * @param query
	 */
	public static void executeUpdate(final Connection conn, final String query) throws SQLException {
		conn.setAutoCommit(false);
		final Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.executeUpdate(query);
	}
	
	// TODO: Change to final ArrayList<LayoutItem_Field> fieldsToGet
	public static String buildSqlSelectWithKey(final String tableName, final List<LayoutItemField> fieldsToGet,
			final Field primaryKey, final TypedDataItem primaryKeyValue, final SQLDialect sqlDialect) {

		Condition whereClause = null; // Note that we ignore quickFind.
		if (primaryKeyValue != null) {
			whereClause = buildSimpleWhereExpression(tableName, primaryKey, primaryKeyValue);
		}

		final SortClause sortClause = null; // Ignored.
		return buildSqlSelectWithWhereClause(tableName, fieldsToGet, whereClause, sortClause, sqlDialect);
	}

	public static Condition buildSimpleWhereExpression(final String tableName, final Field primaryKey,
			final TypedDataItem primaryKeyValue) {

		Condition result = null;

		if (primaryKey == null) {
			return result;
		}

		final String fieldName = primaryKey.getName();
		if (StringUtils.isEmpty(fieldName)) {
			return result;
		}

		final org.jooq.Field<Object> field = createField(tableName, fieldName);
		result = field.equal(primaryKeyValue.getValue());
		return result;
	}

	/*
	 * private static String buildSqlSelectWithWhereClause(final String tableName, final LayoutFieldVector fieldsToGet)
	 * { final Condition whereClause = null; return buildSqlSelectWithWhereClause(tableName, fieldsToGet, whereClause);
	 * }
	 */

	/*
	 * private static String buildSqlSelectWithWhereClause(final String tableName, final LayoutFieldVector fieldsToGet,
	 * final Condition whereClause) { final SortClause sortClause = null; return
	 * buildSqlSelectWithWhereClause(tableName, fieldsToGet, whereClause, sortClause); }
	 */

	public static String buildSqlSelectWithWhereClause(final String tableName, final List<LayoutItemField> fieldsToGet,
			final Condition whereClause, final SortClause sortClause, final SQLDialect sqlDialect) {
		final SelectFinalStep step = buildSqlSelectStepWithWhereClause(tableName, fieldsToGet, whereClause, sortClause, sqlDialect);
		if (step == null) {
			return "";
		}

		final String query = step.getQuery().getSQL(true);
		// Log.info("Query: " + query);
		return query;
	}

	private static SelectSelectStep createSelect(final SQLDialect sqlDialect) {
		final Factory factory = new Factory(sqlDialect);
		final Settings settings = factory.getSettings();
		settings.setRenderNameStyle(RenderNameStyle.QUOTED); // TODO: This doesn't seem to have any effect.
		settings.setRenderKeywordStyle(RenderKeywordStyle.UPPER); // TODO: Just to make debugging nicer.

		final SelectSelectStep selectStep = factory.select();
		return selectStep;
	}

	private static SelectFinalStep buildSqlSelectStepWithWhereClause(final String tableName,
			final List<LayoutItemField> fieldsToGet, final Condition whereClause, final SortClause sortClause, final SQLDialect sqlDialect) {

		final SelectSelectStep selectStep = createSelect(sqlDialect);

		// Add the fields, and any necessary joins:
		final List<UsesRelationship> listRelationships = buildSqlSelectAddFieldsToGet(selectStep, tableName,
				fieldsToGet, sortClause, false /* extraJoin */);

		final Table<Record> table = Factory.tableByName(tableName);
		final SelectJoinStep joinStep = selectStep.from(table);

		// LEFT OUTER JOIN will get the field values from the other tables,
		// and give us our fields for this table even if there is no corresponding value in the other table.
		for (final UsesRelationship usesRelationship : listRelationships) {
			builderAddJoin(joinStep, usesRelationship);
		}

		SelectFinalStep finalStep = joinStep;
		if (whereClause != null) {
			finalStep = joinStep.where(whereClause);
		}

		return finalStep;
	}

	public static String buildSqlCountSelectWithWhereClause(final String tableName,
			final List<LayoutItemField> fieldsToGet, final SQLDialect sqlDialect) {
		final SelectFinalStep selectInner = buildSqlSelectStepWithWhereClause(tableName, fieldsToGet, null, null, sqlDialect);
		return buildSqlSelectCountRows(selectInner, sqlDialect);
	}

	public static String buildSqlCountSelectWithWhereClause(final String tableName,
			final List<LayoutItemField> fieldsToGet, final Condition whereClause, final SQLDialect sqlDialect) {
		final SelectFinalStep selectInner = buildSqlSelectStepWithWhereClause(tableName, fieldsToGet, whereClause, null, sqlDialect);
		return buildSqlSelectCountRows(selectInner, sqlDialect);
	}

	private static String buildSqlSelectCountRows(final SelectFinalStep selectInner, final SQLDialect sqlDialect) {
		// TODO: Find a way to do this with the jOOQ API:
		final SelectSelectStep select = createSelect(sqlDialect);

		final org.jooq.Field<?> field = Factory.field("*");
		final AggregateFunction<?> count = Factory.count(field);
		select.select(count).from(selectInner);
		return select.getQuery().getSQL(true);
		// return "SELECT COUNT(*) FROM (" + query + ") AS glomarbitraryalias";
	}

	private static List<UsesRelationship> buildSqlSelectAddFieldsToGet(SelectSelectStep step, final String tableName,
			final List<LayoutItemField> fieldsToGet, final SortClause sortClause, final boolean extraJoin) {

		// Get all relationships used in the query:
		final List<UsesRelationship> listRelationships = new ArrayList<UsesRelationship>();

		final int layoutFieldsSize = Utils.safeLongToInt(fieldsToGet.size());
		for (int i = 0; i < layoutFieldsSize; i++) {
			final UsesRelationship layoutItem = fieldsToGet.get(i);
			addToRelationshipsList(listRelationships, layoutItem);
		}

		if (sortClause != null) {
			final int sortFieldsSize = Utils.safeLongToInt(sortClause.size());
			for (int i = 0; i < sortFieldsSize; i++) {
				final SortClause.SortField pair = sortClause.get(i);
				final UsesRelationship layoutItem = pair.field;
				addToRelationshipsList(listRelationships, layoutItem);
			}
		}

		boolean oneAdded = false;
		for (int i = 0; i < layoutFieldsSize; i++) {
			final LayoutItemField layoutItem = fieldsToGet.get(i);

			if (layoutItem == null) {
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
			final org.jooq.Field<?> field = createField(tableName, layoutItem);
			if (field != null) {
				step = step.select(field);

				// Avoid duplicate records with doubly-related fields:
				// TODO: if(extra_join)
				// builder->select_group_by(id);
			}
			// }

			oneAdded = true;
		}

		if (!oneAdded) {
			// TODO: std::cerr << G_STRFUNC << ": No fields added: fieldsToGet.size()=" << fieldsToGet.size() <<
			// std::endl;
			return listRelationships;
		}

		return listRelationships;
	}

	private static org.jooq.Field<Object> createField(final String tableName, final String fieldName) {
		if (StringUtils.isEmpty(tableName)) {
			return null;
		}

		if (StringUtils.isEmpty(fieldName)) {
			return null;
		}

		return Factory.fieldByName(tableName, fieldName);
	}

	private static org.jooq.Field<Object> createField(final String tableName, final LayoutItemField layoutField) {
		if (StringUtils.isEmpty(tableName)) {
			return null;
		}

		if (layoutField == null) {
			return null;
		}

		return createField(layoutField.getSqlTableOrJoinAliasName(tableName), layoutField.getName());
	}

	private static void addToRelationshipsList(final List<UsesRelationship> listRelationships,
			final UsesRelationship layoutItem) {

		if (layoutItem == null) {
			return;
		}

		if (!layoutItem.getHasRelationshipName()) {
			return;
		}

		// If this is a related relationship, add the first-level relationship too, so that the related relationship can
		// be defined in terms of it:
		// TODO: //If the table is not yet in the list:
		if (layoutItem.getHasRelatedRelationshipName()) {
			final UsesRelationship usesRel = new UsesRelationshipImpl();
			usesRel.setRelationship(layoutItem.getRelationship());

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
		final UsesRelationship usesRel = new UsesRelationshipImpl();
		usesRel.setRelationship(layoutItem.getRelationship());
		usesRel.setRelatedRelationship(layoutItem.getRelatedRelationship());
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

	private static void builderAddJoin(SelectJoinStep step, final UsesRelationship usesRelationship) {
		final Relationship relationship = usesRelationship.getRelationship();
		if (!relationship.getHasFields()) { // TODO: Handle related_record has_fields.
			if (relationship.getHasToTable()) {
				// It is a relationship that only specifies the table, without specifying linking fields:

				// Table<Record> toTable = Factory.tableByName(relationship.getToTable());
				// TODO: stepResult = step.from(toTable);
			}

			return;
		}

		// Define the alias name as returned by getSqlJoinAliasName():

		// Specify an alias, to avoid ambiguity when using 2 relationships to the same table.
		final String aliasName = usesRelationship.getSqlJoinAliasName();

		// Add the JOIN:
		if (!usesRelationship.getHasRelatedRelationshipName()) {

			final org.jooq.Field<Object> fieldFrom = createField(relationship.getFromTable(),
					relationship.getFromField());
			final org.jooq.Field<Object> fieldTo = createField(aliasName, relationship.getToField());
			final Condition condition = fieldFrom.equal(fieldTo);

			// Note that LEFT JOIN (used in libglom/GdaSqlBuilder) is apparently the same as LEFT OUTER JOIN.
			final Table<Record> toTable = Factory.tableByName(relationship.getToTable());
			step = step.leftOuterJoin(toTable.as(aliasName)).on(condition);
		} else {
			final UsesRelationship parentRelationship = new UsesRelationshipImpl();
			parentRelationship.setRelationship(relationship);
			final Relationship relatedRelationship = usesRelationship.getRelatedRelationship();

			final org.jooq.Field<Object> fieldFrom = createField(parentRelationship.getSqlJoinAliasName(),
					relatedRelationship.getFromField());
			final org.jooq.Field<Object> fieldTo = createField(aliasName, relatedRelationship.getToField());
			final Condition condition = fieldFrom.equal(fieldTo);

			// Note that LEFT JOIN (used in libglom/GdaSqlBuilder) is apparently the same as LEFT OUTER JOIN.
			final Table<Record> toTable = Factory.tableByName(relatedRelationship.getToTable());
			step = step.leftOuterJoin(toTable.as(aliasName)).on(condition);
		}
	}

	public static Condition getFindWhereClauseQuick(final Document document, final String tableName,
			final TypedDataItem quickFindValue) {
		if (StringUtils.isEmpty(tableName)) {
			return null;
		}

		// TODO: if(Conversions::value_is_empty(quick_search))
		// return Gnome::Gda::SqlExpr();

		Condition condition = null;

		// TODO: Cache the list of all fields, as well as caching (m_Fields) the list of all visible fields:
		final List<Field> fields = document.getTableFields(tableName);

		final int fieldsSize = Utils.safeLongToInt(fields.size());
		for (int i = 0; i < fieldsSize; i++) {
			final Field field = fields.get(i);
			if (field == null) {
				continue;
			}

			if (field.getGlomType() != Field.GlomFieldType.TYPE_TEXT) {
				continue;
			}

			final org.jooq.Field<Object> jooqField = createField(tableName, field.getName());

			// Do a case-insensitive substring search:
			// TODO: Use ILIKE: http://sourceforge.net/apps/trac/jooq/ticket/1423
			// http://groups.google.com/group/jooq-user/browse_thread/thread/203ae5a1a06ae65f
			final Condition thisCondition = jooqField.lower().contains(quickFindValue.getText().toLowerCase());

			if (condition == null) {
				condition = thisCondition;
			} else {
				condition = condition.or(thisCondition);
			}
		}

		return condition;
	}

	/**
	 * @param dataItem
	 * @param field
	 * @param rsIndex
	 * @param rs
	 * @param primaryKeyValue
	 * @throws SQLException
	 */
	public static void fillDataItemFromResultSet(final DataItem dataItem, final LayoutItemField field, final int rsIndex,
			final ResultSet rs, final String documentID, final String tableName, final TypedDataItem primaryKeyValue) throws SQLException {
		
		switch (field.getGlomType()) {
		case TYPE_TEXT:
			final String text = rs.getString(rsIndex);
			dataItem.setText(text != null ? text : "");
			break;
		case TYPE_BOOLEAN:
			dataItem.setBoolean(rs.getBoolean(rsIndex));
			break;
		case TYPE_NUMERIC:
			dataItem.setNumber(rs.getDouble(rsIndex));
			break;
		case TYPE_DATE:
			final Date date = rs.getDate(rsIndex);
			if (date != null) {
				// TODO: Pass Date and Time types instead of converting to text here?
				// TODO: Use a 4-digit-year short form, somehow.
				final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ROOT);
				dataItem.setText(dateFormat.format(date));
			} else {
				dataItem.setText("");
			}
			break;
		case TYPE_TIME:
			final Time time = rs.getTime(rsIndex);
			if (time != null) {
				final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.ROOT);
				dataItem.setText(timeFormat.format(time));
			} else {
				dataItem.setText("");
			}
			break;
		case TYPE_IMAGE:
			//We don't get the data here.
			//Instead we provide a way for the client to get the image separately.
			
			//This doesn't seem to work,
			//presumably because the base64 encoding is wrong:
			//final byte[] imageByteArray = rs.getBytes(rsIndex);
			//if (imageByteArray != null) {
			//	String base64 = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(imageByteArray);
			//	base64 = "data:image/png;base64," + base64;
			
			final String url = Utils.buildImageDataUrl(primaryKeyValue, documentID, tableName, field);
			dataItem.setImageDataUrl(url);
			break;
		case TYPE_INVALID:
		default:
			Log.warn(documentID, tableName, "Invalid LayoutItem Field type. Using empty string for value.");
			dataItem.setText("");
			break;
		}
	}
}
