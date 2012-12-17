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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemImage;

import com.google.gwt.http.client.Response;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Murray Cumming <murrayc@murrayc.com>
 * 
 */
public class OnlineGlomImagesServlet extends OnlineGlomServlet {
	
	private static final long serialVersionUID = 4001959815578006604L;

	public OnlineGlomImagesServlet() {
		super();
	}
	
	private void doError(HttpServletResponse resp, int errorCode, final String errorMessage) throws IOException {
		Log.error(errorMessage);
		resp.sendError(errorCode, errorMessage);
	}
	
	private void doError(HttpServletResponse resp, int errorCode, final String errorMessage, final String documentID) throws IOException {
		Log.error(documentID, errorMessage);
		resp.sendError(errorCode, errorMessage);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		//These match the history token keys in DetailsPlace:
		final String attrDocumentID = StringUtils.defaultString(req.getParameter("document"));
		final String attrTableName = StringUtils.defaultString(req.getParameter("table"));
		final String attrPrimaryKeyValue = StringUtils.defaultString(req.getParameter("value"));
		final String attrFieldName = StringUtils.defaultString(req.getParameter("field"));
		
		//To request a static LayoutItemImage from the document
		//instead of from the database:
		final String attrLayoutName = StringUtils.defaultString(req.getParameter("layout"));
		final String attrLayoutPath = StringUtils.defaultString(req.getParameter("layoutpath"));
		
		if(StringUtils.isEmpty(attrDocumentID)) {
			doError(resp, Response.SC_NOT_FOUND, "No document ID was specified.");
			return;
		}

		if(!isAuthenticated(req, attrDocumentID)) {
			doError(resp, Response.SC_NOT_FOUND, "No access to the document.", attrDocumentID);
			return;
		}
		
		if(StringUtils.isEmpty(attrTableName)) {
			doError(resp, Response.SC_NOT_FOUND, "No table name was specified.", attrDocumentID);
			return;
		}
		
		//TODO: Is it from the database or is it a static LayouteItemText from the document.
		
		final boolean fromDb = !StringUtils.isEmpty(attrPrimaryKeyValue);
		final boolean fromLayout = !StringUtils.isEmpty(attrLayoutName);
		
		if(!fromDb && !fromLayout) {
			doError(resp, Response.SC_NOT_FOUND, "No primary key value or layout name was specified.", attrDocumentID);
			return;
		}
		
		if(fromDb && StringUtils.isEmpty(attrFieldName)) {
			doError(resp, Response.SC_NOT_FOUND, "No field name was specified.", attrDocumentID);
			return;
		}

		final ConfiguredDocument configuredDocument = getDocument(attrDocumentID);
		if(configuredDocument == null) {
			doError(resp, Response.SC_NOT_FOUND, "The specified document was not found.", attrDocumentID);
			return;
		}

		final Document document = configuredDocument.getDocument();
		if(document == null) {
			doError(resp, Response.SC_NOT_FOUND, "The specified document details were not found.", attrDocumentID);
			return;
		}
		
		byte[] bytes = null;
		if(fromDb) {
			bytes = getImageFromDatabase(req, resp, attrDocumentID, attrTableName, attrPrimaryKeyValue, attrFieldName,
				configuredDocument, document);
		} else {
			bytes = getImageFromDocument(req, resp, attrDocumentID, attrTableName, attrLayoutName, attrLayoutPath,
					configuredDocument, document);
		}
		
		if(bytes == null) {
			doError(resp, Response.SC_NOT_FOUND, "The image bytes could not be found. Please see the earlier error.", attrDocumentID);
			return;
		}
		
		final InputStream is = new ByteArrayInputStream(bytes);
		final String contentType = URLConnection.guessContentTypeFromStream(is);	
		resp.setContentType(contentType);

		// Set content size:
		resp.setContentLength((int) bytes.length);

		// Open the output stream:
		final OutputStream out = resp.getOutputStream();

		// Copy the contents to the output stream
		out.write(bytes);
		out.close();
	}

	/** Get the image from a specific <data_layout_text> node of a specific layout for a specific table in the document,
	 * with no access to the database data.
	 * 
	 * @param resp
	 * @param attrDocumentID
	 * @param attrTableName
	 * @param attrLayoutName
	 * @param attrLayoutPath
	 * @param configuredDocument
	 * @param document
	 * @return
	 * @throws IOException 
	 */
	private byte[] getImageFromDocument(final HttpServletRequest request , final HttpServletResponse resp, final String attrDocumentID, final String attrTableName,
			final String attrLayoutName, final String attrLayoutPath, final ConfiguredDocument configuredDocument, final Document document) throws IOException {
		final LayoutItem item = document.getLayoutItemByPath(attrTableName, attrLayoutName, attrLayoutPath);
		
		if(item == null) {
			doError(resp, Response.SC_NOT_FOUND, "The item specifed by the layout path could not be found, attrLayoutPath=" + attrLayoutPath, attrDocumentID);
			return null;
		}
		
		if(!(item instanceof LayoutItemImage)) {
			doError(resp, Response.SC_NOT_FOUND, "The item specifed by the layout path is not an image. It has class: " + item.getClass().getName() + " and item name=" + item.getName() + ", attrLayoutPath=" + attrLayoutPath, attrDocumentID);
			return null;
		}
		
		final LayoutItemImage image = (LayoutItemImage)item;
		return image.getImage().getImageData();
	}

	/** Get the image from a specific field of a specific record in a specific table in the database.
	 * 
	 * @param resp
	 * @param attrDocumentID
	 * @param attrTableName
	 * @param attrPrimaryKeyValue
	 * @param attrFieldName
	 * @param configuredDocument
	 * @param document
	 * @return
	 * @throws IOException
	 */
	private byte[] getImageFromDatabase(final HttpServletRequest request, final HttpServletResponse resp, final String attrDocumentID,
			final String attrTableName, final String attrPrimaryKeyValue, final String attrFieldName,
			final ConfiguredDocument configuredDocument, final Document document) throws IOException {
		final Field field = document.getField(attrTableName, attrFieldName);
		if(field == null) {
			doError(resp, Response.SC_NOT_FOUND, "The specified field was not found: field=" + attrFieldName, attrDocumentID);
			return null;
		}
		
		final Field fieldPrimaryKey = document.getTablePrimaryKeyField(attrTableName);
		
		TypedDataItem primaryKeyValue = new TypedDataItem();
		primaryKeyValue.setNumber(Double.parseDouble(attrPrimaryKeyValue));
		
		final LayoutItemField layoutItemField = new LayoutItemField();
		layoutItemField.setFullFieldDetails(field);
		final List<LayoutItemField> fieldsToGet = new ArrayList<LayoutItemField>();
		fieldsToGet.add(layoutItemField);
		final String query = SqlUtils.buildSqlSelectWithKey(attrTableName, fieldsToGet, fieldPrimaryKey, primaryKeyValue);
		
		final ComboPooledDataSource authenticatedConnection = getConnection(request, attrDocumentID);
		if(authenticatedConnection == null) {
			return null;
		}
		
		Connection connection = null;
		try {
			connection = authenticatedConnection.getConnection();
		} catch (final SQLException e) {
			//e.printStackTrace();

			doError(resp, Response.SC_INTERNAL_SERVER_ERROR, "SQL exception: " + e.getMessage(), attrDocumentID);
			return null;
		}
		
		Statement st = null;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			doError(resp, Response.SC_INTERNAL_SERVER_ERROR, "SQL exception: " + e.getMessage(), attrDocumentID);
			return null;
		}
		
		if(st == null) {
			doError(resp, Response.SC_INTERNAL_SERVER_ERROR, "The SQL statement is null.", attrDocumentID);
			return null;
		}

		ResultSet rs = null;
		try {
			rs = st.executeQuery(query);
		} catch (SQLException e) {
			doError(resp, Response.SC_INTERNAL_SERVER_ERROR, "SQL exception: " + e.getMessage(), attrDocumentID);
			return null;
		}

		if(rs == null) {
			doError(resp, Response.SC_INTERNAL_SERVER_ERROR, "The SQL result set is null.", attrDocumentID);
			return null;
		}
		
		byte[] bytes = null;
		try {
			rs.next();
			bytes = rs.getBytes(1); //This is 1-indexed, not 0-indexed.
		} catch (SQLException e) {
			doError(resp, Response.SC_INTERNAL_SERVER_ERROR, "SQL exception: " + e.getMessage(), attrDocumentID);
			return null;
		}

		if(bytes == null) {
			doError(resp, Response.SC_INTERNAL_SERVER_ERROR, "The database contained null.", attrDocumentID);
			return null;
		}

		return bytes;
	}
}
