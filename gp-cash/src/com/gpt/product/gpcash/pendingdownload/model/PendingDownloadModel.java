package com.gpt.product.gpcash.pendingdownload.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.utils.Helper;

@Entity
@Table(name = "PRO_PENDING_DOWNLOAD")
public class PendingDownloadModel  implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	private String id;
	
	@Column(name = "FILE_NM")
	private String fileName;
	
	@Column(name = "FULL_FILE_PATH")
	private String fullFilePath;
	
	@Column(name = "FILE_FORMAT")
	private String fileFormat;
	
	@Column(name = "MENU_CD")
	private String menuCode;
	
	@Column(name = "STATUS")
	private String status;
	
	@Column(name = "IS_READY_FOR_DOWNLOAD")
	private String isReadyForDownload;
	
	@Column(name="LAST_DOWNLOAED_BY")
	protected String lastDownloadedBy;
	
	@Column(name="LAST_DOWNLOADED_DT")
	protected Timestamp lastDownloadedDate;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(
		name = "PRO_PENDING_DOWNLOAD_VALUE", 
		joinColumns = @JoinColumn(name="PARENT_ID")
    )
	@OrderColumn(name = "IDX")
	@Column(name = "VALUES_", length = 1024)
	protected List<String> values;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;
	
	@Column(name = "CORP_ID")
	private String corporateId;
	
	@Column(name = "IS_DELETE")
	protected String deleteFlag;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public void setValues(String values) throws Exception {
		List<String> listOfRecord = Helper.chopItUp(values);

		if (ValueUtils.hasValue(listOfRecord)) {
			setValues(listOfRecord);
		}
	}
	
	public void setValues(List<String> values) {
		this.values = values;
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

	public String getIsReadyForDownload() {
		return isReadyForDownload;
	}

	public void setIsReadyForDownload(String isReadyForDownload) {
		this.isReadyForDownload = isReadyForDownload;
	}

	public String getLastDownloadedBy() {
		return lastDownloadedBy;
	}

	public void setLastDownloadedBy(String lastDownloadedBy) {
		this.lastDownloadedBy = lastDownloadedBy;
	}

	public Timestamp getLastDownloadedDate() {
		return lastDownloadedDate;
	}

	public void setLastDownloadedDate(Timestamp lastDownloadedDate) {
		this.lastDownloadedDate = lastDownloadedDate;
	}

	public String getMenuCode() {
		return menuCode;
	}

	public void setMenuCode(String menuCode) {
		this.menuCode = menuCode;
	}

	public String getFullFilePath() {
		return fullFilePath;
	}

	public void setFullFilePath(String fullFilePath) {
		this.fullFilePath = fullFilePath;
	}

	public String getCorporateId() {
		return corporateId;
	}

	public void setCorporateId(String corporateId) {
		this.corporateId = corporateId;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	
}
