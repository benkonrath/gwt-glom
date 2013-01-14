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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class SelfHoster {

	protected boolean selfHostingActive = false;
	protected Document document = null;
	protected String username = "";
	protected String password = "";

	/**
	 * 
	 */
	public SelfHoster(final Document document) {
		super();
		this.document = document;
	}
	
	public boolean createAndSelfHostFromExample(final Document.HostingMode hostingMode) {
		//Derived classes should implement this.
		return false;
	}
	
	/**
	 */
	public Connection createConnection(boolean failureExpected) {
		//Derived classes should implement this.
		return null;
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
	protected boolean getSelfHostingActive() {
		return selfHostingActive;
	}
	
	protected boolean executeCommandLineAndWait(final ProcessBuilder command) {

		command.redirectErrorStream(true);

		// Run the first command, and wait for it to return:
		Process process = null;
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
			// System.out.print("  Output: " + output);
			return false;
		}

		return true;
	}

	protected boolean executeCommandLineAndWaitUntilSecondCommandReturnsSuccess(final ProcessBuilder command,
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
		boolean result = false;
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
	protected boolean insertExampleData(final Connection connection, final Document document, final String tableName) {

		final Factory factory = new Factory(connection, SQLDialect.POSTGRES);
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
				// e.printStackTrace();
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
	protected boolean addGroupsFromDocument(final Document document) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * @param document
	 * @return
	 */
	protected boolean setTablePrivilegesGroupsFromDocument(final Document document) {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * @param dbDir
	 * @return
	 */
	protected static boolean fileExists(final String filePath) {
		final File file = new File(filePath);
		return file.exists();
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 */
	protected static int discoverFirstFreePort(final int start, final int end) {
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
	protected static boolean fileExistsAndIsExecutable(String path) {
		final File file = new File(path);
		if (!file.exists()) {
			return false;
		}

		if (!file.canExecute()) {
			return false;
		}

		return true;
	}

}