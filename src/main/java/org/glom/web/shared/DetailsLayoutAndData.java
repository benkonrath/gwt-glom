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
import java.util.List;

import org.glom.web.shared.libglom.layout.LayoutGroup;

/**
 * Wrapper DTO for details view layout and data.
 */
@SuppressWarnings("serial")
public class DetailsLayoutAndData implements Serializable {
	private List<LayoutGroup> layout;
	private DataItem[] data;

	public List<LayoutGroup> getLayout() {
		return layout;
	}

	public void setLayout(List<LayoutGroup> layout) {
		this.layout = layout;
	}

	public DataItem[] getData() {
		return data;
	}

	public void setData(DataItem[] data) {
		this.data = data;
	}

}
