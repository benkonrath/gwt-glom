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

import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItem;
import org.glom.web.shared.layout.LayoutItemField;
import org.glom.web.shared.layout.LayoutItemPortal;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
class FlowTable extends FlowPanel {
	FlowPanel groupContents = new FlowPanel();
	private final ArrayList<InlineLabel> dataLabels = new ArrayList<InlineLabel>();

	public FlowTable(LayoutGroup layoutGroup) {
		super();
		setStyleName("group");

		String groupTitle = layoutGroup.getTitle();
		if (!groupTitle.isEmpty()) {
			Label label = new Label(groupTitle);
			label.setStyleName("group-title");
			this.add(label);
		}

		groupContents.setStyleName("group-contents");
		this.add(groupContents);

		createLayout(layoutGroup, "");
	}

	private InlineLabel addDetailsCell(String title) {

		InlineLabel detailsLabel = new InlineLabel(title);
		detailsLabel.setStyleName("details-label");

		InlineLabel detailsData = new InlineLabel();
		detailsData.setStyleName("details-data");

		FlowPanel fieldPanel = new FlowPanel();
		fieldPanel.setStyleName("details-cell");

		fieldPanel.add(detailsLabel);
		fieldPanel.add(detailsData);

		groupContents.add(fieldPanel);

		return detailsData;
	}

	/*
	 * Creates a basic indented layout without the flowtable.
	 */
	private void createLayout(LayoutGroup layoutGroup, String indent) {
		if (layoutGroup == null)
			return;

		// look at each child item
		ArrayList<LayoutItem> layoutItems = layoutGroup.getItems();
		for (LayoutItem layoutItem : layoutItems) {

			if (layoutItem == null)
				continue;

			String title = layoutItem.getTitle();
			if (layoutItem instanceof LayoutItemField) {
				dataLabels.add(addDetailsCell(indent + title));
			} else if (layoutItem instanceof LayoutItemPortal) {
				// ignore portals for now
				continue;
			} else if (layoutItem instanceof LayoutGroup) {
				// create a FlowTable for the child group
				FlowTable flowTable = new FlowTable((LayoutGroup) layoutItem);
				groupContents.add(flowTable);
				dataLabels.addAll(flowTable.getDataLabels());
			}
		}
	}

	public ArrayList<InlineLabel> getDataLabels() {
		return dataLabels;
	}
}