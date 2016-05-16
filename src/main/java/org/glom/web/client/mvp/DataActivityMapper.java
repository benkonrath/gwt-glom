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
import org.glom.web.client.activity.DetailsActivity;
import org.glom.web.client.activity.ListActivity;
import org.glom.web.client.activity.ReportActivity;
import org.glom.web.client.place.DetailsPlace;
import org.glom.web.client.place.HasRecordsPlace;
import org.glom.web.client.place.ListPlace;
import org.glom.web.client.place.ReportPlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

/**
 *
 */
public class DataActivityMapper implements ActivityMapper {

	private final ClientFactory clientFactory;

	/**
	 * ActivityMapper associates each Place with its corresponding {@link Activity}
	 *
	 * @param clientFactory
	 *            Factory to be passed to activities
	 */
	public DataActivityMapper(final ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * Map each Place to its corresponding Activity.
	 */
	@Override
	public Activity getActivity(final Place place) {
		if (place instanceof ListPlace) {
			return new ListActivity((HasRecordsPlace) place, clientFactory);
		}
		if (place instanceof DetailsPlace) {
			return new DetailsActivity((DetailsPlace) place, clientFactory);
		}
		if (place instanceof ReportPlace) {
			return new ReportActivity((ReportPlace) place, clientFactory);
		}

		return null;
	}

}
