package com.gigaspaces.quality.dashboard.client;

import java.util.Map;

import com.gigaspaces.quality.dashboard.shared.CompoundSuiteHistoryResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("dashboardService")
public interface DashboardService extends RemoteService {
	Map<String, CompoundSuiteHistoryResult> submitSuiteResultQuery();
}
