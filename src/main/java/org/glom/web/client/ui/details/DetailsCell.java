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
import org.glom.web.shared.DataItem;
import org.glom.web.shared.GlomNumericFormat;
import org.glom.web.shared.layout.Formatting;
import org.glom.web.shared.layout.LayoutItemField;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;

/**
 * TODO: Create an interface or abstract class that extends Composite for UI items in the details view. Each item can
 * then individually extend the LayoutItem* class.
 * 
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class DetailsCell extends Composite {
	private LayoutItemField layoutItem;
	private Label detailsData = new Label();

	public DetailsCell(LayoutItemField layoutItemField) {
		// Labels (text in div element) are being used so that the height of the details-data element can be set for
		// the multiline height of LayoutItemFeilds. This allows the the data element to display the correct height
		// if style is applied that shows the height. This has the added benefit of allowing the order of the label and
		// data elements to be changed for right-to-left languages.

		layoutItem = layoutItemField;
		Label detailsLabel = new Label(layoutItem.getTitle() + ":");
		detailsLabel.setStyleName("details-label");

		detailsData.setStyleName("details-data");
		Formatting formatting = layoutItem.getFormatting();

		// set the height based on the number of lines
		detailsData.setHeight(formatting.getTextFormatMultilineHeightLines() + "em");

		// set the alignment
		switch (formatting.getHorizontalAlignment()) {
		case HORIZONTAL_ALIGNMENT_LEFT:
			detailsData.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
			break;
		case HORIZONTAL_ALIGNMENT_RIGHT:
			detailsData.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			break;
		case HORIZONTAL_ALIGNMENT_AUTO:
		default:
			detailsData.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_DEFAULT);
			break;
		}

		// set the text foreground and background colours
		String foregroundColour = formatting.getTextFormatColourForeground();
		if (foregroundColour != null && !foregroundColour.isEmpty())
			detailsData.getElement().getStyle().setColor(foregroundColour);
		String backgroundColour = formatting.getTextFormatColourBackground();
		if (backgroundColour != null && !backgroundColour.isEmpty())
			detailsData.getElement().getStyle().setBackgroundColor(backgroundColour);

		FlowPanel mainPanel = new FlowPanel();
		mainPanel.setStyleName("details-cell");

		mainPanel.add(detailsLabel);
		mainPanel.add(detailsData);

		initWidget(mainPanel);
	}

	public void setData(DataItem value) {

		switch (layoutItem.getType()) {
		case TYPE_BOOLEAN:
			// FIXME create checkbox like in the list view
			detailsData.setText(value.getBoolean() ? "TRUE" : "FALSE");
			break;
		case TYPE_NUMERIC:
			GlomNumericFormat glomNumericFormat = layoutItem.getFormatting().getGlomNumericFormat();
			NumberFormat gwtNumberFormat = Utils.getNumberFormat(glomNumericFormat);

			// set the foreground colour to red if the number is negative and this is requested
			if (glomNumericFormat.getUseAltForegroundColourForNegatives() && value.getNumber() < 0) {
				// The default alternative colour in libglom is red.
				detailsData.getElement().getStyle().setColor("Red");
			}

			detailsData.setText(gwtNumberFormat.format(value.getNumber()));
			break;
		case TYPE_TEXT:
			detailsData.setText(value.getText());
		default:
			break;
		}

	}

	public LayoutItemField getLayoutItem() {
		return layoutItem;
	}
}
