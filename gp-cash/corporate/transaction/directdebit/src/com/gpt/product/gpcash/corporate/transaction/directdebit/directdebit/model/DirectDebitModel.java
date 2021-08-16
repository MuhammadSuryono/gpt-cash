package com.gpt.product.gpcash.corporate.transaction.directdebit.directdebit.model;

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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Entity
@Table(name = "TRX_DIRECT_DEBIT")
public class DirectDebitModel implements Serializable {
	// START Basic Information
	@Id
	@Column(name = "ID")
	private String id;
	
	@Column(name = "REF_NO")
	private String referenceNo;

	@Version
	private int version;

	@Column(name = "FILE_NM")
	private String fileName;
	
	@Column(name = "FILE_FORMAT")
	private String fileFormat;

	@Column(name = "FILE_DSCP")
	private String fileDescription;

	@Column(name = "TRX_REFERENCE_NO")
	private String trxReferenceNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD", nullable = false)
	private IDMMenuModel menu;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SRVC_CD", nullable = false)
	private ServiceModel service;

	// START Transaction Information

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CREDIT_ACCT_NO", nullable = false)
	private AccountModel creditAccount;

	@Column(name = "TOTAL_AMOUNT", precision=25, scale=7)
	private BigDecimal totalAmount;
	
	@Column(name = "TRX_CCY_CD")
	private String trxCurrencyCode;	
	
	@Column(name = "TOTAL_RECORD")
	private int totalRecord;
	
	@Column(name = "TOTAL_ERROR")
	private int totalError;

	@Column(name = "IS_HOST_PROCESSED")
	private String isHostProcessed;

	@Column(name = "IS_PROCESSED")
	private String isProcessed;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	private CorporateModel corporate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_USR_GRP_ID")
	private CorporateUserGroupModel corporateUserGroup;	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_CD")
	private IDMApplicationModel application;
	
	@OrderBy("idx ASC")
	@OneToMany(mappedBy = "directDebit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<DirectDebitDetailModel> directDebitDetail;

	// START Charge Information

	@Column(name = "CH_TYP_1_CD")
	private String chargeType1;

	@Column(name = "CH_TYP_1_CCY_CD")
	private String chargeTypeCurrency1;

	@Column(name = "CH_TYP_1_AMT", precision=25, scale=7)
	private BigDecimal chargeTypeAmount1;

	@Column(name = "CH_TYP_2_CD")
	private String chargeType2;

	@Column(name = "CH_TYP_2_CCY_CD")
	private String chargeTypeCurrency2;

	@Column(name = "CH_TYP_2_AMT", precision=25, scale=7)
	private BigDecimal chargeTypeAmount2;

	@Column(name = "CH_TYP_3_CD")
	private String chargeType3;

	@Column(name = "CH_TYP_3_CCY_CD")
	private String chargeTypeCurrency3;

	@Column(name = "CH_TYP_3_AMT", precision=25, scale=7)
	private BigDecimal chargeTypeAmount3;

	@Column(name = "CH_TYP_4_CD")
	private String chargeType4;

	@Column(name = "CH_TYP_4_CCY_CD")
	private String chargeTypeCurrency4;

	@Column(name = "CH_TYP_4_AMT", precision=25, scale=7)
	private BigDecimal chargeTypeAmount4;

	@Column(name = "CH_TYP_5_CD")
	private String chargeType5;

	@Column(name = "CH_TYP_5_CCY_CD")
	private String chargeTypeCurrency5;

	@Column(name = "CH_TYP_5_AMT", precision=25, scale=7)
	private BigDecimal chargeTypeAmount5;
	
	@Column(name = "TOTAL_CH_AMT", precision=25, scale=7)
	private BigDecimal totalChargeEquivalentAmount;

	@Column(name = "TOTAL_CREDIT_AMT", precision=25, scale=7)
	private BigDecimal totalDebitedEquivalentAmount;

	// START Instruction Mode Information
	@Column(name = "INSTRUCTON_MODE")
	private String instructionMode;

	@Column(name = "INSTRUCTON_DT")
	private Timestamp instructionDate;

	// START Default Information
	@Column(name = "CREATED_BY")
	private String createdBy;
	
	@Column(name = "CREATED_DT")
	private Timestamp createdDate;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Column(name = "UPDATED_DT")
	private Timestamp updatedDate;
	
	@Column(name = "IS_ERROR")
	private String isError;
	
	@Column(name = "ERROR_CD")
	private String errorCode;
	
	@Column(name = "STATUS")
	private String status;

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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public String getFileDescription() {
		return fileDescription;
	}

	public void setFileDescription(String fileDescription) {
		this.fileDescription = fileDescription;
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

	public AccountModel getCreditAccount() {
		return creditAccount;
	}

	public void setCreditAccount(AccountModel creditAccount) {
		this.creditAccount = creditAccount;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getTrxCurrencyCode() {
		return trxCurrencyCode;
	}

	public void setTrxCurrencyCode(String trxCurrencyCode) {
		this.trxCurrencyCode = trxCurrencyCode;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
	}

	public int getTotalError() {
		return totalError;
	}

	public void setTotalError(int totalError) {
		this.totalError = totalError;
	}

	public String getIsHostProcessed() {
		return isHostProcessed;
	}

	public void setIsHostProcessed(String isHostProcessed) {
		this.isHostProcessed = isHostProcessed;
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

	public List<DirectDebitDetailModel> getDirectDebitDetail() {
		return directDebitDetail;
	}

	public void setDirectDebitDetail(List<DirectDebitDetailModel> directDebitDetail) {
		this.directDebitDetail = directDebitDetail;
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
	
	
}
