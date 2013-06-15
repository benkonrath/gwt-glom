/*
 * Copyright (C) 2013 Openismus GmbH
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

package org.glom.web.server.users;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.SqlUtils;
import org.glom.web.server.libglom.ServerDetails;
import org.jooq.Condition;
import org.jooq.DeleteConditionStep;
import org.jooq.DeleteFinalStep;
import org.jooq.DeleteWhereStep;
import org.jooq.Field;
import org.jooq.InsertFinalStep;
import org.jooq.InsertSetMoreStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.SelectFinalStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.Factory;

/**
 * This class creates and uses a SQL database with
 * the structure expected by spring-security.
 * See http://static.springsource.org/spring-security/site/docs/3.1.x/reference/appendix-schema.html
 *
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class UserDatabase {

	/**
	 * 
	 */
	private static final String TABLENAME_USERS = "users";
	ServerDetails serverDetails = null;
	String serverUsername = null;
	String serverPassword = null;
	final static String DATABASE_NAME = "dbUsers";

	//TODO: Avoid passing the password around and avoid keeping it as a member variable?
	/**
	 *
	 */
	public UserDatabase(final ServerDetails serverDetails, final String masterUsername, final String masterPassword) {
		this.serverDetails = serverDetails;
		this.serverUsername = masterUsername;
		this.serverPassword = masterPassword;
	}

	private void closeConnection(final Connection connection) {
		if(connection == null) {
			return;
		}

		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean databaseExists() {
		final Connection connection = createConnectionToDatabase();
		closeConnection(connection);

		return connection != null;
	}
	

	private boolean executeQueryWithoutDatabase(final String sql) {
		final Connection connection = createConnectionWithoutDatabase();
		if(connection == null) {
			return false;
		}

		try {
			executeQuery(connection, sql);
			closeConnection(connection);
		} catch (final Exception ex) {
			closeConnection(connection);
			
			return false;
		}

		return true;
	}

	private void executeQuery(final Connection connection, final String sql) throws DataAccessException {
		final Factory factory = createJooqFactory(connection);

		try {
			factory.execute(sql);
		} catch (DataAccessException e) {
			System.out.println("createDatabase(): query failed: " + sql);
			e.printStackTrace();

			throw e;
		}
	}

	private ResultSet executeQuery(final Connection connection, final String sql, final int expectedLength) throws DataAccessException, SQLException {
		final Statement st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if(expectedLength > 0) {
			st.setFetchSize(expectedLength);
		}

		try {
			return st.executeQuery(sql);
		} catch (DataAccessException e) {
			System.out.println("createDatabase(): query failed: " + sql);
			e.printStackTrace();

			throw e;
		}
	}

	/**
	 * @param connection
	 * @return
	 */
	private Factory createJooqFactory(final Connection connection) {
		final Factory factory = new Factory(connection, SqlUtils.getJooqSqlDialect(serverDetails.hostingMode));
		
		final Settings settings = factory.getSettings();
		settings.setRenderNameStyle(RenderNameStyle.QUOTED); // TODO: This doesn't seem to have any effect.
		settings.setRenderKeywordStyle(RenderKeywordStyle.UPPER); // TODO: Just to make debugging nicer.

		return factory;
	}

	public boolean createDatabase() {
		if(databaseExists()) {
			return true;
		}

		final String query = "CREATE DATABASE " + quoteAndEscapeSqlId(DATABASE_NAME);
		if(!executeQueryWithoutDatabase(query)) {
			return false;
		}

		return createTables();
	}
	
	public boolean removeDatabase() {

		final String query = "DROP DATABASE " + quoteAndEscapeSqlId(DATABASE_NAME);
		return executeQueryWithoutDatabase(query);
	}

	/** Create a connection to the database server,
	 * but not to a specific database.
	 * 
	 * @return
	 */
	private Connection createConnectionWithoutDatabase() {
		return createConnection(serverDetails, "", serverUsername, serverPassword);
	}
	
	private Connection createConnectionToDatabase() {
		return createConnection(serverDetails, DATABASE_NAME, serverUsername, serverPassword);
	}

	private static Connection createConnection(final ServerDetails serverDetails, final String database, final String username, final String password) {
		if(StringUtils.isEmpty(username)) {
			System.out.println("setInitialUsernameAndPassword(): username is empty.");
			return null;
		}

		//Previously: final ServerDetails serverDetails = document.getConnectionDetails();
		final SqlUtils.JdbcConnectionDetails details = SqlUtils.getJdbcConnectionDetails(serverDetails, database);
		if (details == null) {
			System.out.println("setInitialUsernameAndPassword(): getJdbcConnectionDetails() returned null.");
			return null;
		}
		
		final Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);

		Connection conn = null;
		try {
			//TODO: Remove these debug prints when we figure out why getConnection sometimes hangs. 
			//System.out.println("debug: SelfHosterMySQL.createConnection(): before createConnection()");
			DriverManager.setLoginTimeout(10);
			conn = DriverManager.getConnection(details.jdbcURL, connectionProps);
			//System.out.println("debug: SelfHosterMySQL.createConnection(): before createConnection()");
		} catch (final SQLException e) {
			/*
			if(!failureExpected) {
				e.printStackTrace();
			}
			*/
			return null;
		}

		return conn;
	}

	/**
	 * @param name
	 * @return
	 */
	private String quoteAndEscapeSqlId(final String name) {
		return SqlUtils.quoteAndEscapeSqlId(name, SqlUtils.getJooqSqlDialect(serverDetails.hostingMode));
	} 

	void createUser(final String username, final String password) {
		final Factory factory = createJooqFactory(null);

		final Table<Record> table = Factory.tableByName(TABLENAME_USERS);
		final InsertSetStep<Record> insertSetStep = factory.insertInto(table);
		
		//TODO: Use constants:
		final Field<Object> fieldUsername = Factory.fieldByName("username"); //TODO: Avoid recreating this.
		InsertSetMoreStep<Record> step = insertSetStep.set(fieldUsername, username);
		final Field<Object> fieldPassword = Factory.fieldByName("password");
		step = step.set(fieldPassword, password);
		final Field<Object> fieldEnabled = Factory.fieldByName("enabled");
		step = step.set(fieldEnabled, true);
		
		InsertFinalStep<Record> finalStep = step;
		
		//TODO: Avoid copy/pasting this:
				
		//TODO: Make sure this is cached:
		final Connection connection = createConnectionToDatabase();
		if(connection == null) {
			//TODO: Throw exception?
			return;
		}

		final String query = finalStep.getSQL(true); //TODO: Find out why, with SelectFinalStep we need .getQuery().getSQL(true);

		try {
			executeQuery(connection, query);
			closeConnection(connection); //TODO: This won't be necessary if we cache. Why aren't we using C3P0?
		} catch (final DataAccessException ex) {
			closeConnection(connection);
			
			return;
		}
	}

	void removeUser(final String username) {
		final Factory factory = createJooqFactory(null);

		final Table<Record> table = Factory.tableByName(TABLENAME_USERS); //TODO: Avoid recreating this.
		final DeleteWhereStep<Record> deleteWhereStep = factory.delete(table);

		final org.jooq.Field<Object> field = SqlUtils.createField(TABLENAME_USERS, "username");
		final Condition condition = field.equal(username);

		final DeleteConditionStep<Record> deleteConditionStep = deleteWhereStep.where(condition);
		final DeleteFinalStep<Record> step = deleteConditionStep;

		//TODO: Make sure this is cached:
		final Connection connection = createConnectionToDatabase();
		if(connection == null) {
			//TODO: Throw exception?
			return;
		}

		final String query = step.getSQL(true); //TODO: Find out why, with SelectFinalStep we need .getQuery().getSQL(true);

		try {
			executeQuery(connection, query);
			closeConnection(connection); //TODO: This won't be necessary if we cache. Why aren't we using C3P0?
		} catch (final DataAccessException ex) {
			closeConnection(connection);
			
			return;
		}
	}

	/**
	 * 
	 */
	public void remonveDatabase() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @return
	 */
	private boolean createTables() {
		//See http://static.springsource.org/spring-security/site/docs/3.1.x/reference/appendix-schema.html
		

		//These are the SQL DDL statements from the spring-security documentation,
		//but they are in HSQLDB's SQL dialect:
		/*

		//TODO: Check that the groups tables are extra, not instead-of these.
		final String sqlCreateUsers = "create table users(" +
			"	username varchar_ignorecase(50) not null primary key," +
			"	password varchar_ignorecase(50) not null," +
			"	enabled boolean not null);";

		final String sqlCreateAuthorities = "create table authorities (" +
			"	username varchar_ignorecase(50) not null," +
			"	authority varchar_ignorecase(50) not null," +
			"	constraint fk_authorities_users foreign key(username) references users(username));" +
			"	create unique index ix_auth_username on authorities (username,authority);";
			
		final String sqlCreateGroups = "create table groups (" +
				"	id bigint generated by default as identity(start with 0) primary key," +
				"	group_name varchar_ignorecase(50) not null);";

		final String sqlCreateAuthorities = "create table group_authorities (" +
				"	group_id bigint not null," +
				"	authority varchar(50) not null," +
				"	hconstraint fk_group_authorities_group foreign key(group_id) references groups(id));";

		final String sqlCreateMembers = "create table group_members (" +
				"	id bigint generated by default as identity(start with 0) primary key," +
				"	username varchar(50) not null," +
				"	group_id bigint not null," +
				"	constraint fk_group_members_group foreign key(group_id) references groups(id));";
		
		final String sqlCreatePersistentLogins = "create table persistent_logins (" +
				"	username varchar(64) not null," +
				"	series varchar(64) primary key," +
				"	token varchar(64) not null," +
				"	last_used timestamp not null);";
        */
		
		//Versions of the DDL statements for PostgreSQL. TODO: MySQL too.
		//Changes from the original DDL:
		//  - Use bigserial instead of "generated by default ...".
		//  - Use varchar() instead of varchar_ignorecase. TODO: Find an equivalent or suggest doing this in the SELECTs.
		//  - Change constraint lines into just references() after the field definition itself. TODO: What is hconstraint
		final String sqlCreateUsers = "create table users(" +
				"	username varchar(50) not null primary key," +
				"	password varchar(50) not null," +
				"	enabled boolean not null);";

		final String sqlCreateAuthorities = "create table authorities (" +
				"	username varchar(50) not null references users(username)," +
				"	authority varchar(50) not null);" +
				"	create unique index ix_auth_username on authorities (username,authority);";
		
		final String sqlCreateGroups = "create table groups (" +
				"	id bigserial primary key," +
				"	group_name varchar(50) not null);";

		final String sqlCreateGroupAuthorities = "create table group_authorities (" +
				"	group_id bigint not null references groups(id)," +
				"	authority varchar(50) not null );";

		final String sqlCreateGroupMembers = "create table group_members (" +
				"	id bigserial primary key," +
				"	username varchar(50) not null," +
				"	group_id bigint not null references groups(id) );";
		
		final String sqlCreatePersistentLogins = "create table persistent_logins (" +
				"	username varchar(64) not null," +
				"	series varchar(64) primary key," +
				"	token varchar(64) not null," +
				"	last_used timestamp not null);";

		final Connection connection = createConnectionToDatabase();
		if(connection == null) {
			return false;
		}

		try {
			executeQuery(connection, sqlCreateUsers);
			executeQuery(connection, sqlCreateAuthorities);
			executeQuery(connection, sqlCreateGroups);
			executeQuery(connection, sqlCreateGroupAuthorities);
			executeQuery(connection, sqlCreateGroupMembers);
			executeQuery(connection, sqlCreatePersistentLogins);
			closeConnection(connection);
		} catch (final DataAccessException ex) {
			closeConnection(connection);
			
			return false;
		}

		return true;
	}

	/**
	 * @param username
	 * @return
	 */
	public boolean userExists(String username) {
		final Factory factory = createJooqFactory(null);

		SelectSelectStep selectStep = factory.select();
		selectStep = selectStep.select(SqlUtils.createField(TABLENAME_USERS, "username")); //TODO: Cache this. //TODO: Use prepared statements.
		final SelectJoinStep selectJoinStep = selectStep.from(TABLENAME_USERS);
		
		final Condition condition = buildUserWhereExpression(username);
		final SelectConditionStep conditionStep = selectJoinStep.where(condition);
		final SelectFinalStep finalStep = conditionStep;
		
		//TODO: Make sure this is cached:
		final Connection connection = createConnectionToDatabase();
		if(connection == null) {
			//TODO: Throw exception?
			return false;
		}

		final String query = finalStep.getSQL(true); //TODO: Find out why, with SelectFinalStep we need .getQuery().getSQL(true);

		ResultSet resultSet = null;
		
		try {
			resultSet = executeQuery(connection, query, 1);
			closeConnection(connection); //TODO: This won't be necessary if we cache. Why aren't we using C3P0?
		} catch (final DataAccessException ex) {
			closeConnection(connection);
			
			return false;
		} catch (SQLException e) {
			closeConnection(connection);
			
			return false;
		}
		
		
		String usernameResult = null;
		try {

			if(!resultSet.next()) {
				return false;
			}

			usernameResult = resultSet.getString("username");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(StringUtils.isEmpty(usernameResult)) {
			return false;
		}
		
		return usernameResult.equals(username);
	}
	
	private Condition buildUserWhereExpression(final String username) {

		Condition result = null;

		if (StringUtils.isEmpty(username)) {
			return result;
		}

		final org.jooq.Field<Object> field = SqlUtils.createField(TABLENAME_USERS, "username"); //Cache this.
		result = field.equal(username);
		return result;
	}
}
