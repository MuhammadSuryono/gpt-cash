package com.gpt.product.gpcash.retail.transaction.timedeposit.placement.model;

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

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.maintenance.timedepositretail.product.model.CustomerTimeDepositProductModel;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@Entity
@Table(name = "CUST_TRX_TD_PLACEMENT")
public class CustomerTimeDepositPlacementModel implements Serializable {
	// START Basic Information
	@Id
	@Column(name = "ID")
	protected String id;
	
	@Column(name = "REF_NO")
	protected String referenceNo;

	@Version
	protected int version;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD", nullable = false)
	protected IDMMenuModel menu;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_ACCT_NO", nullable = false)
	protected AccountModel sourceAccount;
	
	@Column(name = "PRINCIPAL_CCY")
	protected String principalCurrency;
	
	@Column(name = "PRINCIPAL_AMT", precision=25, scale=7)
	protected BigDecimal principalAmount;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PRODUCT", nullable = false)
	protected CustomerTimeDepositProductModel product;
	
	@Column(name = "MATURITY_INS_TYPE")
	protected String maturityInstruction;

	@Column(name = "IS_PROCESSED")
	protected String isProcessed;
	
	@Column(name = "TERM_PARAM")
	protected int depositTermParam;
	
	@Column(name = "TERM_TYPE")
	protected String depositTermType;

	@Column(name = "PLACEMENT_DT")
	protected Timestamp placementDate;
	
	@Column(name = "WITHDRAW_DT")
	protected Timestamp withdrawalDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUST_ID", nullable = false)
	protected CustomerModel customer;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_CD")
	protected IDMApplicationModel application;
	
	//save maker pendingTaskId for recurring trx
	@Column(name = "PENDING_TASK_ID")
	protected String pendingTaskId;
	
	// START Default Information
	
	@Column(name = "CREATED_BY")
	protected String createdBy;

	@Column(name = "CREATED_DT")
	protected Timestamp createdDate;

	@Column(name = "UPDATED_BY")
	protected String updatedBy;

	@Column(name = "UPDATED_DT")
	protected Timestamp updatedDate;
	
	@Column(name = "STATUS")
	protected String status;
	
	@Column(name = "IS_ERROR")
	protected String isError;
	
	@Column(name = "ERROR_CD")
	protected String errorCode;
	
	@Column(name = "TD_NO")
	protected String timeDepositNo;
	
	@Column(name = "DEPOSITOR_NM")
	protected String depositorName;
	
	@Column(name = "IS_DEPOSITOR_SAME")
	protected String isDepositorSame;
	
	@Column(name = "INTEREST_ACCT_NO")
	protected String interestAccountNo;
	
	@Column(name = "INTEREST_ACCT_NM")
	protected String interestAccountName;
	
	@Column(name = "INTEREST_ACCT_CCY")
	protected String interestAccountCurrency;
	
	@Column(name = "IS_INTEREST_SAME")
	protected String isInterestSame;
	
	@Column(name = "PRINCIPAL_ACCT_NO")
	protected String principalAccountNo;
	
	@Column(name = "PRINCIPAL_ACCT_NM")
	protected String principalAccountName;
	
	@Column(name = "PRINCIPAL_ACCT_CCY")
	protected String principalAccountCurrency;
	
	@Column(name = "IS_PRINCIPAL_SAME")
	protected String isPrincipalSame;

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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public IDMMenuModel getMenu() {
		return menu;
	}

	public void setMenu(IDMMenuModel menu) {
		this.menu = menu;
	}

	public String getIsProcessed() {
		return isProcessed;
	}

	public void setIsProcessed(String isProcessed) {
		this.isProcessed = isProcessed;
	}

	public IDMApplicationModel getApplication() {
		return application;
	}

	public void setApplication(IDMApplicationModel application) {
		this.application = application;
	}

	public String getPendingTaskId() {
		return pendingTaskId;
	}

	public void setPendingTaskId(String pendingTaskId) {
		this.pendingTaskId = pendingTaskId;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public AccountModel getSourceAccount() {
		return sourceAccount;
	}

	public void setSourceAccount(AccountModel sourceAccount) {
		this.sourceAccount = sourceAccount;
	}

	public String getPrincipalCurrency() {
		return principalCurrency;
	}

	public void setPrincipalCurrency(String principalCurrency) {
		this.principalCurrency = principalCurrency;
	}

	public BigDecimal getPrincipalAmount() {
		return principalAmount;
	}

	public void setPrincipalAmount(BigDecimal principalAmount) {
		this.principalAmount = principalAmount;
	}

	public String getMaturityInstruction() {
		return maturityInstruction;
	}

	public void setMaturityInstruction(String maturityInstruction) {
		this.maturityInstruction = maturityInstruction;
	}

	public int getDepositTermParam() {
		return depositTermParam;
	}

	public void setDepositTermParam(int depositTermParam) {
		this.depositTermParam = depositTermParam;
	}

	public String getDepositTermType() {
		return depositTermType;
	}

	public void setDepositTermType(String depositTermType) {
		this.depositTermType = depositTermType;
	}

	public Timestamp getPlacementDate() {
		return placementDate;
	}

	public void setPlacementDate(Timestamp placementDate) {
		this.placementDate = placementDate;
	}

	public CustomerTimeDepositProductModel getProduct() {
		return product;
	}

	public void setProduct(CustomerTimeDepositProductModel product) {
		this.product = product;
	}

	public String getTimeDepositNo() {
		return timeDepositNo;
	}

	public void setTimeDepositNo(String timeDepositNo) {
		this.timeDepositNo = timeDepositNo;
	}
	
	public Timestamp getWithdrawalDate() {
		return withdrawalDate;
	}

	public void setWithdrawalDate(Timestamp withdrawalDate) {
		this.withdrawalDate = withdrawalDate;
	}

	public CustomerModel getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerModel customer) {
		this.customer = customer;
	}

	public String getDepositorName() {
		return depositorName;
	}

	public void setDepositorName(String depositorName) {
		this.depositorName = depositorName;
	}

	public String getIsDepositorSame() {
		return isDepositorSame;
	}

	public void setIsDepositorSame(String isDepositorSame) {
		this.isDepositorSame = isDepositorSame;
	}

	public String getIsInterestSame() {
		return isInterestSame;
	}

	public void setIsInterestSame(String isInterestSame) {
		this.isInterestSame = isInterestSame;
	}

	public String getInterestAccountNo() {
		return interestAccountNo;
	}

	public void setInterestAccountNo(String interestAccountNo) {
		this.interestAccountNo = interestAccountNo;
	}

	public String getInterestAccountName() {
		return interestAccountName;
	}

	public void setInterestAccountName(String interestAccountName) {
		this.interestAccountName = interestAccountName;
	}

	public String getInterestAccountCurrency() {
		return interestAccountCurrency;
	}

	public void setInterestAccountCurrency(String interestAccountCurrency) {
		this.interestAccountCurrency = interestAccountCurrency;
	}

	public String getPrincipalAccountNo() {
		return principalAccountNo;
	}

	public void setPrincipalAccountNo(String principalAccountNo) {
		this.principalAccountNo = principalAccountNo;
	}

	public String getPrincipalAccountName() {
		return principalAccountName;
	}

	public void setPrincipalAccountName(String principalAccountName) {
		this.principalAccountName = principalAccountName;
	}

	public String getPrincipalAccountCurrency() {
		return principalAccountCurrency;
	}

	public void setPrincipalAccountCurrency(String principalAccountCurrency) {
		this.principalAccountCurrency = principalAccountCurrency;
	}

	public String getIsPrincipalSame() {
		return isPrincipalSame;
	}

	public void setIsPrincipalSame(String isPrincipalSame) {
		this.isPrincipalSame = isPrincipalSame;
	}

}
