package com.gpt.product.gpcash.retail.logging.error.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.retail.logging.error.model.CustomerErrorLogModel;
import com.gpt.product.gpcash.retail.logging.error.repository.CustomerErrorLogRepository;
import com.gpt.product.gpcash.retail.logging.error.valueobject.CustomerErrorLogVO;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerErrorLogServiceImpl implements CustomerErrorLogService{
	@Autowired
	private CustomerErrorLogRepository customerErrorLogRepo;

	@Override
	public void saveCustomerErrorLog(CustomerErrorLogVO vo) throws Exception {
		CustomerErrorLogModel log = new CustomerErrorLogModel();
		log.setId(vo.getId());
		log.setActivityDate(DateUtils.getCurrentTimestamp());
		log.setReferenceNo(vo.getReferenceNo());
		log.setErrorTrace(vo.getErrorTrace());
		log.setActionBy(vo.getCustomerId());
		log.setLogId(vo.getLogId());
		
		customerErrorLogRepo.save(log);
	}
}
