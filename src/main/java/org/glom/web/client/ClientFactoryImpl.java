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

package org.glom.web.client;

import org.glom.web.client.mvp.AppPlaceHistoryMapper;
import org.glom.web.client.ui.DemoSelectionView;
import org.glom.web.client.ui.DemoSelectionViewImpl;
import org.glom.web.client.ui.OnlineGlomView;
import org.glom.web.client.ui.OnlineGlomViewImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;

public class ClientFactoryImpl implements ClientFactory {
	private final EventBus eventBus = new SimpleEventBus();
	private final PlaceController placeController = new PlaceController(eventBus);
	private final AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
	private final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
	private final OnlineGlomView onlineGlomView = new OnlineGlomViewImpl();
	private final DemoSelectionView demoSelectionView = new DemoSelectionViewImpl();

	public EventBus getEventBus() {
		return eventBus;
	}

	public PlaceController getPlaceController() {
		return placeController;
	}

	public OnlineGlomView getOnlineGlomView() {
		return onlineGlomView;
	}

	public PlaceHistoryHandler getHistoryHandler() {
		return historyHandler;
	}

	public PlaceHistoryMapper getHistoryMapper() {
		return historyMapper;
	}

	@Override
	public DemoSelectionView getDemoSelectionView() {
		return demoSelectionView;
	}
}
