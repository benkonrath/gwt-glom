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

package org.glom.web.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class ReportViewImpl extends Composite implements ReportView {

	final private FlowPanel mainPanel = new FlowPanel();
	final private Label waitingLabel = new Label();
	final private HTML htmlWidget = new HTML();

	@SuppressWarnings("unused")
	private Presenter presenter;

	public ReportViewImpl() {
		initWidget(mainPanel);
		mainPanel.add(waitingLabel);
		mainPanel.add(htmlWidget);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter =
				presenter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glom.web.client.ui.ListView#clear()
	 */
	@Override
	public void clear() {
		waitingLabel.setText("");
		htmlWidget.setHTML("");
	}

	@Override
	public void setReportHTML(final String html) {
		waitingLabel.setText(""); //Because we are not waiting anymore.
		htmlWidget.setHTML(html); // TODO: Use SafeHTML?
	}

	@Override
	public void setWaitingText(final String text) {
		waitingLabel.setText(text);
	}
}
