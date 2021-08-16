package com.gpt.product.gpcash.retail.login.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.login.services.IDMLoginService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerLoginServiceImpl implements CustomerLoginService {
	
	@Autowired
	private IDMLoginService idmLoginService;
	
	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;
	
	@Override
	public Map<String, Object> customerLogin(Map<String, Object> map) throws ApplicationException, BusinessException {
		String passwd = (String) map.get("passwd");
		String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERID);
		String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);

		try {
			//remark if stress test
			CustomerModel customer = customerUtilsRepo.getCustomerRepo().findByUserIdContainingIgnoreCase(loginUserId);
			
			if(customer == null)
				throw new BusinessException("GPT-0100032");
			
			if(ApplicationConstants.YES.equals(customer.getInactiveFlag()))
				throw new BusinessException("GPT-R-0100160");
			//---------------------------------------------------
			
			String custId = customer.getId();
			
			Map<String, Object> returnMap = idmLoginService.login(custId, passwd, ipAddress);
			
			//rewrite special for customer since customer have userCode and corpId
			returnMap.put(ApplicationConstants.CUST_ID, custId);
			
			return returnMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
}
