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
import org.glom.web.client.PlaceControllerExt;
import org.glom.web.client.StringUtils;
import org.glom.web.client.event.TableChangeEvent;
import org.glom.web.client.place.HasDocumentPlace;
import org.glom.web.client.ui.DocumentLoginView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class DocumentLoginActivity extends HasDocumentActivity {

	private final DocumentLoginView view;

	public DocumentLoginActivity(final HasDocumentPlace place, final ClientFactory clientFactory) {
		super(place, clientFactory);
		this.view = clientFactory.getDocumentLoginView();
	}
	
	private void goToPrevious() {
		final PlaceController placeController = clientFactory.getPlaceController();
		if(placeController instanceof PlaceControllerExt) {
			final PlaceControllerExt ext = (PlaceControllerExt)placeController;
			ext.previous();
		} else {
			GWT.log("The PlaceController was not a PlaceControllerExt.");
		}
	}
	
	private void goToDefault() {
		final PlaceController placeController = clientFactory.getPlaceController();
		if(placeController instanceof PlaceControllerExt) {
			final PlaceControllerExt ext = (PlaceControllerExt)placeController;
			goTo(ext.getDefaultPlace());
		} else {
			GWT.log("The PlaceController was not a PlaceControllerExt.");
		}
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		if (StringUtils.isEmpty(documentID)) {
			goToPrevious();
		}

		// register this class as the presenter
		view.setPresenter(this);

		//Find out if there is any need for authentication,
		//asking for the credentials if necessary:
		checkAuthentication(eventBus);

		// indicate that the view is ready to be displayed
		panel.setWidget(view.asWidget());
	}

	@Override
	protected void clearView() {
		super.clearView();
		view.clear();
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
	
	//TODO: Remove or modify this in the base class.
	/**
	 * @param eventBus
	 */
	protected void checkAuthentication(final EventBus eventBus) {
		if(StringUtils.isEmpty(documentID)) {
			//TODO: Show that no document was chosen?
			return;
		}

		// Check if the authentication info has been set for the document
		final AsyncCallback<Boolean> isAuthCallback = new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.isAuthenticated(): " + caught.getMessage());
			}
	
			@Override
			public void onSuccess(final Boolean result) {
				if (!result) {
					//If the user is not already authenticated,
					//then attempt that:
					setUpAuthClickHandlers(eventBus);
				} else {
					// The user was already authenticated, so go to the previous (or default) page:
					goToPrevious();
					eventBus.fireEvent(new TableChangeEvent(clientFactory.getTableSelectionView()
							.getSelectedTableName()));
				}
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().isAuthenticated(documentID, isAuthCallback);
	}

	private void setUpAuthClickHandlers(final EventBus eventBus) {
		
		//The login button:
		view.setClickLoginHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				view.setTextFieldsEnabled(false);
	
				final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(final Throwable caught) {
						// TODO: create a way to notify users of asynchronous callback failures
						GWT.log("AsyncCallback Failed: OnlineGlomService.checkAuthentication(): " + caught.getMessage());
					}
	
					@Override
					public void onSuccess(final Boolean result) {
						if (result) {
							// If authentication succeeded, take us to the requested table:
							goToPrevious();
							//eventBus.fireEvent(new TableChangeEvent(clientFactory.getTableSelectionView()
							//		.getSelectedTableName()));
						} else {
							// If authentication failed, tell the user:
							view.setTextFieldsEnabled(true);
							view.setError();
						}
					}
				};
				OnlineGlomServiceAsync.Util.getInstance().checkAuthentication(documentID,
						view.getUsername(), view.getPassword(), callback);
			}
		});
		
		//The cancel button:
		view.setClickCancelHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				goToDefault();
			}
		});
	}

}
