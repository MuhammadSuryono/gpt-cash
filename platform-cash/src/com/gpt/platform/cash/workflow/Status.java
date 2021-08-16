package com.gpt.platform.cash.workflow;

public enum Status {
	PENDING,
	APPROVED,
	REJECTED,
	DECLINED,
	CANCELED,
	RESUBMITTED, // from approver back to maker
	RELEASED,
	EXPIRED,
	CREATED
}
