package com.gpt.product.gpcash.retail.transactionstatus.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionActivityType;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionStatus;

@Entity
@Table(name = "CUST_TRANSACTION_STS")
public class CustomerTransactionStatusModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Column(name = "PENDING_TASK_ID")
	protected String pendingTaskId;
	
	@Column(name="ACTV_DT")
	protected Timestamp activityDate;
	
	@Column(name = "ACTION_TYPE")
	protected CustomerTransactionActivityType actionType;
	
	@Column(name = "IDM_USER_CD", nullable = false)
	protected String user;
	
	@Column(name = "STATUS")
	protected CustomerTransactionStatus status;
	
	@Column(name = "EAI_REF_NO")
	protected String eaiRefNo;
	
	@Column(name = "IS_ERROR")
	protected boolean error;
	
	@Column(name = "ERROR_CD")
	protected String errorCode;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPendingTaskId() {
		return pendingTaskId;
	}
	
	public void setPendingTaskId(String pendingTaskId) {
		this.pendingTaskId = pendingTaskId;
	}
	
	public Timestamp getActivityDate() {
		return activityDate;
	}

	public void setActivityDate(Timestamp activityDate) {
		this.activityDate = activityDate;
	}

	public CustomerTransactionActivityType getActionType() {
		return actionType;
	}
	
	public void setActionType(CustomerTransactionActivityType actionType) {
		this.actionType = actionType;
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public CustomerTransactionStatus getStatus() {
		return status;
	}
	
	public void setStatus(CustomerTransactionStatus status) {
		this.status = status;
	}
	
	public String getEaiRefNo() {
		return eaiRefNo;
	}

	public void setEaiRefNo(String eaiRefNo) {
		this.eaiRefNo = eaiRefNo;
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
	
}
