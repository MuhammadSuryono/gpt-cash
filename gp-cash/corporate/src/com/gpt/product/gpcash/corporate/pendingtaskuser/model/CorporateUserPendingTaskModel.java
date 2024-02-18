package com.gpt.product.gpcash.corporate.pendingtaskuser.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Entity
@Table(name = "CORPUSR_WF_PENDING", indexes = {
	@Index(name="CORPUSR_WF_PENDING_UNIQUE1", unique = true, columnList = "REFERENCE_NO"),
	@Index(name="CORPUSR_WF_PENDING_INDEX1", unique = false, columnList = "STATUS")	
})
public class CorporateUserPendingTaskModel implements Serializable {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Column(name = "REFERENCE_NO", nullable = false)
	protected String referenceNo;
	
	@Column(name = "SENDER_REF_NO")
	protected String senderRefNo;
	
	@Column(name = "IS_FINAL_PAYMENT")
	protected String isFinalPayment;
	
	@Column(name = "BEN_REF_NO")
	protected String benRefNo;
	
	@Column(name = "SESSION_TIME")
	protected String sessionTime;

	@Column(name = "action", nullable = false)
	protected String action;

	@Column(name = "UNIQUE_KEY", length = 1024, nullable = false)
	protected String uniqueKey;
	
	@Column(name = "UNIQUE_KEY_DISPLAY", length = 1024)
	protected String uniqueKeyDisplay;

	@Column(name = "STATUS", nullable = false)
	protected String status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD", nullable = false)
	protected IDMMenuModel menu;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SRVC_CD")
	protected ServiceModel transactionService;

	@Column(name = "MODEL", nullable = false)
	protected String model;

	@Column(name = "SERVICE", nullable = false)
	protected String service;
	
	@Column(name = "TRX_AMT")
	protected BigDecimal transactionAmount;

	@Column(name = "TRX_CCY_CD")
	protected String transactionCurrency;
	
	@Column(name = "SOURCE_ACCT_NO")
	protected String sourceAccount;
	
	@Column(name = "SOURCE_ACCT_NM")
	protected String sourceAccountName;
	
	@Column(name = "SOURCE_ACCT_CCY_CD")
	protected String sourceAccountCurrencyCode;
	
	@Column(name = "SOURCE_ACCT_CCY_NM")
	protected String sourceAccountCurrencyName;
	
	@Column(name = "BEN_ACCT_NO")
	protected String benAccount;
	
	@Column(name = "BEN_ACCT_NM")
	protected String benAccountName;
	
	@Column(name = "BEN_ACCT_CCY_CD")
	protected String benAccountCurrencyCode;
	
	@Column(name = "BEN_ACCT_CCY_NM")
	protected String benAccountCurrencyName;
	
	@Column(name = "BEN_BANK_CD")
	protected String benBankCode;
	
	@Column(name = "SOURCE_ACCT_NO_GRP_DTL_ID")
	protected String sourceAccountGroupDetailId;
	
	@Column(name = "TOTAL_CH_AMT")
	protected BigDecimal totalChargeEquivalentAmount;

	@Column(name = "TOTAL_DEBIT_AMT")
	protected BigDecimal totalDebitedEquivalentAmount;
	
	@Column(name = "INSTRUCTON_MODE")
	protected String instructionMode;

	@Column(name = "INSTRUCTON_DT")
	protected Timestamp instructionDate;
	
	@Column(name = "RECUR_PARAM_TYP_CD")
	protected String recurringParamType;

	@Column(name = "RECUR_PARAM")
	protected Integer recurringParam;

	@Column(name = "RECUR_START_DT")
	protected Timestamp recurringStartDate;
	
	@Column(name = "RECUR_END_DT")
	protected Timestamp recurringEndDate;
	
	@Column(name = "TASK_EXPIRY_DT")
	protected Timestamp taskExpiryDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	protected CorporateModel corporate;
	
	@Column(name = "USER_GRP_ID")
	protected String userGroupId;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(
		name = "CORPUSR_WF_PENDING_VALUE", 
		joinColumns = @JoinColumn(name="PARENT_ID")
    )
	@OrderColumn(name = "IDX")
	@Column(name = "VALUES_", length = 1024)
	protected List<String> values;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(
		name = "CORPUSR_WF_PENDING_OLD_VALUE",
		joinColumns = @JoinColumn(name="PARENT_ID")
    )
	@OrderColumn(name = "IDX")
	@Column(name = "VALUES_", length = 1024)
	protected List<String> oldValues;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;
	
	@Column(name="ACTIVITY_DT")
	protected Timestamp activityDate;

	@Column(name="ACTIVITY_BY")
	protected String activityBy;
	
	@Column(name="TRX_STATUS")
	protected TransactionStatus trxStatus;
	
	@Column(name = "REMARK1")
	protected String remark1;

	@Column(name = "REMARK2")
	protected String remark2;

	@Column(name = "REMARK3")
	protected String remark3;
	
	@Column(name = "BILLING_ID")
	protected String billingId;
	
	@Column(name = "BILLING_EXPIRY_DT")
	protected Timestamp billExpiryDate;
	
	@Column(name = "IS_ERROR_TIMEOUT")
	protected String errorTimeoutFlag;

	@Column(name = "HOST_REF_NO")
	protected String hostRefNo;
	
	@Column(name = "RETRIEVAL_REF_NO")
	protected String retrievalRefNo;
	
	@Column(name = "REFNO_SPECIALRATE")
	protected String refNoSpecialRate;
	
	@Column(name = "SRVC_CCY_MTRX_CD")
	protected String serviceCurrencyMatrix;
	
	@Column(name = "IS_CHECK_COT")
	protected String checkCOTFlag;
	
	public String getCheckCOTFlag() {
		return checkCOTFlag;
	}

	public void setCheckCOTFlag(String checkCOTFlag) {
		this.checkCOTFlag = checkCOTFlag;
	}
	
    public String getServiceCurrencyMatrix() {
		return serviceCurrencyMatrix;
	}

	public void setServiceCurrencyMatrix(String serviceCurrencyMatrix) {
		this.serviceCurrencyMatrix = serviceCurrencyMatrix;
	}

	public String getRefNoSpecialRate() {
		return refNoSpecialRate;
	}

	public void setRefNoSpecialRate(String refNoSpecialRate) {
		this.refNoSpecialRate = refNoSpecialRate;
	}

		public String getErrorTimeoutFlag() {
		return errorTimeoutFlag;
	}

	public void setErrorTimeoutFlag(String errorTimeoutFlag) {
		this.errorTimeoutFlag = errorTimeoutFlag;
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

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

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

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<String> getValues() {
		return values;
	}

	public String getValuesStr() {
		if(values != null && !values.isEmpty()) {
			return Helper.glueStringChopsBackTogether(values);
		}
		return null;
	}
	
	public String getUserGroupId() {
		return userGroupId;
	}

	public void setUserGroupId(String userGroupId) {
		this.userGroupId = userGroupId;
	}

	public void setValues(String values) throws Exception {
		List<String> listOfRecord = Helper.chopItUp(values);

		if (ValueUtils.hasValue(listOfRecord)) {
			setValues(listOfRecord);
		}
	}
	
	public void setValues(List<String> values) {
		this.values = values;
	}

	public List<String> getOldValues() {
		return oldValues;
	}

	public String getOldValuesStr() {
		if(oldValues != null && !oldValues.isEmpty()) {
			return Helper.glueStringChopsBackTogether(oldValues);
		}
		return null;
	}
	
	public void setOldValues(String oldValues) throws Exception {
		List<String> listOfRecord = Helper.chopItUp(oldValues);

		if (ValueUtils.hasValue(listOfRecord)) {
			setOldValues(listOfRecord);
		}
	}
	
	public void setOldValues(List<String> oldValues) {
		this.oldValues = oldValues;
	}
	
	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
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

	public String getSourceAccount() {
		return sourceAccount;
	}

	public void setSourceAccount(String sourceAccount) {
		this.sourceAccount = sourceAccount;
	}

	
	public String getSourceAccountGroupDetailId() {
		return sourceAccountGroupDetailId;
	}

	public void setSourceAccountGroupDetailId(String sourceAccountGroupDetailId) {
		this.sourceAccountGroupDetailId = sourceAccountGroupDetailId;
	}
	
	public IDMMenuModel getMenu() {
		return menu;
	}

	public void setMenu(IDMMenuModel menu) {
		this.menu = menu;
	}

	public ServiceModel getTransactionService() {
		return transactionService;
	}

	public void setTransactionService(ServiceModel transactionService) {
		this.transactionService = transactionService;
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

	public Timestamp getRecurringStartDate() {
		return recurringStartDate;
	}

	public void setRecurringStartDate(Timestamp recurringStartDate) {
		this.recurringStartDate = recurringStartDate;
	}

	public Timestamp getRecurringEndDate() {
		return recurringEndDate;
	}

	public void setRecurringEndDate(Timestamp recurringEndDate) {
		this.recurringEndDate = recurringEndDate;
	}
	
	public Timestamp getTaskExpiryDate() {
		return taskExpiryDate;
	}

	public void setTaskExpiryDate(Timestamp taskExpiryDate) {
		this.taskExpiryDate = taskExpiryDate;
	}
	public String getUniqueKeyDisplay() {
		return uniqueKeyDisplay;
	}

	public void setUniqueKeyDisplay(String uniqueKeyDisplay) {
		this.uniqueKeyDisplay = uniqueKeyDisplay;
	}
	
	public Integer getRecurringParam() {
		return recurringParam;
	}

	public void setRecurringParam(Integer recurringParam) {
		this.recurringParam = recurringParam;
	}
	
	

	public String getSourceAccountName() {
		return sourceAccountName;
	}

	public void setSourceAccountName(String sourceAccountName) {
		this.sourceAccountName = sourceAccountName;
	}

	public String getSourceAccountCurrencyCode() {
		return sourceAccountCurrencyCode;
	}

	public void setSourceAccountCurrencyCode(String sourceAccountCurrencyCode) {
		this.sourceAccountCurrencyCode = sourceAccountCurrencyCode;
	}

	public String getSourceAccountCurrencyName() {
		return sourceAccountCurrencyName;
	}

	public void setSourceAccountCurrencyName(String sourceAccountCurrencyName) {
		this.sourceAccountCurrencyName = sourceAccountCurrencyName;
	}

	public String getBenAccount() {
		return benAccount;
	}

	public void setBenAccount(String benAccount) {
		this.benAccount = benAccount;
	}

	public String getBenAccountName() {
		return benAccountName;
	}

	public void setBenAccountName(String benAccountName) {
		this.benAccountName = benAccountName;
	}

	public String getBenAccountCurrencyCode() {
		return benAccountCurrencyCode;
	}

	public void setBenAccountCurrencyCode(String benAccountCurrencyCode) {
		this.benAccountCurrencyCode = benAccountCurrencyCode;
	}

	public String getBenAccountCurrencyName() {
		return benAccountCurrencyName;
	}

	public void setBenAccountCurrencyName(String benAccountCurrencyName) {
		this.benAccountCurrencyName = benAccountCurrencyName;
	}

	public TransactionStatus getTrxStatus() {
		return trxStatus;
	}
	
	public void setTrxStatus(TransactionStatus trxStatus) {
		this.trxStatus = trxStatus;
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

	public String getSessionTime() {
		return sessionTime;
	}

	public void setSessionTime(String sessionTime) {
		this.sessionTime = sessionTime;
	}

	public String getActivityBy() {
		return activityBy;
	}
	
	public void setActivityBy(String activityBy) {
		this.activityBy = activityBy;
	}
	
	public void setActivityDate(Timestamp activityDate) {
		this.activityDate = activityDate;
	}
	
	public Timestamp getActivityDate() {
		return activityDate;
	}
	
	public String getIsFinalPayment() {
		return isFinalPayment;
	}

	public void setIsFinalPayment(String isFinalPayment) {
		this.isFinalPayment = isFinalPayment;
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
	
	public String getBenBankCode() {
		return benBankCode;
	}

	public void setBenBankCode(String benBankCode) {
		this.benBankCode = benBankCode;
	}
	
	public String getBillingId() {
		return billingId;
	}
	
	public void setBillingId(String billingId) {
		this.billingId = billingId;
	}
	
	public Timestamp getBillExpiryDate() {
		return billExpiryDate;
	}
	
	public void setBillExpiryDate(Timestamp billExpiryDate) {
		this.billExpiryDate = billExpiryDate;
	}

	public String getHostRefNo() {
		return hostRefNo;
	}

	public void setHostRefNo(String hostRefNo) {
		this.hostRefNo = hostRefNo;
	}

	public String getRetrievalRefNo() {
		return retrievalRefNo;
	}

	public void setRetrievalRefNo(String retrievalRefNo) {
		this.retrievalRefNo = retrievalRefNo;
	}

	public String toString()
	  {
	    StringBuffer localStringBuffer = new StringBuffer();
	    localStringBuffer.append("PendingTaskModel");
	    localStringBuffer.append("{");
	    localStringBuffer.append("id=").append(this.id);
	    localStringBuffer.append(",uniqueKey=").append(this.uniqueKey);
	    localStringBuffer.append(",referenceNo=").append(this.referenceNo);
	    localStringBuffer.append(",action=").append(this.action);
	    localStringBuffer.append(",status=").append(this.status);
	    localStringBuffer.append(",service=").append(this.service);
	    localStringBuffer.append(",model=").append(this.model);
	    localStringBuffer.append(",transactionAmount=").append(this.transactionAmount);
	    localStringBuffer.append(",transactionCurrency=").append(this.transactionCurrency);
	    localStringBuffer.append(",sourceAccount=").append(this.sourceAccount);
	    localStringBuffer.append(",createdBy=").append(this.createdBy);
	    localStringBuffer.append(",createdDate=").append(this.createdDate);
	    localStringBuffer.append('}');
	    return localStringBuffer.toString();
	  }
}

