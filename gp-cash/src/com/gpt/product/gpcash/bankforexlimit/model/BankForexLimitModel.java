package com.gpt.product.gpcash.bankforexlimit.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
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

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;

@Entity
@Cacheable
@Table(name = "PRO_BANK_FOREX_LMT")
public class BankForexLimitModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CCY_CD", nullable = false)
	protected CurrencyModel currency;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_CD", nullable = false)
	protected IDMApplicationModel application;
	
	@Column(name = "MIN_BUY_LMT", precision=25, scale=7)
	protected BigDecimal minBuyLimit;
	
	@Column(name = "MAX_BUY_LMT", precision=25, scale=7)
	protected BigDecimal maxBuyLimit;
	
	@Column(name = "MIN_SELL_LMT", precision=25, scale=7)
	protected BigDecimal minSellLimit;
	
	@Column(name = "MAX_SELL_LMT", precision=25, scale=7)
	protected BigDecimal maxSellLimit;
	
	@Column(name = "LMT_USAGE", precision=25, scale=7)
	protected BigDecimal limitUsage;
	
	@Column(name = "LMT_USAGE_EQUIVALENT", precision=25, scale=7)
	protected BigDecimal limitUsageEquivalent;
	
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

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
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

	public CurrencyModel getCurrency() {
		return currency;
	}

	public void setCurrency(CurrencyModel currency) {
		this.currency = currency;
	}

	public BigDecimal getMinBuyLimit() {
		return minBuyLimit;
	}

	public void setMinBuyLimit(BigDecimal minBuyLimit) {
		this.minBuyLimit = minBuyLimit;
	}

	public BigDecimal getMaxBuyLimit() {
		return maxBuyLimit;
	}

	public void setMaxBuyLimit(BigDecimal maxBuyLimit) {
		this.maxBuyLimit = maxBuyLimit;
	}

	public BigDecimal getMinSellLimit() {
		return minSellLimit;
	}

	public void setMinSellLimit(BigDecimal minSellLimit) {
		this.minSellLimit = minSellLimit;
	}

	public BigDecimal getMaxSellLimit() {
		return maxSellLimit;
	}

	public void setMaxSellLimit(BigDecimal maxSellLimit) {
		this.maxSellLimit = maxSellLimit;
	}

	public IDMApplicationModel getApplication() {
		return application;
	}

	public void setApplication(IDMApplicationModel application) {
		this.application = application;
	}

	public BigDecimal getLimitUsage() {
		return limitUsage;
	}

	public void setLimitUsage(BigDecimal limitUsage) {
		this.limitUsage = limitUsage;
	}

	public BigDecimal getLimitUsageEquivalent() {
		return limitUsageEquivalent;
	}

	public void setlimitUsageEquivalent(BigDecimal limitUsageEquivalent) {
		this.limitUsageEquivalent = limitUsageEquivalent;
	}
	
}
