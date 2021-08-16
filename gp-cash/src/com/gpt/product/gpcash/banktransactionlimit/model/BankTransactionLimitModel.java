package com.gpt.product.gpcash.banktransactionlimit.model;

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
import com.gpt.product.gpcash.service.model.ServiceModel;
import com.gpt.product.gpcash.servicecurrencymatrix.model.ServiceCurrencyMatrixModel;

@Entity
@Cacheable
@Table(name = "PRO_BANK_TRX_LMT")
public class BankTransactionLimitModel implements Serializable {
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
	@JoinColumn(name = "SRVC_CD", nullable = false)
	protected ServiceModel service;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SRVC_CCY_MTRX_ID", nullable = false)
	protected ServiceCurrencyMatrixModel serviceCurrencyMatrix;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_CD", nullable = false)
	protected IDMApplicationModel application;
	
	@Column(name = "MIN_AMT_LMT", precision=25, scale=7)
	protected BigDecimal minAmountLimit;
	
	@Column(name = "MAX_AMT_LMT", precision=25, scale=7)
	protected BigDecimal maxAmountLimit;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

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

	public ServiceModel getService() {
		return service;
	}

	public void setService(ServiceModel service) {
		this.service = service;
	}

	public ServiceCurrencyMatrixModel getServiceCurrencyMatrix() {
		return serviceCurrencyMatrix;
	}

	public void setServiceCurrencyMatrix(ServiceCurrencyMatrixModel serviceCurrencyMatrix) {
		this.serviceCurrencyMatrix = serviceCurrencyMatrix;
	}

	public BigDecimal getMinAmountLimit() {
		return minAmountLimit;
	}

	public void setMinAmountLimit(BigDecimal minAmountLimit) {
		this.minAmountLimit = minAmountLimit;
	}

	public BigDecimal getMaxAmountLimit() {
		return maxAmountLimit;
	}

	public void setMaxAmountLimit(BigDecimal maxAmountLimit) {
		this.maxAmountLimit = maxAmountLimit;
	}

	public IDMApplicationModel getApplication() {
		return application;
	}

	public void setApplication(IDMApplicationModel application) {
		this.application = application;
	}
	
}
