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

package org.glom.web.server;

import java.io.File;

import org.glom.libglom.Field.glom_field_type;
import org.glom.libglom.Value;
import org.glom.web.shared.PrimaryKeyItem;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class Utils {

	/*
	 * This method safely converts longs from libglom into ints. This method was taken from stackoverflow:
	 * 
	 * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
	 */
	public static int safeLongToInt(long value) {
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(value + " cannot be cast to int without changing its value.");
		}
		return (int) value;
	}

	public static String getFileName(String fileURI) {
		String[] splitURI = fileURI.split(File.separator);
		return splitURI[splitURI.length - 1];
	}

	// TODO: compare the type from the primary key to the type found in the Glom document
	public static Value getGdaValueForPrimaryKey(String documentID, String tableName, glom_field_type glomFieldType,
			PrimaryKeyItem primaryKeyValue) {
		Value gdaPrimaryKeyValue = null;
		switch (glomFieldType) {
		case TYPE_NUMERIC:
			gdaPrimaryKeyValue = primaryKeyValue.isEmpty() ? new Value() : new Value(primaryKeyValue.getNumber());
			break;
		case TYPE_TEXT:
			// TODO How can I validate or escape the text? It could have been created from the URL.
			gdaPrimaryKeyValue = primaryKeyValue.isEmpty() ? new Value("") : new Value(primaryKeyValue.getText());
			break;
		default:
			Log.error(documentID, tableName, "Unsupported Glom Field Type to use as a primary key: " + glomFieldType
					+ ". Query may not work.");
			break;
		}
		return gdaPrimaryKeyValue;
	}
}
