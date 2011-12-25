/*
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class SingleLineText extends Composite {

	HandlerRegistration resizeHandlerReg;

	public SingleLineText(String text) {
		Label dataLabel = new Label();
		Style labelStyle = dataLabel.getElement().getStyle();
		labelStyle.setOverflow(Overflow.HIDDEN);
		labelStyle.setProperty("textOverflow", "ellipsis");
		dataLabel.setText(text);
		initWidget(dataLabel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.Widget#onLoad()
	 */
	@Override
	protected void onLoad() {
		setNewWidth();
		getElement().getStyle().setProperty("whiteSpace", "nowrap");

		resizeHandlerReg = Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				setNewWidth();
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.Widget#onUnload()
	 */
	@Override
	protected void onUnload() {
		resizeHandlerReg.removeHandler();
	}

	private void setNewWidth() {
		// Don't set the width if a navigation button is present.
		if (getElement().getParentElement().getNextSiblingElement() != null) {
			return;
		}

		// Set the new width.
		int parentWidth = getElement().getParentElement().getParentElement().getOffsetWidth();
		int labelWidth = getElement().getParentElement().getPreviousSibling().<Element> cast().getOffsetWidth();
		// Make the new width slights smaller than it should be so that it doesn't fall into a second line.
		int newWidth = parentWidth - labelWidth - 2;
		// Don't set negative widths.
		if (newWidth >= 0)
			setWidth(newWidth + "px");
	}
}
