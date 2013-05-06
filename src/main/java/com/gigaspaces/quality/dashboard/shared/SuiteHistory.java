package com.gigaspaces.quality.dashboard.shared;

import java.io.Serializable;

public class SuiteHistory implements Serializable, Comparable<SuiteHistory> {

	private static final long serialVersionUID = -4011605427846948687L;
	private String buildNumber;
    private String buildVersion;
    private String milestone;
    private Integer passedTestsHistory;
    private Integer totalTestsHistory;
    private Integer skippedTestsHistory;
    private Integer suspectedTestsHistory;
    private String timestamp;
    private String type;
    
	public SuiteHistory() {}
	
	public SuiteHistory(String buildNumber, String buildVersion, String milestone,
                        Integer passedTestsHistory, Integer totalTestsHistory, Integer skippedTestsHistory, Integer suspectedTestsHistory,
                        String timestamp, String type) {
		this.buildNumber = buildNumber;
		this.buildVersion = buildVersion;
		this.milestone = milestone;
		this.passedTestsHistory = passedTestsHistory;
        this.totalTestsHistory = totalTestsHistory;
        this.skippedTestsHistory = skippedTestsHistory;
        this.suspectedTestsHistory = suspectedTestsHistory;
		this.timestamp = timestamp;
        this.type = type;
	}
	public String getBuildNumber() {
		return buildNumber;
	}
	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}
	public String getBuildVersion() {
		return buildVersion;
	}
	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}
	public String getMilestone() {
		return milestone;
	}
	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}
	public Integer getPassedTestsHistory() {
		return passedTestsHistory;
	}
	public void setPassedTestsHistory(Integer passedTestsHistory) {
		this.passedTestsHistory = passedTestsHistory;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

    public Integer getTotalTestsHistory() {
        return totalTestsHistory;
    }

    public void setTotalTestsHistory(Integer totalTestsHistory) {
        this.totalTestsHistory = totalTestsHistory;
    }

    public Integer getSkippedTestsHistory() {
        return skippedTestsHistory;
    }

    public void setSkippedTestsHistory(Integer skippedTestsHistory) {
        this.skippedTestsHistory = skippedTestsHistory;
    }


    public Integer getSuspectedTestsHistory() {
        return suspectedTestsHistory;
    }

    public void setSuspectedTestsHistory(Integer suspectedTestsHistory) {
        this.suspectedTestsHistory = suspectedTestsHistory;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
	public int compareTo(SuiteHistory suiteHistory) {
		String[] b = this.buildNumber.split("-");
		int high = Integer.valueOf(b[0]);
		int low = Integer.valueOf(b[1]);
		
		String[] b1 = suiteHistory.getBuildNumber().split("-");
		int high1 = Integer.valueOf(b1[0]);
		int low1 = Integer.valueOf(b1[1]);
		
		if(high > high1){
			return 1;
		}
		if(high1 > high){
			return -1;
		}
		if(high == high1){
			if(low > low1){
				return 1;
			}if(low1 > low){
				return -1;
			}
		}
		return 0;
	}

}
