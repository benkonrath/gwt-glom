package org.glom.web.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
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
	private HorizontalPanel hPanel = new HorizontalPanel();
	private ListBox dropBox = new ListBox();
	private ListLayoutTable table = null;
	private String documentName = "";

	private LibGlomServiceAsync libGlomSvc = GWT.create(LibGlomService.class);

	public void onModuleLoad() {

		dropBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				updateTable();
			}
		});

		hPanel.add(new Label("Table:"));
		hPanel.add(dropBox);
		
		mainVPanel.add(hPanel);

		// associate the main panel with the HTML host page
		RootPanel.get().add(mainVPanel);
		
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
				GlomTable[] tables = result.getTables();
				for (int i = 0; i < tables.length; i++) {
					dropBox.addItem(tables[i].getTitle(), tables[i].getName());
				}
				dropBox.setSelectedIndex(result.getDefaultTableIndex());
				documentName = result.getTitle();
				updateTable();
			}
		};

		// make the call to get the filled in GlomDocument
		libGlomSvc.getGlomDocument(callback);
	}

	private void updateTable() {
		// initialize the service proxy
		if (libGlomSvc == null) {
			libGlomSvc = GWT.create(LibGlomService.class);
		}

		// set up the callback object.
		AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: LibGlomService.updateTable()");
			}
			@Override
			public void onSuccess(String[] result) {
				if (table != null)
					mainVPanel.remove(table);
				table = new ListLayoutTable(result);
				mainVPanel.add(table);
				Window.setTitle("OnlineGlom - " + documentName + ": " + dropBox.getItemText(dropBox.getSelectedIndex()));
			}
		};

		String selectedTable = dropBox.getValue(dropBox.getSelectedIndex());
		libGlomSvc.getLayoutListHeaders(selectedTable, callback);

	}
}
