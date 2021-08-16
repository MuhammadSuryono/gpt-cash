package com.gpt.product.gpcash.corporate.approvalmatrix.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "CORP_APRV_MTRX_SUB")
public class CorporateApprovalMatrixSubModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@Column(name = "LONG_AMT_LMT", precision=25, scale=7)
	protected BigDecimal longAmountLimit;
	
	@Column(name = "NO_APRV")
	protected int noApprover;
	
	@Column(name = "IS_ENABLE_COMBINE_LMT")
	protected String isEnableCombineLimit;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_APRV_MTRX_MSTR_ID", nullable = false)
	protected CorporateApprovalMatrixMasterModel corporateApprovalMatrixMaster;
	
	@OneToMany(mappedBy = "corporateApprovalMatrixSub", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	protected List<CorporateApprovalMatrixDetailModel> corporateApprovalMatrixDetail;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public BigDecimal getLongAmountLimit() {
		return longAmountLimit;
	}

	public void setLongAmountLimit(BigDecimal longAmountLimit) {
		this.longAmountLimit = longAmountLimit;
	}

	public String getIsEnableCombineLimit() {
		return isEnableCombineLimit;
	}

	public void setIsEnableCombineLimit(String isEnableCombineLimit) {
		this.isEnableCombineLimit = isEnableCombineLimit;
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

	public CorporateApprovalMatrixMasterModel getCorporateApprovalMatrixMaster() {
		return corporateApprovalMatrixMaster;
	}

	public void setCorporateApprovalMatrixMaster(CorporateApprovalMatrixMasterModel corporateApprovalMatrixMaster) {
		this.corporateApprovalMatrixMaster = corporateApprovalMatrixMaster;
	}

	public List<CorporateApprovalMatrixDetailModel> getCorporateApprovalMatrixDetail() {
		return corporateApprovalMatrixDetail;
	}

	public void setCorporateApprovalMatrixDetail(List<CorporateApprovalMatrixDetailModel> corporateApprovalMatrixDetail) {
		this.corporateApprovalMatrixDetail = corporateApprovalMatrixDetail;
	}

	public int getNoApprover() {
		return noApprover;
	}

	public void setNoApprover(int noApprover) {
		this.noApprover = noApprover;
	}
	
	
}
