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

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.glom.web.server.libglom.Document;
import org.glom.web.server.libglom.Document.HostingMode;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.jooq.Condition;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 * 
 */
public class SelfHostExampleTest {

	private SelfHoster selfHoster = null;

	@Test
	public void test() throws SQLException {
		final URL url = SelfHostExampleTest.class.getResource("example_music_collection.glom");
		assertTrue(url != null);
		final String strUri = url.toString();

		final Document document = new Document();
		document.setFileURI(strUri);
		assertTrue(document.load());

		selfHoster = new SelfHoster(document);
		final boolean hosted = selfHoster.createAndSelfHostFromExample(HostingMode.HOSTING_MODE_POSTGRES_SELF);
		assertTrue(hosted);
		
		testExampleMusiccollectionData(document);
	}
	
	private void testExampleMusiccollectionData(final Document document) throws SQLException
	{
	  assertTrue(document != null);
	  
	  //Check that some data is as expected:
	  final TypedDataItem quickFindValue = new TypedDataItem();
	  quickFindValue.setText("Born To Run");
	  final Condition whereClause = SqlUtils.getFindWhereClauseQuick(document, "albums", quickFindValue);
	  assertTrue(whereClause != null);

	  final List<LayoutItemField> fieldsToGet = new ArrayList<LayoutItemField>();
	  Field field = document.getField("albums", "album_id");
	  LayoutItemField layoutItemField = new LayoutItemField();
	  layoutItemField.setFullFieldDetails(field);
	  fieldsToGet.add(layoutItemField);
	  field = document.getField("albums", "name");
	  layoutItemField = new LayoutItemField();
	  layoutItemField.setFullFieldDetails(field);
	  fieldsToGet.add(layoutItemField);
	  
	  final String sqlQuery = SqlUtils.buildSqlSelectWithWhereClause("albums", fieldsToGet, whereClause, null);
	  
	  final Connection conn = selfHoster.createConnection(false);
	  assertTrue(conn != null);
	  
	  final Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      //st.setFetchSize(length);
	  final ResultSet rs = st.executeQuery(sqlQuery);
	  assertTrue(rs != null);
	  
	  final ResultSetMetaData rsMetaData = rs.getMetaData();
	  Assert.assertEquals(2, rsMetaData.getColumnCount());
	  
	  rs.last();
	  final int rsRowsCount = rs.getRow();
	  Assert.assertEquals(1, rsRowsCount);
	}

	public void tearDown() {
		if (selfHoster != null) {
			selfHoster.cleanup();
		}
	}
}
