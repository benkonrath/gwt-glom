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

package org.glom.web.server;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignGroup;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;

import org.apache.commons.lang.StringUtils;
import org.glom.libglom.Document;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.LayoutGroup;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.LayoutItem_GroupBy;
import org.glom.libglom.Relationship;
import org.glom.libglom.Report;
import org.glom.libglom.SortClause;
import org.glom.libglom.SortFieldPair;
import org.glom.libglom.SqlBuilder;
import org.glom.libglom.SqlExpr;
import org.glom.libglom.Value;

/**
 * @author Murray Cumming <murrayc@openimus.com>
 * 
 */
public class ReportGenerator {

	final int height = 30; // Points, as specified later.
	// An arbitrary width, because we must specify _some_ width:
	final int width = 100; // Points, as specified later.

	LayoutFieldVector fieldsToGet = new LayoutFieldVector();
	SortClause sortClause = new SortClause();
	String localeID;

	final JasperDesign design = new JasperDesign();
	JRDesignStyle titleStyle = new JRDesignStyle();
	JRDesignStyle normalStyle = new JRDesignStyle();
	JRDesignStyle boldStyle = new JRDesignStyle();

	ReportGenerator(final String localeID) {
		this.localeID = StringUtils.defaultString(localeID);
	}

	/**
	 */
	public String generateReport(final Document document, final String tableName, final Report report,
			final Connection connection) {

		final org.glom.libglom.LayoutGroup layout_group = report.get_layout_group();

		design.setName(report.get_title(localeID)); // TODO: Actually, we want the title.

		titleStyle.setName("Sans_Title");
		titleStyle.setFontName("DejaVu Sans");
		titleStyle.setFontSize(24);
		normalStyle.setName("Sans_Normal");
		normalStyle.setDefault(true);
		normalStyle.setFontName("DejaVu Sans");
		normalStyle.setFontSize(12);
		boldStyle.setName("Sans_Bold");
		boldStyle.setFontName("DejaVu Sans");
		boldStyle.setFontSize(12);
		boldStyle.setBold(true);
		try {
			design.addStyle(titleStyle);
			design.addStyle(normalStyle);
			design.addStyle(boldStyle);
		} catch (final JRException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		final JRDesignBand titleBand = new JRDesignBand();
		titleBand.setHeight(height);
		final JRDesignStaticText staticTitle = new JRDesignStaticText();
		staticTitle.setText(report.get_title(localeID));
		staticTitle.setY(0);
		staticTitle.setX(0);
		staticTitle.setWidth(width * 5); // No data will be shown without this.
		// staticTitle.setStretchWithOverflow(true);
		staticTitle.setHeight(height); // We must specify _some_ height.
		staticTitle.setStyle(titleStyle);
		titleBand.addElement(staticTitle);
		design.setTitle(titleBand);

		final JRDesignBand detailBand = new JRDesignBand();
		detailBand.setHeight(height + 20);

		final JRDesignBand headerBand = new JRDesignBand();
		headerBand.setHeight(height + 20);

		fieldsToGet = new LayoutFieldVector();
		final int x = 0;
		addToReport(layout_group, detailBand, x, headerBand, 0);

		design.setColumnHeader(headerBand);
		((JRDesignSection) design.getDetailSection()).addBand(detailBand);

		// Later versions of libglom actually return an empty SqlExpr when quickFindValue is empty,
		// but let's be sure:
		final String quickFind = ""; // TODO
		SqlExpr whereClause;
		if (StringUtils.isEmpty(quickFind)) {
			whereClause = new SqlExpr();
		} else {
			final Value quickFindValue = new Value(quickFind);
			whereClause = Glom.get_find_where_clause_quick(document, tableName, quickFindValue);
		}

		final Relationship extraJoin = new Relationship(); // Ignored.
		final SqlBuilder builder = Glom.build_sql_select_with_where_clause(tableName, fieldsToGet, whereClause,
				extraJoin, sortClause);
		final String sqlQuery = Glom.sqlbuilder_get_full_query(builder);

		final JRDesignQuery query = new JRDesignQuery();
		query.setText(sqlQuery); // TODO: Extra sort clause to sort the rows within the groups.
		design.setQuery(query);

		JasperReport jasperreport;
		try {
			jasperreport = JasperCompileManager.compileReport(design);
		} catch (final JRException ex) {
			ex.printStackTrace();
			return "Failed to Generate HTML: compileReport() failed.";
		}

		JasperPrint print;
		try {
			final HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("ReportTitle", report.get_title(localeID)); // TODO: Use the title, not the name.
			print = JasperFillManager.fillReport(jasperreport, parameters, connection);
		} catch (final JRException ex) {
			ex.printStackTrace();
			return "Failed to Generate HTML: fillReport() failed.";
		}

		final ByteArrayOutputStream output = new ByteArrayOutputStream();

		// We use this because there is no JasperExportManager.exportReportToHtmlStream() method.
		// JasperExportManager.exportReportToXmlStream(print, output);
		try {
			final JRXhtmlExporter exporter = new JRXhtmlExporter();
			exporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, print);
			exporter.setParameter(JRHtmlExporterParameter.OUTPUT_STREAM, output);

			// Use points instead of pixels for sizes, because pixels are silly
			// in HTML:
			exporter.setParameter(JRHtmlExporterParameter.SIZE_UNIT, "pt");

			exporter.exportReport();
		} catch (final JRException ex) {
			ex.printStackTrace();
			return "Failed to Generate HTML: exportReport() failed.";
		}

		// System.out.print(output.toString() + "\n");
		return output.toString();
	}

	/**
	 * @param layout_group
	 * @param parentBand
	 * @param x
	 * @param fieldTitlesY
	 *            TODO
	 * @param height
	 */
	private int addToReport(final org.glom.libglom.LayoutGroup layout_group, final JRDesignBand parentBand, int x,
			final JRDesignBand headerBand, final int fieldTitlesY) {

		/**
		 * If this is a vertical group then we will layout the fields out vertically instead of horizontally.
		 */
		/*
		 * TODO: final org.glom.libglom.LayoutItem_VerticalGroup verticalGroup = LayoutItem_VerticalGroup
		 * .cast_dynamic(layout_group); final boolean isVertical = (verticalGroup != null);
		 */

		// Where we put the field titles depends on whether we are in a group-by:
		JRDesignBand fieldTitlesBand = headerBand;
		int thisFieldTitlesY = fieldTitlesY; // If they are in a group title the they must be lower.

		final LayoutItemVector layoutItemsVec = layout_group.get_items();
		final int numItems = Utils.safeLongToInt(layoutItemsVec.size());
		for (int i = 0; i < numItems; i++) {
			final org.glom.libglom.LayoutItem libglomLayoutItem = layoutItemsVec.get(i);

			final LayoutGroup libglomLayoutGroup = LayoutGroup.cast_dynamic(libglomLayoutItem);
			final LayoutItem_Field libglomLayoutItemField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
			if (libglomLayoutItemField != null) {
				x = addFieldToDetailBand(parentBand, headerBand, x, libglomLayoutItemField, thisFieldTitlesY);
			} else if (libglomLayoutGroup != null) {
				final LayoutItem_GroupBy libglomGroupBy = LayoutItem_GroupBy.cast_dynamic(libglomLayoutGroup);
				if (libglomGroupBy != null) {
					final LayoutItem_Field fieldGroupBy = libglomGroupBy.get_field_group_by();
					if (fieldGroupBy == null)
						continue;

					final String fieldName = addField(fieldGroupBy);

					// We must sort by the group field,
					// so that JasperReports can start a new group when its value changes.
					// Note that this is not like a SQL GROUP BY.
					final SortFieldPair pair = new SortFieldPair();
					pair.setFirst(fieldGroupBy);
					pair.setSecond(true); // Ascending.
					sortClause.add(pair);

					final JRDesignGroup group = new JRDesignGroup();
					group.setName(fieldName);

					// Show the field value:
					final JRDesignExpression expression = createFieldExpression(fieldName);
					group.setExpression(expression);

					try {
						design.addGroup(group);
					} catch (final JRException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Show the group-by field:
					final JRDesignBand groupBand = new JRDesignBand();

					// TODO: Use height instead of height*2 if there are no child fields,
					// for instance if the only child is a sub group-by.
					groupBand.setHeight(height * 2); // Enough height for the title and the field titles.
					((JRDesignSection) group.getGroupHeaderSection()).addBand(groupBand);

					// Put the field titles inside the group-by instead of just at the top of the page.
					// (or instead of just in the parent group-by):
					fieldTitlesBand = groupBand;
					thisFieldTitlesY = height;

					/*
					 * final JRDesignBand footerBand = new JRDesignBand(); footerBand.setHeight(height);
					 * ((JRDesignSection) group.getGroupFooterSection()).addBand(footerBand);
					 */

					int groupX = addFieldToGroupBand(groupBand, x, fieldGroupBy);

					// Show the secondary fields:
					final LayoutGroup groupSecondaries = libglomGroupBy.get_group_secondary_fields();
					if (groupSecondaries != null)
						groupX = addSecondaryFieldsToGroupBand(groupSecondaries, groupBand, groupX);

					final JRDesignLine line = new JRDesignLine();
					final int lineheight = 1;
					line.setX(0);

					// TODO: Automatically place it below the text, though that needs us to know how much height the
					// text really needs.
					line.setY(height - 15);

					// TODO: Make it as wide as needed by the details band.
					line.setWidth(groupX);
					line.setHeight(lineheight);
					groupBand.addElement(line);
				}

				// Recurse into sub-groups:
				x = addToReport(libglomLayoutGroup, parentBand, x, fieldTitlesBand, thisFieldTitlesY);
			}
		}

		return x;
	}

	private int addSecondaryFieldsToGroupBand(final org.glom.libglom.LayoutGroup layout_group,
			final JRDesignBand groupBand, int x) {
		final LayoutItemVector layoutItemsVec = layout_group.get_items();
		final int numItems = Utils.safeLongToInt(layoutItemsVec.size());
		for (int i = 0; i < numItems; i++) {
			final org.glom.libglom.LayoutItem libglomLayoutItem = layoutItemsVec.get(i);

			final LayoutGroup libglomLayoutGroup = LayoutGroup.cast_dynamic(libglomLayoutItem);
			final LayoutItem_Field libglomLayoutItemField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
			if (libglomLayoutItemField != null) {
				x = addFieldToGroupBand(groupBand, x, libglomLayoutItemField);
			} else if (libglomLayoutGroup != null) {
				// We do not expect LayoutItem_GroupBy in the secondary fields:
				// final LayoutItem_GroupBy libglomGroupBy = LayoutItem_GroupBy.cast_dynamic(libglomLayoutGroup);

				// Recurse into sub-groups:
				x = addSecondaryFieldsToGroupBand(libglomLayoutGroup, groupBand, x);
			}
		}

		return x;
	}

	/**
	 * @param fieldName
	 * @return
	 */
	private JRDesignExpression createFieldExpression(final String fieldName) {
		final JRDesignExpression expression = new JRDesignExpression();

		// TODO: Where is this format documented?
		expression.setText("$F{" + fieldName + "}");
		return expression;
	}

	/**
	 * @param parentBand
	 * @param x
	 * @param libglomLayoutItemField
	 * @param fieldTitlesY
	 *            TODO
	 * @return
	 */
	private int addFieldToDetailBand(final JRDesignBand parentBand, final JRDesignBand headerBand, int x,
			final LayoutItem_Field libglomLayoutItemField, final int fieldTitlesY) {
		final String fieldName = addField(libglomLayoutItemField);

		// Show the field title:
		final JRDesignStaticText textFieldColumn = createFieldTitleElement(x, fieldTitlesY, libglomLayoutItemField,
				false);
		textFieldColumn.setStyle(boldStyle);
		headerBand.addElement(textFieldColumn);

		// Show an instance of the field (the field value):
		final JRDesignTextField textField = createFieldValueElement(x, fieldName);
		textField.setStyle(normalStyle);
		parentBand.addElement(textField);

		x += width;
		return x;
	}

	private int addFieldToGroupBand(final JRDesignBand parentBand, int x, final LayoutItem_Field libglomLayoutItemField) {
		final String fieldName = addField(libglomLayoutItemField);

		// Show the field title:
		final JRDesignStaticText textFieldColumn = createFieldTitleElement(x, 0, libglomLayoutItemField, true);

		// Instead, the field value will be bold, because here it is like a title.
		textFieldColumn.setStyle(normalStyle);

		parentBand.addElement(textFieldColumn);
		x += width;

		// Show an instance of the field (the field value):
		final JRDesignTextField textField = createFieldValueElement(x, fieldName);
		parentBand.addElement(textField);
		textField.setStyle(boldStyle);

		x += width;
		return x;
	}

	/**
	 * @param x
	 * @param fieldName
	 * @return
	 */
	private JRDesignTextField createFieldValueElement(final int x, final String fieldName) {
		final JRDesignTextField textField = new JRDesignTextField();

		// Make sure this field starts at the right of the previous field,
		// because JasperReports uses absolute positioning.
		textField.setY(0);
		textField.setX(x);
		textField.setWidth(width); // No data will be shown without this.

		// This only stretches vertically, but that is better than
		// nothing.
		textField.setStretchWithOverflow(true);
		textField.setHeight(height); // We must specify _some_ height.

		final JRDesignExpression expression = createFieldExpression(fieldName);

		textField.setExpression(expression);
		return textField;
	}

	/**
	 * @param x
	 * @param y
	 *            TODO
	 * @param libglomLayoutItemField
	 * @return
	 */
	private JRDesignStaticText createFieldTitleElement(final int x, final int y,
			final LayoutItem_Field libglomLayoutItemField, final boolean withColon) {
		final JRDesignStaticText textFieldColumn = new JRDesignStaticText();

		String title = StringUtils.defaultString(libglomLayoutItemField.get_title(this.localeID));

		// If the title is at the left, instead of above, we need a : to show that it's a title.
		if (withColon)
			title += ":";

		textFieldColumn.setText(title);
		textFieldColumn.setY(y);
		textFieldColumn.setX(x);
		textFieldColumn.setWidth(width); // No data will be shown without this.
		// textFieldColumn.setStretchWithOverflow(true);
		textFieldColumn.setHeight(height); // We must specify _some_ height.
		return textFieldColumn;
	}

	/**
	 * @param libglomLayoutItemField
	 * @return
	 */
	private String addField(final LayoutItem_Field libglomLayoutItemField) {
		fieldsToGet.add(libglomLayoutItemField);

		final String fieldName = libglomLayoutItemField.get_name();
		// System.out.print("fieldName=" + fieldName + "\n");

		// Tell the JasperDesign about the database field that will be in the SQL query,
		// specified later:
		final JRDesignField field = new JRDesignField();
		field.setName(fieldName); // TODO: Related fields.
		field.setValueClass(getClassTypeForGlomType(libglomLayoutItemField));

		try {
			design.addField(field);
		} catch (final JRException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return fieldName;
	}

	/**
	 * @param libglomLayoutItemField
	 * @return
	 */
	private Class<?> getClassTypeForGlomType(final LayoutItem_Field libglomLayoutItemField) {
		// Choose a suitable java class type for the SQL field:
		Class<?> klass = null;
		switch (libglomLayoutItemField.get_glom_type()) {
		case TYPE_TEXT:
			klass = java.lang.String.class;
			break;
		case TYPE_BOOLEAN:
			klass = java.lang.Boolean.class;
			break;
		case TYPE_NUMERIC:
			klass = java.lang.Double.class;
			break;
		case TYPE_DATE:
			klass = java.util.Date.class;
			break;
		case TYPE_TIME:
			klass = java.sql.Time.class;
			break;
		case TYPE_IMAGE:
			klass = java.sql.Blob.class; // TODO: This does not work.
			break;
		}
		return klass;
	}
}
