package com.gpt.product.gpcash.corporate.corporateuserdashboard.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateUserDashboardServiceImpl implements CorporateUserDashboardService {
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private CorporateUserRepository corporateUserRepo;
	
	@Override
	public Map<String, Object> getCountCorporateIdAndGroupId(String corporateId, String userCode) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			CorporateUserModel corporateUser = corporateUserRepo.findOne(userCode);
			resultMap.put("total", corporateAccountGroupService.
					getCountCorporateIdAndGroupId(corporateId, corporateUser.getCorporateUserGroup().
							getCorporateAccountGroup().getId()));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
}
