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

package org.glom.web.client;

import org.glom.web.shared.DataItem;
import org.glom.web.shared.GlomNumericFormat;
import org.glom.web.shared.layout.LayoutItemField.GlomFieldType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class Utils {

	public static NumberFormat getNumberFormat(GlomNumericFormat glomNumericFormat) {
		NumberFormat gwtNumberFormat = null;

		// try to format the currency with the GWT currency support
		String currencyCode = glomNumericFormat.getCurrencyCode();
		if (!currencyCode.isEmpty()) {
			try {
				gwtNumberFormat = NumberFormat.getCurrencyFormat(currencyCode);
			} catch (IllegalArgumentException e) {
				GWT.log("Error creating GWT NumberFormat for currencyCode " + currencyCode
						+ ". Building NumberFormat with GlomNumericFormat settings.");
			}
		}

		// build our own format pattern based on the GlomNumericFormat if the currency code wasn't set or had an
		// error
		if (gwtNumberFormat == null) {
			StringBuilder pattern = new StringBuilder("0.");

			// add pattern for thousands separator
			if (glomNumericFormat.getUseThousandsSeparator()) {
				pattern.insert(0, "#,##");
			}

			// add pattern for restricted decimal places
			if (glomNumericFormat.getDecimalPlacesRestricted()) {
				for (int i = 0; i < glomNumericFormat.getDecimalPlaces(); i++) {
					pattern.append('0');
				}
			} else {
				// The default precision in libglom is 15.
				pattern.append("###############");
			}

			// TODO use exponential numbers when more than 15 decimal places

			gwtNumberFormat = NumberFormat.getFormat(pattern.toString());
		}

		return gwtNumberFormat;
	}

	/*
	 * Can be used for getting the primary key value and the foreign key value string.
	 */
	public static String getKeyValueStringForQuery(GlomFieldType fieldType, DataItem dataItem) {
		switch (fieldType) {
		case TYPE_NUMERIC:
			String retval = new Double(dataItem.getNumber()).toString();
			if (retval.endsWith(".0"))
				return retval.substring(0, retval.length() - 2);
			else
				return retval;
		case TYPE_TEXT:
		case TYPE_DATE:
		case TYPE_TIME:
		case TYPE_IMAGE:
			return dataItem.getText();
		case TYPE_BOOLEAN:
		case TYPE_INVALID:
		default:
			GWT.log("Error: Invalid primary key type for query.");
			return null;
		}

	}
}
