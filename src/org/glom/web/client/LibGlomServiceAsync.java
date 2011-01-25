package org.glom.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LibGlomServiceAsync {

	void getGlomDocument(AsyncCallback<GlomDocument> callback);

	void getLayoutListHeaders(String table, AsyncCallback<String[]> callback);

	void getTableData(int start, int length, String table, AsyncCallback<List<String[]>> callback);

}
