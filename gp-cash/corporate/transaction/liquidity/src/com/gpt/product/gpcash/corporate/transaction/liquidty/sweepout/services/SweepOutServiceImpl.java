package com.gpt.product.gpcash.corporate.transaction.liquidty.sweepout.services;

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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper;
import com.gpt.component.common.spring.invoker.SpringBeanInvokerHelper.SpringBeanInvokerData;
import com.gpt.component.common.spring.invoker.spi.ISpringBeanInvoker;
import com.gpt.component.common.utils.PagingUtils;
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
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporatecharge.services.CorporateChargeService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.repository.CorporateUserPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
import com.gpt.product.gpcash.corporate.transaction.liquidty.constants.LiquidityConstants;
import com.gpt.product.gpcash.corporate.transaction.liquidty.sweepout.model.SweepOutDetailModel;
import com.gpt.product.gpcash.corporate.transaction.liquidty.sweepout.model.SweepOutModel;
import com.gpt.product.gpcash.corporate.transaction.liquidty.sweepout.repository.SweepOutRepository;
import com.gpt.product.gpcash.corporate.transaction.utils.InstructionModeUtils;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionActivityType;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;
import com.gpt.product.gpcash.corporate.transactionstatus.model.TransactionStatusModel;
import com.gpt.product.gpcash.corporate.transactionstatus.repository.TransactionStatusRepository;
import com.gpt.product.gpcash.corporate.transactionstatus.services.TransactionStatusService;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.service.model.ServiceModel;
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
public class SweepOutServiceImpl implements SweepOutService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;
	
	@Value("${gpcash.report.folder}")
	private String reportFolder;
	
	@Value("${gpcash.report.bank.logo}")
	private String bankLogo;
	
	@Value("${gpcash.report.money.format:#,###.00}")
	private String moneyFormat;
	
	@Value("${gpcash.trxstatus.download.path}")
	private String pathDownload;
	
	@Autowired
	private SweepOutRepository sweepOutRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;

	@Autowired
	private CorporateChargeService corporateChargeService;

	@Autowired
	private TransactionValidationService transactionValidationService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ErrorMappingRepository errorMappingRepo;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
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
	
	@Autowired
	private MessageSource message;
	
	@Autowired
	private ObjectMapper objectMapper;
	
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
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			//------------------------------------------------------
			
			String mainAccount = corporateUtilsRepo.getCorporateAccountGroupRepo().findAccountNoByAccountGroupDetailId(corporateId, (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID));
			
			List<Map<String, Object>> subAccountList = (ArrayList) map.get("subAccountList");
			
			List<String> subAccountChecking = new ArrayList<>();
			BigDecimal totalPercent = BigDecimal.ZERO;
			
			for(Map<String, Object> subAccountMap : subAccountList) {
				BigDecimal remainingAmount = (BigDecimal) subAccountMap.get("remainingAmount");
				
				if(remainingAmount.compareTo(BigDecimal.ZERO) == 0)
					throw new BusinessException("GPT-LQ-0100003");
				
				if (((String) subAccountMap.get("remaining_amount_type")).equals(LiquidityConstants.SWEEP_AMOUNT_TYPE_PERCENTAGE)) {
					totalPercent = totalPercent.add(remainingAmount);
					if(totalPercent.compareTo(new BigDecimal(100)) > 0)
						throw new BusinessException("GPT-LQ-0100005");
				}
				
				String subAccount = corporateUtilsRepo.getCorporateAccountGroupRepo().findAccountNoByAccountGroupDetailId(corporateId, (String) subAccountMap.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID));
				
				if(subAccount.equals(mainAccount)) {
					throw new BusinessException("GPT-LQ-0100001");
				}
				
				if(subAccountChecking.contains(subAccount)) {
					throw new BusinessException("GPT-LQ-0100002", new String[] {subAccount});
				}
				
				subAccountChecking.add(subAccount);
			}

			// TODO implement calculate equivalent amount in future if implement
			// cross currency transaction
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private CorporateUserPendingTaskVO setCorporateUserPendingTaskVO(Map<String, Object> map) throws Exception {
		String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		CorporateUserModel maker = corporateUtilsRepo.isCorporateUserValid(createdBy);
		String userGroupId = maker.getCorporateUserGroup().getId();

		String accountGroupDetailId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
		String uniqueKey = createdBy.concat(ApplicationConstants.DELIMITER_PIPE).concat(accountGroupDetailId)
				.concat(ApplicationConstants.DELIMITER_PIPE).concat(String.valueOf((DateUtils.getCurrentTimestamp().getTime())));

		String instructionMode = (String) map.get(ApplicationConstants.INSTRUCTION_MODE);
		String sessionTime = (String) map.get("sessionTime");
		Timestamp instructionDate = (Timestamp) map.get(ApplicationConstants.INSTRUCTION_DATE);
		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		
		CorporateUserPendingTaskVO vo = new CorporateUserPendingTaskVO();
		vo.setUniqueKey(uniqueKey);
		vo.setCreatedBy(createdBy);
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setTransactionServiceCode(transactionServiceCode);
		vo.setService("SweepOutSC");
		vo.setCorporateId(corporateId);
		vo.setTransactionAmount(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
		vo.setTransactionCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		vo.setSourceAccountGroupDetailId(accountGroupDetailId);
		vo.setSessionTime(sessionTime);
		
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
		
		
		//------------------------------------------------------------------
		
		vo.setInstructionMode(instructionMode);
		
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
	private SweepOutModel setMapToModel(SweepOutModel sweepOut, Map<String, Object> map,
			boolean isNew, CorporateUserPendingTaskVO vo) throws Exception {
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();

		String isAutoSweepBack = (String) map.get("isAutoSweepBack");
		String sweepAmountType = (String) map.get("sweepAmountType");
		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		
		String instructionMode = (String) map.get("instructionMode");
		
		Timestamp instructionDate = vo.getInstructionDate();
		if(ApplicationConstants.SI_RECURRING.equals(vo.getInstructionMode())) {
			//override instructionDate to today timestamp
			Timestamp recurringInstructionDate = InstructionModeUtils.getStandingInstructionDate(vo.getRecurringParamType(), vo.getRecurringParam(), 
					DateUtils.getCurrentTimestamp());
			
			instructionDate = DateUtils.getInstructionDateBySessionTime(vo.getSessionTime(), recurringInstructionDate);
		}

		// set ID
		sweepOut.setId(vo.getId());

		// set transaction information
		sweepOut.setReferenceNo(vo.getReferenceNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		sweepOut.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		sweepOut.setCorporate(corpModel);
		
		sweepOut.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE)); 
		sweepOut.setApplication(application);

		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		sweepOut.setSourceAccount(sourceAccountModel);
		sweepOut.setSweepCurrency((String) map.get(ApplicationConstants.TRANS_CURRENCY));
		
		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		sweepOut.setService(service);
		
		//remove if in the future can sweep back
		if(isAutoSweepBack == null) {
			isAutoSweepBack = ApplicationConstants.NO;
		}
		
		sweepOut.setIsAutoSweepBack(isAutoSweepBack);
		sweepOut.setSweepAmountType(sweepAmountType);
		
		//set sweep detail
		List<Map<String, Object>> subAccountList = (ArrayList<Map<String,Object>>) map.get("subAccountList");
		
		List<SweepOutDetailModel> sweepDetailList = new LinkedList<>();
		for(Map<String, Object> subAccountMap : subAccountList) {
			SweepOutDetailModel sweepDetail = new SweepOutDetailModel();
			
			CorporateAccountGroupDetailModel gdm = corporateUtilsRepo.isCorporateAccountGroupDetailIdValid(vo.getCorporateId(), 
					(String) subAccountMap.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID));		
			
			//set debit info
			AccountModel subAccount = gdm.getCorporateAccount().getAccount();
			
			sweepDetail.setSubAccount(subAccount);
			sweepDetail.setRemainingAmount(new BigDecimal(String.valueOf(subAccountMap.get("remainingAmount"))));
			sweepDetail.setDescription((String) subAccountMap.get("description"));
			sweepDetail.setPriority((Integer) subAccountMap.get("priority"));
			sweepDetail.setSweepOut(sweepOut);
			sweepDetailList.add(sweepDetail);
		}
		sweepOut.setSweepOutDetail(sweepDetailList);
		//----------------------------

		setInstructionMode(sweepOut, instructionMode, instructionDate, map);
		
		setCharge(sweepOut, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		

		return sweepOut;
	}
	
	private void setCharge(SweepOutModel sweepOut, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				sweepOut.setChargeType1(chargeId);
				sweepOut.setChargeTypeAmount1(value);
				sweepOut.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				sweepOut.setChargeType2(chargeId);
				sweepOut.setChargeTypeAmount2(value);
				sweepOut.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				sweepOut.setChargeType3(chargeId);
				sweepOut.setChargeTypeAmount3(value);
				sweepOut.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				sweepOut.setChargeType4(chargeId);
				sweepOut.setChargeTypeAmount4(value);
				sweepOut.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				sweepOut.setChargeType5(chargeId);
				sweepOut.setChargeTypeAmount5(value);
				sweepOut.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}

	private void setInstructionMode(SweepOutModel sweepOut, String instructionMode, Timestamp instructionDate, 
			Map<String, Object> map) throws Exception {
		
		
		// set instruction mode
		sweepOut.setInstructionMode(instructionMode);

		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			sweepOut
					.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		} else if (instructionMode.equals(ApplicationConstants.SI_FUTURE_DATE)) {
			sweepOut.setInstructionDate(instructionDate);
		} else if (instructionMode.equals(ApplicationConstants.SI_RECURRING)) {
			sweepOut.setRecurringParamType((String) map.get("recurringParamType"));
			sweepOut.setRecurringParam((Integer) map.get("recurringParam"));
			sweepOut.setRecurringStartDate(instructionDate);
			sweepOut.setRecurringEndDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("recurringEndDate")));
			sweepOut.setInstructionDate(instructionDate);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				SweepOutModel sweepOut = new SweepOutModel();
				setMapToModel(sweepOut, map, true, vo);

				if(ApplicationConstants.SI_IMMEDIATE.equals(sweepOut.getInstructionMode())) {
					saveSweepOut(sweepOut, vo.getCreatedBy(), ApplicationConstants.YES);
					
					doTransfer(sweepOut, (String)map.get(ApplicationConstants.APP_CODE));
					vo.setErrorCode(sweepOut.getErrorCode());
					vo.setIsError(sweepOut.getIsError());
				} else {
					saveSweepOut(sweepOut, vo.getCreatedBy(), ApplicationConstants.NO);
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

	private void saveSweepOut(SweepOutModel sweepOut, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		sweepOut.setIsProcessed(isProcessed);
		sweepOut.setCreatedDate(DateUtils.getCurrentTimestamp());
		sweepOut.setCreatedBy(createdBy);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(sweepOut.getPendingTaskId() == null)
			sweepOut.setPendingTaskId(sweepOut.getId());

		sweepOutRepo.persist(sweepOut);
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			checkCustomValidation(map);
			
			resultMap.putAll(corporateChargeService.getCorporateChargesPerRecord((String) map.get(ApplicationConstants.APP_CODE),
					(String) map.get(ApplicationConstants.TRANS_SERVICE_CODE),
					(String) map.get(ApplicationConstants.LOGIN_CORP_ID),Long.parseLong(String.valueOf(map.get("totalRecord")))));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void doTransfer(SweepOutModel model, String appCode) throws Exception {
		boolean isRollback = false;
		
		try {
			//get total sweep amount
			calculateTotalSweep(model);
			
			BigDecimal totalCharge = BigDecimal.ZERO;
			BigDecimal totalDebit = BigDecimal.ZERO;
			BigDecimal totalSweepAmount = BigDecimal.ZERO;
			
			//update transaction limit with totalSweepAmount
			transactionValidationService.updateTransactionLimit(model.getCorporate().getId(), 
					model.getService().getCode(), 
					model.getSourceAccount().getCurrency().getCode(), 
					model.getSweepCurrency(), 
					model.getCorporateUserGroup().getId(), 
					model.getTotalSweepAmount(),
					model.getApplication().getCode());
	
			//sort by priority if inputted
			List<SweepOutDetailModel> detaillList = model.getSweepOutDetail();
			
	        Collections.sort(detaillList, new ComparableByPriority());
	        
			//loop sweep in detail to transfer from sub account to main account
			int isSweepDetailErrorCounter = 0;
			for(SweepOutDetailModel detail : detaillList) {
				boolean isSweepDetailError = false;
				String errorMsg = "";
				
				BigDecimal charge = getCharge(model);
				
				
				try {
					detail.setCreatedDate(DateUtils.getCurrentTimestamp());
					if(detail.getSweepAmount().compareTo(BigDecimal.ZERO) > 0) {
						
						//sweep amount minus charge, only for type percentage
						if (model.getSweepAmountType().equals(LiquidityConstants.SWEEP_AMOUNT_TYPE_PERCENTAGE)) {
							BigDecimal sweepAmount = BigDecimal.ZERO;
							sweepAmount = detail.getSweepAmount().subtract(charge);
							detail.setSweepAmount(sweepAmount);						
						}
						
						//for main account after subtract with charge
						Map<String, Object> inputs = prepareInputsForEAI(model, detail, appCode);
						eaiAdapter.invokeService(EAIConstants.LIQUIDITY_TRANSFER, inputs);
						
						//for charge per sub account
						if (charge.compareTo(BigDecimal.ZERO) > 0) {
							Map<String, Object> inputsCharge = prepareInputsForEAICharge(model, detail, appCode,charge);
							eaiAdapter.invokeService(EAIConstants.LIQUIDITY_TRANSFER, inputsCharge);
						}
						//only add success sweep
						totalCharge = totalCharge.add(charge);
						totalSweepAmount = totalSweepAmount.add(detail.getSweepAmount());
						
						//update detail status = success
						detail.setStatus("GPT-0100130");
						detail.setIsError(ApplicationConstants.NO);
						detail.setSweepAmount(detail.getSweepAmount());
						
					} else {
						errorMsg = "GPT-LQ-0100004";
						isSweepDetailError = true;
						isSweepDetailErrorCounter++;
					}
					
					
				} catch (BusinessException e) {
					errorMsg = e.getMessage();
					if (errorMsg!=null && errorMsg.startsWith("EAI-")) {
						ErrorMappingModel errorMappingModel = errorMappingRepo.findOne(e.getMessage());
						isSweepDetailError = true;
						isSweepDetailErrorCounter++;
						
						// Either no error mapping or no rollback...
						
						//jika transaksi gagal tetapi tidak di rollback maka kena error execute with failure
						errorMsg = errorMappingModel == null ? errorMsg : errorMappingModel.getCode();
					} else {
						isSweepDetailError = true;
						isSweepDetailErrorCounter++;
						logger.debug(e.getMessage(), e);
					}
				} catch (Exception e) {
					isSweepDetailError = true;
					isSweepDetailErrorCounter++;
					logger.debug(e.getMessage(), e);
				}
				
				if(isSweepDetailError) {
					//update detail status = failed
					detail.setStatus("GPT-0100129");
					detail.setIsError(ApplicationConstants.YES);
					detail.setErrorCode(errorMsg);
					detail.setSweepAmount(detail.getSweepAmount());
					
					// reverse usage for each failed detail
					try {
						reverseLimit(model, detail);
					} catch(Exception e) {
						logger.error("Failed to reverse the usage "+e.getMessage(),e);
					}
				}
				
				//--------------------------------------------------------------------------
			}
			
			totalDebit = totalDebit.add(totalCharge).add(totalSweepAmount);
			model.setTotalChargeEquivalentAmount(totalCharge);
			model.setTotalSweepAmount(totalSweepAmount);
			model.setTotalDebitedEquivalentAmount(totalDebit);
			model.setTotalError(isSweepDetailErrorCounter);
			
			//update model status
			if(isSweepDetailErrorCounter > 0) {
				
				model.setStatus("GPT-0100190");
				model.setErrorCode(TransactionStatus.EXECUTE_PARTIAL_SUCCESS.name());
				model.setIsError(ApplicationConstants.YES);
				if(isSweepDetailErrorCounter == model.getSweepOutDetail().size()) {
					model.setStatus("GPT-0100129");
					model.setErrorCode(TransactionStatus.EXECUTE_FAIL.name());
				}
			} else {
				model.setStatus("GPT-0100130");
				model.setIsError(ApplicationConstants.NO);
			}
			//---------------------------------------
		} catch (BusinessException e) {
			String errorMsg = e.getMessage();
			if (errorMsg!=null && errorMsg.startsWith("EAI-")) {
				ErrorMappingModel errorMappingModel = errorMappingRepo.findOne(e.getMessage());
				
				if (errorMappingModel!=null && ApplicationConstants.YES.equals(errorMappingModel.getRollbackFlag())) {
					isRollback = true;
					throw e;
				}
				
				// Either no error mapping or no rollback...
				
				//jika transaksi gagal tetapi tidak di rollback maka kena error execute with failure
				model.setStatus("GPT-0100129");
				model.setIsError(ApplicationConstants.YES);
				model.setErrorCode(errorMappingModel == null ? errorMsg : errorMappingModel.getCode());
			} else {
				throw e;
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		} finally {
			//save transaction log
			globalTransactionService.save(model.getCorporate().getId(), SweepOutSC.menuCode, 
					model.getService().getCode(), model.getReferenceNo(), 
					model.getId(), model.getSweepCurrency(), model.getTotalChargeEquivalentAmount(), 
					model.getTotalSweepAmount(), 
					model.getTotalDebitedEquivalentAmount(), model.getIsError());
			//----------------------------------
			
			try {
				//TODO pending notification email, wait susi
				// send email to corporate email
//					notifyCorporate(model, appCode);				
				
				// send email to transaction user history
				notifyTrxUsersHistory(model, appCode);
				
			} catch(Exception e) {
				// ignore any error, just in case something bad happens
			}
		}
	}
	
	private void notifyTrxUsersHistory(SweepOutModel model, String appCode) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getPendingTaskId());
			if (emails.size() > 0) {
				Map<String, Object> inputs = new HashMap<>();
				AccountModel mainAccount = model.getSourceAccount();
				
				IDMMenuModel menu = idmRepo.isIDMMenuValid(SweepOutSC.menuCode);
				
				inputs.put("mainAccountNo", mainAccount.getAccountNo());
				inputs.put("mainAccountName", mainAccount.getAccountName());
				inputs.put("trxCurrencyCode", mainAccount.getCurrency().getCode());
				inputs.put("trxAmount", model.getTotalDebitedEquivalentAmount());
				inputs.put("totalRecord", model.getSweepOutDetail().size());
				inputs.put("totalError", model.getTotalError());
				inputs.put("menuName", menu.getName());
				inputs.put("refNo", model.getReferenceNo());
				
				inputs.put("instructionMode", model.getInstructionMode());
				inputs.put("instructionDate", model.getInstructionDate());
				inputs.put("status", model.getStatus());
				inputs.put("errorCode", model.getErrorCode());
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails", emails);
				inputs.put("subject", menu.getName().concat(" Notification"));
				
				eaiAdapter.invokeService(EAIConstants.USER_LIQUIDITY_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private BigDecimal getCharge(SweepOutModel sweepOut) {
		BigDecimal charge = BigDecimal.ZERO;

		if(sweepOut.getChargeTypeAmount1() != null && BigDecimal.ZERO.compareTo(sweepOut.getChargeTypeAmount1()) < 0) {
			charge = charge.add(sweepOut.getChargeTypeAmount1());
		}
		
		if(sweepOut.getChargeTypeAmount2() != null && BigDecimal.ZERO.compareTo(sweepOut.getChargeTypeAmount2()) < 0) {
			charge = charge.add(sweepOut.getChargeTypeAmount2());
		}
		
		if(sweepOut.getChargeTypeAmount3() != null && BigDecimal.ZERO.compareTo(sweepOut.getChargeTypeAmount3()) < 0) {
			charge = charge.add(sweepOut.getChargeTypeAmount3());
		}
		
		if(sweepOut.getChargeTypeAmount4() != null && BigDecimal.ZERO.compareTo(sweepOut.getChargeTypeAmount4()) < 0) {
			charge = charge.add(sweepOut.getChargeTypeAmount4());
		}
		
		if(sweepOut.getChargeTypeAmount5() != null && BigDecimal.ZERO.compareTo(sweepOut.getChargeTypeAmount5()) < 0) {
			charge = charge.add(sweepOut.getChargeTypeAmount5());
		}
		
		return charge;
	}
	

	
	private void reverseLimit(SweepOutModel model, SweepOutDetailModel detail) {
		try {
			transactionValidationService.reverseUpdateTransactionLimit(model.getCorporate().getId(), 
					model.getService().getCode(), 
					model.getSourceAccount().getCurrency().getCode(), 
					model.getSweepCurrency(), 
					model.getCorporateUserGroup().getId(), 
					detail.getSweepAmount(),
					model.getApplication().getCode());
		} catch(Exception e) {
			logger.error("Failed to reverse the usage "+e.getMessage(),e);
		}
	}
	
	private void calculateTotalSweep(SweepOutModel model) throws BusinessException, ApplicationException{
		BigDecimal totalSweepAmount = BigDecimal.ZERO;
		String sweepAmountType = model.getSweepAmountType();
		
		if(sweepAmountType.equals(LiquidityConstants.SWEEP_AMOUNT_TYPE_FIXED)) {
			for(SweepOutDetailModel detail : model.getSweepOutDetail()) {
				//set sweep amount
				BigDecimal remainingAmount = detail.getRemainingAmount();
				
				if(logger.isDebugEnabled()) {
					logger.debug("\n\n\n\n\n");
					logger.debug("accountNo         : " + detail.getSubAccount().getAccountNo());
					logger.debug("remainingAmount   : " + remainingAmount);
					logger.debug("sourceAccount : " + model.getSourceAccount().getAccountNo());
					logger.debug("sweepAmount       : " + remainingAmount);
					logger.debug("\n\n\n\n\n");
				}
				
				detail.setSweepAmount(remainingAmount);
			
				//add to total sweep amount
				totalSweepAmount = totalSweepAmount.add(remainingAmount);
			}
		} else if (sweepAmountType.equals(LiquidityConstants.SWEEP_AMOUNT_TYPE_PERCENTAGE)) {
			for(SweepOutDetailModel detail : model.getSweepOutDetail()) {
				//set sweep amount
				BigDecimal remainingPercentage = detail.getRemainingAmount();
				BigDecimal sourceAccountBalance = globalTransactionService.checkBalance(model.getSourceAccount());
				BigDecimal sweepAmount = BigDecimal.ZERO;
				
				if(sourceAccountBalance.compareTo(BigDecimal.ZERO) > 0) {
					sweepAmount = sourceAccountBalance.multiply(remainingPercentage).divide(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_DOWN);
				}
				
				if(logger.isDebugEnabled()) {
					logger.debug("\n\n\n\n\n");
					logger.debug("accountNo           : " + detail.getSubAccount().getAccountNo());
					logger.debug("remainingPercentage : " + remainingPercentage);
					logger.debug("sourceAccount : " + model.getSourceAccount().getAccountNo());
					logger.debug("sourceAccountBalance : " + sourceAccountBalance);
					logger.debug("sweepAmount         : " + sweepAmount);
					logger.debug("\n\n\n\n\n");
				}
				
				detail.setSweepAmount(sweepAmount);
				//add to total sweep amount
				totalSweepAmount = totalSweepAmount.add(sweepAmount);
			}
		}
		model.setTotalSweepAmount(totalSweepAmount);
	}
	
	private Map<String, Object> prepareInputsForEAICharge(SweepOutModel model, SweepOutDetailModel detail, String appCode, BigDecimal totalCharge) throws Exception {
		Map<String, Object> inputs = new HashMap<>();
		
		AccountModel mainAccount = model.getSourceAccount();
		String chargeAccount = maintenanceRepo.isSysParamValid(SysParamConstants.SWEEP_CHARGE_ACCOUNT).getValue();
		
		inputs.put("channel", appCode);
		inputs.put("channelRefNo", detail.getId().concat("_CHARGE"));
		
		inputs.put("debitAccountNo", mainAccount.getAccountNo());
		inputs.put("debitAccountName", mainAccount.getAccountName());
		inputs.put("debitAccountType", mainAccount.getAccountType().getCode());
		inputs.put("debitAccountCurrencyCode", mainAccount.getCurrency().getCode());
		
		inputs.put("benAccountNo", chargeAccount);
		inputs.put("benAccountCurrencyCode", mainAccount.getCurrency().getCode());
		
		inputs.put("trxCurrencyCode", model.getSweepCurrency());
		inputs.put("trxAmount", totalCharge);
		
		inputs.put("serviceCode", model.getService().getCode());
		inputs.put("serviceName", model.getService().getName());

		inputs.put("remark1", LiquidityConstants.NARATION_SWEEP_OUT + " " + mainAccount.getAccountNo());

		inputs.put("refNo", model.getReferenceNo());
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	private Map<String, Object> prepareInputsForEAI(SweepOutModel model, SweepOutDetailModel detail, String appCode) {
		Map<String, Object> inputs = new HashMap<>();
		
		AccountModel mainAccount = model.getSourceAccount();
		AccountModel subAccount = detail.getSubAccount();
		
		inputs.put("channel", appCode);
		inputs.put("channelRefNo", detail.getId());
		
		inputs.put("debitAccountNo", mainAccount.getAccountNo());
		inputs.put("debitAccountName", mainAccount.getAccountName());
		inputs.put("debitAccountType", mainAccount.getAccountType().getCode());
		inputs.put("debitAccountCurrencyCode", mainAccount.getCurrency().getCode());
		
		inputs.put("benAccountNo", subAccount.getAccountNo());
		inputs.put("benAccountName", subAccount.getAccountName());
		inputs.put("benAccountCurrencyCode", subAccount.getCurrency().getCode());
		
		inputs.put("trxCurrencyCode", model.getSweepCurrency());
		inputs.put("trxAmount", detail.getSweepAmount());
		
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
		inputs.put("serviceName", model.getService().getName());

		inputs.put("remark1", LiquidityConstants.NARATION_SWEEP_OUT + " " + mainAccount.getAccountNo());

		inputs.put("refNo", model.getReferenceNo());
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
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
				
				List<SweepOutModel> sweepOutList = sweepOutRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_FUTURE_DATE, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(sweepOutList);
				
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
				
				List<SweepOutModel> sweepOutList = sweepOutRepo.findTransactionForSchedulerByInstructionMode(ApplicationConstants.SI_RECURRING, 
						new Timestamp(calFrom.getTimeInMillis()), 
						new Timestamp(calTo.getTimeInMillis()));
				
				executeAllBatch(sweepOutList);
				
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void executeAllBatch(List<SweepOutModel> list) throws Exception {
		List<SpringBeanInvokerData<String>> jobs = new ArrayList<>();
		for(SweepOutModel model : list) {
			jobs.add(new SpringBeanInvokerData<>(model.getId(), "SweepOutService", "executeBatchTransaction", new Object[] {model}, jobTimeout));
		}

		SpringBeanInvokerHelper.invokeAndForget(invoker, jobs, invokerTimeout, concurrencies);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void executeBatchTransaction(SweepOutModel sweepOut) {
		if(logger.isDebugEnabled())
			logger.debug("Process  : " + sweepOut.getId() + " - " + sweepOut.getReferenceNo());
		
		boolean success = false;
		String errorCode = null;
		Timestamp activityDate = DateUtils.getCurrentTimestamp();
		try {			
			sweepOut.setIsProcessed(ApplicationConstants.YES);
			sweepOutRepo.save(sweepOut);
		
			//avoid lazy when get details
			sweepOut = sweepOutRepo.findOne(sweepOut.getId());
			
			doTransfer(sweepOut, ApplicationConstants.APP_GPCASHBO);
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
			boolean isRecurring = false;
			try {
				if(ApplicationConstants.SI_RECURRING.equals(sweepOut.getInstructionMode())) {					
					Timestamp newInstructionDate = InstructionModeUtils.getStandingInstructionDate(sweepOut.getRecurringParamType(), sweepOut.getRecurringParam(), sweepOut.getInstructionDate());
					
					Calendar calNewInstructionDate = DateUtils.getEarliestDate(newInstructionDate);
					Calendar calExpired = DateUtils.getEarliestDate(sweepOut.getRecurringEndDate());
					
					if(logger.isDebugEnabled()) {
						logger.debug("Recurring new instruction date : " + calNewInstructionDate.getTime());
						logger.debug("Recurring expired date : " + calExpired.getTime());
					}
					
					isRecurring = true;
					
					if(calNewInstructionDate.compareTo(calExpired) <= 0) {
						Calendar oldInstructionDate = Calendar.getInstance();
						oldInstructionDate.setTime(sweepOut.getInstructionDate());
						
						//get session time from old recurring
						calNewInstructionDate.set(Calendar.HOUR_OF_DAY, oldInstructionDate.get(Calendar.HOUR_OF_DAY));
						calNewInstructionDate.set(Calendar.MINUTE, oldInstructionDate.get(Calendar.MINUTE));
						saveNewTransactionForRecurring(sweepOut, new Timestamp(calNewInstructionDate.getTimeInMillis()));
					} else {
						isExpired = true;
					}
					
				}
			} catch(Exception e) {
				logger.error("Failed sweepOut id : " + sweepOut.getId());
			}
			
			if (success) {
				if (ApplicationConstants.YES.equals(sweepOut.getIsError())) {
					pendingTaskRepo.updatePendingTask(sweepOut.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				
					trxStatusService.addTransactionStatus(sweepOut.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, sweepOut.getId(), true, sweepOut.getErrorCode());
				} else {
					if (isRecurring) {
						pendingTaskRepo.updatePendingTask(sweepOut.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);
					} else {
						pendingTaskRepo.updatePendingTask(sweepOut.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS);
					}
					
					trxStatusService.addTransactionStatus(sweepOut.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, sweepOut.getId(), false, null);
				}
			} else {
				if (isRecurring) {
					pendingTaskRepo.updatePendingTask(sweepOut.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);
				} else {
					pendingTaskRepo.updatePendingTask(sweepOut.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, activityDate, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL);
				}
				
				trxStatusService.addTransactionStatus(sweepOut.getPendingTaskId(),  activityDate, TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, sweepOut.getId(), true, errorCode);
			}
			
			if (isExpired) {
				pendingTaskRepo.updatePendingTask(sweepOut.getPendingTaskId(), ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED);
				
				//insert into trx status EXPIRED
				trxStatusService.addTransactionStatus(sweepOut.getPendingTaskId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXPIRED, null, true, null);
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void saveNewTransactionForRecurring(SweepOutModel model, Timestamp newInstructionDate) {
		try {
			SweepOutModel newModel = new SweepOutModel();
			newModel.setId(Helper.generateHibernateUUIDGenerator());
			newModel.setReferenceNo(model.getReferenceNo());
			newModel.setMenu(model.getMenu());
			newModel.setCorporate(model.getCorporate());
			newModel.setCorporateUserGroup(model.getCorporateUserGroup());
			newModel.setApplication(model.getApplication());
			newModel.setSourceAccount(model.getSourceAccount());
			newModel.setSweepCurrency(model.getSweepCurrency());
			newModel.setSweepAmountType(model.getSweepAmountType());
			
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
//			BigDecimal totalTransaction = model.getTransactionAmount().add(totalChargeFromTable);
			
			newModel.setTotalChargeEquivalentAmount(totalChargeFromTable);
			newModel.setTotalDebitedEquivalentAmount(BigDecimal.ZERO);
			
			setCharge(newModel, (List<Map<String, Object>>)chargesInfo.get(ApplicationConstants.TRANS_CHARGE_LIST));
			
			newModel.setService(model.getService());

			//set new instructionDate for recurring
			newModel.setInstructionMode(model.getInstructionMode());
			newModel.setRecurringParamType(model.getRecurringParamType());
			newModel.setRecurringParam(model.getRecurringParam());
			newModel.setRecurringStartDate(model.getRecurringStartDate());
			newModel.setRecurringEndDate(model.getRecurringEndDate());
			newModel.setInstructionDate(newInstructionDate);
			
			List<SweepOutDetailModel> sweepDetailList = new LinkedList<>();
			for(SweepOutDetailModel detail : model.getSweepOutDetail()) {
				SweepOutDetailModel newDetail = new SweepOutDetailModel();
				
				newDetail.setSubAccount(detail.getSubAccount());
				newDetail.setRemainingAmount(detail.getRemainingAmount());
				newDetail.setDescription(detail.getDescription());
				newDetail.setPriority(detail.getPriority());
				newDetail.setSweepOut(newModel);
				sweepDetailList.add(newDetail);
			}
			newModel.setSweepOutDetail(sweepDetailList);
			
			
			saveSweepOut(newModel, ApplicationConstants.CREATED_BY_SYSTEM, ApplicationConstants.NO);
			
			//insert into trx status PENDING_EXECUTE with new model id
			Timestamp now = DateUtils.getCurrentTimestamp();
			pendingTaskRepo.updatePendingTask(makerPendingTaskIdForRecurring, ApplicationConstants.WF_STATUS_RELEASED, DateUtils.getCurrentTimestamp(), ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE);
			
			trxStatusService.addTransactionStatus(makerPendingTaskIdForRecurring, now, TransactionActivityType.EXECUTE, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.PENDING_EXECUTE, null, false, null);
		} catch(Exception e) {
			// ignore any error to avoid transaction rollback caused by saving new transaction
			logger.error("Failed save SweepOut recurring with id : " + model.getId());
		}
	}
	
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException {

Map<String, Object> resultMap = new HashMap<>();
		
		try {
			String transactionStatusId = (String) map.get("executedId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
				SweepOutModel model = sweepOutRepo.findOne(trxStatus.getEaiRefNo());
				
				if (model==null) {
//					throw new BusinessException("GPT-0100001");
					CorporateUserPendingTaskModel modelPT = pendingTaskRepo.findOne(trxStatus.getEaiRefNo());
					resultMap.put("executedResult", globalTransactionService.prepareDetailTransactionMapFromPendingTask(modelPT, trxStatus));
				} else {
				
					Page<SweepOutDetailModel> result = sweepOutRepo.searchSweepOutDetailById(model.getId(), 
							PagingUtils.createPageRequest(map));
	
					List<Map<String, Object>> details = setSweepDetailModelToMap(model, result.getContent());
					
					PagingUtils.setPagingInfo(resultMap, result);
					
					resultMap.put("executedResult", details);
				}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> cancelTransactionWF(Map<String, Object> map)
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
	
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> setSweepDetailModelToMap(SweepOutModel sweepModel, List<SweepOutDetailModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (SweepOutDetailModel dtlModel : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("executedDate", dtlModel.getCreatedDate());
			map.put("systemReferenceNo", sweepModel.getId());
			map.put("debitAccount", sweepModel.getSourceAccount().getAccountNo());
			map.put("debitAccountName", sweepModel.getSourceAccount().getAccountName());
			map.put("creditAccount", dtlModel.getSubAccount().getAccountNo());
			map.put("creditAccountName", dtlModel.getSubAccount().getAccountName());
			
			//TODO diganti jika telah implement rate
			map.put("transactionCurrency", sweepModel.getSweepCurrency());
			map.put("transactionAmount", dtlModel.getSweepAmount());
				
			map.put("debitAccountCurrency", sweepModel.getSourceAccount().getCurrency().getName());
			map.put("debitTransactionCurrency", sweepModel.getSweepCurrency());
			map.put("debitExchangeRate", ApplicationConstants.ZERO);
			map.put("debitEquivalentAmount", dtlModel.getSweepAmount());
				
			map.put("creditTransactionCurrency", sweepModel.getSweepCurrency());
			map.put("creditEquivalentAmount", dtlModel.getSweepAmount());
			map.put("creditExchangeRate", ApplicationConstants.ZERO);
			
			
			//--------------------------------------------
			
			//TODO diganti jika telah implement periodical charges
			map.put("chargeAccount", ApplicationConstants.EMPTY_STRING);
			
			LinkedList<Map<String, Object>> chargeList = new LinkedList<>();
			Map<String, Object> chargeMap = null;
			CorporateUserPendingTaskModel pt = pendingTaskRepo.findByReferenceNo(sweepModel.getReferenceNo());
			
			String strValues = pt.getValuesStr();
			if (strValues != null) {
				try {
					Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(strValues, Class.forName(pt.getModel()));
					List<Map<String,Object>> listCharge = (List<Map<String,Object>>)valueMap.get("chargeList");
					
					for (int i = 0; i < listCharge.size(); i++) {
						Map<String, Object> mapCharge = listCharge.get(i);
						if (sweepModel.getChargeType1() != null && sweepModel.getChargeType1().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency1());
							chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount1());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						if (sweepModel.getChargeType2() != null && sweepModel.getChargeType2().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency2());
							chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount2());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						if (sweepModel.getChargeType3() != null && sweepModel.getChargeType3().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency3());
							chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount3());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						if (sweepModel.getChargeType4() != null && sweepModel.getChargeType4().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency4());
							chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount4());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						if (sweepModel.getChargeType5() != null && sweepModel.getChargeType5().equals(mapCharge.get("id"))) {
							chargeMap = new HashMap<>();
							chargeMap.put("chargeType", mapCharge.get("serviceChargeName"));
							chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency5());
							chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount5());
							chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
							chargeList.add(chargeMap);
							continue;
						}
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			/*if(sweepModel.getChargeType1() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(sweepModel.getChargeType1());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency1());
				chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount1());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(sweepModel.getChargeType2() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(sweepModel.getChargeType2());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency2());
				chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount2());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(sweepModel.getChargeType3() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(sweepModel.getChargeType3());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency3());
				chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount3());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(sweepModel.getChargeType4() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(sweepModel.getChargeType4());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency4());
				chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount4());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}
			
			if(sweepModel.getChargeType5() != null) {
				chargeMap = new HashMap<>();
				CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(sweepModel.getChargeType5());
				ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
				chargeMap.put("chargeType", serviceCharge.getName());
				chargeMap.put("chargeCurrency", sweepModel.getChargeTypeCurrency5());
				chargeMap.put("chargeEquivalentAmount", sweepModel.getChargeTypeAmount5());
				chargeMap.put("chargeExchangeRate", ApplicationConstants.ZERO);
				chargeList.add(chargeMap);
			}*/
			
			map.put("chargeList", chargeList);			
			
			String status = null;
			if (ApplicationConstants.NO.equals(dtlModel.getIsError()))
				status = "SUCCESS";
			else
				status = "FAILED";

			map.put("status", status);
			
			String errorCode = dtlModel.getErrorCode();
			if (ValueUtils.hasValue(errorCode)) {				
				map.put("errorCode", ValueUtils.getValue(message.getMessage(errorCode, null, errorCode, LocaleContextHolder.getLocale())));
				map.put("errorDscp", ValueUtils.getValue(message.getMessage(errorCode, null, errorCode, LocaleContextHolder.getLocale())));
			} else {
				map.put("errorCode", ApplicationConstants.EMPTY_STRING);
				map.put("errorDscp", ApplicationConstants.EMPTY_STRING);
			}
			resultList.add(map);
		}

		return resultList;
	}
	
	class ComparableByPriority implements Comparator<SweepOutDetailModel>
	{	
	    public int compare(SweepOutDetailModel detail1, SweepOutDetailModel detail2)
	    {
	        if(detail2.getPriority() == 0 ) {
	        	return -1; // return false, so for empty priority put in the last order (ascending mode)
	        }else if (detail1.getPriority() == 0){
	        	return 1; // return true, so for empty priority put in the last order (ascending mode)
	        }else {	        
	        	return detail1.getPriority() - detail2.getPriority();
	        }
	    }
	}

	@Override
	public Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			//cancelId = pendingTaskId
			String pendingTaskId = (String) map.get("cancelId");
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			
			if(logger.isDebugEnabled()) {
				logger.debug("cancel pendingTaskId : " + pendingTaskId);
			}
			
			try {
				
				SweepOutModel sweepOut = sweepOutRepo.findByPendingTaskIdAndIsProcessed(pendingTaskId, ApplicationConstants.NO);
				
				sweepOut.setIsProcessed(ApplicationConstants.YES);
				sweepOut.setUpdatedDate(DateUtils.getCurrentTimestamp());
				sweepOut.setUpdatedBy(loginUserCode);
				
				sweepOutRepo.save(sweepOut);
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

	@Override
	public Map<String, Object> downloadTransactionStatus(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			
			Locale locale = LocaleContextHolder.getLocale();
			String transactionStatusId = (String) map.get("receiptId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");
			SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
			
			HashMap<String, Object> reportParams = new HashMap<>();
			reportParams.put("trxDate", sdfDate.format(trxStatus.getActivityDate()));
			reportParams.put("trxTime", sdfTime.format(trxStatus.getActivityDate()));
			reportParams.put("transactionStatus", message.getMessage(trxStatus.getStatus().toString(), null, trxStatus.getStatus().toString(), locale));
			reportParams.put("errorDscp", trxStatus.getErrorCode());
			
			Path bankLogoPath = Paths.get(bankLogo);
			BufferedImage bankLogo = ImageIO.read(bankLogoPath.toFile());
			reportParams.put("bankLogo", bankLogo);
			
			SweepOutModel model = sweepOutRepo.findOne(trxStatus.getEaiRefNo());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "Liquidity" + File.separator + "download-transaction-sweepout" + "-" + locale.getLanguage() + ".jasper";
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			setCopyrightSubReport(reportParams);
			
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
	
	private void setCopyrightSubReport(Map<String, Object> reportParams) throws JRException {
		Locale locale = LocaleContextHolder.getLocale();
		String copyrightFile = reportFolder + File.separator + "Copyright" + File.separator + "bank-copyright" + "-" + locale.getLanguage() + ".jasper";
		JasperReport copyrightReport = (JasperReport) JRLoader.loadObject(new File(copyrightFile));
		reportParams.put("COPYRIGHT_REPORT", copyrightReport);
	}

	@SuppressWarnings("unchecked")
	private void setParamForDownloadTrxStatus(HashMap<String, Object> reportParams, SweepOutModel model) {
		
		DecimalFormat df = new DecimalFormat(moneyFormat);
		if(model != null) {
			reportParams.put("service", ValueUtils.getValue(model.getService().getDscp()));
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			
			reportParams.put("autoSweepBack", model.getIsAutoSweepBack());
			reportParams.put("amountType", model.getSweepAmountType());
			reportParams.put("totalRecord", String.valueOf(model.getSweepOutDetail().size()));		
			reportParams.put("transactionCurrency", model.getSweepCurrency());
			
			CorporateUserPendingTaskModel pt = pendingTaskRepo.findByReferenceNo(model.getReferenceNo());
			String strValues = pt.getValuesStr();
			if (strValues != null) {
				try {
					Map<String, Object> valueMap = (Map<String, Object>) objectMapper.readValue(strValues, Class.forName(pt.getModel()));
					List<Map<String,Object>> listCharge = (List<Map<String,Object>>)valueMap.get("chargeList");
					
					for (int i = 0; i < listCharge.size(); i++) {
						Map<String, Object> mapCharge = listCharge.get(i);
						if (model.getChargeType1() != null && model.getChargeType1().equals(mapCharge.get("id")) && model.getChargeTypeAmount1().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency1", model.getChargeTypeCurrency1());
							reportParams.put("chargeType1", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount1", df.format(model.getChargeTypeAmount1()));
							continue;
			}
						if (model.getChargeType2() != null && model.getChargeType2().equals(mapCharge.get("id")) && model.getChargeTypeAmount2().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency2", model.getChargeTypeCurrency2());
							reportParams.put("chargeType2", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount2", df.format(model.getChargeTypeAmount2()));
							continue;
			}
						if (model.getChargeType3() != null && model.getChargeType3().equals(mapCharge.get("id")) && model.getChargeTypeAmount3().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency3", model.getChargeTypeCurrency3());
							reportParams.put("chargeType3", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount3", df.format(model.getChargeTypeAmount3()));
							continue;
			}
						if (model.getChargeType4() != null && model.getChargeType4().equals(mapCharge.get("id")) && model.getChargeTypeAmount4().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency4", model.getChargeTypeCurrency4());
							reportParams.put("chargeType4", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount4", df.format(model.getChargeTypeAmount4()));
							continue;
			}
						if (model.getChargeType5() != null && model.getChargeType5().equals(mapCharge.get("id")) && model.getChargeTypeAmount5().compareTo(BigDecimal.ZERO) > 0) {
				reportParams.put("chargeCurrency5", model.getChargeTypeCurrency5());
							reportParams.put("chargeType5", mapCharge.get("serviceChargeName"));
				reportParams.put("chargeAmount5", df.format(model.getChargeTypeAmount5()));
							continue;
						}
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			
			if(model.getTotalChargeEquivalentAmount().compareTo(BigDecimal.ZERO) > 0)
				reportParams.put("totalCharge", df.format(model.getTotalChargeEquivalentAmount()));
			
			reportParams.put("totalDebited", df.format(model.getTotalSweepAmount()));		
			reportParams.put("refNo", model.getReferenceNo());
		} 
		
		
	}
}
