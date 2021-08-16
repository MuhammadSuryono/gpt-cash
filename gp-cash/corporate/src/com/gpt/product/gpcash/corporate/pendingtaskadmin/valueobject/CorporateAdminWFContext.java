package com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject;

public class CorporateAdminWFContext {
	private String pendingTaskId;
	private String approvalLevel;
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
	
}
