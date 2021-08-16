package com.gpt.product.gpcash.corporate.logging.activity.valueobject;

import java.sql.Timestamp;

public class CorporateActivityLogVO {
	protected String id;
	protected Timestamp activityDate;
	protected String menuCode;
	protected String menuName;
	protected String actionType;
	protected String actionBy;
	protected boolean error;
	protected String errorCode;
	protected String errorDescription;
	protected String errorTrace;
	protected String referenceNo;
	protected String corporateId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Timestamp getCorporateActivityDate() {
		return activityDate;
	}
	public void setCorporateActivityDate(Timestamp activityDate) {
		this.activityDate = activityDate;
	}
	public String getMenuCode() {
		return menuCode;
	}
	public void setMenuCode(String menuCode) {
		this.menuCode = menuCode;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public String getActionBy() {
		return actionBy;
	}
	public void setActionBy(String actionBy) {
		this.actionBy = actionBy;
	}
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public String getErrorDescription() {
		return errorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
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
	public String getCorporateId() {
		return corporateId;
	}
	public void setCorporateId(String corporateId) {
		this.corporateId = corporateId;
	}
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	
}
