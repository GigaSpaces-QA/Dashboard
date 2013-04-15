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
		this.suiteName = suiteName;
		this.buildNumber = buildNumber;
		this.buildVersion = buildVersion;
		this.milestone = milestone;
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
				+ ((buildNumber == null) ? 0 : buildNumber.hashCode());
		result = prime * result
				+ ((buildVersion == null) ? 0 : buildVersion.hashCode());
		result = prime * result
				+ ((milestone == null) ? 0 : milestone.hashCode());
		result = prime * result
				+ ((suiteName == null) ? 0 : suiteName.hashCode());
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
		if (buildNumber == null) {
			if (other.buildNumber != null)
				return false;
		} else if (!buildNumber.equals(other.buildNumber))
			return false;
		if (buildVersion == null) {
			if (other.buildVersion != null)
				return false;
		} else if (!buildVersion.equals(other.buildVersion))
			return false;
		if (milestone == null) {
			if (other.milestone != null)
				return false;
		} else if (!milestone.equals(other.milestone))
			return false;
		if (suiteName == null) {
			if (other.suiteName != null)
				return false;
		} else if (!suiteName.equals(other.suiteName))
			return false;
		return true;
	}  
	
}
