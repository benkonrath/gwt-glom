/*
 * Copyright (C) 2010, 2011 Openismus GmbH
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

package org.glom.web.client.ui;

import java.util.ArrayList;

import org.glom.web.client.OnlineGlomServiceAsync;
import org.glom.web.client.place.DetailsPlace;
import org.glom.web.shared.GlomField;
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
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;

public class ListViewImpl extends Composite implements ListView {

	private class GlomTextCell extends AbstractCell<GlomField> {

		// The SafeHtml class is used to escape strings to avoid XSS attacks. This is not strictly
		// necessary because the values aren't coming from a user but I'm using it anyway as a reminder.
		@Override
		public void render(Context context, GlomField value, SafeHtmlBuilder sb) {
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

	final private VerticalPanel vPanel = new VerticalPanel();
	final private SimplePager pager = new SimplePager(SimplePager.TextLocation.CENTER);
	private Presenter presenter;

	public ListViewImpl() {
		initWidget(vPanel);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setCellTable(final String documentID, final String tableName, LayoutGroup layoutGroup) {
		// This is not really in the MVP style but there are issues creating a re-usable CellTable with methods like
		// setColumnTitles(), setNumRows() etc. The biggest problem is that the column objects (new Column<GlomField[],
		// GlomField>(new GlomTextCell())) aren't destroyed when the column is removed from the CellTable and
		// IndexOutOfBounds exceptions are encountered with invalid array indexes trying access the data in this line:
		// return object[j]. There's probably a workaround that could be done to fix this but I'm leaving it until
		// there's a reason to fix it (performance, ease of testing, alternate implementation or otherwise).

		vPanel.clear();

		final int primaryKeyIndex = layoutGroup.getPrimaryKeyIndex();
		ProvidesKey<GlomField[]> keyProvider = new ProvidesKey<GlomField[]>() {
			@Override
			public Object getKey(GlomField[] item) {
				return item[primaryKeyIndex].getText();
			}
		};

		final CellTable<GlomField[]> table = new CellTable<GlomField[]>(20, keyProvider);

		AsyncDataProvider<GlomField[]> dataProvider = new AsyncDataProvider<GlomField[]>() {
			@Override
			@SuppressWarnings("unchecked")
			protected void onRangeChanged(HasData<GlomField[]> display) {
				// setup the callback object
				final Range range = display.getVisibleRange();
				final int start = range.getStart();
				AsyncCallback<ArrayList<GlomField[]>> callback = new AsyncCallback<ArrayList<GlomField[]>>() {
					public void onFailure(Throwable caught) {
						// FIXME: need to deal with failure
						System.out.println("AsyncCallback Failed: OnlineGlomService.getTableData()");
					}

					public void onSuccess(ArrayList<GlomField[]> result) {
						updateRowData(start, result);
					}
				};

				// get data from the server
				ColumnSortList colSortList = table.getColumnSortList();
				if (colSortList.size() > 0) {
					// ColumnSortEvent has been requested by the user
					ColumnSortInfo info = colSortList.get(0);
					OnlineGlomServiceAsync.Util.getInstance().getSortedListData(documentID, tableName, start,
							range.getLength(), table.getColumnIndex((Column<GlomField[], ?>) info.getColumn()),
							info.isAscending(), callback);
				} else {
					OnlineGlomServiceAsync.Util.getInstance().getListData(documentID, tableName, start,
							range.getLength(), callback);
				}
			}
		};

		dataProvider.addDataDisplay(table);

		// create instances of GlomFieldColumn to retrieve the GlomField objects
		ArrayList<LayoutItem> layoutItems = layoutGroup.getItems();
		int numItems = layoutGroup.hasHiddenPrimaryKey() ? layoutItems.size() - 1 : layoutItems.size();
		for (int i = 0; i < numItems; i++) {
			LayoutItem layoutItem = layoutItems.get(i);

			// only create columns for LayoutItemField types
			if (!(layoutItem instanceof LayoutItemField)) {
				continue;
			}
			LayoutItemField layoutItemField = (LayoutItemField) layoutItem;

			// create a new column
			Column<GlomField[], ?> column = null;
			final int j = new Integer(i);

			switch (layoutItemField.getType()) {
			case TYPE_BOOLEAN:
				// The onBrowserEvent method is overridden to ensure the user can't toggle the checkbox. We'll probably
				// be able to use a CheckboxCell directly when we add support for editing.
				column = new Column<GlomField[], Boolean>(new CheckboxCell(false, false) {
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
					public Boolean getValue(GlomField[] object) {
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
				column = new Column<GlomField[], GlomField>(new GlomTextCell()) {
					@Override
					public GlomField getValue(GlomField[] object) {
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

			// set column properties and add to cell table
			column.setSortable(true);
			table.addColumn(column, new SafeHtmlHeader(SafeHtmlUtils.fromString(layoutItemField.getTitle())));
		}

		Column<GlomField[], String> detailsColumn = new Column<GlomField[], String>(new ButtonCell() {
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
				presenter.goTo(new DetailsPlace(documentID, (String) context.getKey()));
			}

		}) {
			@Override
			public String getValue(GlomField[] object) {
				return "Details";
			}
		};

		table.addColumn(detailsColumn, "");

		// set row count which is needed for paging
		table.setRowCount(layoutGroup.getExpectedResultSize());

		// add an AsyncHandler to activate sorting for the AsyncDataProvider that was created above
		table.addColumnSortHandler(new AsyncHandler(table));

		pager.setDisplay(table);
		vPanel.add(table);
		vPanel.add(pager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.ListView#clear()
	 */
	@Override
	public void clear() {
		vPanel.clear();
	}

}
