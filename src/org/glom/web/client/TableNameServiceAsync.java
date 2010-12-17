package org.glom.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TableNameServiceAsync {

	void getNames(AsyncCallback<GlomTable[]> callback);

}
