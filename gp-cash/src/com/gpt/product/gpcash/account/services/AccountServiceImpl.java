package com.gpt.product.gpcash.account.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class AccountServiceImpl implements AccountService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
    private EAIEngine eaiAdapter;
	
	@Override
	public void executeAccountSyncRequestScheduler(String parameter) throws ApplicationException, BusinessException {
		try {
			if(logger.isDebugEnabled())
				logger.debug("executeAccountSyncRequestScheduler");
			
			eaiAdapter.invokeService(EAIConstants.ACCOUNT_SYNC_REQUEST, null);
		
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void executeAccountSyncResponseScheduler(String parameter) throws ApplicationException, BusinessException {
		try {
			Date requestDate = DateUtils.getCurrentDate();
			String filename = null;
			
			if(parameter != null)
				filename = parameter;
			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("requestDate", requestDate);
			
			if(filename != null)
				inputs.put(ApplicationConstants.FILENAME, filename);
						
			if(logger.isDebugEnabled())
				logger.debug("executeAccountSyncResponseScheduler inputs : " + inputs);
			
			eaiAdapter.invokeService(EAIConstants.ACCOUNT_SYNC_RESPONSE, inputs);
		
		} catch (BusinessException e) {
			throw e;			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	} 
}
