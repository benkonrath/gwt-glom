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
import org.glom.web.shared.libglom.Field.GlomFieldType;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Cell renderer for {@link GlomFieldType} TYPE_TEXT.
 */
public class TextCell extends AbstractCell<String> {
	SafeHtml colorCSSProp;
	SafeHtml backgroundColorCSSProp;

	// TODO Find a way to set the colors on the whole column
	public TextCell(String foregroundColor, String backgroundColor) {
		if (!StringUtils.isEmpty(foregroundColor)) {
			colorCSSProp = SafeHtmlUtils.fromString("color:" + foregroundColor + ";");
		} else {
			colorCSSProp = SafeHtmlUtils.fromSafeConstant("");
		}
		if (!StringUtils.isEmpty(backgroundColor)) {
			backgroundColorCSSProp = SafeHtmlUtils.fromString("background-color:" + backgroundColor + ";");
		} else {
			backgroundColorCSSProp = SafeHtmlUtils.fromSafeConstant("");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.cell.client.AbstractCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Object,
	 * com.google.gwt.safehtml.shared.SafeHtmlBuilder)
	 */
	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
		if (value == null) {
			// The value is from an empty row.
			sb.appendHtmlConstant("&nbsp;");
			return;
		}

		// Set the text and some CSS properties for the text.
		// The overflow and text-overflow properties tell the browser to add an ellipsis when the text overflows the
		// table cell.
		// FIXME this isn't using safe html correctly!
		sb.appendHtmlConstant("<div style=\"overflow: hidden; text-overflow: ellipsis; " + colorCSSProp.asString()
				+ backgroundColorCSSProp.asString() + "\">");
		sb.append(SafeHtmlUtils.fromString(value));
		sb.appendHtmlConstant("</div>");

	}
}
