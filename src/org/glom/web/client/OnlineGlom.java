package org.glom.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class OnlineGlom implements EntryPoint {

	private VerticalPanel mainVPanel = new VerticalPanel();
	private HorizontalPanel tableSelectorHPanel = new HorizontalPanel();
	private ListBox tableListBox = new ListBox();
	private LibGlomServiceAsync libGlomSvc = GWT.create(LibGlomService.class);

	public void onModuleLoad() {

		tableSelectorHPanel.add(new Label("Table:"));
		tableSelectorHPanel.add(tableListBox);
		
		mainVPanel.add(tableSelectorHPanel);

		// associate the main panel with the HTML host page
		RootPanel.get("GlomWebApp").add(mainVPanel);
		
		// initialize the service proxy
		if (libGlomSvc == null) {
			libGlomSvc = GWT.create(LibGlomService.class);
		}

		// set up the callback object.
		AsyncCallback<GlomDocument> callback = new AsyncCallback<GlomDocument>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: LibGlomService");
			}

			public void onSuccess(GlomDocument result) {
				// TODO set page title
				GlomTable[] tables = result.getTables();
				for (int i = 0; i < tables.length; i++) {
					tableListBox.addItem(tables[i].getTitle());
				}
				tableListBox.setSelectedIndex(result.getDefaultTable());
			}

		};

		// make the call to get the filled in GlomDocument
		libGlomSvc.getGlomDocument(callback);
	}
}
