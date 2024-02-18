package com.gpt.product.gpcash.retail.customer.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.idm.userapp.services.IDMUserAppService;
import com.gpt.component.idm.userrole.repository.IDMUserRoleRepository;
import com.gpt.component.idm.userrole.services.IDMUserRoleService;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.CityModel;
import com.gpt.component.maintenance.parametermt.model.CountryModel;
import com.gpt.component.maintenance.parametermt.model.IdentityTypeModel;
import com.gpt.component.maintenance.parametermt.model.PostCodeModel;
import com.gpt.component.maintenance.parametermt.model.StateModel;
import com.gpt.component.maintenance.parametermt.model.SubstateModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageDetailModel;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageDetailModel;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;
import com.gpt.product.gpcash.retail.customeraccount.services.CustomerAccountService;
import com.gpt.product.gpcash.retail.customercharge.model.CustomerChargeModel;
import com.gpt.product.gpcash.retail.customercharge.repository.CustomerChargeRepository;
import com.gpt.product.gpcash.retail.customerlimit.model.CustomerLimitModel;
import com.gpt.product.gpcash.retail.customerlimit.repository.CustomerLimitRepository;
import com.gpt.product.gpcash.retail.registration.model.RegistrationModel;
import com.gpt.product.gpcash.retail.registration.repository.RegistrationRepository;
import com.gpt.product.gpcash.retail.token.tokentype.model.CustomerTokenTypeModel;
import com.gpt.product.gpcash.retail.token.tokenuser.model.CustomerTokenUserModel;
import com.gpt.product.gpcash.servicepackage.model.ServicePackageModel;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerServiceImpl implements CustomerService {
	
	@Value("${gpcash.retail.registrationcode.length:8}")
	private int registrationCodeLength;
	
	@Value("${gpcash.retail.registrationcode.validity:600}")
	private int registrationCodeValidity;

	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private IDMUserRoleService idmUserRoleService;
	
	@Autowired
	private IDMUserAppService idmUserAppService;
	
	@Autowired
	private RegistrationRepository registrationRepo;
	
	@Autowired
	private CustomerAccountService customerAccountService;
	
	@Autowired
	private IDMUserService idmUserService;

	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Autowired
	private CustomerLimitRepository customerLimitRepo;

	@Autowired
	private CustomerChargeRepository customerChargeRepo;
	
	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;
	
	@Autowired
	private IDMUserRoleRepository userRoleRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;

	@Autowired
	private EAIEngine eaiAdapter;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerModel> result = customerRepo.search(map, PagingUtils.createPageRequest(map));

			if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else if (ApplicationConstants.WF_ACTION_DETAIL.equals(map.get(ApplicationConstants.WF_ACTION))) {
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

	private List<Map<String, Object>> setModelToMap(List<CustomerModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.CUST_ID, model.getId());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put(ApplicationConstants.USER_ID, model.getUserId());
		map.put("cifId", ValueUtils.getValue(model.getHostCifId()));
		map.put("inactiveFlag", ValueUtils.getValue(model.getInactiveFlag()));
		
		//---------------------------
		
		if (isGetDetail) {
			map.put("address1", ValueUtils.getValue(model.getAddress1()));
			map.put("address2", ValueUtils.getValue(model.getAddress2()));
			map.put("address3", ValueUtils.getValue(model.getAddress3()));

			if (model.getPostcode() != null) {
				PostCodeModel postcode = maintenanceRepo.getPostCodeRepo().findOne(model.getPostcode().getCode());
				map.put("postcode", ValueUtils.getValue(postcode.getCode()));
				map.put("postcodeName", ValueUtils.getValue(postcode.getName()));
			}

			if (model.getCity() != null) {
				CityModel city = maintenanceRepo.getCityRepo().findOne(model.getCity().getCode());
				map.put("cityCode", ValueUtils.getValue(city.getCode()));
				map.put("cityName", ValueUtils.getValue(city.getName()));
			}

			if (model.getSubstate() != null) {
				SubstateModel substate = maintenanceRepo.getSubstateRepo().findOne(model.getSubstate().getCode());
				map.put("substateCode", ValueUtils.getValue(substate.getCode()));
				map.put("substateName", ValueUtils.getValue(substate.getName()));
			}

			if (model.getState() != null) {
				StateModel state = maintenanceRepo.getStateRepo().findOne(model.getState().getCode());
				map.put("stateCode", ValueUtils.getValue(state.getCode()));
				map.put("stateName", ValueUtils.getValue(state.getName()));
			}

			if (model.getCountry() != null) {
				CountryModel country = maintenanceRepo.getCountryRepo().findOne(model.getCountry().getCode());
				map.put("countryCode", ValueUtils.getValue(country.getCode()));
				map.put("countryName", ValueUtils.getValue(country.getName()));
			}
			
			if (model.getIdentityType() != null) {
				IdentityTypeModel identityType = maintenanceRepo.getIdentityTypeRepo().findOne(model.getCountry().getCode());
				map.put("identityTypeCode", ValueUtils.getValue(identityType.getCode()));
				map.put("identityTypeName", ValueUtils.getValue(identityType.getName()));
				map.put("identityTypeValue", ValueUtils.getValue(model.getIdentityTypeValue()));
			}

			map.put("email1", ValueUtils.getValue(model.getEmail1()));
			map.put("email2", ValueUtils.getValue(model.getEmail2()));
			map.put("phoneNo", ValueUtils.getValue(model.getPhoneNo()));
			map.put("mobileNo", ValueUtils.getValue(model.getMobileNo()));
			map.put("specialChargeFlag", ValueUtils.getValue(model.getSpecialChargeFlag()));
			map.put("specialLimitFlag", ValueUtils.getValue(model.getSpecialLimitFlag()));

			if (model.getBranch() != null) {
				BranchModel branch = maintenanceRepo.getBranchRepo().findOne(model.getBranch().getCode());
				map.put("branchCode", ValueUtils.getValue(branch.getCode()));
				map.put("branchName", ValueUtils.getValue(branch.getName()));
			}

			if (model.getServicePackage() != null) {
				ServicePackageModel servicePackage = productRepo.getServicePackageRepo()
						.findOne(model.getServicePackage().getCode());
				map.put("servicePackageCode", ValueUtils.getValue(servicePackage.getCode()));
				map.put("servicePackageName", ValueUtils.getValue(servicePackage.getName()));
			}

			map.put("taxIdNo", ValueUtils.getValue(model.getTaxIdNo()));

			map.put("lldIsResidence", ValueUtils.getValue(model.getLldIsResidence()));
			map.put("lldIsCitizen", ValueUtils.getValue(model.getLldIsCitizen()));
			
			if (model.getCitizenCountry() != null) {
				CountryModel citizenCountry = maintenanceRepo.getCountryRepo()
						.findOne(model.getCitizenCountry().getCode());
				map.put("citizenCountryCode", ValueUtils.getValue(citizenCountry.getCode()));
				map.put("citizenCountryName", ValueUtils.getValue(citizenCountry.getName()));
			}

			if (model.getResidenceCountry() != null) {
				CountryModel residenceCountry = maintenanceRepo.getCountryRepo()
						.findOne(model.getResidenceCountry().getCode());
				map.put("residenceCountryCode", ValueUtils.getValue(residenceCountry.getCode()));
				map.put("residenceCountryName", ValueUtils.getValue(residenceCountry.getName()));
			}

			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));

		}

		return map;
	}
	
	
	@Override
	public Map<String, Object> customerRegistration(Map<String, Object> map) throws ApplicationException, BusinessException {

		Map<String, Object> result = new HashMap<>();
		
		try {
			String cardNo = (String)map.get("cardNo");
			String accountNo = (String)map.get("accountNo");
			String inputMobileNo = (String)map.get("mobileNo");
			String registrationCode = (String)map.get("registrationCode");
			String userId = ((String)map.get("userId")).toUpperCase();
			validateRegistrationCode(cardNo, accountNo, inputMobileNo, registrationCode, true);
			
			CustomerModel customer = customerRepo.findByUserIdContainingIgnoreCase(userId);
			if(customer != null)
				throw new BusinessException("GPT-REGIS-USR-0000008");
			
			//TODO validate data to host
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put("cardNo", cardNo);
			inputMap.put("accountNo", accountNo);
			
			Map<String, Object> cifMap = getCIFInfo(inputMap, true);
			customer = setMapToModel(cifMap, true);
			customer.setId(userId.concat(ApplicationConstants.DELIMITER_PIPE).concat(registrationCode));
			customer.setUserId(userId); 
			customer.setHostCifId((String) cifMap.get("cifId"));
			
			//save to idm
			IDMUserModel idmUser = idmUserService.saveIDMUser(customer.getId(), userId, customer.getName(), customer.getEmail1(), customer.getId());
			customer.setUser(idmUser);
			
			//save default value
			customer.setSpecialChargeFlag(ApplicationConstants.NO);
			customer.setSpecialLimitFlag(ApplicationConstants.NO);
			customer.setIsNotifyMyTrx(ApplicationConstants.YES);
			saveCustomer(customer, ApplicationConstants.CREATED_BY_SYSTEM, true);
			
			//save customer account
			saveCustomerAccount(inputMap, customer.getId());

			ServicePackageModel servicePackage = customer.getServicePackage();
	
			// create customer limit
			LimitPackageModel limitPackage = servicePackage.getLimitPackage();
			createCustomerLimit(limitPackage, customer, ApplicationConstants.CREATED_BY_SYSTEM);
	
			// create customer charge
			ChargePackageModel chargePackage = servicePackage.getChargePackage();
			createCustomerCharge(chargePackage, customer, ApplicationConstants.CREATED_BY_SYSTEM);
			
			//save idm user role
			idmUserRoleService.saveUserRole(idmUser, servicePackage.getMenuPackage().getRole().getCode(), ApplicationConstants.CREATED_BY_SYSTEM);
			
			//save idm user app
			idmUserAppService.saveUserApplication(idmUser, ApplicationConstants.APP_GPCASHIB_R, ApplicationConstants.CREATED_BY_SYSTEM);
			
			//update registration model
			RegistrationModel regis = registrationRepo.findByRegistrationCode(registrationCode);
			regis.setActiveFlag(ApplicationConstants.YES);
			registrationRepo.save(regis);
			
			String strDateTime = Helper.DATE_TIME_FORMATTER.format(DateUtils.getCurrentTimestamp());
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-REGIS-USR-0000001");
			result.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

	
		
		return result;
	}
	
	@Override
	public Map<String, Object> forgotUserId(Map<String, Object> map) throws ApplicationException, BusinessException {

		Map<String, Object> result = new HashMap<>();
		
		try {
			String cardNo = (String)map.get("cardNo");
			String accountNo = (String)map.get("accountNo");
			String inputMobileNo = (String)map.get("mobileNo");
			String registrationCode = (String)map.get("registrationCode");
			String userId = ((String)map.get("userId")).toUpperCase();
			validateRegistrationCode(cardNo, accountNo, inputMobileNo, registrationCode, true);
			
			//TODO validate data to host
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put("cardNo", cardNo);
			inputMap.put("accountNo", accountNo);
			Map<String, Object> cifMap = getCIFInfo(inputMap, false);
			
			CustomerModel customer = customerRepo.findByHostCifIdContainingIgnoreCase((String) cifMap.get("cifId"));
			if(customer == null)
				throw new BusinessException("GPT-0100009");
			
			//check unique new user id
			CustomerModel newCustomer = customerRepo.findByUserIdContainingIgnoreCase(userId);
			if(newCustomer != null && newCustomer.getId() != customer.getId()) {
				throw new BusinessException("GPT-REGIS-USR-0000008");
			}
			
			
			customer.setUserId(userId); 
			
			//save to idm
			IDMUserModel idmUser = idmRepo.getUserRepo().findOne(customer.getId());
			idmUser.setUserId(userId);
			idmRepo.getUserRepo().save(idmUser);
			
			customer.setUser(idmUser);
			customerRepo.save(customer);
			
			
			String strDateTime = Helper.DATE_TIME_FORMATTER.format(DateUtils.getCurrentTimestamp());
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-REGIS-USR-0000009");
			result.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

	
		
		return result;
	}
	
	@Override
	public Map<String, Object> forgotPassword(Map<String, Object> map) throws ApplicationException, BusinessException {

		Map<String, Object> result = new HashMap<>();
		
		try {
			String cardNo = (String)map.get("cardNo");
			String accountNo = (String)map.get("accountNo");
			String inputMobileNo = (String)map.get("mobileNo");
			String registrationCode = (String)map.get("registrationCode");
			String userId = ((String)map.get("userId")).toUpperCase();
			validateRegistrationCode(cardNo, accountNo, inputMobileNo, registrationCode, true);
			
			//TODO validate data to host
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put("cardNo", cardNo);
			inputMap.put("accountNo", accountNo);
			getCIFInfo(inputMap, false);
			
			CustomerModel customer = customerRepo.findByUserIdContainingIgnoreCase(userId);
			if(customer == null)
				throw new BusinessException("GPT-0100009");
			
			idmUserService.resetUser(customer.getId(), ApplicationConstants.CREATED_BY_SYSTEM);
			
			String strDateTime = Helper.DATE_TIME_FORMATTER.format(DateUtils.getCurrentTimestamp());
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-REGIS-USR-0000009");
			result.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

	
		
		return result;
	}
	
	@Override
	public void validateRegistrationUserId (Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String cardNo = (String)map.get("cardNo");
			String accountNo = (String)map.get("accountNo");
			String inputMobileNo = (String)map.get("mobileNo");
			String registrationCode = (String)map.get("registrationCode");
			String userId = (String)map.get("userId");
//			validateRegistrationCode(cardNo, accountNo, inputMobileNo, registrationCode, false);
			
			//validate user id
//			CustomerModel customer = customerRepo.findByUserIdContainingIgnoreCase(userId);
//			if(customer != null)
//				throw new BusinessException("GPT-REGIS-USR-0000008");
			
//		} catch (BusinessException e) {
//			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void validateRegistrationExistingUserId (Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String cardNo = (String)map.get("cardNo");
			String accountNo = (String)map.get("accountNo");
			String inputMobileNo = (String)map.get("mobileNo");
			String registrationCode = (String)map.get("registrationCode");
			String userId = (String)map.get("userId");
			validateRegistrationCode(cardNo, accountNo, inputMobileNo, registrationCode, false);
			
			//TODO validate data to host
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put("cardNo", cardNo);
			inputMap.put("accountNo", accountNo);
			Map<String, Object> cifMap = getCIFInfo(inputMap, false);
			
			CustomerModel customer = customerRepo.findByHostCifIdContainingIgnoreCase((String) cifMap.get("cifId"));
			if(customer == null)
				throw new BusinessException("GPT-0100009");
			
			//check unique new user id
			CustomerModel newCustomer = customerRepo.findByUserIdContainingIgnoreCase(userId);
			if(newCustomer != null && newCustomer.getId() != customer.getId()) {
				throw new BusinessException("GPT-REGIS-USR-0000008");
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void saveCustomerAccount(Map<String, Object> map, String customerId) throws Exception{
		Map<String, Object> inputMap = new HashMap<>();
		map.put("cifId", map.get("cifId"));

		//call eai to get accounts
		map = getAccounts(inputMap);

		//save customer accounts
		List<Map<String, Object>> accountList =  (ArrayList<Map<String,Object>>) map.get("accountList");
		for(Map<String, Object> accountMap : accountList) {
			customerAccountService.saveCustomerAccount((String) accountMap.get("cifId"), (String) accountMap.get("accountNo"), (String) accountMap.get("accountBranchCode"), 
					(String) accountMap.get("accountCurrencyCode"), (String) accountMap.get("accountTypeCode"), (String) accountMap.get("accountName"), 
					(String) accountMap.get("accountAlias"), (String) accountMap.get("isDebit"), (String) accountMap.get("isCredit"), (String) accountMap.get("isInquiry"), 
					customerId, customerId);
		}
		
	}
	
	private Map<String, Object> getCIFInfo(Map<String, Object> inputMap, boolean isValidateUniqueCIF) throws Exception{
		Map<String, Object> map = new HashMap<>();
		
		//TODO this data only for dummy testing
		boolean isValid = true;
		if(isValid) {
			map.put("cifId", Helper.generateTransactionReferenceNo());
			map.put("cifName", "Test Demo");
			map.put("address1", "Address1");
			map.put("address2", "Address2");
			map.put("address3", "Address3");
			map.put("cityCode", "BANDUNG");
			map.put("countryCode", "ID");
			map.put("email1", "ayenjen@gmail.com");
			map.put("mobileNo", "55334466");
			
			if(isValidateUniqueCIF) {
				CustomerModel customer = customerRepo.findByHostCifIdContainingIgnoreCase((String) map.get("cifId"));
				if (customer != null)
					throw new BusinessException("GPT-REGIS-USR-0000010");
			}
		} else {
			throw new BusinessException("GPT-REGIS-USR-0000002");
		}
		
		
		return map;
	}
	
	private Map<String, Object> getAccounts(Map<String, Object> inputMap) throws Exception{
		Map<String, Object> result = new HashMap<>();
		
		List<Map<String, Object>> accountList = new ArrayList<>();
		Map<String, Object> accountMap = new HashMap<>();
		accountMap.put("cifId", inputMap.get("cifId"));
		accountMap.put("accountNo", Helper.getRandomNumber(10));
		accountMap.put("accountBranchCode", "0000");
		accountMap.put("accountCurrencyCode", "IDR");
		accountMap.put("accountTypeCode", "001");
		accountMap.put("accountName", "Test1");
		accountMap.put("isDebit", ApplicationConstants.YES);
		accountMap.put("isCredit", ApplicationConstants.YES);
		accountMap.put("isInquiry", ApplicationConstants.YES);
		accountList.add(accountMap);
		
		accountMap = new HashMap<>();
		accountMap.put("cifId", inputMap.get("cifId"));
		accountMap.put("accountNo", Helper.getRandomNumber(10));
		accountMap.put("accountBranchCode", "0000");
		accountMap.put("accountCurrencyCode", "IDR");
		accountMap.put("accountTypeCode", "001");
		accountMap.put("accountName", "Test1");
		accountMap.put("isDebit", ApplicationConstants.YES);
		accountMap.put("isCredit", ApplicationConstants.YES);
		accountMap.put("isInquiry", ApplicationConstants.YES);
		accountList.add(accountMap);
		
		accountMap = new HashMap<>();
		accountMap.put("cifId", inputMap.get("cifId"));
		accountMap.put("accountNo", Helper.getRandomNumber(10));
		accountMap.put("accountBranchCode", "0000");
		accountMap.put("accountCurrencyCode", "IDR");
		accountMap.put("accountTypeCode", "001");
		accountMap.put("accountName", "Test1");
		accountMap.put("isDebit", ApplicationConstants.YES);
		accountMap.put("isCredit", ApplicationConstants.YES);
		accountMap.put("isInquiry", ApplicationConstants.YES);
		accountList.add(accountMap);
		
		result.put("accountList", accountList);
		
		return result;
	}
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				checkCustomValidation(map);

				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String) map.get(ApplicationConstants.CUST_ID));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);

				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				CustomerModel customerOld = getExistingRecord((String) map.get(ApplicationConstants.CUST_ID), true);
				vo.setJsonObjectOld(setModelToMap(customerOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				getExistingRecord((String) map.get(ApplicationConstants.CUST_ID), true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UPDATE_STATUS")) {
				vo.setAction("UPDATE_STATUS");

				// check existing record exist or not
				getExistingRecord((String) map.get(ApplicationConstants.CUST_ID), true);
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
			if (ValueUtils.hasValue(map.get("postcode")))
				maintenanceRepo.isPostCodeValid((String) map.get("postcode"));

			if (ValueUtils.hasValue(map.get("substateCode")))
				maintenanceRepo.isSubstateValid((String) map.get("substateCode"));

			if (ValueUtils.hasValue(map.get("stateCode")))
				maintenanceRepo.isStateValid((String) map.get("stateCode"));

			if (ValueUtils.hasValue(map.get("countryCode")))
				maintenanceRepo.isCountryValid((String) map.get("countryCode"));

			if (ValueUtils.hasValue(map.get("branchCode")))
				maintenanceRepo.isBranchValid((String) map.get("branchCode"));

			if (ValueUtils.hasValue(map.get("servicePackageCode")))
				productRepo.isServicePackageValid((String) map.get("servicePackageCode"));

			if (ValueUtils.hasValue(map.get("handlingOfficerCode")))
				maintenanceRepo.isHandlingOfficerValid((String) map.get("handlingOfficerCode"));

			if (ValueUtils.hasValue(map.get("citizenCountryCode")))
				maintenanceRepo.isCountryValid((String) map.get("citizenCountryCode"));

			if (ValueUtils.hasValue(map.get("residenceCountryCode")))
				maintenanceRepo.isCountryValid((String) map.get("residenceCountryCode"));
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private CustomerModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		CustomerModel model = customerRepo.findOne(code);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		} else {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				if (isThrowError) {
					throw new BusinessException("GPT-0100001");
				} else {
					return null;
				}
			}
		}

		return model;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.CUST_ID));
		vo.setUniqueKeyDisplay((String) map.get(ApplicationConstants.STR_NAME));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CustomerSC");

		return vo;
	}

	private CustomerModel setMapToModel(Map<String, Object> map, boolean isNewRegistration) throws Exception {
		CustomerModel customer = new CustomerModel();
		customer.setId((String) map.get(ApplicationConstants.CUST_ID));

		customer.setName((String) map.get("cifName"));
		customer.setAddress1((String) map.get("address1"));
		customer.setAddress2((String) map.get("address2"));
		customer.setAddress3((String) map.get("address3"));

		if(ValueUtils.hasValue(map.get("postcode"))) {
			PostCodeModel postCode = new PostCodeModel();
			postCode.setCode((String) map.get("postcode"));
			customer.setPostcode(postCode);
		}

		if(ValueUtils.hasValue(map.get("cityCode"))) {
			CityModel city = new CityModel();
			city.setCode((String) map.get("cityCode"));
			customer.setCity(city);
		}
		
		if(ValueUtils.hasValue(map.get("substateCode"))) {
			SubstateModel substate = new SubstateModel();
			substate.setCode((String) map.get("substateCode"));
			customer.setSubstate(substate);
		}
		
		if(ValueUtils.hasValue(map.get("stateCode"))) {
			StateModel state = new StateModel();
			state.setCode((String) map.get("stateCode"));
			customer.setState(state);
		}
		
		if(ValueUtils.hasValue(map.get("identityTypeCode"))) {
			IdentityTypeModel identityType = new IdentityTypeModel();
			identityType.setCode((String) map.get("identityTypeCode"));
			customer.setIdentityType(identityType);
		}
		
		if(ValueUtils.hasValue(map.get("identityTypeValue"))) {
			customer.setIdentityTypeValue("identityTypeValue");
		}

		String countryCode = (String) map.get("countryCode");
		CountryModel country = new CountryModel();
		country.setCode(countryCode);
		customer.setCountry(country);

		customer.setEmail1((String) map.get("email1"));
		customer.setEmail2((String) map.get("email2"));
		customer.setPhoneNo((String) map.get("phoneNo"));
		customer.setMobileNo((String) map.get("mobileNo"));
		customer.setFaxNo((String) map.get("faxNo"));
		customer.setSpecialChargeFlag((String) map.get("specialChargeFlag"));
		customer.setSpecialLimitFlag((String) map.get("specialLimitFlag"));

		
		String defaultBranch = (String) map.get("branchCode");
		if(!ValueUtils.hasValue(defaultBranch)) {
			//get default branch
			defaultBranch = maintenanceRepo.isSysParamValid(SysParamConstants.DEFAULT_BRANCH_CODE).getValue();
			
		}
		BranchModel branch = new BranchModel();
		branch.setCode(defaultBranch);
		customer.setBranch(branch);
		

		String servicePackageCode = (String) map.get("servicePackageCode");
		if(!ValueUtils.hasValue(servicePackageCode)) {
			servicePackageCode = maintenanceRepo.isSysParamValid(SysParamConstants.DEFAULT_SERVICE_PACKAGE_CODE).getValue();
		}
		ServicePackageModel servicePackage = productRepo.getServicePackageRepo()
				.findOne(servicePackageCode);
		customer.setServicePackage(servicePackage);

		IDMRoleModel role = new IDMRoleModel();
		role.setCode(servicePackage.getMenuPackage().getRole().getCode());
		customer.setRole(role);
		
		customer.setTaxIdNo((String) map.get("taxIdNo"));

		String localCountryCode = defaultBranch = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_COUNTRY_CODE).getValue();
		if(isNewRegistration) {
			if(localCountryCode.equals(countryCode)) {
				customer.setLldIsResidence(ApplicationConstants.YES);
				customer.setLldIsCitizen(ApplicationConstants.YES);
			} else {
				customer.setLldIsResidence(ApplicationConstants.NO);
				customer.setLldIsCitizen(ApplicationConstants.NO);
			}
			

			CountryModel citizenCountry = new CountryModel();
			citizenCountry.setCode(countryCode);
			customer.setCitizenCountry(citizenCountry);

			CountryModel residenceCountry = new CountryModel();
			residenceCountry.setCode(countryCode);
			customer.setResidenceCountry(residenceCountry);
		} else {
			String lldIsResidence = (String) map.get("lldIsResidence");
			customer.setLldIsResidence(lldIsResidence);
			
			String lldIsCitizen = (String) map.get("lldIsCitizen");
			customer.setLldIsCitizen(lldIsCitizen);
			
			String citizenCountryCode = (String) map.get("citizenCountryCode");
			String residenceCountryCode = (String) map.get("residenceCountryCode");
			
			if(ApplicationConstants.NO.equals(lldIsCitizen)) {
				CountryModel citizenCountry = new CountryModel();
				citizenCountry.setCode(citizenCountryCode);
				customer.setCitizenCountry(citizenCountry);
			} else {
				CountryModel citizenCountry = new CountryModel();
				citizenCountry.setCode(localCountryCode);
				customer.setCitizenCountry(citizenCountry);
			}
			
			if(ApplicationConstants.NO.equals(lldIsResidence)) {
				CountryModel residenceCountry = new CountryModel();
				residenceCountry.setCode(residenceCountryCode);
				customer.setResidenceCountry(residenceCountry);
			} else {
				CountryModel residenceCountry = new CountryModel();
				residenceCountry.setCode(localCountryCode);
				customer.setResidenceCountry(residenceCountry);
			}
		}
		
		return customer;
	}

	private void checkUniqueRecord(String id) throws Exception {
		CustomerModel customer = customerRepo.findOne(id);

		if (customer != null && ApplicationConstants.NO.equals(customer.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				CustomerModel customerNew = setMapToModel(map, false);

				CustomerModel customerExisting = getExistingRecord(customerNew.getId(), true);
				
				//update service package maka update jg limit dan charges
				if(!customerExisting.getServicePackage().getCode().equals(customerNew.getServicePackage().getCode())) {
					updateCustomerLimitAndCharges(customerExisting, customerNew, vo.getCreatedBy());
				}

				//update role jika menu package berubah
				if(!customerExisting.getServicePackage().getMenuPackage().getCode().equals(customerNew.getServicePackage().getMenuPackage().getCode())) {				
					IDMUserModel idmUser = idmUserRepo.findOne(customerExisting.getUser().getCode());
					// delete old user role
					userRoleRepo.deleteByUserCode(customerExisting.getUser().getCode());
					userRoleRepo.flush();
					
					idmUserRoleService.saveUserRole(idmUser, customerNew.getServicePackage().getMenuPackage().getRole().getCode(), vo.getCreatedBy());
										
				}
				//--------------------------
				
				// set value yg boleh di edit
				customerExisting.setName(customerNew.getName());
				customerExisting.setAddress1(customerNew.getAddress1());
				customerExisting.setAddress2(customerNew.getAddress2());
				customerExisting.setAddress3(customerNew.getAddress3());

				customerExisting.setPostcode(customerNew.getPostcode());

				customerExisting.setCity(customerNew.getCity());

				customerExisting.setSubstate(customerNew.getSubstate());

				customerExisting.setState(customerNew.getState());

				customerExisting.setCountry(customerNew.getCountry());

				customerExisting.setEmail1(customerNew.getEmail1());
				customerExisting.setEmail2(customerNew.getEmail2());
				customerExisting.setPhoneNo(customerNew.getPhoneNo());
				customerExisting.setMobileNo(customerNew.getMobileNo());
				customerExisting.setFaxNo(customerNew.getFaxNo());
				customerExisting.setSpecialChargeFlag(customerNew.getSpecialChargeFlag());
				customerExisting.setSpecialLimitFlag(customerNew.getSpecialLimitFlag());

				customerExisting.setBranch(customerNew.getBranch());

				ServicePackageModel servicePackage = customerNew.getServicePackage();
				customerExisting.setServicePackage(servicePackage);

				customerExisting.setRole(servicePackage.getMenuPackage().getRole());

				customerExisting.setTaxIdNo(customerNew.getTaxIdNo());

				customerExisting.setLldIsResidence(customerNew.getLldIsResidence());
				customerExisting.setLldIsCitizen(customerNew.getLldIsCitizen());

				customerExisting.setCitizenCountry(customerNew.getCitizenCountry());

				customerExisting.setResidenceCountry(customerNew.getResidenceCountry());
				
				customerExisting.setIdentityType(customerNew.getIdentityType());
				customerExisting.setIdentityTypeValue(customerNew.getIdentityTypeValue());
				
				updateCustomer(customerExisting, vo.getCreatedBy());
				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				CustomerModel customer = getExistingRecord((String) map.get(ApplicationConstants.CUST_ID), true);

				deleteCustomer(customer, vo.getCreatedBy());
			} else if ("UPDATE_STATUS".equals(vo.getAction())) {
				// check existing record exist or not
				CustomerModel customer = getExistingRecord((String) map.get(ApplicationConstants.CUST_ID), true);

				updateCustomerStatus(customer, (String) map.get(ApplicationConstants.CUST_ID), 
						(String) map.get("inactiveFlag"), (String) map.get("inactiveReason"), 
						(String) map.get(ApplicationConstants.LOGIN_USERID));
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}
	
	private void updateCustomerLimitAndCharges(CustomerModel existing, CustomerModel customerNew, String createdBy) throws Exception {
		String customerId = existing.getId();
		
		//delete old customer limit
		customerLimitRepo.deleteByCustomerId(customerId);
		
		//delete  old customer charge
		customerChargeRepo.deleteByCustomerId(customerId);
		
		ServicePackageModel servicePackage = customerNew.getServicePackage();
		
		// create customer limit
		LimitPackageModel limitPackage = servicePackage.getLimitPackage();
		createCustomerLimit(limitPackage, customerNew, createdBy);

		// create customer charge
		ChargePackageModel chargePackage = servicePackage.getChargePackage();
		createCustomerCharge(chargePackage, customerNew, createdBy);
		
		//delete unused service code from customer customer group detail
		List<String> serviceCodeList = new ArrayList<>();
		for(LimitPackageDetailModel detail : servicePackage.getLimitPackage().getLimitPackageDetail()) {
			serviceCodeList.add(detail.getService().getCode());
		}
		
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	@Override
	public void saveCustomer(CustomerModel customer, String createdBy, boolean isNeedFlush)
			throws ApplicationException, BusinessException {
		// set default value
		customer.setDeleteFlag(ApplicationConstants.NO);
		customer.setInactiveFlag(ApplicationConstants.NO);
		customer.setCreatedDate(DateUtils.getCurrentTimestamp());
		customer.setCreatedBy(createdBy);
		customer.setUpdatedDate(null);
		customer.setUpdatedBy(null);

		if (isNeedFlush) {
			customerRepo.saveAndFlush(customer);
		} else {
			customerRepo.save(customer);
		}

	}

	@Override
	public void updateCustomer(CustomerModel customer, String updatedBy)
			throws ApplicationException, BusinessException {
		customer.setUpdatedDate(DateUtils.getCurrentTimestamp());
		customer.setUpdatedBy(updatedBy);
		customerRepo.save(customer);
	}

	private void updateCustomerStatus(CustomerModel customer, String customerId, String inactiveFlag, String inactiveReason, String updatedBy)
			throws ApplicationException, BusinessException {
		customer.setInactiveReason(inactiveReason);
		customer.setInactiveFlag(inactiveFlag);
		updateCustomer(customer, updatedBy);
	}

	@Override
	public void deleteCustomer(CustomerModel customer, String deletedBy)
			throws ApplicationException, BusinessException {
		customer.setDeleteFlag(ApplicationConstants.YES);
		customer.setUpdatedDate(DateUtils.getCurrentTimestamp());
		customer.setUpdatedBy(deletedBy);

		customerRepo.save(customer);
	}

	private void createCustomerLimit(LimitPackageModel limitPackage, CustomerModel customer, String createdBy)
			throws Exception {
		List<LimitPackageDetailModel> limitPackageDetailList = limitPackage.getLimitPackageDetail();

		List<CustomerLimitModel> customerLimitList = new ArrayList<>();
		for (LimitPackageDetailModel limitPackageDetail : limitPackageDetailList) {
			CustomerLimitModel customerLimit = new CustomerLimitModel();
			customerLimit.setCustomer(customer);
			customerLimit.setCurrency(limitPackageDetail.getCurrency());
			customerLimit.setLimitPackage(limitPackage);
			customerLimit.setService(limitPackageDetail.getService());
			customerLimit.setServiceCurrencyMatrix(limitPackageDetail.getServiceCurrencyMatrix());
			customerLimit.setApplication(limitPackage.getApplication());
			customerLimit.setMaxAmountLimit(limitPackageDetail.getMaxAmountLimit());
			customerLimit.setMaxOccurrenceLimit(limitPackageDetail.getMaxOccurrenceLimit());
			customerLimit.setAmountLimitUsage(new BigDecimal(0));
			customerLimit.setCreatedBy(createdBy);
			customerLimit.setCreatedDate(DateUtils.getCurrentTimestamp());
			customerLimitList.add(customerLimit);
		}
		customerLimitRepo.save(customerLimitList);
	}

	private void createCustomerCharge(ChargePackageModel chargePackage, CustomerModel customer, String createdBy)
			throws Exception {
		List<ChargePackageDetailModel> chargePackageDetailList = chargePackage.getChargePackageDetail();

		List<CustomerChargeModel> customerChargeList = new ArrayList<>();
		for (ChargePackageDetailModel chargePackageDetail : chargePackageDetailList) {
			CustomerChargeModel customerCharge = new CustomerChargeModel();
			customerCharge.setCustomer(customer);
			customerCharge.setValue(chargePackageDetail.getValue());
			customerCharge.setChargePackage(chargePackage);
			customerCharge.setValueType(chargePackageDetail.getValueType());
			customerCharge.setServiceCharge(chargePackageDetail.getServiceCharge());
			customerCharge.setApplication(chargePackage.getApplication());
			customerCharge.setCurrency(chargePackageDetail.getCurrency());
			customerCharge.setCreatedBy(createdBy);
			customerCharge.setCreatedDate(DateUtils.getCurrentTimestamp());
			customerChargeList.add(customerCharge);
		}
		customerChargeRepo.save(customerChargeList);
	}

	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("cifId", map.get("cifId"));

			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.CIF_INQUIRY, inputs);

			Map<String, Object> result = new HashMap<>(10, 1);
			result.put("name", outputs.get("name"));
			result.put("cifId", outputs.get("cifId"));
			result.put("address1", outputs.get("address1"));
			result.put("address2", outputs.get("address2"));
			result.put("address3", outputs.get("address3"));
			result.put("postcode", outputs.get("postCode"));
			result.put("cityCode", outputs.get("cityCode"));
			result.put("stateCode", outputs.get("stateCode"));
			result.put("substateCode", outputs.get("substateCode"));
			result.put("countryCode", outputs.get("countryCode"));

			return result;
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchCustomers() throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<CustomerModel> result = customerRepo.findCustomers();

			List<Map<String, Object>> resultList = new ArrayList<>();

			for (CustomerModel model : result) {
				Map<String, Object> map = new HashMap<>();
				map.put(ApplicationConstants.CUST_ID, model.getId());
				map.put("customerName", model.getId());
				map.put("userId", model.getUserId());
				resultList.add(map);
			}
			
			resultMap.put("result", resultList);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}

	@Override
	public Map<String, Object> customerVerification(Map<String, Object> map) throws BusinessException, ApplicationException {
		Map<String, Object> result = new HashMap<>();
		try {
			String cardNo = (String)map.get("cardNo");
			String accountNo = (String)map.get("accountNo");
			String inputMobileNo = (String)map.get("mobileNo");
			Map<String, Object> cifMap = validateRegistration(cardNo, accountNo, inputMobileNo, true);
			
			String cifId = (String) cifMap.get("cifId");
			String registrationCode = sendRegistrationCode(cifId, accountNo, cardNo, inputMobileNo);
			
			//TODO remark below for production (only for testing)
			result.put("registrationCode", registrationCode);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return result;
	}
	
	@Override
	public Map<String, Object> customerVerificationForExistingUser(Map<String, Object> map) throws BusinessException, ApplicationException {
		Map<String, Object> result = new HashMap<>();
		try {
			String cardNo = (String)map.get("cardNo");
			String accountNo = (String)map.get("accountNo");
			String inputMobileNo = (String)map.get("mobileNo");
			Map<String, Object> cifMap = validateRegistration(cardNo, accountNo, inputMobileNo, false);
			
			String cifId = (String) cifMap.get("cifId");
			String registrationCode = sendRegistrationCode(cifId, accountNo, cardNo, inputMobileNo);
			
			//TODO remark below for production (only for testing)
			result.put("registrationCode", registrationCode);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return result;
	}
	
	private String sendRegistrationCode(String cifId, String accountNo, String cardNo, String mobileNo) throws BusinessException, ApplicationException {
		
		RegistrationModel regis = registrationRepo.findOne(cifId);
		if(regis == null) {
			regis = new RegistrationModel();
			regis.setId(cifId);
			regis.setAccountNo(accountNo);
			regis.setCardNo(cardNo);
			regis.setMobileNo(mobileNo);
			regis.setCreatedDate(DateUtils.getCurrentTimestamp());
		}
		
//		String registrationCode = Helper.getRandomString(registrationCodeLength);
		String registrationCode = "DEMO" + cardNo.substring(0,4); //dummy for demo
		
		//save / update registration code
		regis.setCode(registrationCode);
		regis.setActiveFlag(ApplicationConstants.NO);
		regis.setCodeGeneratedDate(DateUtils.getCurrentTimestamp());
		
		registrationRepo.save(regis);
		
		//TODO send registration code
		
		return registrationCode;
	}
	
	private Map<String, Object> validateRegistration (String cardNo, String accountNo, String inputMobileNo, boolean isValidateUniqueCIF) throws Exception {
		//TODO validate data to host
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("cardNo", cardNo);
		inputMap.put("accountNo", accountNo);
		
		Map<String, Object> cifMap = getCIFInfo(inputMap, isValidateUniqueCIF);
		
		String mobileNo = (String) cifMap.get("mobileNo");
		String email1 = (String) cifMap.get("email1");
		
		if(!ValueUtils.hasValue(email1))
			throw new BusinessException("GPT-REGIS-USR-0000004");
		
		if(!ValueUtils.hasValue(mobileNo))
			throw new BusinessException("GPT-REGIS-USR-0000005");
		
		//check 4 digit mobile phone no
		if(!inputMobileNo.equals(mobileNo.substring(mobileNo.length() - 4, mobileNo.length())))
			throw new BusinessException("GPT-REGIS-USR-0000002");
		
		return cifMap;
	}

	@Override
	public Map<String, Object> customerVerification2(Map<String, Object> map) throws BusinessException, ApplicationException {
		Map<String, Object> result = new HashMap<>();
		try {
			String cardNo = (String)map.get("cardNo");
			String accountNo = (String)map.get("accountNo");
			String inputMobileNo = (String)map.get("mobileNo");
			String registrationCode = (String)map.get("registrationCode");
			validateRegistrationCode(cardNo, accountNo, inputMobileNo, registrationCode, true);
			
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, "SUCCESS");
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return result;
	}
	
	private void validateRegistrationCode (String cardNo, String accountNo, String inputMobileNo, String registrationCode, boolean isValidateRegistrationCodeLife) throws Exception {
		RegistrationModel regis = registrationRepo.findByRegistrationCode(registrationCode);
		
		if(regis == null) 
			throw new BusinessException("GPT-REGIS-USR-0000006");
		
		if(isValidateRegistrationCodeLife) {
			Calendar regisCodeValidity = Calendar.getInstance();
			regisCodeValidity.setTime(regis.getCodeGeneratedDate());
			regisCodeValidity.set(Calendar.SECOND, registrationCodeValidity);
			
			if(DateUtils.getCurrentTimestamp().compareTo(regisCodeValidity.getTime()) > 0)
				throw new BusinessException("GPT-REGIS-USR-0000007");
			
		}
		
		//check 4 digit mobile phone no
		if(!inputMobileNo.equals(regis.getMobileNo()))
			throw new BusinessException("GPT-REGIS-USR-0000006");
		
		if(!cardNo.equals(regis.getCardNo()))
			throw new BusinessException("GPT-REGIS-USR-0000006");
		
		if(!accountNo.equals(regis.getAccountNo()))
			throw new BusinessException("GPT-REGIS-USR-0000006");
	}
	
	@Override
	public List<String> findListOfStringNonFinancialMenu(String customerId, String nonFinMenuType)
			throws ApplicationException {
		try {
			CustomerModel customer = customerRepo.findOne(customerId);
			
			String customerRoleCode = customer.getRole().getCode();

			List<String> roleList = new ArrayList<>();
			roleList.add(customerRoleCode);
			
			return idmRepo.getMenuRepo().findListOfStringForNonFinancialMenuByRoleCode(roleList);
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> findMenuForPendingTask(String customerId)
			throws ApplicationException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			CustomerModel customer = customerRepo.findOne(customerId);

			String userGroupRole = customer.getRole().getCode();
			
			List<IDMMenuModel> menuList = idmRepo.getMenuRepo().findTransactionAndMaintenanceMenuByRoleCode(userGroupRole);
			
			List<Map<String, Object>> menus = new ArrayList<>();
			for (IDMMenuModel model : menuList) {
				Map<String, Object> modelMap = new HashMap<>();
				modelMap.put("pendingTaskCode", model.getCode());
				modelMap.put("pendingTaskName", model.getName());
				menus.add(modelMap);
			}
			resultMap.put("menus", menus);

		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> getUserProfiles(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String customerId = (String) map.get(ApplicationConstants.CUST_ID);
			CustomerModel customer = customerRepo.findOne(customerId);
			IDMUserModel idmUser = customer.getUser();
			
			resultMap.put(ApplicationConstants.CUST_ID, customerId);
			resultMap.put("customerName", customer.getName());
			
			resultMap.put("isNotifyMyTrx", customer.getIsNotifyMyTrx());
			resultMap.put("email", ValueUtils.getValue(idmUser.getEmail()));
			
			
			BranchModel branch = customer.getBranch();
			
			if(branch != null) {
				resultMap.put("customerBranchCode", branch.getCode());
				resultMap.put("customerBranchName", branch.getName());
			} else {
				resultMap.put("customerBranchCode", ApplicationConstants.EMPTY_STRING);
				resultMap.put("customerBranchName", ApplicationConstants.EMPTY_STRING);
			}
			
			resultMap.put("lastLoginDate", ValueUtils.getValue(idmUser.getLastLoginDate()));
			resultMap.put("imageUrl", ValueUtils.getValue(idmUser.getProfileImgUrl()));
			resultMap.put("isShowBeneficiary", ApplicationConstants.YES);
			
			CustomerTokenUserModel tokenUser = customerUtilsRepo.getTokenUserRepo().findByAssignedUserCode(customerId);
			
			if(tokenUser != null) {
				CustomerTokenTypeModel tokenType = tokenUser.getTokenType();
				resultMap.put("tokenTypeCode", tokenType.getCode());
				resultMap.put("tokenTypeName", tokenType.getName());
				
				resultMap.put(ApplicationConstants.LOGIN_TOKEN_NO, tokenUser.getTokenNo());
			}
			
			//get widget list for UI (requested)
			getWidget(resultMap);
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void getWidget(Map<String, Object> map) {
		List<Map<String, Object>> widgetList = new ArrayList<>();
		Map<String, Object> widgetData = new HashMap<>();
		widgetData = new HashMap<>();
		widgetData.put("service", "MNU_R_GPCASH_F_USER_DASHBOARD/searchCustomerAccount");
		widgetData.put("widget", "user_group");
		widgetList.add(widgetData);
		
		widgetData = new HashMap<>();
		widgetData.put("service", "MNU_R_GPCASH_F_USER_DASHBOARD/countTotalCreatedTrx");
		widgetData.put("widget", "created_transaction");
		widgetList.add(widgetData);
		
		widgetData = new HashMap<>();
		widgetData.put("service", "MNU_R_GPCASH_F_USER_DASHBOARD/countTotalExecutedTrx");
		widgetData.put("widget", "executed_transaction");
		widgetList.add(widgetData);
		
		widgetData = new HashMap<>();
		widgetData.put("service", "MNU_R_GPCASH_F_USER_DASHBOARD/findLimitUsage");
		widgetData.put("widget", "limit_usage");
		widgetList.add(widgetData);
		
		widgetData = new HashMap<>();
		widgetData.put("service", "MNU_R_GPCASH_F_USER_DASHBOARD/findCOT");
		widgetData.put("widget", "cot");
		widgetList.add(widgetData);
		
		map.put("widgetList", widgetList);
	}
	
	@Override
	public Map<String, Object> findLimitUsage(String customerId)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> limit = customerLimitRepo.findLimit(ApplicationConstants.APP_GPCASHIB_R, ApplicationConstants.SERVICE_TYPE_TRX, customerId, null);

			List<Map<String, Object>> limitList = new ArrayList<>();
			for (Object[] obj : limit) {
				Map<String, Object> modelMap = new HashMap<>();
				modelMap.put("serviceName", obj[2]);
				modelMap.put("currencyCode", obj[7]);
				modelMap.put("currencyName", obj[8]);
				modelMap.put("maxAmountLimit", obj[3]);
				modelMap.put("amountLimitUsage", obj[5]);
				limitList.add(modelMap);
			}
			resultMap.put("result", limitList);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public void updateUserNotificationFlag(String customerId, String notifyMyTrx) throws ApplicationException, BusinessException {
		try {
			CustomerModel customer = customerRepo.findOne(customerId);
			customer.setIsNotifyMyTrx(notifyMyTrx);
			customerRepo.save(customer);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public CustomerModel findByUserIdContainingIgnoreCase(String customerId)
			throws ApplicationException {
		try {
			return customerRepo.findByUserIdContainingIgnoreCase(customerId);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
}

