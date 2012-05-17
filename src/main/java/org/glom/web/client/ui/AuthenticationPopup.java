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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 */
public class AuthenticationPopup extends DecoratedPopupPanel {
	private final TextBox usernameTextBox = new TextBox();
	private final PasswordTextBox passwordTextBox = new PasswordTextBox();
	private final Label errorMessage = new Label("Username and/or password not correct.");
	private final Button okButton = new Button("OK");
	FlexTable flexTable = new FlexTable();

	private HandlerRegistration authOkHandlerRegistration;

	public AuthenticationPopup() {
		// setup the layout
		flexTable.setCellSpacing(10);
		final FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();
		flexTable.setHTML(0, 0, "<b>Enter the PostgreSQL username and password.</b>");
		cellFormatter.setColSpan(0, 0, 2);
		cellFormatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		flexTable.setHTML(1, 0, "Username");
		flexTable.setWidget(1, 1, usernameTextBox);
		cellFormatter.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		flexTable.setHTML(2, 0, "Password");
		flexTable.setWidget(2, 1, passwordTextBox);
		cellFormatter.setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		flexTable.setWidget(3, 1, okButton);
		cellFormatter.setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);

		// setup the popup
		setGlassEnabled(true);
		setWidget(flexTable);

		// setup properties for the error message
		errorMessage.getElement().getStyle().setColor("Red");
	}

	public void setClickOkHandler(final ClickHandler clickHandler) {
		authOkHandlerRegistration = okButton.addClickHandler(clickHandler);
	}

	public String getUsername() {
		return usernameTextBox.getValue();
	}

	public String getPassword() {
		return passwordTextBox.getValue();

	}

	@Override
	public void clear() {
		if (authOkHandlerRegistration != null) {
			authOkHandlerRegistration.removeHandler();
			authOkHandlerRegistration = null;
		}
		usernameTextBox.setText("");
		passwordTextBox.setText("");
		setTextFieldsEnabled(true);
		clearError();
	}

	public void setTextFieldsEnabled(final boolean enabled) {
		usernameTextBox.setEnabled(enabled);
		passwordTextBox.setEnabled(enabled);
	}

	public void setError() {
		flexTable.setWidget(4, 0, errorMessage);
		final FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();
		cellFormatter.setColSpan(4, 0, 2);
		cellFormatter.setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_CENTER);
	}

	public void clearError() {
		if (flexTable.getRowCount() == 5) {
			flexTable.removeRow(4);
		}
	}
}
