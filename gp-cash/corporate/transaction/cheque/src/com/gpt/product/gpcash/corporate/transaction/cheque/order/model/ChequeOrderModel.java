package com.gpt.product.gpcash.corporate.transaction.cheque.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.IdentityTypeModel;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Entity
@Table(name = "TRX_CHQ_ORDER")
public class ChequeOrderModel implements Serializable {
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
	@OrderColumn(name = "IDX")
	@OneToMany(mappedBy = "chequeOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ChequeOrderDetailModel> chequeOrderDetail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_ACCT_NO", nullable = false)
	protected AccountModel sourceAccount;
	
	@Column(name = "ORDER_NO")
	protected String orderNo;
	
	@Column(name = "NO_OF_PAGES")
	protected int noOfPages;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_CD", nullable = false)
	protected BranchModel branch;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDENTITY_TYP_CD")
	protected IdentityTypeModel identityType;
	
	@Column(name = "IDENTITY_NO")
	protected String identityNo;
	
	@Column(name = "IDENTITY_NM")
	protected String identityName;
	
	@Column(name = "HANDOVER_DT")
	protected Timestamp handoverDate;
	
	@Column(name = "HANDOVER_REF_NO")
	protected String handoverReferenceNo;
	
	@Column(name = "MOBILE_NO")
	protected String mobileNo;
	
	@Column(name = "CHEQUE_NO_FROM")
	protected String chequeNoFrom;
	
	@Column(name = "CHEQUE_NO_TO")
	protected String chequeNoTo;
	
	@Column(name = "CHEQUE_SERIAL")
	protected String chequeSerial;
	
	@Column(name = "DECLINE_REF_NO")
	protected String declineReferenceNo;
	
	@Column(name = "DECLINE_REASON")
	protected String declineReason;
	
	@Column(name = "DECLINE_DT")
	protected Timestamp declineDate;
	
	@Column(name = "IS_FEE_DEBITED")
	protected String isFeeDebited;
	
	@Column(name = "FEE_DEBITED_DT")
	protected Timestamp feeDebitedDate;
	
	@Column(name = "PROCESS_REF_NO")
	protected String processReferenceNo;
	
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

	public int getNoOfPages() {
		return noOfPages;
	}

	public void setNoOfPages(int noOfPages) {
		this.noOfPages = noOfPages;
	}

	public BranchModel getBranch() {
		return branch;
	}

	public void setBranch(BranchModel branch) {
		this.branch = branch;
	}

	public String getIsProcessed() {
		return isProcessed;
	}

	public void setIsProcessed(String isProcessed) {
		this.isProcessed = isProcessed;
	}

	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
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

	public String getPendingTaskId() {
		return pendingTaskId;
	}

	public void setPendingTaskId(String pendingTaskId) {
		this.pendingTaskId = pendingTaskId;
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

	public int getRecurringParam() {
		return recurringParam;
	}

	public void setRecurringParam(int recurringParam) {
		this.recurringParam = recurringParam;
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

	public String getIdentityName() {
		return identityName;
	}

	public void setIdentityName(String identityName) {
		this.identityName = identityName;
	}

	public Timestamp getHandoverDate() {
		return handoverDate;
	}

	public void setHandoverDate(Timestamp handoverDate) {
		this.handoverDate = handoverDate;
	}

	public String getChequeNoFrom() {
		return chequeNoFrom;
	}

	public void setChequeNoFrom(String chequeNoFrom) {
		this.chequeNoFrom = chequeNoFrom;
	}

	public String getChequeNoTo() {
		return chequeNoTo;
	}

	public void setChequeNoTo(String chequeNoTo) {
		this.chequeNoTo = chequeNoTo;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public List<ChequeOrderDetailModel> getChequeOrderDetail() {
		return chequeOrderDetail;
	}

	public void setChequeOrderDetail(List<ChequeOrderDetailModel> chequeOrderDetail) {
		this.chequeOrderDetail = chequeOrderDetail;
	}

	public String getDeclineReferenceNo() {
		return declineReferenceNo;
	}

	public void setDeclineReferenceNo(String declineReferenceNo) {
		this.declineReferenceNo = declineReferenceNo;
	}

	public String getHandoverReferenceNo() {
		return handoverReferenceNo;
	}

	public void setHandoverReferenceNo(String handoverReferenceNo) {
		this.handoverReferenceNo = handoverReferenceNo;
	}

	public String getIsFeeDebited() {
		return isFeeDebited;
	}

	public void setIsFeeDebited(String isFeeDebited) {
		this.isFeeDebited = isFeeDebited;
	}

	public Timestamp getFeeDebitedDate() {
		return feeDebitedDate;
	}

	public void setFeeDebitedDate(Timestamp feeDebitedDate) {
		this.feeDebitedDate = feeDebitedDate;
	}

	public String getDeclineReason() {
		return declineReason;
	}

	public void setDeclineReason(String declineReason) {
		this.declineReason = declineReason;
	}

	public Timestamp getDeclineDate() {
		return declineDate;
	}

	public void setDeclineDate(Timestamp declineDate) {
		this.declineDate = declineDate;
	}

	public String getProcessReferenceNo() {
		return processReferenceNo;
	}

	public void setProcessReferenceNo(String processReferenceNo) {
		this.processReferenceNo = processReferenceNo;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getChequeSerial() {
		return chequeSerial;
	}

	public void setChequeSerial(String chequeSerial) {
		this.chequeSerial = chequeSerial;
	}
	
}
