package org.glom.web.server;

import java.io.File;

import org.glom.libglom.Document;
import org.glom.libglom.Glom;
import org.glom.libglom.StringsVector;
import org.glom.web.client.GlomTable;
import org.glom.web.client.TableNameService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class TableNamesServiceImpl extends RemoteServiceServlet implements
		TableNameService {
	private Document document;
	
	public TableNamesServiceImpl() {
		Glom.libglom_init();
		//FIXME Need to call Glom.libglom_deinit()
		document = new Document();
		document.set_file_uri("file://" + Glom.GLOM_EXAMPLE_FILE_DIR +  File.separator + "example_music_collection.glom");
		int error = 0;
		boolean retval = document.load(error);
		// FIXME handle error condition
	}

	@Override
	public GlomTable[] getNames() {
		StringsVector names = document.get_table_names();
		int tableSize = safeLongToInt(names.size());
		GlomTable[] tableNames = new GlomTable[tableSize];
		for (int i = 0; i < tableSize; i++) {
			tableNames[i] = new GlomTable(names.get(i));
		}
		return tableNames;
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


}
