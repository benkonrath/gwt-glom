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
import org.glom.web.client.place.ListPlace;
import org.glom.web.client.ui.LayoutListView;
import org.glom.web.client.ui.OnlineGlomView;
import org.glom.web.shared.GlomDocument;
import org.glom.web.shared.LayoutListTable;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class OnlineGlomActivity extends AbstractActivity implements OnlineGlomView.Presenter {

	private final String documentTitle;
	private final ClientFactory clientFactory;
	private final OnlineGlomView onlineGlomView;

	public OnlineGlomActivity(ListPlace place, ClientFactory clientFactory) {
		this.documentTitle = place.getDocumentTitle();
		this.clientFactory = clientFactory;
		this.onlineGlomView = clientFactory.getOnlineGlomView();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		Window.setTitle("Online Glom - " + documentTitle);

		// check if the authentication info has been set for the document
		AsyncCallback<Boolean> isAuthCallback = new AsyncCallback<Boolean>() {
			public void onSuccess(Boolean result) {
				if (!result) {
					setUpAuthClickHandler();
					onlineGlomView.showAuthPopup();
				}
			}

			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getGlomDocument()");
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().isAuthenticated(documentTitle, isAuthCallback);

		// get some information about the current document
		AsyncCallback<GlomDocument> glomDocCallback = new AsyncCallback<GlomDocument>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getGlomDocument()");
			}

			public void onSuccess(GlomDocument result) {
				onlineGlomView.setTableSelection(result.getTableNames(), result.getTableTitles());
				onlineGlomView.setTableSelectedIndex(result.getDefaultTableIndex());
				updateTable();
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getGlomDocument(documentTitle, glomDocCallback);

		// set the change handler for the table selection widget
		onlineGlomView.setTableChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				updateTable();
			}
		});

		// register this class as the presenter
		onlineGlomView.setPresenter(this);

		// indicate that the view is ready to be displayed
		panel.setWidget(onlineGlomView.asWidget());
	}

	private void updateTable() {
		AsyncCallback<LayoutListTable> callback = new AsyncCallback<LayoutListTable>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getLayoutListTable()");
			}

			public void onSuccess(LayoutListTable result) {
				// FIXME make the ListView reusable
				onlineGlomView
						.setListTable(new LayoutListView(documentTitle, result.getColumns(), result.getNumRows()));
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getLayoutListTable(documentTitle, onlineGlomView.getSelectedTable(),
				callback);
	}

	private void setUpAuthClickHandler() {
		onlineGlomView.setAuthClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onlineGlomView.setAuthTextFieldsEnabled(false);
				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (result) {
							onlineGlomView.hideAuthPopup();
							updateTable();
						} else {
							onlineGlomView.setAuthTextFieldsEnabled(true);
							onlineGlomView.setAuthError();
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						// FIXME: need to deal with failure
						System.out.println("AsyncCallback Failed: OnlineGlomService.checkAuthentication()");
					}
				};
				OnlineGlomServiceAsync.Util.getInstance().checkAuthentication(documentTitle,
						onlineGlomView.getUsername(), onlineGlomView.getPassword(), callback);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.AbstractActivity#onStop()
	 */
	@Override
	public void onStop() {
		// clear the data in the view once the user has navigated away from this activity
		onlineGlomView.hideAuthPopup();
		onlineGlomView.clear();
	}

	// FIXME this isn't currently being used
	@Override
	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

}
