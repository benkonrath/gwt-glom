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

package org.glom.web.client.ui.details;

import java.util.ArrayList;

import org.glom.web.client.StringUtils;
import org.glom.web.shared.libglom.layout.LayoutGroup;
import org.glom.web.shared.libglom.layout.LayoutItem;
import org.glom.web.shared.libglom.layout.LayoutItemField;
import org.glom.web.shared.libglom.layout.LayoutItemImage;
import org.glom.web.shared.libglom.layout.LayoutItemNotebook;
import org.glom.web.shared.libglom.layout.LayoutItemPortal;
import org.glom.web.shared.libglom.layout.LayoutItemText;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class Group extends Composite {
	private final FlowPanel mainPanel = new FlowPanel();
	private final ArrayList<DetailsCell> cells = new ArrayList<>();
	private final ArrayList<Portal> portals = new ArrayList<>();

	protected Group() {
		// can used by sub-classes
	}

	/**
	 * Creates a new widget for a main Group in the Details View.
	 * 
	 * @param layoutGroup
	 *            The DTO that holds the Group or sub-Group layout information
	 */
	public Group(final LayoutGroup layoutGroup) {
		// is default is a group that is not a sub
		this(layoutGroup, false, true);
	}

	/**
	 * Creates a new widget for a Group or sub-Group.
	 * 
	 * @param layoutGroup
	 *            The DTO that holds the Group or sub-Group layout information
	 * @param subGroup
	 *            true if the layoutGroup is a sub-Group, false if it's a Group
	 * @param setGroupTitle
	 *            whether or not to add a title label for the Group
	 */
	private Group(final LayoutGroup layoutGroup, final boolean subGroup, final boolean setGroupTitle) {

		mainPanel.setStyleName(subGroup ? "subgroup" : "group");

		FlowPanel groupContents;
		final String groupTitle = layoutGroup.getTitle();

		if (setGroupTitle && !StringUtils.isEmpty(groupTitle)) {
			final Label label = new Label(groupTitle);
			mainPanel.add(label);

			label.setStyleName(subGroup ? "subgroup-title" : "group-title");

			groupContents = new FlowPanel();
			groupContents.setStyleName("group-contents");
			mainPanel.add(groupContents);
		} else {
			// Don't make a separate contents panel when the group title is not being set.
			groupContents = mainPanel;
		}

		final FlowTable flowtable = new FlowTable(layoutGroup.getColumnCount());
		for (final LayoutItem layoutItem : layoutGroup.getItems()) {
			final Widget child = createChildWidget(layoutItem, true);
			flowtable.add(child);
		}
		groupContents.add(flowtable);
		initWidget(mainPanel);
	}

	public ArrayList<DetailsCell> getCells() {
		return cells;
	}

	public ArrayList<Portal> getPortals() {
		return portals;
	}

	/**
	 * Creates a child widget for the specified LayoutItem and updates the cells and portals field appropriately. This
	 * can be used by subclasses like {@link Notebook}.
	 * 
	 * @param layoutItem
	 *            The DTO that holds the layout information
	 * @param addGroupTitle
	 *            whether or not to add a title label for the Group
	 */
	protected Widget createChildWidget(final LayoutItem layoutItem, final boolean addGroupTitle) {

		if (layoutItem instanceof LayoutItemField) {

			// create a DetailsCell
			final DetailsCell detailsCell = new DetailsCell((LayoutItemField) layoutItem);
			cells.add(detailsCell); //Store it so we can set its data later.
			return detailsCell;

		} else if (layoutItem instanceof LayoutItemText) {
			return new DetailsCell((LayoutItemText) layoutItem);
		} else if (layoutItem instanceof LayoutItemImage) {
			return new DetailsCell((LayoutItemImage) layoutItem);
		} else if (layoutItem instanceof LayoutGroup) {

			if (layoutItem instanceof LayoutItemPortal) {

				// create a Portal
				final Portal portal = new Portal((LayoutItemPortal) layoutItem, addGroupTitle);
				portals.add(portal);
				return portal;

			} else if (layoutItem instanceof LayoutItemNotebook) {

				// create a Notebook
				final Notebook notebook = new Notebook((LayoutItemNotebook) layoutItem);
				cells.addAll(notebook.getCells());
				portals.addAll(notebook.getPortals());
				return notebook;

			} else {

				// create a subgroup Group
				final Group subGroup = new Group((LayoutGroup) layoutItem, true, addGroupTitle);
				cells.addAll(subGroup.getCells());
				portals.addAll(subGroup.getPortals());
				return subGroup;
			}
		}

		return null; // This should never happen.
	}

}