package com.gpt.product.gpcash.retail.logging.activity.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "CUST_LOG_ACTV", indexes = {
	@Index(name="CUST_LOG_ACTV_SORT_INDEX", unique = true, columnList = "ACTION_BY, ACTV_DT")	
})
public class CustomerActivityLogModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Column(name="ACTV_DT")
	protected Timestamp activityDate;
	
	@Column(name = "REFERENCE_NO")
	protected String referenceNo;
	
	@Column(name = "MENU_CD")
	protected String menuCode;
	
	@Column(name = "MENU_NM")
	protected String menuName;

	@Column(name = "ACTION_TYPE")
	protected String actionType;
	
	@Column(name = "ACTION_BY")
	protected String actionBy;
	
	@Column(name = "IS_ERROR")
	protected String isError;
	
	@Column(name = "ERROR_CD")
	protected String errorCode;
	
	@Column(name = "ERROR_DSCP")
	protected String errorDescription;
	
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

	public String getIsError() {
		return isError;
	}

	public void setIsError(String isError) {
		this.isError = isError;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	
	
	
}
