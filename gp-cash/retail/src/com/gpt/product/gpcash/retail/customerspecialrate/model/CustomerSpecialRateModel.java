package com.gpt.product.gpcash.retail.customerspecialrate.model;

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
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@Entity
@Table(name = "CUST_MT_SPECIAL_RT")
public class CustomerSpecialRateModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6733559344385243657L;

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;

	@Version
	protected int version;

	@Column(name = "IDX")
	protected int idx;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUST_ID")
	protected CustomerModel customer;
	
	@Column(name="RATE_TYPE")
	protected String rateType;
	
	@Column(name="REFNO_SPECIALRATE")
	protected String refNoSpecialRate;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CCY_CD")
	protected CurrencyModel foreignCurrency1;
	
	@Column(name = "CCY_QTY")
	protected int currencyQuantity1;
	
	@Column(name = "TRX_BUY_RT", precision=25, scale=7)
	protected BigDecimal transactionBuyRate;
	
	@Column(name = "TRX_SELL_RT", precision=25, scale=7)
	protected BigDecimal transactionSellRate;
	
	@Column(name = "BUY_AMT_RT", precision=25, scale=7)
	protected BigDecimal buyAmountRate;
	
	@Column(name = "SELL_AMT_RT", precision=25, scale=7)
	protected BigDecimal sellAmountRate;
	
	@Column(name = "TRX_AMT_RT", precision=25, scale=7)
	protected BigDecimal transactionAmountRate;
	
	@Column(name="TYPE")
	protected String type;
	
	@Column(name = "EXPIRY_DT")
	protected Timestamp expiryDate;
	
	@Column(name="REMARK")
	protected String remark;
	
	@Column(name = "STATUS")
	protected String status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CCY_CD2")
	protected CurrencyModel foreignCurrency2;
	
	@Column(name = "CCY_QTY2")
	protected int currencyQuantity2;
	
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
	
	@Column(name = "IS_PENDING_TRX")
	protected String pendingTaskTrxFlag;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIdx() {
		return idx;
	}

	
	public String getRefNoSpecialRate() {
		return refNoSpecialRate;
	}

	public void setRefNoSpecialRate(String refNoSpecialRate) {
		this.refNoSpecialRate = refNoSpecialRate;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public CustomerModel getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerModel customer) {
		this.customer = customer;
	}

	public String getRateType() {
		return rateType;
	}

	public void setRateType(String rateType) {
		this.rateType = rateType;
	}

	public CurrencyModel getForeignCurrency1() {
		return foreignCurrency1;
	}

	public void setForeignCurrency1(CurrencyModel foreignCurrency1) {
		this.foreignCurrency1 = foreignCurrency1;
	}

	public int getCurrencyQuantity1() {
		return currencyQuantity1;
	}

	public void setCurrencyQuantity1(int currencyQuantity1) {
		this.currencyQuantity1 = currencyQuantity1;
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

	public BigDecimal getBuyAmountRate() {
		return buyAmountRate;
	}

	public void setBuyAmountRate(BigDecimal buyAmountRate) {
		this.buyAmountRate = buyAmountRate;
	}

	public BigDecimal getSellAmountRate() {
		return sellAmountRate;
	}

	public void setSellAmountRate(BigDecimal sellAmountRate) {
		this.sellAmountRate = sellAmountRate;
	}

	public BigDecimal getTransactionAmountRate() {
		return transactionAmountRate;
	}

	public void setTransactionAmountRate(BigDecimal transactionAmountRate) {
		this.transactionAmountRate = transactionAmountRate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Timestamp getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Timestamp expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public CurrencyModel getForeignCurrency2() {
		return foreignCurrency2;
	}

	public void setForeignCurrency2(CurrencyModel foreignCurrency2) {
		this.foreignCurrency2 = foreignCurrency2;
	}

	public int getCurrencyQuantity2() {
		return currencyQuantity2;
	}

	public void setCurrencyQuantity2(int currencyQuantity2) {
		this.currencyQuantity2 = currencyQuantity2;
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

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getPendingTaskTrxFlag() {
		return pendingTaskTrxFlag;
	}

	public void setPendingTaskTrxFlag(String pendingTaskTrxFlag) {
		this.pendingTaskTrxFlag = pendingTaskTrxFlag;
	}
	
	
}
