package com.gpt.platform.cash.constants;

import java.math.BigDecimal;

import com.gpt.component.common.Constants;
import com.gpt.platform.cash.workflow.Status;

public interface ApplicationConstants extends Constants {

	String APP_GPCASHIB_R = "GPCASHIB_R";
	
	String APP_GPCASHIB = "GPCASHIB";

	String APP_GPCASHBO = "GPCASHBO";

	String APP_GPCASHIB_ADMIN = "GPCASHIB_ADMIN";

	String APP_CODE = "appCode";

	String SERVICE_TYPE_TRX = "TRX";

	String SERVICE_TYPE_PRD = "PRD";

	int TIMEOUT = 70000;

	int IDX = 999;

	String YES = "Y";

	String NO = "N";

	String STR_CODE = "code";

	String STR_NAME = "name";

	String STR_MENUCODE = "menuCode";

	String STR_CREATEDBY = "createdBy";

	String STR_CREATEDDATE = "createdDt";

	String STR_UPDATEDBY = "updatedBy";

	String STR_UPDATEDDATE = "updatedDt";

	String LOGIN_USERID = "loginId";

	String LOGIN_USERNAME = "loginUsername";

	String LOGIN_CORP_ID = "loginCorporateId";
	
	String LOGIN_USERCODE = "loginUserCode";

	String LOGIN_TOKEN_NO = "loginTokenNo";
	
	String LOGIN_HISTORY_ID = "loginHistoryId";
	
	String LOGIN_MENULIST = "loginMenuList";
	
	String LOGIN_IPADDRESS = "loginIPAddress";
	
	String LOGIN_HANDLES_LOGOUT = "loginHandlesLogout";

	String LOGIN_DATE = "loginDate";
	
	String JSONOBJECT = "JSONOBJECT";

	String WF_ACTION = "action";

	String WF_ACTION_SEARCH = "SEARCH";
	
	String WF_ACTION_CREATE_BUCKET = "CREATE_BUCKET";

	String WF_ACTION_CREATE = "CREATE";
	
	String WF_ACTION_CANCEL = "CANCEL";
	
	String WF_ACTION_CONFIRM = "CONFIRM";
	
	String WF_ACTION_VALIDATE = "VALIDATE";

	String WF_ACTION_UPDATE = "UPDATE";
	
	String WF_ACTION_UPDATE_STATUS = "UPDATE_STATUS";

	String WF_ACTION_DELETE = "DELETE";
	
	String WF_ACTION_DELETE_LIST = "DELETE_LIST";

	String WF_ACTION_DETAIL = "DETAIL";

	String WF_ACTION_APPROVE = "APPROVE";

	String WF_ACTION_REJECT = "REJECT";

	String WF_ACTION_UNLOCK = "UNLOCK";
	
	String WF_ACTION_EXECUTE = "EXECUTE";
	
	String WF_ACTION_LOCK = "LOCK";
	
	String WF_ACTION_RESET = "RESET";
	
	String WF_ACTION_ACTIVATE = "ACTIVATE";
	
	String WF_ACTION_INACTIVATE = "INACTIVATE";

	String WF_ACTION_DOWNLOAD = "DOWNLOAD";
	
	String WF_ACTION_REQUEST_DOWNLOAD = "REQUEST_DOWNLOAD";

	String CREATED_BY_SYSTEM = "SYSTEM";

	String WF_STATUS_PENDING = Status.PENDING.name();

	String WF_STATUS_APPROVED = Status.APPROVED.name();
	
	String WF_STATUS_REJECTED = Status.REJECTED.name();
	
	String WF_STATUS_EXPIRED = Status.EXPIRED.name();
	
	//status wf release hanya di pakai untuk yg batch untuk membedakan dengan wf status approved
	String WF_STATUS_RELEASED = Status.RELEASED.name();
	
	String WF_FIELD_PENDING_TASK_ID = "pendingTaskId";

	String WF_FIELD_REFERENCE_NO = "referenceNo";

	String WF_FIELD_MESSAGE = "message";
	
	String WF_FIELD_DATE_TIME_INFO = "dateTimeInfo";

	String KEY_WF_TASK_INSTANCE_ID = "taskInstanceId";

	String KEY_WF_APPROVAL_LV_CODE = "approvalLvCode";

	String KEY_WF_APPROVAL_REQUIRED_COUNT = "approvalRequiredCount";

	String KEY_WF_APPROVAL_USER_COUNT = "approvalUserCount";

	String CONTROLLER_METHOD_SEARCH = "search";

	String CONTROLLER_METHOD_SUBMIT = "submit";

	String DELIMITER_PIPE = "|";
	
	String DELIMITER_DASH = " - ";
	
	String DELIMITER_COMMA = ",";
	
	String DASH = "-";

	String ROLE_TYPE_AP = "AP";

	String ROLE_TYPE_WF = "WF";

	String IDM_USER_STATUS = "status";
	
	String IDM_USER_STATUS_ACTIVE = "ACTIVE";

	String IDM_USER_STATUS_LOCKED = "LOCKED";
	
	String IDM_USER_STATUS_RESET = "RESET";
	
	String IDM_USER_STATUS_INACTIVE = "INACTIVE";
	
	String WILDCARD = "%";

	String PAGE_SIZE = "20";

	String CHARGE_VAL_TYPE_FIX = "FIX_VALUE";

	String CORP_ID = "corporateId";
	
	String CUST_ID = "customerId";
	
	String USER_ID = "userId";
	
	String CIF_ID = "cifId";

	String WF_ROLE_BNK_USR_CATEGORY = "BNK_USR";

	String WF_ROLE_APPROVER = "%_AP%";
	
	String WF_ROLE_RELEASER = "%_RL%";

	String KEY_BEAN_ID = "KEY_BEAN_ID";

	String KEY_PT_ID = "KEY_PT_ID";

	String KEY_MENU_CODE = "KEY_MENU_CODE";

	String KEY_TRX_AMOUNT = "KEY_TRX_AMOUNT";

	String KEY_TRX_AMOUNT_CCY_CD = "KEY_TRX_AMOUNT_CCY_CD";
	
	String KEY_CORP_ID = "KEY_CORP_ID";
	
	String KEY_CUST_ID = "KEY_CUST_ID";
	
	String ROLE_AP_ADMIN = "CRPADM";

	String ROLE_WF_ADMIN_MAKER = "CRP_ADM_MAKER";

	String ROLE_WF_ADMIN_CHECKER = "CRP_ADM_CHECKER";
	
	String ROLE_WF_CRP_USR_CATEGORY = "CRP_USR";
	 
	String ROLE_WF_CRP_USR_MAKER = "CRP_USR_MK";
	
	String ROLE_WF_CRP_USR_RELEASER = "CRP_USR_RL";
	
	String ROLE_WF_CRP_USR_APPROVER = "CRP_USR_AP";
	
	String ROLE_WF_CRP_USR_MAKER_RELEASER = "CRP_USR_MK_RL";
	
	String ROLE_WF_CRP_USR_MAKER_APPROVER = "CRP_USR_MK_AP";
	
	String ROLE_WF_CRP_USR_APPROVER_RELEASER = "CRP_USR_AP_RL";

	String BEN_SCOPE_CORPORATE = "CORPORATE";

	String BEN_SCOPE_USER_GROUP = "USER_GROUP";

	String TOKEN_STATUS_INACTIVE = "Inactive";

	String TOKEN_STATUS_ACTIVE = "Active";

	String TOKEN_STATUS_BLOCKED = "Blocked";

	String TOKEN_STATUS_LOCKED = "Locked";

	String TOKEN_TYPE_HARD_TOKEN = "TKN";

	String TOKEN_TYPE_MOBILE_TOKEN = "MT";

	String BROADCAST_LOCALIZATION = "gpcash.localization";

	String USER_GROUP_OPTION_ANY_GROUP = "ANY";

	String USER_GROUP_OPTION_INTRA_GROUP = "INTRA";

	String USER_GROUP_OPTION_CROSS_GROUP = "CROSS";

	String USER_GROUP_OPTION_SPECIFY_GROUP = "SPECIFY";

	String ACCOUNT_TYPE_SAVING = "001";

	String ACCOUNT_TYPE_CURRENT = "002";

	String ACCOUNT_TYPE_TIME_DEPOSIT = "003";

	String ACCOUNT_TYPE_LOAN = "004";

	String APPROVAL_MATRIX_NON_TRANSACTION = "-1";
	
	String ACCOUNT_GRP_DTL_ID = "accountGroupDtlId";
	
	String ACCOUNT_DTL_ID = "accountDtlId";

	String MENU_TYPE_TRANSACTION = "T";
	
	String SRVC_GPT_FTR_BILL_PAY = "GPT_FTR_BILL_PAY";
	
	String SRVC_GPT_FTR_PURCHASE = "GPT_FTR_PURCHASE";
	
	String SRVC_GPT_FTR_DOM_LLG = "GPT_FTR_DOM_LLG";
	
	String SRVC_GPT_FTR_DOM_ONLINE = "GPT_FTR_DOM_ONLINE";
	
	String SRVC_GPT_FTR_DOM_RTGS = "GPT_FTR_DOM_RTGS";
	
	String SRVC_GPT_FTR_IH_3RD = "GPT_FTR_IH_3RD";
	
	String SRVC_GPT_FTR_IH_OWN = "GPT_FTR_IH_OWN";
	
	String SRVC_GPT_INQ_BI = "GPT_INQ_BI";
	
	String SRVC_GPT_ST_ESTATEMENT = "GPT_ST_ESTATEMENT";
	
	String TRANS_BEN_ID = "benId";
	
	String TRANS_BEN_ACCT = "benAccountNo";
	
	String TRANS_BEN_ACCT_NAME = "benAccountName";
	
	String TRANS_BEN_ACCT_CURRENCY = "benAccountCurrency";
	
	String TRANS_AMOUNT = "transactionAmount";
	
	String TRANS_AMOUNT_EQ = "transactionAmountEquivalent";
	
	String TRANS_TOTAL_DEBIT_AMOUNT = "totalDebitedAmount";
	
	String TRANS_TOTAL_CHARGE = "totalCharge";
	
	String TRANS_CHARGE_LIST = "chargeList";
	
	String TRANS_TOTAL_CHARGE_CURRENCY = "totalChargeCurrency";
	
	String TRANS_CURRENCY = "transactionCurrency";
	
	String TRANS_SERVICE_CODE = "transactionServiceCode";
	
	String INSTRUCTION_MODE = "instructionMode";

	String INSTRUCTION_DATE = "instructionDate";
	
	String SESSION_TIME = "sessionTime";

	String CCY_MTRX_LL = "LL";
	
	String CCY_MTRX_LF = "LF";
	
	String CCY_MTRX_FL = "FL";
	
	String CCY_MTRX_FS = "FS";
	
	String CCY_MTRX_FC = "FC";
	
	String SI_IMMEDIATE = "I";
	
	String SI_FUTURE_DATE = "F";
	
	String SI_RECURRING = "R";
	
	String SI_RECURRING_TYPE_WEEKLY = "W";

	String SI_RECURRING_TYPE_MONTHLY = "M";
	
	String SI_RECURRING_TYPE_DAILY = "D";
	
	String SI_RECURRING_TYPE_ANNUALLY = "A";

	String IMMEDIATE_TYPE_CREATE = "C";

	String IMMEDIATE_TYPE_RELEASE = "R";
	
	String REMIITER_CHARGES_INSTRUCTION = "OUR";
	
	String SHARE_CHARGES_INSTRUCTION = "SHA";
	
	String BENEFICIARY_CHARGES_INSTRUCTION = "BEN";
	
	String INSTITUTION_AMOUNT_TYPE_USER_SELECTION = "User Selection"; //for voucher use
	
	String INSTITUTION_TYPE_FIX = "FIX"; // amount get from eai or entry screen
	
	String INSTITUTION_TYPE_ANY = "ANY"; // amount get from confirmation screen by user input, amount type only can user input
	
	String INSTITUTION_AMOUNT_TYPE_USER_INPUT = "User Input";  //user key in amount in entry screen
	
	String INSTITUTION_AMOUNT_TYPE_FIX_FULL_ALL = "Full Amount All Bills"; //user pay all bills in confirmation screen
	
	String INSTITUTION_AMOUNT_TYPE_FIX_FULL_PER = "Full Amount Selected Bills"; //user select selected bills in confirmation screen
	
	String MULTI_BILLS_KEY = "bills";
	
	// Workflow Stage Name
	String WF_STAGE_MAKER = "MakerStage";
	
	String WF_STAGE_APPROVE = "ApproverStage";
	
	String WF_STAGE_RELEASE = "ReleaserStage";
	
	String WF_STAGE_CHECKER = "CheckerStage";
	
	String PENDING_UPLOAD_STATUS_IN_PROGRESS = "INPROGRESS";
	
	String PENDING_UPLOAD_STATUS_COMPLETED = "COMPLETED";
	
	String PENDING_UPLOAD_STATUS_FAILED = "FAILED";
	
	//login url
	String BANK_LOGIN_METHOD = "login";
	
	String CORP_LOGIN_METHOD = "corporateLogin";
	
	String CORP_LOGIN_OS_METHOD = "corporateLoginOutsource";
	
	String CUST_LOGIN_METHOD = "customerLogin";
	
	//charset for password encryption
	String CHARSET = "UTF-8";
	
	// FILE_FORMAT
	String FILE_FORMAT_CSV = "CSV";
	
	String FILE_FORMAT_TXT = "TXT";
	
	String FILE_FORMAT_MT_940 = "MT940";
	
	String FILE_FORMAT_MT_942 = "MT942";
	
	String FILE_FORMAT_PDF = "PDF";
	
	String FILE_FORMAT_EXCEL = "EXCEL";
	
	String SOT_TYPE_NEW_CORPORATE_ACCOUNT = "NEW_CORPORATE_ACCOUNT";
	
	String SOT_TYPE_DAILY_TRX = "DAILY_TRX";
	
	//ACCOUNT SYNC STATUS
	String ACCOUNT_SYNC_INACTIVE = "INACTIVE";
	
	String ACCOUNT_SYNC_ACTIVE = "ACTIVE";
	
	String CHALLENGE_NO = "challengeNo";
	
	String RESPONSE_NO = "responseNo";
	
	String CONFIRMATION_DATA = "confirmationData";
	
	BigDecimal ZERO = new BigDecimal(0);
	
	//sub menu type
	String SINGLE_TRANSACTION = "SINGLE";
	
	String BULK_TRANSACTION = "BULK";
	
	//not available for payroll
	String NOT_AVAILABLE = "N/A";
	
	//file setting
	String FILE_SETTING_SEPARATED = "SEPARATED";
	
	String FILE_SETTING_CONSOLIDATED = "CONSOLIDATED";
	
	String KEY_DOWNLOAD_FILE = "downloadFileName";
	
	String FILENAME = "fileName";
	
	String SRVC_GPT_INT = "GPT_FTR_INT";
	
	String ALL_STS = "ALL STATUS";
	
	String SUCCESS_STS = "SUCCESS";
	
	String FAILED_STS = "FAILED";
	
	String NON_FIN_ALL_MENU = "ALL MENU";
	
	String NON_FIN_ADMIN_MENU = "ADMIN MENU";
	
	String NON_FIN_USER_MENU = "USER MENU";
	
	String ACTIVE = "ACTIVE";
	
	String INACTIVE = "INACTIVE";
	
	String VIEW_FROM_BANK = "isViewFromBank";
	
	String EAI_SERVICE_NAME = "eaiServiceName";
	
	String PATH_UPLOAD = "pathUpload";
	
	String PENDINGTASK_VO = "pendingTaskVO";
	
	String REMITTER_TYPE_INDIVIDUAL_CODE = "1";
	
	String MODEL_CODE = "modelCode";
	
	String MIN_PAYMENT= "minimumPayment";
	
	//Rate
	int ROUND_SCALE = 7;
	
	int ROUND_MODE_UP = BigDecimal.ROUND_UP;
	
	int ROUND_MODE_DOWN = BigDecimal.ROUND_DOWN;
	
	String BRANCH_OPTION_SAME = "SAME";
	
	String BRANCH_OPTION_PARENT = "PARENT";
	
	String RATE_COUNTER = "CR";
	
	String RATE_SPECIAL = "SR";
	
	int MAX_LENGTH_ACCOUNT_REG = 13;
	
	String SRVC_GPT_FTR_SIPKD_IH_OWN = "GPT_FTR_SIPKD_IH_OWN";
	String SRVC_GPT_FTR_SIPKD_IH_3RD = "GPT_FTR_SIPKD_IH_3RD";
	String SRVC_GPT_FTR_SIPKD_DOM_LLG = "GPT_FTR_SIPKD_DOM_LLG";
	String SRVC_GPT_FTR_SIPKD_DOM_RTGS = "GPT_FTR_SIPKD_DOM_RTGS";
	String SRVC_GPT_FTR_SIPKD_DOM_ONLINE = "GPT_FTR_SIPKD_ONLINE";
	String SRVC_GPT_FTR_SIPKD_BILL_PAY = "GPT_FTR_SIPKD_BILL_PAY";
	String SRVC_GPT_FTR_PEMDA_SIPD = "GPT_FTR_PEMDA_SIPD_IH_OWN";
	
	String SRVC_GPT_FTR_VA_PAY = "GPT_FTR_VA_PAY";
	String SPECIAL_RATE_OPEN = "OPEN";
	String SPECIAL_RATE_SETTLED = "SETTLED";
	String SPECIAL_RATE_EXPIRED = "EXPIRED";
	
	String BENELIST_TYPE_INTERNAL_BANK = "OWN";
	String BENELIST_TYPE_LOCAL_BANK = "DOM";
	String BENELIST_TYPE_LOCAL_ONLINE_BANK = "ONL";
	String BENELIST_TYPE_OVERSEAS_BANK = "INT";
	String BENELIST_TYPE_VIRTUAL_ACCOUNT = "VAA";
	
	String IS_ONE_SIGNER = "isOneSigner";
}