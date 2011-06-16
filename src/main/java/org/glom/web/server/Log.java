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

/**
 * A class that wraps methods in com.allen_sauer.gwt.log.client.Log to add the calling method name from the servlet to
 * log messages.
 * 
 * @author Ben Konrath <ben@bagu.org>
 */
public class Log {

	// Fatal methods
	public static void fatal(String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + message, e);
	}

	public static void fatal(String message) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + message);
	}

	public static void fatal(String documentTitle, String tableName, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + documentTitle + " - " + tableName + ": "
				+ message, e);
	}

	public static void fatal(String documentTitle, String tableName, String message) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + documentTitle + " - " + tableName + ": "
				+ message);
	}

	public static void fatal(String documentTitle, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + documentTitle + ": " + message, e);
	}

	public static void fatal(String documentTitle, String message) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + documentTitle + ": " + message);
	}

	// Error methods
	public static void error(String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + message, e);
	}

	public static void error(String message) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + message);
	}

	public static void error(String documentTitle, String tableName, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + documentTitle + " - " + tableName + ": "
				+ message, e);
	}

	public static void error(String documentTitle, String tableName, String message) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + documentTitle + " - " + tableName + ": "
				+ message);
	}

	public static void error(String documentTitle, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + documentTitle + ": " + message, e);
	}

	public static void error(String documentTitle, String message) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + documentTitle + ": " + message);
	}

	// Warning methods
	public static void warn(String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + message, e);
	}

	public static void warn(String message) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + message);
	}

	public static void warn(String documentTitle, String tableName, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + documentTitle + " - " + tableName + ": "
				+ message, e);
	}

	public static void warn(String documentTitle, String tableName, String message) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + documentTitle + " - " + tableName + ": "
				+ message);
	}

	public static void warn(String documentTitle, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + documentTitle + ": " + message, e);
	}

	public static void warn(String documentTitle, String message) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + documentTitle + ": " + message);
	}

	// Info methods
	public static void info(String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + message, e);
	}

	public static void info(String message) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + message);
	}

	public static void info(String documentTitle, String tableName, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + documentTitle + " - " + tableName + ": "
				+ message, e);
	}

	public static void info(String documentTitle, String tableName, String message) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + documentTitle + " - " + tableName + ": "
				+ message);
	}

	public static void info(String documentTitle, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + documentTitle + ": " + message, e);
	}

	public static void info(String documentTitle, String message) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + documentTitle + ": " + message);
	}

	// helper method
	private static String getServletMethodName() {
		StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
		if (stackTraces.length > 3 && "org.glom.web.server.OnlineGlomServiceImpl".equals(stackTraces[3].getClassName()))
			return stackTraces[3].getMethodName() + " - ";
		return "";
	}

}
