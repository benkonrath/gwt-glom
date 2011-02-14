/*
 * Copyright (C) 2010, 2011 Openismus GmbH
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

package org.glom.web.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class LayoutListDataProvider extends AsyncDataProvider<String[]> {

	@Override
	protected void onRangeChanged(HasData<String[]> display) {
		final Range range = display.getVisibleRange();

		final int start = range.getStart();
		final int length = range.getLength();

		AsyncCallback<ArrayList<String[]>> callback = new AsyncCallback<ArrayList<String[]>>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: LibGlomService.getTableData()");
			}

			public void onSuccess(ArrayList<String[]> result) {
				updateRowData(start, result);
			}
		};

		LibGlomServiceAsync.Util.getInstance().getTableData(start, length, OnlineGlom.getCurrentTableName(), callback);

	}

}
