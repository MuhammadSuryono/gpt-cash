package com.gpt.product.gpcash.corporate.logging.error.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.logging.error.model.CorporateErrorLogModel;
import com.gpt.product.gpcash.corporate.logging.error.repository.CorporateErrorLogRepository;
import com.gpt.product.gpcash.corporate.logging.error.valueobject.CorporateErrorLogVO;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateErrorLogServiceImpl implements CorporateErrorLogService{
	@Autowired
	private CorporateErrorLogRepository corporateErrorLogRepo;

	@Override
	public void saveCorporateErrorLog(CorporateErrorLogVO vo) throws Exception {
		CorporateErrorLogModel log = new CorporateErrorLogModel();
		log.setId(vo.getId());
		log.setActivityDate(DateUtils.getCurrentTimestamp());
		log.setReferenceNo(vo.getReferenceNo());
		log.setErrorTrace(vo.getErrorTrace());
		log.setCorporateId(vo.getCorporateId());
		log.setLogId(vo.getLogId());
		
		corporateErrorLogRepo.save(log);
	}
}
