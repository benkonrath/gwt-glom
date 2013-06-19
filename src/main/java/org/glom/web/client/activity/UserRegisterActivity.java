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

package org.glom.web.client.activity;

import org.glom.web.client.ClientFactory;
import org.glom.web.client.place.GlomPlace;
import org.glom.web.client.ui.UserRegisterView;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class UserRegisterActivity extends GlomActivity {

	private final UserRegisterView view;

	public UserRegisterActivity(final GlomPlace place, final ClientFactory clientFactory) {
		super(place, clientFactory);
		this.view = clientFactory.getUserRegisterView();
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {

		// register this class as the presenter
		view.setPresenter(this);

		// indicate that the view is ready to be displayed
		panel.setWidget(view.asWidget());
	}

	@Override
	protected void clearView() {
		super.clearView();
		view.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.AbstractActivity#onCancel()
	 */
	@Override
	public void onCancel() {
		clearView();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.activity.shared.AbstractActivity#onStop()
	 */
	@Override
	public void onStop() {
		clearView();
	}

}
