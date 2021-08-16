package com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject;

import java.util.List;

public class CorporateUserWFContext {
	private String pendingTaskId;
	private String approvalLevel;
	private String stageName;
	private String userGroupOption;
	private String userGroup;
	private String approverUserCode;
	private List<String> approverHistory;
	
	public String getPendingTaskId() {
		return pendingTaskId;
	}
	public void setPendingTaskId(String pendingTaskId) {
		this.pendingTaskId = pendingTaskId;
	}
	public String getApprovalLevel() {
		return approvalLevel;
	}
	public void setApprovalLevel(String approvalLevel) {
		this.approvalLevel = approvalLevel;
	}
	public String getStageName() {
		return stageName;
	}
	public void setStageName(String stageName) {
		this.stageName = stageName;
	}
	public String getUserGroupOption() {
		return userGroupOption;
	}
	public void setUserGroupOption(String userGroupOption) {
		this.userGroupOption = userGroupOption;
	}
	public String getUserGroup() {
		return userGroup;
	}
	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}
	public String getApproverUserCode() {
		return approverUserCode;
	}
	public void setApproverUserCode(String approverUserCode) {
		this.approverUserCode = approverUserCode;
	}
	public List<String> getApproverHistory() {
		return approverHistory;
	}
	public void setApproverHistory(List<String> approverHistory) {
		this.approverHistory = approverHistory;
	}
}