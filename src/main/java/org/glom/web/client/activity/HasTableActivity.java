/*
 * Copyright (C) 2012 Openismus GmbH
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
import org.glom.web.client.place.HasTablePlace;


/**
 * @author Ben Konrath <ben@bagu.org>
 *
 */
abstract class HasTableActivity extends HasDocumentActivity {

	final String tableName;

	/**
	 *
	 */
	HasTableActivity(final HasTablePlace place, final ClientFactory clientFactory) {
		super(place, clientFactory);
		this.tableName = place.getTableName();
	}

}