package com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.maintenance.parametermt.model.IdentityTypeModel;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.transaction.tax.deposittype.model.DepositTypeModel;
import com.gpt.product.gpcash.corporate.transaction.tax.taxtype.model.TaxTypeModel;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Entity
@Table(name = "TRX_TAX_PAYMENT", indexes = {
		@Index(name="TRX_TAX_PAYMENT_INDEX1", unique = false, columnList = "NTP")	
})
public class TaxPaymentModel implements Serializable {

	// START Basic Information
	@Id
	@Column(name = "ID")
	protected String id;
	
	@Column(name = "REF_NO")
	protected String referenceNo;

	@Version
	protected int version;

	@Column(name = "PARENT_REFERENCE_NO")
	protected String parentReferenceNo;

	@Column(name = "TRX_REFERENCE_NO")
	protected String trxReferenceNo;

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
	
	@Column(name = "IS_NPWP")
	protected String isNpwp;
	
	@Column(name = "NPWP_NO")
	protected String npwpNo;
	
	@Column(name = "DEPOSITOR_NPWP_NO")
	protected String depositorNpwpNo;
	
	@Column(name = "BILL_EXPIRY_DT")
	protected Timestamp billExpiryDate;
	
	@Column(name = "KPP_NO")
	protected String kppNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDENTITY_TYP_CD")
	protected IdentityTypeModel identityType;
	
	@Column(name = "IDENTITY_NO")
	protected String identityNo;
	
	@Column(name = "BEN_NM")
	protected String benName;
	
	@Column(name = "BEN_ADDRESS_1")
	protected String benAddress1;
	
	@Column(name = "BEN_ADDRESS_2")
	protected String benAddress2;
	
	@Column(name = "BEN_ADDRESS_3")
	protected String benAddress3;
	
	@Column(name = "CITY_NM")
	protected String cityName;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TAX_TYP_CD", nullable = false)
	protected TaxTypeModel taxType;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEPOSIT_TYP_CD", nullable = false)
	protected DepositTypeModel depositType;
	
	@Column(name = "NOP_NO")
	protected String nopNo;
	
	@Column(name = "SK_NO")
	protected String skNo;
	
	@Column(name = "PERIOD_MONTH_FROM")
	protected String periodMonthFrom;
	
	@Column(name = "PERIOD_MONTH_TO")
	protected String periodMonthTo;
	
	@Column(name = "PERIOD_YEAR")
	protected String periodYear;
	
	@Column(name = "NTB")
	protected String ntb;
	
	@Column(name = "NTP")
	protected String ntp;
	
	@Column(name = "STAN")
	protected String stan;
	
	@Column(name = "BILLING_ID")
	protected String billingId;
	
	@Column(name = "PAYMENT_DT")
	protected Timestamp paymentDate;

	@Column(name = "TRX_AMT", precision=25, scale=7)
	protected BigDecimal transactionAmount;

	@Column(name = "TRX_CCY_CD")
	protected String transactionCurrency;

	@Column(name = "REMARK1")
	protected String remark1;

	@Column(name = "REMARK2")
	protected String remark2;

	@Column(name = "REMARK3")
	protected String remark3;
	
	@Column(name = "IS_FINAL_PAYMENT")
	protected String isFinalPayment;
	
	@Column(name = "SENDER_REF_NO")
	protected String senderRefNo;
	
	@Column(name = "IS_PROCESSED")
	protected String isProcessed;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	protected CorporateModel corporate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_USR_GRP_ID")
	protected CorporateUserGroupModel corporateUserGroup;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_CD")
	protected IDMApplicationModel application;
	
	//save maker pendingTaskId for recurring trx
	@Column(name = "PENDING_TASK_ID")
	protected String pendingTaskId;

	// START Charge Information

	@Column(name = "CH_TYP_1_CD")
	protected String chargeType1;

	@Column(name = "CH_TYP_1_CCY_CD")
	protected String chargeTypeCurrency1;

	@Column(name = "CH_TYP_1_AMT", precision=25, scale=7)
	protected BigDecimal chargeTypeAmount1;

	@Column(name = "CH_TYP_2_CD")
	protected String chargeType2;

	@Column(name = "CH_TYP_2_CCY_CD")
	protected String chargeTypeCurrency2;

	@Column(name = "CH_TYP_2_AMT", precision=25, scale=7)
	protected BigDecimal chargeTypeAmount2;

	@Column(name = "CH_TYP_3_CD")
	protected String chargeType3;

	@Column(name = "CH_TYP_3_CCY_CD")
	protected String chargeTypeCurrency3;

	@Column(name = "CH_TYP_3_AMT", precision=25, scale=7)
	protected BigDecimal chargeTypeAmount3;

	@Column(name = "CH_TYP_4_CD")
	protected String chargeType4;

	@Column(name = "CH_TYP_4_CCY_CD")
	protected String chargeTypeCurrency4;

	@Column(name = "CH_TYP_4_AMT", precision=25, scale=7)
	protected BigDecimal chargeTypeAmount4;

	@Column(name = "CH_TYP_5_CD")
	protected String chargeType5;

	@Column(name = "CH_TYP_5_CCY_CD")
	protected String chargeTypeCurrency5;

	@Column(name = "CH_TYP_5_AMT", precision=25, scale=7)
	protected BigDecimal chargeTypeAmount5;
	
	@Column(name = "TOTAL_CH_AMT", precision=25, scale=7)
	protected BigDecimal totalChargeEquivalentAmount;

	@Column(name = "TOTAL_DEBIT_AMT", precision=25, scale=7)
	protected BigDecimal totalDebitedEquivalentAmount;
	
	// START Instruction Mode Information

	@Column(name = "INSTRUCTON_MODE")
	protected String instructionMode;

	@Column(name = "INSTRUCTON_DT")
	protected Timestamp instructionDate;

	@Column(name = "RECUR_PARAM_TYP_CD")
	protected String recurringParamType;

	@Column(name = "RECUR_PARAM")
	protected int recurringParam;

	@Column(name = "RECUR_START_DT")
	protected Timestamp recurringStartDate;
	
	@Column(name = "RECUR_END_DT")
	protected Timestamp recurringEndDate;

	// START Notification Information

	@Column(name = "IS_NOTIFY_BEN")
	protected String isNotifyBen;

	@Column(name = "NOTIFY_BEN_VALUE")
	protected String notifyBenValue;

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

	public String getParentReferenceNo() {
		return parentReferenceNo;
	}

	public void setParentReferenceNo(String parentReferenceNo) {
		this.parentReferenceNo = parentReferenceNo;
	}

	public String getTrxReferenceNo() {
		return trxReferenceNo;
	}

	public void setTrxReferenceNo(String trxReferenceNo) {
		this.trxReferenceNo = trxReferenceNo;
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

	public String getRemark1() {
		return remark1;
	}

	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}

	public String getRemark2() {
		return remark2;
	}

	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}

	public String getRemark3() {
		return remark3;
	}

	public void setRemark3(String remark3) {
		this.remark3 = remark3;
	}

	public String getIsProcessed() {
		return isProcessed;
	}

	public void setIsProcessed(String isProcessed) {
		this.isProcessed = isProcessed;
	}

	public String getChargeType1() {
		return chargeType1;
	}

	public void setChargeType1(String chargeType1) {
		this.chargeType1 = chargeType1;
	}

	public String getChargeTypeCurrency1() {
		return chargeTypeCurrency1;
	}

	public void setChargeTypeCurrency1(String chargeTypeCurrency1) {
		this.chargeTypeCurrency1 = chargeTypeCurrency1;
	}

	public BigDecimal getChargeTypeAmount1() {
		return chargeTypeAmount1;
	}

	public void setChargeTypeAmount1(BigDecimal chargeTypeAmount1) {
		this.chargeTypeAmount1 = chargeTypeAmount1;
	}

	public String getChargeType2() {
		return chargeType2;
	}

	public void setChargeType2(String chargeType2) {
		this.chargeType2 = chargeType2;
	}

	public String getChargeTypeCurrency2() {
		return chargeTypeCurrency2;
	}

	public void setChargeTypeCurrency2(String chargeTypeCurrency2) {
		this.chargeTypeCurrency2 = chargeTypeCurrency2;
	}

	public BigDecimal getChargeTypeAmount2() {
		return chargeTypeAmount2;
	}

	public void setChargeTypeAmount2(BigDecimal chargeTypeAmount2) {
		this.chargeTypeAmount2 = chargeTypeAmount2;
	}

	public String getChargeType3() {
		return chargeType3;
	}

	public void setChargeType3(String chargeType3) {
		this.chargeType3 = chargeType3;
	}

	public String getChargeTypeCurrency3() {
		return chargeTypeCurrency3;
	}

	public void setChargeTypeCurrency3(String chargeTypeCurrency3) {
		this.chargeTypeCurrency3 = chargeTypeCurrency3;
	}

	public BigDecimal getChargeTypeAmount3() {
		return chargeTypeAmount3;
	}

	public void setChargeTypeAmount3(BigDecimal chargeTypeAmount3) {
		this.chargeTypeAmount3 = chargeTypeAmount3;
	}

	public String getChargeType4() {
		return chargeType4;
	}

	public void setChargeType4(String chargeType4) {
		this.chargeType4 = chargeType4;
	}

	public String getChargeTypeCurrency4() {
		return chargeTypeCurrency4;
	}

	public void setChargeTypeCurrency4(String chargeTypeCurrency4) {
		this.chargeTypeCurrency4 = chargeTypeCurrency4;
	}

	public BigDecimal getChargeTypeAmount4() {
		return chargeTypeAmount4;
	}

	public void setChargeTypeAmount4(BigDecimal chargeTypeAmount4) {
		this.chargeTypeAmount4 = chargeTypeAmount4;
	}

	public String getChargeType5() {
		return chargeType5;
	}

	public void setChargeType5(String chargeType5) {
		this.chargeType5 = chargeType5;
	}

	public String getChargeTypeCurrency5() {
		return chargeTypeCurrency5;
	}

	public void setChargeTypeCurrency5(String chargeTypeCurrency5) {
		this.chargeTypeCurrency5 = chargeTypeCurrency5;
	}

	public BigDecimal getChargeTypeAmount5() {
		return chargeTypeAmount5;
	}

	public void setChargeTypeAmount5(BigDecimal chargeTypeAmount5) {
		this.chargeTypeAmount5 = chargeTypeAmount5;
	}

	public String getInstructionMode() {
		return instructionMode;
	}

	public void setInstructionMode(String instructionMode) {
		this.instructionMode = instructionMode;
	}

	public Timestamp getInstructionDate() {
		return instructionDate;
	}

	public void setInstructionDate(Timestamp instructionDate) {
		this.instructionDate = instructionDate;
	}

	public String getRecurringParamType() {
		return recurringParamType;
	}

	public void setRecurringParamType(String recurringParamType) {
		this.recurringParamType = recurringParamType;
	}

	public Timestamp getRecurringEndDate() {
		return recurringEndDate;
	}

	public void setRecurringEndDate(Timestamp recurringEndDate) {
		this.recurringEndDate = recurringEndDate;
	}

	public String getIsNotifyBen() {
		return isNotifyBen;
	}

	public void setIsNotifyBen(String isNotifyBen) {
		this.isNotifyBen = isNotifyBen;
	}

	public String getNotifyBenValue() {
		return notifyBenValue;
	}

	public void setNotifyBenValue(String notifyBenValue) {
		this.notifyBenValue = notifyBenValue;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getSenderRefNo() {
		return senderRefNo;
	}

	public void setSenderRefNo(String senderRefNo) {
		this.senderRefNo = senderRefNo;
	}

	public String getIsFinalPayment() {
		return isFinalPayment;
	}

	public void setIsFinalPayment(String isFinalPayment) {
		this.isFinalPayment = isFinalPayment;
	}

	public BigDecimal getTotalChargeEquivalentAmount() {
		return totalChargeEquivalentAmount;
	}

	public void setTotalChargeEquivalentAmount(BigDecimal totalChargeEquivalentAmount) {
		this.totalChargeEquivalentAmount = totalChargeEquivalentAmount;
	}

	public BigDecimal getTotalDebitedEquivalentAmount() {
		return totalDebitedEquivalentAmount;
	}

	public void setTotalDebitedEquivalentAmount(BigDecimal totalDebitedEquivalentAmount) {
		this.totalDebitedEquivalentAmount = totalDebitedEquivalentAmount;
	}

	public Timestamp getRecurringStartDate() {
		return recurringStartDate;
	}

	public void setRecurringStartDate(Timestamp recurringStartDate) {
		this.recurringStartDate = recurringStartDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
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

	public CorporateUserGroupModel getCorporateUserGroup() {
		return corporateUserGroup;
	}

	public void setCorporateUserGroup(CorporateUserGroupModel corporateUserGroup) {
		this.corporateUserGroup = corporateUserGroup;
	}

	public IDMApplicationModel getApplication() {
		return application;
	}

	public void setApplication(IDMApplicationModel application) {
		this.application = application;
	}

	public void setRecurringParam(int recurringParam) {
		this.recurringParam = recurringParam;
	}

	public int getRecurringParam() {
		return recurringParam;
	}

	public String getPendingTaskId() {
		return pendingTaskId;
	}

	public void setPendingTaskId(String pendingTaskId) {
		this.pendingTaskId = pendingTaskId;
	}

	public String getNpwpNo() {
		return npwpNo;
	}

	public void setNpwpNo(String npwpNo) {
		this.npwpNo = npwpNo;
	}

	public String getKppNo() {
		return kppNo;
	}

	public void setKppNo(String kppNo) {
		this.kppNo = kppNo;
	}

	public IdentityTypeModel getIdentityType() {
		return identityType;
	}

	public void setIdentityType(IdentityTypeModel identityType) {
		this.identityType = identityType;
	}

	public String getIdentityNo() {
		return identityNo;
	}

	public void setIdentityNo(String identityNo) {
		this.identityNo = identityNo;
	}

	public String getBenName() {
		return benName;
	}

	public void setBenName(String benName) {
		this.benName = benName;
	}

	public String getBenAddress1() {
		return benAddress1;
	}

	public void setBenAddress1(String benAddress1) {
		this.benAddress1 = benAddress1;
	}

	public String getBenAddress2() {
		return benAddress2;
	}

	public void setBenAddress2(String benAddress2) {
		this.benAddress2 = benAddress2;
	}

	public String getBenAddress3() {
		return benAddress3;
	}

	public void setBenAddress3(String benAddress3) {
		this.benAddress3 = benAddress3;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public DepositTypeModel getDepositType() {
		return depositType;
	}

	public void setDepositType(DepositTypeModel depositType) {
		this.depositType = depositType;
	}

	public String getNopNo() {
		return nopNo;
	}

	public void setNopNo(String nopNo) {
		this.nopNo = nopNo;
	}

	public String getSkNo() {
		return skNo;
	}

	public void setSkNo(String skNo) {
		this.skNo = skNo;
	}

	public String getPeriodMonthFrom() {
		return periodMonthFrom;
	}

	public void setPeriodMonthFrom(String periodMonthFrom) {
		this.periodMonthFrom = periodMonthFrom;
	}

	public String getPeriodMonthTo() {
		return periodMonthTo;
	}

	public void setPeriodMonthTo(String periodMonthTo) {
		this.periodMonthTo = periodMonthTo;
	}

	public String getPeriodYear() {
		return periodYear;
	}

	public void setPeriodYear(String periodYear) {
		this.periodYear = periodYear;
	}

	public TaxTypeModel getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxTypeModel taxType) {
		this.taxType = taxType;
	}

	public String getIsNpwp() {
		return isNpwp;
	}

	public void setIsNpwp(String isNpwp) {
		this.isNpwp = isNpwp;
	}

	public String getNtb() {
		return ntb;
	}

	public void setNtb(String ntb) {
		this.ntb = ntb;
	}

	public String getNtp() {
		return ntp;
	}

	public String getStan() {
		return stan;
	}

	public void setStan(String stan) {
		this.stan = stan;
	}

	public void setNtp(String ntp) {
		this.ntp = ntp;
	}

	public Timestamp getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Timestamp paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getBillingId() {
		return billingId;
	}

	public void setBillingId(String billingId) {
		this.billingId = billingId;
	}

	public String getDepositorNpwpNo() {
		return depositorNpwpNo;
	}

	public void setDepositorNpwpNo(String depositorNpwpNo) {
		this.depositorNpwpNo = depositorNpwpNo;
	}

	public Timestamp getBillExpiryDate() {
		return billExpiryDate;
	}

	public void setBillExpiryDate(Timestamp billExpiryDate) {
		this.billExpiryDate = billExpiryDate;
	}
	
}

