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
import org.glom.web.client.OnlineGlomLoginServiceAsync;
import org.glom.web.client.StringUtils;
import org.glom.web.client.place.DocumentLoginPlace;
import org.glom.web.client.place.HasDocumentPlace;
import org.glom.web.client.ui.View;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Ben Konrath <ben@bagu.org>
 *
 */
public abstract class HasDocumentActivity extends AbstractActivity implements View.Presenter {

	protected final ClientFactory clientFactory;
	protected final String documentID;

	/**
	 * 
	 */
	public HasDocumentActivity(final HasDocumentPlace place, final ClientFactory clientFactory) {
		super();
		this.documentID = place.getDocumentID(); // TODO: Just store the place?
		this.clientFactory = clientFactory;
	}

	@Override
	public void goTo(final Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

	/**
	 * @param eventBus
	 */
	protected void checkAuthentication(final EventBus eventBus) {
		if(StringUtils.isEmpty(documentID)) {
			//TODO: Show that no document was chosen?
			return;
		}

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
					//If the user/session is not authenticated,
					//then go to the login page, so he can try:
					goTo(new DocumentLoginPlace(documentID));
				}
			}
		};
		OnlineGlomLoginServiceAsync.Util.getInstance().isAuthenticated(documentID, isAuthCallback);
	}
	
	protected void clearView() {
	}

}