package com.gpt.component.idm.user.model;

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

import com.gpt.component.maintenance.branch.model.BranchModel;

@Entity
@Table(name = "IDM_USER")
public class IDMUserModel implements Serializable {
	@Id
	@Column(name = "CD")
	protected String code;

	@Version
	protected int version;

	@Column(name = "NM")
	protected String name;
	
	@Column(name = "EMAIL")
	protected String email;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_CD")
	protected BranchModel branch;
	
	@Column(name = "IS_DELETE")
	protected String deleteFlag;

	@Column(name = "ACTIVE_FR")
	protected Timestamp activeFrom;
	
	@Column(name = "ACTIVE_TO")
	protected Timestamp activeTo;
	
	@Column(name = "PASSWORD")
	protected String passwd;
	
	@Column(name = "STATUS")
	protected String status;
	
	@Column(name = "LOGIN_COUNT")
	protected int loginCount;
	
	@Column(name = "IS_PWD_NEVER_EXPIRE")
	protected String isPwdNeverExpired;
	
	@Column(name = "IS_STILL_LOGIN")
	protected String stillLoginFlag;
	
	@Column(name="CREATED_BY")
	protected String createdBy;
	
	@Column(name="CREATED_DT")
	protected Timestamp createdDate;
	
	@Column(name="UPDATED_BY")
	protected String updatedBy;
	
	@Column(name="UPDATED_DT")
	protected Timestamp updatedDate;
	
	@Column(name="LAST_LOGIN_DT")
	protected Timestamp lastLoginDate;
	
	@Column(name="LAST_CHANGE_PASSWD_DT")
	protected Timestamp lastChangePasswordDate;
	
	@Column(name="LAST_LOCKED_FROM_IDLE_DT")
	protected Timestamp lastLockedFromIdleDate;
	
	@Column(name = "USER_ID")
	protected String userId;
	
	@Column(name = "PROFILE_IMG_URL")
	protected String profileImgUrl;
	
	@Column(name = "PROFILE_IMG_FILE_NAME")
	protected String profileImgFileName;
	
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public BranchModel getBranch() {
		return branch;
	}

	public void setBranch(BranchModel branch) {
		this.branch = branch;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public Timestamp getActiveFrom() {
		return activeFrom;
	}

	public void setActiveFrom(Timestamp activeFrom) {
		this.activeFrom = activeFrom;
	}

	public Timestamp getActiveTo() {
		return activeTo;
	}

	public void setActiveTo(Timestamp activeTo) {
		this.activeTo = activeTo;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(int loginCount) {
		this.loginCount = loginCount;
	}

	public String getIsPwdNeverExpired() {
		return isPwdNeverExpired;
	}

	public void setIsPwdNeverExpired(String isPwdNeverExpired) {
		this.isPwdNeverExpired = isPwdNeverExpired;
	}

	public String getStillLoginFlag() {
		return stillLoginFlag;
	}

	public void setStillLoginFlag(String stillLoginFlag) {
		this.stillLoginFlag = stillLoginFlag;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Timestamp getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Timestamp lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public Timestamp getLastChangePasswordDate() {
		return lastChangePasswordDate;
	}

	public void setLastChangePasswordDate(Timestamp lastChangePasswordDate) {
		this.lastChangePasswordDate = lastChangePasswordDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getProfileImgUrl() {
		return profileImgUrl;
	}

	public void setProfileImgUrl(String profileImgUrl) {
		this.profileImgUrl = profileImgUrl;
	}

	public String getProfileImgFileName() {
		return profileImgFileName;
	}

	public void setProfileImgFileName(String profileImgFileName) {
		this.profileImgFileName = profileImgFileName;
	}

	public Timestamp getLastLockedFromIdleDate() {
		return lastLockedFromIdleDate;
	}

	public void setLastLockedFromIdleDate(Timestamp lastLockedFromIdleDate) {
		this.lastLockedFromIdleDate = lastLockedFromIdleDate;
	}
}
