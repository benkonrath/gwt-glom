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

package org.glom.web.client.activity;

import org.glom.web.client.ClientFactory;
import org.glom.web.client.place.GlomPlace;
import org.glom.web.client.ui.View;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;

/**
 * @author Murray Cumming <murrayc@openismus.com>
 *
 */
public abstract class GlomActivity extends AbstractActivity implements View.Presenter {

	protected final ClientFactory clientFactory;

	/**
	 * @param place 
	 * @param clientFactory
	 * 
	 */
	public GlomActivity(final GlomPlace place, final ClientFactory clientFactory) {
		super();
		
		this.clientFactory = clientFactory;
	}

	@Override
	public void goTo(final Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

	/**
	 * 
	 */
	protected void clearView() {
		// TODO Auto-generated method stub
		
	}

}