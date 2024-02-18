package com.gpt.product.gpcash.retail.beneficiarylist.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.retail.pendingtaskuser.services.CustomerUserWorkflowService;

@AutoDiscoveryImpl
public interface CustomerBeneficiaryListSC extends CustomerUserWorkflowService {
	String menuCode = "MNU_R_GPCASH_F_FUND_BENEFICIARY";
	
	//-----------------------------------InHouse----------------------------------------------------

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	//-----------------------------------Domestic----------------------------------------------------
	
	Map<String, Object> searchDomestic(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchOnlineDomestic(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDomestic(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDeleteDomestic(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBankForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitDeleteList(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitDeleteDomesticList(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDomesticOnline(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	//-----------------------------------International----------------------------------------------------
	
	Map<String, Object> searchInternational(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitInternational(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDeleteInternational(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> submitDeleteInternationalList(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchBankByCountryForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchBeneficiaryTypeForDroplist(Map<String, Object> map)
			throws ApplicationException, BusinessException;
	
	//------------------------------------Virtual Account---------------------------------------------------------
	
		Map<String, Object> searchVirtualAccount(Map<String, Object> map) throws ApplicationException, BusinessException;
}
