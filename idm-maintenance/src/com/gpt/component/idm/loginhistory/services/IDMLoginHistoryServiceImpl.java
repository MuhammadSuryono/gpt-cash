package com.gpt.component.idm.loginhistory.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.loginhistory.model.IDMLoginHistoryModel;
import com.gpt.component.idm.loginhistory.repository.IDMLoginHistoryRepository;
import com.gpt.platform.cash.utils.DateUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMLoginHistoryServiceImpl implements IDMLoginHistoryService {

	@Autowired
	private IDMLoginHistoryRepository loginHistoryRepo;

	@Override
	public IDMLoginHistoryModel saveLoginHistory(String userCode, String applicationCode, String ipAddress)
			throws ApplicationException, BusinessException {

		try {
			IDMLoginHistoryModel loginHistory = new IDMLoginHistoryModel();

			loginHistory.setApplicationCode(applicationCode);
			loginHistory.setUserCode(userCode);
			loginHistory.setLoginDate(DateUtils.getCurrentTimestamp());
			loginHistory.setIpAddr(ipAddress);

			loginHistoryRepo.persist(loginHistory);

			return loginHistory;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

	}

	@Override
	public void updateLogoutLoginHistory(String loginHistoryId) throws ApplicationException, BusinessException {
		try {
			loginHistoryRepo.updateLogoutDate(loginHistoryId, DateUtils.getCurrentTimestamp());
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void updateLogoutLoginHistories(List<String> loginHistoryIds) throws ApplicationException, BusinessException {
		try {
			loginHistoryRepo.updateLogoutDate(loginHistoryIds, DateUtils.getCurrentTimestamp());
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public IDMLoginHistoryModel getLoginHistoryById(String loginHistoryId)
			throws ApplicationException, BusinessException {
		try {
			return loginHistoryRepo.findOne(loginHistoryId);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

}
