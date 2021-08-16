package com.gpt.component.logging.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "LOG_ERROR")
public class ErrorLogModel implements Serializable{
	@Id
	@Column(name = "ID")
	protected String id;
	
	@Column(name="ACTV_DT")
	protected Timestamp activityDate;
	
	@Column(name="ERROR_TRACE", length = 1024)
	protected String errorTrace;
	
	@Column(name = "REFERENCE_NO")
	protected String referenceNo;
	
	@Column(name = "LOG_ID")
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

	public Timestamp getActivityDate() {
		return activityDate;
	}

	public void setActivityDate(Timestamp activityDate) {
		this.activityDate = activityDate;
	}
	
}
