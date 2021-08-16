package com.gpt.component.maintenance.exchangerate.model;

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

import com.gpt.component.maintenance.parametermt.model.CurrencyModel;

@Entity
@Table(name = "MT_EXCHANGE_RT")
public class ExchangeRateModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;

	@Version
	protected int version;

	@Column(name = "IDX")
	protected int idx;
	
	@Column(name = "CCY_QTY")
	protected int currencyQuantity;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CCY_CD")
	protected CurrencyModel currency;
	
	@Column(name = "EFF_DT")
	protected Timestamp effectiveDate;
	
	@Column(name = "TRX_MID_RT", precision=25, scale=7)
	protected BigDecimal transactionMidRate;
	
	@Column(name = "TRX_BUY_RT", precision=25, scale=7)
	protected BigDecimal transactionBuyRate;
	
	@Column(name = "TRX_SELL_RT", precision=25, scale=7)
	protected BigDecimal transactionSellRate;
	
	@Column(name = "BNK_NOTE_BUY_RT", precision=25, scale=7)
	protected BigDecimal bankNoteBuyRate;
	
	@Column(name = "BNK_NOTE_SELL_RT", precision=25, scale=7)
	protected BigDecimal bankNoteSellRate;
	
	@Column(name = "TLLR_BUY_RT", precision=25, scale=7)
	protected BigDecimal tellerBuyRate;
	
	@Column(name = "TLLR_SELL_RT", precision=25, scale=7)
	protected BigDecimal tellerSellRate;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;
	
	@Column(name = "IS_DELETE")
	protected String deleteFlag;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public int getCurrencyQuantity() {
		return currencyQuantity;
	}

	public void setCurrencyQuantity(int currencyQuantity) {
		this.currencyQuantity = currencyQuantity;
	}

	public CurrencyModel getCurrency() {
		return currency;
	}

	public void setCurrency(CurrencyModel currency) {
		this.currency = currency;
	}

	public Timestamp getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Timestamp effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public BigDecimal getTransactionBuyRate() {
		return transactionBuyRate;
	}

	public void setTransactionBuyRate(BigDecimal transactionBuyRate) {
		this.transactionBuyRate = transactionBuyRate;
	}

	public BigDecimal getTransactionSellRate() {
		return transactionSellRate;
	}

	public void setTransactionSellRate(BigDecimal transactionSellRate) {
		this.transactionSellRate = transactionSellRate;
	}

	public BigDecimal getTransactionMidRate() {
		return transactionMidRate;
	}

	public void setTransactionMidRate(BigDecimal transactionMidRate) {
		this.transactionMidRate = transactionMidRate;
	}

	public BigDecimal getBankNoteBuyRate() {
		return bankNoteBuyRate;
	}

	public void setBankNoteBuyRate(BigDecimal bankNoteBuyRate) {
		this.bankNoteBuyRate = bankNoteBuyRate;
	}

	public BigDecimal getBankNoteSellRate() {
		return bankNoteSellRate;
	}

	public void setBankNoteSellRate(BigDecimal bankNoteSellRate) {
		this.bankNoteSellRate = bankNoteSellRate;
	}

	public BigDecimal getTellerBuyRate() {
		return tellerBuyRate;
	}

	public void setTellerBuyRate(BigDecimal tellerBuyRate) {
		this.tellerBuyRate = tellerBuyRate;
	}

	public BigDecimal getTellerSellRate() {
		return tellerSellRate;
	}

	public void setTellerSellRate(BigDecimal tellerSellRate) {
		this.tellerSellRate = tellerSellRate;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
