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

import org.glom.web.client.place.DetailsPlace;
import org.glom.web.client.ui.ListView;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItem;
import org.glom.web.shared.layout.LayoutItemField;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.ProvidesKey;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public abstract class ListTable extends Composite {

	private class GlomTextCell extends AbstractCell<DataItem> {

		// The SafeHtml class is used to escape strings to avoid XSS attacks. This is not strictly
		// necessary because the values aren't coming from a user but I'm using it anyway as a reminder.
		@Override
		public void render(Context context, DataItem value, SafeHtmlBuilder sb) {
			if (value == null) {
				return;
			}

			// get the foreground and background colours if they're set
			SafeHtml fgcolour = null, bgcolour = null;
			String colour = value.getFGColour();
			if (colour == null) {
				fgcolour = SafeHtmlUtils.fromSafeConstant("");
			} else {
				fgcolour = SafeHtmlUtils.fromString("color:" + colour + ";");
			}
			colour = value.getBGColour();
			if (colour == null) {
				bgcolour = SafeHtmlUtils.fromSafeConstant("");
			} else {
				bgcolour = SafeHtmlUtils.fromString("background-color:" + colour + ";");
			}

			// set the text and colours
			sb.appendHtmlConstant("<div style=\"" + fgcolour.asString() + bgcolour.asString() + "\">");
			sb.append(SafeHtmlUtils.fromString(value.getText()));
			sb.appendHtmlConstant("</div>");
		}
	}

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

		this.tableName = layoutGroup.getTableName();

		final int primaryKeyIndex = layoutGroup.getPrimaryKeyIndex();
		ProvidesKey<DataItem[]> keyProvider = new ProvidesKey<DataItem[]>() {
			@Override
			public Object getKey(DataItem[] item) {
				return item[primaryKeyIndex].getText();
			}
		};

		// create the CellTable with the requested number of rows and the key provider
		cellTable = new CellTable<DataItem[]>(numVisibleRows, keyProvider);
		cellTable.setStyleName("data-list");

		// add columns to the CellTable and deal with the case of the hidden primary key
		ArrayList<LayoutItem> layoutItems = layoutGroup.getItems();
		int numItems = layoutGroup.hasHiddenPrimaryKey() ? layoutItems.size() - 1 : layoutItems.size();
		for (int i = 0; i < numItems; i++) {
			LayoutItem layoutItem = layoutItems.get(i);

			// only add columns for LayoutItemField types
			if (layoutItem instanceof LayoutItemField) {
				addColumn((LayoutItemField) layoutItem);
			}

		}

		// set the expected row count which is needed for paging
		cellTable.setRowCount(layoutGroup.getExpectedResultSize());

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

	public void addColumn(LayoutItemField layoutItemField) {
		// create a new column
		Column<DataItem[], ?> column = null;
		final int j = cellTable.getColumnCount();

		switch (layoutItemField.getType()) {
		case TYPE_BOOLEAN:
			// The onBrowserEvent method is overridden to ensure the user can't toggle the checkbox. We'll probably
			// be able to use a CheckboxCell directly when we add support for editing.
			column = new Column<DataItem[], Boolean>(new CheckboxCell(false, false) {
				@Override
				public void onBrowserEvent(Context context, Element parent, Boolean value, NativeEvent event,
						ValueUpdater<Boolean> valueUpdater) {
					String type = event.getType();

					boolean enterPressed = "keydown".equals(type) && event.getKeyCode() == KeyCodes.KEY_ENTER;
					if ("change".equals(type) || enterPressed) {
						InputElement input = parent.getFirstChild().cast();
						input.setChecked(!input.isChecked());
					}
				}
			}) {
				@Override
				public Boolean getValue(DataItem[] object) {
					return object[j].getBoolean();
				}
			};
			// Make checkboxes centred in the column.
			column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			break;
		default:
			// use a text rendering cell for types we don't know about but log an error
			// TODO log error here
		case TYPE_DATE:
		case TYPE_IMAGE:
		case TYPE_INVALID:
		case TYPE_NUMERIC:
		case TYPE_TIME:
		case TYPE_TEXT:
			// All of these types are formatted as text in the servlet.
			column = new Column<DataItem[], DataItem>(new GlomTextCell()) {
				@Override
				public DataItem getValue(DataItem[] object) {
					return object[j];
				}
			};

			// Set the alignment of the text.
			switch (layoutItemField.getFormatting().getHorizontalAlignment()) {
			case HORIZONTAL_ALIGNMENT_LEFT:
				column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
				break;
			case HORIZONTAL_ALIGNMENT_RIGHT:
				column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
				break;
			case HORIZONTAL_ALIGNMENT_AUTO:
			default:
				// TODO: log warning, this shouldn't happen
				column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_DEFAULT);
				break;
			}
			break;
		}

		// set column properties and add to cell cellTable
		column.setSortable(true);
		cellTable.addColumn(column, new SafeHtmlHeader(SafeHtmlUtils.fromString(layoutItemField.getTitle())));
	}

	public void addOpenButtonColumnn(final ListView.Presenter presenter, final String openButtonLabel) {

		Column<DataItem[], String> openButtonColumn = new Column<DataItem[], String>(new ButtonCell() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.google.gwt.cell.client.ButtonCell#onEnterKeyDown(com.google.gwt.cell.client.Cell.Context,
			 * com.google.gwt.dom.client.Element, java.lang.String, com.google.gwt.dom.client.NativeEvent,
			 * com.google.gwt.cell.client.ValueUpdater)
			 */
			@Override
			protected void onEnterKeyDown(Context context, Element parent, String value, NativeEvent event,
					ValueUpdater<String> valueUpdater) {

				super.onEnterKeyDown(context, parent, value, event, valueUpdater);
				presenter.goTo(new DetailsPlace(documentID, tableName, (String) context.getKey()));
			}

		}) {
			@Override
			public String getValue(DataItem[] object) {
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

}
