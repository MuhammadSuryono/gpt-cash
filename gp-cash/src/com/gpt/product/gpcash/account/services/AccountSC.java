package com.gpt.product.gpcash.account.services;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface AccountSC {

	void executeAccountSyncRequestScheduler(String parameter) throws ApplicationException, BusinessException;

	void executeAccountSyncResponseScheduler(String parameter) throws ApplicationException, BusinessException;

}
