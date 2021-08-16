package com.gpt.product.gpcash.retail.logging.error.valueobject;

import java.sql.Timestamp;

public class CustomerErrorLogVO {
	protected String id;
	protected Timestamp activityDate;
	protected String errorTrace;
	protected String referenceNo;
	protected String customerId;
	protected String logId;
	
	public String getLogId() {
		return logId;
	}
	public void setLogId(String logId) {
		this.logId = logId;
	}
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
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
	
}
