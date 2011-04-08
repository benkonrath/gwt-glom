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

package org.glom.web.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DemoSelectionViewImpl extends Composite implements DemoSelectionView {

	interface DemoSelectionViewImplUiBinder extends UiBinder<Widget, DemoSelectionViewImpl> {
	}

	private static DemoSelectionViewImplUiBinder uiBinder = GWT.create(DemoSelectionViewImplUiBinder.class);
	@UiField
	VerticalPanel demoLinks;

	public DemoSelectionViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void addHyperLink(String dbTitle, String targetHistoryToken) {
		demoLinks.add(new Hyperlink(dbTitle, targetHistoryToken));
	}

	public void clearHyperLinks() {
		demoLinks.clear();
	}

}
