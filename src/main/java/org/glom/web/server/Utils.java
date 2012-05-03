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

/**
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

}
