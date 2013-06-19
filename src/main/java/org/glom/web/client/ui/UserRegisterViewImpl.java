/*
 * Copyright (C) 2010, 2011 Openismus GmbH
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

public class UserRegisterViewImpl extends GlomComposite implements UserRegisterView {

	// OnlineGlomConstants.java is generated in the target/ directory,
	// from OnlineGlomConstants.properties
	// by the gwt-maven-plugin's i18n (mvn:i18n) goal.
	private final OnlineGlomConstants constants = GWT.create(OnlineGlomConstants.class);

	interface UserRegisterViewImplUiBinder extends UiBinder<Widget, UserRegisterViewImpl> {
	}

	private static UserRegisterViewImplUiBinder uiBinder = GWT.create(UserRegisterViewImplUiBinder.class);
	@UiField
	VerticalPanel userRegisterFields;
	@UiField
	HTMLPanel userRegisterPanel;

	//final private FlowPanel mainPanel = new FlowPanel();
	private final TextBox usernameTextBox = new TextBox();
	private final PasswordTextBox passwordTextBox = new PasswordTextBox();
	//TODO: Password confirmation.
	private final Label errorMessage = new Label(constants.loginWrong());
	private final Button registerButton = new Button(constants.loginRegister());
	private final Button cancelButton = new Button(constants.cancel());
	FlexTable flexTable = new FlexTable();

	//private Presenter presenter;
	
	private HandlerRegistration authRegisterButtonHandlerRegistration;
	private HandlerRegistration authCancelButtonHandlerRegistration;

	public UserRegisterViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		userRegisterPanel.getElement().setId("userRegisterPanel");
		userRegisterFields.addStyleName("userRegisterFields");
		
		userRegisterFields.add(flexTable);
		
		//TODO: Use UIBinder to lay this out properly:
		flexTable.setCellSpacing(10);
		final FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();
		flexTable.setHTML(0, 0, "<b>" + constants.loginEnter() + "</b>");
		cellFormatter.setColSpan(0, 0, 2);
		cellFormatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		flexTable.setHTML(1, 0, constants.loginUsername());
		flexTable.setWidget(1, 1, usernameTextBox);
		cellFormatter.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		flexTable.setHTML(2, 0, constants.loginPassword());
		flexTable.setWidget(2, 1, passwordTextBox);
		cellFormatter.setHorizontalAlignment(2, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		flexTable.setWidget(3, 0, cancelButton);
		cellFormatter.setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		flexTable.setWidget(3, 1, registerButton);
		cellFormatter.setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);

		final String protocol = Window.Location.getProtocol();
		if((protocol != null) && !protocol.equals("https:")) {
			//Warn that login cannot work unless the login servlet is served via HTTPS.
			//And actually, the entire site must be served via HTTPS,
			//or we would violate the Same Origin Policy by mixing protocols,
			//so this very page should have been delivered by HTTPS.
			GWT.log("The registraion page arrived via http, rather than https. Refusing to allow registration.");

			errorMessage.setText(constants.loginNeedsHttps());
			setError();
			registerButton.setEnabled(false);
		}
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		//this.presenter = presenter;
	}
	
	@Override
	public void setClickRegisterHandler(final ClickHandler clickHandler) {
		authRegisterButtonHandlerRegistration = registerButton.addClickHandler(clickHandler);
	}
	
	@Override
	public void setClickCancelHandler(final ClickHandler clickHandler) {
		authCancelButtonHandlerRegistration = cancelButton.addClickHandler(clickHandler);
	}

	@Override
	public void clear() {
		if (authRegisterButtonHandlerRegistration != null) {
			authRegisterButtonHandlerRegistration.removeHandler();
			authRegisterButtonHandlerRegistration = null;
		}

		if (authCancelButtonHandlerRegistration != null) {
			authCancelButtonHandlerRegistration.removeHandler();
			authCancelButtonHandlerRegistration = null;
		}

		usernameTextBox.setText("");
		passwordTextBox.setText("");
		clearError();
	}
	
	@Override
	public void setError() {
		flexTable.setWidget(4, 0, errorMessage);
		final FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();
		cellFormatter.setColSpan(4, 0, 2);
		cellFormatter.setHorizontalAlignment(4, 0, HasHorizontalAlignment.ALIGN_LEFT);
		errorMessage.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
	}
	
	@Override
	public void clearError() {
		if (flexTable.getRowCount() == 5) {
			flexTable.removeRow(4);
		}
	}

}
