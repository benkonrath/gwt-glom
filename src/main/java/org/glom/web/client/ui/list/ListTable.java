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

package org.glom.web.client.ui.list;

import java.util.ArrayList;

import org.glom.web.client.Utils;
import org.glom.web.client.ui.cell.BooleanCell;
import org.glom.web.client.ui.cell.NumericCell;
import org.glom.web.client.ui.cell.OpenButtonCell;
import org.glom.web.client.ui.cell.TextCell;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.GlomNumericFormat;
import org.glom.web.shared.layout.Formatting;
import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItem;
import org.glom.web.shared.layout.LayoutItemField;
import org.glom.web.shared.layout.LayoutItemField.GlomFieldType;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.ProvidesKey;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public abstract class ListTable extends Composite {

	final private FlowPanel mainPanel = new FlowPanel();
	final private SimplePager pager = new SimplePager(SimplePager.TextLocation.CENTER);
	protected String documentID;
	protected String tableName;
	protected CellTable<DataItem[]> cellTable;

	abstract protected AbstractDataProvider<DataItem[]> getDataProvider();

	@SuppressWarnings("unused")
	private ListTable() {
		// disable default constructor
	}

	public ListTable(String documentID) {
		this.documentID = documentID;
	}

	public void createCellTable(LayoutGroup layoutGroup, int numVisibleRows) {

		tableName = layoutGroup.getTableName();
		ArrayList<LayoutItem> layoutItems = layoutGroup.getItems();

		final int primaryKeyIndex = layoutGroup.getPrimaryKeyIndex();
		LayoutItemField primaryKeyLayoutItem = (LayoutItemField) layoutItems.get(primaryKeyIndex);
		final GlomFieldType primaryKeyFieldType = primaryKeyLayoutItem.getType();
		ProvidesKey<DataItem[]> keyProvider = new ProvidesKey<DataItem[]>() {
			@Override
			public Object getKey(DataItem[] row) {
				if (row.length == 1 && row[0] == null)
					// an empty row
					return null;
				return Utils.getKeyValueStringForQuery(primaryKeyFieldType, row[primaryKeyIndex]);
			}
		};

		// create the CellTable with the requested number of rows and the key provider
		cellTable = new CellTable<DataItem[]>(numVisibleRows, keyProvider);
		cellTable.setStyleName("data-list");

		// add columns to the CellTable and deal with the case of the hidden primary key
		int numItems = layoutGroup.hasHiddenPrimaryKey() ? layoutItems.size() - 1 : layoutItems.size();
		for (int i = 0; i < numItems; i++) {
			LayoutItem layoutItem = layoutItems.get(i);

			// only add columns for LayoutItemField types
			if (layoutItem instanceof LayoutItemField) {
				addColumn((LayoutItemField) layoutItem);
			}

		}

		// create and set the data provider
		AbstractDataProvider<DataItem[]> dataProvider = getDataProvider();
		dataProvider.addDataDisplay(cellTable);

		// add an AsyncHandler to activate sorting for the data provider
		cellTable.addColumnSortHandler(new AsyncHandler(cellTable));

		// pack the widgets into the container
		pager.setDisplay(cellTable);
		mainPanel.add(cellTable);
		mainPanel.add(pager);

		// initialize composite widget
		initWidget(mainPanel);
	}

	public void addColumn(final LayoutItemField layoutItemField) {
		// Setup the default alignment of the column.
		HorizontalAlignmentConstant columnAlignment;
		Formatting formatting = layoutItemField.getFormatting();
		switch (formatting.getHorizontalAlignment()) {
		case HORIZONTAL_ALIGNMENT_LEFT:
			columnAlignment = HasHorizontalAlignment.ALIGN_LEFT;
			break;
		case HORIZONTAL_ALIGNMENT_RIGHT:
			columnAlignment = HasHorizontalAlignment.ALIGN_RIGHT;
			break;
		case HORIZONTAL_ALIGNMENT_AUTO:
		default:
			columnAlignment = HasHorizontalAlignment.ALIGN_DEFAULT;
			break;
		}

		// create a new column
		Column<DataItem[], ?> column = null;
		final int j = cellTable.getColumnCount();
		switch (layoutItemField.getType()) {

		case TYPE_BOOLEAN:
			column = new Column<DataItem[], Boolean>(new BooleanCell()) {
				@Override
				public Boolean getValue(DataItem[] row) {
					if (row.length == 1 && row[0] == null)
						// an empty row
						return null;
					return row[j].getBoolean();
				}
			};
			// override the configured horizontal alignment
			columnAlignment = HasHorizontalAlignment.ALIGN_CENTER;
			break;

		case TYPE_NUMERIC:
			GlomNumericFormat glomNumericFormat = formatting.getGlomNumericFormat();
			NumberFormat gwtNumberFormat = Utils.getNumberFormat(glomNumericFormat);
			column = new Column<DataItem[], Double>(new NumericCell(formatting.getTextFormatColourForeground(),
					formatting.getTextFormatColourBackground(), gwtNumberFormat,
					glomNumericFormat.getUseAltForegroundColourForNegatives())) {
				@Override
				public Double getValue(DataItem[] row) {
					if (row.length == 1 && row[0] == null)
						// an empty row
						return null;
					return row[j].getNumber();
				}
			};
			break;

		default:
			// use a text rendering cell for types we don't know about but log an error
			// TODO log error here
		case TYPE_DATE:
		case TYPE_IMAGE:
		case TYPE_INVALID:
		case TYPE_TIME:
		case TYPE_TEXT:
			column = new Column<DataItem[], String>(new TextCell(formatting.getTextFormatColourForeground(),
					formatting.getTextFormatColourBackground())) {
				@Override
				public String getValue(DataItem[] row) {
					if (row.length == 1 && row[0] == null)
						// an empty row
						return null;
					return row[j].getText();
				}
			};
			break;
		}

		// set column properties and add to cell cellTable
		column.setHorizontalAlignment(columnAlignment);
		column.setSortable(true);
		cellTable.addColumn(column, new SafeHtmlHeader(SafeHtmlUtils.fromString(layoutItemField.getTitle())));
	}

	public void addOpenButtonColumn(final String openButtonLabel, OpenButtonCell openButtonCell) {

		Column<DataItem[], String> openButtonColumn = new Column<DataItem[], String>(openButtonCell) {
			@Override
			public String getValue(DataItem[] row) {
				if (row.length == 1 && row[0] == null)
					// an empty row
					return null;
				return openButtonLabel;
			}
		};

		// the style name for the details column is set on the col element
		cellTable.addColumnStyleName(cellTable.getColumnCount() - 1, "details");

		// Firefox, Chrome, and Safari only support the span and width attributes of the col element so we need to set
		// the alignment with code
		openButtonColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		cellTable.addColumn(openButtonColumn, "");

	}

	/**
	 * Sets the row count for the pager.
	 */
	public void setRowCount(int rowCount) {
		cellTable.setRowCount(rowCount);
	}

	/**
	 * Gets the minimum number of rows the should be displayed. Empty rows will be added when the query returns fewer
	 * rows than this minimum.
	 * 
	 * @return The minimum number of rows that should be displayed.
	 */
	public abstract int getMinNumVisibleRows();

}
