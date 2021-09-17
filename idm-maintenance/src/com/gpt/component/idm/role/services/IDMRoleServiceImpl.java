package com.gpt.component.idm.role.services;

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
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menutree.model.IDMMenuTreeModel;
import com.gpt.component.idm.menutree.services.IDMMenuTreeService;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.idm.role.repository.IDMRoleRepository;
import com.gpt.component.idm.rolemenu.model.IDMRoleMenuModel;
import com.gpt.component.idm.rolemenu.repository.IDMRoleMenuRepository;
import com.gpt.component.idm.rolemenu.services.IDMRoleMenuService;
import com.gpt.component.idm.roletype.model.IDMRoleTypeModel;
import com.gpt.component.idm.userrole.repository.IDMUserRoleRepository;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMRoleServiceImpl implements IDMRoleService {

	@Autowired
	private IDMRoleRepository idmRoleRepo;

	@Autowired
	private IDMRepository idmRepo;

	@Autowired
	private IDMRoleMenuRepository idmRoleMenuRepo;

	@Autowired
	private IDMRoleMenuService idmRoleMenuService;

	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private IDMMenuTreeService menuTreeService;
	
	@Autowired
	private IDMUserRoleRepository idmUserRoleRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<IDMRoleModel> result = idmRoleRepo.search(map, PagingUtils.createPageRequest(map));

			if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else {
				resultMap.put("result", setModelToMap(result.getContent(), true));
			}

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<IDMRoleModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (IDMRoleModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(IDMRoleModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("dscp", ValueUtils.getValue(model.getDscp()));

		IDMApplicationModel app = model.getApplication();
		map.put("appCode", app.getCode());
		map.put("appName", app.getName());

		if (isGetDetail) {
			List<IDMRoleMenuModel> roleMenuList = idmRoleMenuRepo.searchRoleMenu(model.getCode());

			List<Map<String, Object>> menuList = new ArrayList<>();
			for (IDMRoleMenuModel roleMenu : roleMenuList) {
				Map<String, Object> menuMap = new HashMap<>();

				IDMMenuModel menu = roleMenu.getMenu();
				menuMap.put(ApplicationConstants.STR_MENUCODE, menu.getCode());
				menuMap.put("menuName", menu.getName());
				menuMap.put("menuType", menu.getMenuType().getCode());

				menuList.add(menuMap);
			}
			map.put("menuCodeList", menuList);

			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}

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

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String) map.get(ApplicationConstants.STR_CODE)); 
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				IDMRoleModel idmRoleOld = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
				
				validateUpdateDeleteRole(idmRoleOld);
				
				vo.setJsonObjectOld(setModelToMap(idmRoleOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				IDMRoleModel idmRole = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);

				validateUpdateDeleteRole(idmRole);
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
	
	private void validateUpdateDeleteRole(IDMRoleModel idmRole) throws BusinessException {
		// validate cannot delete role type WF
		if (idmRole != null) {
			if (ApplicationConstants.ROLE_TYPE_WF.equals(idmRole.getRoleType().getCode())) {
				throw new BusinessException("GPT-0100046");
			}
			
			if(ApplicationConstants.YES.equals(idmRole.getSystemFlag())) {
				throw new BusinessException("GPT-0100046");
			}
		}
		
		// validate cannot delete role that is used by user
		List<String> userRoleList = idmUserRoleRepo.findByRoleCode(idmRole.getCode());
		if(userRoleList.size()>0) {
			throw new BusinessException("GPT-0100230");
		}
	}

	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			//TODO validasi role name tidak boleh sama
			
			List<Map<String, Object>> menuCodeList = (List<Map<String, Object>>) map.get("menuCodeList");
			for (Map<String, Object> idmMenuCodeMap : menuCodeList) {
				idmRepo.isIDMMenuValid((String) idmMenuCodeMap.get(ApplicationConstants.STR_MENUCODE));
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private IDMRoleModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		IDMRoleModel model = idmRoleRepo.findOne(code);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		} else {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				if (isThrowError) {
					throw new BusinessException("GPT-0100001");
				} else {
					return null;
				}
			}
		}

		return model;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.STR_CODE));
		vo.setUniqueKeyDisplay((String) map.get(ApplicationConstants.STR_NAME));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("IDMRoleSC");

		return vo;
	}

	private IDMRoleModel setMapToModel(Map<String, Object> map) {
		IDMRoleModel idmRole = new IDMRoleModel();
		idmRole.setCode((String) map.get(ApplicationConstants.STR_CODE));
		idmRole.setName((String) map.get(ApplicationConstants.STR_NAME));

		if (ValueUtils.hasValue(map.get("dscp")))
			idmRole.setDscp((String) map.get("dscp"));

		if (ValueUtils.hasValue(map.get("appCode"))) {
			IDMApplicationModel idmApplication = new IDMApplicationModel();
			idmApplication.setCode((String) map.get("appCode"));
			idmRole.setApplication(idmApplication);
		}

		if (ValueUtils.hasValue(map.get("roleTypeCode"))) {
			IDMRoleTypeModel idmRoleType = new IDMRoleTypeModel();
			idmRoleType.setCode((String) map.get("roleTypeCode"));
			idmRole.setRoleType(idmRoleType);
		}

		return idmRole;
	}

	private void checkUniqueRecord(String idmRoleCode) throws Exception {
		IDMRoleModel idmRole = idmRoleRepo.findOne(idmRoleCode);

		if (idmRole != null && ApplicationConstants.NO.equals(idmRole.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				IDMRoleModel idmRole = setMapToModel(map);
				IDMRoleModel idmRoleExisting = idmRoleRepo.findOne(idmRole.getCode());

				// cek jika record replacement
				if (idmRoleExisting != null && ApplicationConstants.YES.equals(idmRoleExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					idmRoleExisting.setName(idmRole.getName());
					idmRoleExisting.setDscp(idmRole.getDscp());
					idmRoleExisting.setApplication(idmRole.getApplication());

					saveRole(idmRoleExisting, vo.getCreatedBy());

					updateRoleMenu((ArrayList<Map<String, Object>>) map.get("menuCodeList"), idmRoleExisting,
							vo.getCreatedBy());
				} else {
					saveRole(idmRole, vo.getCreatedBy());

					updateRoleMenu((ArrayList<Map<String, Object>>) map.get("menuCodeList"), idmRole,
							vo.getCreatedBy());
				}

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				IDMRoleModel idmRoleNew = setMapToModel(map);

				// check existing record exist or not
				IDMRoleModel idmRoleExisting = getExistingRecord(idmRoleNew.getCode(), true);

				// set value yg boleh di edit
				idmRoleExisting.setName(idmRoleNew.getName());
				idmRoleExisting.setDscp(idmRoleNew.getDscp());
				idmRoleExisting.setApplication(idmRoleNew.getApplication());

				updateRole(idmRoleExisting, vo.getCreatedBy());
				
				updateRoleMenu((ArrayList<Map<String, Object>>) map.get("menuCodeList"), idmRoleExisting,
						vo.getCreatedBy());
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				IDMRoleModel idmRole = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);

				deleteRole(idmRole, vo.getCreatedBy());
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
	public void saveRole(IDMRoleModel idmRole, String createdBy) throws ApplicationException, BusinessException {
		// set default value
		idmRole.setDeleteFlag(ApplicationConstants.NO);
		idmRole.setInactiveFlag(ApplicationConstants.NO);
		idmRole.setSystemFlag(ApplicationConstants.NO);
		idmRole.setCreatedDate(DateUtils.getCurrentTimestamp());
		idmRole.setCreatedBy(createdBy);
		idmRole.setUpdatedDate(null);
		idmRole.setUpdatedBy(null);

		// yg boleh di configure hanya AP, WF ada konfigurasi system
		IDMRoleTypeModel idmRoleTypeModel = new IDMRoleTypeModel();
		idmRoleTypeModel.setCode(ApplicationConstants.ROLE_TYPE_AP);
		idmRole.setRoleType(idmRoleTypeModel);

		idmRoleRepo.saveAndFlush(idmRole);
	}

	@Override
	public void updateRole(IDMRoleModel idmRole, String updatedBy) throws ApplicationException, BusinessException {
		idmRole.setUpdatedDate(DateUtils.getCurrentTimestamp());
		idmRole.setUpdatedBy(updatedBy);
		idmRoleRepo.save(idmRole);
	}

	@Override
	public void deleteRole(IDMRoleModel idmRole, String deletedBy) throws ApplicationException, BusinessException {
		idmRole.setDeleteFlag(ApplicationConstants.YES);
		idmRole.setUpdatedDate(DateUtils.getCurrentTimestamp());
		idmRole.setUpdatedBy(deletedBy);

		idmRoleRepo.save(idmRole);
	}

	private void updateRoleMenu(List<Map<String, Object>> list, IDMRoleModel idmRole, String createdBy)
			throws ApplicationException, BusinessException {
		List<IDMMenuModel> menuList = getParentMenuModel(list);
		
		idmRoleMenuService.updateRoleMenu(idmRole, menuList, createdBy);
	}
	
	private List<IDMMenuModel> getParentMenuModel(List<Map<String, Object>> menuMapList) throws ApplicationException, BusinessException{
		List<IDMMenuModel> menuList = new ArrayList<>();
		for(Map<String, Object> menuMap : menuMapList){
			String menuCode = (String)menuMap.get(ApplicationConstants.STR_MENUCODE);
			
			if(menuCode != null) {
				IDMMenuTreeModel menuTree = menuTreeService.searchByMenuCode(menuCode, ApplicationConstants.APP_GPCASHBO);
				//hanya insert parent menu yg blm pernah di insert
				if(menuTree != null) {
					if(!menuList.contains(menuTree.getParentMenu())){
						if(menuTree.getParentMenu() != null) {
							menuList.add(menuTree.getParentMenu());
						}
					}
				}
				
				
				IDMMenuModel menu = new IDMMenuModel();
				menu.setCode(menuCode);
				menuList.add(menu);
			}
			
		}
		
		return menuList;
	}
}
