package com.gpt.product.gpcash.corporate.transaction.liquidty.sweepout.model;

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

import com.gpt.product.gpcash.account.model.AccountModel;

@Entity
@Table(name = "TRX_LIQ_SWEEP_OUT_DTL")
public class SweepOutDetailModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;

	@Version
	private int version;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SWEEP_OUT_ID", nullable = false)
	private SweepOutModel sweepOut;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SUB_ACCT_NO", nullable = false)
	protected AccountModel subAccount;
	
	@Column(name = "REMAINING_AMT", precision=25, scale=7)
	private BigDecimal remainingAmount;
	
	@Column(name = "SWEEP_AMT", precision=25, scale=7)
	private BigDecimal sweepAmount;
	
	@Column(name = "DSCP")
	private String description;
	
	@Column(name = "PRIORITY")
	private int priority;
	
	@Column(name = "IS_ERROR")
	private String isError;
	
	@Column(name = "ERROR_CD")
	private String errorCode;
	
	@Column(name = "STATUS")
	protected String status;
	
	@Column(name = "CREATED_DT")
	protected Timestamp createdDate;
	
	
	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
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

	public SweepOutModel getSweepOut() {
		return sweepOut;
	}

	public void setSweepOut(SweepOutModel sweepOut) {
		this.sweepOut = sweepOut;
	}

	public AccountModel getSubAccount() {
		return subAccount;
	}

	public void setSubAccount(AccountModel subAccount) {
		this.subAccount = subAccount;
	}

	public BigDecimal getRemainingAmount() {
		return remainingAmount;
	}

	public void setRemainingAmount(BigDecimal remainingAmount) {
		this.remainingAmount = remainingAmount;
	}

	public BigDecimal getSweepAmount() {
		return sweepAmount;
	}

	public void setSweepAmount(BigDecimal sweepAmount) {
		this.sweepAmount = sweepAmount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
	
}
