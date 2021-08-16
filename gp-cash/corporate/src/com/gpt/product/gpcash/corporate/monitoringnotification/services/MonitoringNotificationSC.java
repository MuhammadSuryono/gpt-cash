package com.gpt.product.gpcash.corporate.monitoringnotification.services;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface MonitoringNotificationSC {

	void executeEmail(String parameter) throws ApplicationException, BusinessException;
}
