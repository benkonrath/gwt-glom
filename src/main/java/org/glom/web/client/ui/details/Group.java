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

import org.glom.web.shared.layout.Formatting;
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
	private final ArrayList<Label> dataLabels = new ArrayList<Label>();
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
				addDetailsCell((LayoutItemField) layoutItem);
			} else if (layoutItem instanceof LayoutItemPortal) {
				flowtable.add(new Portal((LayoutItemPortal) layoutItem, mainTitleSet));
			} else if (layoutItem instanceof LayoutGroup) {
				// create a Group for the child group
				Group group = new Group((LayoutGroup) layoutItem, true, mainTitleSet);
				flowtable.add(group);
				dataLabels.addAll(group.getDataLabels());
			}
		}

		initWidget(mainPanel);
	}

	private void addDetailsCell(LayoutItemField layoutItemField) {
		// Labels (text in div element) are being used so that the height of the details-data element can be set for
		// the multiline height of LayoutItemFeilds. This allows the the data element to display the correct height
		// if style is applied that shows the height. This has the added benefit of allowing the order of the label and
		// data elements to be changed for right-to-left languages.

		Label detailsLabel = new Label(layoutItemField.getTitle() + ":");
		detailsLabel.setStyleName("details-label");

		Label detailsData = new Label();
		detailsData.setStyleName("details-data");
		Formatting formatting = layoutItemField.getFormatting();
		detailsData.setHeight(formatting.getTextFormatMultilineHeightLines() + "em");

		FlowPanel detailsCell = new FlowPanel();
		detailsCell.setStyleName("details-cell");

		detailsCell.add(detailsLabel);
		detailsCell.add(detailsData);

		flowtable.add(detailsCell);
		dataLabels.add(detailsData);
	}

	public ArrayList<Label> getDataLabels() {
		return dataLabels;
	}
}