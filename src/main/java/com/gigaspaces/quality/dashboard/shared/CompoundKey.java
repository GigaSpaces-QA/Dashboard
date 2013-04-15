package com.gigaspaces.quality.dashboard.shared;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class CompoundKey implements Serializable{

	private static final long serialVersionUID = 3533278748020057087L;
	
	private String suiteName;
	private String buildNumber;
    private String buildVersion;
    private String milestone;

    public CompoundKey() {
    }
    
	public CompoundKey(String suiteName, String buildNumber, String buildVersion, String milestone) {
		this.setSuiteName(suiteName);
		this.setBuildNumber(buildNumber);
		this.setBuildVersion(buildVersion);
		this.setMilestone(milestone);
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getBuildNumber() == null) ? 0 : getBuildNumber().hashCode());
		result = prime * result
				+ ((getBuildVersion() == null) ? 0 : getBuildVersion().hashCode());
		result = prime * result
				+ ((getMilestone() == null) ? 0 : getMilestone().hashCode());
		result = prime * result
				+ ((getSuiteName() == null) ? 0 : getSuiteName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompoundKey other = (CompoundKey) obj;
		if (getBuildNumber() == null) {
			if (other.getBuildNumber() != null)
				return false;
		} else if (!getBuildNumber().equals(other.getBuildNumber()))
			return false;
		if (getBuildVersion() == null) {
			if (other.getBuildVersion() != null)
				return false;
		} else if (!getBuildVersion().equals(other.getBuildVersion()))
			return false;
		if (getMilestone() == null) {
			if (other.getMilestone() != null)
				return false;
		} else if (!getMilestone().equals(other.getMilestone()))
			return false;
		if (getSuiteName() == null) {
			if (other.getSuiteName() != null)
				return false;
		} else if (!getSuiteName().equals(other.getSuiteName()))
			return false;
		return true;
	}  
	
}
