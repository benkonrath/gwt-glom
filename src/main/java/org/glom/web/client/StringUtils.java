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

package org.glom.web.client;

/**
 */
public class StringUtils {

	public static boolean isEmpty(final String str) {
		return (str == null) || (str.isEmpty());
	}

	/**
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean equals(final String a, final String b) {
		if (a == null) {
			return b == null;
		}

		if (b == null) {
			return false; // a was already checked for null.
		}

		return a.equals(b);
	}

	/**
	 * @param text
	 * @return
	 */
	public static String defaultString(final String text) {
		if (text == null) {
			return "";
		} else {
			return text;
		}
	}

}
