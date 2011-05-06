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

import org.glom.web.client.place.ListPlace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DocumentSelectionViewImpl extends Composite implements DocumentSelectionView {

	interface DocumentSelectionViewImplUiBinder extends UiBinder<Widget, DocumentSelectionViewImpl> {
	}

	private static DocumentSelectionViewImplUiBinder uiBinder = GWT.create(DocumentSelectionViewImplUiBinder.class);
	@UiField
	VerticalPanel documentLinks;
	private Presenter presenter;

	public DocumentSelectionViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void addDocumentLink(final String documentTitle) {
		Anchor link = new Anchor(documentTitle);
		link.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.goTo(new ListPlace(documentTitle));
			}
		});
		documentLinks.add(link);
	}

	public void clearHyperLinks() {
		documentLinks.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.DocumentSelectionView#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String message) {
		clearHyperLinks();
		Label label = new Label(message);
		label.getElement().getStyle().setColor("Red");
		documentLinks.add(label);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
