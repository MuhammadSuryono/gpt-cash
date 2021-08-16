package com.gpt.product.gpcash.corporate.inquiry.sp2d.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.platform.cash.constants.EAIConstants;

@Service
@Transactional(rollbackFor = Exception.class)
public class SP2DInquiryServiceImpl implements SP2DInquiryService {

	@Autowired
	private EAIEngine eaiAdapter;

	@Override
	public Map<String, Object> searchOnline(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("sp2dNo", map.get("sp2dNo"));
			inputs.put("pemdaCode", map.get("pemdaCode"));

			return eaiAdapter.invokeService(EAIConstants.SP2D_INQUIRY, inputs);

			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

}
