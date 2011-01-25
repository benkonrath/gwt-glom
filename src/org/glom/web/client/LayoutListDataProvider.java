package org.glom.web.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class LayoutListDataProvider extends AsyncDataProvider<String[]> {

	@Override
	protected void onRangeChanged(HasData<String[]> display) {
		final Range range = display.getVisibleRange();

		final int start = range.getStart();
		final int length = range.getLength();

		AsyncCallback<List<String[]>> callback = new AsyncCallback<List<String[]>>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: LibGlomService.getTableData()");
			}

			@Override
			public void onSuccess(List<String[]> result) {
				updateRowData(start, result);
			}
		};

		OnlineGlom.getLibGlomServiceProxy().getTableData(start, length, OnlineGlom.getCurrentTableName(), callback);

	}

}
