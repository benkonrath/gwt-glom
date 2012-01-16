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

package org.glom.web.client.activity;

import java.util.ArrayList;

import org.glom.web.client.ClientFactory;
import org.glom.web.client.OnlineGlomServiceAsync;
import org.glom.web.client.Utils;
import org.glom.web.client.event.QuickFindChangeEvent;
import org.glom.web.client.event.QuickFindChangeEventHandler;
import org.glom.web.client.event.TableChangeEvent;
import org.glom.web.client.event.TableChangeEventHandler;
import org.glom.web.client.place.DetailsPlace;
import org.glom.web.client.place.DocumentSelectionPlace;
import org.glom.web.client.place.ListPlace;
import org.glom.web.client.ui.DetailsView;
import org.glom.web.client.ui.View;
import org.glom.web.client.ui.cell.NavigationButtonCell;
import org.glom.web.client.ui.details.DetailsCell;
import org.glom.web.client.ui.details.Portal;
import org.glom.web.client.ui.details.RelatedListTable;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.DetailsLayoutAndData;
import org.glom.web.shared.NavigationRecord;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItemField;
import org.glom.web.shared.layout.LayoutItemPortal;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 *
 */
public class DetailsActivity extends AbstractActivity implements View.Presenter {
	/*
	 * Cell renderer for the related list open buttons. Normally this wouldn't be in an Activity class but since it's
	 * making a call to the server it makes sense for it to be here.
	 */
	private class RelatedListNavigationButtonCell extends NavigationButtonCell {

		private final String relationshipName;

		public RelatedListNavigationButtonCell(final String relationshipName) {
			this.relationshipName = relationshipName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.google.gwt.cell.client.ButtonCell#onEnterKeyDown(com.google.gwt.cell.client.Cell.Context,
		 * com.google.gwt.dom.client.Element, java.lang.String, com.google.gwt.dom.client.NativeEvent,
		 * com.google.gwt.cell.client.ValueUpdater)
		 */
		@Override
		protected void onEnterKeyDown(final Context context, final Element parent, final String value,
				final NativeEvent event, final ValueUpdater<String> valueUpdater) {
			final AsyncCallback<NavigationRecord> callback = new AsyncCallback<NavigationRecord>() {
				@Override
				public void onFailure(final Throwable caught) {
					// TODO: create a way to notify users of asynchronous callback failures
					GWT.log("AsyncCallback Failed: OnlineGlomService.getSuitableRecordToViewDetails()");
				}

				@Override
				public void onSuccess(final NavigationRecord result) {
					processNavigation(result.getTableName(), result.getPrimaryKeyValue());
				}

			};
			OnlineGlomServiceAsync.Util.getInstance().getSuitableRecordToViewDetails(documentID, tableName,
					relationshipName, (TypedDataItem) context.getKey(), callback);
		}
	}

	private final String documentID;
	private final String tableName;
	private TypedDataItem primaryKeyValue;
	private final String localeID;
	private final ClientFactory clientFactory;
	private final DetailsView detailsView;
	ArrayList<DetailsCell> detailsCells;
	ArrayList<Portal> portals;

	public DetailsActivity(final DetailsPlace place, final ClientFactory clientFactory) {
		this.documentID = place.getDocumentID();
		this.tableName = place.getTableName();
		this.primaryKeyValue = place.getPrimaryKeyValue();
		this.localeID = place.getLocaleID();
		this.clientFactory = clientFactory;
		detailsView = clientFactory.getDetailsView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.Activity#start(com.google.gwt.user.client.ui.AcceptsOneWidget,
	 * com.google.gwt.event.shared.EventBus)
	 */
	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		if (documentID.isEmpty())
			goTo(new DocumentSelectionPlace());

		// register this class as the presenter
		detailsView.setPresenter(this);

		// TODO here's where we should check for database authentication - see ListActivity.start() for how to do this

		// set the change handler for the table selection widget
		eventBus.addHandler(TableChangeEvent.TYPE, new TableChangeEventHandler() {
			@Override
			public void onTableChange(final TableChangeEvent event) {
				// note the empty primary key item
				goTo(new DetailsPlace(documentID, event.getNewTableName(), localeID, new TypedDataItem()));
			}
		});

		// get the layout and data for the DetailsView
		final AsyncCallback<DetailsLayoutAndData> callback = new AsyncCallback<DetailsLayoutAndData>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.getDetailsLayoutAndData()");
			}

			@Override
			public void onSuccess(final DetailsLayoutAndData result) {
				if (result == null) {
					// The result is null only when the documentID was not found. There's nothing to display without the
					// documentID.
					goTo(new DocumentSelectionPlace());
				} else {
					// create the layout and set the data
					createLayout(result.getLayout());
					setData(result.getData());
				}
			}

		};
		OnlineGlomServiceAsync.Util.getInstance().getDetailsLayoutAndData(documentID, tableName, primaryKeyValue,
				localeID, callback);

		// set the change handler for the quickfind text widget
		eventBus.addHandler(QuickFindChangeEvent.TYPE, new QuickFindChangeEventHandler() {
			@Override
			public void onQuickFindChange(final QuickFindChangeEvent event) {
				// We switch to the List view, to show search results.
				// TODO: Show the details view if there is only one result.
				goTo(new ListPlace(documentID, tableName, localeID, event.getNewQuickFindText()));
			}
		});

		// indicate that the view is ready to be displayed
		panel.setWidget(detailsView.asWidget());
	}

	/*
	 * Create the layout.
	 */
	private void createLayout(final ArrayList<LayoutGroup> layout) {
		// add the groups
		for (final LayoutGroup layoutGroup : layout) {
			detailsView.addGroup(layoutGroup);
		}

		// save references to the DetailsCells and the Portals
		detailsCells = detailsView.getCells();
		portals = detailsView.getPortals();

		// Setup click handlers for the navigation buttons
		for (final DetailsCell detailsCell : detailsCells) {
			final LayoutItemField layoutItemField = detailsCell.getLayoutItemField();
			if (layoutItemField.getAddNavigation()) {
				detailsCell.setOpenButtonClickHandler(new ClickHandler() {
					@Override
					public void onClick(final ClickEvent event) {
						final TypedDataItem primaryKeyItem = Utils.getTypedDataItem(layoutItemField.getType(),
								detailsCell.getData());
						processNavigation(layoutItemField.getNavigationTableName(), primaryKeyItem);

					}
				});
			}
		}

	}

	/*
	 * Set the data.
	 */
	private void setData(final DataItem[] data) {

		if (data == null)
			return;

		// TODO create proper client side logging
		if (data.length != detailsCells.size())
			GWT.log("Warning: The number of data items doesn't match the number of data detailsCells.");

		for (int i = 0; i < Math.min(detailsCells.size(), data.length); i++) {
			final DetailsCell detailsCell = detailsCells.get(i);
			if (data[i] != null) {

				// set the DatailsItem
				detailsCell.setData(data[i]);

				// see if there are any related lists that need to be setup
				for (final Portal portal : portals) {
					final LayoutItemField layoutItemField = detailsCell.getLayoutItemField();
					final LayoutItemPortal layoutItemPortal = portal.getLayoutItem();

					if (layoutItemField.getName().equals(layoutItemPortal.getFromField())) {
						if (data[i] == null)
							continue;

						final TypedDataItem foreignKeyValue = Utils
								.getTypedDataItem(layoutItemField.getType(), data[i]);

						final RelatedListTable relatedListTable = new RelatedListTable(documentID, layoutItemPortal,
								foreignKeyValue, new RelatedListNavigationButtonCell(layoutItemPortal.getName()));

						if (!layoutItemPortal.getAddNavigation()
								|| layoutItemPortal.getNavigationType() == LayoutItemPortal.NavigationType.NAVIGATION_NONE) {
							relatedListTable.hideNavigationButtons();
						}
						portal.setContents(relatedListTable);

						setRowCountForRelatedListTable(relatedListTable, layoutItemPortal.getName(), foreignKeyValue);
					}
				}
			}
		}
	}

	private void refreshData() {

		// get the data for the DetailsView
		final AsyncCallback<DataItem[]> callback = new AsyncCallback<DataItem[]>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.getDetailsData()");
			}

			@Override
			public void onSuccess(final DataItem[] result) {
				setData(result);
			}
		};

		OnlineGlomServiceAsync.Util.getInstance().getDetailsData(documentID, tableName, primaryKeyValue, callback);

	}

	// sets the row count for the related list table
	private void setRowCountForRelatedListTable(final RelatedListTable relatedListTable, final String relationshipName,
			final TypedDataItem foreignKeyValue) {
		final AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.getRelatedListRowCount()");
			}

			@Override
			public void onSuccess(final Integer result) {
				if (result.intValue() <= relatedListTable.getMinNumVisibleRows()) {
					// Set the table row count to the minimum row count if the data row count is less than or equal to
					// the minimum row count. This ensures that data with fewer rows than the minimum will not create
					// indexes in the underlying CellTable that will override the rendering of the empty rows.
					relatedListTable.setRowCount(relatedListTable.getMinNumVisibleRows());
				} else {
					// Set the table row count to the data row count if it's larger than the minimum number of rows
					// visible.
					relatedListTable.setRowCount(result.intValue());
				}
			}
		};

		OnlineGlomServiceAsync.Util.getInstance().getRelatedListRowCount(documentID, tableName, relationshipName,
				foreignKeyValue, callback);
	}

	/*
	 * Process a navigation by either doing: nothing if the navigation isn't valid, refreshing the data for the current
	 * table with a new primary key, or going to a new table with a new primary key.
	 */
	private void processNavigation(final String navigationTableName, final TypedDataItem navigationPrimaryKeyValue) {

		// Ensure the new table name is valid.
		String newTableName;
		if (navigationTableName != null && !navigationTableName.isEmpty()) {
			newTableName = navigationTableName;
		} else {
			newTableName = tableName;
		}

		// Only process the navigation if there's a valid primary key value.
		if (navigationPrimaryKeyValue != null && !navigationPrimaryKeyValue.isEmpty()) {
			if (!newTableName.equals(tableName)) {
				// Go to a new DetailsPlace because the table name has changed.
				goTo(new DetailsPlace(documentID, newTableName, localeID, navigationPrimaryKeyValue));
			} else {
				// Refresh the details view with the new primary because the table name has not changed.
				primaryKeyValue = navigationPrimaryKeyValue;
				refreshData();
			}
		} else {
			// TODO notify the user that navigation isn't possible.
			// This is what Glom displays:
			// Frame_Glom::show_ok_dialog(_("No Corresponding Record Exists"),
			// _("No record with this value exists. Therefore navigation to the related record is not possible."),
			// *window, Gtk::MESSAGE_WARNING);
			// TODO: Make it more clear to the user exactly what record, what field, and what value, we are talking
			// about.
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.Activity#onCancel()
	 */
	@Override
	public void onCancel() {
		detailsView.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.Activity#onStop()
	 */
	@Override
	public void onStop() {
		detailsView.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.View.Presenter#goTo(com.google.gwt.place.shared.Place)
	 */
	@Override
	public void goTo(final Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

}
