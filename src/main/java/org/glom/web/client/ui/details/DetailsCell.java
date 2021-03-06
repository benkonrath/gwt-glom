/*
 * Copyright (C) 2011 Openismus GmbH
 * Copyright (C) 2011 Ben Konrath <ben@bagu.org>
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

import java.io.UnsupportedEncodingException;

import org.glom.web.client.StringUtils;
import org.glom.web.client.Utils;
import org.glom.web.client.ui.OnlineGlomConstants;
import org.glom.web.shared.DataItem;
import org.glom.web.shared.libglom.Field;
import org.glom.web.shared.libglom.NumericFormat;
import org.glom.web.shared.libglom.layout.Formatting;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemImage;
import org.glom.web.shared.libglom.layout.LayoutItemText;
import org.glom.web.shared.libglom.layout.LayoutItemWithFormatting;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * Holds a label, data and a navigation button.
 */
public class DetailsCell extends Composite {

	// OnlineGlomConstants.java is generated in the target/ directory,
	// from OnlineGlomConstants.properties
	// by the gwt-maven-plugin's i18n (mvn:i18n) goal.
	private final OnlineGlomConstants constants = GWT.create(OnlineGlomConstants.class);

	private LayoutItemWithFormatting layoutItem = null;
	private Field.GlomFieldType dataType = Field.GlomFieldType.TYPE_INVALID;
	private final FlowPanel detailsData = new FlowPanel();
	private final Label detailsLabel = new Label();
	private final FlowPanel mainPanel = new FlowPanel();
	private DataItem dataItem;

	private Button openButton = null;
	private HandlerRegistration openButtonHandlerReg = null;

	public DetailsCell(final LayoutItemField layoutItemField) {

		setupWidgets(layoutItemField);

		Formatting formatting = layoutItemField.getFormatting();
		if (formatting == null) {
			GWT.log("setData(): formatting is null");
			formatting = new Formatting(); // To avoid checks later.
		}

		// set the height based on the number of lines
		if (layoutItemField.getGlomType() != Field.GlomFieldType.TYPE_IMAGE) {
			detailsData.setHeight(formatting.getTextFormatMultilineHeightLines() + "em");
		}

		final String navigationTableName = layoutItemField.getNavigationTableName();
		if (!StringUtils.isEmpty(navigationTableName)) {
			openButton = new Button(constants.open());
			openButton.setStyleName("details-navigation");
			openButton.setEnabled(false);
			mainPanel.add(openButton);
		}

		this.layoutItem = layoutItemField;
		this.dataType = layoutItemField.getGlomType();

		initWidget(mainPanel);
	}

	public DetailsCell(final LayoutItemText layoutItemText) {

		setupWidgets(layoutItemText);

		this.layoutItem = layoutItemText;
		this.dataType = Field.GlomFieldType.TYPE_TEXT;

		initWidget(mainPanel);

		//Use the static text:
		final DataItem dataItem = new DataItem();
		final String text = layoutItemText.getText().getTitle();
		dataItem.setText(text);
		setData(dataItem);
	}

	public DetailsCell(final LayoutItemImage layoutItemImage) {

		setupWidgets(layoutItemImage);

		this.layoutItem = layoutItemImage;
		this.dataType = Field.GlomFieldType.TYPE_IMAGE;

		initWidget(mainPanel);

		//Use the static image:
		setData(layoutItemImage.getImage());
	}

	private void setupWidgets(LayoutItemWithFormatting layoutItem) {
		// Labels (text in div element) are being used so that the height of the details-data element can be set for
		// the multiline height of LayoutItemFields. This allows the the data element to display the correct height
		// if style is applied that shows the height. This has the added benefit of allowing the order of the label and
		// data elements to be changed for right-to-left languages.

		String title = layoutItem.getTitle();
		if(!StringUtils.isEmpty(title)) {
			title += ":";
		}

		final Label detailsLabel = new Label(title); //TODO: Rename to titleLabel?
		detailsLabel.setStyleName("details-label");

		detailsData.setStyleName("details-data");
		Formatting formatting = layoutItem.getFormatting();
		if (formatting == null) {
			GWT.log("setData(): formatting is null");
			formatting = new Formatting(); // To avoid checks later.
		}

		// set the alignment
		switch (formatting.getHorizontalAlignment()) {
		case HORIZONTAL_ALIGNMENT_LEFT:
			detailsLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
			break;
		case HORIZONTAL_ALIGNMENT_RIGHT:
			detailsLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			break;
		case HORIZONTAL_ALIGNMENT_AUTO:
		default:
			detailsLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_DEFAULT);
			break;
		}

		// set the text foreground and background colors
		final String foregroundColor = formatting.getTextFormatColorForegroundAsHTMLColor();
		if (!StringUtils.isEmpty(foregroundColor)) {
			detailsData.getElement().getStyle().setColor(foregroundColor);
		}
		final String backgroundColor = formatting.getTextFormatColorBackgroundAsHTMLColor();
		if (!StringUtils.isEmpty(backgroundColor)) {
			detailsData.getElement().getStyle().setBackgroundColor(backgroundColor);
		}

		mainPanel.setStyleName("details-cell");

		mainPanel.add(detailsLabel);
		mainPanel.add(detailsData);
	}

	public DataItem getData() {
		return dataItem;
	}

	public void setData(final DataItem dataItem) {
		detailsData.clear();

		if (dataItem == null) {
			return;
		}

		Formatting formatting = layoutItem.getFormatting();

		// FIXME use the cell renderers from the list view to render the information here
		switch (this.dataType) {
		case TYPE_BOOLEAN:
			final CheckBox checkBox = new CheckBox();
			checkBox.setValue(dataItem.getBoolean());
			checkBox.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(final ClickEvent event) {
					// don't let users change the checkbox
					checkBox.setValue(dataItem.getBoolean());
				}
			});
			detailsData.add(checkBox);
			break;
		case TYPE_NUMERIC:
			if (formatting == null) {
				GWT.log("setData(): formatting is null");
				formatting = new Formatting(); // To avoid checks later.
			}
			final NumericFormat numericFormat = formatting.getNumericFormat();
			final NumberFormat gwtNumberFormat = Utils.getNumberFormat(numericFormat);

			// set the foreground color to red if the number is negative and this is requested
			if (numericFormat.getUseAltForegroundColorForNegatives() && dataItem.getNumber() < 0) {
				// The default alternative color in libglom is red.
				detailsData.getElement().getStyle()
						.setColor(NumericFormat.getAlternativeColorForNegativesAsHTMLColor());
			}

			final String currencyCode = StringUtils.isEmpty(numericFormat.getCurrencySymbol()) ? "" : numericFormat
					.getCurrencySymbol().trim() + " ";
			detailsLabel.setText(currencyCode + gwtNumberFormat.format(dataItem.getNumber()));
			detailsData.add(detailsLabel);
			break;
		case TYPE_DATE:
		case TYPE_TIME:
		case TYPE_TEXT:
			final String text = StringUtils.defaultString(dataItem.getText());

			// Deal with multiline text differently than single line text.
			if ((formatting != null) && (formatting.getTextFormatMultilineHeightLines() > 1)) {
				detailsData.getElement().getStyle().setOverflow(Overflow.AUTO);
				// Convert '\n' to <br/> escaping the data so that it won't be rendered as HTML.
				try {
					// JavaScript requires the charsetName to be "UTF-8". CharsetName values that work in Java (such as
					// "UTF8") will not work when compiled to JavaScript.
					final String utf8NewLine = new String(new byte[] { 0x0A }, "UTF-8");
					final String[] lines = text.split(utf8NewLine);
					final SafeHtmlBuilder sb = new SafeHtmlBuilder();
					for (final String line : lines) {
						sb.append(SafeHtmlUtils.fromString(line));
						sb.append(SafeHtmlUtils.fromSafeConstant("<br/>"));
					}

					// Manually add the HTML to the detailsData container.
					final DivElement div = Document.get().createDivElement();
					div.setInnerHTML(sb.toSafeHtml().asString());
					detailsData.getElement().appendChild(div);

					// Expand the width of detailsData if a vertical scrollbar has been placed on the inside of the
					// detailsData container.
					final int scrollBarWidth = detailsData.getOffsetWidth() - div.getOffsetWidth();
					if (scrollBarWidth > 0) {
						// A vertical scrollbar is on the inside.
						detailsData.setWidth((detailsData.getOffsetWidth() + scrollBarWidth + 4) + "px");
					}

					// TODO Add horizontal scroll bars when detailsData expands beyond its container.

				} catch (final UnsupportedEncodingException e) {
					// If the new String() line throws an exception, don't try to add the <br/> tags. This is unlikely
					// to happen but we should do something if it does.
					detailsLabel.setText(text);
					detailsData.add(detailsLabel);
				}

			} else {
				final SingleLineText textPanel = new SingleLineText(text);
				detailsData.add(textPanel);
			}
			break;
		case TYPE_IMAGE:
			final Image image = new Image();
			final String imageDataUrl = dataItem.getImageDataUrl();
			if (imageDataUrl != null) {
				image.setUrl(imageDataUrl);

				// Set an arbitrary default size:
				// image.setPixelSize(200, 200);
			}

			detailsData.add(image);
			break;
		default:
			break;
		}

		this.dataItem = dataItem;

		// enable the navigation button if it's safe
		if (openButton != null && openButtonHandlerReg != null && this.dataItem != null) {
			openButton.setEnabled(true);
		}

	}

	public LayoutItemWithFormatting getLayoutItem() {
		return layoutItem;
	}

	public HandlerRegistration setOpenButtonClickHandler(final ClickHandler clickHandler) {
		if (openButton != null) {
			openButtonHandlerReg = openButton.addClickHandler(clickHandler);
		}
		return openButtonHandlerReg;
	}

}
