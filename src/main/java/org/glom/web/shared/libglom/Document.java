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
import org.glom.web.shared.libglom.layout.Formatting;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemNotebook;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;
import org.glom.web.shared.libglom.layout.UsesRelationship;
import org.glom.web.shared.libglom.layout.UsesRelationshipImpl;
import org.glom.web.shared.libglom.layout.reportparts.LayoutItemGroupBy;
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

	
	@SuppressWarnings("serial")
	private class TableInfo extends Translatable {
		public boolean isDefault;
		public boolean isHidden;

		private final Hashtable<String, Field> fieldsMap = new Hashtable<String, Field>();
		private final Hashtable<String, Relationship> relationshipsMap = new Hashtable<String, Relationship>();
		private final Hashtable<String, Report> reportsMap = new Hashtable<String, Report>();
		
		private List<LayoutGroup> layoutGroupsList = new ArrayList<LayoutGroup>();
		private List<LayoutGroup> layoutGroupsDetails = new ArrayList<LayoutGroup>();
	}

	private String fileURI = "";
	private org.w3c.dom.Document xmlDocument = null;
	
	private Translatable databaseTitle = new Translatable();
	private List<String> translationAvailableLocales = new ArrayList<String>(); //TODO
	private String connectionServer = "";
	private String connectionDatabase = "";
	private int connectionPort = 0;
	private final Hashtable<String, TableInfo> tablesMap = new Hashtable<String, TableInfo>();
	
	private static final String NODE_CONNECTION = "connection";
	private static final String ATTRIBUTE_CONNECTION_SERVER = "server";
	private static final String ATTRIBUTE_CONNECTION_DATABASE = "database";
	private static final String ATTRIBUTE_CONNECTION_PORT = "port";
	private static final String NODE_TABLE = "table";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_TITLE = "title";
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
	private static final String ATTRIBUTE_PRIMARY_KEY = "primary_key";
	private static final String ATTRIBUTE_FIELD_TYPE = "type";
	private static final String NODE_FORMATTING = "formatting";
	private static final String ATTRIBUTE_TEXT_FORMAT_MULTILINE = "format_text_multiline";
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
	private static final String NODE_DATA_LAYOUT_ITEM_GROUPBY = "data_layout_item_groupby";
	private static final String NODE_GROUPBY = "groupby";
	private static final String NODE_SECONDARY_FIELDS = "secondary_fields";
	private static final String ATTRIBUTE_USE_DEFAULT_FORMATTING = "use_default_formatting";
	private static final String LAYOUT_NAME_DETAILS = "details";
	private static final String LAYOUT_NAME_LIST = "list";
	
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
		
		//We first load the fields, relationships, etc,
		//for all tables:
		final List<Node> listTableNodes = getChildrenByTagName(rootNode, NODE_TABLE);
		for(Node node: listTableNodes) {
			if(!(node instanceof Element))
				continue;

			final Element element = (Element)node;
			final TableInfo info = loadTableNodeBasic(element);
			tablesMap.put(info.get_name(), info);
		}

		//We then load the layouts for all tables, because they
		//need the fields and relationships for all tables:
		for(Node node: listTableNodes) {
			if(!(node instanceof Element))
				continue;

			final Element element = (Element)node;
			final String tableName = element.getAttribute(ATTRIBUTE_NAME);
			
			//We first load the fields, relationships, etc:
			final TableInfo info = getTableInfo(tableName);
			if(info == null) {
				continue;
			}
			
			//We then load the layouts afterwards, because they
			//need the fields and relationships:
			loadTableLayouts(element, info);

			tablesMap.put(info.get_name(), info);
		}
		
		Element nodeConnection = getElementByName(rootNode, NODE_CONNECTION);
		if(nodeConnection != null) {
			connectionServer = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_SERVER);
			connectionDatabase = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_DATABASE);
			connectionPort = getAttributeAsDecimal(nodeConnection, nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_PORT));
		}
		

		return true;
	};
	
	private Element getElementByName(final Element parentElement, final String tagName) {	
		final List<Node> listNodes = getChildrenByTagName(parentElement, tagName);
		if(listNodes == null)
			return null;

		if(listNodes.size() == 0)
			return null;

		return (Element)listNodes.get(0);
	}
	
	private boolean getAttributeAsBoolean(final Element node, final String attributeName) {
		final String str = node.getAttribute(attributeName);
		if(str == null)
			return false;
		
		return str.equals("true");
	}
	
	/**
	 * @param elementFormatting
	 * @param aTTRIBUTE_DECIMAL_PLACES2
	 * @return
	 */
	private int getAttributeAsDecimal(final Element node, final String attributeName) {
		final String str = node.getAttribute(attributeName);
		if(StringUtils.isEmpty(str))
			return 0;
		
		return Integer.valueOf(str);
	}
	
	/** Load a title and its translations.
	 * 
	 * @param node The XML Element that may contain a title attribute and a trans_set of translations of the title.
	 * @param title
	 */
	private void loadTitle(final Element node, final Translatable title) {
		title.set_name(node.getAttribute(ATTRIBUTE_NAME));
		
		title.set_title_original(node.getAttribute(ATTRIBUTE_TITLE));
		
		final Element nodeSet = getElementByName(node, NODE_TRANSLATIONS_SET);
		if(nodeSet == null) {
			return;
		}
		
		final List<Node> listNodes = getChildrenByTagName(nodeSet, NODE_TRANSLATIONS);
		if(listNodes == null)
			return;

		for(Node transNode : listNodes) {
			if(!(transNode instanceof Element)) {
				continue;
			}
			
			final Element element = (Element)transNode;
			
			final String locale = element.getAttribute(ATTRIBUTE_TRANSLATION_LOCALE);
			final String translatedTitle = element.getAttribute(ATTRIBUTE_TRANSLATION_TITLE);
			if(!StringUtils.isEmpty(locale) && !StringUtils.isEmpty(translatedTitle)) {
				title.translationsMap.put(locale, translatedTitle);
			}
		}
	}
	/**
	 * @param tableNode
	 * @return 
	 */
	private TableInfo loadTableNodeBasic(final Element tableNode) {
		TableInfo info = new TableInfo();
		loadTitle(tableNode, info);
		final String tableName = info.get_name();

		info.isDefault = getAttributeAsBoolean(tableNode, ATTRIBUTE_DEFAULT);
		info.isHidden = getAttributeAsBoolean(tableNode, ATTRIBUTE_HIDDEN);
		
		//These should be loaded before the fields, because the fields use them.
		final Element relationshipsNode = getElementByName(tableNode, NODE_RELATIONSHIPS);
		if(relationshipsNode != null) {
			final List<Node> listNodes = getChildrenByTagName(relationshipsNode, NODE_RELATIONSHIP);
			for(Node node : listNodes) {
				if(!(node instanceof Element)) {
					continue;
				}
				
				final Element element = (Element)node;
				Relationship relationship = new Relationship();
				loadTitle(element, relationship);
				relationship.setFromTable(tableName);
				relationship.setFromField( element.getAttribute(ATTRIBUTE_RELATIONSHIP_FROM_FIELD));
				relationship.setToTable( element.getAttribute(ATTRIBUTE_RELATIONSHIP_TO_TABLE));
				relationship.setToField( element.getAttribute(ATTRIBUTE_RELATIONSHIP_TO_FIELD));

				info.relationshipsMap.put(relationship.get_name(), relationship);
			}
		}
		
		final Element fieldsNode = getElementByName(tableNode, NODE_FIELDS);
		if(fieldsNode != null) {
			final List<Node> listNodes = getChildrenByTagName(fieldsNode, NODE_FIELD);
			for(Node node : listNodes) {
				if(!(node instanceof Element)) {
					continue;
				}

				final Element element = (Element)node;
				Field field = new Field();
				loadField(element, field);

				info.fieldsMap.put(field.get_name(), field);
			}
		}

		return info;
	}

	/**
	 * @param tableNode
	 * @param info
	 */
	private void loadTableLayouts(final Element tableNode, TableInfo info) {
		final String tableName = info.get_name();

		final Element layoutsNode = getElementByName(tableNode, NODE_DATA_LAYOUTS);
		if(layoutsNode != null) {
			final List<Node> listNodes = getChildrenByTagName(layoutsNode, NODE_DATA_LAYOUT);
			for(Node node : listNodes) {
				if(!(node instanceof Element)) {
					continue;
				}

				final Element element = (Element)node;
				final String name = element.getAttribute("name");
				final List<LayoutGroup> listLayoutGroups = loadLayoutNode(element, tableName);
				if(name.equals(LAYOUT_NAME_DETAILS)) {
					info.layoutGroupsDetails = listLayoutGroups;
				} else if (name.equals(LAYOUT_NAME_LIST)) {
					info.layoutGroupsList = listLayoutGroups;
				} else {
					Log.error("loadTableNode(): unexpected layout name: " + name);
				}
			}
		}
		
		final Element reportsNode = getElementByName(tableNode, NODE_REPORTS);
		if(reportsNode != null) {
			final List<Node> listNodes = getChildrenByTagName(reportsNode, NODE_REPORT);
			for(Node node : listNodes) {
				if(!(node instanceof Element)) {
					continue;
				}
				
				final Element element = (Element)node;
				Report report = new Report();
				loadReport(element, report, tableName);

				info.reportsMap.put(report.get_name(), report);
			}
		}
	}

	/**
	 * @param node
	 * @return 
	 */
	private List<LayoutGroup> loadLayoutNode(final Element node, final String tableName) {
		if(node == null) {
			return null;
		}
		
		List<LayoutGroup> result = new ArrayList<LayoutGroup>();
		
		final List<Node> listNodes = getChildrenByTagName(node, NODE_DATA_LAYOUT_GROUPS);
		for(Node nodeGroups : listNodes) {
			if(!(nodeGroups instanceof Element)) {
				continue;
			}

			final Element elementGroups = (Element)nodeGroups;
			
			NodeList list = elementGroups.getChildNodes();
			final int num = list.getLength();
			for(int i = 0; i < num; i++) {
				final Node nodeLayoutGroup = list.item(i);
				if(nodeLayoutGroup == null) {
					continue;
				}
				
				if(!(nodeLayoutGroup instanceof Element)) {
					continue;
				}

				final Element element = (Element)nodeLayoutGroup;
				final String tagName = element.getTagName();
				if(tagName == NODE_DATA_LAYOUT_GROUP) {
					LayoutGroup group = new LayoutGroup();
					loadDataLayoutGroup(element, group, tableName);
					result.add(group);
				} else if(tagName == NODE_DATA_LAYOUT_NOTEBOOK) {
					LayoutItemNotebook group = new LayoutItemNotebook();
					loadDataLayoutGroup(element, group, tableName);
					result.add(group);
				} else if(tagName == NODE_DATA_LAYOUT_PORTAL) {
					LayoutItemPortal portal = new LayoutItemPortal();
					loadDataLayoutPortal(element, portal, tableName);
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
	private void loadUsesRelationship(Element element, String tableName, UsesRelationship item) {
		if(element == null) {
			return;
		}

		if(item == null) {
			return;
		}

		final String relationship_name = element.getAttribute(ATTRIBUTE_RELATIONSHIP_NAME);
		Relationship relationship = null;
		if(!StringUtils.isEmpty(relationship_name)) {
			//std::cout << "  debug in : table_name=" << table_name << ", relationship_name=" << relationship_name << std::endl;
			relationship = getRelationship(tableName, relationship_name);
			item.setRelationship(relationship);

			if(relationship == null) {
				Log.error("relationship not found: " + relationship_name + ", in table: " + tableName);
			}
		}

		final String related_relationship_name = element.getAttribute(ATTRIBUTE_RELATED_RELATIONSHIP_NAME);
		if(!StringUtils.isEmpty(related_relationship_name) && (relationship != null)) {
			final Relationship related_relationship = getRelationship(relationship.get_to_table(), related_relationship_name);
			if(related_relationship == null) {
				Log.error("related relationship not found in table=" + relationship.get_to_table() + ",  name=" + related_relationship_name);

				item.setRelatedRelationship(related_relationship);
			}
		}
	}

	/** getElementsByTagName() is recursive, but we do not want that.
	 * 
	 * @param node
	 * @param
	 * @return
	 */
	private List<Node> getChildrenByTagName(final Element parentNode, final String tagName) {
		List<Node> result = new ArrayList<Node>();

		NodeList list = parentNode.getElementsByTagName(tagName);
		final int num = list.getLength();
		for(int i = 0; i < num; i++) {
			final Node node = list.item(i);
			if(node == null) {
				continue;
			}

			final Node itemParentNode = node.getParentNode();
			if(itemParentNode.equals(parentNode)) {
				result.add(node);
			}
		}

		return result;
	}

	/**
	 * @param element
	 * @param group
	 */
	private void loadDataLayoutGroup(Element nodeGroup, LayoutGroup group, final String tableName) {
		loadTitle(nodeGroup, group);
		
		final NodeList listNodes = nodeGroup.getChildNodes();
		final int num = listNodes.getLength();
		for(int i = 0; i < num; i++) {
			final Node node = listNodes.item(i);
			if(!(node instanceof Element))
				continue;

			final Element element = (Element)node;
			final String tagName = element.getTagName();
			if(tagName == NODE_DATA_LAYOUT_GROUP) {
				LayoutGroup childGroup = new LayoutGroup();
				loadDataLayoutGroup(element, childGroup, tableName);
				group.add_item(childGroup);
			} else if(tagName == NODE_DATA_LAYOUT_NOTEBOOK) {
				LayoutItemNotebook childGroup = new LayoutItemNotebook();
				loadDataLayoutGroup(element, childGroup, tableName);
				group.add_item(childGroup);
			} else if(tagName == NODE_DATA_LAYOUT_PORTAL) {
				LayoutItemPortal childGroup = new LayoutItemPortal();
				loadDataLayoutPortal(element, childGroup, tableName);
				group.add_item(childGroup);
			} else if(element.getTagName() == NODE_DATA_LAYOUT_ITEM) {
				final LayoutItemField item = new LayoutItemField();
				loadDataLayoutItemField(element, item, tableName);
				group.add_item(item);
			} else if(element.getTagName() == NODE_DATA_LAYOUT_ITEM_GROUPBY) {
				final LayoutItemGroupBy item = new LayoutItemGroupBy();
				loadDataLayoutItemGroupBy(element, item, tableName);
				group.add_item(item);
			}
		}
	}

	/**
	 * @param element
	 * @param item
	 * @param tableName
	 */
	private void loadDataLayoutItemGroupBy(final Element element, final LayoutItemGroupBy item, final String tableName) {
		loadDataLayoutGroup(element, item, tableName);

		final Element elementGroupBy = getElementByName(element, NODE_GROUPBY);
		if(elementGroupBy == null) {
			return;
		}

		final LayoutItemField fieldGroupBy = new LayoutItemField();
		loadDataLayoutItemField(elementGroupBy, fieldGroupBy, tableName);
		item.set_field_group_by(fieldGroupBy);
		
		final Element elementSecondaryFields = getElementByName(element, NODE_SECONDARY_FIELDS);
		if(elementSecondaryFields == null) {
			return;
		}

		final Element elementLayoutGroup = getElementByName(elementSecondaryFields, NODE_DATA_LAYOUT_GROUP);
		if(elementLayoutGroup != null) {
			final LayoutGroup secondaryLayoutGroup = new LayoutGroup();
			loadDataLayoutGroup(elementLayoutGroup, secondaryLayoutGroup, tableName);
			item.set_secondary_fields(secondaryLayoutGroup);
		}
	}

	/**
	 * @param element
	 * @param item
	 */
	private void loadDataLayoutItemField(final Element element, final LayoutItemField item, final String tableName) {
		loadTitle(element, item);
		loadUsesRelationship(element, tableName, item);
		
		//Get the actual field:
		final String fieldName = item.get_name();
		final String inTableName = item.get_table_used(tableName);
		final Field field = get_field(inTableName, fieldName);
		item.set_full_field_details(field);
		
		item.setUseDefaultFormatting(getAttributeAsBoolean(element, ATTRIBUTE_USE_DEFAULT_FORMATTING));
		
		final Element elementFormatting = getElementByName(element, NODE_FORMATTING);
		if(elementFormatting != null) {
			loadFormatting(elementFormatting, item.getFormatting());
		}
	}

	/**
	 * @param element
	 * @param childGroup
	 */
	private void loadDataLayoutPortal(Element element, LayoutItemPortal portal, final String tableName) {
		loadUsesRelationship(element, tableName, portal);
		final String relatedTableName = portal.get_table_used(tableName);
		loadDataLayoutGroup(element, portal, relatedTableName);

		final Element elementNavigation = getElementByName(element, NODE_DATA_LAYOUT_PORTAL_NAVIGATIONRELATIONSHIP);
		if(elementNavigation != null) {
			final String navigation_type_as_string = elementNavigation.getAttribute(ATTRIBUTE_PORTAL_NAVIGATION_TYPE);
			if(StringUtils.isEmpty(navigation_type_as_string) || navigation_type_as_string == ATTRIBUTE_PORTAL_NAVIGATION_TYPE_AUTOMATIC) {
				portal.setNavigationType(LayoutItemPortal.NavigationType.NAVIGATION_AUTOMATIC);
			} else if(navigation_type_as_string == ATTRIBUTE_PORTAL_NAVIGATION_TYPE_NONE) {
				portal.setNavigationType(LayoutItemPortal.NavigationType.NAVIGATION_NONE);
			} else if(navigation_type_as_string == ATTRIBUTE_PORTAL_NAVIGATION_TYPE_SPECIFIC) {
				//Read the specified relationship name:
				final UsesRelationship relationship_navigation_specific = new UsesRelationshipImpl();
				loadUsesRelationship(elementNavigation, relatedTableName, relationship_navigation_specific);
				portal.setNavigationRelationshipSpecific(relationship_navigation_specific);
			}
		}
		
	}

	/**
	 * @param element
	 * @param field
	 */
	private void loadField(Element element, Field field) {
		loadTitle(element, field);
		
		Field.GlomFieldType fieldType = Field.GlomFieldType.TYPE_INVALID;
		final String fieldTypeStr = element.getAttribute(ATTRIBUTE_FIELD_TYPE);
		if(!StringUtils.isEmpty(fieldTypeStr)) {
			if(fieldTypeStr.equals("Boolean")) {
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
			
		field.set_glom_field_type(fieldType);
		
		field.set_primary_key(getAttributeAsBoolean(element, ATTRIBUTE_PRIMARY_KEY));
		loadTitle(element, field);
		
		final Element elementFormatting = getElementByName(element, NODE_FORMATTING);
		if(elementFormatting != null) {
			loadFormatting(elementFormatting, field.getFormatting());
		}
	}

	/**
	 * @param elementFormatting
	 * @param formatting
	 */
	private void loadFormatting(Element elementFormatting, Formatting formatting) {
		if (elementFormatting == null)
			return;

		if (formatting == null)
			return;
		
		//formatting.setTextFormatMultiline(getAttributeAsBoolean(elementFormatting, ATTRIBUTE_TEXT_FORMAT_MULTILINE));


		final NumericFormat numericFormatting = formatting.getNumericFormat();
		if(numericFormatting != null) {
			numericFormatting.setUseThousandsSeparator(getAttributeAsBoolean(elementFormatting, ATTRIBUTE_USE_THOUSANDS_SEPARATOR));
			numericFormatting.setDecimalPlaces(getAttributeAsDecimal(elementFormatting, ATTRIBUTE_DECIMAL_PLACES));
		}
		
	}

	/**
	 * @param element
	 * @param reportNode
	 */
	private void loadReport(Element element, Report report, final String tableName) {
		report.set_name(element.getAttribute(ATTRIBUTE_NAME));
		loadTitle(element, report);

		final List<LayoutGroup> listLayoutGroups = loadLayoutNode(element, tableName);
		
		//A report can actually only have one LayoutGroup,
		//though it uses the same XML structure as List and Details layouts,
		//which (wrongly) suggests that it can have more than one group.
		LayoutGroup layoutGroup = null;
		if(!listLayoutGroups.isEmpty()) {
			layoutGroup = listLayoutGroups.get(0);
		}

		report.set_layout_group(layoutGroup);
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
	
	public List<String> get_translation_available_locales() {
		return translationAvailableLocales;
	}
	
	public Document.HostingMode get_hosting_mode() {
		return HostingMode.HOSTING_MODE_POSTGRES_CENTRAL; //TODO
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
	
	public List<String> get_table_names() {
		//TODO: Return a Set?
		return new ArrayList<String>(tablesMap.keySet());
	}
	
	public boolean get_table_is_hidden(final String tableName) {
		final TableInfo info = getTableInfo(tableName);
		if(info == null) {
			return false;
		}
		
		return info.isHidden;
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
				return info.get_name();
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
	
	public Field get_field(String tableName, String strFieldName) {
		final TableInfo info = getTableInfo(tableName);
		if(info == null)
			return null;

		return info.fieldsMap.get(strFieldName);
	}
	
	public List<LayoutGroup> get_data_layout_groups(String layoutName, String parentTableName) {
		final TableInfo info = getTableInfo(parentTableName);
		if(info == null)
			return new ArrayList<LayoutGroup>();
		
		if(layoutName == LAYOUT_NAME_DETAILS) {
			return info.layoutGroupsDetails;
		} else if(layoutName == LAYOUT_NAME_LIST) {
			return info.layoutGroupsList;
		} else {
			return new ArrayList<LayoutGroup>();
		}
	}
	
	public List<String> get_report_names(String tableName) {
		final TableInfo info = getTableInfo(tableName);
		if(info == null)
			return new ArrayList<String>();

		return new ArrayList<String>(info.reportsMap.keySet());
	}
	
	public Report get_report(String tableName, String reportName) {
		final TableInfo info = getTableInfo(tableName);
		if(info == null)
			return null;

		return info.reportsMap.get(reportName);
	}

	/**
	 * @param parent_table_name
	 * @param field
	 * @return
	 */
	public Relationship getFieldUsedInRelationshipToOne(String table_name, LayoutItemField layout_field) {
		
		if(layout_field == null)
		{
			Log.error("layout_field was null");
			return null;
		}

		Relationship result = null;

		final String table_used = layout_field.get_table_used(table_name);
		final TableInfo info = getTableInfo(table_used);
		if(info == null) {
			//This table is special. We would not create a relationship to it using a field:
			//if(table_used == GLOM_STANDARD_TABLE_PREFS_TABLE_NAME)
			//	return result;

			Log.error("table not found: " + table_used);
			return null;
		}

		//Look at each relationship:
		final String field_name = layout_field.get_name();
		for(Relationship relationship : info.relationshipsMap.values()) {
			if(relationship != null)
			{
				//If the relationship uses the field
				if(relationship.get_from_field() == field_name)
				{
					//if the to_table is not hidden:
					if(!get_table_is_hidden(relationship.get_to_table()))
					{
						//TODO_Performance: The use of this convenience method means we get the full relationship information again:
						if(getRelationshipIsToOne(table_name, relationship.get_name()))
						{
							result = relationship;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * @param table_name
	 * @param get_name
	 * @return
	 */
	private boolean getRelationshipIsToOne(String table_name, String relationship_name) {
		final Relationship relationship = getRelationship(table_name, relationship_name);
		if(relationship != null)
		{
			final Field field_to = get_field(relationship.get_to_table(), relationship.get_to_field());
			if(field_to != null) {
				return (field_to.get_primary_key() || field_to.getUniqueKey());
			}
		}

		return false;
	}

	/**
	 * @param table_name
	 * @param relationship_name
	 * @return
	 */
	private Relationship getRelationship(String table_name, String relationship_name) {
		final TableInfo info = getTableInfo(table_name);
		if(info == null) {
			Log.error("table not found: " + table_name);
			return null;
		}

		return info.relationshipsMap.get(relationship_name);
	}
}
