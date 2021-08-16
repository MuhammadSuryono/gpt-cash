package com.gpt.product.gpcash.corporate.transaction.purchase.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper.SpringBeanInvokerData;
import com.gpt.component.common.spring.invoker.spi.ISpringBeanInvoker;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;
import com.gpt.component.maintenance.errormapping.repository.ErrorMappingRepository;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.biller.purchaseinstitution.model.PurchaseInstitutionModel;
import com.gpt.product.gpcash.biller.purchaseinstitution.services.PurchaseInstitutionService;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.model.CorporateChargeModel;
import com.gpt.product.gpcash.corporate.corporatecharge.repository.CorporateChargeRepository;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.purchase.model.PurchaseModel;
import com.gpt.product.gpcash.corporate.transaction.purchase.repository.PurchaseRepository;
import com.gpt.product.gpcash.corporate.transaction.purchasepayee.model.PurchasePayeeModel;
import com.gpt.product.gpcash.corporate.transaction.purchasepayee.repository.PurchasePayeeRepository;
import com.gpt.product.gpcash.corporate.transaction.purchasepayee.services.PurchasePayeeService;
import com.gpt.product.gpcash.corporate.transaction.utils.InstructionModeUtils;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityType;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.transactionstatus.repository.TransactionStatusRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.services.TransactionStatusService;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.service.model.ServiceModel;
import com.gpt.product.gpcash.servicecharge.model.ServiceChargeModel;
import com.gpt.product.gpcash.utils.ProductRepository;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Transactional(rollbackFor = Exception.class)
public class PurchaseServiceImpl implements PurchaseService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;

	@Autowired
	private PurchaseRepository purchaseRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;

	@Autowired
	private CorporateChargeService corporateChargeService;

	@Autowired
	private PurchasePayeeService payeeService;
	
	@Autowired
	private PurchasePayeeRepository payeeRepo;

	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private PurchaseInstitutionService purchaseInstitutionService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;	
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private CorporateChargeRepository corporateChargeRepo;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;	
	
	@Autowired
	private MessageSource message;	
	
	@Autowired
	private ISpringBeanInvoker invoker;	
	
	@Autowired
	private TransactionStatusService trxStatusService;
	
	@Autowired
	private TransactionStatusRepository trxStatusRepo;
	
	@Autowired
	private GlobalTransactionService globalTransactionService;
	
	@Autowired
	private CorporateUserPendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private CorporateWFEngine wfEngine;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			String instructionMode = (String) map.get("instructionMode");

			//override instructionDate if Immediate to get server timestamp
			if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
				map.put(ApplicationConstants.INSTRUCTION_DATE, DateUtils.getCurrentTimestamp());
			}
			
			checkCustomValidation(map);

			CorporateUserPendingTaskVO vo = setCorporateUserPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
			} else {
				throw new BusinessException("GPT-0100003");
			}

			resultMap.putAll(pendingTaskService.savePendingTask(vo));
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private CorporateUserGroupModel checkCorporateUserGroup(String userCode) throws Exception {
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
		
		return corporateUser.getCorporateUserGroup();		
	}
	
	private void checkPayee(String payeeName, String corporateUserGroupId) throws Exception {
		payeeService.checkPayee(payeeName, corporateUserGroupId);
	}
	
	private void checkPayeeMustExist(String payeeName, String corporateUserGroupId) throws Exception {
		payeeService.checkPayeeMustExist(payeeName, corporateUserGroupId);
	}	

	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String senderRefNo = (String) map.get("senderRefNo");
			
			CorporateUserGroupModel corporateUserGroup = checkCorporateUserGroup((String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			purchaseInstitutionService.isPurchaseInstitutionValid((String) map.get("purchaseInstitutionCode"));
			
			// if isSavePayeeFlag = Y then validate if accountNo already in
			// already exist or not
			if (ApplicationConstants.YES.equals((String) map.get("isSavePayeeFlag"))) {
				checkPayee((String) map.get("payeeName"), corporateUserGroup.getId());
			}
			
			// if isPredefinedFlag = Y then validate if accountNo must exist in table
			if (ApplicationConstants.YES.equals((String) map.get("isPredefinedFlag"))) {
				checkPayeeMustExist((String) map.get("payeeName"), corporateUserGroup.getId());
			}
			
			//validate final payment
			validateFinalPayment(corporateId, senderRefNo);

			// TODO implement calculate equivalent amount in future if implement
			// cross currency transaction
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void validateFinalPayment(String corporateId, String senderRefNo) throws BusinessException {
		pendingTaskService.validateFinalPayment(senderRefNo, PurchaseSC.menuCode, corporateId);
		
		List<PurchaseModel> modelList = purchaseRepo.findTransactionForFinalPaymentFlag(corporateId, 
				ApplicationConstants.YES, senderRefNo);
		
		if(modelList.size() > 0) {
			throw new BusinessException("GPT-0100199");
		}
	}

	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();
		String isFinalPayment = (String) map.get("isFinalPayment");

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		BigDecimal transactionAmount = (BigDecimal) map.get(ApplicationConstants.TRANS_AMOUNT);
		String purchaseInstitutionCode = (String) map.get("purchaseInstitutionCode");
		String uniqueKey = createdBy.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountGroupDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(transactionAmount.toPlainString())
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(purchaseInstitutionCode);

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode((String) map.get(ApplicationConstants.TRANS_SERVICE_CODE));
		vo.setService("PurchaseSC");
		vo.setCorporateId(corporateId);
		vo.setTransactionAmount(transactionAmount);
		vo.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		vo.setSourceAccountGroupDetailId(accountGroupDetailId);
		vo.setSessionTime(sessionTime);
		vo.setIsFinalPayment(isFinalPayment);
		
		//set userGroupId
		vo.setUserGroupId(userGroupId);
		
		CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(vo.getCorporateId(), 
				vo.getSourceAccountGroupDetailId());		

		//set debit info
		AccountModel sourceAccount = gdm.getCorporateAccount().getAccount();
		vo.setSourceAccount(sourceAccount.getAccountNo());
		vo.setSourceAccountName(sourceAccount.getAccountName());
		
		CurrencyModel sourceCurrency = sourceAccount.getCurrency();
		vo.setSourceAccountCurrencyCode(sourceCurrency.getCode());
		vo.setSourceAccountCurrencyName(sourceCurrency.getName());
		
		vo.setTotalChargeEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_CHARGE));
		vo.setTotalDebitedEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT));
		vo.setInstructionMode(instructionMode);
		
		vo.setSenderRefNo((String) map.get("senderRefNo"));
		
		//if recurring, then instruction Date pertama kali adalah recurringStartDate
		if(ApplicationConstants.SI_RECURRING.equals(instructionMode)) {
			String recurringParamType = (String) map.get("recurringParamType"); 
			int recurringParam = (Integer) map.get("recurringParam");
			
			vo.setRecurringParamType(recurringParamType);
			vo.setRecurringParam(recurringParam);
			Timestamp recurringInstructionDate = InstructionModeUtils.getStandingInstructionDate(recurringParamType, recurringParam, 
					(Timestamp) map.get("recurringStartDate"));
			
			recurringInstructionDate = DateUtils.getInstructionDateBySessionTime(sessionTime, recurringInstructionDate);
			vo.setInstructionDate(recurringInstructionDate);
			vo.setRecurringStartDate(recurringInstructionDate);
			vo.setRecurringEndDate(DateUtils.getInstructionDateBySessionTime(sessionTime, (Timestamp) map.get("recurringEndDate")));
		} else {
			vo.setInstructionDate(DateUtils.getInstructionDateBySessionTime(sessionTime, instructionDate));
		}
				
		vo.setTaskExpiryDate(transactionValidationService.validateExpiryDate(instructionMode, instructionDate));
		
		return vo;
	}

	@SuppressWarnings("unchecked")
	private PurchaseModel setMapToModel(PurchaseModel purchase, Map<String, Object> map,
			boolean isNew, CorporateUserPendingTaskVO vo) throws Exception {
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();

		String payeeId = (String) map.get("payeeId");
		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		String isPredefinedFlag = (String) map.get("isPredefinedFlag");
		String isSavePayeeFlag = (String) map.get("isSavePayeeFlag");
		String instructionMode = (String) map.get("instructionMode");

		Timestamp instructionDate = vo.getInstructionDate();
		if(ApplicationConstants.SI_RECURRING.equals(vo.getInstructionMode())) {
			//override instructionDate to today timestamp
			Timestamp recurringInstructionDate = InstructionModeUtils.getStandingInstructionDate(vo.getRecurringParamType(), vo.getRecurringParam(), 
					DateUtils.getCurrentTimestamp());
			
			instructionDate = DateUtils.getInstructionDateBySessionTime(vo.getSessionTime(), recurringInstructionDate);
		}
		
		String payeeName = (String) map.get("payeeName");
		String description = (String) map.get("description");
		String value1 = (String) map.get("value1");
		String value2 = (String) map.get("value2");
		String value3 = (String) map.get("value3");
		String value4 = (String) map.get("value4");
		String value5 = (String) map.get("value5");
		String purchaseInstitutionCode = (String) map.get("purchaseInstitutionCode");

		// set ID
		purchase.setId(vo.getId());

		// set transaction information
		purchase.setReferenceNo(vo.getReferenceNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		purchase.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		purchase.setCorporate(corpModel);
		
		purchase.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE));
		purchase.setApplication(application);
		
		PurchaseInstitutionModel purchaseinstitutionModel = purchaseInstitutionService.isPurchaseInstitutionValid(purchaseInstitutionCode);
		purchase.setPurchaseInstitution(purchaseinstitutionModel);

		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		purchase.setSourceAccount(sourceAccountModel);		
		
		purchase.setTransactionAmount(new BigDecimal(map.get(ApplicationConstants.TRANS_AMOUNT).toString()));
		purchase.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		purchase.setTotalChargeEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_CHARGE).toString()));
		purchase.setTotalDebitedEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT).toString()));

		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		purchase.setService(service);

		// set payeeName based on benId
		setPayeeInfo(purchase, payeeId, payeeName, description, value1, value2, value3, value4, value5, 
				isSavePayeeFlag, vo.getCorporateId(), corporateUserGroup.getId(), isPredefinedFlag, 
				purchaseInstitutionCode, vo.getCreatedBy());

		// set additional information
		purchase.setSenderRefNo((String) map.get("senderRefNo"));
		purchase.setIsFinalPayment((String) map.get("isFinalPayment"));
		purchase.setBenRefNo((String) map.get("benRefNo"));
		purchase.setIsNotifyBen((String) map.get("isNotify"));
		purchase.setNotifyBenValue((String) map.get("notifyBenValue"));
		
		setInstructionMode(purchase, instructionMode, instructionDate, map);
		
		setCharge(purchase, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		
		return purchase;
	}
	
	private void setCharge(PurchaseModel purchase, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				purchase.setChargeType1(chargeId);
				purchase.setChargeTypeAmount1(value);
				purchase.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				purchase.setChargeType2(chargeId);
				purchase.setChargeTypeAmount2(value);
				purchase.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				purchase.setChargeType3(chargeId);
				purchase.setChargeTypeAmount3(value);
				purchase.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				purchase.setChargeType4(chargeId);
				purchase.setChargeTypeAmount4(value);
				purchase.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				purchase.setChargeType5(chargeId);
				purchase.setChargeTypeAmount5(value);
				purchase.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}

	private void setInstructionMode(PurchaseModel purchase, String instructionMode, Timestamp instructionDate, 
			Map<String, Object> map) throws Exception {
		
		
		// set instruction mode
		purchase.setInstructionMode(instructionMode);

		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			purchase
					.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		} else if (instructionMode.equals(ApplicationConstants.SI_FUTURE_DATE)) {
			purchase.setInstructionDate(instructionDate);
		} else if (instructionMode.equals(ApplicationConstants.SI_RECURRING)) {
			purchase.setRecurringParamType((String) map.get("recurringParamType"));
			purchase.setRecurringParam((Integer) map.get("recurringParam"));
			purchase.setRecurringStartDate(instructionDate);
			purchase.setRecurringEndDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("recurringEndDate")));
			purchase.setInstructionDate(instructionDate);
		}
	}

	private void setPayeeInfo(PurchaseModel purchase, String payeeId, String payeeName, String description, String value1, String value2, 
			String value3, String value4, String value5, String isSavePayeeFlag, String corporateId, String corporateUserGroupId, String isPredefinedFlag,
			String purchaseInstitutionCode, String createdBy) throws Exception {

		
		if (ApplicationConstants.YES.equals(isPredefinedFlag)) {
			// benId from beneficiary droplist
			
			// cek if benId exist in bene table
			PurchasePayeeModel payee = payeeRepo.findOne(payeeId);
			if (payee==null)
				throw new BusinessException("GPT-0100119");
			
			payeeName = payee.getPayeeName();
			
		} else if (ApplicationConstants.YES.equals(isSavePayeeFlag)) {
			// save to bene table
			payeeService.savePayee(corporateId, corporateUserGroupId, payeeName, description, value1, value2, 
					value3, value4, value5, purchaseInstitutionCode, createdBy);
		}

		purchase.setPayeeName(payeeName);
		purchase.setValue1(value1);
		purchase.setValue2(value2);
		purchase.setValue3(value3);
		purchase.setValue4(value4);
		purchase.setValue5(value5);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				PurchaseModel purchase = new PurchaseModel();
				setMapToModel(purchase, map, true, vo);

				if(ApplicationConstants.SI_IMMEDIATE.equals(purchase.getInstructionMode())) {
					savePurchase(purchase, vo.getCreatedBy(), ApplicationConstants.YES);

					doTransfer(purchase, (String)map.get(ApplicationConstants.APP_CODE));
                    vo.setErrorCode(purchase.getErrorCode());
					vo.setIsError(purchase.getIsError());
				} else {
					savePurchase(purchase, vo.getCreatedBy(), ApplicationConstants.NO);
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (ApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	private void savePurchase(PurchaseModel purchase, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		purchase.setIsProcessed(isProcessed);
		purchase.setCreatedDate(DateUtils.getCurrentTimestamp());
		purchase.setCreatedBy(createdBy);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(purchase.getPendingTaskId() == null)
			purchase.setPendingTaskId(purchase.getId());

		purchaseRepo.persist(purchase);
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			
			String institutionCode = (String)map.get("purchaseInstitutionCode");
			String accountGroupDetailId = (String)map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			String corpId = (String)map.get(ApplicationConstants.LOGIN_CORP_ID);
			PurchaseInstitutionModel institution = purchaseInstitutionService.isPurchaseInstitutionValid(institutionCode);
			
			CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(corpId, accountGroupDetailId);			
			String accountNo = gdm.getCorporateAccount().getAccount().getAccountNo();
			
			BigDecimal transactionAmount = getTransactionAmount(map, institution);
			
			resultMap.put("transactionAmount", transactionAmount);
			
			checkCustomValidation(map);
			
			if(ApplicationConstants.YES.equals(institution.getBillOnlineFlag())) {
				
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("debitAccountNo", accountNo);
				inputs.put("institutionCode", institutionCode);
				inputs.put("billKey1", map.get("value1"));
				inputs.put("billKey2", map.get("value2"));
				inputs.put("billKey3", map.get("value3"));
				inputs.put("billKey4", map.get("value4"));
				inputs.put("billKey5", map.get("value5"));
				inputs.put("trxAmount", transactionAmount);
				
				Map<String, Object> outputs = null;
				
				outputs = eaiAdapter.invokeService(EAIConstants.BILL_PAYMENT_INQUIRY, inputs);
							
				//totalBillAmount always get from eai because maybe some biller has adminFee. Eai will add with fee
				resultMap.put("transactionAmount", outputs.get("totalBillAmount"));
				
				List<Map<String,Object>> othersInfo = (List<Map<String,Object>>)outputs.get("othersInfo");
				List<Map<String,Object>> billsInfo = new ArrayList<>();
				if (ValueUtils.hasValue(othersInfo)) {
					Locale locale = LocaleContextHolder.getLocale();
					for (Map<String,Object> info : othersInfo) {
						String label = (String)info.get("label");
						
						Map<String, Object> billInfo = new HashMap<>();
						
						Object obj = info.get("value");
						
						if(obj instanceof List) {
							List<List> bills =  (List<List>) obj;
							for(List<Map<String, Object>> billsData : bills) {
								for(Map<String, Object> billsMap : billsData) {
									billsMap.put("label", message.getMessage((String) billsMap.get("label"), null, (String) billsMap.get("label"), locale));
								}
							}
						}
						
						billInfo.put("key", info.get("key"));
						billInfo.put("label", message.getMessage(label, null, label, locale));
						billInfo.put("value", info.get("value"));
						billInfo.put("visible", info.get("visible"));
						billInfo.put("type", info.get("type"));
						billsInfo.add(billInfo);
						
					}
				}
				resultMap.put("othersInfo", billsInfo);
				
			}
			
			String payeeName = (String)map.get("payeeName");
			if (ApplicationConstants.YES.equals((String) map.get("isPredefinedFlag"))) {
				CorporateUserGroupModel corporateUserGroup = checkCorporateUserGroup((String) map.get(ApplicationConstants.LOGIN_USERCODE));
				
				checkPayeeMustExist(corporateUserGroup.getId(), payeeName);
			}			

			resultMap.putAll(corporateChargeService.getCorporateCharges((String) map.get(ApplicationConstants.APP_CODE),
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE),
					(String) map.get(ApplicationConstants.LOGIN_CORP_ID)));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private BigDecimal getTransactionAmount(Map<String, Object> map, PurchaseInstitutionModel model){
		if("Denomination".equals(model.getNameEng1())) {
			return new BigDecimal((String) map.get("value1"));
		}else if("Denomination".equals(model.getNameEng2())) {
			return new BigDecimal((String) map.get("value2"));
		}else if("Denomination".equals(model.getNameEng3())) {
			return new BigDecimal((String) map.get("value3"));
		}else if("Denomination".equals(model.getNameEng4())) {
			return new BigDecimal((String) map.get("value4"));
		}else {
			return new BigDecimal((String) map.get("value5"));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void doTransfer(PurchaseModel model, String appCode) throws Exception {
		boolean isRollback = false;
		boolean limitUpdated = false;
		boolean isTimeOut = false;
		
		try {
			//update transaction limit
			transactionValidationService.updateTransactionLimit(model.getCorporate().getId(), 
					model.getService().getCode(), 
					model.getSourceAccount().getCurrency().getCode(), 
					model.getTransactionCurrency(), 
					model.getCorporateUserGroup().getId(), 
					model.getTotalDebitedEquivalentAmount(),
					model.getApplication().getCode());
			limitUpdated = true;
			
			Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.BILL_PAYMENT, inputs);

			//update othersInfo ke pending task untuk keperluan view CREATE di trxStatus dan saveNewOthersInfo = true
			if(outputs.get("saveNewOthersInfo") != null && (Boolean) outputs.get("saveNewOthersInfo")) {
				CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findOne(model.getPendingTaskId());
				
				if(pendingTask != null) {
					Class<?> clazz = Class.forName(pendingTask.getModel());

					if(pendingTask.getValuesStr() != null) {
						Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(pendingTask.getValuesStr(), clazz);
						
						List<Map<String,Object>> othersInfo = (List<Map<String,Object>>)outputs.get("othersInfo");
						List<Map<String,Object>> billsInfo = new ArrayList<>();
						if (ValueUtils.hasValue(othersInfo)) {
							Locale locale = LocaleContextHolder.getLocale();
							for (Map<String,Object> info : othersInfo) {
								String label = (String)info.get("label");
								
								Map<String, Object> billInfo = new HashMap<>();
								billInfo.put("key", info.get("key"));
								billInfo.put("label", message.getMessage(label, null, label, locale));
								billInfo.put("value", info.get("value"));						
								billsInfo.add(billInfo);
							}
						}
						
						//replace new othersInfo to map
						valueMap.put("othersInfo", billsInfo);
						String jsonObj = objectMapper.writeValueAsString(valueMap);
						pendingTask.setValues(jsonObj);
						pendingTaskRepo.save(pendingTask);
						//----------------------------------------------
						
					}
				}
			}
			
			model.setStatus("GPT-0100130");
			model.setIsError(ApplicationConstants.NO);	
		} catch (BusinessException e) {
			String errorMsg = e.getMessage();
			if (errorMsg!=null && errorMsg.startsWith("EAI-")) {
				ErrorMappingModel errorMappingModel = errorMappingRepo.findOne(e.getMessage());
				
				//ada pengecekan jika error code terdaftar dalam list errorcode timeout, status diupdate jdi inprogress offline, BSG 26 Feb 2020
				isTimeOut = maintenanceRepo.isTimeOutErrorCodeValid(e.getMessage());
				
				if (errorMappingModel!=null && ApplicationConstants.YES.equals(errorMappingModel.getRollbackFlag()) && !isTimeOut) {
					isRollback = true;
					throw e;
				}
				
				// Either no error mapping or no rollback...
				
				//jika transaksi gagal tetapi tidak di rollback maka kena error execute with failure
				model.setIsError(ApplicationConstants.YES);
				model.setErrorCode(errorMappingModel == null ? errorMsg : errorMappingModel.getCode());
				if(!isTimeOut) {
					model.setStatus("GPT-0100129");					
				}else {
					model.setStatus("GPT-0100185"); // in Progress Offline
				}
				
			} else {
				isRollback = true;
				throw e;
			}
		} catch (Exception e) {
			isRollback = true;
			throw new ApplicationException(e);
		} finally {
			//save transaction log
			if(!isTimeOut) {
				globalTransactionService.save(model.getCorporate().getId(), PurchaseSC.menuCode, 
						model.getService().getCode(), model.getReferenceNo(), 
						model.getId(), model.getTransactionCurrency(), model.getTotalChargeEquivalentAmount(), 
						model.getTransactionAmount(), 
						model.getTotalDebitedEquivalentAmount(), model.getIsError());
			}else {
				notifyBankEmail(model, appCode);
			}
			//----------------------------------
			
			if(isRollback) {
				if(limitUpdated) {
					try {
						transactionValidationService.reverseUpdateTransactionLimit(model.getCorporate().getId(), 
								model.getService().getCode(), 
								model.getSourceAccount().getCurrency().getCode(), 
								model.getTransactionCurrency(), 
								model.getCorporateUserGroup().getId(), 
								model.getTotalDebitedEquivalentAmount(),
								model.getApplication().getCode());
					} catch(Exception e) {
						logger.error("Failed to reverse the usage "+e.getMessage(),e);
					}
				}
			} else {
				try {
					// send email to corporate email
//					notifyCorporate(model, appCode);
					
					// send email to transaction user history
					notifyTrxUsersHistory(model, appCode);					
					
					// send email to beneficiary only if not execute with failure
					if (!ApplicationConstants.YES.equals(model.getIsError()) && ApplicationConstants.YES.equals(model.getIsNotifyBen())) {
						notifyBeneficiary(model, appCode);
					}
				} catch(Exception e) {
					// ignore any error, just in case something bad happens
				}
			}
		}
	}
	
	private Map<String, Object> prepareInputsForEAI(PurchaseModel model, String appCode) throws Exception {
		Map<String, Object> inputs = new HashMap<>();
		
		inputs.put("channel", appCode);
		inputs.put("channelRefNo", model.getId());

		AccountModel sourceAccountModel = model.getSourceAccount();
				
		inputs.put("debitAccountNo", sourceAccountModel.getAccountNo());
		inputs.put("debitAccountName", sourceAccountModel.getAccountName());
		inputs.put("debitAccountType", sourceAccountModel.getAccountType().getCode());
		inputs.put("debitAccountCurrencyCode", sourceAccountModel.getCurrency().getCode());
		
		inputs.put("institutionCode", model.getPurchaseInstitution().getCode());
		inputs.put("institutionName", model.getPurchaseInstitution().getName());
		
		inputs.put("billKey1", model.getValue1());
		inputs.put("billKey2", model.getValue2());
		inputs.put("billKey3", model.getValue3());
		inputs.put("billKey4", model.getValue4());
		inputs.put("billKey5", model.getValue5());		
				
		inputs.put("trxCurrencyCode", model.getTransactionCurrency());
		inputs.put("trxAmount", model.getTransactionAmount());
		
		inputs.put("chargeType1", model.getChargeType1());
		inputs.put("chargeTypeCurrencyCode1", model.getChargeTypeCurrency1());
		inputs.put("chargeTypeAmount1", model.getChargeTypeAmount1());

		inputs.put("chargeType2", model.getChargeType2());
		inputs.put("chargeTypeCurrencyCode2", model.getChargeTypeCurrency2());
		inputs.put("chargeTypeAmount2", model.getChargeTypeAmount2());

		inputs.put("chargeType3", model.getChargeType3());
		inputs.put("chargeTypeCurrencyCode3", model.getChargeTypeCurrency3());
		inputs.put("chargeTypeAmount3", model.getChargeTypeAmount3());

		inputs.put("chargeType4", model.getChargeType4());
		inputs.put("chargeTypeCurrencyCode4", model.getChargeTypeCurrency4());
		inputs.put("chargeTypeAmount4", model.getChargeTypeAmount4());
		
		inputs.put("chargeType5", model.getChargeType5());
		inputs.put("chargeTypeCurrencyCode5", model.getChargeTypeCurrency5());
		inputs.put("chargeTypeAmount5", model.getChargeTypeAmount5());
				
		inputs.put("serviceCode", model.getService().getCode());

		inputs.put("refNo", model.getReferenceNo());
		inputs.put("senderRefNo", model.getSenderRefNo());
		inputs.put("benRefNo", model.getBenRefNo());
		
		CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findOne(model.getPendingTaskId());
		if(pendingTask != null) {
			Class<?> clazz = Class.forName(pendingTask.getModel());

			if(pendingTask.getValuesStr() != null) {
				Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(pendingTask.getValuesStr(), clazz);
				inputs.put("packageCode", valueMap.get("packageCode"));			
			}
		}
		
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	/*private void notifyCorporate(PurchaseModel model, String appCode) {
		try {
			CorporateModel corpModel = model.getCorporate();
			String email1 = corpModel.getEmail1();
			String email2 = corpModel.getEmail2();
			
			List<String> emails = new ArrayList<>();
			if (ValueUtils.hasValue(email1))
				emails.add(email1);
			
			if (ValueUtils.hasValue(email2))
				emails.add(email2);
			
			if (emails.size()>0) {
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("emails", emails);
				inputs.put("subject", "notify corporate");
				
				eaiAdapter.invokeService(EAIConstants.CORPORATE_BP_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}*/
	
	private void notifyTrxUsersHistory(PurchaseModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				IDMMenuModel menu = idmRepo.isIDMMenuValid(PurchaseSC.menuCode);
				
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("instructionMode", model.getInstructionMode());
				inputs.put("instructionDate", model.getInstructionDate());
				inputs.put("status", model.getStatus());
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails", emails);
				inputs.put("subject", menu.getName().concat(" Notification"));
				
				eaiAdapter.invokeService(EAIConstants.USER_BP_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}	
	
	private void notifyBeneficiary(PurchaseModel model, String appCode) {		
		if (ValueUtils.hasValue(model.getNotifyBenValue())) {
			String[] vals = getMultipleEntries(model.getNotifyBenValue());
			if (vals!=null && vals.length>0) {
				try {
					Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
					inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
					inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
					inputs.put("emails", Arrays.asList(vals));
					inputs.put("subject", "Beneficiary Transfer Notification");

					eaiAdapter.invokeService(EAIConstants.BENEFICIARY_TRANSFER_NOTIFICATION, inputs);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}				
			}
		}		
	}
	
	private String[] getMultipleEntries(String entry) {
		StringTokenizer tokenizer = new StringTokenizer(entry,",;");
		int size = tokenizer.countTokens();
		if(size>0) {
			String[] entries = new String[size];
			for(int i=0;tokenizer.hasMoreTokens();i++) {
				entries[i] = (String)tokenizer.nextToken();
			}
			return entries;
		}
		return null;
	}	
	
	@Override
	public void executeFutureTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		try{
			Timestamp futureDate = DateUtils.getCurrentTimestamp();
			String[] parameterArr = parameter.split("\\|");
			
			int sessionTimeValue = Integer.valueOf(parameterArr[0]);
			
			if(parameterArr.length > 1) {
				String[] futureDateArr = parameterArr[1].split("\\=");
				
				if(ValueUtils.hasValue(futureDateArr[1])){
					futureDate = new Timestamp(Helper.DATE_TIME_FORMATTER.parse(futureDateArr[1]).getTime());
				}
			}
			
			Calendar calFrom = DateUtils.getEarliestDate(futureDate);
			
			List<String> sessionTimeList = sysParamService.getTransactionSessionTime();
			
			if(sessionTimeValue > 0 && sessionTimeValue <= sessionTimeList.size()) {
				
				Calendar calTo = DateUtils.getNextSessionTime(futureDate, sessionTimeValue, sessionTimeList);
				
				if(logger.isDebugEnabled()) {
					logger.debug("Future start : " + new Timestamp(calFrom.getTimeInMillis()));
					logger.debug("Future end : " + new Timestamp(calTo.getTimeInMillis()));
				}
				
				List<PurchaseModel> purchaseList = purchaseRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(purchaseList);
				
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void executeRecurringTransactionScheduler(String parameter) throws ApplicationException, BusinessException {
		try{
			Timestamp recurringDate = DateUtils.getCurrentTimestamp();
			String[] parameterArr = parameter.split("\\|");
			
			int sessionTimeValue = Integer.valueOf(parameterArr[0]);
			
			if(parameterArr.length > 1) {
				String[] recurringDateArr = parameterArr[1].split("\\=");
				
				if(ValueUtils.hasValue(recurringDateArr[1])){
					recurringDate = new Timestamp(Helper.DATE_TIME_FORMATTER.parse(recurringDateArr[1]).getTime());
				}
			}
			
			
			Calendar calFrom = DateUtils.getEarliestDate(recurringDate);
			
			List<String> sessionTimeList = sysParamService.getTransactionSessionTime();
			
			if(sessionTimeValue > 0 && sessionTimeValue <= sessionTimeList.size()) {
				
				Calendar calTo = DateUtils.getNextSessionTime(recurringDate, sessionTimeValue, sessionTimeList);

				if(logger.isDebugEnabled()) {
					logger.debug("Recurring start : " + new Timestamp(calFrom.getTimeInMillis()));
					logger.debug("Recurring end : " + new Timestamp(calTo.getTimeInMillis()));
				}
				
				List<PurchaseModel> purchaseList = purchaseRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_RECURRING, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(purchaseList);
				
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<PurchaseModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(PurchaseModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "PurchaseService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);						
	}
		
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(PurchaseModel purchase) {
		if(logger.isDebugEnabled())
			logger.debug("Process recurring purchase : " + purchase.getId() + " - " + purchase.getReferenceNo());

		boolean success = false;
		boolean isTimeOut = false;
		
		String errorCode = null;
		try {			
			purchase.setIsProcessed(ApplicationConstants.YES);
			purchaseRepo.save(purchase);
			
			//avoid lazy when get details
			purchase = purchaseRepo.findOne(purchase.getId());
		
			doTransfer(purchase, ApplicationConstants.APP_GPCASHBO);
			success = true;
		} catch (BusinessException e) {
			errorCode = e.getErrorCode();
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			/*
			 	Only generate new recurring if transaction executed successfully
				create new instructionDate if instructionMode = Recurring 
			*/
			
			boolean isExpired = false;
			try {
				if(ApplicationConstants.SI_RECURRING.equals(purchase.getInstructionMode())) {					
					Timestamp newInstructionDate = InstructionModeUtils.getStandingInstructionDate(purchase.getRecurringParamType(), purchase.getRecurringParam(), purchase.getInstructionDate());
					
					Calendar calNewInstructionDate = DateUtils.getEarliestDate(newInstructionDate);
					Calendar calExpired = DateUtils.getEarliestDate(purchase.getRecurringEndDate());
					
					if(logger.isDebugEnabled()) {
						logger.debug("Recurring new instruction date : " + calNewInstructionDate.getTime());
						logger.debug("Recurring expired date : " + calExpired.getTime());
					}
					
					if(calNewInstructionDate.compareTo(calExpired) >= 0) {
						Calendar oldInstructionDate = Calendar.getInstance();
						oldInstructionDate.setTime(purchase.getInstructionDate());
						
						//get session time from old recurring
						calNewInstructionDate.set(Calendar.HOUR_OF_DAY, oldInstructionDate.get(Calendar.HOUR_OF_DAY));
						calNewInstructionDate.set(Calendar.MINUTE, oldInstructionDate.get(Calendar.MINUTE));
						saveNewTransactionForRecurring(purchase, new Timestamp(calNewInstructionDate.getTimeInMillis()));
					} else {
						isExpired = true;
					}
					
				}
			} catch(Exception e) {
				logger.error("Failed purchase id : " + purchase.getId());
			}
			
			if (success) {
				if (ApplicationConstants.YES.equals(purchase.getIsError())) {
					pendingTaskRepo.updatePendingTask(purchase.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				
					trxStatusService.addTransactionStatus(purchase.getPendingTaskId(),  DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, purchase.getId(), true, purchase.getErrorCode());
				} else {
					pendingTaskRepo.updatePendingTask(purchase.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
					
					trxStatusService.addTransactionStatus(purchase.getPendingTaskId(),  DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, purchase.getId(), false, null);
				}
			} else {
				//ada pengecekan jika error code terdaftar dalam list errorcode timeout, status diupdate jdi inprogress offline, BSG 26 Feb 2020
				try {
					isTimeOut = maintenanceRepo.isTimeOutErrorCodeValid(errorCode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(!isTimeOut) {
					pendingTaskRepo.updatePendingTask(purchase.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
					trxStatusService.addTransactionStatus(purchase.getPendingTaskId(),  DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, purchase.getId(), true, errorCode);
				}else {
					pendingTaskRepo.updatePendingTask(purchase.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE);
					trxStatusService.addTransactionStatus(purchase.getPendingTaskId(),  DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.IN_PROGRESS_OFFLINE, purchase.getId(), true, errorCode);
				}
				
			}
			
			if (isExpired) {
				pendingTaskRepo.updatePendingTask(purchase.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED);
				
				//insert into trx status EXPIRED
				trxStatusService.addTransactionStatus(purchase.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED, null, true, null);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void saveNewTransactionForRecurring(PurchaseModel model, Timestamp newInstructionDate) {
		try {
			PurchaseModel newModel = new PurchaseModel();
			newModel.setId(Helper.generateHibernateUUIDGenerator());
			newModel.setReferenceNo(model.getReferenceNo());
			newModel.setMenu(model.getMenu());
			newModel.setCorporate(model.getCorporate());
			newModel.setCorporateUserGroup(model.getCorporateUserGroup());
			newModel.setApplication(model.getApplication());
			newModel.setSourceAccount(model.getSourceAccount());
			newModel.setTransactionAmount(model.getTransactionAmount());
			newModel.setTransactionCurrency(model.getTransactionCurrency());
			
			//set pendingTaskId maker for recurring
			String makerPendingTaskIdForRecurring = model.getPendingTaskId();
			if(!ValueUtils.hasValue(makerPendingTaskIdForRecurring)) {
				makerPendingTaskIdForRecurring = model.getId();
			}
			newModel.setPendingTaskId(makerPendingTaskIdForRecurring);
			//--------------------------------------------
			
			//TODO recalculate all if implement forex trx
			
			//calculate new charge for recurring
			Map<String, Object> chargesInfo =  corporateChargeService.getCorporateCharges(model.getApplication().getCode(),
					model.getService().getCode(), model.getCorporate().getId());
			
			BigDecimal totalChargeFromTable = new BigDecimal((String) chargesInfo.get(ApplicationConstants.TRANS_TOTAL_CHARGE));
			BigDecimal totalTransaction = model.getTransactionAmount().add(totalChargeFromTable);
			
			newModel.setTotalChargeEquivalentAmount(totalChargeFromTable);
			newModel.setTotalDebitedEquivalentAmount(totalTransaction);
			
			setCharge(newModel, (List<Map<String, Object>>)chargesInfo.get(ApplicationConstants.TRANS_CHARGE_LIST));
			
			newModel.setService(model.getService());
			newModel.setSenderRefNo(model.getSenderRefNo());
			newModel.setIsFinalPayment(model.getIsFinalPayment());
			newModel.setBenRefNo(model.getBenRefNo());
			newModel.setIsNotifyBen(model.getIsNotifyBen());
			newModel.setNotifyBenValue(model.getNotifyBenValue());
			
			newModel.setPayeeName(model.getPayeeName());
			newModel.setValue1(model.getValue1());
			newModel.setValue2(model.getValue2());
			newModel.setValue3(model.getValue3());
			newModel.setValue4(model.getValue4());
			newModel.setValue5(model.getValue5());
			
			//set new instructionDate for recurring
			newModel.setInstructionMode(model.getInstructionMode());
			newModel.setRecurringParamType(model.getRecurringParamType());
			newModel.setRecurringParam(model.getRecurringParam());
			newModel.setRecurringStartDate(model.getRecurringStartDate());
			newModel.setRecurringEndDate(model.getRecurringEndDate());
			newModel.setInstructionDate(newInstructionDate);
			
			savePurchase(newModel, ApplicationConstants.CREATED_BY_SYSTEM, ApplicationConstants.NO);

	        //insert into trx status PENDING_EXECUTE with new model id
			Timestamp now = DateUtils.getCurrentTimestamp();
			pendingTaskRepo.updatePendingTask(makerPendingTaskIdForRecurring, ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);
			
			trxStatusService.addTransactionStatus(makerPendingTaskIdForRecurring, now, TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE, null, false, null);
		} catch(Exception e) {
			// ignore any error to avoid transaction rollback caused by saving new transaction
			logger.error("Failed save Purchase recurring with id : " + model.getId());
		}
	}
	
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException {

		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			String transactionStatusId = (String) map.get("executedId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			//jika success ambil ke model nya, jika tidak maka ambil dr pending task krn blm masuk table
			if(trxStatus.getStatus().equals(TransactionStatus.EXECUTE_SUCCESS)) {
				PurchaseModel model = purchaseRepo.findOne(trxStatus.getEaiRefNo());
				resultMap.put("executedResult", prepareDetailTransactionMap(model, trxStatus));
			} else {
				CorporateUserPendingTaskModel model = pendingTaskRepo.findOne(trxStatus.getEaiRefNo());
				resultMap.put("executedResult", globalTransactionService.prepareDetailTransactionMapFromPendingTask(model, trxStatus));
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private List<Map<String, Object>> prepareDetailTransactionMap(PurchaseModel model, TransactionStatusModel trxStatus) throws Exception {
		List<Map<String, Object>> executedTransactionList = new LinkedList<>();
		
		AccountModel sourceAccount = model.getSourceAccount();
		
		Map<String, Object> modelMap = new HashMap<>();
		modelMap.put("executedDate", trxStatus.getActivityDate());
		modelMap.put("systemReferenceNo", model.getId());
		modelMap.put("debitAccount", sourceAccount.getAccountNo());
		modelMap.put("debitAccountName", sourceAccount.getAccountName());
		modelMap.put("debitAccountCurrency", sourceAccount.getCurrency().getCode());
		modelMap.put("debitAccountCurrencyName", sourceAccount.getCurrency().getName());
		modelMap.put("transactionCurrency", model.getTransactionCurrency());
		modelMap.put("transactionAmount", model.getTransactionAmount());
		modelMap.put("senderRefNo", ValueUtils.getValue(model.getSenderRefNo()));
		modelMap.put("benRefNo", ValueUtils.getValue(model.getBenRefNo()));
		
		//TODO diganti jika telah implement rate
		modelMap.put("debitTransactionCurrency", model.getTransactionCurrency());
		modelMap.put("debitEquivalentAmount", model.getTransactionAmount());
		modelMap.put("debitExchangeRate", ApplicationConstants.ZERO);
		modelMap.put("creditTransactionCurrency", model.getTransactionCurrency());
		modelMap.put("creditEquivalentAmount", model.getTransactionAmount());
		modelMap.put("creditExchangeRate", ApplicationConstants.ZERO);
		//--------------------------------------------
		
		//TODO diganti jika telah implement periodical charges
		modelMap.put("chargeAccount", ApplicationConstants.EMPTY_STRING);
		
		LinkedList<Map<String, Object>> chargeList = new LinkedList<>();
		
		Map<String, Object> chargeMap = null;
		
		if(model.getChargeType1() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType1());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency1());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount1());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType2() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType2());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency2());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount2());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType3() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType3());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency3());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount3());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType4() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType4());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency4());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount4());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		if(model.getChargeType5() != null) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType5());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("chargeType", serviceCharge.getName());
			chargeMap.put("chargeCurrency", model.getChargeTypeCurrency5());
			chargeMap.put("chargeEquivalentAmount", model.getChargeTypeAmount5());
			chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
			chargeList.add(chargeMap);
		}
		
		modelMap.put("chargeList", chargeList);
		
		//--------------------------------------------
		
		modelMap.put("status", trxStatus.getStatus());
		modelMap.put("errorCode", ValueUtils.getValue(trxStatus.getErrorCode(), ApplicationConstants.EMPTY_STRING));
		
		//ini buat di translate
		Locale locale = LocaleContextHolder.getLocale();
		modelMap.put("errorDscp", ValueUtils.getValue(message.getMessage(trxStatus.getErrorCode(), null, trxStatus.getErrorCode(), locale), ApplicationConstants.EMPTY_STRING));
		
		executedTransactionList.add(modelMap);
		
		return executedTransactionList;
	}
	
	@Override
	public Map<String, Object> cancelTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException {

		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			//cancelId = pendingTaskId
			String pendingTaskId = (String) map.get("cancelId");
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			
			if(logger.isDebugEnabled()) {
				logger.debug("cancel pendingTaskId : " + pendingTaskId);
			}
			
			//expired to wf engine
			try {
				wfEngine.cancelInstance(pendingTaskId);
			} catch (Exception e) {
				logger.debug(e.getMessage() + " " + pendingTaskId);
			}
			
			
			//update expired to pending task
			pendingTaskRepo.updatePendingTask(pendingTaskId, Status.CANCELED.name(), DateUtils.getCurrentTimestamp(), loginUserCode, TransactionStatus.CANCELLED);

			//insert into trx status EXPIRE
			trxStatusService.addTransactionStatus(pendingTaskId, DateUtils.getCurrentTimestamp(), TransactionActivityType.CANCEL, loginUserCode, TransactionStatus.CANCELLED, null, true, null);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Locale locale = LocaleContextHolder.getLocale();
			String transactionStatusId = (String) map.get("receiptId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");
			SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			
			HashMap<String, Object> reportParams = new HashMap<>();
			reportParams.put("trxDate", sdfDate.format(trxStatus.getActivityDate()));
			reportParams.put("trxTime", sdfTime.format(trxStatus.getActivityDate()));
			reportParams.put("transactionStatus", message.getMessage(trxStatus.getStatus().toString(), null, trxStatus.getStatus().toString(), locale));
			reportParams.put("errorDscp", trxStatus.getErrorCode());
			reportParams.put("printDate", sdfDateTime.format(new Date()));
			
			Path bankLogoPath = Paths.get(bankLogo);
			BufferedImage bankLogo = ImageIO.read(bankLogoPath.toFile());
			reportParams.put("bankLogo", bankLogo);
			
			PurchaseModel model = purchaseRepo.findOne(trxStatus.getEaiRefNo());
			CorporateUserPendingTaskModel pending = (CorporateUserPendingTaskModel) pendingTaskRepo.findOne(trxStatus.getPendingTaskId());
			setParamForDownloadTrxStatus(reportParams, model, pending);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "Purchase" + File.separator + "download-transaction-purchase" + "-" + ((String) reportParams.get("purchaseInstitution")).toLowerCase() + "-"
					+ locale.getLanguage() + ".jasper";
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			setCopyrightSubReport(reportParams);
			
			if(ApplicationConstants.YES.equals(pending.getErrorTimeoutFlag())) {
				setSuspectTrxSubReport(reportParams);
			}
			
			JasperPrint print = JasperFillManager.fillReport(masterReport, reportParams, new JREmptyDataSource());
			byte[] bytes = JasperExportManager.exportReportToPdf(print);
		
			//download reference no agar unique tiap request download
			String downloadRefNo = Helper.generateReportReferenceNo();
			String destinationFile = pathDownload + File.separator + downloadRefNo.concat(".").concat("pdf");
			Path destinationFilePath = Paths.get(destinationFile);
			
			//write files
			Files.write(destinationFilePath, bytes);
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.FILENAME, destinationFile);
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void setSuspectTrxSubReport(Map<String, Object> reportParams) throws JRException {
		Locale locale = LocaleContextHolder.getLocale();
		String suspectTrxFile = reportFolder + File.separator + "SuspectTrx" + File.separator + "suspect-trx" + "-" + locale.getLanguage() + ".jasper";
		JasperReport suspectTrxReport = (JasperReport) JRLoader.loadObject(new File(suspectTrxFile));
		reportParams.put("SUSPECT_TRX_REPORT", suspectTrxReport);
	}
	
	
	private void setCopyrightSubReport(Map<String, Object> reportParams) throws JRException {
		Locale locale = LocaleContextHolder.getLocale();
		String copyrightFile = reportFolder + File.separator + "Copyright" + File.separator + "bank-copyright" + "-" + locale.getLanguage() + ".jasper";
		JasperReport copyrightReport = (JasperReport) JRLoader.loadObject(new File(copyrightFile));
		reportParams.put("COPYRIGHT_REPORT", copyrightReport);
	}

	@SuppressWarnings("unchecked")
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, PurchaseModel model, CorporateUserPendingTaskModel pending) throws Exception {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		String purchaseInstitution;
				
		Map<String, Object> pendingTaskValues = (Map<String, Object>) objectMapper.readValue(pending.getValuesStr(), Class.forName(pending.getModel()));
		if(model != null) {
			purchaseInstitution = model.getPurchaseInstitution().getCode();
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("refNo", model.getReferenceNo());
			reportParams.put("transactionCurrency", model.getTransactionCurrency());
			
			if(model.getChargeTypeAmount1() != null && model.getChargeTypeAmount1().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType1());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency1", model.getChargeTypeCurrency1());
				reportParams.put("chargeType1", serviceCharge.getName());
				reportParams.put("chargeAmount1", df.format(model.getChargeTypeAmount1()));
			}

			if(model.getChargeTypeAmount2() != null && model.getChargeTypeAmount2().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType2());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency2", model.getChargeTypeCurrency2());
				reportParams.put("chargeType2", serviceCharge.getName());
				reportParams.put("chargeAmount2", df.format(model.getChargeTypeAmount2()));
			}
			
			if(model.getChargeTypeAmount3() != null && model.getChargeTypeAmount3().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType3());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency3", model.getChargeTypeCurrency3());
				reportParams.put("chargeType3", serviceCharge.getName());
				reportParams.put("chargeAmount3", df.format(model.getChargeTypeAmount3()));
			}
			
			if(model.getChargeTypeAmount4() != null && model.getChargeTypeAmount4().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType4());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency4", model.getChargeTypeCurrency4());
				reportParams.put("chargeType4", serviceCharge.getName());
				reportParams.put("chargeAmount4", df.format(model.getChargeTypeAmount4()));
			}
			
			if(model.getChargeTypeAmount5() != null && model.getChargeTypeAmount5().compareTo(BigDecimal.ZERO) > 0) {
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType5());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				reportParams.put("chargeCurrency5", model.getChargeTypeCurrency5());
				reportParams.put("chargeType5", serviceCharge.getName());
				reportParams.put("chargeAmount5", df.format(model.getChargeTypeAmount5()));
			}
			
			if(model.getTotalChargeEquivalentAmount().compareTo(BigDecimal.ZERO) > 0)
				reportParams.put("totalCharge", df.format(model.getTotalChargeEquivalentAmount()));
			
			reportParams.put("totalDebited", df.format(model.getTotalDebitedEquivalentAmount()));
			
			reportParams.put("purchaseInstitution", purchaseInstitution);
			reportParams.put("institutionName", model.getPurchaseInstitution().getName());			
			
			if("ISATPRE".equals(purchaseInstitution)) {
				setReportParamIndosat(reportParams, pendingTaskValues);
			}
			
		}
	}

	@SuppressWarnings("unchecked")
	private void setReportParamIndosat(Map<String, Object> reportParams, Map<String, Object> pendingTaskValues) {
		
		if (pendingTaskValues.get("othersInfo") != null) {

			DecimalFormat df = new DecimalFormat(moneyFormat);
			List<Map<String, Object>> infoList = (List<Map<String, Object>>) pendingTaskValues.get("othersInfo");

			for (Map<String, Object> infoMap : infoList) {
				
				if (infoMap.get("key") !=null) {
					if (String.valueOf(infoMap.get("key")).toLowerCase().contains("amount")) {
						reportParams.put(String.valueOf(infoMap.get("key")), df.format(new BigDecimal(String.valueOf(infoMap.get("value")))));
					}else {
						reportParams.put(String.valueOf(infoMap.get("key")), String.valueOf(infoMap.get("value")));
					}
					
				}
				
			}
			
			Map<String, Object> confirmData = (Map<String, Object>)pendingTaskValues.get("confirm_data");
			reportParams.put("title", String.valueOf(confirmData.get("institutionName")).toUpperCase());

		}
		
	}
	
	private void notifyBankEmail(PurchaseModel model, String appCode) {		
		
		try {

			String emails = maintenanceRepo.isSysParamValid(SysParamConstants.BANK_EMAIL).getValue();
			String[] vals = getMultipleEntries(emails);
			if (vals!=null && vals.length>0) {
				Map<String, Object> inputs = prepareInputsForEAI(model, appCode);
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails",Arrays.asList(vals));
				inputs.put("subject", "Suspect Transaction Notification");

				eaiAdapter.invokeService(EAIConstants.SUSPECT_TRANCATION_NOTIFICATION, inputs);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}				
	}

	@Override
	public Map<String, Object> updateTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			//cancelId = pendingTaskId
			String pendingTaskId = (String) map.get("pendingTaskId");
			String status = (String) map.get("status");			
			if(logger.isDebugEnabled()) {
				logger.debug("pendingTaskId : " + pendingTaskId);
			}
			
			Timestamp activityDate = DateUtils.getCurrentTimestamp();
			String isError = "Y";
			
			PurchaseModel purchase = purchaseRepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.YES);
			
			if(status.equals("SUCCESS")) {
				isError = "N";
				pendingTaskRepo.updatePendingTask(purchase.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
				trxStatusService.addTransactionStatus(purchase.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, purchase.getId(), false, null);
				
				// send email to beneficiary only if not execute with failure
				if (ApplicationConstants.YES.equals(purchase.getIsNotifyBen())) {
					notifyBeneficiary(purchase, ApplicationConstants.APP_GPCASHBO);
				}
				
			}else {
				pendingTaskRepo.updatePendingTask(purchase.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				trxStatusService.addTransactionStatus(purchase.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, purchase.getId(), true, purchase.getErrorCode());
			}
			
			pendingTaskRepo.updateErrorTimeoutFlagPendingTask(pendingTaskId, ApplicationConstants.NO);
			
			globalTransactionService.save(purchase.getCorporate().getId(), PurchaseSC.menuCode, 
					purchase.getService().getCode(), purchase.getReferenceNo(), 
					purchase.getId(), purchase.getTransactionCurrency(), purchase.getTotalChargeEquivalentAmount(), 
					purchase.getTransactionAmount(), 
					purchase.getTotalDebitedEquivalentAmount(), isError);
			
			notifyTrxUsersHistory(purchase, ApplicationConstants.APP_GPCASHBO);
			
			
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
}
