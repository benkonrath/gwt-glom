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

import static org.junit.Assert.*;

import org.glom.web.server.SelfHosterPostgreSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class UserDatabaseTest {
	
	UserDatabase userDatabase = null;
	SelfHosterPostgreSQL selfHoster = null; //TODO: Test MySQL too.

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		selfHoster = new SelfHosterPostgreSQL(UserDatabase.DATABASE_NAME);
		assertTrue(selfHoster.createAndSelfHostNewEmpty());

		userDatabase = new UserDatabase(selfHoster.getServerDetails(), selfHoster.getUsername(), selfHoster.getPassword());
		assertFalse(userDatabase.databaseExists());
		assertTrue(userDatabase.createDatabase());
		selfHoster.setDatabaseCreated();
		assertTrue(userDatabase.databaseExists());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		if(userDatabase != null) {
			userDatabase.removeDatabase();
		}

		if(selfHoster != null) {
			selfHoster.cleanup();
		}
	}

	/**
	 * Test method for {@link org.glom.web.server.users.UserDatabase#createUser(java.lang.String)}.
	 */
	@Test
	public void testCreateUser() {
		final String username = "someTestUser1";
		userDatabase.createUser(username, "somepassword");
		assertTrue(userDatabase.userExists(username));
	}

	/**
	 * Test method for {@link org.glom.web.server.users.UserDatabase#removeUser(java.lang.String)}.
	 */
	@Test
	public void testRemoveUser() {
		final String username = "someTestUser2";
		userDatabase.createUser(username, "somepassword");
		assertTrue(userDatabase.userExists(username));
		
		userDatabase.removeUser(username);
		assertFalse(userDatabase.userExists(username));
	}

}
