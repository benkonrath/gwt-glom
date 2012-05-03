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

package org.glom.web.client.ui.details;

import org.glom.web.client.Utils;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 *
 */
public class Portal extends Composite {

	private FlowPanel contents;
	private LayoutItemPortal layoutItem;

	@SuppressWarnings("unused")
	private Portal() {
		// disable default constructor
	}

	/**
	 * Creates a new widget for a Portal.
	 * 
	 * @param layoutItem
	 *            The DTO that holds the Portal layout information
	 * @param setTitle
	 *            true if the title should be set, false otherwise
	 */
	public Portal(LayoutItemPortal layoutItemPortal, boolean setTitle) {
		layoutItem = layoutItemPortal;

		FlowPanel mainPanel = new FlowPanel();
		mainPanel.setStyleName("subgroup");

		if (setTitle) {
			Label title = new Label(layoutItem.getTitle());
			title.setStyleName("subgroup-title");
			mainPanel.add(title);

			contents = new FlowPanel();
			contents.setStyleName("group-contents");
			mainPanel.add(contents);
		} else {
			contents = mainPanel;
		}

		mainPanel.setStyleName("portal");

		// Calculate and set the expected height of the portal using the expected height of the RelatedListTable and the
		// expected height of the Portal container. Height information is needed so that Notebooks can be set to the
		// appropriate size for the RelatedListTable. The FlowTable also needs the height to be set so that it can
		// decide how to create the layout.
		int relatedListTableHeight = RelatedListTable.getExpectedHeight();
		// Use a temporary label in the main panel so that the margin sizes can be calculated.
		Label tempLabel = new Label("A");
		mainPanel.add(tempLabel);
		int containerHeight = Utils.getWidgetHeight(mainPanel) - Utils.getWidgetHeight(tempLabel);
		mainPanel.remove(setTitle ? 2 : 0); // removes tempLabel
		mainPanel.setHeight((relatedListTableHeight + containerHeight) + "px");

		initWidget(mainPanel);
	}

	// TODO The RelatedListTable should be created in this class
	public void setContents(RelatedListTable relatedListTable) {
		contents.clear();
		contents.add(relatedListTable);
	}

	public LayoutItemPortal getLayoutItem() {
		return layoutItem;
	}

}
