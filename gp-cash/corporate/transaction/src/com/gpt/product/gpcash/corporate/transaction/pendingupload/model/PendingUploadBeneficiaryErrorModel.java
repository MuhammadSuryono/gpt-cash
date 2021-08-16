package com.gpt.product.gpcash.corporate.transaction.pendingupload.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "TRX_PENDING_UPLOAD_ERROR_BEN")
public class PendingUploadBeneficiaryErrorModel implements Serializable {
	
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PENDING_UPLOAD_ID", nullable = false)
	private PendingUploadBeneficiaryModel parent;
	
	@Column(name = "LINE")
	private int line;
	
	@Column(name = "LINE_TYPE")
	private String lineType;

	@Column(name = "ACCT_NO")
	private String accountNo;

	@Column(name = "TRX_CCY_CD")
	private String trxCurrencyCode;	
		
	@Column(name = "TRX_AMT", precision=25, scale=7)
	private BigDecimal trxAmount;
	
	@Column(name = "ERROR_CD")
	private String errorCode;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PendingUploadBeneficiaryModel getParent() {
		return parent;
	}

	public void setParent(PendingUploadBeneficiaryModel parent) {
		this.parent = parent;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getLineType() {
		return lineType;
	}

	public void setLineType(String lineType) {
		this.lineType = lineType;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
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

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
}
