/*
 * Copyright (C) 2010, 2011 Openismus GmbH
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
import org.glom.web.client.mvp.DataActivityMapper;
import org.glom.web.client.mvp.DocumentSelectionActivityMapper;
import org.glom.web.client.mvp.TableSelectionActivityMapper;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class OnlineGlom implements EntryPoint {

	/*
	 * Some of these are protected, rather than private, so that GwtTestOnlineGlom can access them.
	 */
	private final LayoutPanel rootLayoutPanel = RootLayoutPanel.get();
	private final FlowPanel layoutPanel = new FlowPanel();
	protected SimplePanel docSelectionPanel = new SimplePanel(); //Or document login.
	protected SimplePanel dataPanel = new SimplePanel();
	protected SimplePanel tableSelectionPanel = new SimplePanel();

	protected ClientFactory clientFactory;

	AcceptsOneWidget docSelectionDisplay = new AcceptsOneWidget() {
		@Override
		public void setWidget(final IsWidget activityWidget) {
			final Widget widget = Widget.asWidgetOrNull(activityWidget);
			docSelectionPanel.setVisible(widget != null);
			docSelectionPanel.setWidget(widget);
		}
	};

	AcceptsOneWidget dataDisplay = new AcceptsOneWidget() {
		@Override
		public void setWidget(final IsWidget activityWidget) {
			final Widget widget = Widget.asWidgetOrNull(activityWidget);
			dataPanel.setVisible(widget != null);
			dataPanel.setWidget(widget);
		}
	};

	AcceptsOneWidget tableSelectionDisplay = new AcceptsOneWidget() {
		@Override
		public void setWidget(final IsWidget activityWidget) {
			final Widget widget = Widget.asWidgetOrNull(activityWidget);
			tableSelectionPanel.setVisible(widget != null);
			tableSelectionPanel.setWidget(widget);
		}
	};

	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {

		rootLayoutPanel.add(layoutPanel);
		rootLayoutPanel.setWidgetVisible(layoutPanel, true);

		// TODO This value should really come from the css for the body tag but reading the value using
		// RootPanel.getBodyElement().getStyle().getMargin() doesn't seem to be working.
		layoutPanel.getElement().getStyle().setMargin(1, Unit.EM);

		// add the display regions to the main layout panel
		layoutPanel.add(docSelectionPanel);
		layoutPanel.add(tableSelectionPanel);
		layoutPanel.add(dataPanel);

		// set some properties for the display regions
		// The 'overflow: visible' adds a horizontal scrollbar when the content is larger than the browser window.
		// TODO: It would be better to just have the regular browser scrollbars, but for some reason they
		// are not shown.
		rootLayoutPanel.getWidgetContainerElement(layoutPanel).getStyle().setOverflow(Overflow.VISIBLE);

		// hide the display regions for the list and details places because they are not shown by default
		tableSelectionPanel.setVisible(false);
		dataPanel.setVisible(false);

		// We might, in future, use different ClientFactory implementations to create different views
		// for different browser types (such as mobile), so we use GWT.create() to have deferred binding.
		// See http://code.google.com/webtoolkit/doc/latest/DevGuideMvpActivitiesAndPlaces.html
		// which describes how to do this via our OnlineGlom.gwt.xml file.
		clientFactory = GWT.create(ClientFactory.class);
		final EventBus eventBus = clientFactory.getEventBus();
		final PlaceController placeController = clientFactory.getPlaceController();

		// Activity manager for the data display region (list or details view).
		final ActivityMapper dataActivityMapper = new DataActivityMapper(clientFactory);
		final ActivityManager dataActivityManager = new ActivityManager(dataActivityMapper, eventBus);
		dataActivityManager.setDisplay(dataDisplay);

		// Activity manager for the document selection display region.
		final ActivityMapper docSelectionActivityMapper = new DocumentSelectionActivityMapper(clientFactory);
		final ActivityManager docSelectionActivityManager = new ActivityManager(docSelectionActivityMapper, eventBus);
		docSelectionActivityManager.setDisplay(docSelectionDisplay);

		// Activity manager for the table selection display region.
		final ActivityMapper tableSelectionActivityMapper = new TableSelectionActivityMapper(clientFactory);
		final ActivityManager tableSelectionActivityManager = new ActivityManager(tableSelectionActivityMapper,
				eventBus);
		tableSelectionActivityManager.setDisplay(tableSelectionDisplay);

		// Start PlaceHistoryHandler with our PlaceHistoryMapper.
		final AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
		final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
		
		Place defaultPlace = null;
		if(placeController instanceof PlaceControllerExt) {
			PlaceControllerExt ext = (PlaceControllerExt)placeController;
			defaultPlace = ext.getDefaultPlace();
		}
		historyHandler.register(placeController, eventBus, defaultPlace);

		// Goes to the place represented on the URL or the default place.
		historyHandler.handleCurrentHistory();
	}
}
