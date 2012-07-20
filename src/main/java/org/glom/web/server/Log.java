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

import org.apache.commons.lang3.StringUtils;

/**
 * A class that wraps methods in com.allen_sauer.gwt.log.client.Log to add the calling method name from the servlet to
 * log messages.
 */
public class Log {

	// Fatal methods
	public static void fatal(final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + StringUtils.defaultString(message), e);
	}

	public static void fatal(final String message) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + StringUtils.defaultString(message));
	}

	public static void fatal(final String documentID, final String tableName, final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + StringUtils.defaultString(documentID) + " - " + StringUtils.defaultString(tableName) + ": "
				+ StringUtils.defaultString(message), e);
	}

	public static void fatal(final String documentID, final String tableName, final String message) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + StringUtils.defaultString(documentID) + " - " + StringUtils.defaultString(tableName) + ": "
				+ StringUtils.defaultString(message));
	}

	public static void fatal(final String documentID, final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + StringUtils.defaultString(documentID) + ": " + StringUtils.defaultString(message), e);
	}

	public static void fatal(final String documentID, final String message) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + StringUtils.defaultString(documentID) + ": " + StringUtils.defaultString(message));
	}

	// Error methods
	public static void error(final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + StringUtils.defaultString(message), e);
	}

	public static void error(final String message) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + StringUtils.defaultString(message));
	}

	public static void error(final String documentID, final String tableName, final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + StringUtils.defaultString(documentID) + " - " + StringUtils.defaultString(tableName) + ": "
				+ StringUtils.defaultString(message), e);
	}

	public static void error(final String documentID, final String tableName, final String message) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + StringUtils.defaultString(documentID) + " - " + StringUtils.defaultString(tableName) + ": "
				+ StringUtils.defaultString(message));
	}

	public static void error(final String documentID, final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + StringUtils.defaultString(documentID) + ": " + StringUtils.defaultString(message), e);
	}

	public static void error(final String documentID, final String message) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + StringUtils.defaultString(documentID) + ": " + StringUtils.defaultString(message));
	}

	// Warning methods
	public static void warn(final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + StringUtils.defaultString(message), e);
	}

	public static void warn(final String message) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + StringUtils.defaultString(message));
	}

	public static void warn(final String documentID, final String tableName, final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + StringUtils.defaultString(documentID) + " - " + StringUtils.defaultString(tableName) + ": "
				+ StringUtils.defaultString(message), e);
	}

	public static void warn(final String documentID, final String tableName, final String message) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + StringUtils.defaultString(documentID) + " - " + StringUtils.defaultString(tableName) + ": "
				+ StringUtils.defaultString(message));
	}

	public static void warn(final String documentID, final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + StringUtils.defaultString(documentID) + ": " + StringUtils.defaultString(message), e);
	}

	public static void warn(final String documentID, final String message) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + StringUtils.defaultString(documentID) + ": " + StringUtils.defaultString(message));
	}

	// Info methods
	public static void info(final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + StringUtils.defaultString(message), e);
	}

	public static void info(final String message) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + StringUtils.defaultString(message));
	}

	public static void info(final String documentID, final String tableName, final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + StringUtils.defaultString(documentID) + " - " + StringUtils.defaultString(tableName) + ": "
				+ StringUtils.defaultString(message), e);
	}

	public static void info(final String documentID, final String tableName, final String message) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + StringUtils.defaultString(documentID) + " - " + StringUtils.defaultString(tableName) + ": "
				+ StringUtils.defaultString(message));
	}

	public static void info(final String documentID, final String message, final Throwable e) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + StringUtils.defaultString(documentID) + ": " + StringUtils.defaultString(message), e);
	}

	public static void info(final String documentID, final String message) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + StringUtils.defaultString(documentID) + ": " + StringUtils.defaultString(message));
	}

	// helper method
	private static String getServletMethodName() {
		final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
		if (stackTraces.length > 3 && "org.glom.web.server.OnlineGlomServiceImpl".equals(stackTraces[3].getClassName())) {
			return stackTraces[3].getMethodName() + " - ";
		}
		return "";
	}

}
