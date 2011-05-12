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

import org.glom.web.client.ui.AuthenticationPopup;
import org.glom.web.client.ui.DocumentSelectionView;
import org.glom.web.client.ui.DocumentSelectionViewImpl;
import org.glom.web.client.ui.ListView;
import org.glom.web.client.ui.ListViewImpl;
import org.glom.web.client.ui.TableSelectionView;
import org.glom.web.client.ui.TableSelectionViewImpl;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;

public class ClientFactoryImpl implements ClientFactory {
	private final EventBus eventBus = new SimpleEventBus();
	private final PlaceController placeController = new PlaceController(eventBus);
	private final DocumentSelectionView documentSelectionView = new DocumentSelectionViewImpl();
	private final TableSelectionView tableSelectionView = new TableSelectionViewImpl();
	private final ListView listView = new ListViewImpl();
	private final AuthenticationPopup authenticationPopup = new AuthenticationPopup();

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	@Override
	public PlaceController getPlaceController() {
		return placeController;
	}

	@Override
	public DocumentSelectionView getDocumentSelectionView() {
		return documentSelectionView;
	}

	@Override
	public TableSelectionView getTableSelectionView() {
		return tableSelectionView;
	}

	@Override
	public ListView getListView() {
		return listView;
	}

	@Override
	public AuthenticationPopup getAuthenticationPopup() {
		return authenticationPopup;
	}

}
