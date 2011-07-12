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
import org.glom.web.shared.layout.LayoutGroup;
import org.glom.web.shared.layout.LayoutItem;
import org.glom.web.shared.layout.LayoutItemField;
import org.glom.web.shared.layout.LayoutItemPortal;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Ben Konrath <ben@bagu.org>
 * 
 */
public class DetailsViewImpl extends Composite implements DetailsView {

	class GroupPanel extends FlowPanel {
		FlowPanel groupContents = new FlowPanel();
		private final ArrayList<InlineLabel> dataLabels = new ArrayList<InlineLabel>();

		public GroupPanel(LayoutGroup layoutGroup) {
			super();
			setStyleName("group");

			String groupTitle = layoutGroup.getTitle();
			if (!groupTitle.isEmpty()) {
				Label label = new Label(groupTitle);
				label.setStyleName("group-title");
				this.add(label);
			}

			groupContents.setStyleName("group-contents");
			this.add(groupContents);

			createLayout(layoutGroup, "");
		}

		private InlineLabel addDetailsCell(String title) {

			InlineLabel detailsLabel = new InlineLabel(title);
			detailsLabel.setStyleName("details-label");

			InlineLabel detailsData = new InlineLabel();
			detailsData.setStyleName("details-data");

			FlowPanel fieldPanel = new FlowPanel();
			fieldPanel.setStyleName("details-cell");

			fieldPanel.add(detailsLabel);
			fieldPanel.add(detailsData);

			groupContents.add(fieldPanel);

			return detailsData;
		}

		/*
		 * This is just a temporary method for creating a basic indented layout without the flowtable.
		 */
		public void createLayout(LayoutGroup layoutGroup, String indent) {
			if (layoutGroup == null)
				return;

			// look at each child item
			ArrayList<LayoutItem> layoutItems = layoutGroup.getItems();
			for (LayoutItem layoutItem : layoutItems) {

				if (layoutItem == null)
					continue;

				String title = layoutItem.getTitle();
				if (layoutItem instanceof LayoutItemField) {
					dataLabels.add(addDetailsCell(indent + title));
				} else if (layoutItem instanceof LayoutItemPortal) {
					// ignore portals for now
					continue;
				} else if (layoutItem instanceof LayoutGroup) {
					// recurse into child groups
					createLayout((LayoutGroup) layoutItem, indent + "-- ");
				}
			}
		}

		public ArrayList<InlineLabel> getDataLabels() {
			return dataLabels;
		}
	}

	@SuppressWarnings("unused")
	private Presenter presenter;
	private final FlowPanel mainPanel = new FlowPanel();
	private final ArrayList<InlineLabel> dataLabels = new ArrayList<InlineLabel>();

	public DetailsViewImpl() {
		// TODO this value should really come from the css file - the body class
		mainPanel.getElement().getStyle().setMargin(1, Unit.EM);
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
		GroupPanel groupPanel = new GroupPanel(layoutGroup);
		dataLabels.addAll(groupPanel.getDataLabels());
		mainPanel.add(groupPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#addLayoutField(org.glom.web.shared.layout.LayoutItemField)
	 */
	@Override
	public void addLayoutField(LayoutItemField layoutItem) {
		// TODO Auto-generated method stub
		// maybe I won't use this but instead create a LayoutGroup for the LayoutItems not in a LayoutGroup
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DetailsView#setData(org.glom.web.shared.GlomField[])
	 */
	public void setData(GlomField[] glomFields) {
		if (glomFields == null) {
			return;
		}

		for (int i = 0; i < dataLabels.size(); i++) {
			InlineLabel dataLabel = dataLabels.get(i);
			dataLabel.setText(glomFields[i] == null ? "" : glomFields[i].getText());
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
