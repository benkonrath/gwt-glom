package org.glom.web.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("libGlom")
public interface LibGlomService extends RemoteService {

	GlomDocument getGlomDocument();

	String[] getLayoutListHeaders(String table);

}