package com.gpt.product.gpcash.retail.transactionstatus;

public enum CustomerTransactionStatus {
	PENDING_EXECUTE,
	EXECUTE_SUCCESS,
	EXECUTE_FAIL,
	EXECUTE_PARTIAL_SUCCESS,
	REJECTED,
	CANCELLED,
	EXPIRED,
	IN_PROGRESS_OFFLINE,
	FAIL_ON_RECURRING_CREATION
}
