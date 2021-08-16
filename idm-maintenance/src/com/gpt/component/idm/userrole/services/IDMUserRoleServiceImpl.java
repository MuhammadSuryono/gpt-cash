package com.gpt.component.idm.userrole.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.userrole.model.IDMUserRoleModel;
import com.gpt.component.idm.userrole.repository.IDMUserRoleRepository;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMUserRoleServiceImpl implements IDMUserRoleService {

	@Autowired
	private IDMUserRoleRepository userRoleRepo;

	@Autowired
	private IDMRepository idmRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<IDMUserRoleModel> result = userRoleRepo.search(map, PagingUtils.createPageRequest(map)); 
			
			resultMap.put("result", setModelToMap(result.getContent()));
			
			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<IDMUserRoleModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (IDMUserRoleModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(IDMUserRoleModel model) {
		Map<String, Object> map = new HashMap<>();
		
		IDMUserModel user = model.getUser();
		map.put("userCode", user.getCode());
		map.put("userName", user.getName());
		
		IDMRoleModel role = model.getRole();
		map.put("roleCode", role.getCode());
		map.put("roleName", role.getName());
		
		map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
		map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
		
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

			idmRepo.isIDMUserValid((String) map.get("userCode"));

			List<Map<String, Object>> roleCodeList = (List<Map<String, Object>>) map.get("roleCodeList");
			for (Map<String, Object> idmRoleCodeMap : roleCodeList) {
				idmRepo.isIDMRoleValid((String) idmRoleCodeMap.get("roleCode"));
			}

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// jika edit maka simpan pending task untuk old value. Old value
				// diambil dari DB
				Map<String, Object> idmUserRoleOld = new HashMap<>();
				idmUserRoleOld.put("userCode", (String) map.get("userCode"));

				idmUserRoleOld.put("roleCodeList",
						setModelToMap(userRoleRepo.search(idmUserRoleOld, null).getContent()));
				vo.setJsonObjectOld(idmUserRoleOld);
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
		vo.setUniqueKey((String) map.get("userCode"));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("IDMUserRoleSC");

		return vo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

				String userCode = (String) map.get("userCode");

				List<Map<String, Object>> list = (ArrayList<Map<String, Object>>) map.get("roleCodeList");
				IDMUserModel idmUser = new IDMUserModel();
				idmUser.setCode(userCode);
				updateUserRole(idmUser, list, vo.getCreatedBy());
			}
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
	public void updateUserRole(IDMUserModel idmUser, List<Map<String, Object>> roleCodeList, String createdBy){
		// delete all by user code
		userRoleRepo.deleteByUserCode(idmUser.getCode());
		userRoleRepo.flush();

		for (Map<String, Object> roleMap : roleCodeList) {
			// insert new user role
			IDMUserRoleModel userRole = new IDMUserRoleModel();

			IDMRoleModel role = new IDMRoleModel();
			role.setCode((String) roleMap.get("roleCode"));
			userRole.setRole(role);

			userRole.setUser(idmUser);

			userRole.setCreatedDate(DateUtils.getCurrentTimestamp());
			userRole.setCreatedBy(createdBy);

			userRoleRepo.persist(userRole);
		}
	}
	
	@Override
	public void saveUserRole(IDMUserModel idmUser, String roleCode, String createdBy){
		IDMUserRoleModel userRole = new IDMUserRoleModel();
		userRole.setUser(idmUser);
		
		IDMRoleModel role = new IDMRoleModel();
		role.setCode(roleCode);
		userRole.setRole(role);
		userRole.setCreatedBy(createdBy);
		userRole.setCreatedDate(DateUtils.getCurrentTimestamp());
		userRoleRepo.save(userRole);
	}
}