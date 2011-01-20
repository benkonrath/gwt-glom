package org.glom.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LibGlomServiceAsync {

	void getGlomDocument(AsyncCallback<GlomDocument> callback);

	void getLayoutListHeaders(String table, AsyncCallback<String[]> callback);

}
