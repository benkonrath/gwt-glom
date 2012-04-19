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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

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
import org.glom.libglom.Field.glom_field_type;
import org.glom.libglom.Formatting;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutFieldVector;
import org.glom.libglom.LayoutGroup;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.LayoutItem_Field;
import org.glom.libglom.LayoutItem_GroupBy;
import org.glom.libglom.LayoutItem_VerticalGroup;
import org.glom.libglom.NumericFormat;
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

	private class Position {
		public Position(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		public Position(final Position pos) {
			this.x = pos.x;
			this.y = pos.y;
		}

		public int x = 0;
		public int y = 0;
	}

	final int height = 30; // Points, as specified later.
	// An arbitrary width, because we must specify _some_ width:
	final int width = 100; // Points, as specified later.

	LayoutFieldVector fieldsToGet = new LayoutFieldVector();
	SortClause sortClause = new SortClause();
	String localeID;

	final JasperDesign design = new JasperDesign();
	JRDesignStyle titleStyle = new JRDesignStyle();
	JRDesignStyle normalStyle = new JRDesignStyle();
	JRDesignStyle fieldTitleStyle = new JRDesignStyle();

	ReportGenerator(final String localeID) {
		this.localeID = StringUtils.defaultString(localeID);
	}

	/**
	 */
	public String generateReport(final Document document, final String tableName, final Report report,
			final Connection connection, final String quickFind) {

		final org.glom.libglom.LayoutGroup layout_group = report.get_layout_group();

		design.setName(report.get_title(localeID)); // TODO: Actually, we want the title.

		titleStyle.setName("Sans_Title");
		titleStyle.setFontName("DejaVu Sans");
		titleStyle.setFontSize(24);
		normalStyle.setName("Sans_Normal");
		normalStyle.setDefault(true);
		normalStyle.setFontName("DejaVu Sans");
		normalStyle.setFontSize(12);
		normalStyle.setBlankWhenNull(true); // Avoid "null" appearing in reports.
		fieldTitleStyle.setName("Sans_Bold");
		fieldTitleStyle.setFontName("DejaVu Sans");
		fieldTitleStyle.setFontSize(12);
		fieldTitleStyle.setBold(true);
		fieldTitleStyle.setBlankWhenNull(true); // Avoid "null" appearing in reports when this is used for a GroupBy
												// title.
		try {
			design.addStyle(titleStyle);
			design.addStyle(normalStyle);
			design.addStyle(fieldTitleStyle);
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
		addGroupToReport(layout_group, detailBand, x, headerBand, 0);

		design.setColumnHeader(headerBand);
		((JRDesignSection) design.getDetailSection()).addBand(detailBand);

		// Later versions of libglom actually return an empty SqlExpr when quickFindValue is empty,
		// but let's be sure:
		SqlExpr whereClause;
		if (StringUtils.isEmpty(quickFind)) {
			whereClause = new SqlExpr();
		} else {
			final Value quickFindValue = new Value(quickFind);
			whereClause = Glom.get_find_where_clause_quick(document, tableName, quickFindValue);
		}

		String sqlQuery = "";
		if (!fieldsToGet.isEmpty()) {
			final Relationship extraJoin = new Relationship(); // Ignored.
			final SqlBuilder builder = Glom.build_sql_select_with_where_clause(tableName, fieldsToGet, whereClause,
					extraJoin, sortClause);
			sqlQuery = Glom.sqlbuilder_get_full_query(builder);
		} else {
			Log.info("generateReport(): fieldsToGet is empty.");
		}

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
		final String html = output.toString();

		// This does not work because jasperReports does not put individual rows in separate table rows.
		// jasperReports just puts the whole thing in one table row.
		// Remove the arbitrary width and height that JasperReports forced us to specify:
		//html = html.replaceAll("position:absolute;", "");
		//html = html.replaceAll("top:(\\d*)pt;", "");
		//html = html.replaceAll("left:(\\d*)pt;", "");
		//html = html.replaceAll("height:(\\d*)pt;", "");
		//html = html.replaceAll("width:(\\d*)pt;", "");
		//html = html.replaceAll("overflow: hidden;", "");

		return html;
	}

	/**
	 * A vertical group lays the fields out vertically instead of horizontally, with titles to the left.
	 * 
	 * @param layout_group
	 * @param parentBand
	 * @param x
	 * @param fieldTitlesY
	 *            TODO
	 * @param height
	 */
	private Position addVerticalGroupToReport(final org.glom.libglom.LayoutItem_VerticalGroup layout_group,
			final JRDesignBand parentBand, final Position pos) {
		Position pos_result = new Position(pos);

		final LayoutItemVector layoutItemsVec = layout_group.get_items();
		final int numItems = Utils.safeLongToInt(layoutItemsVec.size());
		for (int i = 0; i < numItems; i++) {
			final org.glom.libglom.LayoutItem libglomLayoutItem = layoutItemsVec.get(i);

			final LayoutItem_Field libglomLayoutItemField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
			if (libglomLayoutItemField != null) {
				pos_result = addFieldToDetailBandVertical(parentBand, pos_result, libglomLayoutItemField);
				pos_result.x = pos.x;
			} else {

				// TODO: Handle other item types.

				// Recurse into sub-groups:
				// TODO: x = addGroupToReport(libglomLayoutGroup, parentBand, x, fieldTitlesBand, thisFieldTitlesY);
			}
		}

		pos_result.x += width * 2;
		return pos_result;
	}

	/**
	 * @param layout_group
	 * @param parentBand
	 * @param x
	 * @param fieldTitlesY
	 *            TODO
	 * @param height
	 */
	private Position addGroupToReport(final org.glom.libglom.LayoutGroup layout_group, final JRDesignBand parentBand,
			final int x, final JRDesignBand headerBand, final int fieldTitlesY) {

		Position pos_result = new Position(x, 0);

		/**
		 * * If this is a vertical group then we will lay the fields out vertically instead of horizontally.
		 */
		final org.glom.libglom.LayoutItem_VerticalGroup verticalGroup = LayoutItem_VerticalGroup
				.cast_dynamic(layout_group);
		if (verticalGroup != null) {
			return addVerticalGroupToReport(verticalGroup, parentBand, pos_result);
		}

		// Where we put the field titles depends on whether we are in a group-by:
		JRDesignBand fieldTitlesBand = headerBand;
		int thisFieldTitlesY = fieldTitlesY; // If they are in a group title then they must be lower.

		final LayoutItemVector layoutItemsVec = layout_group.get_items();
		final int numItems = Utils.safeLongToInt(layoutItemsVec.size());
		for (int i = 0; i < numItems; i++) {
			final org.glom.libglom.LayoutItem libglomLayoutItem = layoutItemsVec.get(i);

			final LayoutGroup libglomLayoutGroup = LayoutGroup.cast_dynamic(libglomLayoutItem);
			final LayoutItem_Field libglomLayoutItemField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
			if (libglomLayoutItemField != null) {
				pos_result = addFieldToDetailBand(parentBand, headerBand, pos_result.x, libglomLayoutItemField,
						thisFieldTitlesY, pos_result.y);
			} else if (libglomLayoutGroup != null) {
				final LayoutItem_GroupBy libglomGroupBy = LayoutItem_GroupBy.cast_dynamic(libglomLayoutGroup);
				if (libglomGroupBy != null) {
					final LayoutItem_Field fieldGroupBy = libglomGroupBy.get_field_group_by();
					if (fieldGroupBy == null)
						continue;

					final String fieldName = addField(fieldGroupBy);
					if (StringUtils.isEmpty(fieldName)) {
						continue;
					}

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
					final JRDesignExpression expression = createFieldExpression(fieldGroupBy);
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
					final LayoutGroup groupSecondaries = libglomGroupBy.get_secondary_fields();
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
				pos_result = addGroupToReport(libglomLayoutGroup, parentBand, pos_result.x, fieldTitlesBand,
						thisFieldTitlesY);
			}
		}

		return pos_result;
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
	 * @param libglomLayoutItemField
	 * @return
	 */
	private JRDesignExpression createFieldExpression(final LayoutItem_Field libglomLayoutItemField) {
		final JRDesignExpression expression = new JRDesignExpression();

		final String fieldName = libglomLayoutItemField.get_name(); // TODO: Is this enough for related fields?

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
	private Position addFieldToDetailBand(final JRDesignBand parentBand, final JRDesignBand headerBand, final int x,
			final LayoutItem_Field libglomLayoutItemField, final int fieldTitlesY, final int fieldY) {
		addField(libglomLayoutItemField);

		// Show the field title:
		final JRDesignStaticText textFieldColumn = createFieldTitleElement(new Position(x, fieldTitlesY),
				libglomLayoutItemField, false);
		textFieldColumn.setStyle(fieldTitleStyle);
		headerBand.addElement(textFieldColumn);

		// Show an instance of the field (the field value):
		final JRDesignTextField textField = createFieldValueElement(new Position(x, 0), libglomLayoutItemField);
		textField.setStyle(normalStyle);
		parentBand.addElement(textField);

		return new Position(x + width, 0);
	}

	/**
	 * @param parentBand
	 * @param x
	 * @param libglomLayoutItemField
	 * @param fieldTitlesY
	 *            TODO
	 * @return
	 */
	private Position addFieldToDetailBandVertical(final JRDesignBand parentBand, final Position pos,
			final LayoutItem_Field libglomLayoutItemField) {
		addField(libglomLayoutItemField);

		final Position pos_result = new Position(pos);

		// Make the band high enough if necessary:
		if (parentBand.getHeight() < (pos_result.y + height))
			parentBand.setHeight(pos_result.y + height + 20);

		// Show the field title:
		final JRDesignStaticText textFieldColumn = createFieldTitleElement(pos_result, libglomLayoutItemField, true);
		textFieldColumn.setStyle(fieldTitleStyle);
		parentBand.addElement(textFieldColumn);
		pos_result.x += width;

		// Show an instance of the field (the field value):
		final JRDesignTextField textField = createFieldValueElement(pos_result, libglomLayoutItemField);
		textField.setStyle(normalStyle);
		parentBand.addElement(textField);

		pos_result.x += width;

		pos_result.y += height;

		return pos_result;
	}

	private int addFieldToGroupBand(final JRDesignBand parentBand, final int x,
			final LayoutItem_Field libglomLayoutItemField) {
		addField(libglomLayoutItemField);

		final Position pos = new Position(x, 0);

		// Show the field title:
		final JRDesignStaticText textFieldColumn = createFieldTitleElement(pos, libglomLayoutItemField, true);

		// Instead, the field value will be bold, because here it is like a title.
		textFieldColumn.setStyle(normalStyle);

		parentBand.addElement(textFieldColumn);
		pos.x += width;

		// Show an instance of the field (the field value):
		final JRDesignTextField textField = createFieldValueElement(pos, libglomLayoutItemField);
		parentBand.addElement(textField);
		textField.setStyle(fieldTitleStyle);

		pos.x += width;
		return pos.x;
	}

	/**
	 * @param x
	 * @param libglomLayoutItemField
	 * @return
	 */
	private JRDesignTextField createFieldValueElement(final Position pos, final LayoutItem_Field libglomLayoutItemField) {
		final JRDesignTextField textField = new JRDesignTextField();

		// Make sure this field starts at the right of the previous field,
		// because JasperReports uses absolute positioning.
		textField.setY(pos.y);
		textField.setX(pos.x);
		textField.setWidth(width); // No data will be shown without this.

		// This only stretches vertically, but that is better than
		// nothing.
		textField.setStretchWithOverflow(true);
		textField.setHeight(height); // We must specify _some_ height.

		final JRDesignExpression expression = createFieldExpression(libglomLayoutItemField);
		textField.setExpression(expression);

		if (libglomLayoutItemField.get_glom_type() == glom_field_type.TYPE_NUMERIC) {
			// Numeric formatting:
			final Formatting formatting = libglomLayoutItemField.get_formatting_used();
			final NumericFormat numericFormat = formatting.get_numeric_format();

			final DecimalFormat format = new DecimalFormat();
			format.setMaximumFractionDigits((int) numericFormat.get_decimal_places());
			format.setGroupingUsed(numericFormat.get_use_thousands_separator());

			// TODO: Use numericFormat.get_currency_symbol(), possibly via format.setCurrency().
			textField.setPattern(format.toPattern());
		} else if (libglomLayoutItemField.get_glom_type() == glom_field_type.TYPE_DATE) {
			// Date formatting
			// TODO: Use a 4-digit-year short form, somehow.
			try //We use a try block because getDateInstance() is not guaranteed to return a SimpleDateFormat.
			{
			  final SimpleDateFormat format = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT, Locale.ROOT);

			  textField.setPattern(format.toPattern());
			} catch (final Exception ex) {
				Log.info("ReportGenerator: The cast of SimpleDateFormat failed.");
			}
		} else if (libglomLayoutItemField.get_glom_type() == glom_field_type.TYPE_TIME) {
			// Time formatting
			try //We use a try block because getDateInstance() is not guaranteed to return a SimpleDateFormat.
			{
				final SimpleDateFormat format = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.SHORT, Locale.ROOT);

				textField.setPattern(format.toPattern());
			} catch (final Exception ex) {
				Log.info("ReportGenerator: The cast of SimpleDateFormat failed.");
			}
	}

		return textField;
	}

	/**
	 * @param x
	 * @param y
	 *            TODO
	 * @param libglomLayoutItemField
	 * @return
	 */
	private JRDesignStaticText createFieldTitleElement(final Position pos,
			final LayoutItem_Field libglomLayoutItemField, final boolean withColon) {
		final JRDesignStaticText textFieldColumn = new JRDesignStaticText();

		String title = StringUtils.defaultString(libglomLayoutItemField.get_title(this.localeID));

		// If the title is at the left, instead of above, we need a : to show that it's a title.
		if (withColon)
			title += ":";

		textFieldColumn.setText(title);
		textFieldColumn.setY(pos.y);
		textFieldColumn.setX(pos.x);
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

		final String fieldName = libglomLayoutItemField.get_name(); // TODO: Is this enough for related fields?

		// Avoid an unnamed field:
		if (StringUtils.isEmpty(fieldName)) {
			Log.info("addField(): Ignoring LayoutItem_Field with no field name");
			return fieldName;
		}

		// Avoid adding duplicate fields,
		// because JasperDesign.addField() throws a "Duplicate declaration of field" exception.
		for (int i = 0; i < fieldsToGet.size(); ++i) {
			final LayoutItem_Field thisField = fieldsToGet.get(i);
			if (thisField.equals(libglomLayoutItemField))
				return fieldName;
		}

		fieldsToGet.add(libglomLayoutItemField);

		// System.out.print("fieldName=" + fieldName + "\n");

		// Tell the JasperDesign about the database field that will be in the SQL query,
		// specified later:
		final JRDesignField field = new JRDesignField();
		field.setName(fieldName); // TODO: Related fields.

		final Class<?> klass = getClassTypeForGlomType(libglomLayoutItemField);
		if (klass != null) {
			field.setValueClass(klass);
		} else {
			Log.info("getClassTypeForGlomType() returned null");
		}

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

		final glom_field_type glom_type = libglomLayoutItemField.get_glom_type();
		switch (glom_type) {
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
		case TYPE_INVALID:
			Log.info("getClassTypeForGlomType() returning null for TYPE_INVALID glom type. Field name="
					+ libglomLayoutItemField.get_layout_display_name());
		default:
			Log.info("getClassTypeForGlomType() returning null for glom type: " + glom_type + ". Field name="
					+ libglomLayoutItemField.get_layout_display_name());
			break;
		}
		return klass;
	}
}
