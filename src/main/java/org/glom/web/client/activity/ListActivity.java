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
import org.glom.web.client.event.TableChangeEventHandler;
import org.glom.web.client.place.ListPlace;
import org.glom.web.client.ui.AuthenticationPopup;
import org.glom.web.client.ui.ListView;
import org.glom.web.shared.layout.LayoutGroup;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ListActivity extends AbstractActivity implements ListView.Presenter {

	private final String documentTitle;
	private final ClientFactory clientFactory;
	private final ListView listView;
	private final AuthenticationPopup authenticationPopup;

	public ListActivity(ListPlace place, ClientFactory clientFactory) {
		this.documentTitle = place.getDocumentTitle();
		this.clientFactory = clientFactory;
		listView = clientFactory.getListView();
		authenticationPopup = clientFactory.getAuthenticationPopup();
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		// register this class as the presenter
		listView.setPresenter(this);

		// check if the authentication info has been set for the document
		AsyncCallback<Boolean> isAuthCallback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getGlomDocument()");
			}

			public void onSuccess(Boolean result) {
				if (!result) {
					setUpAuthClickHandler(eventBus);
					authenticationPopup.center();
				}
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().isAuthenticated(documentTitle, isAuthCallback);

		// set the change handler for the table selection widget
		eventBus.addHandler(TableChangeEvent.TYPE, new TableChangeEventHandler() {
			@Override
			public void onTableChange(final TableChangeEvent event) {
				AsyncCallback<LayoutGroup> callback = new AsyncCallback<LayoutGroup>() {
					public void onFailure(Throwable caught) {
						// FIXME: need to deal with failure
						System.out.println("AsyncCallback Failed: OnlineGlomService.getLayoutListTable()");
					}

					public void onSuccess(LayoutGroup result) {
						listView.setCellTable(documentTitle, event.getTableName(), result);
					}
				};
				OnlineGlomServiceAsync.Util.getInstance().getListLayout(documentTitle, event.getTableName(), callback);
			}
		});

		// populate the cell table with data
		final String selectedTable = clientFactory.getTableSelectionView().getSelectedTable();
		if (!selectedTable.isEmpty()) {
			// The table name has been set so we can use the selected table name to populate the cell table.
			AsyncCallback<LayoutGroup> callback = new AsyncCallback<LayoutGroup>() {
				public void onFailure(Throwable caught) {
					// FIXME: need to deal with failure
					System.out.println("AsyncCallback Failed: OnlineGlomService.getLayoutListTable()");
				}

				public void onSuccess(LayoutGroup result) {
					listView.setCellTable(documentTitle, selectedTable, result);
				}
			};
			OnlineGlomServiceAsync.Util.getInstance().getListLayout(documentTitle, selectedTable, callback);
		} else {
			// The table name has not been set so we need to fill in the cell table using the default table for the glom
			// document.
			AsyncCallback<LayoutGroup> callback = new AsyncCallback<LayoutGroup>() {
				public void onFailure(Throwable caught) {
					// FIXME: need to deal with failure
					System.out.println("AsyncCallback Failed: OnlineGlomService.getLayoutListTable()");
				}

				public void onSuccess(LayoutGroup result) {
					listView.setCellTable(documentTitle, result.getDefaultTableName(), result);
				}
			};
			OnlineGlomServiceAsync.Util.getInstance().getDefaultListLayout(documentTitle, callback);
		}

		// indicate that the view is ready to be displayed
		panel.setWidget(listView.asWidget());
	}

	private void setUpAuthClickHandler(final EventBus eventBus) {
		authenticationPopup.setClickOkHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				authenticationPopup.setTextFieldsEnabled(false);
				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						// FIXME: need to deal with failure
						System.out.println("AsyncCallback Failed: OnlineGlomService.checkAuthentication()");
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result) {
							authenticationPopup.hide();
							eventBus.fireEvent(new TableChangeEvent(clientFactory.getTableSelectionView()
									.getSelectedTable()));
						} else {
							authenticationPopup.setTextFieldsEnabled(true);
							authenticationPopup.setError();
						}
					}
				};
				OnlineGlomServiceAsync.Util.getInstance().checkAuthentication(documentTitle,
						authenticationPopup.getUsername(), authenticationPopup.getPassword(), callback);
			}

		});
	}

	private void clearView() {
		authenticationPopup.hide();
		authenticationPopup.clear();
		listView.clear();
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
	 * @see org.glom.web.client.ui.ListView.Presenter#goTo(com.google.gwt.place.shared.Place)
	 */
	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

}
