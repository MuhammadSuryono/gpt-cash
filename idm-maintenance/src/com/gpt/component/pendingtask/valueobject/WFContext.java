package com.gpt.component.pendingtask.valueobject;

public class WFContext {
	private String pendingTaskId;
	private String approvalLevel;
	private String branchOption;
	
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
	public String getBranchOption() {
		return branchOption;
	}
	public void setBranchOption(String branchOption) {
		this.branchOption = branchOption;
	}
	
}
