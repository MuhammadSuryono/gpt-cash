package com.gpt.component.pendingtask.services;

import java.util.ArrayList;
import java.util.Collections;
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
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.idm.userrole.repository.IDMUserRoleRepository;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.pendingtask.model.PendingTaskModel;
import com.gpt.component.pendingtask.repository.PendingTaskRepository;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.component.pendingtask.valueobject.WFContext;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.platform.cash.workflow.WFEngine;

@Service
@Transactional(rollbackFor = Exception.class)
public class PendingTaskServiceImpl implements PendingTaskService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private WFEngine wfEngine;

	@Autowired
	private PendingTaskRepository pendingTaskRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private IDMUserRoleRepository idmUserRoleRepo;

	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private ApplicationContext appCtx;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			PendingTaskVO pendingTaskVO = new PendingTaskVO();
			pendingTaskVO.setReferenceNo((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
			pendingTaskVO.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
			pendingTaskVO.setMenuCode((String) map.get("pendingTaskMenuCode"));
			pendingTaskVO.setStatus((String) map.get("status"));

			Page<PendingTaskModel> result = pendingTaskRepo.searchPendingTask(pendingTaskVO,
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
			PendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNo((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
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
			PendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNoAndStatusIsPending((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));
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
		Map<String, Object> resultMap = wfEngine.findTasksByUser(map, WFEngine.Type.BackOfficeUser);

		return resultMap;
	}
	
	@Override
	public Map<String, Object> countPendingTaskByUser(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		int count = wfEngine.countActiveTasksByUser((String)map.get(ApplicationConstants.LOGIN_USERID));
		Map<String, Object> result = new HashMap<>();
		result.put("total", count);
		return result;
	}

	private Map<String, Object> prepareVarsForWorkflow(PendingTaskVO vo) throws Exception {
		Map<String, Object> appVars = new HashMap<>(3, 1);
		appVars.put(ApplicationConstants.KEY_PT_ID, vo.getId());
		appVars.put(ApplicationConstants.KEY_MENU_CODE, vo.getMenuCode());
		return appVars;
	}

	private List<Map<String, Object>> setModelToMap(List<PendingTaskModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PendingTaskModel model : list) {
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

			PendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
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
			
			PendingTaskModel model = pendingTaskRepo.findOne(pendingTaskId);
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

			PendingTaskModel model = pendingTaskRepo.findByReferenceNo(referenceNo);
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
	public Map<String, Object> savePendingTask(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			vo.setReferenceNo(Helper.generateBackOfficeReferenceNo());
			
			//TODO validasi apakah user tersebut memiliki akses terhadap menu nya

			//validasi user
			idmRepo.isIDMUserValid(vo.getCreatedBy());
			
			PendingTaskModel pendingTask = new PendingTaskModel();
			pendingTask.setUniqueKey(vo.getUniqueKey());
			pendingTask.setUniqueKeyDisplay(vo.getUniqueKeyDisplay());
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
				pendingTask.setOldValuesStr(jsonObjOld);
			}
			
			pendingTaskRepo.save(pendingTask);

			vo.setId(pendingTask.getId());			
			
			// start workflow
			return wfEngine.createInstance(WFEngine.Type.BackOfficeUser, vo.getCreatedBy(), pendingTask.getCreatedDate(), prepareVarsForWorkflow(vo));

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
			PendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNoAndStatusIsPending((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));

			if (pendingTask != null) {
				return wfEngine.endUserTask((String) map.get("taskId"), (String) map.get(ApplicationConstants.LOGIN_USERID), pendingTask.getId(),
						Status.APPROVED, null);
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
			PendingTaskModel pendingTask = pendingTaskRepo.findById(pendingTaskId);
			if (pendingTask == null) {
				throw new BusinessException("GPT-0100001");
			}

			WorkflowService service = (WorkflowService) appCtx.getBean(pendingTask.getService());
			
			PendingTaskVO vo = getVOFromPendingTaskModel(pendingTask);
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
			PendingTaskModel pendingTask = pendingTaskRepo.findByReferenceNoAndStatusIsPending((String) map.get(ApplicationConstants.WF_FIELD_REFERENCE_NO));

			if (pendingTask != null) {
				return wfEngine.endUserTask((String) map.get("taskId"), (String) map.get(ApplicationConstants.LOGIN_USERID), pendingTask.getId(), Status.REJECTED, null);
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
			PendingTaskModel pendingTask = pendingTaskRepo.findById(pendingTaskId);
			if (pendingTask == null) {
				throw new BusinessException("GPT-0100001");
			}

			WorkflowService service = (WorkflowService) appCtx.getBean(pendingTask.getService());

			PendingTaskVO vo = getVOFromPendingTaskModel(pendingTask);
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
	public void checkUniquePendingTask(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ValueUtils.hasValue(pendingTaskRepo.findByUniqueKeyAndStatusAndMenuCode(vo.getUniqueKey(), ApplicationConstants.WF_STATUS_PENDING, vo.getMenuCode()))) {
				throw new BusinessException("GPT-0100002");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void checkUniquePendingTaskLike(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ValueUtils.hasValue(pendingTaskRepo.findByUniqueKeyContainingAndStatusAndMenuCode(vo.getUniqueKey(), ApplicationConstants.WF_STATUS_PENDING, vo.getMenuCode()))) {
				throw new BusinessException("GPT-0100002");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	private PendingTaskVO getVOFromPendingTaskModel(PendingTaskModel pendingTask) throws Exception {
		PendingTaskVO vo = new PendingTaskVO();
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

		Class<?> clazz = Class.forName(pendingTask.getModel());

		vo.setJsonObject(objectMapper.readValue(pendingTask.getValuesStr(), clazz));

		String oldValues = pendingTask.getOldValuesStr();
		if (oldValues != null)
			vo.setJsonObjectOld(objectMapper.readValue(oldValues, clazz));

		return vo;
	}

	private Object getPendingTaskValue(PendingTaskModel pendingTask) throws Exception {
		String strValues = pendingTask.getValuesStr();
		if (strValues != null) {
			return objectMapper.readValue(strValues, Class.forName(pendingTask.getModel()));
		}
		return Collections.EMPTY_MAP;
	}

	private Object getPendingTaskOldValue(PendingTaskModel pendingTask) throws Exception {
		String strValues = pendingTask.getOldValuesStr();
		if (strValues != null) { 
			return objectMapper.readValue(strValues, Class.forName(pendingTask.getModel()));
		}
		return Collections.EMPTY_MAP;
	}

	@Override
	public List<String> getUserForWorkflow(WFContext profileContext) throws ApplicationException, BusinessException {
		if (logger.isDebugEnabled()) {
			logger.debug("profileContext.getPendingTaskId = {}", profileContext.getPendingTaskId());
			logger.debug("approval level = {}", profileContext.getApprovalLevel());
		}

		List<String> wfUsers = new ArrayList<>();

		try {

			PendingTaskModel pendingTask = pendingTaskRepo.findOne(profileContext.getPendingTaskId());

			//cari user yg WF berdasarkan approval level
			String bankWFRoleCode = ApplicationConstants.WF_ROLE_APPROVER + profileContext.getApprovalLevel();
			String BNK_USR = ApplicationConstants.WF_ROLE_BNK_USR_CATEGORY + ApplicationConstants.WILDCARD;
			List<String> userWFList = idmUserRoleRepo.findByRoleTypeCodeAndWFRoleCode(ApplicationConstants.ROLE_TYPE_WF, 
					bankWFRoleCode, BNK_USR);
			
			if(userWFList.isEmpty())
				throw new BusinessException("GPT-0100031");
			

		IDMUserModel maker = idmUserRepo.findOne(pendingTask.getCreatedBy());
			String branchCode = null;
			if (profileContext.getBranchOption().equals(ApplicationConstants.BRANCH_OPTION_SAME)) {
				branchCode = maker.getBranch().getCode();
			} else {
				branchCode = "0000"; //parent Branch (HO)
			}
			
			// get user berdasarkan branch option
			List<String> userByBranchOption = idmUserRepo.findUserByBranchForWorkflow(branchCode, userWFList);
			
			//get users yg dapat mengakses menuCode yg di kirim
			List<String> userList = idmUserRepo.findAuthorizedUserForWorkflow(pendingTask.getMenu().getCode(), userByBranchOption);

			String userMaker = maker.getCode();

			for (String user : userList) {
				// validasi user yg di routing tidak boleh sama dengan maker dan
				// user tidak double
				if (!userMaker.equals(user) && ValueUtils.hasValue(user)
						&& !wfUsers.contains(user)) {

					wfUsers.add(user);
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
	
}
