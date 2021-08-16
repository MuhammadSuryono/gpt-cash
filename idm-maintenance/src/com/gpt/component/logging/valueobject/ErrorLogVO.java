package com.gpt.component.logging.valueobject;

import java.sql.Timestamp;

public class ErrorLogVO {
	protected String id;
	protected Timestamp activityDate;
	protected String errorTrace;
	protected String referenceNo;
	protected String logId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Timestamp getActivityDate() {
		return activityDate;
	}
	public void setActivityDate(Timestamp activityDate) {
		this.activityDate = activityDate;
	}
	public String getErrorTrace() {
		return errorTrace;
	}
	public void setErrorTrace(String errorTrace) {
		this.errorTrace = errorTrace;
	}
	public String getReferenceNo() {
		return referenceNo;
	}
	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}
	public String getLogId() {
		return logId;
	}
	public void setLogId(String logId) {
		this.logId = logId;
	}
	
}
