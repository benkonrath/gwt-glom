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
import org.glom.web.client.event.TableChangeEvent;
import org.glom.web.client.place.ListPlace;
import org.glom.web.client.ui.TableSelectionView;
import org.glom.web.shared.GlomDocument;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class TableSelectionActivity extends AbstractActivity {
	private final ClientFactory clientFactory;
	private String documentTitle;
	private HandlerRegistration changeHandlerRegistration = null;

	public TableSelectionActivity(ListPlace place, ClientFactory clientFactory) {
		this.documentTitle = place.getDocumentTitle();
		this.clientFactory = clientFactory;
	}

	/**
	 * Invoked by the ActivityManager to start a new Activity
	 */
	@Override
	public void start(AcceptsOneWidget containerWidget, final EventBus eventBus) {
		Window.setTitle("Online Glom - " + documentTitle);

		final TableSelectionView tableSelectionView = clientFactory.getTableSelectionView();

		HasChangeHandlers tableSelector = tableSelectionView.getTableSelector();

		changeHandlerRegistration = tableSelector.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				eventBus.fireEvent(new TableChangeEvent(tableSelectionView.getSelectedTable()));
			}
		});

		// get the table names, table titles and default table index for the current document
		AsyncCallback<GlomDocument> callback = new AsyncCallback<GlomDocument>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getGlomDocument()");
			}

			public void onSuccess(GlomDocument result) {
				tableSelectionView.setTableSelection(result.getTableNames(), result.getTableTitles());
				tableSelectionView.setTableSelectedIndex(result.getDefaultTableIndex());
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getGlomDocument(documentTitle, callback);

		// we're done, set the widget
		containerWidget.setWidget(tableSelectionView.asWidget());
	}

	private void shutdown() {
		clientFactory.getTableSelectionView().clear();
		if (changeHandlerRegistration != null) {
			changeHandlerRegistration.removeHandler();
			changeHandlerRegistration = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.AbstractActivity#onCancel()
	 */
	@Override
	public void onCancel() {
		shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.AbstractActivity#onStop()
	 */
	@Override
	public void onStop() {
		shutdown();
	}
}
