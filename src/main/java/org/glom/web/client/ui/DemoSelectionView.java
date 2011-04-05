package org.glom.web.client.ui;

import com.google.gwt.user.client.ui.IsWidget;

public interface DemoSelectionView extends IsWidget {

	void addHyperLink(String dbTitle, String string);

	void clearHyperLinks();

}
