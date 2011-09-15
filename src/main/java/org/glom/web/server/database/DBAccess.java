/*
 * Copyright (C) 2011 Openismus GmbH
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

package org.glom.web.server.database;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import org.glom.libglom.Document;
import org.glom.libglom.Field;
import org.glom.libglom.FieldFormatting;
import org.glom.libglom.FieldVector;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.LayoutItem;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.LayoutItem_Portal;
import org.glom.libglom.NumericFormat;
import org.glom.web.server.Log;
import org.glom.web.server.Utils;
import org.glom.web.shared.GlomField;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
abstract class DBAccess {
	protected Document document;
	protected String documentID;
	protected String tableName;
	protected ComboPooledDataSource cpds;

	protected DBAccess(Document document, String documentID, ComboPooledDataSource cpds, String tableName) {
		this.document = document;
		this.documentID = documentID;
		this.cpds = cpds;
		this.tableName = tableName;
	}

	/*
	 * Converts data from a ResultSet to an ArrayList of GlomField array suitable for sending back to the client.
	 */
	final protected ArrayList<GlomField[]> convertResultSetToDTO(int length, LayoutFieldVector layoutFields,
			ResultSet rs) throws SQLException {

		// get the data we've been asked for
		int rowCount = 0;
		ArrayList<GlomField[]> rowsList = new ArrayList<GlomField[]>();
		while (rs.next() && rowCount <= length) {
			int layoutFieldsSize = Utils.safeLongToInt(layoutFields.size());
			GlomField[] rowArray = new GlomField[layoutFieldsSize];
			for (int i = 0; i < layoutFieldsSize; i++) {
				// make a new GlomField to set the text and colours
				rowArray[i] = new GlomField();

				// get foreground and background colours
				LayoutItem_Field field = layoutFields.get(i);
				FieldFormatting formatting = field.get_formatting_used();
				String fgcolour = formatting.get_text_format_color_foreground();
				if (!fgcolour.isEmpty())
					rowArray[i].setFGColour(convertGdkColorToHtmlColour(fgcolour));
				String bgcolour = formatting.get_text_format_color_background();
				if (!bgcolour.isEmpty())
					rowArray[i].setBGColour(convertGdkColorToHtmlColour(bgcolour));

				// Convert the field value to a string based on the glom type. We're doing the formatting on the
				// server side for now but it might be useful to move this to the client side.
				switch (field.get_glom_type()) {
				case TYPE_TEXT:
					String text = rs.getString(i + 1);
					rowArray[i].setText(text != null ? text : "");
					break;
				case TYPE_BOOLEAN:
					rowArray[i].setBoolean(rs.getBoolean(i + 1));
					break;
				case TYPE_NUMERIC:
					// Take care of the numeric formatting before converting the number to a string.
					NumericFormat numFormatGlom = formatting.getM_numeric_format();
					// There's no isCurrency() method in the glom NumericFormat class so we're assuming that the
					// number should be formatted as a currency if the currency code string is not empty.
					String currencyCode = numFormatGlom.getM_currency_symbol();
					NumberFormat numFormatJava = null;
					boolean useGlomCurrencyCode = false;
					if (currencyCode.length() == 3) {
						// Try to format the currency using the Java Locales system.
						try {
							Currency currency = Currency.getInstance(currencyCode);
							Log.info(documentID, tableName, "A valid ISO 4217 currency code is being used."
									+ " Overriding the numeric formatting with information from the locale.");
							int digits = currency.getDefaultFractionDigits();
							numFormatJava = NumberFormat.getCurrencyInstance(Locale.ROOT);
							numFormatJava.setCurrency(currency);
							numFormatJava.setMinimumFractionDigits(digits);
							numFormatJava.setMaximumFractionDigits(digits);
						} catch (IllegalArgumentException e) {
							Log.warn(documentID, tableName, currencyCode + " is not a valid ISO 4217 code."
									+ " Manually setting currency code with this value.");
							// The currency code is not this is not an ISO 4217 currency code.
							// We're going to manually set the currency code and use the glom numeric formatting.
							useGlomCurrencyCode = true;
							numFormatJava = convertToJavaNumberFormat(numFormatGlom);
						}
					} else if (currencyCode.length() > 0) {
						Log.warn(documentID, tableName, currencyCode + " is not a valid ISO 4217 code."
								+ " Manually setting currency code with this value.");
						// The length of the currency code is > 0 and != 3; this is not an ISO 4217 currency code.
						// We're going to manually set the currency code and use the glom numeric formatting.
						useGlomCurrencyCode = true;
						numFormatJava = convertToJavaNumberFormat(numFormatGlom);
					} else {
						// The length of the currency code is 0; the number is not a currency.
						numFormatJava = convertToJavaNumberFormat(numFormatGlom);
					}

					// TODO: Do I need to do something with NumericFormat.get_default_precision() from libglom?

					double number = rs.getDouble(i + 1);
					if (number < 0) {
						if (formatting.getM_numeric_format().getM_alt_foreground_color_for_negatives())
							// overrides the set foreground colour
							rowArray[i].setFGColour(convertGdkColorToHtmlColour(NumericFormat
									.get_alternative_color_for_negatives()));
					}

					// Finally convert the number to text using the glom currency string if required.
					if (useGlomCurrencyCode) {
						rowArray[i].setText(currencyCode + " " + numFormatJava.format(number));
					} else {
						rowArray[i].setText(numFormatJava.format(number));
					}
					break;
				case TYPE_DATE:
					Date date = rs.getDate(i + 1);
					if (date != null) {
						DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ROOT);
						rowArray[i].setText(dateFormat.format(date));
					} else {
						rowArray[i].setText("");
					}
					break;
				case TYPE_TIME:
					Time time = rs.getTime(i + 1);
					if (time != null) {
						DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.ROOT);
						rowArray[i].setText(timeFormat.format(time));
					} else {
						rowArray[i].setText("");
					}
					break;
				case TYPE_IMAGE:
					byte[] image = rs.getBytes(i + 1);
					if (image != null) {
						// TODO implement field TYPE_IMAGE
						rowArray[i].setText("Image (FIXME)");
					} else {
						rowArray[i].setText("");
					}
					break;
				case TYPE_INVALID:
				default:
					Log.warn(documentID, tableName, "Invalid LayoutItem Field type. Using empty string for value.");
					rowArray[i].setText("");
					break;
				}
			}

			// add the row of GlomFields to the ArrayList we're going to return and update the row count
			rowsList.add(rowArray);
			rowCount++;
		}

		return rowsList;
	}

	/*
	 * Gets a LayoutFieldVector to use when generating an SQL query.
	 */
	protected LayoutFieldVector getFieldsToShowForSQLQuery(LayoutGroupVector layoutGroupVec) {
		LayoutFieldVector layoutFieldVector = new LayoutFieldVector();

		// We will show the fields that the document says we should:
		for (int i = 0; i < layoutGroupVec.size(); i++) {
			org.glom.libglom.LayoutGroup layoutGroup = layoutGroupVec.get(i);

			// satisfy the precondition of getDetailsLayoutGroup(String tableName, org.glom.libglom.LayoutGroup
			// libglomLayoutGroup)
			if (layoutGroup == null)
				continue;

			// Get the fields:
			ArrayList<LayoutItem_Field> layoutItemFields = getFieldsToShowForSQLQueryAddGroup(layoutGroup);
			for (LayoutItem_Field layoutItem_Field : layoutItemFields) {
				layoutFieldVector.add(layoutItem_Field);
			}
		}
		return layoutFieldVector;
	}

	/*
	 * Gets an ArrayList of LayoutItem_Field objects to use when generating an SQL query.
	 * 
	 * @precondition libglomLayoutGroup must not be null
	 */
	private ArrayList<LayoutItem_Field> getFieldsToShowForSQLQueryAddGroup(
			org.glom.libglom.LayoutGroup libglomLayoutGroup) {

		ArrayList<LayoutItem_Field> layoutItemFields = new ArrayList<LayoutItem_Field>();
		LayoutItemVector items = libglomLayoutGroup.get_items();
		int numItems = Utils.safeLongToInt(items.size());
		for (int i = 0; i < numItems; i++) {
			LayoutItem layoutItem = items.get(i);

			LayoutItem_Field layoutItemField = LayoutItem_Field.cast_dynamic(layoutItem);
			if (layoutItemField != null) {
				// the layoutItem is a LayoutItem_Field
				FieldVector fields;
				if (layoutItemField.get_has_relationship_name()) {
					// layoutItemField is a field in a related table
					fields = document.get_table_fields(layoutItemField.get_table_used(tableName));
				} else {
					// layoutItemField is a field in this table
					fields = document.get_table_fields(tableName);
				}

				// set the layoutItemFeild with details from its Field in the document and
				// add it to the list to be returned
				for (int j = 0; j < fields.size(); j++) {
					// check the names to see if they're the same
					// this works because we're using the field list from the related table if necessary
					if (layoutItemField.get_name().equals(fields.get(j).get_name())) {
						Field field = fields.get(j);
						if (field != null) {
							layoutItemField.set_full_field_details(field);
							layoutItemFields.add(layoutItemField);
						} else {
							Log.warn(document.get_database_title(), tableName,
									"LayoutItem_Field " + layoutItemField.get_layout_display_name()
											+ " not found in document field list.");
						}
						break;
					}
				}

			} else {
				// the layoutItem is not a LayoutItem_Field
				org.glom.libglom.LayoutGroup subLayoutGroup = org.glom.libglom.LayoutGroup.cast_dynamic(layoutItem);
				if (subLayoutGroup != null) {
					// the layoutItem is a LayoutGroup
					LayoutItem_Portal layoutItemPortal = LayoutItem_Portal.cast_dynamic(layoutItem);
					if (layoutItemPortal == null) {
						// The subGroup is not a LayoutItem_Portal.
						// We're ignoring portals because they are filled by means of a separate SQL query.
						layoutItemFields.addAll(getFieldsToShowForSQLQueryAddGroup(subLayoutGroup));
					}
				}
			}
		}
		return layoutItemFields;
	}

	/**
	 * Gets the primary key Field for this table.
	 * 
	 * @return primary key Field
	 */
	public Field getPrimaryKeyField() {
		Field primaryKey = null;
		FieldVector fieldsVec = document.get_table_fields(tableName);
		for (int i = 0; i < Utils.safeLongToInt(fieldsVec.size()); i++) {
			Field field = fieldsVec.get(i);
			if (field.get_primary_key()) {
				primaryKey = field;
				break;
			}
		}
		return primaryKey;
	}

	/**
	 * Gets the primary key LayoutItem_Field for this table.
	 * 
	 * @return primary key LayoutItem_Field
	 */
	public LayoutItem_Field getPrimaryKeyLayoutItemField() {
		Field primaryKey = getPrimaryKeyField();

		LayoutItem_Field libglomLayoutItemField = new LayoutItem_Field();

		if (primaryKey != null) {
			libglomLayoutItemField.set_full_field_details(primaryKey);
		} else {
			Log.error(document.get_database_title(), tableName,
					"A primary key was not found in the FieldVector for this table.");
		}

		return libglomLayoutItemField;
	}

	/*
	 * Converts a Gdk::Color (16-bits per channel) to an HTML colour (8-bits per channel) by discarding the least
	 * significant 8-bits in each channel.
	 */
	private String convertGdkColorToHtmlColour(String gdkColor) {
		if (gdkColor.length() == 13)
			return gdkColor.substring(0, 3) + gdkColor.substring(5, 7) + gdkColor.substring(9, 11);
		else if (gdkColor.length() == 7) {
			// This shouldn't happen but let's deal with it if it does.
			Log.warn("Expected a 13 character string but received a 7 character string. Returning received string.");
			return gdkColor;
		} else {
			Log.error("Did not receive a 13 or 7 character string. Returning black HTML colour code.");
			return "#000000";
		}
	}

	private static NumberFormat convertToJavaNumberFormat(NumericFormat numFormatGlom) {
		// TODO implement locale
		NumberFormat numFormatJava = NumberFormat.getInstance(Locale.ROOT);
		if (numFormatGlom.getM_decimal_places_restricted()) {
			int digits = Utils.safeLongToInt(numFormatGlom.getM_decimal_places());
			numFormatJava.setMinimumFractionDigits(digits);
			numFormatJava.setMaximumFractionDigits(digits);
		}
		numFormatJava.setGroupingUsed(numFormatGlom.getM_use_thousands_separator());
		return numFormatJava;
	}

}
