package com.gpt.product.gpcash.virtualaccounthistory.model;

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

import org.hibernate.annotations.GenericGenerator;

import com.gpt.product.gpcash.account.model.AccountModel;

@Entity
@Table(name = "PRO_ACCT_VA_HISTORY")
public class VirtualAccountHistoryModel implements Serializable {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ACCT_NO", nullable = false)
	protected AccountModel account;
	
	@Column(name="POSTING_DT")
	protected Timestamp postingDate;
	
	@Column(name="EFF_DT")
	protected Timestamp effectiveDate;
	
	@Column(name = "DEBIT_AMT", precision=25, scale=7)
	protected BigDecimal debitAmount;
	
	@Column(name = "CREDIT_AMT", precision=25, scale=7)
	protected BigDecimal creditAmount;
	
	@Column(name = "LEDGER_BALANCE", precision=25, scale=7)
	protected BigDecimal ledgerBalance;
	
	@Column(name = "DSCP")
	protected String description;
	
	@Column(name = "REF_NO")
	protected String referenceNo;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="SEQ_NO")
	protected Integer seqNo;	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Timestamp getPostingDate() {
		return postingDate;
	}

	public void setPostingDate(Timestamp postingDate) {
		this.postingDate = postingDate;
	}

	public Timestamp getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Timestamp effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public BigDecimal getDebitAmount() {
		return debitAmount;
	}

	public void setDebitAmount(BigDecimal debitAmount) {
		this.debitAmount = debitAmount;
	}

	public BigDecimal getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(BigDecimal creditAmount) {
		this.creditAmount = creditAmount;
	}

	public BigDecimal getLedgerBalance() {
		return ledgerBalance;
	}

	public void setLedgerBalance(BigDecimal ledgerBalance) {
		this.ledgerBalance = ledgerBalance;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public AccountModel getAccount() {
		return account;
	}

	public void setAccount(AccountModel account) {
		this.account = account;
	}

	public Integer getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(Integer seqNo) {
		this.seqNo = seqNo;
	}
		
}
