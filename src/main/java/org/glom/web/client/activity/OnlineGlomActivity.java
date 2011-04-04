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

import org.glom.web.client.ClientFactoryImpl;
import org.glom.web.client.place.OnlineGlomPlace;
import org.glom.web.client.ui.OnlineGlomView;
import org.glom.web.client.ui.OnlineGlomViewImpl;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class OnlineGlomActivity extends AbstractActivity implements OnlineGlomView.Presenter {

	private final String documentName;
	private final ClientFactoryImpl clientFactory;
	private OnlineGlomView onlineGlomView;

	public OnlineGlomActivity(OnlineGlomPlace place, ClientFactoryImpl clientFactory) {
		this.documentName = place.getDocumentName();
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// FIXME make the OnlineGlomView Widget reusable instead of
		// creating a new one every time the activity is started
		// OnlineGlomView onlineGlomView = clientFactory.getOnlineGlomView();
		OnlineGlomView onlineGlomView = new OnlineGlomViewImpl();
		onlineGlomView.setPresenter(this);
		panel.setWidget(onlineGlomView.asWidget());

	}

	@Override
	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

}
