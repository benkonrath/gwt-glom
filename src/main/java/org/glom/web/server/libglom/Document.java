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

package org.glom.web.server.libglom;

//import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
//import java.io.InputStream;
//import java.net.URLConnection;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.Log;
import org.glom.web.server.Utils;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.libglom.CustomTitle;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.Field.GlomFieldType;
import org.glom.web.shared.libglom.NumericFormat;
import org.glom.web.shared.libglom.Relationship;
import org.glom.web.shared.libglom.Report;
import org.glom.web.shared.libglom.Translatable;
import org.glom.web.shared.libglom.layout.Formatting;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemImage;
import org.glom.web.shared.libglom.layout.LayoutItemNotebook;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;
import org.glom.web.shared.libglom.layout.LayoutItemPortal.NavigationType;
import org.glom.web.shared.libglom.layout.LayoutItemText;
import org.glom.web.shared.libglom.layout.StaticText;
import org.glom.web.shared.libglom.layout.TableToViewDetails;
import org.glom.web.shared.libglom.layout.UsesRelationship;
import org.glom.web.shared.libglom.layout.UsesRelationshipImpl;
import org.glom.web.shared.libglom.layout.reportparts.LayoutItemGroupBy;
import org.jooq.SQLDialect;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.io.Files;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 * 
 */
public class Document {

	@SuppressWarnings("serial")
	private static class TableInfo extends Translatable {
		private boolean isDefault;
		private boolean isHidden;

		private final Hashtable<String, Field> fieldsMap = new Hashtable<String, Field>();
		private final Hashtable<String, Relationship> relationshipsMap = new Hashtable<String, Relationship>();
		private final Hashtable<String, Report> reportsMap = new Hashtable<String, Report>();

		private List<LayoutGroup> layoutGroupsList = new ArrayList<LayoutGroup>();
		private List<LayoutGroup> layoutGroupsDetails = new ArrayList<LayoutGroup>();

		// A list of maps (field name to value).
		private List<Map<String, DataItem>> exampleRows = null;
	}
	
	/** This is passed between methods to keep track of the hierarchy of layout items,
	 * so we can later use it to specify the path to a layout item.
	 */
	private static class Path {
		public String tableName = null;
		public String layoutName = null;
		public int[] indices = new int[1];
	}

	private String fileURI = "";
	private org.w3c.dom.Document xmlDocument = null;

	private final Translatable databaseTitle = new Translatable();
	private String translationOriginalLocale = "";
	private final List<String> translationAvailableLocales = new ArrayList<String>();
	private boolean isExample = false;
	private HostingMode hostingMode = HostingMode.HOSTING_MODE_POSTGRES_CENTRAL;
	private String connectionServer = "";
	private String connectionDatabase = "";
	private int connectionPort = 0;
	private final Hashtable<String, TableInfo> tablesMap = new Hashtable<String, TableInfo>();
	private String documentID = null; //Only for use in the Path, for use in image DataItems.

	private static final String NODE_ROOT = "glom_document";
	private static final String ATTRIBUTE_IS_EXAMPLE = "is_example";
	private static final String ATTRIBUTE_TRANSLATION_ORIGINAL_LOCALE = "translation_original_locale";
	private static final String NODE_CONNECTION = "connection";
	private static final String ATTRIBUTE_CONNECTION_HOSTING_MODE = "hosting_mode";
	private static final String ATTRIBUTE_CONNECTION_SERVER = "server";
	private static final String ATTRIBUTE_CONNECTION_DATABASE = "database";
	private static final String ATTRIBUTE_CONNECTION_PORT = "port";
	private static final String NODE_TABLE = "table";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_TITLE = "title";
	private static final String DEPRECATED_ATTRIBUTE_DATABASE_TITLE = "database_title";
	private static final String ATTRIBUTE_DEFAULT = "default";
	private static final String ATTRIBUTE_HIDDEN = "hidden";
	private static final String NODE_TRANSLATIONS_SET = "trans_set";
	private static final String NODE_TRANSLATIONS = "trans";
	private static final String ATTRIBUTE_TRANSLATION_LOCALE = "loc";
	private static final String ATTRIBUTE_TRANSLATION_TITLE = "val";
	private static final String NODE_REPORTS = "reports";
	private static final String NODE_REPORT = "report";
	private static final String NODE_FIELDS = "fields";
	private static final String NODE_FIELD = "field";
	private static final String NODE_EXAMPLE_ROWS = "example_rows";
	private static final String NODE_EXAMPLE_ROW = "example_row";
	private static final String NODE_VALUE = "value";
	private static final String ATTRIBUTE_COLUMN = "column";
	private static final String ATTRIBUTE_PRIMARY_KEY = "primary_key";
	private static final String ATTRIBUTE_FIELD_TYPE = "type";
	private static final String NODE_FORMATTING = "formatting";
	// private static final String ATTRIBUTE_TEXT_FORMAT_MULTILINE = "format_text_multiline";
	private static final String ATTRIBUTE_USE_THOUSANDS_SEPARATOR = "format_thousands_separator";
	private static final String ATTRIBUTE_DECIMAL_PLACES = "format_decimal_places";
	private static final String NODE_RELATIONSHIPS = "relationships";
	private static final String NODE_RELATIONSHIP = "relationship";
	private static final String ATTRIBUTE_RELATIONSHIP_FROM_FIELD = "key";
	private static final String ATTRIBUTE_RELATIONSHIP_TO_TABLE = "other_table";
	private static final String ATTRIBUTE_RELATIONSHIP_TO_FIELD = "other_key";
	private static final String NODE_DATA_LAYOUTS = "data_layouts";
	private static final String NODE_DATA_LAYOUT = "data_layout";
	private static final String NODE_DATA_LAYOUT_GROUPS = "data_layout_groups";
	private static final String NODE_DATA_LAYOUT_GROUP = "data_layout_group";
	private static final String ATTRIBUTE_LAYOUT_GROUP_COLUMNS_COUNT = "columns_count";
	private static final String NODE_DATA_LAYOUT_NOTEBOOK = "data_layout_notebook";
	private static final String NODE_DATA_LAYOUT_PORTAL = "data_layout_portal";
	private static final String NODE_DATA_LAYOUT_PORTAL_NAVIGATIONRELATIONSHIP = "portal_navigation_relationship";
	private static final String ATTRIBUTE_PORTAL_NAVIGATION_TYPE = "navigation_type";
	private static final String ATTRIBUTE_PORTAL_NAVIGATION_TYPE_AUTOMATIC = "automatic";
	private static final String ATTRIBUTE_PORTAL_NAVIGATION_TYPE_SPECIFIC = "specific";
	private static final String ATTRIBUTE_PORTAL_NAVIGATION_TYPE_NONE = "none";
	private static final String ATTRIBUTE_RELATIONSHIP_NAME = "relationship";
	private static final String ATTRIBUTE_RELATED_RELATIONSHIP_NAME = "related_relationship";
	private static final String NODE_DATA_LAYOUT_ITEM = "data_layout_item";
	private static final String NODE_CUSTOM_TITLE = "title_custom";
	private static final String ATTRIBUTE_CUSTOM_TITLE_USE_CUSTOM = "use_custom";
	private static final String NODE_DATA_LAYOUT_TEXTOBJECT = "data_layout_text";
	private static final String NODE_DATA_LAYOUT_TEXTOBJECT_TEXT = "text";
	private static final String NODE_DATA_LAYOUT_IMAGEOBJECT = "data_layout_image";
	private static final String NODE_DATA_LAYOUT_ITEM_GROUPBY = "data_layout_item_groupby";
	private static final String NODE_GROUPBY = "groupby";
	private static final String NODE_SECONDARY_FIELDS = "secondary_fields";
	private static final String ATTRIBUTE_USE_DEFAULT_FORMATTING = "use_default_formatting";
	public static final String LAYOUT_NAME_DETAILS = "details";
	public static final String LAYOUT_NAME_LIST = "list";
	private static final String QUOTE_FOR_FILE_FORMAT = "\"";
	
	private static final String ATTRIBUTE_CONNECTION_HOSTING_POSTGRES_CENTRAL = "postgres_central";
	private static final String ATTRIBUTE_CONNECTION_HOSTING_POSTGRES_SELF = "postgres_self";
	private static final String ATTRIBUTE_CONNECTION_HOSTING_MYSQL_CENTRAL = "mysql_central";
	private static final String ATTRIBUTE_CONNECTION_HOSTING_MYSQL_SELF = "mysql_self";
	private static final String ATTRIBUTE_CONNECTION_HOSTING_SQLITE = "sqlite";

	/**
	 * Instantiate a Document with no documentID,
	 * meaning that its LayoutItemImage items will not be able to provide a URI to request their data.
	 * This constructor is useful for tests.
	 */
	public Document() {
	}
	
	/**
	 * Instantiate a Document.
	 *
	 * @param documentID Used by LayoutItemImage items to provide a URI to request their data.
	 */
	public Document(final String documentID) {
		this.documentID = documentID;
	}

	public void setFileURI(final String fileURI) {
		this.fileURI = fileURI;
	}

	public String getFileURI() {
		return fileURI;
	}

	// TODO: Make sure these have the correct values.
	public enum LoadFailureCodes {
		LOAD_FAILURE_CODE_NONE, LOAD_FAILURE_CODE_NOT_FOUND, LOAD_FAILURE_CODE_FILE_VERSION_TOO_NEW
	};

	public boolean load() {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = dbf.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			xmlDocument = documentBuilder.parse(fileURI);
		} catch (final SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		final Element rootNode = xmlDocument.getDocumentElement();
		if (rootNode.getNodeName() != NODE_ROOT) {
			Log.error("Unexpected XML root node name found: " + rootNode.getNodeName());
			return false;
		}

		//Get the database title, falling back to the deprecated XML format for it:
		//TODO: load() show complain (via an enum result) if the document format version is less than 7.
		final String databaseTitleStr = rootNode.getAttribute(ATTRIBUTE_TITLE);
		final String deprecatedDatabaseTitleStr = rootNode.getAttribute(DEPRECATED_ATTRIBUTE_DATABASE_TITLE);
		if(!StringUtils.isEmpty(databaseTitleStr)) {
			databaseTitle.setTitleOriginal(databaseTitleStr);
		} else {
			databaseTitle.setTitleOriginal(deprecatedDatabaseTitleStr);
		}
		loadTitle(rootNode, databaseTitle);

		translationOriginalLocale = rootNode.getAttribute(ATTRIBUTE_TRANSLATION_ORIGINAL_LOCALE);
		translationAvailableLocales.add(translationOriginalLocale); // Just a cache.

		isExample = getAttributeAsBoolean(rootNode, ATTRIBUTE_IS_EXAMPLE);

		final Element nodeConnection = getElementByName(rootNode, NODE_CONNECTION);
		if (nodeConnection != null) {
			final String strHostingMode = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_HOSTING_MODE);
			if (strHostingMode.equals(ATTRIBUTE_CONNECTION_HOSTING_POSTGRES_CENTRAL)) {
				hostingMode = HostingMode.HOSTING_MODE_POSTGRES_CENTRAL;
			} else if (strHostingMode.equals(ATTRIBUTE_CONNECTION_HOSTING_POSTGRES_SELF)) {
				hostingMode = HostingMode.HOSTING_MODE_POSTGRES_SELF;
			} else if (strHostingMode.equals(ATTRIBUTE_CONNECTION_HOSTING_MYSQL_CENTRAL)) {
				hostingMode = HostingMode.HOSTING_MODE_MYSQL_CENTRAL;
			} else if (strHostingMode.equals(ATTRIBUTE_CONNECTION_HOSTING_MYSQL_SELF)) {
				hostingMode = HostingMode.HOSTING_MODE_MYSQL_SELF;
			} else if (strHostingMode.equals(ATTRIBUTE_CONNECTION_HOSTING_SQLITE)) {
				hostingMode = HostingMode.HOSTING_MODE_SQLITE;
			} else {
				hostingMode = HostingMode.HOSTING_MODE_POSTGRES_SELF;
			}

			connectionServer = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_SERVER);
			connectionDatabase = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_DATABASE);
			connectionPort = (int) getAttributeAsDecimal(nodeConnection, ATTRIBUTE_CONNECTION_PORT);
		}

		// We first load the fields, relationships, etc,
		// for all tables:
		final List<Node> listTableNodes = getChildrenByTagName(rootNode, NODE_TABLE);
		for (final Node node : listTableNodes) {
			if (!(node instanceof Element)) {
				continue;
			}

			final Element element = (Element) node;
			final TableInfo info = loadTableNodeBasic(element);
			tablesMap.put(info.getName(), info);
		}

		// We then load the layouts for all tables, because they
		// need the fields and relationships for all tables:
		for (final Node node : listTableNodes) {
			if (!(node instanceof Element)) {
				continue;
			}

			final Element element = (Element) node;
			final String tableName = element.getAttribute(ATTRIBUTE_NAME);

			// We first load the fields, relationships, etc:
			final TableInfo info = getTableInfo(tableName);
			if (info == null) {
				continue;
			}

			// We then load the layouts afterwards, because they
			// need the fields and relationships:
			loadTableLayouts(element, info);

			tablesMap.put(info.getName(), info);
		}

		return true;
	}

	private Element getElementByName(final Element parentElement, final String tagName) {
		final List<Node> listNodes = getChildrenByTagName(parentElement, tagName);
		if (listNodes == null) {
			return null;
		}

		if (listNodes.size() == 0) {
			return null;
		}

		return (Element) listNodes.get(0);
	}

	private boolean getAttributeAsBoolean(final Element node, final String attributeName) {
		final String str = node.getAttribute(attributeName);
		if (str == null) {
			return false;
		}

		return str.equals("true");
	}

	private void setAttributeAsBoolean(final Element node, final String attributeName, final boolean value) {
		final String str = value ? "true" : "false";
		node.setAttribute(attributeName, str);
	}

	private double getAttributeAsDecimal(final Element node, final String attributeName) {
		final String str = node.getAttribute(attributeName);
		if (StringUtils.isEmpty(str)) {
			return 0;
		}

		// TODO: Are we sure that this is locale-independent?
		double value = 0;
		try {
			value = Double.valueOf(str);
		} catch (final NumberFormatException e) {
			// e.printStackTrace();
		}

		return value;
	}

	private String getStringForDecimal(final double value) {
		final NumberFormat format = NumberFormat.getInstance(Locale.US);
		format.setGroupingUsed(false); // TODO: Does this change it system-wide?
		return format.format(value);
	}

	private void setAttributeAsDecimal(final Element node, final String attributeName, final double value) {
		node.setAttribute(attributeName, getStringForDecimal(value));
	}

	/**
	 * Load a title and its translations.
	 * 
	 * @param node
	 *            The XML Element that may contain a title attribute and a trans_set of translations of the title.
	 * @param title
	 */
	private void loadTitle(final Element node, final Translatable title) {
		title.setName(node.getAttribute(ATTRIBUTE_NAME));

		title.setTitleOriginal(node.getAttribute(ATTRIBUTE_TITLE));

		final Element nodeSet = getElementByName(node, NODE_TRANSLATIONS_SET);
		if (nodeSet == null) {
			return;
		}

		final List<Node> listNodes = getChildrenByTagName(nodeSet, NODE_TRANSLATIONS);
		if (listNodes == null) {
			return;
		}

		for (final Node transNode : listNodes) {
			if (!(transNode instanceof Element)) {
				continue;
			}

			final Element element = (Element) transNode;

			final String locale = element.getAttribute(ATTRIBUTE_TRANSLATION_LOCALE);
			final String translatedTitle = element.getAttribute(ATTRIBUTE_TRANSLATION_TITLE);
			if (!StringUtils.isEmpty(locale) && !StringUtils.isEmpty(translatedTitle)) {
				title.setTitle(translatedTitle, locale);

				// Remember any new translation locales in our cached list:
				if (!translationAvailableLocales.contains(locale)) {
					translationAvailableLocales.add(locale);
				}
			}
		}
	}

	private void saveTitle(final org.w3c.dom.Document doc, final Element node, final Translatable title) {
		node.setAttribute(ATTRIBUTE_NAME, title.getName());

		node.setAttribute(ATTRIBUTE_TITLE, title.getTitleOriginal());

		final Element nodeSet = createElement(doc, node, NODE_TRANSLATIONS_SET);

		for (final Entry<String, String> entry : title.getTranslationsMap().entrySet()) {
			final Element element = createElement(doc, nodeSet, NODE_TRANSLATIONS);

			element.setAttribute(ATTRIBUTE_TRANSLATION_LOCALE, entry.getKey());
			element.setAttribute(ATTRIBUTE_TRANSLATION_TITLE, entry.getValue());
		}
	}

	/**
	 * @param tableNode
	 * @return
	 */
	private TableInfo loadTableNodeBasic(final Element tableNode) {
		final TableInfo info = new TableInfo();
		loadTitle(tableNode, info);
		final String tableName = info.getName();

		info.isDefault = getAttributeAsBoolean(tableNode, ATTRIBUTE_DEFAULT);
		info.isHidden = getAttributeAsBoolean(tableNode, ATTRIBUTE_HIDDEN);

		// These should be loaded before the fields, because the fields use them.
		final Element relationshipsNode = getElementByName(tableNode, NODE_RELATIONSHIPS);
		if (relationshipsNode != null) {
			final List<Node> listNodes = getChildrenByTagName(relationshipsNode, NODE_RELATIONSHIP);
			for (final Node node : listNodes) {
				if (!(node instanceof Element)) {
					continue;
				}

				final Element element = (Element) node;
				final Relationship relationship = new Relationship();
				loadTitle(element, relationship);
				relationship.setFromTable(tableName);
				relationship.setFromField(element.getAttribute(ATTRIBUTE_RELATIONSHIP_FROM_FIELD));
				relationship.setToTable(element.getAttribute(ATTRIBUTE_RELATIONSHIP_TO_TABLE));
				relationship.setToField(element.getAttribute(ATTRIBUTE_RELATIONSHIP_TO_FIELD));

				info.relationshipsMap.put(relationship.getName(), relationship);
			}
		}

		final Element fieldsNode = getElementByName(tableNode, NODE_FIELDS);
		if (fieldsNode != null) {
			final List<Node> listNodes = getChildrenByTagName(fieldsNode, NODE_FIELD);
			for (final Node node : listNodes) {
				if (!(node instanceof Element)) {
					continue;
				}

				final Element element = (Element) node;
				final Field field = new Field();
				loadField(element, field);

				info.fieldsMap.put(field.getName(), field);
			}
		}

		// We do not normally use this,
		// though we do use it during testing, in SelfHosterPostgreSQL, to recreate the database data.
		final Element exampleRowsNode = getElementByName(tableNode, NODE_EXAMPLE_ROWS);
		if (exampleRowsNode != null) {

			final List<Map<String, DataItem>> exampleRows = new ArrayList<Map<String, DataItem>>();
			final List<Node> listNodes = getChildrenByTagName(exampleRowsNode, NODE_EXAMPLE_ROW);
			for (final Node node : listNodes) {
				if (!(node instanceof Element)) {
					continue;
				}

				final Element element = (Element) node;
				final Map<String, DataItem> row = new HashMap<String, DataItem>();

				final List<Node> listNodesValues = getChildrenByTagName(element, NODE_VALUE);
				for (final Node nodeValue : listNodesValues) {
					if (!(nodeValue instanceof Element)) {
						continue;
					}

					final Element elementValue = (Element) nodeValue;
					final String fieldName = elementValue.getAttribute(ATTRIBUTE_COLUMN);
					if (StringUtils.isEmpty(fieldName)) {
						continue;
					}

					DataItem value = null;
					final Field field = info.fieldsMap.get(fieldName);
					if (field != null) {
						value = getNodeTextChildAsValue(elementValue, field.getGlomType());
					}
					row.put(fieldName, value);
				}

				exampleRows.add(row);
			}

			info.exampleRows = exampleRows;
		}

		return info;
	}

	/**
	 * @param elementValue
	 * @param glomType
	 * @return
	 */
	private DataItem getNodeTextChildAsValue(final Element element, final GlomFieldType type) {
		final DataItem result = new DataItem();

		final String str = element.getTextContent();

		// Unescape "" to ", because to_file_format() escaped ", as specified by the CSV RFC:
		String unescaped = "";
		if (type == GlomFieldType.TYPE_IMAGE) {
			unescaped = str; // binary data does not have quote characters so we do not bother to escape or unescape it.
		} else {
			unescaped = str.replace(QUOTE_FOR_FILE_FORMAT + QUOTE_FOR_FILE_FORMAT, QUOTE_FOR_FILE_FORMAT);
		}

		switch (type) {
		case TYPE_BOOLEAN: {
			final boolean value = (unescaped.equals("true"));
			result.setBoolean(value);
			break;
		}
		case TYPE_DATE: {
			final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ROOT);
			Date value = null;
			try {
				value = dateFormat.parse(unescaped);
			} catch (final ParseException e) {
				// e.printStackTrace();
			}
			result.setDate(value);
			break;
		}
		case TYPE_IMAGE: {
			//Glom (at least since 2.23/24) uses base64 for the images:
			
			//This is only used on the server-side,
			//either to create a database, during tests,
			//or to return the full data from our OnlineGlomImage service.
			//It is removed before being passed to the client-side.
			
			/* This does not seem to work with the text from g_base64_encode() that Glom uses,
			 * maybe because of the newlines, which are apparently OK:
			 * http://en.wikipedia.org/wiki/Base64#MIME
			 * final byte[] bytes = com.google.gwt.user.server.Base64Utils.fromBase64(unescaped);
			 */
			
			/* Use org.apache.commons.codec.binary.Base64: */
			final Base64 decoder = new Base64();
			byte[] bytes = (byte[]) decoder.decode(unescaped.getBytes());

			result.setImageData(bytes);

			break;
		}
		case TYPE_NUMERIC: {
			double value = 0;
			try {
				value = Double.valueOf(unescaped);
			} catch (final NumberFormatException e) {
				// e.printStackTrace();
			}

			result.setNumber(value);
			break;
		}
		case TYPE_TEXT:
			result.setText(unescaped);
			break;
		case TYPE_TIME:
			// TODO
			break;
		default:
			Log.error(documentID, "getNodeTextChildAsValue(): unexpected or invalid field type.");
			break;
		}

		return result;
	}

	private void setNodeTextChildAsValue(final Element element, final DataItem value, final GlomFieldType type) {
		String str = "";

		switch (type) {
		case TYPE_BOOLEAN: {
			str = value.getBoolean() ? "true" : "false";
			break;
		}
		case TYPE_DATE: {
			// TODO: This is not really the format used by the Glom document:
			final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ROOT);
			str = dateFormat.format(value.getDate());
			break;
		}
		case TYPE_IMAGE: {
			str = ""; // TODO
			break;
		}
		case TYPE_NUMERIC: {
			str = getStringForDecimal(value.getNumber());
			break;
		}
		case TYPE_TEXT:
			str = value.getText();
			break;
		case TYPE_TIME:
			str = ""; // TODO
			break;
		default:
			Log.error(documentID, "setNodeTextChildAsValue(): unexpected or invalid field type.");
			break;
		}

		final String escaped = str.replace(QUOTE_FOR_FILE_FORMAT, QUOTE_FOR_FILE_FORMAT + QUOTE_FOR_FILE_FORMAT);
		element.setTextContent(escaped);
	}

	private void saveTableNodeBasic(final org.w3c.dom.Document doc, final Element tableNode, final TableInfo info) {
		saveTitle(doc, tableNode, info);

		setAttributeAsBoolean(tableNode, ATTRIBUTE_DEFAULT, info.isDefault);
		setAttributeAsBoolean(tableNode, ATTRIBUTE_HIDDEN, info.isHidden);

		final Element relationshipsNode = createElement(doc, tableNode, NODE_RELATIONSHIPS);
		for (final Relationship relationship : info.relationshipsMap.values()) {
			final Element element = createElement(doc, relationshipsNode, NODE_RELATIONSHIP);
			saveTitle(doc, element, relationship);

			element.setAttribute(ATTRIBUTE_RELATIONSHIP_FROM_FIELD, relationship.getFromField());
			element.setAttribute(ATTRIBUTE_RELATIONSHIP_TO_TABLE, relationship.getToTable());
			element.setAttribute(ATTRIBUTE_RELATIONSHIP_TO_FIELD, relationship.getToField());
		}

		final Element fieldsNode = createElement(doc, tableNode, NODE_FIELDS);
		for (final Field field : info.fieldsMap.values()) {
			final Element element = createElement(doc, fieldsNode, NODE_FIELD);
			saveField(doc, element, field);
		}

		final Element exampleRowsNode = createElement(doc, tableNode, NODE_EXAMPLE_ROWS);

		for (final Map<String, DataItem> row : info.exampleRows) {
			final Element node = createElement(doc, exampleRowsNode, NODE_EXAMPLE_ROW);

			// TODO: This assumes that fieldsMap.values() will have the same sequence as the values,
			final int i = 0;
			for (final Field field : info.fieldsMap.values()) {
				if (i < row.size()) {
					break;
				}

				final String fieldName = field.getName();
				if (StringUtils.isEmpty(fieldName)) {
					continue;
				}

				final DataItem dataItem = row.get(fieldName);
				if (dataItem == null) {
					continue;
				}

				final Element elementValue = createElement(doc, node, NODE_VALUE);
				elementValue.setAttribute(ATTRIBUTE_COLUMN, fieldName);
				setNodeTextChildAsValue(elementValue, dataItem, field.getGlomType());
			}
		}
	}

	/**
	 * @param doc
	 * @param element
	 * @param field
	 */
	private void saveField(final org.w3c.dom.Document doc, final Element element, final Field field) {
		saveTitle(doc, element, field);

		String fieldTypeStr = "";

		switch (field.getGlomType()) {
		case TYPE_BOOLEAN:
			fieldTypeStr = "Boolean";
			break;
		case TYPE_DATE:
			fieldTypeStr = "Date";
			break;
		case TYPE_IMAGE:
			fieldTypeStr = "Image";
			break;
		case TYPE_NUMERIC:
			fieldTypeStr = "Number";
			break;
		case TYPE_TEXT:
			fieldTypeStr = "Text";
			break;
		case TYPE_TIME:
			fieldTypeStr = "Time";
			break;
		default:
			break;
		}
		element.setAttribute(ATTRIBUTE_FIELD_TYPE, fieldTypeStr);

		setAttributeAsBoolean(element, ATTRIBUTE_PRIMARY_KEY, field.getPrimaryKey());

		final Element elementFormatting = createElement(doc, element, NODE_FORMATTING);
		saveFormatting(elementFormatting, field.getFormatting());
	}

	/**
	 * @param elementFormatting
	 * @param formatting
	 */
	private void saveFormatting(final Element element, final Formatting formatting) {
		// formatting.setTextFormatMultiline(getAttributeAsBoolean(elementFormatting, ATTRIBUTE_TEXT_FORMAT_MULTILINE));

		final NumericFormat numericFormatting = formatting.getNumericFormat();
		if (numericFormatting != null) {
			setAttributeAsBoolean(element, ATTRIBUTE_USE_THOUSANDS_SEPARATOR,
					numericFormatting.getUseThousandsSeparator());
			setAttributeAsDecimal(element, ATTRIBUTE_DECIMAL_PLACES, numericFormatting.getDecimalPlaces());
		}
	}

	/**
	 * @param tableNode
	 * @param info
	 */
	private void loadTableLayouts(final Element tableNode, final TableInfo info) {
		final String tableName = info.getName();

		final Element layoutsNode = getElementByName(tableNode, NODE_DATA_LAYOUTS);
		if (layoutsNode != null) {
			final List<Node> listNodes = getChildrenByTagName(layoutsNode, NODE_DATA_LAYOUT);
			for (final Node node : listNodes) {
				if (!(node instanceof Element)) {
					continue;
				}

				final Element element = (Element) node;
				final String name = element.getAttribute(ATTRIBUTE_NAME);
				final List<LayoutGroup> listLayoutGroups = loadLayoutNode(element, tableName, name);
				if (name.equals(LAYOUT_NAME_DETAILS)) {
					info.layoutGroupsDetails = listLayoutGroups;
				} else if (name.equals(LAYOUT_NAME_LIST)) {
					info.layoutGroupsList = listLayoutGroups;
				} else {
					Log.error(documentID, "loadTableNode(): unexpected layout name: " + name);
				}
			}
		}

		final Element reportsNode = getElementByName(tableNode, NODE_REPORTS);
		if (reportsNode != null) {
			final List<Node> listNodes = getChildrenByTagName(reportsNode, NODE_REPORT);
			for (final Node node : listNodes) {
				if (!(node instanceof Element)) {
					continue;
				}

				final Element element = (Element) node;
				final Report report = new Report();
				loadReport(element, report, tableName);

				info.reportsMap.put(report.getName(), report);
			}
		}
	}

	/**
	 * @param node
	 * @return
	 */
	private List<LayoutGroup> loadLayoutNode(final Element node, final String tableName, final String layoutName) {
		if (node == null) {
			return null;
		}

		final List<LayoutGroup> result = new ArrayList<LayoutGroup>();
		int groupIndex = 0;
		final List<Node> listNodes = getChildrenByTagName(node, NODE_DATA_LAYOUT_GROUPS);
		for (final Node nodeGroups : listNodes) {
			if (!(nodeGroups instanceof Element)) {
				continue;
			}

			final Element elementGroups = (Element) nodeGroups;

			final NodeList list = elementGroups.getChildNodes();
			final int num = list.getLength();
			for (int i = 0; i < num; i++) {
				final Node nodeLayoutGroup = list.item(i);
				if (nodeLayoutGroup == null) {
					continue;
				}

				if (!(nodeLayoutGroup instanceof Element)) {
					continue;
				}
				
				final Path path = new Path();
				path.tableName = tableName;
				path.layoutName = layoutName;
				path.indices[0 /* depth */] = groupIndex;
				++groupIndex;

				final Element element = (Element) nodeLayoutGroup;
				final String tagName = element.getTagName();
				if (tagName.equals(NODE_DATA_LAYOUT_GROUP)) {
					final LayoutGroup group = new LayoutGroup();
					loadDataLayoutGroup(element, group, tableName, path);
					result.add(group);
				} else if (tagName.equals(NODE_DATA_LAYOUT_NOTEBOOK)) {
					final LayoutItemNotebook group = new LayoutItemNotebook();
					loadDataLayoutGroup(element, group, tableName, path);
					result.add(group);
				} else if (tagName.equals(NODE_DATA_LAYOUT_PORTAL)) {
					final LayoutItemPortal portal = new LayoutItemPortal();
					loadDataLayoutPortal(element, portal, tableName, path);
					result.add(portal);
				}
			}
		}

		return result;
	}

	/**
	 * @param element
	 * @param tableName
	 * @param portal
	 */
	private void loadUsesRelationship(final Element element, final String tableName, final UsesRelationship item) {
		if (element == null) {
			return;
		}

		if (item == null) {
			return;
		}

		final String relationshipName = element.getAttribute(ATTRIBUTE_RELATIONSHIP_NAME);
		Relationship relationship = null;
		if (!StringUtils.isEmpty(relationshipName)) {
			// std::cout << "  debug in : tableName=" << tableName << ", relationshipName=" << relationship_name <<
			// std::endl;
			relationship = getRelationship(tableName, relationshipName);
			item.setRelationship(relationship);

			if (relationship == null) {
				Log.error("relationship not found: " + relationshipName + ", in table: " + tableName);
			}
		}

		// TODO: Unit test loading of doubly-related fields.
		final String relatedRelationshipName = element.getAttribute(ATTRIBUTE_RELATED_RELATIONSHIP_NAME);
		if (!StringUtils.isEmpty(relatedRelationshipName) && (relationship != null)) {
			final Relationship relatedRelationship = getRelationship(relationship.getToTable(), relatedRelationshipName);
			item.setRelatedRelationship(relatedRelationship);

			if (relatedRelationship == null) {
				Log.error("related relationship not found in table=" + relationship.getToTable() + ",  name="
						+ relatedRelationshipName);
			}
		}
	}

	/**
	 * getElementsByTagName() is recursive, but we do not want that.
	 * 
	 * @param node
	 * @param
	 * @return
	 */
	private List<Node> getChildrenByTagName(final Element parentNode, final String tagName) {
		final List<Node> result = new ArrayList<Node>();

		final NodeList list = parentNode.getElementsByTagName(tagName);
		final int num = list.getLength();
		for (int i = 0; i < num; i++) {
			final Node node = list.item(i);
			if (node == null) {
				continue;
			}

			final Node itemParentNode = node.getParentNode();
			if (itemParentNode.equals(parentNode)) {
				result.add(node);
			}
		}

		return result;
	}

	/**
	 * @param element
	 * @param group
	 */
	private void loadDataLayoutGroup(final Element nodeGroup, final LayoutGroup group, final String tableName, final Path path) {
		loadTitle(nodeGroup, group);

		// Read the column count:
		int columnCount = (int) getAttributeAsDecimal(nodeGroup, ATTRIBUTE_LAYOUT_GROUP_COLUMNS_COUNT);
		if (columnCount < 1) {
			columnCount = 1; // 0 is a useless default.
		}
		group.setColumnCount(columnCount);

		final int depth = path.indices.length;

		// Get the child items:
		final NodeList listNodes = nodeGroup.getChildNodes();
		final int num = listNodes.getLength();
		int pathIndex = 0;
		for (int i = 0; i < num; i++) {

			final Node node = listNodes.item(i);
			if (!(node instanceof Element)) {
				continue;
			}

			final Element element = (Element) node;
			final String tagName = element.getTagName();
			
			//Do not increment pathIndex for an item
			//that we will not use:
			if(tagName.equals(NODE_TRANSLATIONS_SET)) {
				continue;
			}

			// Create a path of indices for the child:
			final Path pathChild = new Path();
			pathChild.tableName = path.tableName;
			pathChild.layoutName = path.layoutName;
			pathChild.indices = new int[path.indices.length + 1];
			System.arraycopy( path.indices, 0, pathChild.indices, 0, path.indices.length );
			pathChild.indices[depth] = pathIndex;
			pathIndex++;

			if (tagName.equals(NODE_DATA_LAYOUT_GROUP)) {
				final LayoutGroup childGroup = new LayoutGroup();
				loadDataLayoutGroup(element, childGroup, tableName, pathChild);
				group.addItem(childGroup);
			} else if (tagName.equals(NODE_DATA_LAYOUT_NOTEBOOK)) {
				final LayoutItemNotebook childGroup = new LayoutItemNotebook();
				loadDataLayoutGroup(element, childGroup, tableName, pathChild);
				group.addItem(childGroup);
			} else if (tagName.equals(NODE_DATA_LAYOUT_PORTAL)) {
				final LayoutItemPortal childGroup = new LayoutItemPortal();
				loadDataLayoutPortal(element, childGroup, tableName, pathChild);
				group.addItem(childGroup);
			} else if (tagName.equals(NODE_DATA_LAYOUT_ITEM)) {
				final LayoutItemField item = new LayoutItemField();
				loadDataLayoutItemField(element, item, tableName);
				group.addItem(item);
			} else if (tagName.equals(NODE_DATA_LAYOUT_TEXTOBJECT)) {
				final LayoutItemText item = new LayoutItemText();
				loadDataLayoutItemText(element, item);
				group.addItem(item);
			} else if (tagName.equals(NODE_DATA_LAYOUT_IMAGEOBJECT)) {
				final LayoutItemImage item = new LayoutItemImage();
				loadDataLayoutItemImage(element, item, pathChild);
				group.addItem(item);
			} else if (tagName.equals(NODE_DATA_LAYOUT_ITEM_GROUPBY)) {
				final LayoutItemGroupBy item = new LayoutItemGroupBy();
				loadDataLayoutItemGroupBy(element, item, tableName, pathChild);
				group.addItem(item);
			}
		}
	}

	/**
	 * @param element
	 * @param item
	 */
	private void loadDataLayoutItemImage(Element element, LayoutItemImage item, final Path path) {
		loadTitle(element, item);
		
		final Element elementValue = getElementByName(element, NODE_VALUE);
		if (elementValue == null) {
			return;
		}

		final DataItem image = getNodeTextChildAsValue(elementValue, Field.GlomFieldType.TYPE_IMAGE);
		
		//This lets the client-side request the full data from our OnlineGlomImage service.
		final String layoutPath = Utils.buildImageDataUrl(documentID, path.tableName, path.layoutName, path.indices);
		image.setImageDataUrl(layoutPath);
		
		item.setImage(image);
	}

	/**
	 * @param element
	 * @param item
	 */
	private void loadDataLayoutItemText(Element element, LayoutItemText item) {
		loadTitle(element, item);
		
		final Element elementText = getElementByName(element, NODE_DATA_LAYOUT_TEXTOBJECT_TEXT);
		if (elementText == null) {
			return;
		}
		
		final StaticText text = new StaticText();
		loadTitle(elementText, text); //This node reuses the title structure to hold its text.
		item.setText(text);
	}

	/**
	 * @param element
	 * @param item
	 * @param tableName
	 */
	private void loadDataLayoutItemGroupBy(final Element element, final LayoutItemGroupBy item, final String tableName, final Path path) {
		loadDataLayoutGroup(element, item, tableName, path);

		final Element elementGroupBy = getElementByName(element, NODE_GROUPBY);
		if (elementGroupBy == null) {
			return;
		}

		final LayoutItemField fieldGroupBy = new LayoutItemField();
		loadDataLayoutItemField(elementGroupBy, fieldGroupBy, tableName);
		item.setFieldGroupBy(fieldGroupBy);

		final Element elementSecondaryFields = getElementByName(element, NODE_SECONDARY_FIELDS);
		if (elementSecondaryFields == null) {
			return;
		}

		final Element elementLayoutGroup = getElementByName(elementSecondaryFields, NODE_DATA_LAYOUT_GROUP);
		if (elementLayoutGroup != null) {
			final LayoutGroup secondaryLayoutGroup = new LayoutGroup();
			loadDataLayoutGroup(elementLayoutGroup, secondaryLayoutGroup, tableName, path); //TODO: Add the main group items count to path first?
			item.setSecondaryFields(secondaryLayoutGroup);
		}
	}

	/**
	 * @param element
	 * @param item
	 */
	private void loadDataLayoutItemField(final Element element, final LayoutItemField item, final String tableName) {
		item.setName(element.getAttribute(ATTRIBUTE_NAME));
		loadUsesRelationship(element, tableName, item);

		final Element elementCustomTitle = getElementByName(element, NODE_CUSTOM_TITLE);
		if (elementCustomTitle != null) {
			final CustomTitle customTitle = item.getCustomTitle();
			customTitle.setUseCustomTitle(getAttributeAsBoolean(elementCustomTitle, ATTRIBUTE_CUSTOM_TITLE_USE_CUSTOM));
			loadTitle(elementCustomTitle, customTitle); // LayoutItemField doesn't use its own title member.
		}

		// Get the actual field:
		final String fieldName = item.getName();
		final String inTableName = item.getTableUsed(tableName);
		final Field field = getField(inTableName, fieldName);
		item.setFullFieldDetails(field);

		item.setUseDefaultFormatting(getAttributeAsBoolean(element, ATTRIBUTE_USE_DEFAULT_FORMATTING));

		final Element elementFormatting = getElementByName(element, NODE_FORMATTING);
		if (elementFormatting != null) {
			loadFormatting(elementFormatting, item.getFormatting());
		}
	}

	/**
	 * @param element
	 * @param childGroup
	 */
	private void loadDataLayoutPortal(final Element element, final LayoutItemPortal portal, final String tableName, final Path path) {
		loadUsesRelationship(element, tableName, portal);
		final String relatedTableName = portal.getTableUsed(tableName);
		loadDataLayoutGroup(element, portal, relatedTableName, path);

		final Element elementNavigation = getElementByName(element, NODE_DATA_LAYOUT_PORTAL_NAVIGATIONRELATIONSHIP);
		if (elementNavigation != null) {
			final String navigationTypeAsString = elementNavigation.getAttribute(ATTRIBUTE_PORTAL_NAVIGATION_TYPE);
			if (StringUtils.isEmpty(navigationTypeAsString)
					|| navigationTypeAsString.equals(ATTRIBUTE_PORTAL_NAVIGATION_TYPE_AUTOMATIC)) {
				portal.setNavigationType(LayoutItemPortal.NavigationType.NAVIGATION_AUTOMATIC);
			} else if (navigationTypeAsString.equals(ATTRIBUTE_PORTAL_NAVIGATION_TYPE_NONE)) {
				portal.setNavigationType(LayoutItemPortal.NavigationType.NAVIGATION_NONE);
			} else if (navigationTypeAsString.equals(ATTRIBUTE_PORTAL_NAVIGATION_TYPE_SPECIFIC)) {
				// Read the specified relationship name:
				final UsesRelationship relationshipNavigationSpecific = new UsesRelationshipImpl();
				loadUsesRelationship(elementNavigation, relatedTableName, relationshipNavigationSpecific);
				portal.setNavigationRelationshipSpecific(relationshipNavigationSpecific);
			}
		}

	}

	/**
	 * @param element
	 * @param field
	 */
	private void loadField(final Element element, final Field field) {
		loadTitle(element, field);

		Field.GlomFieldType fieldType = Field.GlomFieldType.TYPE_INVALID;
		final String fieldTypeStr = element.getAttribute(ATTRIBUTE_FIELD_TYPE);
		if (!StringUtils.isEmpty(fieldTypeStr)) {
			if (fieldTypeStr.equals("Boolean")) {
				fieldType = Field.GlomFieldType.TYPE_BOOLEAN;
			} else if (fieldTypeStr.equals("Date")) {
				fieldType = Field.GlomFieldType.TYPE_DATE;
			} else if (fieldTypeStr.equals("Image")) {
				fieldType = Field.GlomFieldType.TYPE_IMAGE;
			} else if (fieldTypeStr.equals("Number")) {
				fieldType = Field.GlomFieldType.TYPE_NUMERIC;
			} else if (fieldTypeStr.equals("Text")) {
				fieldType = Field.GlomFieldType.TYPE_TEXT;
			} else if (fieldTypeStr.equals("Time")) {
				fieldType = Field.GlomFieldType.TYPE_TIME;
			}
		}

		field.setGlomFieldType(fieldType);

		field.setPrimaryKey(getAttributeAsBoolean(element, ATTRIBUTE_PRIMARY_KEY));

		final Element elementFormatting = getElementByName(element, NODE_FORMATTING);
		if (elementFormatting != null) {
			loadFormatting(elementFormatting, field.getFormatting());
		}
	}

	/**
	 * @param elementFormatting
	 * @param formatting
	 */
	private void loadFormatting(final Element elementFormatting, final Formatting formatting) {
		if (elementFormatting == null) {
			return;
		}

		if (formatting == null) {
			return;
		}

		// formatting.setTextFormatMultiline(getAttributeAsBoolean(elementFormatting, ATTRIBUTE_TEXT_FORMAT_MULTILINE));

		final NumericFormat numericFormatting = formatting.getNumericFormat();
		if (numericFormatting != null) {
			numericFormatting.setUseThousandsSeparator(getAttributeAsBoolean(elementFormatting,
					ATTRIBUTE_USE_THOUSANDS_SEPARATOR));
			numericFormatting
					.setDecimalPlaces((int) getAttributeAsDecimal(elementFormatting, ATTRIBUTE_DECIMAL_PLACES));
		}

	}

	/**
	 * @param element
	 * @param reportNode
	 */
	private void loadReport(final Element element, final Report report, final String tableName) {
		report.setName(element.getAttribute(ATTRIBUTE_NAME));
		loadTitle(element, report);

		final List<LayoutGroup> listLayoutGroups = loadLayoutNode(element, tableName, null /* not needed */);

		// A report can actually only have one LayoutGroup,
		// though it uses the same XML structure as List and Details layouts,
		// which (wrongly) suggests that it can have more than one group.
		LayoutGroup layoutGroup = null;
		if (!listLayoutGroups.isEmpty()) {
			layoutGroup = listLayoutGroups.get(0);
		}

		report.setLayoutGroup(layoutGroup);
	}

	private TableInfo getTableInfo(final String tableName) {
		return tablesMap.get(tableName);
	}

	public enum HostingMode {
		HOSTING_MODE_POSTGRES_CENTRAL, HOSTING_MODE_POSTGRES_SELF, HOSTING_MODE_SQLITE, HOSTING_MODE_MYSQL_CENTRAL, HOSTING_MODE_MYSQL_SELF
	};

	public String getDatabaseTitle(final String locale) {
		return databaseTitle.getTitle(locale);
	}

	public String getDatabaseTitleOriginal() {
		return databaseTitle.getTitleOriginal();
	}

	public List<String> getTranslationAvailableLocales() {
		return translationAvailableLocales;
	}

	public Document.HostingMode getHostingMode() {
		return hostingMode;
	}

	/**
	 * @param hostingMode
	 */
	public void setHostingMode(final HostingMode hostingMode) {
		this.hostingMode = hostingMode;
	}

	public String getConnectionServer() {
		return connectionServer;
	}

	public int getConnectionPort() {
		return connectionPort;
	}

	public void setConnectionPort(final int port) {
		connectionPort = port;
	}

	public String getConnectionDatabase() {
		return connectionDatabase;
	}

	public List<String> getTableNames() {
		// TODO: Return a Set?
		return new ArrayList<String>(tablesMap.keySet());
	}

	public boolean getTableIsHidden(final String tableName) {
		final TableInfo info = getTableInfo(tableName);
		if (info == null) {
			return false;
		}

		return info.isHidden;
	}

	public String getTableTitle(final String tableName, final String locale) {
		final TableInfo info = getTableInfo(tableName);
		if (info == null) {
			return "";
		}

		return info.getTitle(locale);
	}

	public List<Map<String, DataItem>> getExampleRows(final String tableName) {
		final TableInfo info = getTableInfo(tableName);
		if (info == null) {
			return null;
		}

		return info.exampleRows;
	}

	public String getDefaultTable() {
		for (final TableInfo info : tablesMap.values()) {
			if (info.isDefault) {
				return info.getName();
			}
		}

		return "";
	}

	public boolean getTableIsKnown(final String tableName) {
		final TableInfo info = getTableInfo(tableName);
		if (info == null) {
			return false;
		}

		return true;
	}

	public List<Field> getTableFields(final String tableName) {
		final TableInfo info = getTableInfo(tableName);
		if (info == null) {
			return null;
		}

		return new ArrayList<Field>(info.fieldsMap.values());
	}

	public Field getField(final String tableName, final String strFieldName) {
		final TableInfo info = getTableInfo(tableName);
		if (info == null) {
			return null;
		}

		return info.fieldsMap.get(strFieldName);
	}

	public List<LayoutGroup> getDataLayoutGroups(final String layoutName, final String parentTableName) {
		final TableInfo info = getTableInfo(parentTableName);
		if (info == null) {
			return new ArrayList<LayoutGroup>();
		}

		if (layoutName.equals(LAYOUT_NAME_DETAILS)) {
			return info.layoutGroupsDetails;
		} else if (layoutName.equals(LAYOUT_NAME_LIST)) {
			return info.layoutGroupsList;
		} else {
			return new ArrayList<LayoutGroup>();
		}
	}

	public List<String> getReportNames(final String tableName) {
		final TableInfo info = getTableInfo(tableName);
		if (info == null) {
			return new ArrayList<String>();
		}

		return new ArrayList<String>(info.reportsMap.keySet());
	}

	public Report getReport(final String tableName, final String reportName) {
		final TableInfo info = getTableInfo(tableName);
		if (info == null) {
			return null;
		}

		return info.reportsMap.get(reportName);
	}

	/**
	 * @param tableName
	 * @param field
	 * @return
	 */
	public Relationship getFieldUsedInRelationshipToOne(final String tableName, final LayoutItemField layoutField) {

		if (layoutField == null) {
			Log.error("layoutField was null");
			return null;
		}

		Relationship result = null;

		final String tableUsed = layoutField.getTableUsed(tableName);
		final TableInfo info = getTableInfo(tableUsed);
		if (info == null) {
			// This table is special. We would not create a relationship to it using a field:
			// if(tableUsed == GLOM_STANDARD_TABLE_PREFS_TABLE_NAME)
			// return result;

			Log.error("table not found: " + tableUsed);
			return null;
		}

		// Look at each relationship:
		final String fieldName = layoutField.getName();
		for (final Relationship relationship : info.relationshipsMap.values()) {
			if (relationship != null) {
				// If the relationship uses the field
				if (StringUtils.equals(relationship.getFromField(), fieldName)) {
					// if the to_table is not hidden:
					if (!getTableIsHidden(relationship.getToTable())) {
						// TODO_Performance: The use of this convenience method means we get the full relationship
						// information again:
						if (getRelationshipIsToOne(tableName, relationship.getName())) {
							result = relationship;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * @param tableName
	 * @param relationshipName
	 * @return
	 */
	private boolean getRelationshipIsToOne(final String tableName, final String relationshipName) {
		final Relationship relationship = getRelationship(tableName, relationshipName);
		if (relationship != null) {
			final Field fieldTo = getField(relationship.getToTable(), relationship.getToField());
			if (fieldTo != null) {
				return (fieldTo.getPrimaryKey() || fieldTo.getUniqueKey());
			}
		}

		return false;
	}

	/** Get the relationship by name for a table.
	 * 
	 * @param tableName
	 * @param relationshipName
	 * @return
	 */
	public Relationship getRelationship(final String tableName, final String relationshipName) {
		final TableInfo info = getTableInfo(tableName);
		if (info == null) {
			Log.error("table not found: " + tableName);
			return null;
		}

		return info.relationshipsMap.get(relationshipName);
	}

	/**
	 * @param tableName
	 *            Output parameter
	 * @param relationship
	 * @param portal
	 *            TODO
	 */
	public TableToViewDetails getPortalSuitableTableToViewDetails(final LayoutItemPortal portal) {
		UsesRelationship navigationRelationship = null;

		// Check whether a relationship was specified:
		if (portal.getNavigationType() == NavigationType.NAVIGATION_AUTOMATIC) {
			navigationRelationship = getPortalNavigationRelationshipAutomatic(portal);
		} else {
			navigationRelationship = portal.getNavigationRelationshipSpecific();
		}

		// Get the navigation table name from the chosen relationship:
		final String directlyRelatedTableName = portal.getTableUsed("" /* not relevant */);

		// The navigation_table_name (and therefore, the table_name output parameter,
		// as well) stays empty if the navrel type was set to none.
		String navigationTableName = null;
		if (navigationRelationship != null) {
			navigationTableName = navigationRelationship.getTableUsed(directlyRelatedTableName);
		} else if (portal.getNavigationType() != NavigationType.NAVIGATION_NONE) {
			// An empty result from get_portal_navigation_relationship_automatic() or
			// get_navigation_relationship_specific() means we should use the directly related table:
			navigationTableName = directlyRelatedTableName;
		}

		if (StringUtils.isEmpty(navigationTableName)) {
			return null;
		}

		if (this == null) {
			Log.error("document is null.");
			return null;
		}

		if (getTableIsHidden(navigationTableName)) {
			Log.error("navigation_table_name indicates a hidden table: " + navigationTableName);
			return null;
		}

		final TableToViewDetails result = new TableToViewDetails();
		result.tableName = navigationTableName;
		result.usesRelationship = navigationRelationship;
		return result;
	}

	/**
	 * @param portal
	 *            TODO
	 * @return
	 */
	private UsesRelationship getPortalNavigationRelationshipAutomatic(final LayoutItemPortal portal) {
		if (this == null) {
			return null;
		}

		// If the related table is not hidden then we can just navigate to that:
		final String direct_related_table_name = portal.getTableUsed("" /* parent table - not relevant */);
		if (!getTableIsHidden(direct_related_table_name)) {
			// Non-hidden tables can just be shown directly. Navigate to it:
			return null;
		} else {
			// If the related table is hidden,
			// then find a suitable related non-hidden table by finding the first layout field that mentions one:
			final LayoutItemField field = getPortalFieldIsFromNonHiddenRelatedRecord(portal);
			if (field != null) {
				return field; // Returns the UsesRelationship base part. (A relationship belonging to the portal's
								// related table.)
			} else {
				// Instead, find a key field that's used in a relationship,
				// and pretend that we are showing the to field as a related field:
				final Relationship fieldIndentifies = getPortalFieldIdentifiesNonHiddenRelatedRecord(portal);
				if (fieldIndentifies != null) {
					final UsesRelationship result = new UsesRelationshipImpl();
					result.setRelationship(fieldIndentifies);
					return result;
				}
			}
		}

		// There was no suitable related table to show:
		return null;
	}

	/**
	 * @param portal
	 *            TODO
	 * @return
	 */
	private LayoutItemField getPortalFieldIsFromNonHiddenRelatedRecord(final LayoutItemPortal portal) {
		// Find the first field that is from a non-hidden related table.

		if (this == null) {
			return null;
		}

		final LayoutItemField result = null;

		final String parent_table_name = portal.getTableUsed("" /* parent table - not relevant */);

		final List<LayoutItem> items = portal.getItems();
		for (final LayoutItem item : items) {
			if (item instanceof LayoutItemField) {
				final LayoutItemField field = (LayoutItemField) item;
				if (field.getHasRelationshipName()) {
					final String table_name = field.getTableUsed(parent_table_name);
					if (!(getTableIsHidden(table_name))) {
						return field;
					}
				}
			}
		}

		return result;
	}

	/**
	 * @param used_in_relationship
	 * @param portal
	 *            TODO
	 * @return
	 */
	private Relationship getPortalFieldIdentifiesNonHiddenRelatedRecord(final LayoutItemPortal portal) {
		// Find the first field that is from a non-hidden related table.

		if (this == null) {
			Log.error("document is null");
			return null;
		}

		final String parent_table_name = portal.getTableUsed("" /* parent table - not relevant */);

		final List<LayoutItem> items = portal.getItems();
		for (final LayoutItem item : items) {
			if (item instanceof LayoutItemField) {
				final LayoutItemField field = (LayoutItemField) item;
				if (field.getHasRelationshipName()) {
					final Relationship relationship = getFieldUsedInRelationshipToOne(parent_table_name, field);
					if (relationship != null) {
						final String table_name = relationship.getToTable();
						if (!StringUtils.isEmpty(table_name)) {
							if (!(getTableIsHidden(table_name))) {
								return relationship;
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * @param tableName
	 * @param layoutItem
	 * @return The destination table name for navigation.
	 */
	public String getLayoutItemFieldShouldHaveNavigation(final String tableName, final LayoutItemField layoutItem) {
		if (StringUtils.isEmpty(tableName)) {
			return null;
		}

		if (layoutItem == null) {
			return null;
		}

		// Check whether the field controls a relationship,
		// meaning it identifies a record in another table.
		final Relationship fieldUsedInRelationshipToOne = getFieldUsedInRelationshipToOne(tableName, layoutItem);
		if (fieldUsedInRelationshipToOne != null) {
			return fieldUsedInRelationshipToOne.getToTable();
		}

		// Check whether the field identifies a record in another table
		// just because it is a primary key in that table:
		final Field fieldInfo = layoutItem.getFullFieldDetails();
		final boolean fieldIsRelatedPrimaryKey = layoutItem.getHasRelationshipName() && (fieldInfo != null)
				&& fieldInfo.getPrimaryKey();
		if (fieldIsRelatedPrimaryKey) {
			return layoutItem.getRelationship().getToTable();
		}

		return null;
	}

	/**
	 * @param isExample
	 */
	public void setIsExampleFile(final boolean isExample) {
		this.isExample = isExample;
	}

	/**
	 */
	public boolean getIsExampleFile() {
		return isExample;
	}

	/**
	 * @return
	 */
	public boolean save() {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = dbf.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		final org.w3c.dom.Document doc = documentBuilder.newDocument();
		final Element rootNode = doc.createElement(NODE_ROOT);
		doc.appendChild(rootNode);

		rootNode.setAttribute(ATTRIBUTE_TITLE, databaseTitle.getTitleOriginal());
		rootNode.setAttribute(ATTRIBUTE_TRANSLATION_ORIGINAL_LOCALE, translationOriginalLocale);
		setAttributeAsBoolean(rootNode, ATTRIBUTE_IS_EXAMPLE, isExample);

		String strHostingMode = "";
		if (hostingMode == HostingMode.HOSTING_MODE_POSTGRES_CENTRAL) {
			strHostingMode = ATTRIBUTE_CONNECTION_HOSTING_POSTGRES_CENTRAL;
		} else if (hostingMode == HostingMode.HOSTING_MODE_POSTGRES_SELF) {
			strHostingMode = ATTRIBUTE_CONNECTION_HOSTING_POSTGRES_SELF;
		} else if (hostingMode == HostingMode.HOSTING_MODE_MYSQL_CENTRAL) {
			strHostingMode = ATTRIBUTE_CONNECTION_HOSTING_MYSQL_CENTRAL;
		} else if (hostingMode == HostingMode.HOSTING_MODE_MYSQL_SELF) {
			strHostingMode = ATTRIBUTE_CONNECTION_HOSTING_MYSQL_SELF;
		} else if (hostingMode == HostingMode.HOSTING_MODE_SQLITE) {
			strHostingMode = ATTRIBUTE_CONNECTION_HOSTING_SQLITE;
		} else {
			strHostingMode = ATTRIBUTE_CONNECTION_HOSTING_POSTGRES_SELF;
		}
		final Element nodeConnection = createElement(doc, rootNode, NODE_CONNECTION);
		nodeConnection.setAttribute(ATTRIBUTE_CONNECTION_HOSTING_MODE, strHostingMode);
		nodeConnection.setAttribute(ATTRIBUTE_CONNECTION_SERVER, connectionServer);
		nodeConnection.setAttribute(ATTRIBUTE_CONNECTION_DATABASE, connectionDatabase);
		setAttributeAsDecimal(nodeConnection, ATTRIBUTE_CONNECTION_PORT, connectionPort);

		// for all tables:
		for (final TableInfo table : tablesMap.values()) {
			final Element nodeTable = createElement(doc, rootNode, NODE_TABLE);
			saveTableNodeBasic(doc, nodeTable, table);
			saveTableLayouts(doc, nodeTable, table);
		}

		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (final TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		// TODO: This probably distorts text nodes,
		// so careful when we load/save them. For instance, scripts.
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		// Make sure that the parent directory exists:
		final File file = new File(fileURI);
		try {
			Files.createParentDirs(file);
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}

		final DOMSource source = new DOMSource(doc);
		final StreamResult result = new StreamResult(file);

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		try {
			transformer.transform(source, result);
		} catch (final TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * @param doc
	 * @param nodeTable
	 * @param table
	 */
	private void saveTableLayouts(final org.w3c.dom.Document doc, final Element tableNode, final TableInfo table) {

		final Element layoutsNode = createElement(doc, tableNode, NODE_DATA_LAYOUTS);

		final Element nodeLayoutDetails = createElement(doc, layoutsNode, NODE_DATA_LAYOUT);
		nodeLayoutDetails.setAttribute(ATTRIBUTE_NAME, LAYOUT_NAME_DETAILS);
		saveLayoutNode(doc, nodeLayoutDetails, table.layoutGroupsDetails);

		final Element nodeLayoutList = createElement(doc, layoutsNode, NODE_DATA_LAYOUT);
		nodeLayoutList.setAttribute(ATTRIBUTE_NAME, LAYOUT_NAME_LIST);
		saveLayoutNode(doc, nodeLayoutList, table.layoutGroupsList);

		final Element reportsNode = createElement(doc, tableNode, NODE_REPORTS);
		for (final Report report : table.reportsMap.values()) {
			final Element element = createElement(doc, reportsNode, NODE_REPORT);
			saveReport(doc, element, report);
		}

	}

	/**
	 * @param doc
	 * @param element
	 * @param report
	 */
	private void saveReport(final org.w3c.dom.Document doc, final Element element, final Report report) {
		// TODO Auto-generated method stub

	}

	private void saveLayoutNode(final org.w3c.dom.Document doc, final Element element,
			final List<LayoutGroup> layoutGroups) {
		final Element elementGroups = createElement(doc, element, NODE_DATA_LAYOUT_GROUPS);

		for (final LayoutGroup layoutGroup : layoutGroups) {
			if (layoutGroup instanceof LayoutItemNotebook) {
				final Element elementGroup = createElement(doc, elementGroups, NODE_DATA_LAYOUT_NOTEBOOK);
				saveDataLayoutGroup(doc, elementGroup, layoutGroup);
			} else if (layoutGroup instanceof LayoutItemPortal) {
				final Element elementGroup = createElement(doc, elementGroups, NODE_DATA_LAYOUT_PORTAL);
				saveDataLayoutPortal(doc, elementGroup, (LayoutItemPortal) layoutGroup);
			} else {
				final Element elementGroup = createElement(doc, elementGroups, NODE_DATA_LAYOUT_GROUP);
				saveDataLayoutGroup(doc, elementGroup, layoutGroup);
			}
		}

	}

	/**
	 * @param doc
	 * @param elementGroup
	 * @param layoutGroup
	 */
	private void saveDataLayoutPortal(final org.w3c.dom.Document doc, final Element element,
			final LayoutItemPortal portal) {
		saveUsesRelationship(element, portal);
		saveDataLayoutGroup(doc, element, portal);

		final Element elementNavigation = createElement(doc, element, NODE_DATA_LAYOUT_PORTAL_NAVIGATIONRELATIONSHIP);
		String navigationTypeAsString = "";
		switch (portal.getNavigationType()) {
		case NAVIGATION_AUTOMATIC:
			navigationTypeAsString = ATTRIBUTE_PORTAL_NAVIGATION_TYPE_AUTOMATIC;
			break;
		case NAVIGATION_NONE:
			navigationTypeAsString = ATTRIBUTE_PORTAL_NAVIGATION_TYPE_NONE;
			break;
		case NAVIGATION_SPECIFIC:
			navigationTypeAsString = ATTRIBUTE_PORTAL_NAVIGATION_TYPE_SPECIFIC;
			break;
		default:
			break;
		}
		elementNavigation.setAttribute(ATTRIBUTE_PORTAL_NAVIGATION_TYPE, navigationTypeAsString);

		if (navigationTypeAsString.equals(ATTRIBUTE_PORTAL_NAVIGATION_TYPE_SPECIFIC)) {
			// Write the specified relationship name:
			saveUsesRelationship(elementNavigation, portal.getNavigationRelationshipSpecific());
		}
	}

	/**
	 * @param doc
	 * @param elementGroup
	 * @param layoutGroup
	 */
	private void saveDataLayoutGroup(final org.w3c.dom.Document doc, final Element nodeGroup, final LayoutGroup group) {
		saveTitle(doc, nodeGroup, group);

		// Write the column count:
		setAttributeAsDecimal(nodeGroup, ATTRIBUTE_LAYOUT_GROUP_COLUMNS_COUNT, group.getColumnCount());

		// Write the child items:
		for (final LayoutItem layoutItem : group.getItems()) {
			if (layoutItem instanceof LayoutItemPortal) {
				final Element element = createElement(doc, nodeGroup, NODE_DATA_LAYOUT_PORTAL);
				saveDataLayoutPortal(doc, element, (LayoutItemPortal) layoutItem);
			} else if (layoutItem instanceof LayoutItemNotebook) {
				final Element element = createElement(doc, nodeGroup, NODE_DATA_LAYOUT_NOTEBOOK);
				saveDataLayoutGroup(doc, element, (LayoutItemNotebook) layoutItem);
			} else if (layoutItem instanceof LayoutGroup) {
				final Element element = createElement(doc, nodeGroup, NODE_DATA_LAYOUT_GROUP);
				saveDataLayoutGroup(doc, element, (LayoutGroup) layoutItem);
			} else if (layoutItem instanceof LayoutItemField) {
				final Element element = createElement(doc, nodeGroup, NODE_DATA_LAYOUT_ITEM);
				saveDataLayoutItemField(doc, element, (LayoutItemField) layoutItem);
			} else if (layoutItem instanceof LayoutItemGroupBy) {
				final Element element = createElement(doc, nodeGroup, NODE_DATA_LAYOUT_ITEM_GROUPBY);
				saveDataLayoutItemGroupBy(doc, element, (LayoutItemGroupBy) layoutItem);
			}
		}
	}

	/**
	 * @param doc
	 * @param element
	 * @param layoutItem
	 */
	private void saveDataLayoutItemGroupBy(final org.w3c.dom.Document doc, final Element element,
			final LayoutItemGroupBy item) {
		saveDataLayoutGroup(doc, element, item);

		final Element elementGroupBy = createElement(doc, element, NODE_GROUPBY);
		saveDataLayoutItemField(doc, elementGroupBy, item.getFieldGroupBy());

		final Element elementSecondaryFields = createElement(doc, element, NODE_SECONDARY_FIELDS);
		final Element elementLayoutGroup = createElement(doc, elementSecondaryFields, NODE_DATA_LAYOUT_GROUP);
		saveDataLayoutGroup(doc, elementLayoutGroup, item.getSecondaryFields());
	}

	/**
	 * @param doc
	 * @param element
	 * @param layoutItem
	 */
	private void saveDataLayoutItemField(final org.w3c.dom.Document doc, final Element element,
			final LayoutItemField item) {
		element.setAttribute(ATTRIBUTE_NAME, item.getName());
		saveUsesRelationship(element, item);

		final CustomTitle customTitle = item.getCustomTitle();
		if (customTitle != null) {
			final Element elementCustomTitle = createElement(doc, element, NODE_CUSTOM_TITLE);
			setAttributeAsBoolean(elementCustomTitle, ATTRIBUTE_CUSTOM_TITLE_USE_CUSTOM,
					customTitle.getUseCustomTitle());
			saveTitle(doc, elementCustomTitle, customTitle); // LayoutItemField doesn't use its own title member.
		}

		setAttributeAsBoolean(element, ATTRIBUTE_USE_DEFAULT_FORMATTING, item.getUseDefaultFormatting());

		final Element elementFormatting = createElement(doc, element, NODE_FORMATTING);
		saveFormatting(elementFormatting, item.getFormatting());
	}

	/**
	 * @param element
	 * @param item
	 */
	private void saveUsesRelationship(final Element element, final UsesRelationship item) {
		final Relationship relationship = item.getRelationship();
		if (relationship != null) {
			element.setAttribute(ATTRIBUTE_RELATIONSHIP_NAME, relationship.getName());
		}

		final Relationship relatedRelationship = item.getRelatedRelationship();
		if (relatedRelationship != null) {
			element.setAttribute(ATTRIBUTE_RELATED_RELATIONSHIP_NAME, relatedRelationship.getName());
		}
	}

	/**
	 * @param rootNode
	 * @param nodeConnection
	 * @return
	 */
	private Element createElement(final org.w3c.dom.Document doc, final Element parentNode, final String name) {
		final Element node = doc.createElement(name);
		parentNode.appendChild(node);
		return node;
	}

	public String getSelfHostedDirectoryPath() {
		final String uriFile = getFileURI();
		if (!StringUtils.isEmpty(uriFile)) {
			final File file = new File(uriFile);
			final File parent = file.getParentFile();
			if (parent == null) {
				// TODO: Warn.
				return "";
			}

			File dataDir = null;
			switch (hostingMode) {
			case HOSTING_MODE_POSTGRES_SELF:
				dataDir = new File(parent, "glom_postgres_data");
				break;
			case HOSTING_MODE_POSTGRES_CENTRAL:
				dataDir = parent;
				break;
			case HOSTING_MODE_MYSQL_SELF:
				dataDir = new File(parent, "glom_mysql_data");
				break;
			case HOSTING_MODE_MYSQL_CENTRAL:
				dataDir = parent;
				break;
			case HOSTING_MODE_SQLITE:
				dataDir = parent;
				break;
			default:
				// TODO: Warn.
				break;
			}

			if (dataDir != null) {
				return dataDir.getPath();
			}
		}

		// TODO: std::cerr << G_STRFUNC << ": returning empty string." << std::endl;
		return "";
	}

	/**
	 */
	public void setConnectionDatabase(final String databaseName) {
		connectionDatabase = databaseName;
	}

	/**
	 * Gets the primary key Field for the specified table name.
	 * 
	 * @param tableName
	 *            name of table to search for the primary key field
	 * @return primary key Field
	 */
	public Field getTablePrimaryKeyField(final String tableName) {
		Field primaryKey = null;
		final List<Field> fieldsVec = getTableFields(tableName);
		for (int i = 0; i < Utils.safeLongToInt(fieldsVec.size()); i++) {
			final Field field = fieldsVec.get(i);
			if (field.getPrimaryKey()) {
				primaryKey = field;
				break;
			}
		}
		return primaryKey;
	}

	/**
	 * @param resp
	 * @param attrDocumentID
	 * @param tableName
	 * @param layoutName
	 * @param layoutPath
	 * @return
	 * @throws IOException
	 */
	public LayoutItem getLayoutItemByPath(
			final String tableName, final String layoutName, final String layoutPath) throws IOException {
		final List<LayoutGroup> listLayoutGroups = getDataLayoutGroups(layoutName, tableName);
		if(listLayoutGroups == null) {
			Log.error("The layout with the specified name was not found. tableName=" + tableName + ", layoutName=" + layoutName);
			return null;
		}

		if(listLayoutGroups.isEmpty()) {
			Log.error("The layout was empty. attrTableName=" + tableName + ", layoutName=" + layoutName);
			return null;
		}
		
		final int[] indices = Utils.parseLayoutPath(layoutPath);
		if((indices == null) || (indices.length == 0)) {
			Log.error("The layout path was empty or could not be parsed. layoutPath=" + layoutPath);
			return null;
		}
	
		LayoutItem item = null;
		int depth = 0;
		for(int index:indices) {
			if(index < 0) {
				Log.error("An index in the layout path was negative, at depth=" + depth + ", layoutPath=" + layoutPath);
				return null;
			}

			//Get the nth item of either the top-level list or the current item:
			if(depth == 0) {
				if(index < listLayoutGroups.size()) {
					item = listLayoutGroups.get(index);
				} else {
					Log.error("An index in the layout path is larger than the number of child items, at depth=" + depth + ", layoutPath=" + layoutPath);
					return null;
				}
			} else {
				if(item instanceof LayoutGroup) {
					final LayoutGroup group = (LayoutGroup)item;
					final List<LayoutItem> items = group.getItems();
					if(index < items.size()) {
						item = items.get(index);
					} else {
						Log.error("An index in the layout path is larger than the number of child items, at depth=" + depth + ", layoutPath=" + layoutPath);
						return null;
					}
				} else {
					Log.error("An intermediate item in the layout path is not a layout group, at depth=" + depth + ", layoutPath=" + layoutPath);
					return null;
				}
			}
			
			depth++;
		}
		
		if(item == null) {
			Log.error("The item specifed by the layout path could not be found. layoutPath=" + layoutPath);
			return null;
		}
		return item;
	}

	/**
	 * @return
	 */
	public SQLDialect getSqlDialect() {
		switch (hostingMode) {
		case HOSTING_MODE_POSTGRES_SELF:
		case HOSTING_MODE_POSTGRES_CENTRAL:
			return SQLDialect.POSTGRES;
		case HOSTING_MODE_MYSQL_SELF:
		case HOSTING_MODE_MYSQL_CENTRAL:
			return SQLDialect.MYSQL;
		case HOSTING_MODE_SQLITE:
			return SQLDialect.SQLITE;
		default:
			return null;
		}
	}
}
