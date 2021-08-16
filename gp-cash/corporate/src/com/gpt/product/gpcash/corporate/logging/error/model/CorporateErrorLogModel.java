package com.gpt.product.gpcash.corporate.logging.error.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CORP_LOG_ERROR")
public class CorporateErrorLogModel implements Serializable{
	@Id
	@Column(name = "ID")
	protected String id;
	
	@Column(name="ACTV_DT")
	protected Timestamp activityDate;
	
	@Column(name="ERROR_TRACE", length = 1024)
	protected String errorTrace;
	
	@Column(name = "REFERENCE_NO")
	protected String referenceNo;
	
	@Column(name = "CORP_ID")
	protected String corporateId;
	
	@Column(name = "LOG_ID")
	protected String logId;

	public String getCorporateId() {
		return corporateId;
	}

	public void setCorporateId(String corporateId) {
		this.corporateId = corporateId;
	}

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

	public String getErrorTrace() {
		return errorTrace;
	}

	public void setErrorTrace(String errorTrace) {
		this.errorTrace = errorTrace;
	}
	
	
}
