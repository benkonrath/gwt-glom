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

import org.glom.web.shared.libglom.Field.GlomFieldType;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell renderer for {@link GlomFieldType} TYPE_BOOLEAN.
 */
public class BooleanCell extends CheckboxCell {

	public BooleanCell() {
		super(false, false);
	}

	/*
	 * This method is overridden to ensure the user can't toggle the checkbox. This method can be removed when support
	 * for editing is added.
	 *
	 * @see com.google.gwt.cell.client.CheckboxCell#onBrowserEvent(com.google.gwt.cell.client.Cell.Context,
	 * com.google.gwt.dom.client.Element, java.lang.Boolean, com.google.gwt.dom.client.NativeEvent,
	 * com.google.gwt.cell.client.ValueUpdater)
	 */
	@Override
	public void onBrowserEvent(final com.google.gwt.cell.client.Cell.Context context, final Element parent,
			final Boolean value, final NativeEvent event, final ValueUpdater<Boolean> valueUpdater) {
		final String type = event.getType();
		final boolean enterPressed = "keydown".equals(type) && event.getKeyCode() == KeyCodes.KEY_ENTER;
		if ("change".equals(type) || enterPressed) {
			final InputElement input = parent.getFirstChild().cast();
			input.setChecked(!input.isChecked());
		}
	}

	/*
	 * This method is overridden to handle rendering empty rows.
	 *
	 * @see com.google.gwt.cell.client.CheckboxCell#render(com.google.gwt.cell.client.Cell.Context, java.lang.Boolean,
	 * com.google.gwt.safehtml.shared.SafeHtmlBuilder)
	 */
	@Override
	public void render(final com.google.gwt.cell.client.Cell.Context context, final Boolean value,
			final SafeHtmlBuilder sb) {
		if (value == null) {
			// The value is from an empty row.
			sb.appendHtmlConstant("&nbsp;");
		} else {
			super.render(context, value, sb);
		}
	}

}
