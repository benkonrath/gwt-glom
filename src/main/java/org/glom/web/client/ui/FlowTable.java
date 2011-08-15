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

package org.glom.web.client.ui;

import java.util.ArrayList;

import org.glom.web.shared.layout.Formatting;
import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItem;
import org.glom.web.shared.layout.LayoutItemField;
import org.glom.web.shared.layout.LayoutItemPortal;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
class FlowTable extends FlowPanel {
	FlowPanel groupContents;
	private final ArrayList<Label> dataLabels = new ArrayList<Label>();

	@SuppressWarnings("unused")
	private FlowTable() {
	}

	public FlowTable(LayoutGroup layoutGroup, boolean subGroup, boolean mainTitleSet) {
		super();
		this.setStyleName(subGroup ? "subgroup" : "group");

		String groupTitle = layoutGroup.getTitle();
		if (!groupTitle.isEmpty()) {
			Label label = new Label(groupTitle);
			this.add(label);

			// the "group-title" class could could be used for the subgroup title if the group title is empty
			label.setStyleName(mainTitleSet ? "subgroup-title" : "group-title");
			mainTitleSet = true;

			groupContents = new FlowPanel();
			groupContents.setStyleName("group-contents");
			this.add(groupContents);
		} else {
			// don't make a separate contents panel when the group title has not been set
			groupContents = this;
		}

		// create the appropriate UI element for each child item
		for (LayoutItem layoutItem : layoutGroup.getItems()) {

			if (layoutItem == null)
				continue;

			if (layoutItem instanceof LayoutItemField) {
				addDetailsCell((LayoutItemField) layoutItem);
			} else if (layoutItem instanceof LayoutItemPortal) {
				// TODO implement support for portals
				continue;
			} else if (layoutItem instanceof LayoutGroup) {
				// create a FlowTable for the child group
				FlowTable flowTable = new FlowTable((LayoutGroup) layoutItem, true, mainTitleSet);
				groupContents.add(flowTable);
				dataLabels.addAll(flowTable.getDataLabels());
			}
		}

	}

	private void addDetailsCell(LayoutItemField layoutItemField) {

		// Labels (text in div a element) are being used so that the height of the details-data element can be set for
		// the multiline height of LayoutItemFeilds. This allows the the data element to display the correct height
		// if style is applied that shows the height. This has the added benefit of allowing the order of the label and
		// data elements to be changed for right-to-left languages.

		Label detailsLabel = new Label(layoutItemField.getTitle() + ":");
		detailsLabel.setStyleName("details-label");

		Label detailsData = new Label();
		detailsData.setStyleName("details-data");
		Formatting formatting = layoutItemField.getFormatting();
		detailsData.setHeight(formatting.getTextFormatMultilineHeightLines() + "em");

		FlowPanel fieldPanel = new FlowPanel();
		fieldPanel.setStyleName("details-cell");

		fieldPanel.add(detailsLabel);
		fieldPanel.add(detailsData);

		groupContents.add(fieldPanel);
		dataLabels.add(detailsData);
	}

	public ArrayList<Label> getDataLabels() {
		return dataLabels;
	}
}