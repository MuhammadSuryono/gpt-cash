package com.gpt.product.gpcash.corporate.authorizedlimitscheme.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

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

import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;

@Entity
@Table(name = "CORP_AUTH_LMT_SCHEME")
public class AuthorizedLimitSchemeModel implements Serializable {
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;

	@Column(name = "APRV_LVL_ALIAS")
	protected String approvalLevelAlias;
	
	@Column(name = "MAKER_LMT", nullable = false, precision=25, scale=7)
	protected BigDecimal makerLimit;
	
	@Column(name = "SINGLE_APRV_LMT", nullable = false, precision=25, scale=7)
	protected BigDecimal singleApprovalLimit;
	
	@Column(name = "INTRA_GRP_APRV_LMT", nullable = false, precision=25, scale=7)
	protected BigDecimal intraGroupApprovalLimit;
	
	@Column(name = "CROSS_GRP_APRV_LMT", nullable = false, precision=25, scale=7)
	protected BigDecimal crossGroupApprovalLimit;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID", nullable = false)
	protected CorporateModel corporate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APRV_LVL_CD", nullable = false)
	protected ApprovalLevelModel approvalLevel;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LMT_CCY_CD", nullable = false)
	protected CurrencyModel limitCurrency;

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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getApprovalLevelAlias() {
		return approvalLevelAlias;
	}

	public void setApprovalLevelAlias(String approvalLevelAlias) {
		this.approvalLevelAlias = approvalLevelAlias;
	}

	public BigDecimal getMakerLimit() {
		return makerLimit;
	}

	public void setMakerLimit(BigDecimal makerLimit) {
		this.makerLimit = makerLimit;
	}

	public BigDecimal getSingleApprovalLimit() {
		return singleApprovalLimit;
	}

	public void setSingleApprovalLimit(BigDecimal singleApprovalLimit) {
		this.singleApprovalLimit = singleApprovalLimit;
	}

	public BigDecimal getIntraGroupApprovalLimit() {
		return intraGroupApprovalLimit;
	}

	public void setIntraGroupApprovalLimit(BigDecimal intraGroupApprovalLimit) {
		this.intraGroupApprovalLimit = intraGroupApprovalLimit;
	}

	public BigDecimal getCrossGroupApprovalLimit() {
		return crossGroupApprovalLimit;
	}

	public void setCrossGroupApprovalLimit(BigDecimal crossGroupApprovalLimit) {
		this.crossGroupApprovalLimit = crossGroupApprovalLimit;
	}

	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
	}

	public ApprovalLevelModel getApprovalLevel() {
		return approvalLevel;
	}

	public void setApprovalLevel(ApprovalLevelModel approvalLevel) {
		this.approvalLevel = approvalLevel;
	}

	public CurrencyModel getLimitCurrency() {
		return limitCurrency;
	}

	public void setLimitCurrency(CurrencyModel limitCurrency) {
		this.limitCurrency = limitCurrency;
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
	
	
}
