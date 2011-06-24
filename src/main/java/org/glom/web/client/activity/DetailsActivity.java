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
import org.glom.web.client.place.DetailsPlace;
import org.glom.web.client.ui.DetailsView;
import org.glom.web.shared.GlomField;
import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItem;
import org.glom.web.shared.layout.LayoutItemField;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * @author Ben Konrath <ben@bagu.org>
 */
public class DetailsActivity extends AbstractActivity implements DetailsView.Presenter {
	private final String documentTitle;
	private final String primaryKey;
	private final ClientFactory clientFactory;
	private final DetailsView detailsView;

	public DetailsActivity(DetailsPlace place, ClientFactory clientFactory) {
		this.documentTitle = place.getDocumentTitle();
		this.primaryKey = place.getPrimaryKey();
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

		// get the layout for the DetailsView
		final String selectedTable = clientFactory.getTableSelectionView().getSelectedTable();
		if (!selectedTable.isEmpty()) {
			// The table name has been set so we can use the selected table name to populate the cell table.
			AsyncCallback<ArrayList<LayoutGroup>> callback = new AsyncCallback<ArrayList<LayoutGroup>>() {
				public void onFailure(Throwable caught) {
					// FIXME: need to deal with failure
					System.out.println("AsyncCallback Failed: OnlineGlomService.getDetailsLayout()");
				}

				@Override
				public void onSuccess(ArrayList<LayoutGroup> result) {
					addLayoutGroups(result);
				}
			};
			OnlineGlomServiceAsync.Util.getInstance().getDetailsLayout(documentTitle, selectedTable, callback);
		} else {
			// The table name has not been set so we need to fill in the details layout using the default table for the
			// glom document.
			AsyncCallback<ArrayList<LayoutGroup>> callback = new AsyncCallback<ArrayList<LayoutGroup>>() {
				public void onFailure(Throwable caught) {
					// FIXME: need to deal with failure
					System.out.println("AsyncCallback Failed: OnlineGlomService.getDefaultDetailsLayout()");
				}

				@Override
				public void onSuccess(ArrayList<LayoutGroup> result) {
					addLayoutGroups(result);
				}
			};
			OnlineGlomServiceAsync.Util.getInstance().getDefaultDetailsLayout(documentTitle, callback);
		}

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
		// FIXME Need a getDefaultDetailsData so that we can grab data from the default list when a table is not
		// specified in the URL (which it's not now). This affects starting OnlineGlom from a bookmarked or shared
		// URL to the DetailsView. We'll also want to add a table URL variable and perform validation of the URL
		// variables.
		OnlineGlomServiceAsync.Util.getInstance().getDetailsData(documentTitle, selectedTable, primaryKey, callback);

		// indicate that the view is ready to be displayed
		panel.setWidget(detailsView.asWidget());
	}

	private void addLayoutGroups(ArrayList<LayoutGroup> layoutGroups) {
		for (LayoutGroup layoutGroup : layoutGroups) {
			addLayoutGroup(layoutGroup, "");
		}
	}

	/*
	 * This is just a temporary method for creating a basic indented layout without the flowtable/spreadtable that Glom
	 * has.
	 */
	private void addLayoutGroup(LayoutGroup layoutGroup, String indent) {
		if (layoutGroup == null)
			return;

		// look at each child item
		ArrayList<LayoutItem> layoutItems = layoutGroup.getItems();
		for (LayoutItem layoutItem : layoutItems) {

			if (layoutItem == null)
				continue;

			String title = layoutItem.getTitle();
			if (layoutItem instanceof LayoutItemField)
				detailsView.addLayoutField(indent + title);
			else if (!title.isEmpty())
				detailsView.addLayoutGroup(indent + title);

			// recurse into child groups
			if (layoutItem instanceof LayoutGroup)
				addLayoutGroup((LayoutGroup) layoutItem, indent + "-- ");
		}
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
