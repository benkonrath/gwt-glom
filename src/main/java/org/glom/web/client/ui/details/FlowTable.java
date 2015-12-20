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

package org.glom.web.client.ui.details;

import java.util.ArrayList;

import org.glom.web.client.Utils;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * A container widget that implements the Glom details view flow table behaviour. Child widgets are arranged using the
 * least vertical space in the specified number of columns.
 */
public class FlowTable extends Composite {

	// Represents an item to be inserted into the FlowTable. The primary reason for this class is to cache the vertical
	// height of the widget being added to the FlowTable.
	class FlowTableItem implements IsWidget {

		Widget widget;
		int height;

		@SuppressWarnings("unused")
		private FlowTableItem() {
			// disable default constructor
		}

		FlowTableItem(final Widget widget) {
			height = Utils.getWidgetHeight(widget);
			this.widget = widget;
		}

		int getHeight() {
			return height;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.gwt.user.client.ui.IsWidget#asWidget()
		 */
		@Override
		public Widget asWidget() {
			return widget;
		}
	}

	private final ArrayList<FlowPanel> columns = new ArrayList<FlowPanel>();
	private final ArrayList<FlowTableItem> items = new ArrayList<FlowTableItem>();

	@SuppressWarnings("unused")
	private FlowTable() {
		// disable default constructor
	}

	public FlowTable(int columnCount) {
		// get the formatters
		FlexTable table = new FlexTable();
		final CellFormatter cellFormatter = table.getFlexCellFormatter();
		final ColumnFormatter columnFormatter = table.getColumnFormatter();
		final RowFormatter rowFormater = table.getRowFormatter();

		// align the Cells to the top of the row
		rowFormater.setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);

		// take up all available horizontal space and remove the border
		table.setWidth("100%");
		table.getElement().getStyle().setProperty("borderCollapse", "collapse");
		table.setBorderWidth(0);

		if (columnCount < 1) {
			columnCount = 1; // Avoid a division by zero.
		}

		// The column widths are evenly distributed amongst the number of columns with 1% padding between the columns.
		final double columnWidth = (100 - (columnCount - 1)) / columnCount;
		for (int i = 0; i < columnCount; i++) {
			// create and add a column
			final FlowPanel column = new FlowPanel();
			table.setWidget(0, i, column);

			// set the column with from the calucation above
			columnFormatter.setWidth(i, columnWidth + "%");

			// Add space between the columns.
			// Don't set the left padding on the first column.
			if (i != 0) {
				cellFormatter.getElement(0, i).getStyle().setPaddingLeft(0.5, Unit.PCT);
			}
			// Don't set the right padding on the last column.
			if (i != columnCount - 1) {
				cellFormatter.getElement(0, i).getStyle().setPaddingRight(0.5, Unit.PCT);
			}

			// TODO The style name should be placed on the column FlexTable when I add it. - Ben
			cellFormatter.addStyleName(0, i, "group-column");

			// Keep track of the columns so it can be accessed later
			columns.add(column);
		}

		initWidget(table);
	}

	/**
	 * Adds a Widget to the FlowTable. The layout of the child widgets is adjusted to minimize the vertical height of
	 * the entire FlowTable.
	 * 
	 * @param widget
	 *            widget to add to the FlowTable
	 */
	public void add(final Widget widget) {

		// keep track for the child items
		items.add(new FlowTableItem(widget));

		// Discover the total amount of minimum space needed by this container widget, by examining its child widgets,
		// by examining every possible sequential arrangement of the widgets in this fixed number of columns:
		final int minColumnHeight = getMinimumColumnHeight(0, columns.size()); // This calls itself recursively.

		// Rearrange the widgets taking the newly added widget into account.
		int currentColumnIndex = 0;
		int currentColumnHeight = 0;
		FlowPanel currentColumn = columns.get(currentColumnIndex);
		for (final FlowTableItem item : items) {
			if (currentColumnHeight + item.getHeight() > minColumnHeight) {
				// Ensure that we never try to add widgets to an existing column. This shouldn't happen so it's just a
				// precaution. TODO: log a message if columnNumber is greater than columns.size()
				if (currentColumnIndex < columns.size() - 1) {
					currentColumn = columns.get(++currentColumnIndex);
					currentColumnHeight = 0;
				}
			}
			currentColumn.add(item.asWidget()); // adding the widget to the column removes it from its current container
			currentColumnHeight += item.getHeight();
		}
	}

	/*
	 * Discover how best (least column height) to arrange these widgets in these columns, keeping them in sequence, and
	 * then say how high the columns must be.
	 * 
	 * This method was ported from the FlowTable class of Glom.
	 */
	private int getMinimumColumnHeight(final int startWidget, final int columnCount) {

		if (columnCount == 1) {
			// Just add the heights together:
			final int widgetsCount = items.size() - startWidget;
			return getColumnHeight(startWidget, widgetsCount);

		} else {
			// Try each combination of widgets in the first column, combined with the the other combinations in the
			// following columns:
			int minimumColumnHeight = 0;
			boolean atLeastOneCombinationChecked = false;

			final int countItemsRemaining = items.size() - startWidget;

			for (int firstColumnWidgetsCount = 1; firstColumnWidgetsCount <= countItemsRemaining; firstColumnWidgetsCount++) {
				final int firstColumnHeight = getColumnHeight(startWidget, firstColumnWidgetsCount);
				int minimumColumnHeightSoFar = firstColumnHeight;
				final int othersColumnStartWidget = startWidget + firstColumnWidgetsCount;

				// Call this function recursively to get the minimum column height in the other columns, when these
				// widgets are in the first column:
				int minimumColumnHeightNextColumns = 0;
				if (othersColumnStartWidget < items.size()) {
					minimumColumnHeightNextColumns = getMinimumColumnHeight(othersColumnStartWidget, columnCount - 1);
					minimumColumnHeightSoFar = Math.max(firstColumnHeight, minimumColumnHeightNextColumns);
				}

				// See whether this is better than the last one:
				if (atLeastOneCombinationChecked) {
					if (minimumColumnHeightSoFar < minimumColumnHeight) {
						minimumColumnHeight = minimumColumnHeightSoFar;
					}
				} else {
					minimumColumnHeight = minimumColumnHeightSoFar;
					atLeastOneCombinationChecked = true;
				}
			}

			return minimumColumnHeight;
		}
	}

	private int getColumnHeight(final int startWidget, final int widgetCount) {
		// Just add the heights together:
		int columnHeight = 0;
		for (int i = startWidget; i < (startWidget + widgetCount); i++) {
			final FlowTableItem item = items.get(i);
			final int itemHeight = item.getHeight();
			columnHeight += itemHeight;
		}
		return columnHeight;
	}

}
