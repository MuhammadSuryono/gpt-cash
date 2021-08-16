package com.gpt.product.gpcash.corporate.transaction.cheque.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "TRX_CHQ_ORDER_DTL")
public class ChequeOrderDetailModel implements Serializable {
	@Id
	@Column(name = "CHQ_NO")
	private String chequeNo;

	@Version
	private int version;
	
	@Column(name = "IDX")
	protected int idx;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CHQ_ORDER_ID", nullable = false)
	private ChequeOrderModel chequeOrder;
	
	@Column(name = "CHQ_AMT", precision=25, scale=7)
	private BigDecimal chequeAmount;
	
	@Column(name = "CHQ_CCY")
	private String chequeCurrency;
	
	@Column(name="WITHDRAW_DT")
	protected Timestamp withdrawDate;

	public String getChequeNo() {
		return chequeNo;
	}

	public void setChequeNo(String chequeNo) {
		this.chequeNo = chequeNo;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public ChequeOrderModel getChequeOrder() {
		return chequeOrder;
	}

	public void setChequeOrder(ChequeOrderModel chequeOrder) {
		this.chequeOrder = chequeOrder;
	}

	public BigDecimal getChequeAmount() {
		return chequeAmount;
	}

	public void setChequeAmount(BigDecimal chequeAmount) {
		this.chequeAmount = chequeAmount;
	}

	public String getChequeCurrency() {
		return chequeCurrency;
	}

	public void setChequeCurrency(String chequeCurrency) {
		this.chequeCurrency = chequeCurrency;
	}

	public Timestamp getWithdrawDate() {
		return withdrawDate;
	}

	public void setWithdrawDate(Timestamp withdrawDate) {
		this.withdrawDate = withdrawDate;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}
	
}
