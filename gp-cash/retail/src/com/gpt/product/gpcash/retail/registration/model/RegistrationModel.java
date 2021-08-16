package com.gpt.product.gpcash.retail.registration.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Cacheable
@Table(name = "CUST_REGISTRATION")
public class RegistrationModel implements Serializable{
	@Id
	@Column(name = "ID")
	protected String id;

	@Column(name="CD")
	protected String code;

	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="CD_GENERATED_DT")
	protected Timestamp codeGeneratedDate;
	
	@Column(name="MOBILE_NO")
	protected String mobileNo;
	
	@Column(name="CARD_NO")
	protected String cardNo;
	
	@Column(name="ACCT_NO")
	protected String accountNo;
	
	@Column(name = "IS_ACTIVE")
	protected String activeFlag;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}

	public Timestamp getCodeGeneratedDate() {
		return codeGeneratedDate;
	}

	public void setCodeGeneratedDate(Timestamp codeGeneratedDate) {
		this.codeGeneratedDate = codeGeneratedDate;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

}
