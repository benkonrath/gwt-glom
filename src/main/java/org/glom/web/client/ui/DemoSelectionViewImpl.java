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
