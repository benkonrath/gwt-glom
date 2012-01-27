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

package org.glom.web.client.mvp;

import org.glom.web.client.ClientFactory;
import org.glom.web.client.activity.TableSelectionActivity;
import org.glom.web.client.place.HasTablePlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

/**
 *
 */
public class TableSelectionActivityMapper implements ActivityMapper {

	private TableSelectionActivity tableSelectionActivity;

	/**
	 * ActivityMapper associates each Place with its corresponding {@link Activity}
	 * 
	 * @param clientFactory
	 *            Factory to be passed to activities
	 */
	public TableSelectionActivityMapper(ClientFactory clientFactory) {
		tableSelectionActivity = new TableSelectionActivity(clientFactory);
	}

	/**
	 * Map each Place to its corresponding Activity.
	 */
	@Override
	public Activity getActivity(Place place) {
		// We're not creating new TableSelectionActivity objects for each place because we want the ListBox to keep the
		// table name when the Details button is clicked.
		if (place instanceof HasTablePlace) {
			tableSelectionActivity.setPlace((HasTablePlace) place);
			return tableSelectionActivity;
		}

		return null;
	}
}
