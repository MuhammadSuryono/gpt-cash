package com.gpt.product.gpcash.corporate.transaction.pendingupload.model;

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

import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.service.model.ServiceModel;

@Entity
@Table(name = "TRX_PENDING_UPLOAD")
public class PendingUploadModel implements Serializable {

	@Id
	@Column(name = "ID")
	private String id;
	
	@Column(name = "FILE_NM")
	private String fileName;
	
	@Column(name = "FILE_FORMAT")
	private String fileFormat;

	@Column(name = "FILE_DSCP")
	private String fileDescription;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_ACCT_NO", nullable = false)
	protected AccountModel sourceAccount;	
	
	@Column(name = "UPLOAD_DATE")
	private Timestamp uploadDate;

	@Column(name = "TOTAL_RECORD")
	private int totalRecord;
	
	@Column(name = "TOTAL_ERROR")
	private int totalError;
	
	@Column(name = "TRX_CCY_CD")
	private String trxCurrencyCode;

	@Column(name = "TOTAL_AMOUNT")
	private BigDecimal totalAmount;
	
	@Column(name = "INSTRUCTON_MODE")
	private String instructionMode;

	@Column(name = "INSTRUCTON_DT")
	private Timestamp instructionDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	private CorporateModel corporate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_USR_GRP_ID")
	private CorporateUserGroupModel corporateUserGroup;	
	
	@Column(name = "CORP_ACCT_GRP_DTL_ID")
	protected String corporateAccountGroupDetail;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD", nullable = false)
	private IDMMenuModel menu;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SRVC_CD")
	private ServiceModel service;
	
	@Column(name = "STATUS")
	private String status;

	@Column(name = "IS_PROCESSED")
	private String isProcessed;	
	
	@OrderColumn(name = "IDX")
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PendingUploadDetailModel> details;
	
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PendingUploadErrorModel> errors;	
	
	@Version
	private int version;

	// START Default Information
	@Column(name = "CREATED_BY")
	private String createdBy;
	
	@Column(name = "CREATED_DT")
	private Timestamp createdDate;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

	@Column(name = "UPDATED_DT")
	private Timestamp updatedDate;
	
	@Column(name = "IS_DELETE")
	private String deleteFlag;

	@Column(name = "IS_ERROR")
	private String isError;
	
	@Column(name = "ERROR_CD")
	private String errorCode;

	@Column(name = "PENDING_TASK_ID")
	private String pendingTaskId;	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public AccountModel getSourceAccount() {
		return sourceAccount;
	}

	public void setSourceAccount(AccountModel sourceAccount) {
		this.sourceAccount = sourceAccount;
	}

	public Timestamp getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Timestamp uploadDate) {
		this.uploadDate = uploadDate;
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

	public String getTrxCurrencyCode() {
		return trxCurrencyCode;
	}

	public void setTrxCurrencyCode(String trxCurrencyCode) {
		this.trxCurrencyCode = trxCurrencyCode;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIsProcessed() {
		return isProcessed;
	}

	public void setIsProcessed(String isProcessed) {
		this.isProcessed = isProcessed;
	}
	
	public List<PendingUploadDetailModel> getDetails() {
		return details;
	}

	public void setDetails(List<PendingUploadDetailModel> details) {
		this.details = details;
	}
	
	public List<PendingUploadErrorModel> getErrors() {
		return errors;
	}

	public void setErrors(List<PendingUploadErrorModel> errors) {
		this.errors = errors;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
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
	
	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
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

	public String getPendingTaskId() {
		return pendingTaskId;
	}

	public void setPendingTaskId(String pendingTaskId) {
		this.pendingTaskId = pendingTaskId;
	}

	public String getCorporateAccountGroupDetail() {
		return corporateAccountGroupDetail;
	}

	public void setCorporateAccountGroupDetail(String corporateAccountGroupDetail) {
		this.corporateAccountGroupDetail = corporateAccountGroupDetail;
	}

}
