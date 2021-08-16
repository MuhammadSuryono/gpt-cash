package com.gpt.component.idm.usermanager.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMUserManagerServiceImpl implements IDMUserManagerService{
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String stillLoginFlag = Helper.getSearchWildcardValue((String) map.get("stillLoginFlag"));
			String applicationCode = Helper.getSearchWildcardValue((String) map.get(ApplicationConstants.APP_CODE));
			String userId = Helper.getSearchWildcardValue((String) map.get(ApplicationConstants.STR_CODE));
			
			Page<Object[]> result = idmUserRepo.findStillLoginUser(stillLoginFlag, 
					applicationCode, userId, PagingUtils.createPageRequest(map));
			
			Map<String, Object> resultMap = new HashMap<>();
			PagingUtils.setPagingInfo(resultMap, result);
			resultMap.put("result", setModelToMap(result.getContent()));
			return resultMap;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private List<Map<String, Object>> setModelToMap(List<Object[]> list){
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		for (Object[] model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.STR_CODE, model[0]);
			map.put(ApplicationConstants.STR_NAME, ValueUtils.hasValue(model[1])?model[1]:ApplicationConstants.EMPTY_STRING);
			map.put("appCode", ValueUtils.hasValue(model[2])?model[2]:ApplicationConstants.EMPTY_STRING);
			map.put("stillLoginFlag", ValueUtils.hasValue(model[3])?model[3]:ApplicationConstants.EMPTY_STRING);
			
			resultList.add(map);
		}
		
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateStillLoginFlag(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			List<String> userList = (List<String>)map.get("userList");
			if(ValueUtils.hasValue(userList)){
				idmUserRepo.updateStillLoginFlagByCodes(userList);
			}else{
				throw new BusinessException("GPT-0100010");
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateStillLoginFlagALL(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			idmUserRepo.updateStillLoginFlagByApplicationCodes((List<String>) map.get(ApplicationConstants.APP_CODE));
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

}
