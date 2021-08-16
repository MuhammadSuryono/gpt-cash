package com.gpt.product.gpcash.corporate.pendingtaskadmin.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.repository.CorporateAdminPendingTaskRepository;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminWFContext;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateAdminPendingTaskServiceImpl implements CorporateAdminPendingTaskService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private CorporateWFEngine wfEngine;

	@Autowired
	private CorporateAdminPendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private CorporateUserRepository corporateUserRepo;

	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private ApplicationContext appCtx;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			CorporateAdminPendingTaskVO pendingTaskVO = new CorporateAdminPendingTaskVO();
			pendingTaskVO.setReferenceNo((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
			pendingTaskVO.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
			pendingTaskVO.setMenuCode((String) map.get("pendingTaskMenuCode"));
			pendingTaskVO.setStatus((String) map.get("status"));

			Page<CorporateAdminPendingTaskModel> result = pendingTaskRepo.searchPendingTask(pendingTaskVO,
					PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchPendingTaskHistoryByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			CorporateAdminPendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNo((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
			if (pendingTask != null) {
				Map<String, Object> wfResultMap = wfEngine.findTasksHistory(pendingTask.getId());
				return wfResultMap;
			} else {
				throw new BusinessException("GPT-0100001");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchPendingTaskByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			CorporateAdminPendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNoAndStatusIsPending((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
			if (pendingTask != null) {
				Map<String, Object> resultMap = wfEngine.findTasksByProcessInstanceId(pendingTask.getId());
				return resultMap;
			} else {
				throw new BusinessException("GPT-0100001");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> searchPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = wfEngine.findTasksByUser(map, CorporateWFEngine.Type.CorporateAdmin);		
		
		return resultMap;
	}
	
	@Override
	public Map<String, Object> countPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		int count = wfEngine.countActiveTasksByUser((String)map.get(ApplicationConstants.LOGIN_USERCODE));
		Map<String, Object> result = new HashMap<>();
		result.put("total", count);
		return result;
	}

	private Map<String, Object> prepareVarsForWorkflow(CorporateAdminPendingTaskVO vo) throws Exception {
		Map<String, Object> appVars = new HashMap<>(3, 1);
		appVars.put(ApplicationConstants.KEY_PT_ID, vo.getId());
		appVars.put(ApplicationConstants.KEY_MENU_CODE, vo.getMenuCode());
		return appVars;
	}
	
	private List<Map<String, Object>> setModelToMap(List<CorporateAdminPendingTaskModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CorporateAdminPendingTaskModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, ValueUtils.getValue(model.getReferenceNo()));
			map.put("status", ValueUtils.getValue(model.getStatus()));
			map.put("pendingTaskMenuCode", ValueUtils.getValue(model.getMenu().getCode()));
			map.put("actionDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("actionBy", ValueUtils.getValue(model.getCreatedBy()));

			resultList.add(map);
		}

		return resultList;
	}

	@Override
	public Map<String, Object> detailPendingTask(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);
			
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, referenceNo);

			CorporateAdminPendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
			if (model != null) {
				result.put("service", model.getService()); // put service
				result.put("details", getPendingTaskValue(model));
			}

			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> detailPendingTask(String pendingTaskId)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> result = new HashMap<>();
			
			CorporateAdminPendingTaskModel model = pendingTaskRepo.findOne(pendingTaskId);
			if (model != null) {
				result.put("service", model.getService()); // put service
				result.put("details", getPendingTaskValue(model));
			}

			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> detailPendingTaskOld(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		try {
			String referenceNo = (String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO);

			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, referenceNo);

			CorporateAdminPendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
			if (model != null) {
				result.put("service", model.getService()); // put service
				result.put("details", getPendingTaskOldValue(model));
			}

			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> savePendingTask(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			vo.setReferenceNo(Helper.generateCorporateReferenceNo());

			// TODO validasi apakah user tersebut memiliki akses terhadap menu nya

			// TODO validasi corporate id nya benar atau tidak

			// validasi user
			idmRepo.isIDMUserValid(vo.getCreatedBy());

			CorporateAdminPendingTaskModel pendingTask = new CorporateAdminPendingTaskModel();
			pendingTask.setUniqueKey(vo.getUniqueKey());
			pendingTask.setUniqueKeyDisplay(vo.getUniqueKeyDisplay());

			CorporateModel corporate = new CorporateModel();
			corporate.setId(vo.getCorporateId());
			pendingTask.setCorporate(corporate);
			pendingTask.setReferenceNo(vo.getReferenceNo());
			pendingTask.setAction(vo.getAction());
			pendingTask.setCreatedBy(vo.getCreatedBy());
			pendingTask.setCreatedDate(DateUtils.getCurrentTimestamp());
			pendingTask.setStatus(ApplicationConstants.WF_STATUS_PENDING);
			
			IDMMenuModel menu = new IDMMenuModel();
			menu.setCode(vo.getMenuCode());
			pendingTask.setMenu(menu);
			pendingTask.setModel(vo.getJsonObject().getClass().getName());
			pendingTask.setService(vo.getService());

			if (vo.getJsonObject() != null) {
				String jsonObj = objectMapper.writeValueAsString(vo.getJsonObject());
				pendingTask.setValuesStr(jsonObj);
			}

			if (vo.getJsonObjectOld() != null) {
				String jsonObjOld = objectMapper.writeValueAsString(vo.getJsonObjectOld());
				pendingTask.setOldValues(jsonObjOld);
			}

			if (logger.isDebugEnabled())
				logger.debug("Admin PendingTask : " + pendingTask);

			pendingTaskRepo.save(pendingTask);

			vo.setId(pendingTask.getId());			
			
			// start workflow
			return wfEngine.createInstance(CorporateWFEngine.Type.CorporateAdmin, vo.getCreatedBy(), pendingTask.getCreatedDate(), prepareVarsForWorkflow(vo));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public Map<String, Object> approve(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			// get pending task by reference
			CorporateAdminPendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNoAndStatusIsPending((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));

			if (pendingTask != null) {
				return wfEngine.endUserTask((String)map.get("taskId"), (String)map.get(ApplicationConstants.LOGIN_USERCODE), pendingTask.getId(), Status.APPROVED, null);
			} else {
				throw new BusinessException("GPT-0100001");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void approve(String pendingTaskId, String userId) throws ApplicationException, BusinessException { 
		try {
			CorporateAdminPendingTaskModel pendingTask = pendingTaskRepo.findById(pendingTaskId);
			if(pendingTask == null) {
				throw new BusinessException("GPT-0100001");
			}
			
			CorporateAdminWorkflowService service = (CorporateAdminWorkflowService) appCtx.getBean(pendingTask.getService());
	
			CorporateAdminPendingTaskVO vo = getVOFromCorporateAdminPendingTaskModel(pendingTask); 
			vo.setActionBy(userId);
			service.approve(vo);
	
			pendingTaskRepo.updateStatusPendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_APPROVED);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public Map<String, Object> reject(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			// get pending task by reference
			CorporateAdminPendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNoAndStatusIsPending((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));

			if (pendingTask != null) {
				return wfEngine.endUserTask((String)map.get("taskId"), (String)map.get(ApplicationConstants.LOGIN_USERCODE), pendingTask.getId(), Status.REJECTED, null);
			} else {
				throw new BusinessException("GPT-0100001");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void reject(String pendingTaskId, String userId) throws ApplicationException, BusinessException {
		try {
			CorporateAdminPendingTaskModel pendingTask = pendingTaskRepo.findById(pendingTaskId);
			if(pendingTask == null) {
				throw new BusinessException("GPT-0100001");
			}
			
			CorporateAdminWorkflowService service = (CorporateAdminWorkflowService) appCtx.getBean(pendingTask.getService());
		
			CorporateAdminPendingTaskVO vo = getVOFromCorporateAdminPendingTaskModel(pendingTask); 
			vo.setActionBy(userId);
			service.reject(vo);

			pendingTaskRepo.updateStatusPendingTask(pendingTask.getId(), ApplicationConstants.WF_STATUS_REJECTED);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void checkUniquePendingTask(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ValueUtils.hasValue(pendingTaskRepo.findByUniqueKeyAndStatusAndMenuCodeAndCorporateId(vo.getUniqueKey(), ApplicationConstants.WF_STATUS_PENDING, vo.getMenuCode(), vo.getCorporateId()))) {
				throw new BusinessException("GPT-0100002");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void checkUniquePendingTaskLike(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ValueUtils.hasValue(pendingTaskRepo.findByUniqueKeyContainingAndStatusAndMenuCodeAndCorporateId(vo.getUniqueKey(), ApplicationConstants.WF_STATUS_PENDING, vo.getMenuCode(), vo.getCorporateId()))) {
				throw new BusinessException("GPT-0100002");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	private CorporateAdminPendingTaskVO getVOFromCorporateAdminPendingTaskModel(CorporateAdminPendingTaskModel pendingTask) throws Exception {
		CorporateAdminPendingTaskVO vo = new CorporateAdminPendingTaskVO();
		vo.setId(pendingTask.getId());
		vo.setUniqueKey(pendingTask.getUniqueKey());
		vo.setUniqueKeyDisplay(pendingTask.getUniqueKeyDisplay());
		vo.setReferenceNo(pendingTask.getReferenceNo());
		vo.setAction(pendingTask.getAction());
		vo.setCreatedBy(pendingTask.getCreatedBy());
		vo.setCreatedDate(pendingTask.getCreatedDate());
		vo.setStatus(pendingTask.getStatus());
		vo.setMenuCode(pendingTask.getMenu().getCode());
		vo.setService(pendingTask.getService());
		vo.setModel(pendingTask.getModel());
		vo.setCorporateId(pendingTask.getCorporate().getId());

		Class<?> clazz = Class.forName(pendingTask.getModel());

		vo.setJsonObject(objectMapper.readValue(pendingTask.getValuesStr(), clazz));

		String oldValues = pendingTask.getOldValuesStr();
		if (oldValues != null)
			vo.setJsonObjectOld(objectMapper.readValue(oldValues, clazz));

		return vo;
	}

	private Object getPendingTaskValue(CorporateAdminPendingTaskModel pendingTask) throws Exception {
		String strValues = pendingTask.getValuesStr();
		if (strValues != null) {
			return objectMapper.readValue(strValues, Class.forName(pendingTask.getModel()));
		}
		return null;
	}

	private Object getPendingTaskOldValue(CorporateAdminPendingTaskModel pendingTask) throws Exception {
		String strValues = pendingTask.getOldValuesStr();
		if (strValues != null) { 
			return objectMapper.readValue(strValues, Class.forName(pendingTask.getModel()));
		}
		return null;
	}

	@Override
	public List<String> getUserForWorkflow(CorporateAdminWFContext profileContext) throws ApplicationException, BusinessException {
		if (logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n\nprofileContext.getPendingTaskId = " + profileContext.getPendingTaskId());
			logger.debug("approval level = " + profileContext.getApprovalLevel());
		}

		List<String> wfUsers = new ArrayList<>();

		try {

			CorporateAdminPendingTaskModel pendingTask = pendingTaskRepo.findOne(profileContext.getPendingTaskId());

			List<CorporateUserModel> userList = corporateUserRepo.findAdminUserChecker(pendingTask.getCorporate().getId());
			
			if(userList.isEmpty())
				throw new BusinessException("GPT-0100031");
			
			String userMaker = pendingTask.getCreatedBy();
			
			for (CorporateUserModel userCorporate : userList) {
				// validasi user yg di routing tidak boleh sama dengan maker dan
				// user tidak double
				
				IDMUserModel user = userCorporate.getUser();
				
				if (!userMaker.equals(user.getCode()) && ValueUtils.hasValue(user.getCode())
						&& !wfUsers.contains(user.getCode())) {
					
					wfUsers.add(user.getCode());
				}
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Assign tasks to = {}", wfUsers);
		}
		
		return wfUsers;
	}
	
	@Override
	public Map<String, Object> searchPendingTaskHistoryByPendingTaskId(String pendingTaskId)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> wfResultMap = wfEngine.findTasksHistory(pendingTaskId);
			return wfResultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
}
