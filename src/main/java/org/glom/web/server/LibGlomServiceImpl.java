package org.glom.web.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.glom.libglom.Document;
import org.glom.libglom.ExampleRowVector;
import org.glom.libglom.Field;
import org.glom.libglom.FieldVector;
import org.glom.libglom.Glom;
import org.glom.libglom.LayoutGroupVector;
import org.glom.libglom.LayoutItem;
import org.glom.libglom.LayoutItemVector;
import org.glom.libglom.RowDataVector;
import org.glom.libglom.StringVector;
import org.glom.web.client.GlomDocument;
import org.glom.web.client.GlomTable;
import org.glom.web.client.LibGlomService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LibGlomServiceImpl extends RemoteServiceServlet implements LibGlomService {
	private Document document;

	public LibGlomServiceImpl() {
		Glom.libglom_init();
		// FIXME Need to call Glom.libglom_deinit()
		document = new Document();
		document.set_file_uri("file://" + Glom.GLOM_EXAMPLE_FILE_DIR + File.separator + "example_music_collection.glom");
		int error = 0;
		@SuppressWarnings("unused")
		boolean retval = document.load(error);
		// FIXME handle error condition
	}

	/*
	 * FIXME I think Swig is generating long on 64-bit machines and int on
	 * 32-bit machines - need to keep this constant
	 * http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
	 */
	public static int safeLongToInt(long l) {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
		}
		return (int) l;
	}

	public GlomDocument getGlomDocument() {
		GlomDocument glomDocument = new GlomDocument();

		// set visible title
		glomDocument.setTitle(document.get_database_title());

		// set array of GlomTables and the default table index
		StringVector tableNames = document.get_table_names();
		GlomTable[] tables = new GlomTable[safeLongToInt(tableNames.size())];
		for (int i = 0; i < tableNames.size(); i++) {
			String tableName = tableNames.get(i);
			GlomTable glomTable = new GlomTable();
			glomTable.setName(tableName);
			glomTable.setTitle(document.get_table_title(tableName));
			tables[i] = glomTable;
			if (tableName.equals(document.get_default_table())) {
				glomDocument.setDefaultTableIndex(i);
			}
		}
		glomDocument.setTables(tables);
		return glomDocument;

	}

	public String[] getLayoutListHeaders(String table) {
		LayoutGroupVector layoutList = document.get_data_layout_groups("list", table);
		LayoutItemVector layoutItems = layoutList.get(0).get_items();
		String[] headers = new String[safeLongToInt(layoutItems.size())];
		for (int i = 0; i < layoutItems.size(); i++) {
			headers[i] = layoutItems.get(i).get_title_or_name();
		}
		return headers;
	}

	/*
	 * This is a big hack just get the Layout List widget working. Next steps
	 * will be adding the example data to a db so that it can be queried from it
	 * rather than from the example data API as I'm doing now.
	 */
	public List<String[]> getTableData(int start, int length, String table) {
		LayoutGroupVector layoutList = document.get_data_layout_groups("list", table);
		LayoutItemVector layoutItems = layoutList.get(0).get_items();
		ExampleRowVector rows = document.get_table_example_data(table);

		/*
		 * deal with the case when the requested number of rows is larger than
		 * the number of rows in the data
		 */
		int displayLength = Math.min(safeLongToInt(rows.size()), length);

		List<String[]> rowsList = new ArrayList<String[]>(displayLength);
		for (int i = start; i < displayLength; i++) {
			RowDataVector row = rows.get(i);

			String[] rowArray = new String[safeLongToInt(layoutItems.size())];
			for (int j = 0; j < layoutItems.size(); j++) {
				LayoutItem layoutItem = layoutItems.get(j);
				String fieldName = layoutItem.get_layout_display_name();
				if (fieldName.contains("::")) {
					// implement this when querying data from a db
					rowArray[j] = "Not Implemented";
				} else {

					/*
					 * need to find the field that the layoutItem is referring
					 * to so we can put the example data in the layout list
					 * order
					 */
					FieldVector fields = document.get_table_fields(table);
					Field field = null;
					int k = 0;
					for (; k < fields.size(); k++) {
						field = fields.get(k);
						if (fieldName.equals(field.get_name()))
							break;
					}
					rowArray[j] = Glom.get_text_for_gda_value(field.get_glom_type(), row.get(k));
				}
			}
			rowsList.add(rowArray);
		}
		return rowsList;

	}

}
