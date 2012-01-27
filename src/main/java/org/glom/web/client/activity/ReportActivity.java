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

import org.glom.web.client.StringUtils;
import org.glom.web.client.ClientFactory;
import org.glom.web.client.OnlineGlomServiceAsync;
import org.glom.web.client.Utils;
import org.glom.web.client.event.LocaleChangeEvent;
import org.glom.web.client.event.LocaleChangeEventHandler;
import org.glom.web.client.event.QuickFindChangeEvent;
import org.glom.web.client.event.QuickFindChangeEventHandler;
import org.glom.web.client.event.TableChangeEvent;
import org.glom.web.client.event.TableChangeEventHandler;
import org.glom.web.client.place.DocumentSelectionPlace;
import org.glom.web.client.place.ReportPlace;
import org.glom.web.client.ui.AuthenticationPopup;
import org.glom.web.client.ui.ReportView;
import org.glom.web.client.ui.View;
import org.glom.web.shared.layout.LayoutGroup;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ReportActivity extends AbstractActivity implements View.Presenter {

	private final String documentID;
	private final String tableName;
	private final String reportName;
	private final String quickFind;
	private final ClientFactory clientFactory;
	private final ReportView reportView;
	private final AuthenticationPopup authenticationPopup;

	public ReportActivity(final ReportPlace place, final ClientFactory clientFactory) {
		this.documentID = place.getDocumentID(); // TODO: Just store the place?
		this.tableName = place.getTableName();
		this.quickFind = place.getQuickFind();
		this.reportName = place.getReportName();
		this.clientFactory = clientFactory;
		reportView = clientFactory.getReportView();
		authenticationPopup = clientFactory.getAuthenticationPopup();
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		if (StringUtils.isEmpty(documentID))
			goTo(new DocumentSelectionPlace());

		// register this class as the presenter
		reportView.setPresenter(this);

		// TODO this should really be it's own Place/Activity
		// check if the authentication info has been set for the document
		final AsyncCallback<Boolean> isAuthCallback = new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.isAuthenticated()");
			}

			@Override
			public void onSuccess(final Boolean result) {
				if (!result) {
					setUpAuthClickHandler(eventBus);
					authenticationPopup.center();
				}
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().isAuthenticated(documentID, isAuthCallback);

		// set the change handler for the table selection widget
		eventBus.addHandler(TableChangeEvent.TYPE, new TableChangeEventHandler() {
			@Override
			public void onTableChange(final TableChangeEvent event) {
				goTo(new ReportPlace(documentID, event.getNewTableName(), reportName, ""));
			}
		});

		// populate the cell table with data
		final AsyncCallback<LayoutGroup> callback = new AsyncCallback<LayoutGroup>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.getListViewLayout()");
			}

			@Override
			public void onSuccess(final LayoutGroup result) {
				// TODO check if result.getTableName() is the same as the tableName field. Update it if it's not the
				// same.
				//reportView.setCellTable(documentID, result, reportName, quickFind);
			}
		};
		
		final String localeID = Utils.getCurrentLocaleID();
		OnlineGlomServiceAsync.Util.getInstance().getReportLayout(documentID, tableName, reportName, localeID, callback);

		// TODO: Avoid the code duplication with DetailsActivity.
		// set the change handler for the quickfind text widget
		eventBus.addHandler(QuickFindChangeEvent.TYPE, new QuickFindChangeEventHandler() {
			@Override
			public void onQuickFindChange(final QuickFindChangeEvent event) {
				// We switch to the List view, to show search results.
				// TODO: Show the details view if there is only one result.
				goTo(new ReportPlace(documentID, tableName, reportName, event.getNewQuickFindText()));
			}
		});

		// Set the change handler for the table selection widget
		eventBus.addHandler(LocaleChangeEvent.TYPE, new LocaleChangeEventHandler() {
			@Override
			public void onLocaleChange(final LocaleChangeEvent event) {
				// note the empty primary key item
				goTo(new ReportPlace(documentID, tableName, reportName, quickFind));
			}
		});

		// indicate that the view is ready to be displayed
		panel.setWidget(reportView.asWidget());
	}

	private void setUpAuthClickHandler(final EventBus eventBus) {
		authenticationPopup.setClickOkHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				authenticationPopup.setTextFieldsEnabled(false);
				final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(final Throwable caught) {
						// TODO: create a way to notify users of asynchronous callback failures
						GWT.log("AsyncCallback Failed: OnlineGlomService.checkAuthentication()");
					}

					@Override
					public void onSuccess(final Boolean result) {
						if (result) {
							authenticationPopup.hide();
							eventBus.fireEvent(new TableChangeEvent(clientFactory.getTableSelectionView()
									.getSelectedTableName()));
						} else {
							authenticationPopup.setTextFieldsEnabled(true);
							authenticationPopup.setError();
						}
					}
				};
				OnlineGlomServiceAsync.Util.getInstance().checkAuthentication(documentID,
						authenticationPopup.getUsername(), authenticationPopup.getPassword(), callback);
			}

		});
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
