package com.gpt.product.gpcash.bankapprovalmatrix.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.component.idm.menu.model.IDMMenuModel;

@Entity
@Cacheable
@Table(name = "PRO_BANK_APRV_MTRX")
public class BankApprovalMatrixModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@Column(name = "IDX")
	protected int idx;
	
	@Column(name = "IS_DELETE")
	protected String deleteFlag;
	
	@Column(name = "NO_APRV")
	protected int noOfApprover;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD", nullable = false)
	protected IDMMenuModel menu;
	
	@OneToMany(mappedBy = "bankApprovalMatrix", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	protected List<BankApprovalMatrixDetailModel> bankApprovalMatrixDetail;
	
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

	public int getNoOfApprover() {
		return noOfApprover;
	}

	public void setNoOfApprover(int noOfApprover) {
		this.noOfApprover = noOfApprover;
	}

	public IDMMenuModel getMenu() {
		return menu;
	}

	public void setMenu(IDMMenuModel menu) {
		this.menu = menu;
	}

	@OrderBy("sequenceNo")
	public List<BankApprovalMatrixDetailModel> getBankApprovalMatrixDetail() {
		return bankApprovalMatrixDetail;
	}

	public void setBankApprovalMatrixDetail(List<BankApprovalMatrixDetailModel> bankApprovalMatrixDetail) {
		this.bankApprovalMatrixDetail = bankApprovalMatrixDetail;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	
}
