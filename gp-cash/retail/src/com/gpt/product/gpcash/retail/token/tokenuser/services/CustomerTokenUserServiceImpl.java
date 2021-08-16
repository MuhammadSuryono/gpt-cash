package com.gpt.product.gpcash.retail.token.tokenuser.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;
import com.gpt.product.gpcash.retail.token.tokentype.model.CustomerTokenTypeModel;
import com.gpt.product.gpcash.retail.token.tokenuser.model.CustomerTokenUserModel;
import com.gpt.product.gpcash.retail.token.tokenuser.repository.CustomerTokenUserRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTokenUserServiceImpl implements CustomerTokenUserService {
	
	@Autowired
	private CustomerTokenUserRepository tokenUserRepo;
	
	@Autowired
	private CustomerRepository customerRepo;

	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private EAIEngine eaiAdapter;	
	
	@Autowired
	private IDMUserRepository idmUserRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerTokenUserModel> result = tokenUserRepo.search(map, PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<CustomerTokenUserModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerTokenUserModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerTokenUserModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("tokenNo", model.getTokenNo());

		CustomerTokenTypeModel tokenType = model.getTokenType();
		map.put("tokenTypeCode", tokenType.getCode());
		map.put("tokenTypeName", tokenType.getName());
		
		IDMUserModel assignedUser = model.getAssignedUser();
		
		if (assignedUser != null) {
			map.put("customerId", assignedUser.getCode());
			
			CustomerModel customerUser = customerRepo.findOne(assignedUser.getCode());
			map.put("userId", customerUser.getUserId());
			map.put("userName", assignedUser.getName());
		}else {
			map.put("customerId", ApplicationConstants.EMPTY_STRING);
			map.put("userId", ApplicationConstants.EMPTY_STRING);
			map.put("userName", ApplicationConstants.EMPTY_STRING);
		}

		
		IDMUserModel registeredBy = model.getRegisteredBy();
		if(registeredBy != null){
			map.put("registeredByCode", registeredBy.getCode());
			map.put("registeredBy", registeredBy.getName());
			map.put("registeredDate", model.getRegisteredDate());
		}else{
			map.put("registeredByCode", ApplicationConstants.EMPTY_STRING);
			map.put("registeredBy", ApplicationConstants.EMPTY_STRING);
			map.put("registeredDate", ApplicationConstants.EMPTY_STRING);
		}
		
		IDMUserModel assignedBy = model.getAssignedBy();
		if(assignedBy != null){
			map.put("assignedByCode", assignedBy.getCode());
			map.put("assignedBy", assignedBy.getName());
			map.put("assignedDate", ValueUtils.getValue(model.getAssignedDate()));
		}else{
			map.put("assignedByCode", ApplicationConstants.EMPTY_STRING);
			map.put("assignedBy", ApplicationConstants.EMPTY_STRING);
			map.put("assignedDate", ApplicationConstants.EMPTY_STRING);
		}
		map.put("status", ValueUtils.getValue(model.getStatus()));
		map.put("retry", ValueUtils.getValue(model.getRetry()));

		map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
		map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));

		return map;
	}

	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check unique tokenNo
				String tokenNo = (String) map.get("tokenNo");
				checkUniqueRecord(tokenNo);

			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UNASSIGN")) {
				vo.setAction((String) map.get(ApplicationConstants.WF_ACTION));
				
				String customerId = (String) map.get(ApplicationConstants.CUST_ID);
				CustomerTokenUserModel token = tokenUserRepo.findByAssignedUserCode(customerId);
				
				if(token == null)
					throw new BusinessException("GPT-0100070");
				
				if(!ValueUtils.hasValue(token.getTokenNo()))
					throw new BusinessException("GPT-0100070");
				
			} else {
				throw new BusinessException("GPT-0100003");
			}

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
		vo.setUniqueKey((String) map.get(ApplicationConstants.CUST_ID));
		vo.setUniqueKeyDisplay((String) map.get(ApplicationConstants.STR_NAME));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CustomerTokenUserSC");

		return vo;
	}

	private void checkUniqueRecord(String tokenNo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("tokenNo", tokenNo);
		Page<CustomerTokenUserModel> result = (Page<CustomerTokenUserModel>) tokenUserRepo.search(map, null);

		if (result.getContent().size() > 0) {
			throw new BusinessException("GPT-0100088");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			String customerId = (String) map.get(ApplicationConstants.CUST_ID);
			
			if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {

				String tokenNo = (String) map.get("tokenNo");
				saveCustomerTokenUserAndAssign(tokenNo, vo.getCreatedBy(), 
						customerId, ApplicationConstants.TOKEN_TYPE_HARD_TOKEN);
				
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("custId", customerId);
				inputs.put("tokenNo", tokenNo);
				
//				eaiAdapter.invokeService(EAIConstants.ASSIGN_TOKEN, inputs);

			} else if ("UNASSIGN".equals(vo.getAction())) {
				String tokenNo = (String) map.get("tokenNo");
				unassignToken(tokenNo, customerId);
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

	@Override
	public CustomerTokenUserModel saveCustomerTokenUser(String tokenNo, String registeredBy, String customerId, String tokenTypeCode)
			throws ApplicationException, BusinessException {
		try{
			checkUniqueRecord(tokenNo);
			
			CustomerTokenUserModel tokenUser = new CustomerTokenUserModel();
			tokenUser.setTokenNo(tokenNo);
			tokenUser.setRegisteredDate(DateUtils.getCurrentTimestamp());

			IDMUserModel idmUser = new IDMUserModel();
			idmUser.setCode(registeredBy);
			tokenUser.setRegisteredBy(idmUser);

			CustomerTokenTypeModel tokenType = new CustomerTokenTypeModel();
			tokenType.setCode(tokenTypeCode);
			tokenUser.setTokenType(tokenType);

			// set default value
			tokenUser.setRetry(0);
			tokenUser.setResetFlag(ApplicationConstants.NO);
			tokenUser.setStatus(ApplicationConstants.TOKEN_STATUS_ACTIVE);
			tokenUser.setUpdatedDate(null);
			tokenUser.setUpdatedBy(null);

			tokenUserRepo.persist(tokenUser);
			
			return tokenUser;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void saveCustomerTokenUserAndAssign(String tokenNo, String registeredBy, String customerId, String tokenTypeCode)
			throws ApplicationException, BusinessException {
		try{
			checkUniqueRecord(tokenNo);
			
			CustomerTokenUserModel tokenUser = tokenUserRepo.findByAssignedUserCode(customerId);
			
			if(tokenUser == null)
				tokenUser = new CustomerTokenUserModel();
			
			tokenUser.setTokenNo(tokenNo);
			tokenUser.setRegisteredDate(DateUtils.getCurrentTimestamp());

			IDMUserModel idmUser = new IDMUserModel();
			idmUser.setCode(registeredBy);
			tokenUser.setRegisteredBy(idmUser);

			CustomerTokenTypeModel tokenType = new CustomerTokenTypeModel();
			tokenType.setCode(tokenTypeCode);
			tokenUser.setTokenType(tokenType);

			IDMUserModel assignedUser = new IDMUserModel();
			assignedUser.setCode(customerId);
			tokenUser.setAssignedUser(assignedUser);
			tokenUser.setAssignedDate(DateUtils.getCurrentTimestamp());
			
			IDMUserModel assignedByIDMUser = idmUserRepo.findOne(registeredBy);
			tokenUser.setAssignedBy(assignedByIDMUser);
			
			// set default value
			tokenUser.setRetry(0);
			tokenUser.setResetFlag(ApplicationConstants.NO);
			tokenUser.setStatus(ApplicationConstants.TOKEN_STATUS_ACTIVE);
			tokenUser.setUpdatedDate(null);
			tokenUser.setUpdatedBy(null);

			tokenUserRepo.saveAndFlush(tokenUser);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void deleteCustomerTokenUser(String tokenNo) throws ApplicationException, BusinessException {
		try {
			CustomerTokenUserModel model = tokenUserRepo.findByTokenNo(tokenNo);

			if (model == null)
				throw new BusinessException("GPT-0100061");

			if(model.getAssignedUser() != null)
				throw new BusinessException("GPT-0100062");
			
			tokenUserRepo.delete(model.getId());
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void assignToken(String tokenNo, String customerId, String createdBy) throws ApplicationException, BusinessException {
		try{
			
			CustomerTokenUserModel tokenUserModel = tokenUserRepo.findByTokenNoAndAssignedUser(tokenNo, customerId);
			
			if(tokenUserModel == null)
				throw new BusinessException("GPT-0100061");
			
			IDMUserModel idmUser = idmUserRepo.findOne(customerId);
			tokenUserModel.setAssignedUser(idmUser);
			tokenUserModel.setAssignedDate(DateUtils.getCurrentTimestamp());
			
			IDMUserModel assignedByIDMUser = idmUserRepo.findOne(createdBy);
			tokenUserModel.setAssignedBy(assignedByIDMUser);
			
			tokenUserModel.setTokenNo(tokenNo);

			tokenUserRepo.save(tokenUserModel);
			
			//assign to token server
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("custId", customerId);
			inputs.put("userName", idmUser.getName()); 
			inputs.put("tokenNo", tokenNo);

			eaiAdapter.invokeService(EAIConstants.ASSIGN_TOKEN, inputs);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void unassignToken(String tokenNo, String customerId) throws ApplicationException, BusinessException {
		try{
			
			CustomerTokenUserModel tokenUserModel = tokenUserRepo.findByTokenNoAndAssignedUser(tokenNo, customerId);
			
			if(tokenUserModel == null)
				throw new BusinessException("GPT-0100061");
			
			//reset token status tu active
			tokenUserModel.setStatus(ApplicationConstants.TOKEN_STATUS_ACTIVE);
			tokenUserModel.setAssignedDate(null);
			tokenUserModel.setAssignedBy(null);
			tokenUserModel.setTokenNo(null);

			tokenUserRepo.save(tokenUserModel);
			
			//unassign to token server
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("custId", customerId);

//			eaiAdapter.invokeService(EAIConstants.UNASSIGN_TOKEN, inputs);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> unblockToken(String tokenNo, String customerId) throws ApplicationException, BusinessException {
		try{
			
			CustomerTokenUserModel tokenUserModel = tokenUserRepo.findByTokenNoAndAssignedUser(tokenNo, customerId);
			
			if(tokenUserModel == null)
				throw new BusinessException("GPT-0100061");
			
			//reset token status tu active
			tokenUserModel.setStatus(ApplicationConstants.TOKEN_STATUS_ACTIVE);
			
			tokenUserRepo.save(tokenUserModel);
			
			//call unblock to token server
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("custId", customerId);
//			eaiAdapter.invokeService(EAIConstants.UNBLOCK_TOKEN, inputs);
			
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100146");
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> unlockToken(String tokenNo, String customerId, String randomLockedCode) throws ApplicationException, BusinessException {
		try{
			
			CustomerTokenUserModel tokenUserModel = tokenUserRepo.findByTokenNoAndAssignedUser(tokenNo, customerId);
			
			if(tokenUserModel == null)
				throw new BusinessException("GPT-0100061");
			
			tokenUserRepo.save(tokenUserModel);
			
			//call unblock to token server
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("custId", customerId);
			inputs.put("randomLockedCode", randomLockedCode);
			
//			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.UNLOCK_TOKEN_RETAIL, inputs);
			
			//TODO need to replace with eai
			Map<String, Object> outputs = new HashMap<>();
			outputs.put("unlockCode", "123345");

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "{GPT-0100147|"+ (String) outputs.get("unlockCode") +"}");
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchToken(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {

			Map<String, Object> inputs = new HashMap<>();
			inputs.put("tokenType", map.get("tokenType"));
			inputs.put("tokenNo", map.get("tokenNo"));
			
			Map<String,Object> outputs = eaiAdapter.invokeService(EAIConstants.CHECK_TOKEN, inputs);
			
			Map<String,Object> result = new HashMap<>();
			result.put("tokenType", map.get("tokenType"));	
			result.put("tokenNo", outputs.get("tokenNo"));			
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	@Override
	public CustomerTokenUserModel findByAssignedUserCode(String customerId, boolean isThrowError) throws ApplicationException, BusinessException {
		try {
			CustomerTokenUserModel tokenUser = tokenUserRepo.findByAssignedUserCode(customerId);
			if(tokenUser == null && isThrowError) {
				throw new BusinessException("GPT-0100153");
			}
			
			return tokenUser;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	
}