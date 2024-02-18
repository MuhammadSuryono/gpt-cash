package com.gpt.product.gpcash.retail.transaction.inhouse.model;

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
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Entity
@Table(name = "CUST_TRX_INHOUSE")
public class CustomerInHouseTransferModel implements Serializable {

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

	@Column(name = "BEN_ACCT_NO")
	protected String benAccountNo;

	@Column(name = "BEN_ACCT_NM")
	protected String benAccountName;

	@Column(name = "BEN_ACCT_CCY_CD")
	protected String benAccountCurrency;

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
	
	@Column(name = "BEN_REF_NO")
	protected String benRefNo;

	@Column(name = "IS_PROCESSED")
	protected String isProcessed;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUST_ID", nullable = false)
	protected CustomerModel customer;
	
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
	
	@Column(name = "CH_TYP_1_AMT_EQ", precision=25, scale=7)
	protected BigDecimal chargeTypeAmountEquivalent1;

	@Column(name = "CH_TYP_2_CD")
	protected String chargeType2;

	@Column(name = "CH_TYP_2_CCY_CD")
	protected String chargeTypeCurrency2;

	@Column(name = "CH_TYP_2_AMT", precision=25, scale=7)
	protected BigDecimal chargeTypeAmount2;

	@Column(name = "CH_TYP_2_AMT_EQ", precision=25, scale=7)
	protected BigDecimal chargeTypeAmountEquivalent2;
	
	@Column(name = "CH_TYP_3_CD")
	protected String chargeType3;

	@Column(name = "CH_TYP_3_CCY_CD")
	protected String chargeTypeCurrency3;

	@Column(name = "CH_TYP_3_AMT", precision=25, scale=7)
	protected BigDecimal chargeTypeAmount3;
	
	@Column(name = "CH_TYP_3_AMT_EQ", precision=25, scale=7)
	protected BigDecimal chargeTypeAmountEquivalent3;

	@Column(name = "CH_TYP_4_CD")
	protected String chargeType4;

	@Column(name = "CH_TYP_4_CCY_CD")
	protected String chargeTypeCurrency4;

	@Column(name = "CH_TYP_4_AMT", precision=25, scale=7)
	protected BigDecimal chargeTypeAmount4;
	
	@Column(name = "CH_TYP_4_AMT_EQ", precision=25, scale=7)
	protected BigDecimal chargeTypeAmountEquivalent4;

	@Column(name = "CH_TYP_5_CD")
	protected String chargeType5;

	@Column(name = "CH_TYP_5_CCY_CD")
	protected String chargeTypeCurrency5;

	@Column(name = "CH_TYP_5_AMT", precision=25, scale=7)
	protected BigDecimal chargeTypeAmount5;
	
	@Column(name = "CH_TYP_5_AMT_EQ", precision=25, scale=7)
	protected BigDecimal chargeTypeAmountEquivalent5;
	
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
	
	@Column(name="REFNO_SPECIALRATE")
	protected String refNoSpecialRate;
	
	public String getRefNoSpecialRate() {
		return refNoSpecialRate;
	}

	public void setRefNoSpecialRate(String refNoSpecialRate) {
		this.refNoSpecialRate = refNoSpecialRate;
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

	public String getBenAccountNo() {
		return benAccountNo;
	}

	public void setBenAccountNo(String benAccountNo) {
		this.benAccountNo = benAccountNo;
	}

	public String getBenAccountName() {
		return benAccountName;
	}

	public void setBenAccountName(String benAccountName) {
		this.benAccountName = benAccountName;
	}

	public String getBenAccountCurrency() {
		return benAccountCurrency;
	}

	public void setBenAccountCurrency(String benAccountCurrency) {
		this.benAccountCurrency = benAccountCurrency;
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

	public String getBenRefNo() {
		return benRefNo;
	}

	public void setBenRefNo(String benRefNo) {
		this.benRefNo = benRefNo;
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

	public CustomerModel getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerModel customer) {
		this.customer = customer;
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

	public BigDecimal getChargeTypeAmountEquivalent1() {
		return chargeTypeAmountEquivalent1;
	}

	public void setChargeTypeAmountEquivalent1(BigDecimal chargeTypeAmountEquivalent1) {
		this.chargeTypeAmountEquivalent1 = chargeTypeAmountEquivalent1;
	}

	public BigDecimal getChargeTypeAmountEquivalent2() {
		return chargeTypeAmountEquivalent2;
	}

	public void setChargeTypeAmountEquivalent2(BigDecimal chargeTypeAmountEquivalent2) {
		this.chargeTypeAmountEquivalent2 = chargeTypeAmountEquivalent2;
	}

	public BigDecimal getChargeTypeAmountEquivalent3() {
		return chargeTypeAmountEquivalent3;
	}

	public void setChargeTypeAmountEquivalent3(BigDecimal chargeTypeAmountEquivalent3) {
		this.chargeTypeAmountEquivalent3 = chargeTypeAmountEquivalent3;
	}

	public BigDecimal getChargeTypeAmountEquivalent4() {
		return chargeTypeAmountEquivalent4;
	}

	public void setChargeTypeAmountEquivalent4(BigDecimal chargeTypeAmountEquivalent4) {
		this.chargeTypeAmountEquivalent4 = chargeTypeAmountEquivalent4;
	}

	public BigDecimal getChargeTypeAmountEquivalent5() {
		return chargeTypeAmountEquivalent5;
	}

	public void setChargeTypeAmountEquivalent5(BigDecimal chargeTypeAmountEquivalent5) {
		this.chargeTypeAmountEquivalent5 = chargeTypeAmountEquivalent5;
	}
	
	
}
