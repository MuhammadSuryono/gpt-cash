package com.gpt.product.gpcash.corporate.token.tokenuser.services;

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
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;
import com.gpt.product.gpcash.corporate.token.tokentype.model.TokenTypeModel;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;
import com.gpt.product.gpcash.corporate.token.tokenuser.repository.TokenUserRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class TokenUserServiceImpl implements TokenUserService {
	
	@Autowired
	private TokenUserRepository tokenUserRepo;

	@Autowired
	private CorporateUserRepository corporateUserRepo;
	
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
			Page<TokenUserModel> result = tokenUserRepo.search(map, PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<TokenUserModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (TokenUserModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(TokenUserModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("tokenNo", model.getTokenNo());

		IDMUserModel assignedUser = model.getAssignedUser();

		if (assignedUser != null) {
			CorporateUserModel corporateUser = corporateUserRepo.findOne(assignedUser.getCode());
			map.put("userId", corporateUser.getUserId());
			map.put("userName", assignedUser.getName());
		}else {
			map.put("userId", ApplicationConstants.EMPTY_STRING);
			map.put("userName", ApplicationConstants.EMPTY_STRING);
		}

		
		IDMUserModel registeredBy = model.getRegisteredBy();
		if(registeredBy != null){
			map.put("registeredBy", registeredBy.getName());
			map.put("registeredDate", model.getRegisteredDate());
		}else{
			map.put("registeredBy", ApplicationConstants.EMPTY_STRING);
			map.put("registeredDate", ApplicationConstants.EMPTY_STRING);
		}
		
		IDMUserModel assignedBy = model.getAssignedBy();
		if(assignedBy != null){
			map.put("assignedBy", assignedBy.getName());
			map.put("assignedDate", ValueUtils.getValue(model.getAssignedDate()));
		}else{
			map.put("assignedBy", ApplicationConstants.EMPTY_STRING);
			map.put("assignedDate", ApplicationConstants.EMPTY_STRING);
		}
		map.put("status", ValueUtils.getValue(model.getStatus()));
		map.put("retry", ValueUtils.getValue(model.getRetry()));

		map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
		map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));

		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);

				// check unique tokenNo
				List<String> tokenList = (List<String>) map.get("tokenList");

				for (String tokenNo : tokenList) {
					checkUniqueRecord(tokenNo);
				}

			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				List<Map<String, Object>> tokenList = (ArrayList<Map<String,Object>>) map.get("tokenList");

				for (Map<String, Object> tokenMap : tokenList) {
					getExistingRecord((String) tokenMap.get("tokenNo"));
				}
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("UNASSIGN")) {
				vo.setAction((String) map.get(ApplicationConstants.WF_ACTION));
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

	private TokenUserModel getExistingRecord(String tokenNo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("tokenNo", tokenNo);
		Page<TokenUserModel> result = tokenUserRepo.search(map, null);

		List<TokenUserModel> modelList = result.getContent();

		if (modelList.size() == 0) {
			throw new BusinessException("GPT-0100001");
		}

		return modelList.get(0);
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.CORP_ID));
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.CORP_ID)).concat(ApplicationConstants.DELIMITER_DASH)
				.concat((String) map.get(ApplicationConstants.STR_NAME)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("TokenUserSC");

		return vo;
	}

	private void checkUniqueRecord(String tokenNo) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("tokenNo", tokenNo);
		Page<TokenUserModel> result = (Page<TokenUserModel>) tokenUserRepo.search(map, null);

		if (result.getContent().size() > 0) {
			throw new BusinessException("GPT-0100088");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				List<String> tokenList = (List<String>) map.get("tokenList");

				for (String tokenNo : tokenList) {
					saveTokenUser(tokenNo, vo.getCreatedBy(), corporateId, ApplicationConstants.TOKEN_TYPE_HARD_TOKEN);
				}
				
				Map<String, Object> inputs = new HashMap<>();
				inputs.put("corpId", corporateId);
				inputs.put("listOfTokenNo", tokenList);
				
				eaiAdapter.invokeService(EAIConstants.RESERVE_TOKEN, inputs);

			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				List<Map<String, Object>> tokenList = (ArrayList<Map<String,Object>>) map.get("tokenList");

				for (Map<String, Object> tokenMap : tokenList) {
					deleteTokenUser((String) tokenMap.get("tokenNo"));
				}
			} else if ("UNASSIGN".equals(vo.getAction())) {
				String userId = (String) map.get("userId");
				String userCode = Helper.getCorporateUserCode(corporateId, userId);
				String tokenNo = (String) map.get("tokenNo");
				unassignToken(userCode, tokenNo, corporateId);
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
	public TokenUserModel saveTokenUser(String tokenNo, String registeredBy, String corporateId, String tokenTypeCode)
			throws ApplicationException, BusinessException {
		try{
			checkUniqueRecord(tokenNo);
			
			TokenUserModel tokenUser = new TokenUserModel();
			tokenUser.setTokenNo(tokenNo);
			tokenUser.setRegisteredDate(DateUtils.getCurrentTimestamp());

			IDMUserModel idmUser = new IDMUserModel();
			idmUser.setCode(registeredBy);
			tokenUser.setRegisteredBy(idmUser);

			TokenTypeModel tokenType = new TokenTypeModel();
			tokenType.setCode(tokenTypeCode);
			tokenUser.setTokenType(tokenType);

			CorporateModel corporate = new CorporateModel();
			corporate.setId(corporateId);
			tokenUser.setCorporate(corporate);

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
	public void saveTokenUserAndAssign(String tokenNo, String registeredBy, String assignedUserStr, String corporateId, String tokenTypeCode)
			throws ApplicationException, BusinessException {
		try{
			checkUniqueRecord(tokenNo);
			
			TokenUserModel tokenUser = new TokenUserModel();
			tokenUser.setTokenNo(tokenNo);
			tokenUser.setRegisteredDate(DateUtils.getCurrentTimestamp());

			IDMUserModel idmUser = new IDMUserModel();
			idmUser.setCode(registeredBy);
			tokenUser.setRegisteredBy(idmUser);

			TokenTypeModel tokenType = new TokenTypeModel();
			tokenType.setCode(tokenTypeCode);
			tokenUser.setTokenType(tokenType);

			CorporateModel corporate = new CorporateModel();
			corporate.setId(corporateId);
			tokenUser.setCorporate(corporate);

			IDMUserModel assignedUser = new IDMUserModel();
			assignedUser.setCode(assignedUserStr);
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
	public void deleteTokenUser(String tokenNo) throws ApplicationException, BusinessException {
		try {
			TokenUserModel model = tokenUserRepo.findByTokenNo(tokenNo);

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
	public void assignToken(String userCode, String tokenNo, String corporateId, String createdBy) throws ApplicationException, BusinessException {
		try{
			
			TokenUserModel tokenUserModel = tokenUserRepo.findByCorporateIdAndTokenNo(corporateId, tokenNo);
			
			if(tokenUserModel == null)
				throw new BusinessException("GPT-0100061");
			
			IDMUserModel idmUser = idmUserRepo.findOne(userCode);
			tokenUserModel.setAssignedUser(idmUser);
			tokenUserModel.setAssignedDate(DateUtils.getCurrentTimestamp());
			
			IDMUserModel assignedByIDMUser = idmUserRepo.findOne(createdBy);
			tokenUserModel.setAssignedBy(assignedByIDMUser);

			tokenUserRepo.save(tokenUserModel);
			
			//assign to token server
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("corpId", corporateId);
			inputs.put("corpName", tokenUserModel.getCorporate().getName()); 
			inputs.put("userId", userCode);
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
	public void unassignToken(String userCode, String tokenNo, String corporateId) throws ApplicationException, BusinessException {
		try{
			
			TokenUserModel tokenUserModel = tokenUserRepo.findByCorporateIdAndTokenNoAndAssignedUser(corporateId, tokenNo, userCode);
			
			if(tokenUserModel == null)
				throw new BusinessException("GPT-0100061");
			
			//reset token status tu active
			tokenUserModel.setStatus(ApplicationConstants.TOKEN_STATUS_ACTIVE);
			tokenUserModel.setAssignedUser(null);
			tokenUserModel.setAssignedDate(null);
			tokenUserModel.setAssignedBy(null);

			tokenUserRepo.save(tokenUserModel);
			
			//unassign to token server
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("corpId", corporateId);
			inputs.put("userId", userCode);

			eaiAdapter.invokeService(EAIConstants.UNASSIGN_TOKEN, inputs);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> unblockToken(String userCode, String tokenNo, String corporateId) throws ApplicationException, BusinessException {
		try{
			
			TokenUserModel tokenUserModel = tokenUserRepo.findByCorporateIdAndTokenNoAndAssignedUser(corporateId, tokenNo, userCode);
			
			if(tokenUserModel == null)
				throw new BusinessException("GPT-0100061");
			
			//reset token status tu active
			tokenUserModel.setStatus(ApplicationConstants.TOKEN_STATUS_ACTIVE);
			
			tokenUserRepo.save(tokenUserModel);
			
			//call unblock to token server
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("corpId", corporateId);
			inputs.put("userId", userCode);			
			eaiAdapter.invokeService(EAIConstants.UNBLOCK_TOKEN, inputs);
			
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
	public Map<String, Object> unlockToken(String userCode, String tokenNo, String corporateId, String randomLockedCode) throws ApplicationException, BusinessException {
		try{
			
			TokenUserModel tokenUserModel = tokenUserRepo.findByCorporateIdAndTokenNoAndAssignedUser(corporateId, tokenNo, userCode);
			
			if(tokenUserModel == null)
				throw new BusinessException("GPT-0100061");
			
			tokenUserRepo.save(tokenUserModel);
			
			//call unblock to token server
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("corpId", corporateId);
			inputs.put("userId", userCode);
			inputs.put("randomLockedCode", randomLockedCode);
			
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.UNLOCK_TOKEN, inputs);

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
//			result.put("tokenNo", map.get("tokenNo"));	
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	@Override
	public TokenUserModel findByAssignedUserCode(String userCode, boolean isThrowError) throws ApplicationException, BusinessException {
		try {
			TokenUserModel tokenUser = tokenUserRepo.findByAssignedUserCode(userCode);
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