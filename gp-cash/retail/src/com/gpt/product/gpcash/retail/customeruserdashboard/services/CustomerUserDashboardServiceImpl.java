package com.gpt.product.gpcash.retail.customeruserdashboard.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.retail.customeraccount.services.CustomerAccountService;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerUserDashboardServiceImpl implements CustomerUserDashboardService {
	@Autowired
	private CustomerAccountService customerAccountService;
	
	@Override
	public Map<String, Object> getCountCustomerAccount(String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			resultMap.put("total", customerAccountService.getCountCustomerAccount(customerId));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
}
