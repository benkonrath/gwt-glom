package org.glom.web.server;

import java.io.File;

import org.glom.libglom.Document;
import org.glom.libglom.Glom;
import org.glom.libglom.StringVector;
import org.glom.web.client.GlomDocument;
import org.glom.web.client.GlomTable;
import org.glom.web.client.LibGlomService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LibGlomServiceImpl extends RemoteServiceServlet implements
		LibGlomService {
	private Document document;

	public LibGlomServiceImpl() {
		Glom.libglom_init();
		// FIXME Need to call Glom.libglom_deinit()
		document = new Document();
		document.set_file_uri("file://" + Glom.GLOM_EXAMPLE_FILE_DIR +  File.separator + "example_music_collection.glom");
		int error = 0;
		@SuppressWarnings("unused")
		boolean retval = document.load(error);
		// FIXME handle error condition
	}

	// FIXME I think Swig is generating long on 64-bit machines and int on 32-bit machines - need to keep this constant
	// From http://stackoverflow.com/questions/1590831/safely-casting-long-to-int-in-java
	public static int safeLongToInt(long l) {
	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
	        throw new IllegalArgumentException
	            (l + " cannot be cast to int without changing its value.");
	    }
	    return (int) l;
	}

	@Override
	public GlomDocument getGlomDocument() {
		GlomDocument glomDocument = new GlomDocument();

		// set visable title
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
				glomDocument.setDefaultTable(i);
			}
		}
		glomDocument.setTableNames(tables);

		return glomDocument;

	}

}
