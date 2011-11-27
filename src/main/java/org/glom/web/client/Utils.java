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

package org.glom.web.client;

import org.glom.web.shared.DataItem;
import org.glom.web.shared.GlomNumericFormat;
import org.glom.web.shared.TypedDataItem;
import org.glom.web.shared.layout.LayoutItemField.GlomFieldType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class Utils {

	public static NumberFormat getNumberFormat(GlomNumericFormat glomNumericFormat) {

		StringBuilder pattern = new StringBuilder("0.");

		// add pattern for thousands separator
		if (glomNumericFormat.getUseThousandsSeparator()) {
			pattern.insert(0, "#,##");
		}

		// add pattern for restricted decimal places
		if (glomNumericFormat.getDecimalPlacesRestricted()) {
			for (int i = 0; i < glomNumericFormat.getDecimalPlaces(); i++) {
				pattern.append('0');
			}
		} else {
			// The default precision in libglom is 15.
			pattern.append("###############");
		}

		// TODO use exponential numbers when more than 15 decimal places

		return NumberFormat.getFormat(pattern.toString());

	}

	/**
	 * Get the vertical height with decorations (i.e. CSS) by temporarily adding the widget to the body element of the
	 * document in a transparent container. This is required because the size information is only available when the
	 * widget is attached to the DOM.
	 * 
	 * This method must be called before the widget is added to its container because it will be removed from any
	 * container it is already inside. TODO: Fix this problem by saving a reference to its parent and re-addding it
	 * after the height information has been calculated.
	 * 
	 * TODO: Remove duplicate code in FlowTable.FlowTableItem
	 * 
	 * @param widget
	 *            get the height information for this widget
	 * @return the height of the widget with styling applied
	 */
	public static int getWidgetHeight(Widget widget) {
		Document doc = Document.get();
		com.google.gwt.dom.client.Element div = doc.createDivElement();

		div.getStyle().setOpacity(0.0);
		div.appendChild(widget.getElement().<com.google.gwt.user.client.Element> cast());

		doc.getBody().appendChild(div);
		int height = widget.getOffsetHeight();
		doc.getBody().removeChild(div);

		return height;
	}

	public static TypedDataItem getTypedDataItem(GlomFieldType glomFieldType, DataItem dataItem) {
		TypedDataItem primaryKeyItem = new TypedDataItem();
		switch (glomFieldType) {
		case TYPE_BOOLEAN:
			primaryKeyItem.setBoolean(dataItem.getBoolean());
			break;
		case TYPE_NUMERIC:
			primaryKeyItem.setNumber(dataItem.getNumber());
			break;
		case TYPE_TEXT:
			primaryKeyItem.setText(new String(dataItem.getText() == null ? "" : dataItem.getText()));
			break;
		default:
			GWT.log("getTypedDataItem(): Unsupported Glom Field Type: " + glomFieldType);
			break;
		}

		return primaryKeyItem;
	}

}
