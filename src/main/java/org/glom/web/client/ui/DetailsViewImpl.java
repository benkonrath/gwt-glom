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

import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItemField;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class DetailsViewImpl extends Composite implements DetailsView {

	@SuppressWarnings("unused")
	private Presenter presenter;
	// private final VerticalPanel mainPanel = new VerticalPanel();
	private final FlowPanel mainPanel = new FlowPanel();

	public DetailsViewImpl() {
		initWidget(mainPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#setPresenter(org.glom.web.client.ui.DetailsView.Presenter)
	 */
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#addLayoutGroup(org.glom.web.shared.layout.LayoutGroup)
	 */
	@Override
	public void addLayoutGroup(LayoutGroup layoutGroup) {
		FlowPanel group = new FlowPanel();
		group.setStyleName("group");

		Label label = new Label(layoutGroup.getTitle());
		label.setStyleName("group-title");
		group.add(label);

		mainPanel.add(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#addLayoutField(org.glom.web.shared.layout.LayoutItemField)
	 */
	@Override
	public void addLayoutField(LayoutItemField layoutItem) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#clear()
	 */
	@Override
	public void clear() {
		mainPanel.clear();
	}

}
