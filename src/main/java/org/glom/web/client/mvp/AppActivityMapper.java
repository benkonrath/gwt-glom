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

import org.glom.web.client.ClientFactoryImpl;
import org.glom.web.client.activity.OnlineGlomActivity;
import org.glom.web.client.place.OnlineGlomPlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

/*
 * This code is mostly from AppActivityMapper.java in the hellomvp GWT example:
 *
 * https://code.google.com/webtoolkit/doc/2.2/DevGuideMvpActivitiesAndPlaces.html
 */
public class AppActivityMapper implements ActivityMapper {

	private final ClientFactoryImpl clientFactory;

	/**
	 * AppActivityMapper associates each Place with its corresponding {@link Activity}
	 * 
	 * @param clientFactory
	 *            Factory to be passed to activities
	 */
	public AppActivityMapper(ClientFactoryImpl clientFactory) {
		super();
		this.clientFactory = clientFactory;
	}

	/**
	 * Map each Place to its corresponding Activity. This would be a great use for GIN.
	 */
	@Override
	public Activity getActivity(Place place) {
		// This is begging for GIN
		if (place instanceof OnlineGlomPlace)
			return new OnlineGlomActivity((OnlineGlomPlace) place, clientFactory);
		return null;
	}

}
