package com.gigaspaces.quality.dashboard.shared;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity(name = "SgtestResult")
public class SuiteResult implements Serializable {

	private static final long serialVersionUID = 8495624153937009649L;
	
	private CompoundKey compoundKey;
	private String timestamp;
	private long suiteDuration;
	private String fullBuildLog;
	private int totalTestsRun;
	private int failedTests;
	private int passedTests;
	private int skippedTests;
	private int suspectedTests;
	private int orphans;
	private String suiteReportLink;
	private String jvmType;

	public SuiteResult() {
		
	}

	@EmbeddedId
	public CompoundKey getCompoundKey() {
		return compoundKey;
	}

	public void setCompoundKey(CompoundKey compoundKey) {
		this.compoundKey = compoundKey;
	}

	public long getSuiteDuration() {
		return suiteDuration;
	}

	public void setSuiteDuration(long suiteDuration) {
		this.suiteDuration = suiteDuration;
	}

	public String getFullBuildLog() {
		return fullBuildLog;
	}

	public void setFullBuildLog(String fullBuildLog) {
		this.fullBuildLog = fullBuildLog;
	}

	public int getTotalTestsRun() {
		return totalTestsRun;
	}

	public void setTotalTestsRun(int totalTestsRun) {
		this.totalTestsRun = totalTestsRun;
	}

	public int getFailedTests() {
		return failedTests;
	}

	public void setFailedTests(int failedTests) {
		this.failedTests = failedTests;
	}

	public int getPassedTests() {
		return passedTests;
	}

	public void setPassedTests(int passedTests) {
		this.passedTests = passedTests;
	}

	public int getSkippedTests() {
		return skippedTests;
	}

	public void setSkippedTests(int skippedTests) {
		this.skippedTests = skippedTests;
	}

	public int getSuspectedTests() {
		return suspectedTests;
	}

	public void setSuspectedTests(int suspectedTests) {
		this.suspectedTests = suspectedTests;
	}

	public String getSuiteReportLink() {
		return suiteReportLink;
	}

	public void setSuiteReportLink(String suiteReport) {
		this.suiteReportLink = suiteReport;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int getOrphans() {
		return orphans;
	}

	public void setOrphans(int orphans) {
		this.orphans = orphans;
	}

	public String getJvmType() {
		return jvmType;
	}

	public void setJvmType(String jvmType) {
		this.jvmType = jvmType;
	}	
}