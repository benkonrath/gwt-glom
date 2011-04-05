package org.glom.web.client;

import java.util.ArrayList;

import org.glom.web.shared.GlomDocument;
import org.glom.web.shared.GlomField;
import org.glom.web.shared.LayoutListTable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OnlineGlomServiceAsync {

	/**
	 * Utility class to get the RPC Async interface from client-side code
	 */
	public static final class Util {
		private static OnlineGlomServiceAsync instance;

		public static final OnlineGlomServiceAsync getInstance() {
			if (instance == null) {
				instance = (OnlineGlomServiceAsync) GWT.create(OnlineGlomService.class);
			}
			return instance;
		}

		private Util() {
			// Utility class should not be instanciated
		}
	}

	void getGlomDocument(AsyncCallback<GlomDocument> callback);

	void getLayoutListTable(String table, AsyncCallback<LayoutListTable> callback);

	void getTableData(String table, int start, int length, AsyncCallback<ArrayList<GlomField[]>> callback);

	void getSortedTableData(String table, int start, int length, int columnIndex, boolean isAscending,
			AsyncCallback<ArrayList<GlomField[]>> callback);

	void getDemoDatabaseTitles(AsyncCallback<ArrayList<String>> callback);
}
