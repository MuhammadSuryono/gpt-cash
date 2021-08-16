package com.gpt.product.gpcash.corporate.mobile;

public enum Status {
	/**
	 * ready to be used
	 */
	ACTIVE,
	
	/**
	 * need provisioning
	 */
	INACTIVE,
	
	/**
	 * usage not allowed 
	 * for consistency and security reason, this status must be applied to all users on the same device
	 * so once the device is blocked, nobody can use that device to login
	 */
	BLOCKED, 
}
