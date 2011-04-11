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
import org.glom.web.client.place.OnlineGlomPlace;
import org.glom.web.client.ui.DocumentSelectionView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class DocumentSelectionActivity extends AbstractActivity {

	// TODO inject with GIN
	private final ClientFactory clientFactory;
	private final ArrayList<OnlineGlomPlace> documentPlaces = new ArrayList<OnlineGlomPlace>();

	public DocumentSelectionActivity(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		Window.setTitle("Online Glom");
		final DocumentSelectionView documentSelectionView = clientFactory.getDocumentSelectionView();

		AsyncCallback<ArrayList<String>> callback = new AsyncCallback<ArrayList<String>>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getDocumentTitles()");
			}

			public void onSuccess(ArrayList<String> documentTitles) {
				documentSelectionView.clearHyperLinks();
				if (!documentTitles.isEmpty()) {
					for (String documentTitle : documentTitles) {
						OnlineGlomPlace place = new OnlineGlomPlace(documentTitle);
						documentPlaces.add(place);
						documentSelectionView.addHyperLink(documentTitle,
								clientFactory.getHistoryMapper().getToken(place));
					}
				} else {
					documentSelectionView
							.setErrorMessage("Could not find any Glom documents to load. Check the error log for more information.");
				}
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getDocumentTitles(callback);

		panel.setWidget(documentSelectionView.asWidget());
	}
}
