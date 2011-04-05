package org.glom.web.client.activity;

import java.util.ArrayList;

import org.glom.web.client.ClientFactory;
import org.glom.web.client.OnlineGlomServiceAsync;
import org.glom.web.client.place.OnlineGlomPlace;
import org.glom.web.client.ui.DemoSelectionView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class DemoSelectionActivity extends AbstractActivity {

	// TODO inject with GIN
	private final ClientFactory clientFactory;
	private final ArrayList<OnlineGlomPlace> demoPlaces = new ArrayList<OnlineGlomPlace>();

	public DemoSelectionActivity(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		final DemoSelectionView demoSelectionView = clientFactory.getDemoSelectionView();

		AsyncCallback<ArrayList<String>> callback = new AsyncCallback<ArrayList<String>>() {
			public void onFailure(Throwable caught) {
				// FIXME: need to deal with failure
				System.out.println("AsyncCallback Failed: OnlineGlomService.getDemoDatabaseTitles()");
			}

			public void onSuccess(ArrayList<String> dbTitles) {
				demoSelectionView.clearHyperLinks();
				for (String dbTitle : dbTitles) {
					OnlineGlomPlace place = new OnlineGlomPlace(dbTitle);
					demoPlaces.add(place);
					demoSelectionView.addHyperLink(dbTitle, clientFactory.getHistoryMapper().getToken(place));
				}

			}
		};
		OnlineGlomServiceAsync.Util.getInstance().getDemoDatabaseTitles(callback);

		panel.setWidget(demoSelectionView.asWidget());
	}
}
