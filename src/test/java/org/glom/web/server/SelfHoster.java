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

package org.glom.web.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.libglom.Field;
import org.jooq.InsertResultStep;
import org.jooq.InsertSetStep;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.Factory;

import com.google.common.io.Files;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
class SelfHoster {

	private boolean selfHostingActive = false;
	Document document = null;
	String username = "";
	String password = "";

	/**
	 *
	 */
	SelfHoster(final Document document) {
		super();
		this.document = document;
	}

	static boolean createDirIfNecessaryWithParents(final File file) {
		if (!file.exists()) {
			try {
				Files.createParentDirs(file);
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			}

			if (!file.mkdir()) {
				return false;
			}
		}

		return true;
	}

	public boolean createAndSelfHostFromExample() {

		if (!createAndSelfHostNewEmpty()) {
			// std::cerr << G_STRFUNC << ": test_create_and_selfhost_new_empty() failed." << std::endl;
			return false;
		}

		final boolean recreated = recreateDatabaseFromDocument(); /* TODO: Progress callback */
		if (!recreated) {
			if (!cleanup()) {
				return false;
			}
		}

		return recreated;
	}

	/**
	 * @return
	 */
	private boolean createDatabase(final Connection connection, final String databaseName) {
		final String query = "CREATE DATABASE  " + quoteAndEscapeSqlId(databaseName);
		final Factory factory = new Factory(connection, getSqlDialect());

		if (!executeCreateDatabase(query, factory)) {
			return false;
		}

		return true;
	}

	private static boolean executeCreateDatabase(String query, Factory factory) {
		try {
			factory.execute(query);
		} catch (DataAccessException e) {
			System.out.println("createDatabase(): query failed: " + query);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * @param document
	 * @return
	 */
	protected boolean recreateDatabaseFromDocument() {
		// Check whether the database exists already.
		final String dbName = document.getConnectionDatabase();
		if (StringUtils.isEmpty(dbName)) {
			return false;
		}

		document.setConnectionDatabase(dbName);
		Connection connection = createConnection(true);
		if (connection != null) {
			// Connection to the database succeeded, so the database
			// exists already.
			try {
				connection.close();
			} catch (final SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		// Create the database:
		progress();
		document.setConnectionDatabase("");

		connection = createConnection(false);
		if (connection == null) {
			System.out.println("recreatedDatabase(): createConnection() failed, before creating the database.");
			return false;
		}

		final boolean dbCreated = createDatabase(connection, dbName);

		if (!dbCreated) {
			return false;
		}

		progress();

		// Check that we can connect:
		try {
			connection.close();
		} catch (final SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connection = null;

		document.setConnectionDatabase(dbName);
		connection = createConnection(false);
		if (connection == null) {
			System.out.println("recreatedDatabase(): createConnection() failed, after creating the database.");
			return false;
		}

		progress();

		// Create each table:
		final List<String> tables = document.getTableNames();
		for (final String tableName : tables) {

			// Create SQL to describe all fields in this table:
			final List<Field> fields = document.getTableFields(tableName);

			progress();
			final boolean tableCreationSucceeded = createTable(connection, document, tableName, fields);
			progress();
			if (!tableCreationSucceeded) {
				// TODO: std::cerr << G_STRFUNC << ": CREATE TABLE failed with the newly-created database." <<
				// std::endl;
				return false;
			}
		}

		// Note that create_database() has already called add_standard_tables() and add_standard_groups(document).

		// Add groups from the document:
		progress();
		if (!addGroupsFromDocument(document)) {
			// TODO: std::cerr << G_STRFUNC << ": add_groups_from_document() failed." << std::endl;
			return false;
		}

		// Set table privileges, using the groups we just added:
		progress();
		if (!setTablePrivilegesGroupsFromDocument(document)) {
			// TODO: std::cerr << G_STRFUNC << ": set_table_privileges_groups_from_document() failed." << std::endl;
			return false;
		}

		for (final String tableName : tables) {
			// Add any example data to the table:
			progress();

			// try
			// {
			progress();
			final boolean tableInsertSucceeded = insertExampleData(connection, document, tableName);

			if (!tableInsertSucceeded) {
				// TODO: std::cerr << G_STRFUNC << ": INSERT of example data failed with the newly-created database." <<
				// std::endl;
				return false;
			}
			// }
			// catch(final std::exception& ex)
			// {
			// std::cerr << G_STRFUNC << ": exception: " << ex.what() << std::endl;
			// HandleError(ex);
			// }

		} // for(tables)

		return true; // All tables created successfully.
	}

	/**
	 * @param document
	 * @param tableName
	 * @param fields
	 * @return
	 */
	private boolean createTable(final Connection connection, final Document document, final String tableName,
								final List<Field> fields) {
		boolean tableCreationSucceeded = false;

		/*
		 * TODO: //Create the standard field too: //(We don't actually use this yet) if(std::find_if(fields.begin(),
		 * fields.end(), predicate_FieldHasName<Field>(GLOM_STANDARD_FIELD_LOCK)) == fields.end()) { sharedptr<Field>
		 * field = sharedptr<Field>::create(); field->set_name(GLOM_STANDARD_FIELD_LOCK);
		 * field->set_glom_type(Field::TYPE_TEXT); fields.push_back(field); }
		 */

		final Field.SqlDialect fieldSqlDialect = getSqlDialect() ==
				SQLDialect.POSTGRES ? Field.SqlDialect.POSTGRESQL : Field.SqlDialect.MYSQL;

		// Create SQL to describe all fields in this table:
		String sqlFields = "";
		for (final Field field : fields) {
			// Create SQL to describe this field:
			String sqlFieldDescription = quoteAndEscapeSqlId(field.getName()) + " " + field.getSqlType(fieldSqlDialect);

			if (field.getPrimaryKey()) {
				sqlFieldDescription += " NOT NULL  PRIMARY KEY";
			}

			// Append it:
			if (!StringUtils.isEmpty(sqlFields)) {
				sqlFields += ", ";
			}

			sqlFields += sqlFieldDescription;
		}

		if (StringUtils.isEmpty(sqlFields)) {
			// TODO: std::cerr << G_STRFUNC << ": sql_fields is empty." << std::endl;
		}

		// Actually create the table
		final String query = "CREATE TABLE " + quoteAndEscapeSqlId(tableName) + " (" + sqlFields + ");";
		final Factory factory = new Factory(connection, getSqlDialect());
		factory.execute(query);
		tableCreationSucceeded = true;
		if (!tableCreationSucceeded) {
			System.out.println("recreatedDatabase(): CREATE TABLE() failed.");
		}

		return tableCreationSucceeded;
	}

	/**
	 * @param hostingMode
	 * @return
	 */
	boolean createAndSelfHostNewEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 *
	 */
	private void progress() {
		// TODO Auto-generated method stub

	}

	/**
	 *
	 */
	public boolean cleanup() {
		//Derived classes should implement this.
		return false;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * @return
	 */
	boolean getSelfHostingActive() {
		return selfHostingActive;
	}

	boolean executeCommandLineAndWait(final ProcessBuilder command) {

		command.redirectErrorStream(true);

		// Run the first command, and wait for it to return:
		Process process;
		try {
			process = command.start();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		// final InputStream stderr = process.getInputStream();
		// final InputStreamReader isr = new InputStreamReader(stderr);
		// final BufferedReader br = new BufferedReader(isr);
		// String output = "";
		// String line;
		/*
		 * try { //TODO: readLine() can hang, waiting for an end of line that never comes. while ((line = br.readLine())
		 * != null) { output += line + "\n"; } } catch (final IOException e1) { e1.printStackTrace(); return false; }
		 */

		int result = 0;
		try {
			result = process.waitFor();
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		if (result != 0) {
			System.out.println("executeCommandLineAndWait(): Command failed: " + command.command().toString());
			try (final InputStream is = process.getInputStream()) {
				try (final InputStreamReader isr = new InputStreamReader(is)) {
					final BufferedReader br = new BufferedReader(isr);
					String line;
					try {
						while ((line = br.readLine()) != null) {
							System.out.println(line);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}

			return false;
		}

		return true;
	}

	boolean executeCommandLineAndWaitUntilSecondCommandReturnsSuccess(final ProcessBuilder command,
																	  final ProcessBuilder commandSecond, final String secondCommandSuccessText) {
		command.redirectErrorStream(true);

		// Run the first command, and do not wait for it to return:
		// Process process = null;
		try {
			// Process process =
			command.start();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		// final InputStream stderr = process.getInputStream();
		// final InputStreamReader isr = new InputStreamReader(stderr);
		// final BufferedReader br = new BufferedReader(isr);

		/*
		 * We do not wait, because this (postgres, for instance), does not return: final int result = process.waitFor();
		 * if (result != 0) { // TODO: Warn. return false; }
		 */

		// Now run the second command, usually to verify that the first command has really done its work:
		// We run this repeatedly until it succeeds, to show that the first command has finished.
		boolean result;
		while (true) {
			result = executeCommandLineAndWait(commandSecond);
			if (result) {
				System.out.println("executeCommandLineAndWait(): second command succeeded.");
				return true;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}

				System.out.println("executeCommandLineAndWait(): Trying the second command again.");
			}
		}

		// Try to get the output:
		/*
		 * if (!result) { String output = ""; /* String line; try { // TODO: readLine() can hang, waiting for an end of
		 * line that never comes. while ((line = br.readLine()) != null) { output += line + "\n";
		 * System.out.println(line); } } catch (final IOException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); return false; }
		 */

		// System.out.println("  Output of first command: " + output);
		// System.out.println("  first command: " + command.command().toString());
		// System.out.println("  second command: " + commandSecond.command().toString());
		// }
	}

	/**
	 * @param document
	 * @param tableName
	 * @return
	 */
	boolean insertExampleData(final Connection connection, final Document document, final String tableName) {

		final Factory factory = new Factory(connection, getSqlDialect());
		final Table<Record> table = Factory.tableByName(tableName);

		final List<Map<String, DataItem>> exampleRows = document.getExampleRows(tableName);
		for (final Map<String, DataItem> row : exampleRows) {
			InsertSetStep<Record> insertStep = factory.insertInto(table);

			for (final Entry<String, DataItem> entry : row.entrySet()) {
				final String fieldName = entry.getKey();
				final DataItem value = entry.getValue();
				if (value == null) {
					continue;
				}

				final Field field = document.getField(tableName, fieldName);
				if (field == null) {
					continue;
				}

				final org.jooq.Field<Object> jooqField = Factory.fieldByName(field.getName());
				if (jooqField == null) {
					continue;
				}

				final Object fieldValue = value.getValue(field.getGlomType());
				insertStep = insertStep.set(jooqField, fieldValue);
			}

			if (!(insertStep instanceof InsertResultStep<?>)) {
				continue;
			}

			// We suppress the warning because we _do_ check the cast above.
			@SuppressWarnings("unchecked")
			final InsertResultStep<Record> insertResultStep = (InsertResultStep<Record>) insertStep;

			try {
				insertResultStep.fetchOne();
			} catch (final DataAccessException e) {
				System.out.println("createAndSelfHostNewEmpty(): insertResultStep failed.");
				e.printStackTrace();
				return false;
			}
			// TODO: Check that it worked.
		}

		return true;
	}


	/**
	 * @param document
	 * @return
	 */
	boolean addGroupsFromDocument(final Document document) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * @param document
	 * @return
	 */
	boolean setTablePrivilegesGroupsFromDocument(final Document document) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * @param dbDir
	 * @return
	 */
	static boolean fileExists(final String filePath) {
		final File file = new File(filePath);
		return file.exists();
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	static int discoverFirstFreePort(final int start, final int end) {
		for (int port = start; port <= end; ++port) {
			try {
				final ServerSocket socket = new ServerSocket(port);

				// If the instantiation succeeded then the port was free:
				final int result = socket.getLocalPort(); // This must equal port.
				socket.close();
				return result;
			} catch (final IOException ex) {
				continue; // try next port
			}
		}

		return 0;
	}

	/**
	 * @param path
	 * @return
	 */
	static boolean fileExistsAndIsExecutable(String path) {
		final File file = new File(path);
		if (!file.exists()) {
			return false;
		}

		if (!file.canExecute()) {
			return false;
		}

		return true;
	}

	/**
	 * @param portNumber
	 * @return
	 */
	String portNumberAsText(final int portNumber) {
		final NumberFormat format = NumberFormat.getInstance(Locale.US);
		format.setGroupingUsed(false); // TODO: Does this change it system-wide?
		return format.format(portNumber);
	}

	/**
	 */
	public Connection createConnection(boolean failureExpected) {
		//We don't just use SqlUtils.tryUsernameAndPassword() because it uses ComboPooledDataSource,
		//which does not automatically close its connections,
		//leading to errors because connections are already open.
		final SqlUtils.JdbcConnectionDetails details = SqlUtils.getJdbcConnectionDetails(document);
		if (details == null) {
			return null;
		}

		final Properties connectionProps = new Properties();
		connectionProps.put("user", this.username);
		connectionProps.put("password", this.password);

		Connection conn;
		try {
			//TODO: Remove these debug prints when we figure out why getConnection sometimes hangs.
			//System.out.println("debug: SelfHosterPostgreSQL.createConnection(): before createConnection()");
			DriverManager.setLoginTimeout(10);
			conn = DriverManager.getConnection(details.jdbcURL, connectionProps);
			//System.out.println("debug: createConnection(): before createConnection()");
		} catch (final SQLException e) {
			if(!failureExpected) {
				e.printStackTrace();
			}
			return null;
		}

		return conn;
	}

	/**
	 * @param dbDirData
	 * @return
	 */
	String shellQuote(final String str) {
		// TODO: If we add the quotes then they seem to be used as part of the path, though that is not a problem with
		// the normal command line.
		return str;

		// TODO: Escape.
		// return "'" + str + "'";
	}

	/**
	 * @return The temporary directory where the file was saved.
	 */
	File saveDocumentCopy(Document.HostingMode hostingMode) {
		// Save a copy, specifying the path to file in a directory:
		// For instance, /tmp/testglom/testglom.glom");
		final String tempFilename = "testglom";
		final File tempFolder = Files.createTempDir();
		final File tempDir = new File(tempFolder, tempFilename);

		final String tempDirPath = tempDir.getPath();
		final String tempFilePath = tempDirPath + File.separator + tempFilename;
		final File file = new File(tempFilePath);

		// Make sure that the file does not exist yet:
		//noinspection ResultOfMethodCallIgnored
		tempDir.delete();

		// Save the example as a real file:
		document.setFileURI(file.getPath());

		document.setHostingMode(hostingMode);
		document.setIsExampleFile(false);
		final boolean saved = document.save();
		if (!saved) {
			System.out.println("createAndSelfHostNewEmpty(): Document.save() failed.");
			return null; // TODO: Delete the directory.
		}
		return tempDir;
	}

	/**
	 * @return
	 */
	public SQLDialect getSqlDialect() {
		//This must be overriden by the derived classes.
		return null;
	}

	/**
	 * @param name
	 * @return
	 */
	String quoteAndEscapeSqlId(final String name) {
		return quoteAndEscapeSqlId(name, getSqlDialect());
	}

	/**
	 * @param name
	 * @return
	 */
	public static String quoteAndEscapeSqlId(final String name, final SQLDialect sqlDialect) {
		//final Factory factory = new Factory(connection, getSqlDialect());
		final org.jooq.Name jooqName = Factory.name(name);
		if(jooqName == null) {
			return null;
		}

		final Factory factory = new Factory(sqlDialect);
		return factory.render(jooqName);
	}

}