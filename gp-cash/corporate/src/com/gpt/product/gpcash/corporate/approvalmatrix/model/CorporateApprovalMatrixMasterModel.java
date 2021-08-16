package com.gpt.product.gpcash.corporate.approvalmatrix.model;

import java.io.Serializable;
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

import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;

@Entity
@Table(name = "CORP_APRV_MTRX_MSTR")
public class CorporateApprovalMatrixMasterModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	protected CorporateModel corporate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CCY_CD")
	protected CurrencyModel currency;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MENU_CD", nullable = false)
	protected IDMMenuModel menu;

	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;
	
	@OneToMany(mappedBy = "corporateApprovalMatrixMaster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	protected List<CorporateApprovalMatrixSubModel> corporateApprovalMatrixSub;

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

	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
	}

	public CurrencyModel getCurrency() {
		return currency;
	}

	public void setCurrency(CurrencyModel currency) {
		this.currency = currency;
	}

	public IDMMenuModel getMenu() {
		return menu;
	}

	public void setMenu(IDMMenuModel menu) {
		this.menu = menu;
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

	public List<CorporateApprovalMatrixSubModel> getCorporateApprovalMatrixSub() {
		return corporateApprovalMatrixSub;
	}

	public void setCorporateApprovalMatrixSub(List<CorporateApprovalMatrixSubModel> corporateApprovalMatrixSub) {
		this.corporateApprovalMatrixSub = corporateApprovalMatrixSub;
	}
	
	
	
	
}
