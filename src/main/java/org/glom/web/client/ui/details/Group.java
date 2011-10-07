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

import java.util.ArrayList;

import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItem;
import org.glom.web.shared.layout.LayoutItemField;
import org.glom.web.shared.layout.LayoutItemPortal;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Ben Konrath <ben@bagu.org>
 */
public class Group extends Composite {
	private FlowPanel mainPanel = new FlowPanel();
	private FlowPanel groupContents; // set in constructor
	private final ArrayList<DetailsCell> detailsCells = new ArrayList<DetailsCell>();
	private final ArrayList<Portal> portals = new ArrayList<Portal>();
	FlowTable flowtable;// set in constructor

	@SuppressWarnings("unused")
	private Group() {
		// disable default constructor
	}

	/**
	 * Creates a new widget for a Group or sub-Group.
	 * 
	 * @param layoutGroup
	 *            The DTO that holds the Group or sub-Group layout information
	 * @param subGroup
	 *            true if the layoutGroup is a sub-Group, false if it's a Group
	 * @param mainTitleSet
	 *            true if the main title for the Group has been set, false if hasn't been set yet
	 */
	public Group(LayoutGroup layoutGroup, boolean subGroup, boolean mainTitleSet) {
		mainPanel.setStyleName(subGroup ? "subgroup" : "group");

		String groupTitle = layoutGroup.getTitle();
		if (!groupTitle.isEmpty()) {
			Label label = new Label(groupTitle);
			mainPanel.add(label);

			// the "group-title" class could could be used for the subgroup title if the group title is empty
			label.setStyleName(mainTitleSet ? "subgroup-title" : "group-title");
			mainTitleSet = true;

			groupContents = new FlowPanel();
			groupContents.setStyleName("group-contents");
			mainPanel.add(groupContents);
		} else {
			// don't make a separate contents panel when the group title has not been set
			groupContents = mainPanel;
		}

		flowtable = new FlowTable(layoutGroup.getColumnCount());
		groupContents.add(flowtable);

		// create the appropriate UI element for each child item
		for (LayoutItem layoutItem : layoutGroup.getItems()) {

			if (layoutItem instanceof LayoutItemField) {
				DetailsCell field = new DetailsCell((LayoutItemField) layoutItem);
				flowtable.add(field);
				detailsCells.add(field);
			} else if (layoutItem instanceof LayoutItemPortal) {
				Portal portal = new Portal((LayoutItemPortal) layoutItem, mainTitleSet);
				flowtable.add(portal);
				portals.add(portal);
			} else if (layoutItem instanceof LayoutGroup) {
				// create a Group for the child group
				Group childGroup = new Group((LayoutGroup) layoutItem, true, mainTitleSet);
				flowtable.add(childGroup);
				detailsCells.addAll(childGroup.getDetailsCells());
				portals.addAll(childGroup.getPortals());
			}
		}

		initWidget(mainPanel);
	}

	public ArrayList<DetailsCell> getDetailsCells() {
		return detailsCells;
	}

	/**
	 * @return
	 */
	public ArrayList<Portal> getPortals() {
		return portals;
	}

}