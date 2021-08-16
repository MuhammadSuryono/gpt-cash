package com.gpt.product.gpcash.corporate.transaction.tax.mpn.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Entity
@Cacheable
@Table(name = "TRX_MPN")
public class MPNModel implements Serializable{

	/**
	 * 
	 */
	
	@Id
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
		
	@Column(name = "REF_NO")
	protected String referenceNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD", nullable = false)
	protected IDMMenuModel menu;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SRVC_CD", nullable = false)
	protected ServiceModel service;

	// START Transaction Information
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_ACCT_NO", nullable = false)
	protected AccountModel sourceAccount;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	protected CorporateModel corporate;
	
	@Column(name = "BEN_NM")
	protected String benName;
	
	@Column(name = "NTB")
	protected String ntb;
	
	@Column(name = "NTPN")
	protected String ntpn;
	
	@Column(name = "STAN")
	protected String stan;
	
	@Column(name = "BILLING_ID")
	protected String billingId;
	
	@Column(name = "NPWP_NO")
	protected String npwpNo;
	
	@Column(name = "PAYMENT_DT")
	protected Timestamp paymentDate;

	@Column(name = "TRX_AMT", precision=25, scale=7)
	protected BigDecimal transactionAmount;

	@Column(name = "TRX_CCY_CD")
	protected String transactionCurrency;
	
	@Column(name = "PENDING_TASK_ID")
	protected String pendingTaskId;

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

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
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

	public AccountModel getSourceAccount() {
		return sourceAccount;
	}

	public void setSourceAccount(AccountModel sourceAccount) {
		this.sourceAccount = sourceAccount;
	}

	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
	}

	public String getBenName() {
		return benName;
	}

	public void setBenName(String benName) {
		this.benName = benName;
	}

	public String getNtb() {
		return ntb;
	}

	public void setNtb(String ntb) {
		this.ntb = ntb;
	}

	public String getNtpn() {
		return ntpn;
	}

	public void setNtpn(String ntpn) {
		this.ntpn = ntpn;
	}

	public String getStan() {
		return stan;
	}

	public void setStan(String stan) {
		this.stan = stan;
	}

	public String getBillingId() {
		return billingId;
	}

	public void setBillingId(String billingId) {
		this.billingId = billingId;
	}

	public String getNpwpNo() {
		return npwpNo;
	}

	public void setNpwpNo(String npwpNo) {
		this.npwpNo = npwpNo;
	}

	public Timestamp getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Timestamp paymentDate) {
		this.paymentDate = paymentDate;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public String getTransactionCurrency() {
		return transactionCurrency;
	}

	public void setTransactionCurrency(String transactionCurrency) {
		this.transactionCurrency = transactionCurrency;
	}

	public String getPendingTaskId() {
		return pendingTaskId;
	}

	public void setPendingTaskId(String pendingTaskId) {
		this.pendingTaskId = pendingTaskId;
	}

}
