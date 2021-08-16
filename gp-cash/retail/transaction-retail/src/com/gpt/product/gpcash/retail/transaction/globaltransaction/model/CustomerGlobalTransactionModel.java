package com.gpt.product.gpcash.retail.transaction.globaltransaction.model;

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

import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Entity
@Table(name = "CUST_GLOBAL_TRX")
public class CustomerGlobalTransactionModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;

	@Column(name = "REFERENCE_NO")
	protected String referenceNo;
	
	@Column(name = "MODEL_ID")
	protected String modelId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD")
	protected IDMMenuModel menu;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SRVC_CD")
	protected ServiceModel service;
	
	@Column(name = "TRX_CCY_CD")
	protected String transactionCurrency;
	
	@Column(name = "TOTAL_CHARGE_EQ", precision=25, scale=7)
	protected BigDecimal totalChargeEquivalent;
	
	@Column(name = "TOTAL_TRX_AMOUNT_EQ", precision=25, scale=7)
	protected BigDecimal totalTransactionAmountEquivalent;
	
	@Column(name = "TOTAL_DEBITED_AMOUNT_EQ", precision=25, scale=7)
	protected BigDecimal totalDebitedAmountEquivalent;
	
	@Column(name = "IS_ERROR")
	protected String isError;
	
	@Column(name = "CUST_ID")
	protected String customerId;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public String getTransactionCurrency() {
		return transactionCurrency;
	}

	public void setTransactionCurrency(String transactionCurrency) {
		this.transactionCurrency = transactionCurrency;
	}

	public BigDecimal getTotalChargeEquivalent() {
		return totalChargeEquivalent;
	}

	public void setTotalChargeEquivalent(BigDecimal totalChargeEquivalent) {
		this.totalChargeEquivalent = totalChargeEquivalent;
	}

	public BigDecimal getTotalTransactionAmountEquivalent() {
		return totalTransactionAmountEquivalent;
	}

	public void setTotalTransactionAmountEquivalent(BigDecimal totalTransactionAmountEquivalent) {
		this.totalTransactionAmountEquivalent = totalTransactionAmountEquivalent;
	}

	public BigDecimal getTotalDebitedAmountEquivalent() {
		return totalDebitedAmountEquivalent;
	}

	public void setTotalDebitedAmountEquivalent(BigDecimal totalDebitedAmountEquivalent) {
		this.totalDebitedAmountEquivalent = totalDebitedAmountEquivalent;
	}

	public String getIsError() {
		return isError;
	}

	public void setIsError(String isError) {
		this.isError = isError;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public IDMMenuModel getMenu() {
		return menu;
	}

	public void setMenu(IDMMenuModel menu) {
		this.menu = menu;
	}

	public ServiceModel getService() {
		return service;
	}

	public void setService(ServiceModel service) {
		this.service = service;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
}
