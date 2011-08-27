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
import org.glom.web.client.place.DocumentSelectionPlace;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class OnlineGlom implements EntryPoint {
	private Place defaultPlace = new DocumentSelectionPlace();
	private LayoutPanel layoutPanel = RootLayoutPanel.get();
	private SimplePanel docSelectionPanel = new SimplePanel();
	private SimplePanel dataPanel = new SimplePanel();
	private SimplePanel tableSelectionPanel = new SimplePanel();

	AcceptsOneWidget docSelectionDisplay = new AcceptsOneWidget() {
		@Override
		public void setWidget(IsWidget activityWidget) {
			Widget widget = Widget.asWidgetOrNull(activityWidget);
			layoutPanel.setWidgetVisible(docSelectionPanel, widget != null);
			docSelectionPanel.setWidget(widget);
		}
	};

	AcceptsOneWidget dataDisplay = new AcceptsOneWidget() {
		@Override
		public void setWidget(IsWidget activityWidget) {
			Widget widget = Widget.asWidgetOrNull(activityWidget);
			layoutPanel.setWidgetVisible(dataPanel, widget != null);
			dataPanel.setWidget(widget);
		}
	};

	AcceptsOneWidget tableSelectionDisplay = new AcceptsOneWidget() {
		@Override
		public void setWidget(IsWidget activityWidget) {
			Widget widget = Widget.asWidgetOrNull(activityWidget);
			layoutPanel.setWidgetVisible(tableSelectionPanel, widget != null);
			tableSelectionPanel.setWidget(widget);
		}
	};

	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {

		// TODO This value should really come from the css for the body tag but reading the value using
		// RootPanel.getBodyElement().getStyle().getMargin() doesn't seem to be working.
		layoutPanel.getElement().getStyle().setMargin(1, Unit.EM);

		// add the display regions to the main layout panel
		layoutPanel.add(docSelectionPanel);
		layoutPanel.add(tableSelectionPanel);
		layoutPanel.add(dataPanel);

		// set some properties for the display regions
		// The 'overflow: visible' adds a horizontal scrollbar when the list view table is larger than the browser
		// window.
		layoutPanel.getWidgetContainerElement(dataPanel).getStyle().setOverflow(Overflow.VISIBLE);

		// set the layout for the list and details places
		// TODO Figure out a way to make the layout without absolute positioning. Right now changes to the vertical
		// height of the table selector (i.e. CSS changes that affect the vertical height) require the
		// tableSelectionSize to be updated.
		double tableSelectionSize = 4.7;
		layoutPanel.setWidgetTopHeight(tableSelectionPanel, 0, Unit.PCT, tableSelectionSize, Unit.EM);
		layoutPanel.setWidgetTopHeight(dataPanel, tableSelectionSize, Unit.EM, 100, Unit.PCT);

		// hide the display regions for the list and details places because they are not shown by default
		layoutPanel.setWidgetVisible(tableSelectionPanel, false);
		layoutPanel.setWidgetVisible(dataPanel, false);

		ClientFactory clientFactory = GWT.create(ClientFactory.class);
		EventBus eventBus = clientFactory.getEventBus();
		PlaceController placeController = clientFactory.getPlaceController();

		// Activity manager for the data display region (list or details view).
		ActivityMapper dataActivityMapper = new DataActivityMapper(clientFactory);
		ActivityManager dataActivityManager = new ActivityManager(dataActivityMapper, eventBus);
		dataActivityManager.setDisplay(dataDisplay);

		// Activity manager for the document selection display region.
		ActivityMapper docSelectionActivityMapper = new DocumentSelectionActivityMapper(clientFactory);
		ActivityManager docSelectionActivityManager = new ActivityManager(docSelectionActivityMapper, eventBus);
		docSelectionActivityManager.setDisplay(docSelectionDisplay);

		// Activity manager for the table selection display region.
		ActivityMapper tableSelectionActivityMapper = new TableSelectionActivityMapper(clientFactory);
		ActivityManager tableSelectionActivityManager = new ActivityManager(tableSelectionActivityMapper, eventBus);
		tableSelectionActivityManager.setDisplay(tableSelectionDisplay);

		// Start PlaceHistoryHandler with our PlaceHistoryMapper.
		AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
		PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
		historyHandler.register(placeController, eventBus, defaultPlace);

		// Goes to the place represented on the URL or the default place.
		historyHandler.handleCurrentHistory();
	}
}
