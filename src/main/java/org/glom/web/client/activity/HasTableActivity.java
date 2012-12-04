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

package org.glom.web.client.activity;

import org.glom.web.client.ClientFactory;
import org.glom.web.client.OnlineGlomServiceAsync;
import org.glom.web.client.event.TableChangeEvent;
import org.glom.web.client.place.HasTablePlace;
import org.glom.web.client.ui.AuthenticationPopup;
import org.glom.web.client.ui.View;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public abstract class HasTableActivity extends AbstractActivity implements View.Presenter {

	protected final ClientFactory clientFactory;
	protected final String documentID;
	protected final String tableName;
	protected final AuthenticationPopup authenticationPopup;

	/**
	 * 
	 */
	public HasTableActivity(final HasTablePlace place, final ClientFactory clientFactory) {
		super();
		this.documentID = place.getDocumentID(); // TODO: Just store the place?
		this.tableName = place.getTableName();
		this.clientFactory = clientFactory;
		this.authenticationPopup = clientFactory.getAuthenticationPopup();
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
	
	/**
	 * @param eventBus
	 */
	protected void checkAuthentication(final EventBus eventBus) {
		// TODO this should really be it's own Place/Activity
		// check if the authentication info has been set for the document
		final AsyncCallback<Boolean> isAuthCallback = new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.isAuthenticated(): " + caught.getMessage());
			}

			@Override
			public void onSuccess(final Boolean result) {
				if (!result) {
					setUpAuthClickHandler(eventBus);
					authenticationPopup.center();
				}
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().isAuthenticated(documentID, isAuthCallback);
	}
	
	private void setUpAuthClickHandler(final EventBus eventBus) {
		authenticationPopup.setClickOkHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				authenticationPopup.setTextFieldsEnabled(false);

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
							authenticationPopup.hide();
							eventBus.fireEvent(new TableChangeEvent(clientFactory.getTableSelectionView()
									.getSelectedTableName()));
						} else {
							// If authentication failed, tell the user:
							authenticationPopup.setTextFieldsEnabled(true);
							authenticationPopup.setError();
						}
					}
				};
				OnlineGlomServiceAsync.Util.getInstance().checkAuthentication(documentID,
						authenticationPopup.getUsername(), authenticationPopup.getPassword(), callback);
			}

		});
	}

}