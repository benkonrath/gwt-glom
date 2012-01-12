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

import org.glom.web.client.ClientFactory;
import org.glom.web.client.OnlineGlomServiceAsync;
import org.glom.web.client.event.TableChangeEvent;
import org.glom.web.client.place.DetailsPlace;
import org.glom.web.client.place.HasSelectableTablePlace;
import org.glom.web.client.place.ListPlace;
import org.glom.web.client.ui.TableSelectionView;
import org.glom.web.client.ui.View;
import org.glom.web.shared.DocumentInfo;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 *
 */
public class TableSelectionActivity extends AbstractActivity implements View.Presenter {
	private final ClientFactory clientFactory;
	private String documentID;
	private String documentTitle;
	private String tableName;
	private HandlerRegistration changeHandlerRegistration = null;

	// This activity isn't properly configured until the List or Details Place is set with the appropriate methods
	public TableSelectionActivity(final ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * Invoked by the ActivityManager to start a new Activity
	 */
	@Override
	public void start(final AcceptsOneWidget containerWidget, final EventBus eventBus) {

		final TableSelectionView tableSelectionView = clientFactory.getTableSelectionView();
		tableSelectionView.setPresenter(this);

		// For table changes with the tableSelector:
		HasChangeHandlers tableSelector = tableSelectionView.getTableSelector();
		changeHandlerRegistration = tableSelector.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(final ChangeEvent event) {
				// Fire a table change event so that other views (e.g. the details view) know about the change and can
				// update themselves.
				eventBus.fireEvent(new TableChangeEvent(tableSelectionView.getSelectedTableName()));

				// Update the browser title because there's place change and the setPlace() method will not be called.
				Window.setTitle(documentTitle + ": " + tableSelectionView.getSelectedTableTitle());
			}
		});

		// get the table names, table titles and default table index for the current document
		final AsyncCallback<DocumentInfo> callback = new AsyncCallback<DocumentInfo>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.getDocumentInfo()");
			}

			@Override
			public void onSuccess(final DocumentInfo result) {
				tableSelectionView.setTableSelection(result.getTableNames(), result.getTableTitles());

				if (tableName == null || tableName.isEmpty()) {
					tableName = result.getTableNames().get(result.getDefaultTableIndex());
				}

				tableSelectionView.setSelectedTableName(tableName);
				documentTitle = result.getTitle();
				tableSelectionView.setDocumentTitle(documentTitle);
				Window.setTitle(documentTitle + ": " + result.getTableTitles().get(result.getDefaultTableIndex()));
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getDocumentInfo(documentID, callback);

		// we're done, set the widget
		containerWidget.setWidget(tableSelectionView.asWidget());
	}

	// This method will be called before the {@link TableSelectionActivity#start(AcceptsOneWidget, EventBus)} method and
	// any time the Place changes after the start method has been called.
	public void setPlace(final HasSelectableTablePlace place) {
		documentID = place.getDocumentID();
		tableName = place.getTableName();

		final TableSelectionView tableSelectionView = clientFactory.getTableSelectionView();

		// Update the selected table if it's not correct.
		if (!tableSelectionView.getSelectedTableName().equals(tableName)) {
			tableSelectionView.setSelectedTableName(tableName);
		}

		// Update the browser title if document title has already been setup.
		if (documentTitle != null && !documentTitle.isEmpty()) {
			Window.setTitle(documentTitle + ": " + tableSelectionView.getSelectedTableTitle());
		}

		// show the 'back to list' link if we're at a DetailsPlace, hide it otherwise
		if (place instanceof DetailsPlace) {
			tableSelectionView.setBackLinkVisible(true);
			tableSelectionView.setBackLink(documentID, tableName);
		} else if (place instanceof ListPlace) {
			tableSelectionView.setBackLinkVisible(false);
		}
	}

	private void clearView() {
		clientFactory.getTableSelectionView().clear();
		if (changeHandlerRegistration != null) {
			changeHandlerRegistration.removeHandler();
			changeHandlerRegistration = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.AbstractActivity#onCancel()
	 */
	@Override
	public void onCancel() {
		clearView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.AbstractActivity#onStop()
	 */
	@Override
	public void onStop() {
		clearView();
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
