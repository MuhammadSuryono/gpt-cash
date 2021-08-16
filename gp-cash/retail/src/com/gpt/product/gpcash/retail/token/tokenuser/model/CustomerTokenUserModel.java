package com.gpt.product.gpcash.retail.token.tokenuser.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.product.gpcash.retail.token.tokentype.model.CustomerTokenTypeModel;

@Entity
@Cacheable
@Table(name = "CUST_TOKEN_USER", indexes = {
	@Index(name="CUST_TOKEN_USER_SORT_INDEX", unique = false, columnList = "IDM_USER_CD")	
})
public class CustomerTokenUserModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Column(name="TOKEN_NO")
	protected String tokenNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TOKEN_TYP_CD", nullable = false)
	protected CustomerTokenTypeModel tokenType;
	
	@Version
	protected int version;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDM_USER_CD")
	protected IDMUserModel assignedUser;
	
	@Column(name="STATUS")
	protected String status;
	
	@Column(name = "RETRY")
	protected int retry;
	
	@Column(name = "IS_RESET")
	public String resetFlag;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "REGISTERED_BY")
	protected IDMUserModel registeredBy;
	
	@Column(name="REGISTERED_DT")
	protected Timestamp registeredDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ASSIGNED_BY")
	protected IDMUserModel assignedBy;
	
	@Column(name="ASSIGNED_DT")
	protected Timestamp assignedDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IDMUserModel getRegisteredBy() {
		return registeredBy;
	}

	public void setRegisteredBy(IDMUserModel registeredBy) {
		this.registeredBy = registeredBy;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public IDMUserModel getAssignedUser() {
		return assignedUser;
	}

	public void setAssignedUser(IDMUserModel assignedUser) {
		this.assignedUser = assignedUser;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public String getResetFlag() {
		return resetFlag;
	}

	public void setResetFlag(String resetFlag) {
		this.resetFlag = resetFlag;
	}


	public Timestamp getRegisteredDate() {
		return registeredDate;
	}
	
	public void setRegisteredDate(Timestamp registeredDate) {
		this.registeredDate = registeredDate;
	}

	public Timestamp getAssignedDate() {
		return assignedDate;
	}

	public void setAssignedDate(Timestamp assignedDate) {
		this.assignedDate = assignedDate;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Timestamp getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Timestamp updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getTokenNo() {
		return tokenNo;
	}

	public void setTokenNo(String tokenNo) {
		this.tokenNo = tokenNo;
	}

	public IDMUserModel getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedBy(IDMUserModel assignedBy) {
		this.assignedBy = assignedBy;
	}

	public CustomerTokenTypeModel getTokenType() {
		return tokenType;
	}

	public void setTokenType(CustomerTokenTypeModel tokenType) {
		this.tokenType = tokenType;
	}

}
