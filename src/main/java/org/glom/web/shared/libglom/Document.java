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

package org.glom.web.shared.libglom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.StringVector;
import org.glom.web.shared.layout.LayoutItemField;
import org.jfree.util.Log;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public class Document {

	
	private class TableInfo extends Translatable {
		public String name = "";
		public boolean isDefault;
		public boolean isHidden;

		private final Hashtable<String, Field> fieldsMap = new Hashtable<String, Field>();
		private final Hashtable<String, Report> reportsMap = new Hashtable<String, Report>();
	};

	private String fileURI = "";
	private org.w3c.dom.Document xmlDocument = null;
	
	private Translatable databaseTitle = new Translatable();
	private String connectionServer = "";
	private String connectionDatabase = "";
	private int connectionPort = 0;
	private final Hashtable<String, TableInfo> tablesMap = new Hashtable<String, TableInfo>();
	
	private static String NODE_CONNECTION = "connection";
	private static String ATTRIBUTE_CONNECTION_SERVER = "server";
	private static String ATTRIBUTE_CONNECTION_DATABASE = "database";
	private static String ATTRIBUTE_CONNECTION_PORT = "port";
	private static String NODE_TABLE = "table";
	private static String ATTRIBUTE_NAME = "name";
	private static String ATTRIBUTE_TITLE = "title";
	private static String ATTRIBUTE_DEFAULT = "default";
	private static String ATTRIBUTE_HIDDEN = "hidden";
	private static String NODE_TRANSLATIONS_SET = "trans_set";
	private static String NODE_TRANSLATIONS = "trans";
	private static String ATTRIBUTE_TRANSLATION_LOCALE = "loc";
	private static String ATTRIBUTE_TRANSLATION_TITLE = "val";
	private static String NODE_REPORTS = "reports";
	private static String NODE_REPORT = "report";
	private static String NODE_FIELDS = "fields";
	private static String NODE_FIELD = "field";
	private static String ATTRIBUTE_PRIMARY_KEY = "primary_key";
	private static String ATTRIBUTE_FIELD_TYPE = "type";
	
	public void set_file_uri(final String fileURI) {
		this.fileURI = fileURI;
	}
	
	public String get_file_uri() {
		return fileURI;
	}

	//TODO: Make sure these have the correct values.
	public enum LoadFailureCodes {
		LOAD_FAILURE_CODE_NONE,
	    LOAD_FAILURE_CODE_NOT_FOUND,
		LOAD_FAILURE_CODE_FILE_VERSION_TOO_NEW
	};

	    
	public boolean load(int failure_code) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		try {
			xmlDocument = documentBuilder.parse(fileURI);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		final Element rootNode = xmlDocument.getDocumentElement();
		if(rootNode.getNodeName() != "glom_document") {
			Log.error("Unexpected XML root node name found: " + rootNode.getNodeName());
			return false;
		}
		
		databaseTitle.set_title_original( rootNode.getAttribute(ATTRIBUTE_TITLE) );
		
		final NodeList listTableNodes = rootNode.getElementsByTagName(NODE_TABLE);
		final int num = listTableNodes.getLength();
		for(int i = 0; i < num; i++) {
			final Node node = listTableNodes.item(i);
			final Element element = (Element)node; //TODO: Check the cast.
			loadTableNode(element);
		}
		
		Element nodeConnection = getElementByName(rootNode, NODE_CONNECTION);
		if(nodeConnection != null) {
			connectionServer = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_SERVER);
			connectionDatabase = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_DATABASE);
			
			final String strPort = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_PORT);
			int port = 0;
			if(!StringUtils.isEmpty(strPort)) {
				port = Integer.valueOf(strPort);
			}
			connectionPort = port;
		}
		

		return true;
	};
	
	private Element getElementByName(final Element parentElement, final String tagName) {
		final NodeList listNodes = parentElement.getElementsByTagName(NODE_TABLE);
		if(listNodes == null)
			return null;

		if(listNodes.getLength() == 0)
			return null;

		return (Element)listNodes.item(0);
	}
	
	private boolean getAttributeAsBoolean(final Element node, final String attributeName) {
		final String str = node.getAttribute(attributeName);
		if(str == null)
			return false;
		
		return (str.equals("true"));
	}
	
	/** Load a title and its translations.
	 * 
	 * @param node The XML Element that may contain a title attribute and a trans_set of translations of the title.
	 * @param title
	 */
	private void loadTitle(final Element node, final Translatable title) {
		title.set_title_original(node.getAttribute(ATTRIBUTE_TITLE));
		
		final Element nodeSet = getElementByName(node, NODE_TRANSLATIONS_SET);
		if(nodeSet == null) {
			return;
		}
		
		final NodeList listNodes = nodeSet.getElementsByTagName(NODE_TRANSLATIONS);
		if(listNodes == null)
			return;

		final int num = listNodes.getLength();
		for(int i = 0; i < num; i++) {
			final Node transNode = listNodes.item(i);
			final Element element = (Element)transNode; //TODO: Check the cast.
			
			final String locale = element.getAttribute(ATTRIBUTE_TRANSLATION_LOCALE);
			final String translatedTitle = element.getAttribute(ATTRIBUTE_TRANSLATION_TITLE);
			if(!StringUtils.isEmpty(locale) && !StringUtils.isEmpty(translatedTitle)) {
				title.translationsMap.put(locale, translatedTitle);
			}
		}
	}
	/**
	 * @param node
	 */
	private void loadTableNode(final Element node) {
		TableInfo info = new TableInfo();
		info.name = node.getAttribute(ATTRIBUTE_NAME);
		loadTitle(node, info);
		info.isDefault = getAttributeAsBoolean(node, ATTRIBUTE_DEFAULT);
		info.isHidden = getAttributeAsBoolean(node, ATTRIBUTE_HIDDEN);

		final Element fieldsNode = getElementByName(node, NODE_FIELDS);
		if(fieldsNode != null) {
			final NodeList listFieldNodes = fieldsNode.getElementsByTagName(NODE_FIELD);
			final int numFields = listFieldNodes.getLength();
			for(int i = 0; i < numFields; i++) {
				final Node fieldNode = listFieldNodes.item(i);
				final Element element = (Element)fieldNode; //TODO: Check the cast.
				Field field = new Field();
				loadField(element, field);

				info.fieldsMap.put(field.get_name(), field);
			}
		}

		final Element reportsNode = getElementByName(node, NODE_REPORTS);
		if(reportsNode != null) {
			final NodeList listReportNodes = reportsNode.getElementsByTagName(NODE_REPORT);
			final int numReports = listReportNodes.getLength();
			for(int i = 0; i < numReports; i++) {
				final Node reportNode = listReportNodes.item(i);
				final Element element = (Element)reportNode; //TODO: Check the cast.
				Report report = new Report();
				loadReport(element, report);

				info.reportsMap.put(report.get_name(), report);
			}
		}
		
		tablesMap.put(info.name, info);
	}

	/**
	 * @param element
	 * @param field
	 */
	private void loadField(Element element, Field field) {
		field.set_name(element.getAttribute(ATTRIBUTE_NAME));
		
		Field.glom_field_type fieldType = Field.glom_field_type.TYPE_INVALID;
		final String fieldTypeStr = element.getAttribute(ATTRIBUTE_FIELD_TYPE);
		if(!StringUtils.isEmpty(fieldTypeStr)) {
			if(fieldTypeStr == "boolean") {
				fieldType = Field.glom_field_type.TYPE_BOOLEAN;
			} else if (fieldTypeStr == "date") {
				fieldType = Field.glom_field_type.TYPE_DATE;
			} else if (fieldTypeStr == "image") {
				fieldType = Field.glom_field_type.TYPE_IMAGE;
			} else if (fieldTypeStr == "numeric") {
				fieldType = Field.glom_field_type.TYPE_NUMERIC;
			} else if (fieldTypeStr == "text") {
				fieldType = Field.glom_field_type.TYPE_TEXT;
			} else if (fieldTypeStr == "time") {
				fieldType = Field.glom_field_type.TYPE_TIME;
			}
		}
			
		field.set_glom_field_type(fieldType);
		
		field.set_primary_key(getAttributeAsBoolean(element, ATTRIBUTE_PRIMARY_KEY));
		loadTitle(element, field);
	}

	/**
	 * @param element
	 * @param reportNode
	 */
	private void loadReport(Element element, Report report) {
		report.set_name(element.getAttribute(ATTRIBUTE_NAME));
		loadTitle(element, report);
	}
	
	private TableInfo getTableInfo(final String tableName) {
		return tablesMap.get(tableName);
	}

	public enum HostingMode {
	    HOSTING_MODE_POSTGRES_CENTRAL,
	    HOSTING_MODE_POSTGRES_SELF,
	    HOSTING_MODE_SQLITE
	};
	
	public String get_database_title(final String locale) {
		return databaseTitle.get_title(locale);
	}
	
	public String get_database_title_original() {
		return databaseTitle.get_title_original();
	}
	
	public StringVector get_translation_available_locales() {
		return new StringVector();
	}
	
	public Document.HostingMode get_hosting_mode() {
		return HostingMode.HOSTING_MODE_POSTGRES_CENTRAL;
	}
	
	public String get_connection_server() {
		return connectionServer;
	}
	
	public long get_connection_port() {
		return connectionPort;
	}
	
	public String get_connection_database() {
		return connectionDatabase;
	}
	
	public StringVector get_table_names() {
		StringVector result = new StringVector(); //TODO: Use a normal type.
		
		//return tablesMap.keySet();
		
		for (final TableInfo info : tablesMap.values()) {
			result.add(info.name);
		}
		
		return result;
	}
	
	public boolean get_table_is_hidden(final String table_name) {
		for (final TableInfo info : tablesMap.values()) {
			return info.isHidden;
		}
		
		return false;
	}
			
	public String get_table_title(final String tableName, final String locale) {
		final TableInfo info = getTableInfo(tableName);
		if(info == null) {
			return "";
		}
		
		return info.get_title(locale);
	}
	
	public String get_default_table() {
		for (final TableInfo info : tablesMap.values()) {
			if(info.isDefault) {
				return info.name;
			}
		}
		
		return "";
	}
	
	public boolean get_table_is_known(String tableName) {
		final TableInfo info = getTableInfo(tableName);
		if(info == null) {
			return false;
		}
		
		return true;
	}
	
	public List<Field> get_table_fields(final String tableName) {
		final TableInfo info = getTableInfo(tableName);
		if(info == null)
			return null;

		return new ArrayList<Field>(info.fieldsMap.values());
	}
	
	public Field get_field(String table_name, String strFieldName) {
		//TODO:
		return new Field();
	}
	
	public LayoutGroupVector get_data_layout_groups(String layout_name, String parent_table_name) {
		//TODO:
		return new LayoutGroupVector();
	}
	
	public StringVector get_report_names(String tableName) {
		StringVector result = new StringVector();

		final TableInfo info = getTableInfo(tableName);
		if(info == null)
			return result;

		for (final Report report : info.reportsMap.values()) {
			result.add(report.get_name());
		}

		return new StringVector();
	}
	
	public Report get_report(String tableName, String reportName) {
		final TableInfo info = getTableInfo(tableName);
		if(info == null)
			return null;

		return info.reportsMap.get(reportName);
	}
}
