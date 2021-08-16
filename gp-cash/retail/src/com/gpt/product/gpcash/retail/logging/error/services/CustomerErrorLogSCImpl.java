package com.gpt.product.gpcash.retail.logging.error.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.product.gpcash.retail.logging.error.valueobject.CustomerErrorLogVO;

@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class CustomerErrorLogSCImpl implements CustomerErrorLogSC{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustomerErrorLogService customerErrorLogService;
	
	@Override
	public void saveCustomerErrorLog(String id, String referenceNo, String customerErrorTrace, String customerId, String logId) {
		CustomerErrorLogVO vo = new CustomerErrorLogVO();
		try{
			vo.setId(id);
		    vo.setReferenceNo(referenceNo);
		    vo.setErrorTrace(customerErrorTrace);
		    vo.setLogId(logId);
		    vo.setCustomerId(customerId);
		    customerErrorLogService.saveCustomerErrorLog(vo);
		}catch(Exception e){
			logger.debug(" customerError  saveCustomerErrorLog : " + e.getMessage());
		}
	}

}
