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
import org.glom.web.client.event.LocaleChangeEvent;
import org.glom.web.client.event.QuickFindChangeEvent;
import org.glom.web.client.event.TableChangeEvent;
import org.glom.web.client.place.DetailsPlace;
import org.glom.web.client.place.HasRecordsPlace;
import org.glom.web.client.place.HasTablePlace;
import org.glom.web.client.place.ListPlace;
import org.glom.web.client.place.ReportPlace;
import org.glom.web.client.ui.TableSelectionView;
import org.glom.web.client.ui.View;
import org.glom.web.shared.DocumentInfo;
import org.glom.web.shared.Reports;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 *
 */
public class TableSelectionActivity extends AbstractActivity implements View.Presenter {
	private final ClientFactory clientFactory;
	private String documentID;
	private String documentTitle;
	private String tableName;
	private String quickFind;
	private String reportName;
	private HandlerRegistration tableChangeHandlerRegistration = null;
	private HandlerRegistration quickFindChangeHandlerRegistration = null;
	private HandlerRegistration localeChangeHandlerRegistration = null;
	private HandlerRegistration reportChangeHandlerRegistration = null;

	// This activity isn't properly configured until the List or Details Place is set with the appropriate methods
	public TableSelectionActivity(final ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * Invoked by the ActivityManager to start a new Activity
	 */
	@Override
	public void start(final AcceptsOneWidget containerWidget, final EventBus eventBus) {

		final TableSelectionView tableSelectionView = clientFactory.getTableSelectionView();
		tableSelectionView.setPresenter(this);

		// For table changes with the tableSelector:
		final HasChangeHandlers tableSelector = tableSelectionView.getTableSelector();
		tableChangeHandlerRegistration = tableSelector.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(final ChangeEvent event) {
				// Fire a table change event so that other views (e.g. the details view) know about the change and can
				// update themselves.
				eventBus.fireEvent(new TableChangeEvent(tableSelectionView.getSelectedTableName()));

				// Update the browser title because there's place change and the setPlace() method will not be called.
				Window.setTitle(documentTitle + ": " + tableSelectionView.getSelectedTableTitle());
			}
		});

		// For quick find changes with the quick find box:
		final HasChangeHandlers quickFindBox = tableSelectionView.getQuickFindBox();
		quickFindChangeHandlerRegistration = quickFindBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(final ChangeEvent event) {
				// Fire a quickfind change event so that other views (e.g. the details view) know about the change and
				// can
				// update themselves.
				eventBus.fireEvent(new QuickFindChangeEvent(tableSelectionView.getQuickFindText()));

				// Update the browser title because there's place change and the setPlace() method will not be called.
				// TODO? Window.setTitle(documentTitle + ": " + tableSelectionView.getSelectedTableTitle());
			}
		});

		// For locale changes with the localeSelector:
		final HasChangeHandlers localeSelector = tableSelectionView.getLocaleSelector();
		localeChangeHandlerRegistration = localeSelector.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(final ChangeEvent event) {
				// Show the translated version of the document title and the table names:
				final String localeID = tableSelectionView.getSelectedLocale();
				fillView(tableSelectionView);

				final String newURL = Window.Location.createUrlBuilder()
						.setParameter(LocaleInfo.getLocaleQueryParam(), localeID).buildString();
				Window.Location.assign(newURL);

				// Fire a locale change event so that other views (e.g. the details view) know about the change and can
				// update themselves.
				eventBus.fireEvent(new LocaleChangeEvent(localeID));
			}
		});

		// For report choices with the reportSelector:
		final HasChangeHandlers reportSelector = tableSelectionView.getReportSelector();
		reportChangeHandlerRegistration = reportSelector.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(final ChangeEvent event) {
				final String reportName = tableSelectionView.getSelectedReport();
				if(StringUtils.isEmpty(reportName)) {
					// Interpret selecting no report as requesting the list view.
					goTo(new ListPlace(documentID, tableName, quickFind));
				} else {
					// Show the selected report:
					goTo(new ReportPlace(documentID, tableName, reportName, quickFind));
				}
			}
		});

		fillView(tableSelectionView);

		// we're done, set the widget
		containerWidget.setWidget(tableSelectionView.asWidget());
	}

	private void fillView(final TableSelectionView tableSelectionView) {
		// get the table names, table titles and default table index for the current document
		final AsyncCallback<DocumentInfo> callback = new AsyncCallback<DocumentInfo>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.getDocumentInfo()");
			}

			@Override
			public void onSuccess(final DocumentInfo result) {
				tableSelectionView.setTableSelection(result.getTableNames(), result.getTableTitles());

				if (StringUtils.isEmpty(tableName)) {
					tableName = result.getTableNames().get(result.getDefaultTableIndex());
				}

				tableSelectionView.setSelectedTableName(tableName);

				tableSelectionView.setLocaleList(result.getLocaleIDs(), result.getLocaleTitles());

				//Show what locale is currently being used:
				String localeIDForCombo = Utils.getCurrentLocaleID();
				
				//Indicate that we use English if no other locale has been specified by either
				//the URL or the configuration.
				//Alternatively we could also show the locale in the URL, even if it is en.
				if(StringUtils.isEmpty(localeIDForCombo)) {
					localeIDForCombo = "en";
				}
				tableSelectionView.setSelectedLocale(localeIDForCombo);

				documentTitle = result.getTitle();
				tableSelectionView.setDocumentTitle(documentTitle);
				Window.setTitle(documentTitle + ": " + tableSelectionView.getSelectedTableTitle());
			}
		};

		final String localeID = Utils.getCurrentLocaleID();
		OnlineGlomServiceAsync.Util.getInstance().getDocumentInfo(documentID, localeID, callback);

		// get the reports list for the current table:
		final AsyncCallback<Reports> callback_report = new AsyncCallback<Reports>() {
			@Override
			public void onFailure(final Throwable caught) {
				// TODO: create a way to notify users of asynchronous callback failures
				GWT.log("AsyncCallback Failed: OnlineGlomService.getReportsList()");
			}

			@Override
			public void onSuccess(final Reports result) {
				tableSelectionView.setReportList(result);
				
				// Show the selected report name again:
				// TODO: Avoid duplication in ReportActivity.
				tableSelectionView.setSelectedReport(reportName);
			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getReportsList(documentID, tableName, localeID, callback_report);

		// Show the quickFind text that was specified by the URL token:
		tableSelectionView.setQuickFindText(quickFind);
	}

	// This method will be called before the {@link TableSelectionActivity#start(AcceptsOneWidget, EventBus)} method and
	// any time the Place changes after the start method has been called.
	public void setPlace(final HasTablePlace place) {
		documentID = place.getDocumentID();
		tableName = place.getTableName();

		try {
			final HasRecordsPlace asPlace = (HasRecordsPlace) place;
			quickFind = asPlace.getQuickFind();
		} catch (final ClassCastException ex) {
			quickFind = "";
		}

		final TableSelectionView tableSelectionView = clientFactory.getTableSelectionView();

		// Show the 'back to list' link if we're at a DetailsPlace or a ReportPlace.
		if (place instanceof DetailsPlace || place instanceof ReportPlace) {
			tableSelectionView.setBackLinkVisible(true);
			tableSelectionView.setBackLink(documentID, tableName, ""); // TODO: quickfind?
		} else if (place instanceof ListPlace) {
			tableSelectionView.setBackLinkVisible(false);
		}
		
		reportName = "";
		if (place instanceof ReportPlace) {
			reportName = ((ReportPlace)place).getReportName();
		}

		fillView(tableSelectionView);
	}

	private void clearView() {
		clientFactory.getTableSelectionView().clear();

		if (tableChangeHandlerRegistration != null) {
			tableChangeHandlerRegistration.removeHandler();
			tableChangeHandlerRegistration = null;
		}

		if (quickFindChangeHandlerRegistration != null) {
			quickFindChangeHandlerRegistration.removeHandler();
			quickFindChangeHandlerRegistration = null;
		}

		if (localeChangeHandlerRegistration != null) {
			localeChangeHandlerRegistration.removeHandler();
			localeChangeHandlerRegistration = null;
		}

		if (reportChangeHandlerRegistration != null) {
			reportChangeHandlerRegistration.removeHandler();
			reportChangeHandlerRegistration = null;
		}
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
