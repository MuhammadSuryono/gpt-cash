package com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.model;

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

@Entity
@Table(name = "TRX_DIRECT_DEBIT_DTL")
public class DirectDebitDetailModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;

	@Version
	private int version;

	@Column(name = "TRX_REFERENCE_NO")
	private String trxReferenceNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DIRECT_DEBIT_ID", nullable = false)
	private DirectDebitModel directDebit;

	// START Transaction Information

	@Column(name = "DEBIT_ACCT_NO")
	private String debitAccountNo;

	@Column(name = "DEBIT_ACCT_NM")
	private String debitAccountName;
	
	@Column(name = "BEN_NOTIF_FLAG")
	private String benNotificationFlag;
	
	@Column(name = "BEN_NOTIF_VALUE")
	private String benNotificationValue;	

	@Column(name = "TRX_CCY_CD")
	private String trxCurrencyCode;

	@Column(name = "TRX_AMT", precision=25, scale=7)
	private BigDecimal trxAmount;

	@Column(name = "REMARK1")
	private String remark1;

	@Column(name = "REMARK2")
	private String remark2;

	@Column(name = "REMARK3")
	private String remark3;

	@Column(name = "SENDER_REF_NO")
	private String senderRefNo;

	@Column(name = "BEN_REF_NO")
	private String benRefNo;

	@Column(name = "IS_HOST_PROCESSED")
	private String isHostProcessed;
	
	@Column(name = "IS_ERROR")
	private String isError;
	
	@Column(name = "ERROR_CD")
	private String errorCode;
	
	@Column(name = "FINALIZE_FLAG")
	private String finalizeFlag;	

	// START Default Information
	@Column(name = "CREATED_BY")
	private String createdBy;	

	@Column(name = "CREATED_DT")
	private Timestamp createdDate;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Column(name = "UPDATED_DT")
	private Timestamp updatedDate;
	
	@Column(name = "IDX")
	private int idx;	
	
	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

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

	public String getTrxReferenceNo() {
		return trxReferenceNo;
	}

	public void setTrxReferenceNo(String trxReferenceNo) {
		this.trxReferenceNo = trxReferenceNo;
	}

	public DirectDebitModel getDirectDebit() {
		return directDebit;
	}

	public void setDirectDebit(DirectDebitModel directDebit) {
		this.directDebit = directDebit;
	}

	public String getDebitAccountNo() {
		return debitAccountNo;
	}

	public void setDebitAccountNo(String debitAccountNo) {
		this.debitAccountNo = debitAccountNo;
	}

	public String getDebitAccountName() {
		return debitAccountName;
	}

	public void setDebitAccountName(String debitAccountName) {
		this.debitAccountName = debitAccountName;
	}

	public String getBenNotificationFlag() {
		return benNotificationFlag;
	}

	public void setBenNotificationFlag(String benNotificationFlag) {
		this.benNotificationFlag = benNotificationFlag;
	}

	public String getBenNotificationValue() {
		return benNotificationValue;
	}

	public void setBenNotificationValue(String benNotificationValue) {
		this.benNotificationValue = benNotificationValue;
	}

	public String getTrxCurrencyCode() {
		return trxCurrencyCode;
	}

	public void setTrxCurrencyCode(String trxCurrencyCode) {
		this.trxCurrencyCode = trxCurrencyCode;
	}

	public BigDecimal getTrxAmount() {
		return trxAmount;
	}

	public void setTrxAmount(BigDecimal trxAmount) {
		this.trxAmount = trxAmount;
	}

	public String getRemark1() {
		return remark1;
	}

	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}

	public String getRemark2() {
		return remark2;
	}

	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}

	public String getRemark3() {
		return remark3;
	}

	public void setRemark3(String remark3) {
		this.remark3 = remark3;
	}

	public String getSenderRefNo() {
		return senderRefNo;
	}

	public void setSenderRefNo(String senderRefNo) {
		this.senderRefNo = senderRefNo;
	}

	public String getBenRefNo() {
		return benRefNo;
	}

	public void setBenRefNo(String benRefNo) {
		this.benRefNo = benRefNo;
	}

	public String getIsHostProcessed() {
		return isHostProcessed;
	}

	public void setIsHostProcessed(String isHostProcessed) {
		this.isHostProcessed = isHostProcessed;
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

	public String getFinalizeFlag() {
		return finalizeFlag;
	}

	public void setFinalizeFlag(String finalizeFlag) {
		this.finalizeFlag = finalizeFlag;
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
