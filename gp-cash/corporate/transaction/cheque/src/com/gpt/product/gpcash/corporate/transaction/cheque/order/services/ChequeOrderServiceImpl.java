package com.gpt.product.gpcash.corporate.transaction.cheque.order.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.CityModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.parametermt.model.IdentityTypeModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.product.gpcash.account.model.AccountModel;
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
import com.gpt.product.gpcash.corporate.transaction.cheque.constants.ChequeConstants;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.model.ChequeOrderDetailModel;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.model.ChequeOrderModel;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.repository.ChequeOrderCustomRepository;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.repository.ChequeOrderRepository;
import com.gpt.product.gpcash.corporate.transaction.globaltransaction.services.GlobalTransactionService;
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
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
@Transactional(rollbackFor = Exception.class)
public class ChequeOrderServiceImpl implements ChequeOrderService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.batch.transations.timeout-invoker}")
	private int invokerTimeout;
	
	@Value("${gpcash.batch.transations.timeout-job}")
	private int jobTimeout;

	@Value("${gpcash.batch.transations.concurrencies}")
	private int concurrencies;
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private ChequeOrderRepository chequeOrderRepo;
	
	@Autowired
	private ChequeOrderCustomRepository chequeOrderCustomRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
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
	private SysParamService sysParamService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;	
	
	@Autowired
	private CorporateChargeRepository corporateChargeRepo;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
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
	private MessageSource message;
	
	@Override
	public Map<String, Object> searchForCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String loginUserCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String accountGroupDetaiId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			String chequeNo = (String) map.get("chequeNo");
			
			Page<ChequeOrderModel> result;
			
			CorporateUserModel corpUser = corporateUtilsRepo.getCorporateUserRepo().findOne(loginUserCode);
			String userGroupId = corpUser.getCorporateUserGroup().getId();
			
			if(ValueUtils.hasValue(chequeNo)) {
				List<String> chequeOrderList = chequeOrderRepo.findByChequeNo(Helper.getSearchWildcardValue(chequeNo));
				if(chequeOrderList.size() == 0) {
					chequeOrderList = new ArrayList<>();
					//add empty string to avoid error missing expression
					chequeOrderList.add(ApplicationConstants.EMPTY_STRING);
				}
				result = chequeOrderRepo.findByChequeOrderId(chequeOrderList, userGroupId,
						PagingUtils.createPageRequest(map));
			} else {
				
				if(ValueUtils.hasValue(accountGroupDetaiId)) {
					CorporateAccountGroupDetailModel accountGroupDetail = corporateUtilsRepo.
							isCorporateAccountGroupDetailIdValid(loginCorporateId, accountGroupDetaiId);
					map.put("sourceAccount", accountGroupDetail.getCorporateAccount().getAccount().getAccountNo());
				}
				
				map.put("userGroupId", userGroupId);
				result = chequeOrderCustomRepo.searchForCorporate(map, PagingUtils.createPageRequest(map));
			}

			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else {
				resultMap.put("result", setModelToMap(result.getContent(), true));
			}
			
			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchOnlineForCorporate(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String loginCorporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String accountGroupDetaiId = (String) map.get(ApplicationConstants.ACCOUNT_GRP_DTL_ID);
			
			CorporateAccountGroupDetailModel accountGroupDetail = corporateUtilsRepo.
					isCorporateAccountGroupDetailIdValid(loginCorporateId, accountGroupDetaiId);
			
			Map<String, Object> input = new HashMap<>();
			AccountModel account = accountGroupDetail.getCorporateAccount().getAccount();
			input.put("accountNo", account.getAccountNo());
			input.put("accountName", account.getAccountName());
			input.put("accountCurrency", account.getCurrency().getCode());
			
			return eaiAdapter.invokeService(ChequeConstants.CHEQUE_ONLINE_INQUIRY, input);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> findOnlineBySerialNo(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String serialNo = (String) map.get("serialNo");
			Map<String, Object> input = new HashMap<>();
			input.put("serialNo", serialNo);
			input.put("accountNo", map.get("accountNo"));
			input.put("partNo", map.get("partNo"));
			input.put("accountBranch", map.get("accountBranch"));
			input.put("accountSuffix", map.get("accountSuffix"));
			input.put("chequeTypeCode", map.get("chequeTypeCode"));
			input.put("firstSerialNo", map.get("firstSerialNo"));
			input.put("lastSerialNo", map.get("lastSerialNo"));
			
			Map<String, Object> output = eaiAdapter.invokeService(ChequeConstants.CHEQUE_ONLINE_INQUIRY_DETAIL, input);
			List<Map<String, Object>> chequeList =  (ArrayList<Map<String,Object>>) output.get("chequeList");
			List<Map<String, Object>> cheques =  new ArrayList<>();
			
			Locale locale = LocaleContextHolder.getLocale();
			for(Map<String, Object> data : chequeList) {
				String chequeNo = (String) data.get("chequeNo");
				String status = (String) data.get("status");
				Map<String, Object> cheque = new HashMap<>();
				cheque.put("chequeNo", chequeNo);
				cheque.put("status", message.getMessage(status, null, status, locale));
				cheques.add(cheque);
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put("chequeFrom", output.get("chequeFrom"));
			result.put("chequeTo", output.get("chequeTo"));
			result.put("chequeList", cheques);
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchForBank(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERID);
			
			IDMUserModel idmUser = idmUserRepo.findOne(loginUserId);
			String userBranchCode = idmUser.getBranch().getCode();
			
			//only search by login user branch
			map.put("branchCode", userBranchCode);
			Page<ChequeOrderModel> result = chequeOrderCustomRepo.searchForBank(map, PagingUtils.createPageRequest(map));

			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else {
				resultMap.put("result", setModelToMap(result.getContent(), true));
			}
			
			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<ChequeOrderModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (ChequeOrderModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(ChequeOrderModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("chequeOrderId", model.getId());
		map.put("orderNo", model.getOrderNo());
		map.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, model.getReferenceNo());
		
		SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd-MMM-yyyy hh:mm");
		if(model.getHandoverDate() != null) {
			map.put("handoverDate", sdfDateTime.format(model.getHandoverDate()));
		} else {
			map.put("handoverDate", ApplicationConstants.EMPTY_STRING);
		}
				
		AccountModel account = model.getSourceAccount();
		map.put("sourceAccountNo", account.getAccountNo());
		map.put("sourceAccountName", account.getAccountName());
		map.put("sourceAccountCurrency", account.getCurrency().getCode());
		map.put("noOfPages", model.getNoOfPages());
		
		BranchModel branch = model.getBranch();
		map.put("branchCode", branch.getCode());
		map.put("branchName", branch.getName());
		
		CorporateModel corporate = model.getCorporate();
		map.put("corporateId", corporate.getId());
		map.put("corporateName", corporate.getName());
		
		CityModel city = branch.getCity();
		map.put("cityCode", city.getCode());
		map.put("cityName", city.getName());
		
		Locale locale = LocaleContextHolder.getLocale();
		map.put("statusCode", model.getStatus());
		map.put("statusName", message.getMessage(model.getStatus(), null, model.getStatus(), locale));
		
		if(isGetDetail){
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy");
			map.put("pickupDate", ValueUtils.getValue(sdfDate.format(model.getInstructionDate())));
			
			SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm");
			map.put("pickupTime", ValueUtils.getValue(sdfTime.format(model.getInstructionDate())));
			
			map.put("identityNo", model.getIdentityNo());
			map.put("identityName", model.getIdentityName());
			if (model.getIdentityType() !=null) {
				map.put("identityType", model.getIdentityType().getCode());
			}
			map.put("branchAddress1", ValueUtils.getValue(branch.getAddress1()));
			map.put("branchAddress2", ValueUtils.getValue(branch.getAddress2()));
			map.put("branchAddress3", ValueUtils.getValue(branch.getAddress3()));
			
			map.put("chequeNoFrom", model.getChequeNoFrom());
			map.put("chequeNoTo", model.getChequeNoTo());
			map.put("chequeSerial", model.getChequeSerial());
			
			map.put("mobileNo", model.getMobileNo());
			map.put("reason", model.getDeclineReason());
			
			//set charges
			setCharges(model, map);
			//------------------------------------------
		}

		return map;
	}
	
	private void setCharges(ChequeOrderModel model, Map<String, Object> map) throws Exception {
		List<Map<String, Object>> chargeList = new ArrayList<>();
		BigDecimal totalCharge = BigDecimal.ZERO;
		Map<String, Object> chargeMap;
		if(ValueUtils.hasValue(model.getChargeType1())) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType1());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("id", serviceCharge.getId());
			chargeMap.put("serviceChargeName", serviceCharge.getName());
			chargeMap.put("value", model.getChargeTypeAmount1().toPlainString());
			
			CurrencyModel currency = maintenanceRepo.isCurrencyValid(model.getChargeTypeCurrency1());
			chargeMap.put("currencyCode", currency.getCode());
			chargeMap.put("currencyName", currency.getName());
			
			totalCharge = totalCharge.add(model.getChargeTypeAmount1());
			chargeList.add(chargeMap);
		}
		
		if(ValueUtils.hasValue(model.getChargeType2())) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType2());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("id", serviceCharge.getId());
			chargeMap.put("serviceChargeName", serviceCharge.getName());
			chargeMap.put("value", model.getChargeTypeAmount2().toPlainString());
			
			CurrencyModel currency = maintenanceRepo.isCurrencyValid(model.getChargeTypeCurrency2());
			chargeMap.put("currencyCode", currency.getCode());
			chargeMap.put("currencyName", currency.getName());
			
			totalCharge = totalCharge.add(model.getChargeTypeAmount2());
			chargeList.add(chargeMap);
		}
		
		if(ValueUtils.hasValue(model.getChargeType3())) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType3());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("id", serviceCharge.getId());
			chargeMap.put("serviceChargeName", serviceCharge.getName());
			chargeMap.put("value", model.getChargeTypeAmount3().toPlainString());
			
			CurrencyModel currency = maintenanceRepo.isCurrencyValid(model.getChargeTypeCurrency3());
			chargeMap.put("currencyCode", currency.getCode());
			chargeMap.put("currencyName", currency.getName());
			
			totalCharge = totalCharge.add(model.getChargeTypeAmount3());
			chargeList.add(chargeMap);
		}
		
		if(ValueUtils.hasValue(model.getChargeType4())) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType4());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("id", serviceCharge.getId());
			chargeMap.put("serviceChargeName", serviceCharge.getName());
			chargeMap.put("value", model.getChargeTypeAmount4().toPlainString());
			
			CurrencyModel currency = maintenanceRepo.isCurrencyValid(model.getChargeTypeCurrency4());
			chargeMap.put("currencyCode", currency.getCode());
			chargeMap.put("currencyName", currency.getName());
			
			totalCharge = totalCharge.add(model.getChargeTypeAmount4());
			chargeList.add(chargeMap);
		}
		
		if(ValueUtils.hasValue(model.getChargeType5())) {
			chargeMap = new HashMap<>();
			CorporateChargeModel corporateCharge = corporateChargeRepo.findOne(model.getChargeType5());
			ServiceChargeModel serviceCharge = corporateCharge.getServiceCharge();
			chargeMap.put("id", serviceCharge.getId());
			chargeMap.put("serviceChargeName", serviceCharge.getName());
			chargeMap.put("value", model.getChargeTypeAmount5().toPlainString());
			
			CurrencyModel currency = maintenanceRepo.isCurrencyValid(model.getChargeTypeCurrency5());
			chargeMap.put("currencyCode", currency.getCode());
			chargeMap.put("currencyName", currency.getName());
			
			totalCharge = totalCharge.add(model.getChargeTypeAmount5());
			chargeList.add(chargeMap);
		}
		
		map.put("chargeList", chargeList);
		map.put("totalCharge", totalCharge.toPlainString());
		map.put("totalChargeCurrency",sysParamService.getLocalCurrency());
		map.put("transactionCurrency", sysParamService.getLocalCurrency());
	}
	
	@Override
	public Map<String, Object> findDetailByOrderNo(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String chequeOrderId = (String) map.get("chequeOrderId");
			
			Page<ChequeOrderDetailModel> result = chequeOrderRepo.findDetailByOrderId(chequeOrderId, PagingUtils.createPageRequest(map));
			
			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
				resultMap.put("result", setModelToMapDetail(result.getContent(), false));
			} else {
				resultMap.put("result", setModelToMapDetail(result.getContent(), true));
			}
			
			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMapDetail(List<ChequeOrderDetailModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (ChequeOrderDetailModel model : list) {
			resultList.add(setModelToMapDetail(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMapDetail(ChequeOrderDetailModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("no", String.valueOf(model.getIdx() + 1));
		map.put("chequeNo", model.getChequeNo());
		map.put("chequeAmount", model.getChequeAmount().toPlainString());
		map.put("chequeAmountCurrency", model.getChequeCurrency());
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy"); //use to format withdrawDate
		map.put("withdrawDate", ""); //TODO integrate with host
		
		map.put("status", ""); //TODO integrate with host
		
		return map;
	}
	
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
			
			//generate order no for cheque
			String orderNo = ApplicationConstants.EMPTY_STRING;
			
			//find unique orderNo
			boolean unique = false;
			while(!unique) {
				orderNo = Helper.getRandomNumber(11);
				
				List<ChequeOrderModel> chequeList = chequeOrderRepo.findByOrderNo(orderNo);
				
				if(chequeList.size() == 0) {
					unique = true;
				}
			}
			//-----------------------------------------
			
			map.put("orderNo", orderNo);
			
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
			resultMap.put("orderNo", orderNo);

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
			
			

			// TODO validate cheque
//		} catch (BusinessException e) {
//			throw e;
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
		vo.setService("ChequeOrderSC");
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
		
		
		vo.setTotalChargeEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_CHARGE));
		vo.setTotalDebitedEquivalentAmount((BigDecimal) map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT));
		vo.setInstructionMode(instructionMode);
		
		vo.setInstructionDate(DateUtils.getInstructionDateBySessionTime(sessionTime, instructionDate));
		
		vo.setTaskExpiryDate(transactionValidationService.validateExpiryDate(instructionMode, instructionDate));
		
		return vo;
	}
	
	@SuppressWarnings("unchecked")
	private ChequeOrderModel setMapToModel(ChequeOrderModel chequeOrder, Map<String, Object> map,
			boolean isNew, CorporateUserPendingTaskVO vo) throws Exception {
		CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(vo.getCreatedBy());
		CorporateUserGroupModel corporateUserGroup = corporateUser.getCorporateUserGroup();

		String transactionServiceCode = (String) map.get(ApplicationConstants.TRANS_SERVICE_CODE);
		String instructionMode = (String) map.get("instructionMode");
		
		Timestamp instructionDate = vo.getInstructionDate();

		// set ID
		chequeOrder.setId(vo.getId());

		// set transaction information
		chequeOrder.setReferenceNo(vo.getReferenceNo());

		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode(vo.getMenuCode());
		chequeOrder.setMenu(menu);
		
		CorporateModel corpModel = corporateUtilsRepo.isCorporateValid(vo.getCorporateId());
		chequeOrder.setCorporate(corpModel);
		
		chequeOrder.setCorporateUserGroup(corporateUserGroup);
		
		IDMApplicationModel application = new IDMApplicationModel();
		//application code diambil dr yg login
		application.setCode((String) map.get(ApplicationConstants.APP_CODE)); 
		chequeOrder.setApplication(application);

		AccountModel sourceAccountModel = productRepo.isAccountValid(vo.getSourceAccount());
		chequeOrder.setSourceAccount(sourceAccountModel);
		
		chequeOrder.setTotalChargeEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_CHARGE).toString()));
		chequeOrder.setTotalDebitedEquivalentAmount(
				new BigDecimal(map.get(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT).toString()));

		// set transaction service code
		ServiceModel service = new ServiceModel();
		service.setCode(transactionServiceCode);
		chequeOrder.setService(service);

		// set additional information
		chequeOrder.setNoOfPages((Integer) map.get("noOfPages"));
		
		chequeOrder.setOrderNo((String) map.get("orderNo"));
		
		BranchModel branch = new BranchModel();
		String branchCode = (String) map.get("branchCode"); 
		branch.setCode(branchCode);
		chequeOrder.setBranch(branch);
		chequeOrder.setNoOfPages((Integer) map.get("noOfPages"));
		
		setInstructionMode(chequeOrder, instructionMode, instructionDate, map);
		
		setCharge(chequeOrder, (ArrayList<Map<String,Object>>) map.get("chargeList"));
		

		return chequeOrder;
	}
	
	private void setCharge(ChequeOrderModel chequeOrder, List<Map<String, Object>> chargeList){
		for(int i=1; i <= chargeList.size(); i++){
			Map<String, Object> chargeMap = chargeList.get(i - 1);
			String chargeId = (String) chargeMap.get("id");
			BigDecimal value = new BigDecimal(chargeMap.get("value").toString());
			String currencyCode = (String) chargeMap.get("currencyCode");
			
			if(i == 1){
				chequeOrder.setChargeType1(chargeId);
				chequeOrder.setChargeTypeAmount1(value);
				chequeOrder.setChargeTypeCurrency1(currencyCode);
			} else if(i == 2){
				chequeOrder.setChargeType2(chargeId);
				chequeOrder.setChargeTypeAmount2(value);
				chequeOrder.setChargeTypeCurrency2(currencyCode);
			} else if(i == 3){
				chequeOrder.setChargeType3(chargeId);
				chequeOrder.setChargeTypeAmount3(value);
				chequeOrder.setChargeTypeCurrency3(currencyCode);
			} else if(i == 4){
				chequeOrder.setChargeType4(chargeId);
				chequeOrder.setChargeTypeAmount4(value);
				chequeOrder.setChargeTypeCurrency4(currencyCode);
			} else if(i == 5){
				chequeOrder.setChargeType5(chargeId);
				chequeOrder.setChargeTypeAmount5(value);
				chequeOrder.setChargeTypeCurrency5(currencyCode);
			} 
		}
	}

	private void setInstructionMode(ChequeOrderModel chequeOrder, String instructionMode, Timestamp instructionDate, 
			Map<String, Object> map) throws Exception {
		
		
		// set instruction mode
		chequeOrder.setInstructionMode(instructionMode);

		if (instructionMode.equals(ApplicationConstants.SI_IMMEDIATE)) {
			chequeOrder
					.setInstructionDate(transactionValidationService.getInstructionDateForRelease(instructionMode, instructionDate));
		} else if (instructionMode.equals(ApplicationConstants.SI_FUTURE_DATE)) {
			chequeOrder.setInstructionDate(instructionDate);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				ChequeOrderModel chequeOrder = new ChequeOrderModel();
				setMapToModel(chequeOrder, map, true, vo);

				saveChequeOrder(chequeOrder, vo.getCreatedBy(), ApplicationConstants.NO);
				
				chequeOrder.setIsError(ApplicationConstants.NO);
				vo.setErrorCode(chequeOrder.getErrorCode());
				vo.setIsError(chequeOrder.getIsError());
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

	private void saveChequeOrder(ChequeOrderModel chequeOrder, String createdBy, String isProcessed)
			throws ApplicationException, BusinessException {
		// set default value
		chequeOrder.setIsProcessed(isProcessed);
		chequeOrder.setCreatedDate(DateUtils.getCurrentTimestamp());
		chequeOrder.setCreatedBy(createdBy);
		chequeOrder.setIsFeeDebited(ApplicationConstants.NO);
		
		//set status to new request
		chequeOrder.setStatus(ChequeConstants.CHQ_STS_NEW_REQUEST);
		
		//jika pendingTaskId null maka transaksi bukan recurring, maka pendingTaskId = model.Id
		if(chequeOrder.getPendingTaskId() == null)
			chequeOrder.setPendingTaskId(chequeOrder.getId());

		chequeOrderRepo.persist(chequeOrder);
	}
	
	private void updateChequeOrder(ChequeOrderModel chequeOrder, String updatedBy)
			throws ApplicationException, BusinessException {
		chequeOrder.setUpdatedDate(DateUtils.getCurrentTimestamp());
		chequeOrder.setUpdatedBy(updatedBy);
		chequeOrderRepo.save(chequeOrder);
	}
	
	@Override
	public Map<String, Object> process(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		String chequeOrderId = (String) map.get("chequeOrderId");
		String loginUser = (String) map.get(ApplicationConstants.LOGIN_USERID);
		
		try {
			ChequeOrderModel chequeOrder = chequeOrderRepo.findOne(chequeOrderId);
			
			if(!ApplicationConstants.YES.equals(chequeOrder.getIsFeeDebited())) {
				chequeOrder.setIsFeeDebited(ApplicationConstants.YES);
				chequeOrder.setFeeDebitedDate(DateUtils.getCurrentTimestamp());
				updateChequeOrder(chequeOrder, loginUser);
				
			}
			resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "success");
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> submitProcess(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		String chequeOrderId = (String) map.get("chequeOrderId");
		String loginUser = (String) map.get(ApplicationConstants.LOGIN_USERID);
		String chequeSerial = (String) map.get("chequeSerial");
		String chequeNoFrom = (String) map.get("chequeNoFrom");
		String chequeNoTo = (String) map.get("chequeNoTo");
		
		try {
			ChequeOrderModel chequeOrder = chequeOrderRepo.findOne(chequeOrderId);
			
			if(ChequeConstants.CHQ_STS_NEW_REQUEST.equals(chequeOrder.getStatus()) &&
					ApplicationConstants.YES.equals(chequeOrder.getIsFeeDebited())) {
				String processReferenceNo = Helper.generateBackOfficeReferenceNo();
				
				chequeOrder.setChequeNoFrom(chequeNoFrom);
				chequeOrder.setChequeNoTo(chequeNoTo);
				chequeOrder.setChequeSerial(chequeSerial);
				chequeOrder.setStatus(ChequeConstants.CHQ_STS_READY);
				chequeOrder.setIsProcessed(ApplicationConstants.YES);
				chequeOrder.setProcessReferenceNo(processReferenceNo);
				updateChequeOrder(chequeOrder, loginUser);
				
				pendingTaskRepo.updatePendingTask(chequeOrder.getPendingTaskId(), ApplicationConstants.WF_STATUS_APPROVED, DateUtils.getCurrentTimestamp(), loginUser, TransactionStatus.EXECUTE_SUCCESS);
				trxStatusService.addTransactionStatus(chequeOrder.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_SUCCESS, processReferenceNo, false, null);
				
				notifyTrxUsersHistory(chequeOrder);
				
				Locale locale = LocaleContextHolder.getLocale();
				resultMap.put("statusCode", chequeOrder.getStatus());
				resultMap.put("statusName", message.getMessage(chequeOrder.getStatus(), null, chequeOrder.getStatus(), locale));
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, processReferenceNo);
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200009");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + Helper.DATE_TIME_FORMATTER.format(DateUtils.getCurrentTimestamp()));
			} else {
				throw new BusinessException("GPT-ILLEGAL-ACTION");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void notifyTrxUsersHistory(ChequeOrderModel model) {
		try {
			List<String> emails = pendingTaskService.getCorporateUserEmailWithNotifyTrx(model.getId());
			if (emails.size() > 0) {
				Map<String, Object> inputs = new HashMap<>();

				IDMMenuModel menu = idmRepo.isIDMMenuValid(ChequeOrderSC.menuCode);
				AccountModel debitAccount = model.getSourceAccount();
				
				inputs.put("orderNo", model.getOrderNo());
				inputs.put("debitAccountNo", debitAccount.getAccountNo());
				inputs.put("debitAccountName", debitAccount.getAccountName());
				inputs.put("trxCurrencyCode", debitAccount.getCurrency().getCode());
				inputs.put("trxAmount", model.getTotalDebitedEquivalentAmount());
				inputs.put("menuName", menu.getName());
				inputs.put("refNo", model.getReferenceNo());
				
				inputs.put("noOfPages", model.getNoOfPages());
				inputs.put("branchName", model.getBranch().getName());
				inputs.put("pickupDate", model.getInstructionDate());
				
				inputs.put("status", model.getStatus());
				inputs.put("errorCode", model.getErrorCode());
				inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
				inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());				
				inputs.put("emails", emails);
				inputs.put("subject", menu.getName().concat(" Notification"));
				
				eaiAdapter.invokeService(EAIConstants.USER_CHEQUE_ORDER_NOTIFICATION, inputs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unused")
	@Override
	public Map<String, Object> confirmProcess(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		String chequeOrderId = (String) map.get("chequeOrderId");
		String chequeSerial = (String) map.get("chequeSerial");
		String chequeNoFrom = chequeSerial.concat((String) map.get("chequeNoFrom"));
		String chequeNoTo = chequeSerial.concat((String) map.get("chequeNoTo"));
		
		try {
			ChequeOrderModel chequeOrder = chequeOrderRepo.findOne(chequeOrderId);

			//TODO validate chequeNoFrom and chequeNoTo
			
			resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "success");
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> decline(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		String chequeOrderId = (String) map.get("chequeOrderId");
		String loginUser = (String) map.get(ApplicationConstants.LOGIN_USERID);
		String declineReason = (String) map.get("reason");
		
		try {
			ChequeOrderModel chequeOrder = chequeOrderRepo.findOne(chequeOrderId);
			
			if(ChequeConstants.CHQ_STS_NEW_REQUEST.equals(chequeOrder.getStatus())) {
				String declineRefNo = Helper.generateBackOfficeReferenceNo();
				
				Timestamp declineDate = DateUtils.getCurrentTimestamp();
				
				chequeOrder.setDeclineReason(declineReason);
				chequeOrder.setDeclineReferenceNo(declineRefNo);
				chequeOrder.setDeclineDate(declineDate);
				
				chequeOrder.setStatus(ChequeConstants.CHQ_STS_DECLINED);
				
				updateChequeOrder(chequeOrder, loginUser);
				
				pendingTaskRepo.updatePendingTask(chequeOrder.getPendingTaskId(), ApplicationConstants.WF_STATUS_REJECTED, DateUtils.getCurrentTimestamp(), loginUser, TransactionStatus.EXECUTE_FAIL);
				trxStatusService.addTransactionStatus(chequeOrder.getId(), DateUtils.getCurrentTimestamp(), TransactionActivityType.EXECUTE_TO_HOST, ApplicationConstants.CREATED_BY_SYSTEM, TransactionStatus.EXECUTE_FAIL, declineRefNo, false, declineReason);
				
				notifyTrxUsersHistory(chequeOrder);
				
				Locale locale = LocaleContextHolder.getLocale();
				resultMap.put("statusCode", chequeOrder.getStatus());
				resultMap.put("statusName", message.getMessage(chequeOrder.getStatus(), null, chequeOrder.getStatus(), locale));
				
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, declineRefNo);
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200009");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + Helper.DATE_TIME_FORMATTER.format(declineDate));
				
			} else {
				throw new BusinessException("GPT-ILLEGAL-ACTION");
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> handover(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		String chequeOrderId = (String) map.get("chequeOrderId");
		String loginUser = (String) map.get(ApplicationConstants.LOGIN_USERID);
		String identityTypeCode = (String) map.get("identityTypeCode");
		String identityNo = (String) map.get("identityNo");
		String identityName = (String) map.get("identityName");
		String mobileNo = (String) map.get("mobileNo");
		String chequeSerial = (String) map.get("chequeSerial");
		
		try {
			ChequeOrderModel chequeOrder = chequeOrderRepo.findOne(chequeOrderId);
			
			if(ChequeConstants.CHQ_STS_READY.equals(chequeOrder.getStatus())) {
				String handoverRefNo = Helper.generateBackOfficeReferenceNo();
				
				Timestamp handoverDate = DateUtils.getCurrentTimestamp();
				chequeOrder.setIdentityNo(identityNo);
				chequeOrder.setIdentityName(identityName);
				chequeOrder.setHandoverReferenceNo(handoverRefNo);
				chequeOrder.setHandoverDate(handoverDate);
				chequeOrder.setChequeSerial(chequeSerial);
				
				IdentityTypeModel identityType = new IdentityTypeModel();
				identityType.setCode(identityTypeCode);
				chequeOrder.setIdentityType(identityType);
				chequeOrder.setMobileNo(mobileNo);
				
				chequeOrder.setStatus(ChequeConstants.CHQ_STS_PICKED_UP);
				updateChequeOrder(chequeOrder, loginUser);
				
				Locale locale = LocaleContextHolder.getLocale();
				resultMap.put("statusCode", chequeOrder.getStatus());
				resultMap.put("statusName", message.getMessage(chequeOrder.getStatus(), null, chequeOrder.getStatus(), locale));
				
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, handoverRefNo);
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200009");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + Helper.DATE_TIME_FORMATTER.format(handoverDate));
								
			} else {
				throw new BusinessException("GPT-ILLEGAL-ACTION");
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
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
	
	@Override
	public Map<String, Object> detailExecutedTransaction(Map<String, Object> map)
			throws ApplicationException, BusinessException {

		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			String transactionStatusId = (String) map.get("executedId");
			TransactionStatusModel trxStatus = trxStatusRepo.findOne(transactionStatusId);
			
			//jika success ambil ke model nya, jika tidak maka ambil dr pending task krn blm masuk table
			if(trxStatus.getStatus().equals(TransactionStatus.EXECUTE_SUCCESS)) {
				ChequeOrderModel model = chequeOrderRepo.findOne(trxStatus.getPendingTaskId());
				resultMap.put("executedResult", prepareDetailTransactionMap(model, trxStatus));
			} else {
				CorporateUserPendingTaskModel model = pendingTaskRepo.findOne(trxStatus.getPendingTaskId());
				resultMap.put("executedResult", globalTransactionService.prepareDetailTransactionMapFromPendingTask(model, trxStatus));
			}
			
			
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
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
	
	private List<Map<String, Object>> prepareDetailTransactionMap(ChequeOrderModel model, TransactionStatusModel trxStatus) throws Exception {
		List<Map<String, Object>> executedTransactionList = new LinkedList<>();

		AccountModel sourceAccount = model.getSourceAccount();
		
		Map<String, Object> modelMap = new HashMap<>();
		modelMap.put("executedDate", trxStatus.getActivityDate());
		modelMap.put("systemReferenceNo", model.getId());
		modelMap.put("debitAccount", sourceAccount.getAccountNo());
		modelMap.put("debitAccountName", sourceAccount.getAccountName());
		modelMap.put("debitAccountCurrency", sourceAccount.getCurrency().getCode());
		modelMap.put("debitAccountCurrencyName", sourceAccount.getCurrency().getName());
		modelMap.put("transactionCurrency", ApplicationConstants.EMPTY_STRING);
		modelMap.put("transactionAmount", ApplicationConstants.ZERO);
		modelMap.put("creditAccount", ApplicationConstants.EMPTY_STRING);
		modelMap.put("creditAccountName", ApplicationConstants.EMPTY_STRING);
		modelMap.put("creditAccountCurrency", ApplicationConstants.EMPTY_STRING);
		modelMap.put("senderRefNo", ApplicationConstants.EMPTY_STRING);
		modelMap.put("benRefNo", ApplicationConstants.EMPTY_STRING);
		
		//TODO diganti jika telah implement rate
		modelMap.put("debitTransactionCurrency", ApplicationConstants.EMPTY_STRING);
		modelMap.put("debitEquivalentAmount", ApplicationConstants.ZERO);
		modelMap.put("debitExchangeRate", ApplicationConstants.ZERO);
		modelMap.put("creditTransactionCurrency", ApplicationConstants.EMPTY_STRING);
		modelMap.put("creditEquivalentAmount", ApplicationConstants.ZERO);
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
			
			ChequeOrderModel model = chequeOrderRepo.findOne(trxStatus.getEaiRefNo());
			setParamForDownloadTrxStatus(reportParams, model);
			
			String masterReportFile = reportFolder + File.separator + "TransactionStatus" + 
					File.separator + "ChequeOrder" + File.separator + "download-transaction-chequeorder" + "-" + locale.getLanguage() + ".jasper";
			JasperReport masterReport = (JasperReport) JRLoader.loadObject(new File(masterReportFile));
			
			JasperPrint print = JasperFillManager.fillReport(masterReport, reportParams, new JREmptyDataSource());
			byte[] bytes = JasperExportManager.exportReportToPdf(print);
		
			//download reference no agar unique tiap request download
			String downloadRefNo = Helper.generateShortTransactionReferenceNo();
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
	
	private void setParamForDownloadTrxStatus(Map<String, Object> reportParams, ChequeOrderModel model) throws JsonParseException, JsonMappingException, ClassNotFoundException, IOException {
		DecimalFormat df = new DecimalFormat(moneyFormat);
		if(model != null) {
			reportParams.put("service", ValueUtils.getValue(model.getService().getDscp()));
			
			AccountModel sourceAccount = model.getSourceAccount();
			reportParams.put("sourceAccount", sourceAccount.getAccountNo());
			reportParams.put("sourceAccountName", sourceAccount.getAccountName());
			
			reportParams.put("noOfPages", model.getNoOfPages());
			
			BranchModel branch = model.getBranch();
			reportParams.put("branchCode", branch.getCode());
			reportParams.put("branchName", branch.getName());
			
			CityModel city = branch.getCity();
			reportParams.put("cityCode", city.getCode());
			reportParams.put("cityName", city.getName());
			
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
			
			reportParams.put("refNo", model.getReferenceNo());
		} 
	}
}
