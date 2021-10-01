package com.gpt.product.gpcash.corporate.workflow;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.LockModeType;
import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.platform.cash.workflow.Status;
import com.gpt.product.gpcash.approvallevel.repository.ApprovalLevelRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.model.CorporateAdminPendingTaskModel;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateProcessInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateVariable;
import com.gpt.product.gpcash.corporate.workflow.repository.CorporateProcessInstanceRepository;
import com.gpt.product.gpcash.corporate.workflow.repository.CorporateTaskInstanceRepository;

@Component
public class CorporateSimpleWFEngine implements CorporateWFEngine {
	
	/**
	 * Hold all available definitions
	 */
	private Map<String, CorporateProcessDefinition> definitions;

	/**
	 * Hold only latest version of each type of definitions
	 * The name is ignored here
	 */
	private Map<Type, CorporateProcessDefinition> latestDefinitions;
	
	@Autowired
	private CorporateVariableFactory variableFactory;
	
	@Autowired
	private CorporateProcessInstanceRepository piRepo;
	
	@Autowired
	private CorporateTaskInstanceRepository taskRepo;
	
	//perlu di review apakah boleh inject idm di workflow engine
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private ApprovalLevelRepository apprLevelRepo;

	@Autowired
	public void setDefinitions(List<CorporateProcessDefinition> rawDefinitions) {
		latestDefinitions = new HashMap<>(Type.values().length, 1);
		definitions = new HashMap<>(rawDefinitions.size(), 1);
		rawDefinitions.forEach( p -> {
			definitions.put(p.getName(), p);
			
			CorporateProcessDefinition p2 = latestDefinitions.get(p.getType());
			if(p2 == null || p.getVersion() > p2.getVersion()) {
				latestDefinitions.put(p.getType(), p);
			}
		});
	}
	
	@Override
	public void expireInstance(String processInstanceId) throws BusinessException, ApplicationException {
		CorporateProcessInstance pi = piRepo.findOne(processInstanceId);
		if(pi.getEndDate()!=null) {
			//TODO: use error business exception error code ???
			throw new ApplicationException("Workflow has already ended", null);
		}
		taskRepo.cancelUnfinishedTasks(processInstanceId);
		pi.setStatus(Status.EXPIRED);
		pi.setEndDate(new Timestamp(System.currentTimeMillis()));
		piRepo.save(pi);
	}
	
	@Override
	public void cancelInstance(String processInstanceId) throws BusinessException, ApplicationException {
		CorporateProcessInstance pi = piRepo.findOne(processInstanceId);
		if(pi.getEndDate()!=null) {
			//TODO: use error business exception error code ???
			throw new ApplicationException("Workflow has already ended", null);
		}
		taskRepo.cancelUnfinishedTasks(processInstanceId);
		pi.setStatus(Status.CANCELED);
		pi.setEndDate(new Timestamp(System.currentTimeMillis()));
		piRepo.save(pi);
	}
	
	@Override
	public Map<String, Object> createInstance(Type type, String createdBy, Timestamp createdDate,
			Map<String, Object> processVars) throws BusinessException, ApplicationException {
		try {
			CorporateProcessDefinition pd = latestDefinitions.get(type);
			
			assert(pd != null) : "Configuration exception, can not find process definition of type: " + type;
			
			CorporateProcessInstance pi = new CorporateProcessInstance();
			pd.setSpecifiParams(pi, processVars);
			pi.setCreatedBy(createdBy);
			pi.setCreatedDate(createdDate);
			pi.setProcessDefinition(pd.getName());
			if(!processVars.isEmpty()) {
				Set<CorporateVariable> vars = new HashSet<>();
				processVars.forEach( (key, value) -> {
					CorporateVariable var = variableFactory.createVariable(key, value);
					var.setProcessInstance(pi);
					vars.add(var);
				});
				pi.setVariables(vars);
			}
	
			piRepo.persist(pi);
			
			CorporateStage s = pd.start(pi);
			while(s != null) {
				pi.setCurrentStageId(s.getId());
				s = s.execute(pi);
			}
	
			piRepo.save(pi);
			
			pd.updatePendingTaskActivity(pi);
			
			String strDateTime = Helper.DATE_TIME_FORMATTER.format(pi.getCreatedDate());
			Map<String, Object> result = new HashMap<>();
			result.put(ApplicationConstants.WF_FIELD_MESSAGE, getWFStatus(pi));
			result.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
			result.put("dateTime", strDateTime);
			return result;
		} catch (ApplicationException e) {
			throw e;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	protected String getWFStatus(CorporateProcessInstance pi) {
		if(pi.getEndDate() != null) {
			// wf has ended
			switch(pi.getStatus()) {
				case APPROVED:
					return "GPT-0200003";
				case REJECTED:
					return "GPT-0200004";
				case RELEASED:
					return "GPT-0200005";
				case CANCELED:
					// canceled by maker
					return "GPT-0200007";
				default:
			}
		} else {
			// wf has pending task
			if(pi.getStatus() == null) {
				// submitted by maker waiting for approval
				return "GPT-0200001";
			}
			switch(pi.getStatus()) {
				case APPROVED:
					return "GPT-0200002";
				case RESUBMITTED:
					return "GPT-0200001";
				case DECLINED:
					return "GPT-0200006";
				default:
			}
		}
		
		return null;
	}
	
	@Override
	public int countActiveTasksByUser(String user) throws BusinessException, ApplicationException {
		return taskRepo.countActiveTasksByUser(user);
	}
	
	@Override
	public Map<String, Object> findTasksByUser(Map<String, Object> map, Type type) throws BusinessException, ApplicationException {
		try {
			Page<Tuple> page = taskRepo.findActiveTasksByUser(map, type, PagingUtils.createPageRequest(map));
			List<Map<String, Object>> tasks = new ArrayList<>();
			
			List<Tuple> ts = page.getContent();
			for(Tuple t : ts) {
				Map<String, Object> task = new LinkedHashMap<>(12, 1);
				task.put("pendingTaskId", t.get("pendingTaskId"));
				task.put("taskId", t.get("taskId"));
				
				String stageId = (String)t.get("stageId");
				
				task.put("stageId", stageId);
				task.put("startDate", t.get("startDate"));
				task.put("approvalLvCount", t.get("currApprLvCount"));
				
				if(stageId.equals(ApplicationConstants.WF_STAGE_APPROVE)) {
			        List<List<Object>> approvalMatrix = CorporateProcessInstance.getListOfApprovalMatrix((String)t.get("approvalMatrix"));
			        int currApprLv = (Integer)t.get("currApprLv");
			        List<Object> approvalData = approvalMatrix.get(currApprLv);
	 		        
					String approvalLvCode = (String)approvalData.get(0);
					
					if(approvalLvCode == null) {
						task.put("approvalLvCode", 0);
						task.put("approvalLvName", ApplicationConstants.EMPTY_STRING);
					} else {
						task.put("approvalLvCode", approvalLvCode);
						task.put("approvalLvName", apprLevelRepo.findOne(approvalLvCode).getName());
					}
					
					task.put("approvalLvRequired", approvalData.get(1));
				} else if(stageId.equals(ApplicationConstants.WF_STAGE_RELEASE)) {
					task.put("approvalLvCode", 0);
					task.put("approvalLvName", ApplicationConstants.EMPTY_STRING);
					task.put("approvalLvRequired", 1);
				}
				
				task.put("referenceNo", t.get("referenceNo"));
				task.put("actionBy", t.get("actionBy"));
				task.put("actionByName", t.get("actionByName"));
				task.put("actionDate", t.get("actionDate"));

				if(type == Type.CorporateUser) {
					task.put("debitAccount", t.get("debitAccount"));
					task.put("debitAccountName", t.get("debitAccountName"));
					task.put("debitAccountCurrency", t.get("debitAccountCurrency"));
					task.put("transactionAmount", t.get("transactionAmount"));
					task.put("transactionCurrency", t.get("transactionCurrency"));
					task.put("creditAccount", t.get("creditAccount"));
					task.put("creditAccountName", t.get("creditAccountName"));
					task.put("creditAccountCurrency", t.get("creditAccountCurrency"));
					
					// request add by BJB
					task.put("instructionMode", t.get("instructionMode"));
					task.put("billingId", t.get("billingId"));
				}
				
				task.put("pendingTaskMenuCode", t.get("pendingTaskMenuCode"));
				task.put("pendingTaskMenuName", t.get("pendingTaskMenuName"));
				task.put("uniqueKeyDisplay", ValueUtils.getValue(t.get("uniqueKeyDisplay")));
				task.put("action", t.get("action"));
				
				tasks.add(task);
			}
			
			Map<String, Object> result = new HashMap<>();
			result.put("tasks", tasks);
			PagingUtils.setPagingInfo(result, page);
			return result;
			
		} catch(Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> endUserTask(String taskInstanceId, String userId, String processInstanceId, Status status, Map<String, Object> taskVars) throws BusinessException, ApplicationException {
		CorporateProcessInstance pi = piRepo.getOne(processInstanceId, LockModeType.PESSIMISTIC_WRITE);
		pi.setTrxStatus(null); // just in case
		 
		CorporateTaskInstance ti = taskRepo.findByIdAndUserAndProcessInstanceId(taskInstanceId, userId, processInstanceId);
		
		if(ti == null) {
			// there is no task with the specified userId and taskInstanceId in the process instance
			// somebody is trying to approve task that is not belonged to him/her
			throw new BusinessException("GPT-0100038");
		}
		
		if(ti.getEndDate()!=null) // task has ended
			throw new BusinessException("GPT-0100039");
		
		if(taskVars!=null && !taskVars.isEmpty()) {
			Set<CorporateVariable> vars = new HashSet<>();
			taskVars.forEach( (key, value) -> {
				CorporateVariable var = variableFactory.createVariable(key, value);
				var.setProcessInstance(pi);
				var.setTaskInstance(ti);
				vars.add(var);
			});
			ti.setVariables(vars);
		}
		
		CorporateProcessDefinition pd = definitions.get(pi.getProcessDefinition());
		
		ti.setStatus(status);
		
		// TODO: calculcate cross currency if needed here
		ti.setAmount(pi.getApprovalLimit());
		ti.setAmountCcyCd(pi.getApprovalLimitCcyCd());
		ti.setEndDate(DateUtils.getCurrentTimestamp());

		pi.setUpdatedBy(ti.getUser());
		pi.setUpdatedDate(ti.getEndDate());
		pi.setStatus(ti.getStatus());
		
		CorporateStage s = pd.executeTask(ti);
		while(s != null) {
			pi.setCurrentStageId(s.getId());
			s = s.execute(pi);
		}

		piRepo.save(pi);

		pd.updatePendingTaskActivity(pi);
		
		String strDateTime = Helper.DATE_TIME_FORMATTER.format(ti.getEndDate());
		Map<String, Object> result = new HashMap<>();
		result.put(ApplicationConstants.WF_FIELD_MESSAGE, getWFStatus(pi));
		result.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
		result.put("dateTime", strDateTime);
		result.put("trxStatus", pi.getTrxStatus());
		return result;
	}
			
	 @Override
	public Map<String, Object> findTasksByProcessInstanceId(String processInstanceId)
			throws BusinessException, ApplicationException {
		try {
			CorporateProcessInstance pi = piRepo.findOne(processInstanceId);
			return definitions.get(pi.getProcessDefinition()).findTasksByProcessInstanceId(pi);
		}catch(Exception e) {
			throw new ApplicationException(e.getMessage(), e);
		}
	}
	
	@Override
	public Map<String, Object> findTasksHistory(String processInstanceId) throws BusinessException, ApplicationException {
		try {
			CorporateProcessInstance pi = piRepo.findOne(processInstanceId);
			return definitions.get(pi.getProcessDefinition()).findTasksHistory(pi);
		}catch(Exception e) {
			throw new ApplicationException(e.getMessage(), e);
		}
	} 

	@Override
	public Map<String, Object> findPendingTasks(Map<String, Object> map)
			throws BusinessException, ApplicationException {
		
		Page<CorporateUserPendingTaskModel> pendingTasks = taskRepo.findPendingTasks(map, PagingUtils.createPageRequest(map));		
		
		List<CorporateUserPendingTaskModel> list = pendingTasks.getContent();
				
		Map<String, Object> resultMap = new HashMap<>();
		
		List<Map<String, Object>> resultList = new ArrayList<>(list.size());

		for (CorporateUserPendingTaskModel model : list) {
			resultList.add(setModelToMapPendingTask(model));
		}

		resultMap.put("result", resultList);

		PagingUtils.setPagingInfo(resultMap, pendingTasks);
		
		return resultMap;
	}

	private Map<String, Object> setModelToMapPendingTask(CorporateUserPendingTaskModel model) {
		Map<String, Object> map = new HashMap<>();
		
		CorporateModel corporate = model.getCorporate();
		map.put("corporateId", corporate.getId());
		map.put("corporateName", corporate.getName());
		map.put("latestActivityDate", model.getActivityDate());
		map.put("referenceNo", model.getReferenceNo());
		
		IDMUserModel idmUser = idmUserRepo.findOne(model.getCreatedBy());
		map.put("createdByUserId", idmUser.getUserId());
		map.put("createdByUserName", idmUser.getName());
		map.put("pendingTaskId", model.getId());
		map.put("pendingTaskMenuCode", model.getMenu().getCode());
		map.put("pendingTaskMenuName", model.getMenu().getName());
		map.put("sourceAccount", ValueUtils.getValue(model.getSourceAccount()));
		map.put("sourceAccountName", ValueUtils.getValue(model.getSourceAccountName()));
		map.put("sourceAccountCurrencyCode", ValueUtils.getValue(model.getSourceAccountCurrencyCode()));
		map.put("sourceAccountCurrencyName", ValueUtils.getValue(model.getSourceAccountCurrencyName()));
		map.put("transactionAmount", ValueUtils.getValue(model.getTransactionAmount()));
		map.put("transactionCurrency", ValueUtils.getValue(model.getTransactionCurrency()));
		map.put("status", model.getTrxStatus());

		return map;
	}	
	
	@Override
	public Map<String, Object> findNonFinancialPendingTasks(Map<String, Object> map)
			throws BusinessException, ApplicationException {
		
		Page<CorporateAdminPendingTaskModel> pendingTasks = taskRepo.findNonFinancialPendingTasks(map, PagingUtils.createPageRequest(map));		
		
		List<CorporateAdminPendingTaskModel> list = pendingTasks.getContent();
				
		Map<String, Object> resultMap = new HashMap<>();
		
		List<Map<String, Object>> resultList = new ArrayList<>(list.size());

		for (CorporateAdminPendingTaskModel model : list) {
			resultList.add(setModelToMapPendingTask(model));
		}

		resultMap.put("result", resultList);

		PagingUtils.setPagingInfo(resultMap, pendingTasks);
		
		return resultMap;
	}
	
	private Map<String, Object> setModelToMapPendingTask(CorporateAdminPendingTaskModel model) {
		Map<String, Object> map = new HashMap<>();
		
		CorporateModel corporate = model.getCorporate();
		map.put("corporateCode", corporate.getId());
		map.put("corporateName", corporate.getName());
		map.put("latestActivityDate", model.getCreatedDate());
		map.put("referenceNo", model.getReferenceNo());
		map.put("pendingTaskId", model.getId());
		map.put("pendingTaskMenuCode", model.getMenu().getCode());
		map.put("pendingTaskMenuName", model.getMenu().getName());
		map.put("activityType", model.getAction());
		map.put("uniqueKeyDisplay", model.getUniqueKeyDisplay());
		map.put("status", model.getStatus());

		return map;
	}

	@Override
	public Map<String, Object> findPendingTasksForDownload(Map<String, Object> map) throws BusinessException, ApplicationException {
		
		Pageable pageInfo = new PageRequest(0,Integer.MAX_VALUE, new Sort("activityDate"));
		Page<CorporateUserPendingTaskModel> pendingTasks = taskRepo.findPendingTasks(map, pageInfo);		
		
		List<CorporateUserPendingTaskModel> list = pendingTasks.getContent();
				
		Map<String, Object> resultMap = new HashMap<>();
		
		List<Map<String, Object>> resultList = new ArrayList<>(list.size());

		for (CorporateUserPendingTaskModel model : list) {
			resultList.add(setModelToMapPendingTask(model));
		}

		resultMap.put("result", resultList);

//		PagingUtils.setPagingInfo(resultMap, pendingTasks);
		
		return resultMap;
	}
	
	

	@Override
	public void endInstance(String processInstanceId) throws BusinessException, ApplicationException {
		CorporateProcessInstance pi = piRepo.findOne(processInstanceId);

		taskRepo.endUnfinishedTasks(pi.getId(),pi.getApprovalLimit(),pi.getApprovalLimitCcyCd(),Status.APPROVED); //for One Signer
		pi.setStatus(Status.APPROVED);
		pi.setEndDate(new Timestamp(System.currentTimeMillis()));
		piRepo.save(pi);
	}
	
	
}
