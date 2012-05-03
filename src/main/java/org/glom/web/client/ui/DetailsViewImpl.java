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

import java.util.ArrayList;

import org.glom.web.client.ui.details.DetailsCell;
import org.glom.web.client.ui.details.Group;
import org.glom.web.client.ui.details.Portal;
import org.glom.web.shared.libglom.layout.LayoutGroup;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * 
 */
public class DetailsViewImpl extends Composite implements DetailsView {

	@SuppressWarnings("unused")
	private Presenter presenter;
	private final FlowPanel mainPanel = new FlowPanel();
	private final ArrayList<DetailsCell> cells = new ArrayList<DetailsCell>();
	private final ArrayList<Portal> portals = new ArrayList<Portal>();

	public DetailsViewImpl() {
		initWidget(mainPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.View#setPresenter(org.glom.web.client.ui.View.Presenter)
	 */
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#addGroup(LayoutGroup)
	 */
	@Override
	public void addGroup(LayoutGroup layoutGroup) {
		Group group = new Group(layoutGroup);
		cells.addAll(group.getCells());
		portals.addAll(group.getPortals());
		mainPanel.add(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#clear()
	 */
	@Override
	public void clear() {
		mainPanel.clear();
		cells.clear();
		portals.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#getCells()
	 */
	@Override
	public ArrayList<DetailsCell> getCells() {
		return cells;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#getPortals()
	 */
	@Override
	public ArrayList<Portal> getPortals() {
		return portals;
	}

}
