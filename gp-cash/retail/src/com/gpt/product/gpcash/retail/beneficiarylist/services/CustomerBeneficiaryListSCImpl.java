package com.gpt.product.gpcash.retail.beneficiarylist.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.maintenance.domesticbank.services.DomesticBankService;
import com.gpt.component.maintenance.internationalbank.services.InternationalBankService;
import com.gpt.component.maintenance.parametermt.services.ParameterMaintenanceService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.retail.logging.annotation.EnableCustomerActivityLog;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserPendingTaskService;
import com.gpt.product.gpcash.retail.pendingtaskuser.valueobject.CustomerUserPendingTaskVO;

@Validate
@Service
public class CustomerBeneficiaryListSCImpl implements CustomerBeneficiaryListSC {

	@Autowired
	private CustomerBeneficiaryListInHouseService beneficiaryListInHouseService;

	@Autowired
	private CustomerBeneficiaryListDomesticService beneficiaryListDomesticService;

	@Autowired
	private CustomerBeneficiaryListInternationalService beneficiaryListInternationalService;
	
	@Autowired
	private DomesticBankService domesticBankService;
	
	@Autowired
	private ParameterMaintenanceService parameterMaintenanceService;
	
	@Autowired
	private InternationalBankService internationalBankService;

	@Autowired
	private CustomerUserPendingTaskService customerUserPendingTaskService;


	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	// -----------------------------------InHouse----------------------------------------------------
	@EnableCustomerActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "benAccountNo", required = false), @Variable(name = "benAccountName", required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListInHouseService.search(map);
	}

	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName") 
	})
	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListInHouseService.searchOnline(map);
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName"),
		@Variable(name = "benAccountCurrency"),
		@Variable(name = "isNotify", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "email", format = Format.EMAIL, required = false), 
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "CREATE_OVERBOOKING", "UPDATE_OVERBOOKING" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		Map<String, Object> resultMap = beneficiaryListInHouseService.submit(map);
		CustomerUserPendingTaskVO vo = (CustomerUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
		
		customerUserPendingTaskService.approve(vo, beneficiaryListInHouseService);
		
		resultMap = new HashMap<>();
		String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
		resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-R-0200005");
		resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
		resultMap.put("dateTime", strDateTime);
		
		return resultMap;
	}

	@EnableCustomerActivityLog
	@Override
	public CustomerUserPendingTaskVO approve(CustomerUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		
		if ("CREATE_DOMESTIC".equals(vo.getAction()) || "UPDATE_DOMESTIC".equals(vo.getAction()) ||
				"DELETE_DOMESTIC".equals(vo.getAction()) || "DELETE_DOMESTIC_LIST".equals(vo.getAction())) {
			return beneficiaryListDomesticService.approve(vo);
		} else if ("CREATE_INTERNATIONAL".equals(vo.getAction()) || "UPDATE_INTERNATIONAL".equals(vo.getAction()) ||
				"DELETE_INTERNATIONAL".equals(vo.getAction()) || "DELETE_INTERNATIONAL_LIST".equals(vo.getAction())) {
			return beneficiaryListInternationalService.approve(vo); 
		} else {
			return beneficiaryListInHouseService.approve(vo);
		}
	}

	@EnableCustomerActivityLog
	@Override
	public CustomerUserPendingTaskVO reject(CustomerUserPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		if ("CREATE_DOMESTIC".equals(vo.getAction()) || "UPDATE_DOMESTIC".equals(vo.getAction()) ||
				"DELETE_DOMESTIC".equals(vo.getAction()) || "DELETE_DOMESTIC_LIST".equals(vo.getAction())) {
			return beneficiaryListDomesticService.reject(vo);
		} else {
			return beneficiaryListInHouseService.reject(vo);
		}
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_OVERBOOKING" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		Map<String, Object> resultMap = beneficiaryListInHouseService.submit(map);
		CustomerUserPendingTaskVO vo = (CustomerUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
		
		customerUserPendingTaskService.approve(vo, beneficiaryListInHouseService);
		
		resultMap = new HashMap<>();
		String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
		resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-R-0200005");
		resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
		resultMap.put("dateTime", strDateTime);
		
		return resultMap;
		
	}
	
	@EnableCustomerActivityLog
	@Validate
	@Input({
		@Variable(name = "benAccountList", type = List.class, subVariables = {
			@SubVariable(name = "benAccountNo"),
		}),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_OVERBOOKING_LIST" }),
		@Variable(name = "menuCode", options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Override
	public Map<String, Object> submitDeleteList(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		Map<String, Object> resultMap = beneficiaryListInHouseService.submit(map);
		CustomerUserPendingTaskVO vo = (CustomerUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
		
		customerUserPendingTaskService.approve(vo, beneficiaryListInHouseService);
		
		resultMap = new HashMap<>();
		String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
		resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-R-0200005");
		resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
		resultMap.put("dateTime", strDateTime);
		
		return resultMap;
	}

	// -----------------------------------Domestic----------------------------------------------------
	@EnableCustomerActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "benAccountNo", required = false), @Variable(name = "benAccountName", required = false),
		@Variable(name = "bankCode", required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchDomestic(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListDomesticService.search(map);
	}

	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_SEARCH }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName") 
	})
	@Override
	public Map<String, Object> searchOnlineDomestic(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return beneficiaryListDomesticService.searchOnline(map);
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"), @Variable(name = "benAccountName"), @Variable(name = "benAliasName"),
		@Variable(name = "address1", required = false), @Variable(name = "address2", required = false),
		@Variable(name = "address3", required = false),
		@Variable(name = "isBenResident", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "benResidentCountryCode", required = false),
		@Variable(name = "isBenCitizen", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "benCitizenCountryCode", required = false), @Variable(name = "beneficiaryTypeCode"),
		@Variable(name = "bankCode"),
		@Variable(name = "isNotify", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "email", required = false, format = Format.EMAIL),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "CREATE_DOMESTIC", "UPDATE_DOMESTIC" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Override
	public Map<String, Object> submitDomestic(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		Map<String, Object> resultMap = beneficiaryListDomesticService.submit(map);
		CustomerUserPendingTaskVO vo = (CustomerUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
		
		customerUserPendingTaskService.approve(vo, beneficiaryListDomesticService);
		
		resultMap = new HashMap<>();
		String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
		resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-R-0200005");
		resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
		resultMap.put("dateTime", strDateTime);
		
		return resultMap;
		
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_DOMESTIC" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Override
	public Map<String, Object> submitDeleteDomestic(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		Map<String, Object> resultMap = beneficiaryListDomesticService.submit(map);
		CustomerUserPendingTaskVO vo = (CustomerUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
		
		customerUserPendingTaskService.approve(vo, beneficiaryListDomesticService);
		
		resultMap = new HashMap<>();
		String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
		resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-R-0200005");
		resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
		resultMap.put("dateTime", strDateTime);
		
		return resultMap;
	}
	
	@EnableCustomerActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountList", type = List.class, subVariables = {
			@SubVariable(name = "benAccountNo"),
		}),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_DOMESTIC_LIST" }),
		@Variable(name = "menuCode", options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Override
	public Map<String, Object> submitDeleteDomesticList(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		Map<String, Object> resultMap = beneficiaryListDomesticService.submit(map);
		CustomerUserPendingTaskVO vo = (CustomerUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
		
		customerUserPendingTaskService.approve(vo, beneficiaryListDomesticService);
		
		resultMap = new HashMap<>();
		String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
		resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
		resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-R-0200005");
		resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
		resultMap.put("dateTime", strDateTime);
		
		return resultMap;
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.STR_CODE, required = false), 
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchBankForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		return domesticBankService.search(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchBeneficiaryTypeForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_BENEFICIARY_TYP"); 
		return parameterMaintenanceService.searchModel(map);
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_SEARCH), 
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		map.put("modelCode", "COM_MT_COUNTRY"); 
		return parameterMaintenanceService.searchModel(map);
	}

	@EnableCustomerActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "benAccountNo", required = false), @Variable(name = "benAccountName", required = false),
		@Variable(name = "bankCode", required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> searchInternational(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListInternationalService.search(map);
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = "benAccountName"),
		@Variable(name = "benAliasName"),
		@Variable(name = "address1", required = false), 
		@Variable(name = "address2", required = false),
		@Variable(name = "address3", required = false),
		@Variable(name = "isBenResident", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "benResidentCountryCode", required = false),
		@Variable(name = "isBenCitizen", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "benCitizenCountryCode", required = false),
		@Variable(name = "bankCode"),
		@Variable(name = "intCountryCode"),
		@Variable(name = "isNotify", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "email", required = false, format = Format.EMAIL),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "CREATE_INTERNATIONAL", "UPDATE_INTERNATIONAL" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Override
	public Map<String, Object> submitInternational(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListInternationalService.submit(map);
	}
	
	@EnableCustomerActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountNo"),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_INTERNATIONAL" }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Override
	public Map<String, Object> submitDeleteInternational(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListInternationalService.submit(map);
	}

	@EnableCustomerActivityLog
	@Validate
	@Input({ 
		@Variable(name = "benAccountList", type = List.class, subVariables = {
			@SubVariable(name = "benAccountNo"),
		}),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { "DELETE_INTERNATIONAL_LIST" }),
		@Variable(name = "menuCode", options = menuCode) 
	})
	@Output({ 
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N) 
	})
	@Override
	public Map<String, Object> submitDeleteInternationalList(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneficiaryListInternationalService.submit(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "countryCode", required = false),
		@Variable(name = ApplicationConstants.CUST_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = ApplicationConstants.WF_ACTION_DETAIL)
	})
	@Override
	public Map<String, Object> searchBankByCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		return internationalBankService.search(map);
	}
}
