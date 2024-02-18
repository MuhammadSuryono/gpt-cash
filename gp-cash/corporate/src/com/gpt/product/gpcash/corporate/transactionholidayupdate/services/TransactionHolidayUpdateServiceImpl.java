package com.gpt.product.gpcash.corporate.transactionholidayupdate.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.Util;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.transactionholidayupdate.model.HolidayTransactionModel;
import com.gpt.product.gpcash.corporate.transactionholidayupdate.repository.HolidayTransactionRepository;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.model.TransactionStatusMappingModel;
import com.gpt.product.gpcash.corporate.transactionstatusservicemapping.repository.TransactionStatusMappingRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class TransactionHolidayUpdateServiceImpl implements TransactionHolidayUpdateService {

	@Autowired
	private ApplicationContext appCtx;
	
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private HolidayTransactionRepository holidayTransactionRepo;
	
	@Autowired
	private TransactionStatusMappingRepository transactionStatusMappingRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
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
		vo.setUniqueKey((String) map.get("trxRefNo"));
		vo.setUniqueKeyDisplay((String) map.get("trxRefNo"));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("TransactionHolidayUpdateSC");

		return vo;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			HolidayTransactionModel model = holidayTransactionRepo.findOne((String)map.get("holidayTrxId"));
			
			String executedMenuCode = model.getMenu().getCode();
			
			String service = ((TransactionStatusMappingModel) transactionStatusMappingRepo.findOne(executedMenuCode)).getMenuService();
			
			if (ApplicationConstants.WF_ACTION_UPDATE_STATUS.equals(vo.getAction())) {
				
				map.put("pendingTaskId",model.getPendingTaskId());
				Map<String, Object> resultMap = Util.invokeSpringBean(appCtx, service, "updateHolidayTransaction", map);
				
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> searchHolidayTransactionByReferenceNo(Map<String, Object> map)
			throws Exception {
		// TODO Auto-generated method stub
		
		Page<HolidayTransactionModel> trxHolidays = holidayTransactionRepo.findHolidayTransactions(map, PagingUtils.createPageRequest(map));		
		
		List<HolidayTransactionModel> list = trxHolidays.getContent();
				
		Map<String, Object> resultMap = new HashMap<>();
		
		List<Map<String, Object>> resultList = new ArrayList<>(list.size());

		for (HolidayTransactionModel model : list) {
			resultList.add(setModelToMap(model));
		}

		resultMap.put("result", resultList);

		PagingUtils.setPagingInfo(resultMap, trxHolidays);
		
		return resultMap;
	}
	
	private Map<String, Object> setModelToMap(HolidayTransactionModel model) {
		Map<String, Object> map = new HashMap<>();
		
		CorporateModel corporate = model.getCorporate();
		map.put("corporateId", corporate.getId());
		map.put("corporateName", corporate.getName());
		map.put("referenceNo", model.getReferenceNo());
		
		IDMUserModel idmUser = idmUserRepo.findOne(model.getCreatedBy());
		map.put("createdByUserId", idmUser.getUserId());
		map.put("createdByUserName", idmUser.getName());
		map.put("pendingTaskId", model.getId());
		map.put("menuCode", model.getMenu().getCode());
		map.put("menuName", model.getMenu().getName());
		map.put("sourceAccount", ValueUtils.getValue(model.getSourceAccount().getAccountNo()));
		map.put("sourceAccountName", ValueUtils.getValue(model.getSourceAccount().getAccountName()));
		map.put("sourceAccountCurrencyCode", ValueUtils.getValue(model.getSourceAccount().getCurrency().getCode()));
		map.put("sourceAccountCurrencyName", ValueUtils.getValue(model.getSourceAccount().getCurrency().getName()));
		map.put("transactionAmount", ValueUtils.getValue(model.getTransactionAmount()));
		map.put("transactionCurrency", ValueUtils.getValue(model.getTransactionCurrency()));
		map.put("status", "PENDING_EXECUTE");
		map.put("id", model.getId());

		return map;
	}
}
