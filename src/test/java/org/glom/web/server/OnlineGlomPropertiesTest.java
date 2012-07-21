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


import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class OnlineGlomPropertiesTest extends TestCase {

	OnlineGlomProperties config = null;

	@BeforeClass
	public void setUp() throws IOException {
		config = new OnlineGlomProperties();

		final InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("onlineglom.properties.sample");
		assertNotNull(is);

		config.load(is); // can throw an IOException
	}
	
	/**
	 * Test method for {@link org.glom.web.server.OnlineGlomProperties#getCredentials(java.lang.String)}.
	 */
	@Test
	public void testGetCredentials() {
		OnlineGlomProperties.Credentials credentials = config.getCredentials("lesson-planner.glom");
		assertEquals("ben", credentials.userName);
		assertEquals("ChangeMeToo", credentials.password);
	}

	/**
	 * Test method for {@link org.glom.web.server.OnlineGlomProperties#getGlobalUsername()}.
	 */
	@Test
	public void testGetGlobalUsername() {
		assertEquals("someuser", config.getGlobalUsername());

	}

	/**
	 * Test method for {@link org.glom.web.server.OnlineGlomProperties#getGlobalPassword()}.
	 */
	@Test
	public void testGetGlobalPassword() {
		assertEquals("ChangeMe", config.getGlobalPassword());
	}

	/**
	 * Test method for {@link org.glom.web.server.OnlineGlomProperties#getGlobalLocale()}.
	 */
	@Test
	public void testGetGlobalLocale() {
		assertEquals("", config.getGlobalLocale());
	}

	/**
	 * Test method for {@link org.glom.web.server.OnlineGlomProperties#getDocumentsDirectory()}.
	 */
	@Test
	public void testGetDocumentsDirectory() {
		assertEquals("/home/someuser/glomfiles", config.getDocumentsDirectory());
	}
	
	@AfterClass
	public void tearDown() {
		config = null;
	}

}
