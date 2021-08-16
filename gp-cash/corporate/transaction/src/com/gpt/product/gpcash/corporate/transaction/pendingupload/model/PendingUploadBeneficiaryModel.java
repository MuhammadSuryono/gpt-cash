package com.gpt.product.gpcash.corporate.transaction.pendingupload.model;

import java.io.Serializable;
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
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;

@Entity
@Table(name = "TRX_PENDING_UPLOAD_BEN")
public class PendingUploadBeneficiaryModel implements Serializable {

	@Id
	@Column(name = "ID")
	private String id;
	
	@Column(name = "FILE_NM")
	private String fileName;
	
	@Column(name = "FILE_FORMAT")
	private String fileFormat;

	@Column(name = "FILE_DSCP")
	private String fileDescription;
	
	@Column(name = "UPLOAD_DATE")
	private Timestamp uploadDate;

	@Column(name = "TOTAL_RECORD")
	private int totalRecord;
	
	@Column(name = "TOTAL_ERROR")
	private int totalError;
	
	@Column(name = "BENEFICIARY_TYPE")
	private String beneficiaryUploadType;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	private CorporateModel corporate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_USR_GRP_ID")
	private CorporateUserGroupModel corporateUserGroup;	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD", nullable = false)
	private IDMMenuModel menu;
	
	@Column(name = "STATUS")
	private String status;

	@Column(name = "IS_PROCESSED")
	private String isProcessed;	
	
	@OrderColumn(name = "IDX")
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PendingUploadBeneficiaryDetailModel> details;
	
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PendingUploadBeneficiaryErrorModel> errors;	
	
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
	
	public List<PendingUploadBeneficiaryDetailModel> getDetails() {
		return details;
	}

	public void setDetails(List<PendingUploadBeneficiaryDetailModel> details) {
		this.details = details;
	}
	
	public List<PendingUploadBeneficiaryErrorModel> getErrors() {
		return errors;
	}

	public void setErrors(List<PendingUploadBeneficiaryErrorModel> errors) {
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

	public String getBeneficiaryUploadType() {
		return beneficiaryUploadType;
	}

	public void setBeneficiaryUploadType(String beneficiaryUploadType) {
		this.beneficiaryUploadType = beneficiaryUploadType;
	}
}
