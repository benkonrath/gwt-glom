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

package org.glom.web.client.ui.cell;

import org.glom.web.client.StringUtils;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Cell renderer for {@link GlomFieldType} TYPE_NUMERIC.
 */
public class NumericCell extends AbstractCell<Double> {
	private SafeHtml colourCSSProp;
	private SafeHtml backgroundColourCSSProp;
	private NumberFormat numberFormat;
	private boolean useAltColourForNegatives;
	private String currencyCode;

	// TODO Find a way to set the colours on the whole column
	public NumericCell(String foregroundColour, String backgroundColour, NumberFormat numberFormat,
			boolean useAltColourForNegatives, String currencyCode) {
		if (!StringUtils.isEmpty(foregroundColour)) {
			colourCSSProp = SafeHtmlUtils.fromString("color:" + foregroundColour + ";");
		} else {
			colourCSSProp = SafeHtmlUtils.fromSafeConstant("");
		}
		if (!StringUtils.isEmpty(backgroundColour)) {
			backgroundColourCSSProp = SafeHtmlUtils.fromString("background-color:" + backgroundColour + ";");
		} else {
			backgroundColourCSSProp = SafeHtmlUtils.fromSafeConstant("");
		}
		this.numberFormat = numberFormat;
		this.useAltColourForNegatives = useAltColourForNegatives;
		this.currencyCode = StringUtils.isEmpty(currencyCode) ? "" : currencyCode + " ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object,
	 * com.google.gwt.safehtml.shared.SafeHtmlBuilder)
	 */
	@Override
	public void render(Context context, Double value, SafeHtmlBuilder sb) {
		if (value == null) {
			// The value is from an empty row.
			sb.appendHtmlConstant("&nbsp;");
			return;
		}

		// set the foreground colour to red if the number is negative and this is requested
		if (useAltColourForNegatives && value.doubleValue() < 0) {
			// The default alternative colour in libglom is red.
			colourCSSProp = SafeHtmlUtils.fromString("color: #FF0000;");
		}

		// Convert the number to a string and set some CSS properties on the text.
		// The overflow and text-overflow properties tell the browser to add an ellipsis when the text overflows the
		// table cell.
		// FIXME this isn't using safe html correctly!
		sb.appendHtmlConstant("<div style=\"overflow: hidden; text-overflow: ellipsis; " + colourCSSProp.asString()
				+ backgroundColourCSSProp.asString() + "\">");
		sb.append(SafeHtmlUtils.fromString(currencyCode + numberFormat.format(value)));
		sb.appendHtmlConstant("</div>");

	}
}