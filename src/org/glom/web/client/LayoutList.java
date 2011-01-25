package org.glom.web.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LayoutList extends Composite {

	private CellTable<String[]> table = null;

	public LayoutList(String[] headers) {
		table = new CellTable<String[]>();
		LayoutListDataProvider dataProvider = new LayoutListDataProvider();
		dataProvider.addDataDisplay(table);

		// TODO wire up the pager
		SimplePager pager = new SimplePager(SimplePager.TextLocation.CENTER);
		pager.setDisplay(table);

		// a panel to hold our widgets
		VerticalPanel panel = new VerticalPanel();
		panel.add(table);
		panel.add(pager);

		for (int i = 0; i < headers.length; i++) {
			// create a new column
			final int j = new Integer(i);
			TextColumn<String[]> column = new TextColumn<String[]>() {
				@Override
				public String getValue(String[] object) {
					return object[j];
				}
			};

			// add the column to the list
			table.addColumn(column, headers[i]);

		}

		table.setRowCount(0);

		// take care of the necessary stuff required for composite widgets
		initWidget(panel);
		setStyleName("glom-ListLayoutTable");
	}

}
