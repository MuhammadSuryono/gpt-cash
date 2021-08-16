package com.gpt.product.gpcash.corporate.transactionupdate.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.Util;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.model.TransactionStatusMappingModel;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.repository.TransactionStatusMappingRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class TransactionUpdateServiceImpl implements TransactionUpdateService {
	
	@Autowired
	private ApplicationContext appCtx;
	
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private TransactionStatusMappingRepository transactionStatusMappingRepo;
	

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			vo.setAction(ApplicationConstants.WF_ACTION_UPDATE_STATUS);

			resultMap.putAll(pendingTaskService.savePendingTask(vo)); 
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("TransactionUpdateSC");

		return vo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			String executedMenuCode = (String) map.get("executedMenuCode");
			
			String service = ((TransactionStatusMappingModel) transactionStatusMappingRepo.findOne(executedMenuCode)).getMenuService();
			
			if (ApplicationConstants.WF_ACTION_UPDATE_STATUS.equals(vo.getAction())) {
				
				
				Map<String, Object> resultMap = Util.invokeSpringBean(appCtx, service, "updateTransaction", map);
				
			} 
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}	
	
}