package com.gpt.product.gpcash.bankapprovalmatrix.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;

@Entity
@Cacheable
@Table(name = "PRO_BANK_APRV_MTRX_DTL")
public class BankApprovalMatrixDetailModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@Column(name = "NO_USER")
	protected int noOfUser;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APRV_LVL_CD", nullable = false)
	protected ApprovalLevelModel approvalLevel;

	@Column(name = "SEQ_NO")
	protected int sequenceNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BANK_APRV_MTRX_ID", nullable = false)
	protected BankApprovalMatrixModel bankApprovalMatrix;
	
	@Column(name = "BRANCH_OPT")
	protected String branchOption;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

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

	public int getNoOfUser() {
		return noOfUser;
	}

	public void setNoOfUser(int noOfUser) {
		this.noOfUser = noOfUser;
	}

	public ApprovalLevelModel getApprovalLevel() {
		return approvalLevel;
	}

	public void setApprovalLevel(ApprovalLevelModel approvalLevel) {
		this.approvalLevel = approvalLevel;
	}

	public int getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(int sequenceNo) {
		this.sequenceNo = sequenceNo;
	}

	public BankApprovalMatrixModel getBankApprovalMatrix() {
		return bankApprovalMatrix;
	}

	public void setBankApprovalMatrix(BankApprovalMatrixModel bankApprovalMatrix) {
		this.bankApprovalMatrix = bankApprovalMatrix;
	}
	
	public String getBranchOption() {
		return branchOption;
	}

	public void setBranchOption(String branchOption) {
		this.branchOption = branchOption;
	}
	
}
