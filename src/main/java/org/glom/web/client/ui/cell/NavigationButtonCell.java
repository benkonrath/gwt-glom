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

import org.glom.web.client.ui.list.ListTable;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Cell renderer for {@link ListTable} open buttons.
 */
public class NavigationButtonCell extends ButtonCell {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.cell.client.ButtonCell#render(com.google.gwt.cell.client.Cell.Context,
	 * com.google.gwt.safehtml.shared.SafeHtml, com.google.gwt.safehtml.shared.SafeHtmlBuilder)
	 */
	@Override
	public void render(final Context context, final SafeHtml data, final SafeHtmlBuilder sb) {
		if (data == null) {
			// The value is from an empty row when the data is null.
			// Use a disabled invisible button with a capital letter so that the height of an empty row is the same as
			// the height of a row with data.
			sb.appendHtmlConstant("<button type=\"button\" style=\"visibility:hidden;\">B</button>");
		} else {
			super.render(context, data, sb);
		}
	}

}
