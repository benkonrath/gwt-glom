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
import org.glom.web.client.place.OnlineGlomPlace;
import org.glom.web.client.ui.LayoutListView;
import org.glom.web.client.ui.OnlineGlomView;
import org.glom.web.shared.GlomDocument;
import org.glom.web.shared.LayoutListTable;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class OnlineGlomActivity extends AbstractActivity implements OnlineGlomView.Presenter {

	private final String documentName;
	private String documentTitle;
	private final ClientFactory clientFactory;
	private final OnlineGlomView onlineGlomView;

	public OnlineGlomActivity(OnlineGlomPlace place, ClientFactory clientFactory) {
		this.documentName = place.getDocumentName();
		this.clientFactory = clientFactory;
		this.onlineGlomView = clientFactory.getOnlineGlomView();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {

		onlineGlomView.setTableChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				updateTable();
			}
		});

		AsyncCallback<GlomDocument> callback = new AsyncCallback<GlomDocument>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getGlomDocument()");
			}

			public void onSuccess(GlomDocument result) {
				onlineGlomView.setTableSelection(result.getTableNames(), result.getTableTitles());
				onlineGlomView.setTableSelectedIndex(result.getDefaultTableIndex());
				documentTitle = result.getTitle();
				updateTable();
			}
		};

		// make the RPC call to get the GlomDocument
		OnlineGlomServiceAsync.Util.getInstance().getGlomDocument(callback);

		onlineGlomView.setPresenter(this);
		panel.setWidget(onlineGlomView.asWidget());
	}

	protected void updateTable() {
		// set up the callback object.
		AsyncCallback<LayoutListTable> callback = new AsyncCallback<LayoutListTable>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getLayoutListTable()");
			}

			public void onSuccess(LayoutListTable result) {
				// FIXME make the ListView reusable
				onlineGlomView.setListTable(new LayoutListView(result.getColumns(), result.getNumRows()));
				onlineGlomView.setDocumentTitle(documentTitle);
			}
		};

		OnlineGlomServiceAsync.Util.getInstance().getLayoutListTable(onlineGlomView.getSelectedTable(), callback);

	}

	@Override
	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

}
