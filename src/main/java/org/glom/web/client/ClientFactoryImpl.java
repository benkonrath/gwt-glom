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

import org.glom.web.client.place.DocumentSelectionPlace;
import org.glom.web.client.ui.DetailsView;
import org.glom.web.client.ui.DetailsViewImpl;
import org.glom.web.client.ui.DocumentLoginView;
import org.glom.web.client.ui.DocumentLoginViewImpl;
import org.glom.web.client.ui.DocumentSelectionView;
import org.glom.web.client.ui.DocumentSelectionViewImpl;
import org.glom.web.client.ui.ListView;
import org.glom.web.client.ui.ListViewImpl;
import org.glom.web.client.ui.ReportView;
import org.glom.web.client.ui.ReportViewImpl;
import org.glom.web.client.ui.TableSelectionView;
import org.glom.web.client.ui.TableSelectionViewImpl;
import org.glom.web.client.ui.UserRegisterView;
import org.glom.web.client.ui.UserRegisterViewImpl;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

public class ClientFactoryImpl implements ClientFactory {
	private final EventBus eventBus = new SimpleEventBus();
	private final PlaceController placeController = new PlaceControllerExt(eventBus, new DocumentSelectionPlace());
	private final DocumentLoginView documentLoginView = new DocumentLoginViewImpl();
	private final DocumentSelectionView documentSelectionView = new DocumentSelectionViewImpl();
	private final TableSelectionView tableSelectionView = new TableSelectionViewImpl();
	private final ListView listView = new ListViewImpl();
	private final DetailsView detailsView = new DetailsViewImpl();
	private final ReportView reportView = new ReportViewImpl();
	private final UserRegisterView userRegisterView = new UserRegisterViewImpl();

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	@Override
	public PlaceController getPlaceController() {
		return placeController;
	}

	@Override
	public DocumentLoginView getDocumentLoginView() {
		return documentLoginView;
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
	public DetailsView getDetailsView() {
		return detailsView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ClientFactory#getReportView()
	 */
	@Override
	public ReportView getReportView() {
		return reportView;
	}
	
	@Override
	public UserRegisterView getUserRegisterView() {
		return userRegisterView;
	}

}
