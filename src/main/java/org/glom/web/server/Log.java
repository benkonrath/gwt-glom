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
 */
public class Log {

	// Fatal methods
	public static void fatal(String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + message, e);
	}

	public static void fatal(String message) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + message);
	}

	public static void fatal(String documentID, String tableName, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + documentID + " - " + tableName + ": "
				+ message, e);
	}

	public static void fatal(String documentID, String tableName, String message) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + documentID + " - " + tableName + ": "
				+ message);
	}

	public static void fatal(String documentID, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + documentID + ": " + message, e);
	}

	public static void fatal(String documentID, String message) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + documentID + ": " + message);
	}

	// Error methods
	public static void error(String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + message, e);
	}

	public static void error(String message) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + message);
	}

	public static void error(String documentID, String tableName, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + documentID + " - " + tableName + ": "
				+ message, e);
	}

	public static void error(String documentID, String tableName, String message) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + documentID + " - " + tableName + ": "
				+ message);
	}

	public static void error(String documentID, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + documentID + ": " + message, e);
	}

	public static void error(String documentID, String message) {
		com.allen_sauer.gwt.log.client.Log.error(getServletMethodName() + documentID + ": " + message);
	}

	// Warning methods
	public static void warn(String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.fatal(getServletMethodName() + message, e);
	}

	public static void warn(String message) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + message);
	}

	public static void warn(String documentID, String tableName, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + documentID + " - " + tableName + ": "
				+ message, e);
	}

	public static void warn(String documentID, String tableName, String message) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + documentID + " - " + tableName + ": "
				+ message);
	}

	public static void warn(String documentID, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + documentID + ": " + message, e);
	}

	public static void warn(String documentID, String message) {
		com.allen_sauer.gwt.log.client.Log.warn(getServletMethodName() + documentID + ": " + message);
	}

	// Info methods
	public static void info(String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + message, e);
	}

	public static void info(String message) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + message);
	}

	public static void info(String documentID, String tableName, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + documentID + " - " + tableName + ": "
				+ message, e);
	}

	public static void info(String documentID, String tableName, String message) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + documentID + " - " + tableName + ": "
				+ message);
	}

	public static void info(String documentID, String message, Throwable e) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + documentID + ": " + message, e);
	}

	public static void info(String documentID, String message) {
		com.allen_sauer.gwt.log.client.Log.info(getServletMethodName() + documentID + ": " + message);
	}

	// helper method
	private static String getServletMethodName() {
		StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
		if (stackTraces.length > 3 && "org.glom.web.server.OnlineGlomServiceImpl".equals(stackTraces[3].getClassName()))
			return stackTraces[3].getMethodName() + " - ";
		return "";
	}

}
