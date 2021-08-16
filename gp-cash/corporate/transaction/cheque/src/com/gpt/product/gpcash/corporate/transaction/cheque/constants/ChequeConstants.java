package com.gpt.product.gpcash.corporate.transaction.cheque.constants;

public interface ChequeConstants {
	String SRVC_GPT_CHQ_ORDER = "GPT_CHQ_ORDER";
	
	String CHQ_STS_NEW_REQUEST = "NEW REQUEST";
	
	String CHQ_STS_READY = "READY";
	
	String CHQ_STS_PICKED_UP = "PICKED UP";
	
	String CHQ_STS_DECLINED = "DECLINED";
	
	String CHQ_STS_EXPIRED = "EXPIRED";
	
	String WF_ACTION_PROCESS = "PROCESS";
	
	String WF_ACTION_CONFIRM_PROCESS = "CONFIRM_PROCESS";
	
	String WF_ACTION_SUBMIT_PROCESS = "SUBMIT_PROCESS";
	
	String WF_ACTION_SUBMIT_HANDOVER= "SUBMIT_HANDOVER";
	
	String WF_ACTION_SUBMIT_DECLINE = "SUBMIT_DECLINE";
	
	//Cheque Services
	
	String CHEQUE_ONLINE_INQUIRY = "DoChequeInquiry";
	
	String CHEQUE_ONLINE_INQUIRY_DETAIL = "DoChequeInquiryDetail";
}
