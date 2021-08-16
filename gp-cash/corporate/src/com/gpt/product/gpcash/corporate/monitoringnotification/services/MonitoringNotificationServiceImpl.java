package com.gpt.product.gpcash.corporate.monitoringnotification.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.EAIConstants;

@Service
@Transactional(rollbackFor = Exception.class)
public class MonitoringNotificationServiceImpl implements MonitoringNotificationService{

	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Override
	public void executeEmail(String parameter) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		try{
			String email = maintenanceRepo.isSysParamValid(SysParamConstants.DEFAULT_EMAIL_MONITORING).getValue();
			
			Map<String, Object> inputs = new HashMap<String, Object>();
			inputs.put("email", email);
			inputs.put("subject", "Monitoring layanan ibank corporate");
			
			eaiAdapter.invokeService(EAIConstants.MONITORING_EMAIL_NOTIFICATION, inputs);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

}
