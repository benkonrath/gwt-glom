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
import net.sf.jasperreports.engine.design.JRDesignQuery;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
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
import org.glom.libglom.Relationship;
import org.glom.libglom.SortClause;
import org.glom.libglom.SqlBuilder;
import org.glom.libglom.SqlExpr;
import org.glom.libglom.Value;

/**
 * @author Murray Cumming <murrayc@openimus.com>
 * 
 */
public class ReportGenerator {

	final int height = 30;
	LayoutFieldVector fieldsToGet = new LayoutFieldVector();

	/**
	 * @param tableName
	 * @param reportName
	 * @param configuredDoc
	 * @param layout_group
	 * @return
	 */
	public String generateReport(final Document document, final String tableName, final String reportName,
			final Connection connection, final org.glom.libglom.LayoutGroup layout_group) {

		final JasperDesign design = new JasperDesign();
		design.setName(reportName); // TODO: Actually, we want the title.

		final JRDesignBand titleBand = new JRDesignBand();
		titleBand.setHeight(height);
		final JRDesignStaticText staticTitle = new JRDesignStaticText();
		staticTitle.setText("debug: test report title text");
		titleBand.addElement(staticTitle);
		design.setTitle(titleBand);

		final JRDesignBand detailBand = new JRDesignBand();
		detailBand.setHeight(height + 20);

		fieldsToGet = new LayoutFieldVector();
		final int x = 0;
		addToReport(layout_group, design, detailBand, x);

		((JRDesignSection) design.getDetailSection()).addBand(detailBand);

		// Later versions of libglom actually return an empty SqlExpr when quickFindValue is empty,
		// but let's be sure:
		final String quickFind = ""; // TODO
		final SortClause sortClause = new SortClause(); // TODO
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
		query.setText(sqlQuery); // TODO: quickfind and sort clause.
		design.setQuery(query);

		JasperReport report;
		try {
			report = JasperCompileManager.compileReport(design);
		} catch (final JRException ex) {
			ex.printStackTrace();
			return "Failed to Generate HTML: compileReport() failed.";
		}

		JasperPrint print;
		try {
			final HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("ReportTitle", reportName); // TODO: Use the title, not the name.
			print = JasperFillManager.fillReport(report, parameters, connection);
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
	 * @param design
	 * @param height
	 * @param detailBand
	 * @param x
	 */
	private int addToReport(final org.glom.libglom.LayoutGroup layout_group, final JasperDesign design,
			final JRDesignBand detailBand, int x) {
		final LayoutItemVector layoutItemsVec = layout_group.get_items();
		final int numItems = Utils.safeLongToInt(layoutItemsVec.size());
		for (int i = 0; i < numItems; i++) {
			final org.glom.libglom.LayoutItem libglomLayoutItem = layoutItemsVec.get(i);

			final LayoutGroup libglomLayoutGroup = LayoutGroup.cast_dynamic(libglomLayoutItem);
			final LayoutItem_Field libglomLayoutItemField = LayoutItem_Field.cast_dynamic(libglomLayoutItem);
			if (libglomLayoutItemField != null) {
				fieldsToGet.add(libglomLayoutItemField);

				final String fieldName = libglomLayoutItemField.get_name();
				// System.out.print("fieldName=" + fieldName + "\n");

				// Tell the JasperDesign about the database field that will be in the SQL query,
				// specified later:
				final JRDesignField field = new JRDesignField();
				field.setName(fieldName); // TODO: Related fields.

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
				field.setValueClass(klass);

				try {
					design.addField(field);
				} catch (final JRException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				// Tell the JasperDesign to show an instance of the field:
				final JRDesignTextField textField = new JRDesignTextField();

				// Make sure this field starts at the right of the previous field,
				// because JasperReports uses absolute positioning.
				textField.setY(0);
				textField.setX(x);

				// An arbitrary width, because we must specify _some_ width:
				final int width = 100; // Points, as specified later.
				textField.setWidth(width); // No data will be shown without this.
				x += width;

				// This only stretches vertically, but that is better than
				// nothing.
				textField.setStretchWithOverflow(true);
				textField.setHeight(height); // We must specify _some_ height.

				// TODO: Where is this format documented?
				final JRDesignExpression expression = new JRDesignExpression();
				expression.setText("$F{" + fieldName + "}");

				textField.setExpression(expression);
				detailBand.addElement(textField);
			} else if (libglomLayoutGroup != null) {
				// Recurse into sub-groups:
				x = addToReport(libglomLayoutGroup, design, detailBand, x);
			}
		}

		return x;
	}
}
