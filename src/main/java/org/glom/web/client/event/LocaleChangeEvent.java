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

package org.glom.web.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class LocaleChangeEvent extends GwtEvent<LocaleChangeEventHandler> {
	public static Type<LocaleChangeEventHandler> TYPE = new Type<>();
	private final String newLocaleID;

	public LocaleChangeEvent(final String newLocaleID) {
		this.newLocaleID = newLocaleID;
	}

	public String getNewLocaleID() {
		return newLocaleID;
	}

	@Override
	public Type<LocaleChangeEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(final LocaleChangeEventHandler handler) {
		handler.onLocaleChange(this);
	}

	@Override
	public String toDebugString() {
		String name = this.getClass().getName();
		name = name.substring(name.lastIndexOf(".") + 1);
		return "event: " + name + ": " + newLocaleID;
	}
}
