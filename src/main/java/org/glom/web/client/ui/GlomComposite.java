/*
 * Copyright (C) 2013 Openismus GmbH
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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Ben Konrath <ben@bagu.org>
 *
 */
public abstract class GlomComposite extends Composite {

	/**
	 * 
	 */
	public GlomComposite() {
		super();
	}

	/**
	 * @return
	 */
	protected boolean usingSecureProtocol() {

		final String protocol = Window.Location.getProtocol();
		if(protocol == null)
			return false; //This seems safer than assuming that we are secure.
	
		return protocol.equals("https:");
	}

}