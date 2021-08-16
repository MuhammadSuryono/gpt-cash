package com.gpt.product.gpcash.corporate.logging.error.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import com.gpt.product.gpcash.corporate.logging.error.valueobject.CorporateErrorLogVO;

@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class CorporateErrorLogSCImpl implements CorporateErrorLogSC{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CorporateErrorLogService corporateErrorLogService;
	
	@Override
	public void saveCorporateErrorLog(String id, String referenceNo, String corporateErrorTrace, String corporateId, String logId) {
		CorporateErrorLogVO vo = new CorporateErrorLogVO();
		try{
			vo.setId(id);
		    vo.setReferenceNo(referenceNo);
		    vo.setErrorTrace(corporateErrorTrace);
		    vo.setLogId(logId);
		    vo.setCorporateId(corporateId);
		    corporateErrorLogService.saveCorporateErrorLog(vo);
		}catch(Exception e){
			logger.debug(" corporateError  saveCorporateErrorLog : " + e.getMessage());
		}
	}

}
