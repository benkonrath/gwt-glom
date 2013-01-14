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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.glom.web.server.libglom.Document;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.Relationship;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.jooq.Condition;
import org.junit.Assert;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class SelfHostTestUtils {
	static public void testExampleMusiccollectionData(final SelfHosterPostgreSQL selfHosterPostgreSQL, final Document document) throws SQLException
	{
	  assertTrue(document != null);
	  
	  //Check that some data is as expected:
	  final TypedDataItem quickFindValue = new TypedDataItem();
	  quickFindValue.setText("Born To Run");
	  final String tableName = "albums";
	  final Condition whereClause = SqlUtils.getFindWhereClauseQuick(document, tableName, quickFindValue);
	  assertTrue(whereClause != null);

	  final List<LayoutItemField> fieldsToGet = new ArrayList<LayoutItemField>();
	  Field field = document.getField(tableName, "album_id");
	  final LayoutItemField layoutItemFieldAlbumID = new LayoutItemField();
	  layoutItemFieldAlbumID.setFullFieldDetails(field);
	  fieldsToGet.add(layoutItemFieldAlbumID);
	  field = document.getField(tableName, "name");
	  LayoutItemField layoutItemField = new LayoutItemField();
	  layoutItemField.setFullFieldDetails(field);
	  fieldsToGet.add(layoutItemField);
	  
	  final String sqlQuery = SqlUtils.buildSqlSelectWithWhereClause(tableName, fieldsToGet, whereClause, null);
	  
	  final Connection conn = selfHosterPostgreSQL.createConnection(false);
	  assertTrue(conn != null);
	  
	  final Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      //st.setFetchSize(length);
	  final ResultSet rs = st.executeQuery(sqlQuery);
	  assertTrue(rs != null);
	  
	  final ResultSetMetaData rsMetaData = rs.getMetaData();
	  Assert.assertEquals(2, rsMetaData.getColumnCount());
	  
	  rs.last();
	  final int rsRowsCount = rs.getRow();
	  Assert.assertEquals(1, rsRowsCount);
	  
	  final TypedDataItem albumID = new TypedDataItem();
	  SqlUtils.fillDataItemFromResultSet(albumID, layoutItemFieldAlbumID, 1,
				rs, "fake-document-id", tableName, null);
	  testExampleMusiccollectionDataRelated(selfHosterPostgreSQL, document, albumID);
	}

	/** Check that we can get data via a relationship.
	 * @param document
	 * @param albumID
	 * @throws SQLException 
	 */
	static private void testExampleMusiccollectionDataRelated(final SelfHosterPostgreSQL selfHosterPostgreSQL, final Document document, TypedDataItem albumID) throws SQLException {
		final String tableName = "albums";
		
		//Normal fields:
		final List<LayoutItemField> fieldsToGet = new ArrayList<LayoutItemField>();
		final Field fieldAlbumID = document.getField(tableName, "album_id");
		assertNotNull(fieldAlbumID);
		LayoutItemField layoutItemField = new LayoutItemField();
		layoutItemField.setFullFieldDetails(fieldAlbumID);
		fieldsToGet.add(layoutItemField);
		Field field = document.getField(tableName, "name");
		assertNotNull(field);
		layoutItemField = new LayoutItemField();
		layoutItemField.setFullFieldDetails(field);
		fieldsToGet.add(layoutItemField);
		  
		//Related field:
		final Relationship relationship = document.getRelationship(tableName, "artist");
		assertNotNull(relationship);
		layoutItemField = new LayoutItemField();
		layoutItemField.setRelationship(relationship);
		field = document.getField("artists", "name");
		assertNotNull(field);
		layoutItemField.setFullFieldDetails(field);
		fieldsToGet.add(layoutItemField);
		
		  
		final String sqlQuery = SqlUtils.buildSqlSelectWithKey(tableName, fieldsToGet, fieldAlbumID, albumID);
		  
		final Connection conn = selfHosterPostgreSQL.createConnection(false);
		assertTrue(conn != null);
		  
		final Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		//st.setFetchSize(length);
		final ResultSet rs = st.executeQuery(sqlQuery);
		assertTrue(rs != null);
		
		final ResultSetMetaData rsMetaData = rs.getMetaData();
		Assert.assertEquals(3, rsMetaData.getColumnCount());

		rs.last();
		final int rsRowsCount = rs.getRow();
		Assert.assertEquals(1, rsRowsCount);
	}
}
