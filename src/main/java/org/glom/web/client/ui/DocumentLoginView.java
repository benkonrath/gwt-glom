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


/**
 *
 */
public interface DocumentLoginView extends View {

	/** Specify a handler to be called when the user clicks the Login button
	 * to attempt authentication.
	 * 
	 * @param clickHandler
	 */
	void setClickLoginHandler(final ClickHandler clickHandler);
	
	/** Specify a handler to be called when the user clicks the cancel button
	 * to cancel the attempt at authentication.
	 * 
	 * @param clickHandler
	 */
	void setClickCancelHandler(final ClickHandler clickHandler);

	void setTextFieldsEnabled(final boolean enabled);

	String getUsername();

	String getPassword();

	void setError();

	void clearError();
}
