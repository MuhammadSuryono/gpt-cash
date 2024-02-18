package com.gpt.component.idm.userlock.services;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMUserLockServiceImpl implements IDMUserLockService{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private IDMUserService idmUserService;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@SuppressWarnings("unchecked")
	@Override
	public void unlockUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			List<String> userList = (List<String>)map.get("userList");
			if(ValueUtils.hasValue(userList)){
				idmUserRepo.unlockUsers(userList);
			}else{
				throw new BusinessException("GPT-0100010");
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void unlockUser(String userCode)
			throws ApplicationException, BusinessException {
		try {
			IDMUserModel idmUser = idmRepo.isIDMUserValid(userCode);
			int ret = 0;
			if(idmUser.getLastLoginDate()==null) {
				 ret = idmUserRepo.lockUnlockUser(ApplicationConstants.IDM_USER_STATUS_RESET, userCode);
			}else {
				 ret = idmUserRepo.lockUnlockUser(ApplicationConstants.IDM_USER_STATUS_ACTIVE, userCode);
			}
			
			if (ret==0)
				throw new BusinessException("GPT-0100009");
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void lockUser(String userCode)
			throws ApplicationException, BusinessException {
		try {
			int ret = idmUserRepo.lockUnlockUser(ApplicationConstants.IDM_USER_STATUS_LOCKED, userCode);
			if (ret==0)
				throw new BusinessException("GPT-0100009");
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			map = idmUserService.search(map);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return map;
	}

}