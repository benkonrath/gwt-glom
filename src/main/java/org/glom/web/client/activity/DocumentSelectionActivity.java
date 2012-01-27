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
import org.glom.web.client.ui.DocumentSelectionView;
import org.glom.web.client.ui.View;
import org.glom.web.shared.Documents;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class DocumentSelectionActivity extends AbstractActivity implements View.Presenter {

	// TODO inject with GIN
	private final ClientFactory clientFactory;
	private final DocumentSelectionView documentSelectionView;

	public DocumentSelectionActivity(final ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		this.documentSelectionView = clientFactory.getDocumentSelectionView();
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		Window.setTitle("Online Glom");

		documentSelectionView.setPresenter(this);

		final AsyncCallback<Documents> callback = new AsyncCallback<Documents>() {
			@Override
			public void onFailure(final Throwable caught) {
				// Try to get an error message. Most likely this won't work but it's worth a try.
				getAndSetErrorMessage();
			}

			@Override
			public void onSuccess(final Documents documents) {
				documentSelectionView.clearHyperLinks();
				if (documents.getCount() > 0) {
					for (int i = 0; i < documents.getCount(); i++) {
						documentSelectionView.addDocumentLink(documents.getDocumentID(i), documents.getTitle(i));
						// TODO: Get default locale.
					}
				} else {
					getAndSetErrorMessage();
				}
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getDocuments(callback);

		panel.setWidget(documentSelectionView.asWidget());
	}

	@Override
	public void goTo(final Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

	private void getAndSetErrorMessage() {

		final AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onFailure(final Throwable caught) {
				documentSelectionView
						.setErrorMessage("Unable to communicate with the servlet. Check the error log for more information.");
			}

			@Override
			public void onSuccess(final String errorMessage) {
				documentSelectionView.setErrorMessage("Configuration Error: " + errorMessage);
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getConfigurationErrorMessage(callback);

	}
}