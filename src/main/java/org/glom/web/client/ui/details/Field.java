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

import org.glom.web.shared.layout.Formatting;
import org.glom.web.shared.layout.LayoutItemField;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * TODO: Create an interface or abstract class that extends Composite for UI items in the details view. Each item can
 * then individually extend the LayoutItem* class.
 * 
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class Field extends Composite {
	private LayoutItemField layoutItem;
	private Label detailsData = new Label();

	public Field(LayoutItemField layoutItemField) {
		// Labels (text in div element) are being used so that the height of the details-data element can be set for
		// the multiline height of LayoutItemFeilds. This allows the the data element to display the correct height
		// if style is applied that shows the height. This has the added benefit of allowing the order of the label and
		// data elements to be changed for right-to-left languages.

		layoutItem = layoutItemField;
		Label detailsLabel = new Label(layoutItem.getTitle() + ":");
		detailsLabel.setStyleName("details-label");

		detailsData.setStyleName("details-data");
		Formatting formatting = layoutItem.getFormatting();
		detailsData.setHeight(formatting.getTextFormatMultilineHeightLines() + "em");

		FlowPanel mainPanel = new FlowPanel();
		mainPanel.setStyleName("details-cell");

		mainPanel.add(detailsLabel);
		mainPanel.add(detailsData);

		initWidget(mainPanel);
	}

	public void setText(String text) {
		detailsData.setText(text);
	}

	public LayoutItemField getLayoutItem() {
		return layoutItem;
	}
}
