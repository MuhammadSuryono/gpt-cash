package com.gpt.product.gpcash.corporate.corporateuser.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.product.gpcash.approvalmap.model.ApprovalMapModel;

@Entity
@Table(name = "CORP_USR")
public class CorporateUserModel implements Serializable{
	@Id
	@Column(name = "ID")
	protected String id;
	
	@Version
	protected int version;
	
	@Column(name="USER_ID")
	protected String userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDM_USER_CD", nullable = false)
	protected IDMUserModel user;
	
	@Column(name = "IS_GRANT_VIEW_DETAIL")
	protected String isGrantViewDetail;
	
	@Column(name = "IS_NOTIFY_MY_TASK")
	protected String isNotifyMyTask;
	
	@Column(name = "IS_NOTIFY_MY_TRX")
	protected String isNotifyMyTrx;
	
	@Column(name = "MB_PHONE_NO")
	protected String mobilePhoneNo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_ID")
	protected CorporateModel corporate;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORP_USR_GRP_ID", nullable = false)
	protected CorporateUserGroupModel corporateUserGroup;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APRV_MAP_ID", nullable = false)
	protected ApprovalMapModel approvalMap;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AUTH_LMT_SCHEME_ID")
	protected AuthorizedLimitSchemeModel authorizedLimit;
	
	@Column(name = "IS_APP_RLS")
	protected String isApproverReleaser;
	
	@Column(name = "IS_ONE_SIGNER")
	protected String isOneSigner;
	
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

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public IDMUserModel getUser() {
		return user;
	}

	public void setUser(IDMUserModel user) {
		this.user = user;
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

	public CorporateModel getCorporate() {
		return corporate;
	}

	public void setCorporate(CorporateModel corporate) {
		this.corporate = corporate;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getIsGrantViewDetail() {
		return isGrantViewDetail;
	}

	public void setIsGrantViewDetail(String isGrantViewDetail) {
		this.isGrantViewDetail = isGrantViewDetail;
	}

	public String getMobilePhoneNo() {
		return mobilePhoneNo;
	}

	public void setMobilePhoneNo(String mobilePhoneNo) {
		this.mobilePhoneNo = mobilePhoneNo;
	}

	public CorporateUserGroupModel getCorporateUserGroup() {
		return corporateUserGroup;
	}

	public void setCorporateUserGroup(CorporateUserGroupModel corporateUserGroup) {
		this.corporateUserGroup = corporateUserGroup;
	}

	public ApprovalMapModel getApprovalMap() {
		return approvalMap;
	}

	public void setApprovalMap(ApprovalMapModel approvalMap) {
		this.approvalMap = approvalMap;
	}

	public String getIsNotifyMyTask() {
		return isNotifyMyTask;
	}

	public void setIsNotifyMyTask(String isNotifyMyTask) {
		this.isNotifyMyTask = isNotifyMyTask;
	}

	public String getIsNotifyMyTrx() {
		return isNotifyMyTrx;
	}

	public void setIsNotifyMyTrx(String isNotifyMyTrx) {
		this.isNotifyMyTrx = isNotifyMyTrx;
	}

	public AuthorizedLimitSchemeModel getAuthorizedLimit() {
		return authorizedLimit;
	}

	public void setAuthorizedLimit(AuthorizedLimitSchemeModel authorizedLimit) {
		this.authorizedLimit = authorizedLimit;
	}

	public String getIsApproverReleaser() {
		return isApproverReleaser;
	}

	public void setIsApproverReleaser(String isApproverReleaser) {
		this.isApproverReleaser = isApproverReleaser;
	}

	public String getIsOneSigner() {
		return isOneSigner;
	}

	public void setIsOneSigner(String isOneSigner) {
		this.isOneSigner = isOneSigner;
	}

	
}
