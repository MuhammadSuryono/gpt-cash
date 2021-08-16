package com.gpt.product.gpcash.corporate.corporateaccount.model;

import java.io.Serializable;
import java.math.BigDecimal;
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

import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.account.model.AccountModel;

@Entity
@Table(name = "CORP_ACCT")
public class CorporateAccountModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;

	@Column(name = "ACCT_ALIAS")
	protected String accountAlias;
	
	@Column(name = "DEBIT_LMT", precision=25, scale=7)
	protected BigDecimal debitLimit;
	
	@Column(name = "IS_DEBIT", nullable = false)
	protected String isDebit;

	@Column(name = "IS_CREDIT", nullable = false)
	protected String isCredit;

	@Column(name = "IS_INQ", nullable = false)
	protected String isInquiry;
	
	@Column(name = "IS_PARENT_DEBIT")
	protected String isParentDebit;

	@Column(name = "IS_PARENT_CREDIT")
	protected String isParentCredit;

	@Column(name = "IS_PARENT_INQ")
	protected String isParentInquiry;
	
	@Column(name = "INACTIVE_STS", nullable = false)
	protected String inactiveStatus;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	protected CorporateModel corporate;
	
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

	public BigDecimal getDebitLimit() {
		return debitLimit;
	}

	public void setDebitLimit(BigDecimal debitLimit) {
		this.debitLimit = debitLimit;
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

	public String getIsParentDebit() {
		return isParentDebit;
	}

	public void setIsParentDebit(String isParentDebit) {
		this.isParentDebit = isParentDebit;
	}

	public String getIsParentCredit() {
		return isParentCredit;
	}

	public void setIsParentCredit(String isParentCredit) {
		this.isParentCredit = isParentCredit;
	}

	public String getIsParentInquiry() {
		return isParentInquiry;
	}

	public void setIsParentInquiry(String isParentInquiry) {
		this.isParentInquiry = isParentInquiry;
	}

	public String getInactiveStatus() {
		return inactiveStatus;
	}

	public void setInactiveStatus(String inactiveStatus) {
		this.inactiveStatus = inactiveStatus;
	}

	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
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