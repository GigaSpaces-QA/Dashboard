package com.gigaspaces.quality.dashboard.client;

import java.util.Map;

import com.gigaspaces.quality.dashboard.shared.CompoundSuiteHistoryResult;
import com.google.gwt.user.client.rpc.AsyncCallback;


public interface DashboardServiceAsync {
	void submitSuiteResultQuery(AsyncCallback<Map<String, CompoundSuiteHistoryResult>> asyncCallback);
}
