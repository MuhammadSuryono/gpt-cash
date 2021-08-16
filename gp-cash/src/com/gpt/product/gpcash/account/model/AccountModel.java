package com.gpt.product.gpcash.account.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.AccountProductTypeModel;
import com.gpt.component.maintenance.parametermt.model.AccountTypeModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;

@Entity
@Table(name = "PRO_ACCT")
public class AccountModel implements Serializable {

	@Id
	@Column(name = "ACCT_NO")
	protected String accountNo;
	
	@Column(name = "CARD_NO")
	protected String cardNo;

	@Version
	protected int version;

	@Column(name = "ACCT_NM")
	protected String accountName;
	
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
	@JoinColumn(name = "BRANCH_CD")
	protected BranchModel branch;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CCY_CD", nullable = false)
	protected CurrencyModel currency;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ACCT_TYP_CD", nullable = false)
	protected AccountTypeModel accountType;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ACCT_PRD_TYP_CD")
	protected AccountProductTypeModel accountProductType;
	
	@Column(name = "HOST_CIF_ID")
	protected String hostCifId;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
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

	public BranchModel getBranch() {
		return branch;
	}

	public void setBranch(BranchModel branch) {
		this.branch = branch;
	}

	public CurrencyModel getCurrency() {
		return currency;
	}

	public void setCurrency(CurrencyModel currency) {
		this.currency = currency;
	}

	public AccountTypeModel getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountTypeModel accountType) {
		this.accountType = accountType;
	}

	public String getHostCifId() {
		return hostCifId;
	}

	public void setHostCifId(String hostCifId) {
		this.hostCifId = hostCifId;
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

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public AccountProductTypeModel getAccountProductType() {
		return accountProductType;
	}

	public void setAccountProductType(AccountProductTypeModel accountProductType) {
		this.accountProductType = accountProductType;
	}
	
}
