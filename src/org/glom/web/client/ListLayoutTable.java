package org.glom.web.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ListLayoutTable extends Composite {

	private CellTable<String[]> table = new CellTable<String[]>();

	public ListLayoutTable(String[] headers) {

		// a panel to hold our widgets
		VerticalPanel panel = new VerticalPanel();
		panel.add(table);

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
