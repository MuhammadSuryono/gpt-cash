package com.gpt.product.gpcash.corporate.corporateadmindashboard.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporateaccount.model.CorporateAccountModel;
import com.gpt.product.gpcash.corporate.corporateaccount.services.CorporateAccountService;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.services.CorporateUserGroupService;

@Service
public class CorporateAdminDashboardServiceImpl implements CorporateAdminDashboardService {

	@Autowired
	private CorporateUserGroupService corporateUserGroupService;

	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;

	@Autowired
	private CorporateAccountService corporateAccountService;

	@Autowired
	private CorporateUserRepository corporateUserRepo;
	
	@Override
	public Map<String, Object> searchCorporateAccountGroup(String corporateId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			List<Map<String, Object>> corporateAccountGroupList = corporateAccountGroupService
					.searchByCorporateId(corporateId, false);
			resultMap.put("total", corporateAccountGroupList.size());
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchCorporateUserGroup(String corporateId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			Map<String, Object> map = corporateUserGroupService.searchUserGroupByCorporateId(corporateId);
			List<CorporateUserGroupModel> corporateUserGroupList = (List<CorporateUserGroupModel>) map.get("result"); 
			resultMap.put("total", corporateUserGroupList.size());
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchCorporateAccount(String corporateId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			List<CorporateAccountModel> corporateAccountList = corporateAccountService.searchByCorporateId(corporateId);
			resultMap.put("total", corporateAccountList.size());
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchCorporateUser(String corporateId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			List<CorporateUserModel> corporateUserList = corporateUserRepo.findByCorporateIdAndDeleteFlagExcludeAdmin(corporateId, ApplicationConstants.NO);
			resultMap.put("total", corporateUserList.size());
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
}
