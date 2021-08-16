package com.gpt.product.gpcash.corporate.corporateusergroup.model;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupModel;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;

@Entity
@Cacheable
@Table(name = "CORP_USR_GRP")
public class CorporateUserGroupModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Column(name = "CD")
	protected String code;

	@Version
	protected int version;

	@Column(name = "NM")
	protected String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROLE_CD", nullable = false)
	protected IDMRoleModel role;
	
	@Column(name = "IDX")
	protected int idx;
	
	@Column(name = "APRV_LMT_AMT", precision=25, scale=7)
	protected BigDecimal approvalLimit = BigDecimal.ZERO;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APRV_LMT_CCY_CD")
	protected CurrencyModel approvalLimitCurrency;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	protected CorporateModel corporate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ACCT_GRP_ID")
	protected CorporateAccountGroupModel corporateAccountGroup;
	
	@Column(name="BEN_SCOPE")
	protected String benScope;
	
	@OneToMany(mappedBy = "corporateUserGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	protected List<CorporateUserGroupDetailModel> corporateUserGroupDetail;
	
	@Column(name = "IS_DELETE")
	protected String deleteFlag;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IDMRoleModel getRole() {
		return role;
	}

	public void setRole(IDMRoleModel role) {
		this.role = role;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public BigDecimal getApprovalLimit() {
		return approvalLimit;
	}

	public void setApprovalLimit(BigDecimal approvalLimit) {
		this.approvalLimit = approvalLimit;
	}

	public CurrencyModel getApprovalLimitCurrency() {
		return approvalLimitCurrency;
	}

	public void setApprovalLimitCurrency(CurrencyModel approvalLimitCurrency) {
		this.approvalLimitCurrency = approvalLimitCurrency;
	}

	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
	}

	public CorporateAccountGroupModel getCorporateAccountGroup() {
		return corporateAccountGroup;
	}

	public void setCorporateAccountGroup(CorporateAccountGroupModel corporateAccountGroup) {
		this.corporateAccountGroup = corporateAccountGroup;
	}

	public String getBenScope() {
		return benScope;
	}

	public void setBenScope(String benScope) {
		this.benScope = benScope;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
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

	public List<CorporateUserGroupDetailModel> getCorporateUserGroupDetail() {
		return corporateUserGroupDetail;
	}

	public void setCorporateUserGroupDetail(List<CorporateUserGroupDetailModel> corporateUserGroupDetail) {
		this.corporateUserGroupDetail = corporateUserGroupDetail;
	}
	
}
