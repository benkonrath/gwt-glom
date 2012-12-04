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
import org.glom.web.client.StringUtils;
import org.glom.web.client.Utils;
import org.glom.web.client.place.DocumentSelectionPlace;
import org.glom.web.client.place.ReportPlace;
import org.glom.web.client.ui.OnlineGlomConstants;
import org.glom.web.client.ui.ReportView;
import org.glom.web.client.ui.TableSelectionView;
import org.glom.web.client.ui.View;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ReportActivity extends HasTableActivity implements View.Presenter {

	private final String reportName;
	private final String quickFind;
	private final ClientFactory clientFactory;
	private final ReportView reportView;

	// OnlineGlomConstants.java is generated in the target/ directory,
	// from OnlineGlomConstants.properties
	// by the gwt-maven-plugin's i18n (mvn:i18n) goal.
	private final OnlineGlomConstants constants = GWT.create(OnlineGlomConstants.class);

	public ReportActivity(final ReportPlace place, final ClientFactory clientFactory) {
		super(place, clientFactory);
		this.quickFind = place.getQuickFind();
		this.reportName = place.getReportName();
		this.clientFactory = clientFactory;
		reportView = clientFactory.getReportView();
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		if (StringUtils.isEmpty(documentID)) {
			goTo(new DocumentSelectionPlace());
		}

		// register this class as the presenter
		reportView.setPresenter(this);

		checkAuthentication(eventBus);

		// populate the report part:
		final AsyncCallback<String> callback = new AsyncCallback<String>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.getReportHTML(): " + caught.getMessage());
			}

			@Override
			public void onSuccess(final String result) {
				reportView.setReportHTML(result);
			}
		};

		final TableSelectionView tableSelectionView = clientFactory.getTableSelectionView();
		tableSelectionView.setSelectedReport(reportName);

		reportView.setWaitingText(constants.generatingReport()); // This is cleared by setReportHTML().
		final String localeID = Utils.getCurrentLocaleID();
		OnlineGlomServiceAsync.Util.getInstance().getReportHTML(documentID, tableName, reportName, quickFind, localeID,
				callback);

		// indicate that the view is ready to be displayed
		panel.setWidget(reportView.asWidget());
	}

	private void clearView() {
		authenticationPopup.hide();
		authenticationPopup.clear();
		reportView.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.AbstractActivity#onCancel()
	 */
	@Override
	public void onCancel() {
		clearView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.AbstractActivity#onStop()
	 */
	@Override
	public void onStop() {
		clearView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.View.Presenter#goTo(com.google.gwt.place.shared.Place)
	 */
	@Override
	public void goTo(final Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

}
