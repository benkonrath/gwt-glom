/*
 * Copyright (C) 2010, 2011 Openismus GmbH
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.glom.libglom.Document;
import org.glom.libglom.Field;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.LayoutItem;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.SortFieldPair;
import org.glom.libglom.StringVector;
import org.glom.libglom.SortClause;
import org.glom.web.client.LibGlomService;
import org.glom.web.shared.GlomDocument;
import org.glom.web.shared.GlomTable;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

@SuppressWarnings("serial")
public class LibGlomServiceImpl extends RemoteServiceServlet implements LibGlomService {
	private Document document;
	ComboPooledDataSource cpds;

	// Called only when the servlet is stopped (the servlet container is stopped or restarted)
	public LibGlomServiceImpl() {
		Glom.libglom_init();
		document = new Document();
		// TODO hardcoded for now, need to figure out something for this
		document.set_file_uri("file:///home/ben/music-collection.glom");
		int error = 0;
		@SuppressWarnings("unused")
		boolean retval = document.load(error);
		// TODO handle error condition (also below)

		cpds = new ComboPooledDataSource();
		// load the jdbc driver
		try {
			cpds.setDriverClass("org.postgresql.Driver");
		} catch (PropertyVetoException e) {
			// TODO log error, fatal error can't continue, user can be nofified when db access doesn't work
			e.printStackTrace();
		}

		cpds.setJdbcUrl("jdbc:postgresql://" + document.get_connection_server() + "/"
				+ document.get_connection_database());
		// TODO figure out something for db user name and password
		cpds.setUser("ben");
		cpds.setPassword("ChangeMe"); // of course it's not the password I'm using on my server
	}

	/* FIXME I think Swig is generating long on 64-bit machines and int on 32-bit machines - need to keep this constant
	 * http://stackoverflow.com/questions /1590831/safely-casting-long-to-int-in-java */
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	public GlomDocument getGlomDocument() {
		GlomDocument glomDocument = new GlomDocument();

		// set visible title
		glomDocument.setTitle(document.get_database_title());

		// set array of GlomTables and the default table index
		StringVector tableNames = document.get_table_names();
		GlomTable[] tables = new GlomTable[safeLongToInt(tableNames.size())];
		for (int i = 0; i < tableNames.size(); i++) {
			String tableName = tableNames.get(i);
			GlomTable glomTable = new GlomTable();
			glomTable.setName(tableName);
			glomTable.setTitle(document.get_table_title(tableName));
			tables[i] = glomTable;
			if (tableName.equals(document.get_default_table())) {
				glomDocument.setDefaultTableIndex(i);
			}
		}
		glomDocument.setTables(tables);
		return glomDocument;

	}

	public String[] getLayoutListHeaders(String table) {
		LayoutGroupVector layoutList = document.get_data_layout_groups("list", table);
		LayoutItemVector layoutItems = layoutList.get(0).get_items();
		String[] headers = new String[safeLongToInt(layoutItems.size())];
		for (int i = 0; i < layoutItems.size(); i++) {
			headers[i] = layoutItems.get(i).get_title_or_name();
		}
		return headers;
	}

	public ArrayList<String[]> getTableData(int start, int length, String table) {
		LayoutGroupVector layoutList = document.get_data_layout_groups("list", table);
		LayoutItemVector layoutItems = layoutList.get(0).get_items();

		LayoutFieldVector layoutFields = new LayoutFieldVector();
		SortClause sortClause = new SortClause();
		for (int i = 0; i < layoutItems.size(); i++) {
			LayoutItem item = layoutItems.get(i);
			LayoutItem_Field field = LayoutItem_Field.cast_dynamic(item);
			if (field != null) {
				layoutFields.add(field);
				Field details = field.get_full_field_details();
				if (details != null && details.get_primary_key()) {
					sortClause.addLast(new SortFieldPair(field, true)); // ascending
				}
			}
		}

		ArrayList<String[]> rowsList = new ArrayList<String[]>();
		try {
			Connection conn = cpds.getConnection();
			Statement st = conn.createStatement();

			String query = Glom.build_sql_select_simple(table, layoutFields, sortClause);
			ResultSet rs = st.executeQuery(query);

			while (rs.next()) {
				String[] rowArray = new String[safeLongToInt(layoutItems.size())];
				for (int i = 0; i < layoutItems.size(); i++) {
					rowArray[i] = rs.getString(i + 1);
				}
				rowsList.add(rowArray);
			}

			rs.close();
			st.close();
		} catch (SQLException e) {
			// TODO: log error, notify user of problem
			e.printStackTrace();
		}

		return rowsList;
	}

	// Called only when the servlet is stopped (the servlet container is stopped or restarted)
	public void destroy() {
		Glom.libglom_deinit();
		try {
			DataSources.destroy(cpds);
		} catch (SQLException e) {
			// TODO log error, don't need to notify user because this is a clean up method
			e.printStackTrace();
		}
	}

}
