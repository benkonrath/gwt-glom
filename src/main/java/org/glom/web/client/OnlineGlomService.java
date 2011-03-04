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

import org.glom.web.shared.GlomDocument;
import org.glom.web.shared.GlomField;
import org.glom.web.shared.LayoutListTable;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("libGlom")
public interface OnlineGlomService extends RemoteService {

	GlomDocument getGlomDocument();

	LayoutListTable getLayoutListTable(String table);

	ArrayList<GlomField[]> getTableData(String table, int start, int length);

	ArrayList<GlomField[]> getSortedTableData(String table, int start, int length, int sortColumnIndex,
			boolean isAscending);

}