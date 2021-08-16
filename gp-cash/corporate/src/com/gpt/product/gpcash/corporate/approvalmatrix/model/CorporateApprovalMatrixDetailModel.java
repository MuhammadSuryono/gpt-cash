package com.gpt.product.gpcash.corporate.approvalmatrix.model;

import java.io.Serializable;
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

import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;

@Entity
@Table(name = "CORP_APRV_MTRX_DTL")
public class CorporateApprovalMatrixDetailModel implements Serializable{
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@Column(name = "NO_USR")
	protected int noUser;
	
	@Column(name = "SEQ_NO")
	protected int sequenceNo;
	
	@Column(name = "USR_GRP_OPT")
	protected String userGroupOption;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_APRV_MTRX_SUB_ID", nullable = false)
	protected CorporateApprovalMatrixSubModel corporateApprovalMatrixSub;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AUTH_LMT_SCHEME_ID")
	protected AuthorizedLimitSchemeModel authorizedLimitScheme;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_USR_GRP_ID")
	protected CorporateUserGroupModel corporateUserGroup;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APRV_LVL_CD")
	protected ApprovalLevelModel approvalLevel;
	
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

	public int getNoUser() {
		return noUser;
	}

	public void setNoUser(int noUser) {
		this.noUser = noUser;
	}

	public int getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(int sequenceNo) {
		this.sequenceNo = sequenceNo;
	}

	public String getUserGroupOption() {
		return userGroupOption;
	}

	public void setUserGroupOption(String userGroupOption) {
		this.userGroupOption = userGroupOption;
	}

	public CorporateApprovalMatrixSubModel getCorporateApprovalMatrixSub() {
		return corporateApprovalMatrixSub;
	}

	public void setCorporateApprovalMatrixSub(CorporateApprovalMatrixSubModel corporateApprovalMatrixSub) {
		this.corporateApprovalMatrixSub = corporateApprovalMatrixSub;
	}

	public AuthorizedLimitSchemeModel getAuthorizedLimitScheme() {
		return authorizedLimitScheme;
	}

	public void setAuthorizedLimitScheme(AuthorizedLimitSchemeModel authorizedLimitScheme) {
		this.authorizedLimitScheme = authorizedLimitScheme;
	}

	public CorporateUserGroupModel getCorporateUserGroup() {
		return corporateUserGroup;
	}

	public void setCorporateUserGroup(CorporateUserGroupModel corporateUserGroup) {
		this.corporateUserGroup = corporateUserGroup;
	}

	public ApprovalLevelModel getApprovalLevel() {
		return approvalLevel;
	}

	public void setApprovalLevel(ApprovalLevelModel approvalLevel) {
		this.approvalLevel = approvalLevel;
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
