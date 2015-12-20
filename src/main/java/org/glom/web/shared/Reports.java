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

package org.glom.web.shared;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * TODO: Can't we have a list of pairs of DocumentInfo instead, avoiding the custom class?
 */
@SuppressWarnings("serial")
public class Reports implements Serializable {
	/*
	 * Don't make these final, because that breaks GWT serialization. See
	 * http://code.google.com/p/google-web-toolkit/issues/detail?id=1054
	 */
	// TODO: Test that.
	private/* final */ArrayList<String> names = new ArrayList<>();
	private/* final */ArrayList<String> titles = new ArrayList<>();

	public Reports() {
	}

	public void addReport(final String name, final String title) {
		names.add(name);
		titles.add(title);
	}

	public String getName(final int index) {
		return names.get(index);
	}

	public String getTitle(final int index) {
		return titles.get(index);
	}

	public int getCount() {
		return names.size();
	}
}
