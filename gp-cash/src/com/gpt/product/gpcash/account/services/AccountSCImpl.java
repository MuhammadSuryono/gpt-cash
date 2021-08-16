package com.gpt.product.gpcash.account.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;

@Service
public class AccountSCImpl implements AccountSC {
	
	@Autowired
	private AccountService accountService;
	
	@Override
	public void executeAccountSyncRequestScheduler(String parameter) throws ApplicationException, BusinessException {
		accountService.executeAccountSyncRequestScheduler(parameter);
	}

	@Override
	public void executeAccountSyncResponseScheduler(String parameter) throws ApplicationException, BusinessException {
		accountService.executeAccountSyncResponseScheduler(parameter);
	}
}
