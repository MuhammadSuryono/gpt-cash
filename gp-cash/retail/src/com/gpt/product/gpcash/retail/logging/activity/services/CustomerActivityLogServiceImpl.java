package com.gpt.product.gpcash.retail.logging.activity.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.MDCHelper;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;
import com.gpt.product.gpcash.retail.customer.services.CustomerService;
import com.gpt.product.gpcash.retail.logging.activity.model.CustomerActivityLogModel;
import com.gpt.product.gpcash.retail.logging.activity.repository.CustomerActivityLogRepository;
import com.gpt.product.gpcash.retail.logging.activity.valueobject.CustomerActivityLogVO;
import com.gpt.product.gpcash.retail.logging.error.model.CustomerErrorLogModel;
import com.gpt.product.gpcash.retail.logging.error.repository.CustomerErrorLogRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerActivityLogServiceImpl implements CustomerActivityLogService {
//	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CustomerActivityLogRepository activityLogRepo;
	
	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private CustomerService customerService;
	
	@Autowired
	private CustomerErrorLogRepository errorLogRepo;
	
	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
	public void saveCustomerActivityLog(CustomerActivityLogVO vo) throws Exception {
		CustomerActivityLogModel log = new CustomerActivityLogModel();
		log.setActivityDate(vo.getActivityDate());
		log.setMenuCode(vo.getMenuCode());
		log.setMenuName(vo.getMenuName());
		log.setActionType(vo.getActionType());
		log.setActionBy(vo.getActionBy());
		log.setIsError(vo.isError() ? ApplicationConstants.YES : ApplicationConstants.NO);
		log.setErrorCode(vo.getErrorCode());
		log.setErrorDescription(vo.getErrorDescription());
		log.setReferenceNo(vo.getReferenceNo());
		
		activityLogRepo.save(log);
		
		vo.setId(log.getId());
		if(vo.isError()) {
			CustomerErrorLogModel elog = new CustomerErrorLogModel();
			elog.setId(vo.getId());
			elog.setActivityDate(vo.getActivityDate());
			elog.setReferenceNo(vo.getReferenceNo());
			elog.setActionBy(vo.getActionBy());
			elog.setErrorTrace(vo.getErrorTrace());
			elog.setLogId(MDCHelper.getTraceId());
			
			errorLogRepo.persist(elog);
		}
	}
	
	@Override
	public Map<String, Object> getActivityByUser(Map<String, Object> map) throws ApplicationException, BusinessException{
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Tuple> result = activityLogRepo.searchActivityLog(map, PagingUtils.createPageRequest(map));
			
			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<Tuple> list) {
		List<Map<String, Object>> resultList = new ArrayList<>(list.size());

		for (Tuple tuple : list) {
			resultList.add(setModelToMap((CustomerActivityLogModel)tuple.get("act"), (String)tuple.get("name")));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(CustomerActivityLogModel activity, String userName) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", activity.getId());
		map.put("actionBy", userName);
		map.put("actionDate", activity.getActivityDate());
		map.put("actionType", activity.getActionType());
		map.put("isError",  ValueUtils.getValue(activity.getIsError()));
		map.put("errorCode", ValueUtils.getValue(activity.getErrorCode()));
		map.put("errorDescription", ValueUtils.getValue(activity.getErrorDescription()));
		map.put(ApplicationConstants.STR_MENUCODE, activity.getMenuCode());
		map.put("menuName", ValueUtils.getValue(activity.getMenuName()));
		map.put("referenceNo", ValueUtils.getValue(activity.getReferenceNo()));
		
		return map;
	}
	
	@Override
	public Map<String, Object> getNonFinancialActvity(Map<String, Object> map) throws ApplicationException, BusinessException{
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
			if(!ValueUtils.hasValue(referenceNo)) {
				String customerId = (String) map.get(ApplicationConstants.CUST_ID);
				String menuType = (String) map.get("menuType");
				map.put("activityLogMenuList", customerService.findListOfStringNonFinancialMenu(customerId, menuType));
				
				String actionByUserId = (String) map.get("actionByUserId");
				if(ValueUtils.hasValue(actionByUserId)) {
					String userCode = Helper.getCustomerUserCode(customerId, actionByUserId);
					map.put("actionBy", userCode);
				}
			}
			
			String status = (String) map.get("status");
			if(ValueUtils.hasValue(status) && !ApplicationConstants.ALL_STS.equals(status)) {
				String isErrorFlag = ApplicationConstants.NO;
				if(status.equals(ApplicationConstants.FAILED_STS)) {
					isErrorFlag = ApplicationConstants.YES;
				}
				map.put("isError", isErrorFlag);
			}
			Page<Tuple> result = activityLogRepo.searchActivityLog(map, PagingUtils.createPageRequest(map));
			
			resultMap.put("result", setModelToMapForNonFinancial(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMapForNonFinancial(List<Tuple> list) {
		List<Map<String, Object>> resultList = new ArrayList<>(list.size());

		for (Tuple tuple : list) {
			resultList.add(setModelToMapForNonFinancial((CustomerActivityLogModel)tuple.get("act"), (String)tuple.get("name")));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMapForNonFinancial(CustomerActivityLogModel activity, String userName) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", activity.getId());
		
		IDMUserModel idmUser = idmUserRepo.findOne(activity.getActionBy());
		map.put("actionByUserId", idmUser.getUserId());
		map.put("actionByUserName", idmUser.getName());
		
		map.put("actionDate", activity.getActivityDate());
		map.put("actionType", activity.getActionType());
		
		String status = ApplicationConstants.SUCCESS_STS;
		if(ApplicationConstants.YES.equals(activity.getIsError())) {
			status = ApplicationConstants.FAILED_STS;
		}
		map.put("status", status);
		
		map.put("errorCode", ValueUtils.getValue(activity.getErrorCode()));
		map.put("errorDescription", ValueUtils.getValue(activity.getErrorDescription()));
		map.put("activityLogMenuCode", activity.getMenuCode());
		map.put("activityLogMenuName", ValueUtils.getValue(activity.getMenuName()));
		map.put("referenceNo", ValueUtils.getValue(activity.getReferenceNo()));
		
		CustomerModel customer = customerRepo.findOne(activity.getActionBy());
		map.put("customerId", customer.getId());
		map.put("customerName", customer.getName());

		return map;
	}
}
