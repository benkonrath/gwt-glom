package org.glom.web.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("tableNames")
public interface TableNameService extends RemoteService {
	
  GlomTable[] getNames();

}