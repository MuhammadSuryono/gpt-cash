package com.gpt.product.gpcash.corporate.outsourceadmin.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.outsourceadmin.model.OutsourceAdminLoginModel;
import com.gpt.product.gpcash.corporate.outsourceadmin.repository.OutsourceAdminRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class OutsourceAdminServiceImpl implements OutsourceAdminService{

	@Autowired
	private OutsourceAdminRepository osRepository;
	
	@Override
	public Map<String, Object> saveForLogin(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		String corpId = map.get(ApplicationConstants.CORP_ID).toString();
		
		//delete old data
		osRepository.deleteByCorpId(corpId);
		
		//save new Data
		CorporateModel corporate = new CorporateModel();
		corporate.setId(corpId);
		
		OutsourceAdminLoginModel modelMaker = new OutsourceAdminLoginModel();
		modelMaker.setCorporate(corporate);
		modelMaker.setUserId("OSADMINMAKER");
		modelMaker.setCreatedDate(DateUtils.getCurrentTimestamp());
		modelMaker = osRepository.save(modelMaker);
		
		OutsourceAdminLoginModel modelApp = new OutsourceAdminLoginModel();
		modelApp.setCorporate(corporate);
		modelApp.setUserId("OSADMINAPPROVER");
		modelApp.setCreatedDate(DateUtils.getCurrentTimestamp());
		modelApp = osRepository.save(modelApp);
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("sIdMaker", modelMaker.getId());	
		returnMap.put("sIdApp", modelApp.getId());
		
		return returnMap;
	}

}
