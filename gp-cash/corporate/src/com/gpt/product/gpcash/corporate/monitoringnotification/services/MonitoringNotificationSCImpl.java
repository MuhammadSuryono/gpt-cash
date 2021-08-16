package com.gpt.product.gpcash.corporate.monitoringnotification.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Validate;

@Validate
@Service
public class MonitoringNotificationSCImpl implements MonitoringNotificationSC {

	@Autowired
	private MonitoringNotificationService monitoringNotificationService;
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public void executeEmail(String parameter) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		monitoringNotificationService.executeEmail(parameter);
	}

}
