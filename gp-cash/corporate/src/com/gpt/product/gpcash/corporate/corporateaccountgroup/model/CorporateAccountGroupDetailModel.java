package com.gpt.product.gpcash.corporate.corporateaccountgroup.model;

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

import com.gpt.product.gpcash.corporate.corporateaccount.model.CorporateAccountModel;

@Entity
@Table(name = "CORP_ACCT_GRP_DTL")
public class CorporateAccountGroupDetailModel  implements Serializable {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@Column(name = "IS_DEBIT", nullable = false)
	protected String isDebit;

	@Column(name = "IS_CREDIT", nullable = false)
	protected String isCredit;

	@Column(name = "IS_INQ", nullable = false)
	protected String isInquiry;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ACCT_NO", nullable = false)
	protected CorporateAccountModel corporateAccount;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ACCT_GRP_ID", nullable = false)
	protected CorporateAccountGroupModel corporateAccountGroup;
	
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

	public CorporateAccountModel getCorporateAccount() {
		return corporateAccount;
	}

	public void setCorporateAccount(CorporateAccountModel corporateAccount) {
		this.corporateAccount = corporateAccount;
	}

	public CorporateAccountGroupModel getCorporateAccountGroup() {
		return corporateAccountGroup;
	}

	public void setCorporateAccountGroup(CorporateAccountGroupModel corporateAccountGroup) {
		this.corporateAccountGroup = corporateAccountGroup;
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
