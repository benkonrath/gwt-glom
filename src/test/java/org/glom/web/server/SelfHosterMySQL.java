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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.server.libglom.Document.HostingMode;
import org.glom.web.shared.libglom.Field;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import com.google.common.io.Files;

/**
 * @author Murray Cumming <murrayc@murrayc.com>
 * 
 */
public class SelfHosterMySQL extends SelfHoster {
	SelfHosterMySQL(final Document document) {
		super(document);

		selfHostedServer.hostingMode = HostingMode.HOSTING_MODE_MYSQL_SELF;
	}
	
	public SelfHosterMySQL(final String databaseName) {
		super(databaseName);

		selfHostedServer.hostingMode = HostingMode.HOSTING_MODE_MYSQL_SELF;
	}

	private static final int PORT_MYSQL_SELF_HOSTED_START = 3306;
	private static final int PORT_MYSQL_SELF_HOSTED_END = 3350;
	
	private static final String FILENAME_DATA = "data";
	
	private static final String DEFAULT_DATABASE_NAME = "INFORMATION_SCHEMA";
	  
	//These are remembered in order to use them to issue the shutdown command via mysqladmin:
	private String savedUsername;
	private String savedPassword;
	
	boolean temporaryPasswordActive; //Whether the password is an initial temporary one.
	private String initialPasswordToSet;
	private String initialUsernameToSet;
	private String temporaryPassword;

	/**
	 * @param document
	 * @param
	 * @param subDirectoryPath
	 * @return
	 * @Override
	 */
	public boolean createAndSelfHostNewEmpty() { 
		final File tempDir = saveDocumentCopy(Document.HostingMode.HOSTING_MODE_MYSQL_SELF);

		// We must specify a default username and password:
		final String user = "glom_dev_user"; //Shorter than what we use for MYSQL, to satisfy MYSQL.
		final String password = "glom_default_developer_password";

		// Create the self-hosting files:
		if (!initialize(user, password)) {
			System.out.println("createAndSelfHostNewEmpty(): initialize failed.");
			// TODO: Delete directory.
		}

		// Check that it really created some files:
		if (!tempDir.exists()) {
			System.out.println("createAndSelfHostNewEmpty(): tempDir does not exist.");
			// TODO: Delete directory.
		}

		return selfHost(user, password);
	}

	/**
	 * @param document
	 * @param user
	 * @param password
	 * @return
	 * @Override
	 */
	private boolean selfHost(final String user, final String password) {
		if (getSelfHostingActive()) {
			// TODO: std::cerr << G_STRFUNC << ": Already started." << std::endl;
			return false; // STARTUPERROR_NONE; //Just do it once.
		}

		final String dbDirData = getSelfHostingDataPath(false);
		if (StringUtils.isEmpty(dbDirData) || !SelfHoster.fileExists(dbDirData)) {
			/*
			 * final String dbDirBackup = dbDir + File.separator + FILENAME_BACKUP;
			 * 
			 * if(fileExists(dbDirBackup)) { //TODO: std::cerr << G_STRFUNC <<
			 * ": There is no data, but there is backup data." << std::endl; //Let the caller convert the backup to real
			 * data and then try again: return false; // STARTUPERROR_FAILED_NO_DATA_HAS_BACKUP_DATA; } else {
			 */
			// TODO: std::cerr << "ConnectionPool::create_self_hosting(): The data sub-directory could not be found." <<
			// dbdir_data_uri << std::endl;
			return false; // STARTUPERROR_FAILED_NO_DATA;
			// }
		}

		final int availablePort = SelfHoster.discoverFirstFreePort(PORT_MYSQL_SELF_HOSTED_START,
				PORT_MYSQL_SELF_HOSTED_END);

		if (availablePort == 0) {
			// TODO: Use a return enum or exception so we can tell the user about this:
			// TODO: std::cerr << G_STRFUNC << ": No port was available between " << PORT_MYSQL_SELF_HOSTED_START
			// << " and " << PORT_MYSQL_SELF_HOSTED_END << std::endl;
			return false; // STARTUPERROR_FAILED_UNKNOWN_REASON;
		}

		final String portAsText = portNumberAsText(availablePort);
		final String dbDirPid = getSelfHostingPath("pid", false);
		final String dbDirSocket = getSelfHostingPath("mysqld.sock", false);

		final String commandPathStart = getPathToMysqlExecutable("mysqld_safe");
		if (StringUtils.isEmpty(commandPathStart)) {
			System.out.println("selfHost(): getPathToMysqlExecutable(mysqld_safe) failed.");
			return false;
		}

		final ProcessBuilder commandMysqlStart = new ProcessBuilder(commandPathStart,
				"--no-defaults",
				"--port=" + portAsText,
				"--datadir=" + shellQuote(dbDirData),
				"--socket=" + shellQuote(dbDirSocket),
				"--pid-file=" + shellQuote(dbDirPid));


		selfHostedServer.port = availablePort; //Needed by getMysqlAdminCommand().
		 
		final List<String> progAndArgsCheck = getMysqlAdminCommand(savedUsername, savedPassword);
		progAndArgsCheck.add("ping");
		final ProcessBuilder commandCheckMysqlHasStarted = new ProcessBuilder(progAndArgsCheck);

		final String secondCommandSuccessText = "mysqld is alive"; // TODO: This is not a stable API. Also, watch out for
																// localisation.

		// The first command does not return, but the second command can check whether it succeeded:
		// TODO: Progress
		final boolean result = executeCommandLineAndWaitUntilSecondCommandReturnsSuccess(commandMysqlStart,
				commandCheckMysqlHasStarted, secondCommandSuccessText);
		if (!result) {
			System.out.println("selfHost(): Error while attempting to self-host a database.");
			return false; // STARTUPERROR_FAILED_UNKNOWN_REASON;
		}

		// Remember the port for later:
		selfHostedServer.port = availablePort; //Previously document.setConnectionPort(availablePort);

		// Check that we can really connect:
		
		//Sleep for a fairly long time initially to avoid distracting error messages when trying to connect,
		//while the database server is still starting up.
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// mysqladmin could report success before it is really ready to let us connect,
		// so in this case we can just keep trying until it works, for a while:
		for (int i = 0; i < 10; i++) {

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String dbName = null;
			if(document != null) {
				dbName = document.getConnectionDatabase();
				document.setConnectionDatabase(""); // We have not created the database yet.
			}

			//Check that we can connect:
			final Connection connection = createConnection(false);

			selfHostedDatabaseName = dbName;
			if(document != null) {
				document.setConnectionDatabase(dbName);
			}

			if (connection != null) {
				//Close the connection:
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println("selfHost(): Connection succeeded after retries=" + i);
				
				if (temporaryPasswordActive) {
					return setInitialUsernameAndPassword();
				}

				return true; // STARTUPERROR_NONE;
			}

			/*
			System.out
					.println("selfHost(): Waiting and retrying the connection due to suspected too-early success of pg_ctl. retries="
							+ i);
			*/
		}

		System.out.println("selfHost(): Test connection failed after multiple retries.");
		return false;
	}

	/**
	 * @return 
	 * 
	 */
	private boolean setInitialUsernameAndPassword() {
		//If necessary, set the initial root password and rename the root user:
		if(!temporaryPasswordActive)
			return true;
		
		if (StringUtils.isEmpty(initialUsernameToSet)) {
			System.out.println("setInitialUsernameAndPassword(): initialUsernameToSet is empty.");
			return false;
		}

		if (StringUtils.isEmpty(initialPasswordToSet)) {
			System.out.println("setInitialUsernameAndPassword(): initialPasswordToSet is empty.");
			return false;
		}
		
		//Set the root password:
		final List<String> progAndArgs = getMysqlAdminCommand("root", temporaryPassword);
		progAndArgs.add("password");
		progAndArgs.add(shellQuote(initialPasswordToSet));

		final ProcessBuilder commandInitdbSetInitialPassword = new ProcessBuilder(progAndArgs);
		final boolean result = executeCommandLineAndWait(commandInitdbSetInitialPassword);
		
		if(!result) {
			System.out.println("setInitialUsernameAndPassword(): commandInitdbSetInitialPassword failed.");
			return false;
		}

		temporaryPasswordActive = false;
		temporaryPassword = null;

		//Rename the root user,
		//so we can connnect as the expected username:
		//We connect to the INFORMATION_SCHEMA database, because libgda needs us to specify some database.
		final Connection connection = createConnection(DEFAULT_DATABASE_NAME, "root", initialPasswordToSet, false);
		if (connection == null) {
			//std::cerr << G_STRFUNC << "Error while attempting to start self-hosting MYSQL database, when setting the initial username: connection failed." << std::endl;
			return false;
		}

		savedPassword = initialPasswordToSet;

		final String query = buildQueryChangeUsername(connection, "root", initialUsernameToSet);

		try {
			SqlUtils.executeUpdate(connection, query);
		} catch (final SQLException e) {
			System.out.println("setInitialUsernameAndPassword(): change username query failed: " + query.toString());
			e.printStackTrace();
			return false;
		} finally {
			// cleanup everything that has been used
			try {
				// TODO: If we use the ResultSet.
			} catch (final Exception e) {
				return false;
			}
		}

		savedUsername = initialUsernameToSet;
		initialUsernameToSet = null;

		return true;
	}

	/**
	 * @param connection
	 * @param string
	 * @param initialUsernameToSet2
	 * @return
	 */
	private String buildQueryChangeUsername(Connection connection, String oldUsername, String newUsername) {
		if (StringUtils.isEmpty(oldUsername)) {
			//std::cerr << G_STRFUNC << ": old_username is empty." << std::endl;
			return "";
		}

		if (StringUtils.isEmpty(newUsername)) {
			//std::cerr << G_STRFUNC << ": new_username is empty." << std::endl;
			return "";
		}

		//TODO: Try to avoid specifing @localhost.
		//We do this to avoid this error:
		//mysql> RENAME USER root TO glom_dev_user;
		//ERROR 1396 (HY000): Operation RENAME USER failed for 'root'@'%'
		//mysql> RENAME USER root@localhost TO glom_dev_user;
		//Query OK, 0 rows affected (0.00 sec)
		final String user = quoteAndEscapeSqlId(oldUsername) + "@localhost";

		//Login will fail after restart if we don't specify @localhost here too:
		final String newUser = quoteAndEscapeSqlId(newUsername) + "@localhost";

		return "RENAME USER " + user + " TO " + newUser;
	}

	private String getSelfHostingPath(final String subpath, final boolean create) {
		final String dbDir = selfHostedDirectoryPath; //The same as document.getSelfHostedDirectoryPath(), when document != nul.
		if (StringUtils.isEmpty(dbDir)) {
			System.out.println("getSelfHostingPath(): selfHostedDirectoryPath has no path.");
			return null;
		}

		if (StringUtils.isEmpty(subpath)) {
			return dbDir;
		}

		final String dbDirData = dbDir + File.separator + subpath;
		final File file = new File(dbDirData);

		// Return the path regardless of whether it exists:
		if (!create) {
			return dbDirData;
		}

		if (!file.exists()) {
			try {
				Files.createParentDirs(file);
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}

			if (!file.mkdir()) {
				return "";
			}
		}

		return dbDirData;
	}

	private String getSelfHostingDataPath(final boolean create) {
		return getSelfHostingPath(FILENAME_DATA, create);
	}

	/**
	 * @param string
	 * @return
	 */
	private static String getPathToMysqlExecutable(final String string) {
		final List<String> dirPaths = new ArrayList<String>();
		dirPaths.add("/usr/bin");

		for (String dir : dirPaths) {
			final String path = dir + File.separator + string;
			if (fileExistsAndIsExecutable(path)) {
				return path;
			}
		}

		return "";
	}
	
	private List<String> getMysqlAdminCommand(final String username, final String password) {
		if (StringUtils.isEmpty(username)) {
			return null;
		}
		
		final String portAsText = portNumberAsText(selfHostedServer.port);

		final List<String> progAndArgs = new ArrayList<String>();
		progAndArgs.add(getPathToMysqlExecutable("mysqladmin"));
		progAndArgs.add("--no-defaults");
		progAndArgs.add("--port=" + portAsText);
		progAndArgs.add("--protocol=tcp"); //Otherwise we cannot connect as root. TODO: However, maybe we could use --skip-networking if network sharing is not enabled.
		progAndArgs.add("--user=" + shellQuote(username));

		//--password='' is not always interpreted the same as specifying no --password.
		if(!StringUtils.isEmpty(password)) {
			progAndArgs.add("--password=" + shellQuote(password));
		}

		return progAndArgs;
	}

	/**
	 * @param cpds
	 * @return
	 */
	private boolean initialize(final String initialUsername, final String initialPassword) {
		if (StringUtils.isEmpty(initialUsername)) {
			System.out.println("initialize(): initialUsername is empty.");
			return false;
		}

		if (StringUtils.isEmpty(initialPassword)) {
			System.out.println("initialize(): initialPassword is empty.");
			return false;
		}
		
		// mysql_install_db creates a new mysql database cluster:

		// Get file:// URI for the tmp/ directory:

		// Make sure to use double quotes for the executable path, because the
		// CreateProcess() API used on Windows does not support single quotes.
		final String dbDirData = getSelfHostingDataPath(false /* create */);
		if(dbDirData.isEmpty()) {
			System.out.println("initialize(): getSelfHostingDataPath returned no path.");
			return false;
		}

		// Note that initdb returns this error if we split the arguments more,
		// for instance splitting -D and dbDirData into separate strings:
		// too many command-line arguments (first is "(null)")
		// TODO: If we quote tempPwFile then initdb says that it cannot find it.
		// Note: If we use "-D " instead of "-D" then the initdb seems to make the space part of the filepath,
		// though that does not happen with the normal command line.
		boolean result = false;
		final String commandPath = getPathToMysqlExecutable("mysql_install_db");
		if (StringUtils.isEmpty(commandPath)) {
			System.out.println("initialize(): getPathToMysqlExecutable(mysql_install_db) failed.");
		} else {
			final ProcessBuilder commandInitdb = new ProcessBuilder(commandPath, "--no-defaults", "--datadir=" + shellQuote(dbDirData));

			// Note that --pwfile takes the password from the first line of a file. It's an alternative to supplying it
			// when
			// prompted on stdin.
			result = executeCommandLineAndWait(commandInitdb);
		}

		if (!result) {
			System.out.println("initialize(): Error while attempting to create self-hosting database.");
			return false;
		}

		//This is used during the first start:
		initialPasswordToSet = initialPassword;
	    initialUsernameToSet = initialUsername;

	    //TODO: With MYSQL 5.6, use the new --random-passwords option (see above)
	    temporaryPassword = "";
	    temporaryPasswordActive = true;
	    savedUsername = "root";
	    savedPassword = "";
	    username = "root";

		return result; // ? INITERROR_NONE : INITERROR_COULD_NOT_START_SERVER;
	}

	//TODO: Move this to the base class:
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
	 *
	 */
	private void progress() {
		// TODO Auto-generated method stub

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

		// Create SQL to describe all fields in this table:
		String sqlFields = "";
		for (final Field field : fields) {
			// Create SQL to describe this field:
			String sqlFieldDescription = quoteAndEscapeSqlId(field.getName()) + " " + field.getSqlType(Field.SqlDialect.MYSQL);

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
		final DSLContext dslContext = DSL.using(connection, getSqlDialect());
		try {
			dslContext.execute(query);
		} catch (DataAccessException e) {
			System.out.println("createDatabase(): query failed: " + query);
			e.printStackTrace();
			return false;
		}

		tableCreationSucceeded = true;
		if (!tableCreationSucceeded) {
			System.out.println("recreatedDatabase(): CREATE TABLE() failed.");
		}

		return tableCreationSucceeded;
	}

	/**
	 * @param name
	 * @return
	 */
	private static String quoteAndEscapeSqlId(final String name) {
		return SqlUtils.quoteAndEscapeSqlId(name, SQLDialect.MYSQL);
	}

	/**
	 * @return
	 */
	private static boolean createDatabase(final Connection connection, final String databaseName) {
		final String query = "CREATE DATABASE " + quoteAndEscapeSqlId(databaseName);
		final DSLContext dslContext = DSL.using(connection, SQLDialect.MYSQL);

		try {
			dslContext.execute(query);
		} catch (DataAccessException e) {
			System.out.println("createDatabase(): query failed: " + query);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 *
	 */
	public boolean cleanup() {
		boolean result = true;

		// Stop the server:
		if ((document != null) && (document.getConnectionPort() != 0)) {
			final List<String> progAndArgs = getMysqlAdminCommand(savedUsername, savedPassword);
			progAndArgs.add("shutdown");

			final ProcessBuilder commandMySQLStop = new ProcessBuilder(progAndArgs);
			result = executeCommandLineAndWait(commandMySQLStop);
			if (!result) {
				System.out.println("cleanup(): Failed to stop the MYSQL server.");
			}

			document.setConnectionPort(0);
		}

		// Delete the files:
		final String selfhostingPath = getSelfHostingPath("", false);
		final File fileSelfHosting = new File(selfhostingPath);
		fileSelfHosting.delete();

		final String docPath = document.getFileURI();
		final File fileDoc = new File(docPath);
		fileDoc.delete();

		return result;
	}
	
	@Override
	public SQLDialect getSqlDialect() {
		return SQLDialect.MYSQL;
	}
}
