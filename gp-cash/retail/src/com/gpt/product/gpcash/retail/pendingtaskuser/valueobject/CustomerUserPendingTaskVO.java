package com.gpt.product.gpcash.retail.pendingtaskuser.valueobject;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class CustomerUserPendingTaskVO {
	protected String id;
	protected String customerId;
	protected String uniqueKey;
	protected String uniqueKeyDisplay;
	protected String referenceNo;
	protected String senderRefNo;
	protected String benRefNo;
	protected String action;
	protected String createdBy;
	protected String menuCode;
	protected Object jsonObject;
	protected Object jsonObjectOld;
	protected String status;
	protected String service;
	protected Timestamp createdDate;
	protected String model;
	protected BigDecimal transactionAmount;
	protected String transactionCurrency;
	protected String isFinalPayment;
	
	protected String sourceAccount;
	protected String sourceAccountName;
	protected String sourceAccountCurrencyCode;
	protected String sourceAccountCurrencyName;
	
	protected String benAccount;
	protected String benAccountName;
	protected String benAccountCurrencyCode;
	protected String benAccountCurrencyName;
	protected String benBankCode;
	
	protected String sourceAccountDetailId;
	protected String transactionServiceCode;
	protected BigDecimal totalChargeEquivalentAmount;
	protected BigDecimal totalDebitedEquivalentAmount;
	protected String instructionMode;
	protected Timestamp instructionDate;
	protected String recurringParamType;
	protected Integer recurringParam;
	protected Timestamp recurringStartDate;
	protected Timestamp recurringEndDate;
	protected Timestamp taskExpiryDate;
	
	protected String sessionTime;
	
	protected String actionBy;
	
	protected String isError;
	protected String errorCode;
	
	protected String remark1;
	protected String remark2;
	protected String remark3;
	
	
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

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getStatus() {
		return status;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getMenuCode() {
		return menuCode;
	}

	public void setMenuCode(String menuCode) {
		this.menuCode = menuCode;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Object getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(Object jsonObject) {
		this.jsonObject = jsonObject;
	}

	public Object getJsonObjectOld() {
		return jsonObjectOld;
	}

	public void setJsonObjectOld(Object jsonObjectOld) {
		this.jsonObjectOld = jsonObjectOld;
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

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
	public String getSourceAccount() {
		return sourceAccount;
	}

	public void setSourceAccount(String sourceAccount) {
		this.sourceAccount = sourceAccount;
	}
	
	public String getTransactionServiceCode() {
		return transactionServiceCode;
	}

	public void setTransactionServiceCode(String transactionServiceCode) {
		this.transactionServiceCode = transactionServiceCode;
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

	public String getActionBy() {
		return actionBy;
	}

	public void setActionBy(String actionBy) {
		this.actionBy = actionBy;
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
	public String getSourceAccountDetailId() {
		return sourceAccountDetailId;
	}

	public void setSourceAccountDetailId(String sourceAccountDetailId) {
		this.sourceAccountDetailId = sourceAccountDetailId;
	}

	public String toString()
	  {
	    StringBuffer localStringBuffer = new StringBuffer();
	    localStringBuffer.append("PendingTaskVO");
	    localStringBuffer.append("{");
	    localStringBuffer.append("id=").append(this.id);
	    localStringBuffer.append(",customerId=").append(this.customerId);
	    localStringBuffer.append(",uniqueKey=").append(this.uniqueKey);
	    localStringBuffer.append(",referenceNo=").append(this.referenceNo);
	    localStringBuffer.append(",action=").append(this.action);
	    localStringBuffer.append(",menuCode=").append(this.menuCode);
	    localStringBuffer.append(",status=").append(this.status);
	    localStringBuffer.append(",service=").append(this.service);
	    localStringBuffer.append(",model=").append(this.model);
	    localStringBuffer.append(",transactionServiceCode=").append(this.transactionServiceCode);
	    localStringBuffer.append(",createdBy=").append(this.createdBy);
	    localStringBuffer.append(",createdDate=").append(this.createdDate);
	    localStringBuffer.append(",jsonObject=").append(this.jsonObject);
	    localStringBuffer.append(",jsonObjectOld=").append(this.jsonObjectOld);
	    localStringBuffer.append('}');
	    return localStringBuffer.toString();
	  }
}
