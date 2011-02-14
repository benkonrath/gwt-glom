package org.glom.web.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("libGlom")
public interface LibGlomService extends RemoteService {

	GlomDocument getGlomDocument();

	String[] getLayoutListHeaders(String table);

	ArrayList<String[]> getTableData(int start, int length, String table);

}