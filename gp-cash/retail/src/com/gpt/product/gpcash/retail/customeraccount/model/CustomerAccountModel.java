package com.gpt.product.gpcash.retail.customeraccount.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@Entity
@Table(name = "CUST_ACCT")
public class CustomerAccountModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;

	@Column(name = "ACCT_ALIAS")
	protected String accountAlias;
	
	@Column(name = "IS_DEBIT", nullable = false)
	protected String isDebit;

	@Column(name = "IS_CREDIT", nullable = false)
	protected String isCredit;

	@Column(name = "IS_INQ", nullable = false)
	protected String isInquiry;
	
	@Column(name = "INACTIVE_STS", nullable = false)
	protected String inactiveStatus;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUST_ID", nullable = false)
	protected CustomerModel customer;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ACCT_NO", nullable = false)
	protected AccountModel account;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getAccountAlias() {
		return accountAlias;
	}

	public void setAccountAlias(String accountAlias) {
		this.accountAlias = accountAlias;
	}

	public String getIsDebit() {
		return isDebit;
	}

	public void setIsDebit(String isDebit) {
		this.isDebit = isDebit;
	}

	public String getIsCredit() {
		return isCredit;
	}

	public void setIsCredit(String isCredit) {
		this.isCredit = isCredit;
	}

	public String getIsInquiry() {
		return isInquiry;
	}

	public void setIsInquiry(String isInquiry) {
		this.isInquiry = isInquiry;
	}

	public String getInactiveStatus() {
		return inactiveStatus;
	}

	public void setInactiveStatus(String inactiveStatus) {
		this.inactiveStatus = inactiveStatus;
	}

	public CustomerModel getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerModel customer) {
		this.customer = customer;
	}

	public AccountModel getAccount() {
		return account;
	}

	public void setAccount(AccountModel account) {
		this.account = account;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
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
}