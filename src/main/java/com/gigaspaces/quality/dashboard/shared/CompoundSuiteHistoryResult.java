package com.gigaspaces.quality.dashboard.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CompoundSuiteHistoryResult implements Serializable{
	
	private static final long serialVersionUID = 7094537451696479373L;
	
	private List<SuiteResult> results;
	private Map<String,  List<SuiteHistory>> suiteHistory;
	
	public List<SuiteResult> getResults() {
		return results;
	}
	public void setResults(List<SuiteResult> results) {
		this.results = results;
	}
	public Map<String, List<SuiteHistory>> getSuiteHistory() {
		return suiteHistory;
	}
	public void setSuiteHistory(Map<String, List<SuiteHistory>> suiteHistory) {
		this.suiteHistory = suiteHistory;
	}
}
