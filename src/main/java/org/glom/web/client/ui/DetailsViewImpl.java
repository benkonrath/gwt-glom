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

import org.glom.web.shared.GlomField;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class DetailsViewImpl extends Composite implements DetailsView {

	@SuppressWarnings("unused")
	private Presenter presenter;
	private final FlowPanel mainPanel = new FlowPanel();
	private final ArrayList<InlineLabel> dataLabels = new ArrayList<InlineLabel>();

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
	public void addLayoutGroup(String title) {
		InlineLabel groupTitle = new InlineLabel(title);
		groupTitle.setStyleName("group-title");

		mainPanel.add(groupTitle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#addLayoutField(org.glom.web.shared.layout.LayoutItemField)
	 */
	@Override
	public void addLayoutField(String title) {

		InlineLabel detailsLabel = new InlineLabel(title);
		detailsLabel.setStyleName("details-label");

		InlineLabel detailsData = new InlineLabel();
		detailsData.setStyleName("details-data");

		FlowPanel fieldPanel = new FlowPanel();
		fieldPanel.setStyleName("details-cell");

		fieldPanel.add(detailsLabel);
		fieldPanel.add(detailsData);

		dataLabels.add(detailsData);

		mainPanel.add(fieldPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#setData(org.glom.web.shared.GlomField[])
	 */
	public void setData(GlomField[] glomFields) {
		if (glomFields != null) {
			for (int i = 0; i < glomFields.length; i++) {
				InlineLabel dataLabel = dataLabels.get(i);
				dataLabel.setText(glomFields[i].getText());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#clear()
	 */
	@Override
	public void clear() {
		mainPanel.clear();
		dataLabels.clear();
	}
}
