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
import org.glom.web.client.event.TableChangeEvent;
import org.glom.web.client.event.TableChangeEventHandler;
import org.glom.web.client.place.DetailsPlace;
import org.glom.web.client.ui.DetailsView;
import org.glom.web.shared.GlomField;
import org.glom.web.shared.layout.LayoutGroup;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * @author Ben Konrath <ben@bagu.org>
 */
public class DetailsActivity extends AbstractActivity implements DetailsView.Presenter {
	private final String documentID;
	private final String tableName;
	private final String primaryKey;
	private final ClientFactory clientFactory;
	private final DetailsView detailsView;

	public DetailsActivity(DetailsPlace place, ClientFactory clientFactory) {
		this.documentID = place.getDocumentID();
		this.tableName = place.getTableName();
		this.primaryKey = place.getPrimaryKeyValue();
		this.clientFactory = clientFactory;
		detailsView = clientFactory.getDetailsView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.Activity#start(com.google.gwt.user.client.ui.AcceptsOneWidget,
	 * com.google.gwt.event.shared.EventBus)
	 */
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// register this class as the presenter
		detailsView.setPresenter(this);

		// TODO here's where we should check for database authentication - see ListActivity.start() for how to do this

		// set the change handler for the table selection widget
		eventBus.addHandler(TableChangeEvent.TYPE, new TableChangeEventHandler() {
			@Override
			public void onTableChange(final TableChangeEvent event) {
				goTo(new DetailsPlace(documentID, event.getTableName(), ""));
			}
		});

		// get the layout for the DetailsView
		AsyncCallback<ArrayList<LayoutGroup>> layoutCallback = new AsyncCallback<ArrayList<LayoutGroup>>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getDetailsLayout()");
			}

			@Override
			public void onSuccess(ArrayList<LayoutGroup> result) {
				for (LayoutGroup layoutGroup : result) {
					detailsView.addLayoutGroup(layoutGroup);
				}
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getDetailsLayout(documentID, tableName, layoutCallback);

		// get the data from the server
		AsyncCallback<GlomField[]> callback = new AsyncCallback<GlomField[]>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getDetailsData()");
			}

			@Override
			public void onSuccess(GlomField[] result) {
				// FIXME there's no guarantee that the layout will be ready for this
				detailsView.setData(result);
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getDetailsData(documentID, tableName, primaryKey, callback);

		// indicate that the view is ready to be displayed
		panel.setWidget(detailsView.asWidget());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.Activity#onCancel()
	 */
	@Override
	public void onCancel() {
		detailsView.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.Activity#onStop()
	 */
	@Override
	public void onStop() {
		detailsView.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView.Presenter#goTo(com.google.gwt.place.shared.Place)
	 */
	@Override
	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

}
