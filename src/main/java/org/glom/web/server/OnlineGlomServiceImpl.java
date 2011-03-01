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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import org.glom.libglom.Document;
import org.glom.libglom.Field;
import org.glom.libglom.FieldFormatting;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.LayoutItem;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.NumericFormat;
import org.glom.libglom.SortClause;
import org.glom.libglom.SortFieldPair;
import org.glom.libglom.StringVector;
import org.glom.web.client.OnlineGlomService;
import org.glom.web.shared.GlomDocument;
import org.glom.web.shared.LayoutListTable;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

@SuppressWarnings("serial")
public class OnlineGlomServiceImpl extends RemoteServiceServlet implements OnlineGlomService {
	private Document document;
	private ComboPooledDataSource cpds;
	// TODO implement locale
	private Locale locale = Locale.ENGLISH;

	// Called only when the servlet is stopped (the servlet container is stopped or restarted)
	public OnlineGlomServiceImpl() {
		Glom.libglom_init();
		document = new Document();
		// TODO hardcoded for now, need to figure out something for this
		document.set_file_uri("file:///home/ben/small-business-example.glom");
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

	/*
	 * FIXME I think Swig is generating long on 64-bit machines and int on 32-bit machines - need to keep this constant
	 * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
	 */
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	public GlomDocument getGlomDocument() {
		GlomDocument glomDocument = new GlomDocument();

		// get arrays of table names and titles, and find the default table index
		StringVector tablesVec = document.get_table_names();
		int numTables = safeLongToInt(tablesVec.size());
		String[] tableNames = new String[numTables];
		String[] tableTitles = new String[numTables];
		boolean foundDefaultTable = false;
		for (int i = 0; i < numTables; i++) {
			String tableName = tablesVec.get(i);
			tableNames[i] = tableName;
			// JNI is "expensive", the comparison will only be called if we haven't already found the default table
			if (!foundDefaultTable && tableName.equals(document.get_default_table())) {
				glomDocument.setDefaultTableIndex(i);
				foundDefaultTable = true;
			}
			tableTitles[i] = document.get_table_title(tableName);
		}

		// set everything we need
		glomDocument.setTableNames(tableNames);
		glomDocument.setTableTitles(tableTitles);
		glomDocument.setTitle(document.get_database_title());

		return glomDocument;
	}

	public LayoutListTable getLayoutListTable(String tableName) {
		LayoutListTable tableInfo = new LayoutListTable();

		LayoutGroupVector layoutListVec = document.get_data_layout_groups("list", tableName);
		LayoutItemVector layoutItemsVec = layoutListVec.get(0).get_items();
		int numItems = safeLongToInt(layoutItemsVec.size());
		String[] columnTitles = new String[numItems];
		LayoutFieldVector layoutFields = new LayoutFieldVector();
		for (int i = 0; i < numItems; i++) {
			LayoutItem item = layoutItemsVec.get(i);
			columnTitles[i] = item.get_title_or_name();
			LayoutItem_Field field = LayoutItem_Field.cast_dynamic(item);
			if (field != null) {
				layoutFields.add(field);
			}
		}

		tableInfo.setColumnTitles(columnTitles);

		// get the size of the returned query for the pager
		// TODO since we're executing a query anyway, maybe we should return the rows that will be displayed on the
		// first page
		// TODO this code is really similar to code in getTableData, find a way to not duplicate the code
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			// setup and execute the query
			conn = cpds.getConnection();
			st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String query = Glom.build_sql_select_simple(tableName, layoutFields);
			rs = st.executeQuery(query);

			// get the number of rows in the query
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			rs.last();
			tableInfo.setNumRows(rs.getRow());

		} catch (SQLException e) {
			// TODO log error
			// we don't know how many rows are in the query
			e.printStackTrace();
			tableInfo.setNumRows(0);
		} finally {
			// this is a little awkward but we want to ensure that we're cleaning everything up that has been used
			try {
				rs.close();
				st.close();
				conn.close();
			} catch (Exception e) {
				// TODO log error
				e.printStackTrace();
			}
		}

		return tableInfo;
	}

	public ArrayList<String[]> getTableData(int start, int length, String table) {
		LayoutGroupVector layoutList = document.get_data_layout_groups("list", table);
		LayoutItemVector layoutItems = layoutList.get(0).get_items();

		LayoutFieldVector layoutFields = new LayoutFieldVector();
		SortClause sortClause = new SortClause();
		int numItems = safeLongToInt(layoutItems.size());
		for (int i = 0; i < numItems; i++) {
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
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			// setup and execute the query
			conn = cpds.getConnection();
			st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String query = Glom.build_sql_select_simple(table, layoutFields, sortClause);
			rs = st.executeQuery(query);

			// get data we're asked for
			// TODO need to setup the result set in cursor mode so that not all of the results are pulled into memory
			rs.setFetchDirection(ResultSet.FETCH_FORWARD);
			rs.absolute(start);
			int rowCount = 0;
			while (rs.next() && rowCount <= length) {
				int layoutItemsSize = safeLongToInt(layoutItems.size());
				String[] rowArray = new String[layoutItemsSize];
				for (int i = 0; i < layoutItemsSize; i++) {
					LayoutItem_Field field = layoutFields.get(i);
					FieldFormatting formatting = field.get_formatting_used();

					// field values are converted to strings differently for every glom type
					Field.glom_field_type fieldType = field.get_glom_type();
					switch (fieldType) {
					case TYPE_TEXT:
						rowArray[i] = rs.getString(i + 1);
						break;
					case TYPE_BOOLEAN:
						rowArray[i] = rs.getBoolean(i + 1) ? "TRUE" : "FALSE";
						break;
					case TYPE_NUMERIC:
						NumericFormat numFormatGlom = formatting.getM_numeric_format();
						// there's no isCurrency() method in the glom NumericFormat class so we're assuming that the
						// number should be formatted as a currency if the currency symbol is set
						String currencySymbol = numFormatGlom.getM_currency_symbol();
						NumberFormat numFormatJava;
						if (currencySymbol.length() == 3) {
							Currency currency = Currency.getInstance(currencySymbol);
							// we're not using the glom value for digits or grouping when it's a currency
							int digits = currency.getDefaultFractionDigits();
							numFormatJava = (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
							numFormatJava.setCurrency(currency);
							numFormatJava.setMinimumFractionDigits(digits);
							numFormatJava.setMaximumFractionDigits(digits);
						} else {
							numFormatJava = NumberFormat.getInstance(locale);
							if (numFormatGlom.getM_decimal_places_restricted()) {
								int digits = safeLongToInt(numFormatGlom.getM_decimal_places());
								numFormatJava.setMinimumFractionDigits(digits);
								numFormatJava.setMaximumFractionDigits(digits);
							}
							numFormatJava.setGroupingUsed(numFormatGlom.getM_use_thousands_separator());
						}

						// TODO: Do I need to do something with this from libglom?
						// NumericFormat.get_default_precision();

						rowArray[i] = numFormatJava.format(rs.getDouble(i + 1));
						break;
					case TYPE_DATE:
						Date date = rs.getDate(i + 1);
						if (date != null) {
							DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
							rowArray[i] = dateFormat.format(rs.getDate(i + 1));
						} else {
							rowArray[i] = "";
						}
						break;
					case TYPE_TIME:
						Time time = rs.getTime(i + 1);
						if (time != null) {
							DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
							rowArray[i] = timeFormat.format(time);
						} else {
							rowArray[i] = "";
						}
						break;
					case TYPE_IMAGE:
						// TODO log warning message
						break;
					case TYPE_INVALID:
					default:
						// TODO log warning message
						break;
					}
				}
				rowsList.add(rowArray);
				rowCount++;
			}
		} catch (SQLException e) {
			// TODO: log error, notify user of problem
			e.printStackTrace();
		} finally {
			// this is a little awkward but we want to ensure that we're cleaning everything up that has been used
			try {
				rs.close();
				st.close();
				conn.close();
			} catch (Exception e) {
				// TODO log error
				e.printStackTrace();
			}
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