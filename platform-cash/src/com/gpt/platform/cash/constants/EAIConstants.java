package com.gpt.platform.cash.constants;

public interface EAIConstants {
	String ERROR_TOKEN_BLOCKED = "EAI-EAI-05";
	
	String CIF_INQUIRY = "DoCIFInquiry";
	
	String ACCOUNT_INQUIRY = "DoAccountInquiry";
	
	String RETRIEVE_ACCOUNTS_BY_CIF = "DoRetrieveAccountsByCIF";
	
	String BALANCE_SUMMARY = "DoBalanceSummary";
	
	String LATEST_TRANSACTION = "DoLatestTransaction";
	
	String TRANSACTION_HISTORY = "DoTransactionHistory";

	String TRANSACTION_HISTORY_DOWNLOAD = "DoTransactionHistoryDownload";
	
	String TRANSACTION_HISTORY_DOWNLOAD_MT = "DoTransactionHistoryDownloadMT";
	
	String TRANSACTION_HISTORY_DOWNLOAD_PDF_MULTI_ACCOUNT = "DoTransactionHistoryDownloadPDFMultiAccount";

	String DOMESTIC_ONLINE_ACCOUNT_INQUIRY = "DoAccountInquiryDomOnline";
	
	String INHOUSE_TRANSFER = "DoInHouseTransfer";
	
	String INHOUSE_TRANSFER_SIPD = "DoInHouseTransferSIPD";
	
	String DOMESTIC_TRANSFER = "DoDomesticTransfer";
	
	String BILL_PAYMENT = "DoBPPayment";
	
	String BILL_PAYMENT_INQUIRY = "DoBPInquiry";
	
	String LIQUIDITY_TRANSFER = "DoLiquidityTransfer";
	
	String TRANSFER_VA_INQUIRY = "DoTransferVAInquiry";
	
	
	// NOTIFICATION SERVICES	
	String BENEFICIARY_TRANSFER_NOTIFICATION = "BeneficiaryTransferNotification";
	
	String USER_TRANSFER_NOTIFICATION = "UserTransferNotification";
	
	String USER_BULK_TRANSFER_NOTIFICATION = "UserBulkTransferNotification";
	
	String USER_LIQUIDITY_NOTIFICATION = "UserLiquidityTransferNotification";
	
	String USER_CHEQUE_ORDER_NOTIFICATION = "UserChequeOrderNotification";
	
	String USER_DIRECT_DEBIT_TRANSFER_NOTIFICATION = "UserDirectDebitTransferNotification";

	String USER_BP_NOTIFICATION = "UserBillPaymentNotification";

	String TASK_NOTIFICATION = "TaskNotification";
	
	String USER_PASSWORD_NOTIFICATION = "UserPasswordNotification";
	
	String MONITORING_EMAIL_NOTIFICATION = "MonitoringEmailNotification";
	
	String BENEFICIARY_BP_NOTIFICATION = "BeneficiaryBillPaymentNotification";
	
	// END OF NOTIFICATION SERVICES

	
	// TOKEN SERVICES
	String CHECK_TOKEN = "DoCheckToken";
		
	String GENERATE_TOKEN_CHALLENGE_N0 = "DoGenerateTokenChallengeNo";
	
	String TOKEN_AUTHENTICATION = "DoTokenAuthentication";	
	
	String UNBLOCK_TOKEN = "DoUnblockToken";
	
	String UNLOCK_TOKEN = "DoUnlockToken";
	
	String UNASSIGN_TOKEN = "DoUnassignToken";
	
	String REGISTER_CORPORATE_TOKEN = "DoRegisterCorporateToken";
	
	String RESERVE_TOKEN = "DoReserveToken";
	
	String ASSIGN_TOKEN = "DoAssignToken";
	
	//retail token service
	String UNBLOCK_TOKEN_RETAIL = "DoUnblockTokenRetail";
	
	String UNLOCK_TOKEN_RETAIL = "DoUnlockTokenRetail";
	
	String UNASSIGN_TOKEN_RETAIL = "DoUnassignTokenRetail";
	
	String REGISTER_RETAIL_TOKEN = "DoRegisterCustomerToken";
	
	String ASSIGN_TOKEN_RETAIL = "DoAssignTokenRetail";
	
	// END OF TOKEN SERVICES
	
	// PAYROLL SERVICES
	String PARSE_CUSTOMER_PAYROLL = "DoParseCustomerPayroll";

	String PAYROLL_TRANSFER = "DoPayrollTransfer";
	
	String PAYROLL_RESPONSE = "DoPayrollResponse";
		
	String PAYROLL_RESPONSE_VA = "DoPayrollResponseVA";
	
	String PAYROLL_UPDATE_HEADER_STATUS= "DoPayrollUpdateHeaderStatus";
	
	// BULK SERVICES
		String PARSE_CUSTOMER_BULK = "DoParseCustomerBulkPayment";

		String BULK_TRANSFER = "DoBulkPaymentTransfer";
		
		String BULK_RESPONSE = "DoBulkPaymentResponse";
		
	//SOT
	String SOT_REQUEST = "DoSOTRequest";
	
	String SOT_RESPONSE = "DoSOTResponse";

	String ACCOUNT_SYNC_REQUEST = "DoAccountSyncRequest";
	
	String ACCOUNT_SYNC_RESPONSE = "DoAccountSyncResponse";
	
	//Transaction Log
	String RETRIEVE_TRANSACTION = "DoRetrieveTransactionLog";
	
	String SIPKD_INQUIRY = "DoSIPKDInquiry";
	
	String SIPKD_POST_BELANAJA = "DoSIPKDPostBelanja";
	
	//Pemda SP2D
	String SP2D_INQUIRY = "DoSP2DInquiry";

    String SUSPECT_TRANCATION_NOTIFICATION = "SuspectTransactionNotification";
    
    //VA
    String VA_TRANSACTION_HISTORY = "DoVATransactionHistory";
    
    String VA_TRANSACTION_HISTORY_DOWNLOAD = "DoVATransactionHistoryDownload";
	
	String VA_TRANSACTION_HISTORY_DOWNLOAD_MT = "DoVATransactionHistoryDownloadMT";
	
	String VA_TRANSACTION_HISTORY_DOWNLOAD_PDF_MULTI_ACCOUNT = "DoVATransactionHistoryDownloadPDFMultiAccount";
	
	String VA_SOT_REQUEST = "DoVASOTRequest";
	
	String VA_SOT_RESPONSE = "DoVASOTResponse";

    String VA_PAYMENT_INQUIRY = "DoVAInquiry";
    
    String VA_PAYMENT = "DoVAPayment";

  // Beneficairy Upload
 	String PARSE_BENEFICIARY_UPLOAD = "DoParseUploadBeneficiary";

 	// Time Deposit Placement
 	String TIME_DEPOSIT_PLACEMENT = "DoTimeDepositPlacement";

 	
}
