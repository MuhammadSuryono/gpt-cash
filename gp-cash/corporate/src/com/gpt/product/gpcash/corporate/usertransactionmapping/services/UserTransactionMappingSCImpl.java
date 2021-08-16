package com.gpt.product.gpcash.corporate.usertransactionmapping.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;

@Service
public class UserTransactionMappingSCImpl implements UserTransactionMappingSC {

	@Autowired
	private UserTransactionMappingService userTransactionMappingService;
	
	@Override
	public void resetTotalExecuted(String parameter) throws ApplicationException, BusinessException {
		userTransactionMappingService.resetTotalExecuted(parameter);
	}
}
