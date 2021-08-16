package com.gpt.component.idm.rolemenu.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menutree.model.IDMMenuTreeModel;
import com.gpt.component.idm.menutree.services.IDMMenuTreeService;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.idm.rolemenu.model.IDMRoleMenuModel;
import com.gpt.component.idm.rolemenu.repository.IDMRoleMenuRepository;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMRoleMenuServiceImpl implements IDMRoleMenuService {

	@Autowired
	private IDMRoleMenuRepository roleMenuRepo;

	@Autowired
	private IDMRepository idmRepo;

	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private IDMMenuTreeService menuTreeService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			resultMap.put("result", setModelToMap(roleMenuRepo.searchRoleMenu((String) map.get("roleCode"))));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<IDMRoleMenuModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (IDMRoleMenuModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(IDMRoleMenuModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_MENUCODE,
				ValueUtils.hasValue(model.getMenu()) ? model.getMenu().getCode() : ApplicationConstants.EMPTY_STRING);
		map.put("menuName",
				ValueUtils.hasValue(model.getMenu()) ? model.getMenu().getName() : ApplicationConstants.EMPTY_STRING);

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

			idmRepo.isIDMRoleValid((String) map.get("roleCode"));

			List<Map<String, Object>> menuCodeList = (List<Map<String, Object>>) map.get("menuCodeList");
			for (Map<String, Object> idmMenuCodeMap : menuCodeList) {
				idmRepo.isIDMMenuValid((String) idmMenuCodeMap.get(ApplicationConstants.STR_MENUCODE));
			}

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// jika edit maka simpan pending task untuk old value. Old value
				// diambil dari DB
				Map<String, Object> idmRoleMenuOld = new HashMap<>();
				idmRoleMenuOld.put("menuCodeList",
						setModelToMap(roleMenuRepo.searchRoleMenu((String) map.get("roleCode"))));
				vo.setJsonObjectOld(idmRoleMenuOld);
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
		vo.setUniqueKey((String) map.get("roleCode"));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("IDMRoleMenuSC");

		return vo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

				String roleCode = (String) map.get("roleCode");
				IDMRoleModel idmRole = new IDMRoleModel();
				idmRole.setCode(roleCode);

				List<Map<String, Object>> list = (ArrayList<Map<String, Object>>) map.get("menuCodeList");

				List<IDMMenuModel> menuList = new ArrayList<>();
				for (Map<String, Object> menuMap : list) {
					IDMMenuModel menu = new IDMMenuModel();
					menu.setCode((String) menuMap.get(ApplicationConstants.STR_MENUCODE));

					menuList.add(menu);
				}
				updateRoleMenu(idmRole, menuList, vo.getCreatedBy());
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
	public void saveRoleMenu(IDMRoleModel idmRole, List<IDMMenuModel> menuList, String createdBy)
			throws ApplicationException, BusinessException {
		for (IDMMenuModel menu : menuList) {
			if(menu != null && menu.getCode() != null) {
				// insert new role menu
				IDMRoleMenuModel roleMenu = new IDMRoleMenuModel();

				roleMenu.setRole(idmRole);
				roleMenu.setMenu(menu);

				roleMenu.setCreatedDate(DateUtils.getCurrentTimestamp());
				roleMenu.setCreatedBy(createdBy);

				roleMenuRepo.save(roleMenu);
			}
		}
	}

	@Override
	public void updateRoleMenu(IDMRoleModel idmRole, List<IDMMenuModel> menuList, String updatedBy)
			throws ApplicationException, BusinessException {
		// delete all by role code
		roleMenuRepo.deleteByRoleCode(idmRole.getCode());
		roleMenuRepo.flush();

		for (IDMMenuModel menu : menuList) {
			// insert new role menu
			IDMRoleMenuModel roleMenu = new IDMRoleMenuModel();

			roleMenu.setRole(idmRole);
			roleMenu.setMenu(menu);

			roleMenu.setCreatedDate(DateUtils.getCurrentTimestamp());
			roleMenu.setCreatedBy(updatedBy);

			roleMenuRepo.persist(roleMenu);
		}
	}

	@Override
	public void deleteRoleMenu(IDMRoleModel idmRole) throws ApplicationException, BusinessException {
		roleMenuRepo.deleteByRoleCode(idmRole.getCode());
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void deleteRoleMenuNewTrx(IDMRoleModel idmRole) throws ApplicationException, BusinessException {
		roleMenuRepo.deleteByRoleCode(idmRole.getCode());
	}

	@Override
	public List<IDMRoleMenuModel> searchRoleMenu(String roleCode) throws ApplicationException, BusinessException {
		try {
			return roleMenuRepo.searchRoleMenu(roleCode);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public List<Map<String, Object>> searchRoleMenuGetMap(String roleCode, boolean isIncludeParent) throws ApplicationException, BusinessException {
		try {
			List<IDMRoleMenuModel> idmRoleMenuList = (ArrayList<IDMRoleMenuModel>)roleMenuRepo.searchRoleMenu(roleCode);
			
			List<Map<String, Object>> resultList = new ArrayList<>();
			for (IDMRoleMenuModel model : idmRoleMenuList) {
				IDMMenuModel menu = model.getMenu();
				if(menu != null) {
					String menuTypeCode = menu.getMenuType().getCode();
					
					if(isIncludeParent){
						//query to menu tree
						IDMMenuTreeModel menuTree= menuTreeService.searchByMenuCode(menu.getCode(), Helper.getActiveApplicationCode());
						
						Map<String, Object> menuTreeMap = getMenuTreeMap(menuTree);
						if(menuTreeMap != null) {
							resultList.add(menuTreeMap);
						}
					}else{
						//tidak perlu di add jika menu nya adalah parent
						if(!"E".equals(menuTypeCode)){
							//query to menu tree
							IDMMenuTreeModel menuTree= menuTreeService.searchByMenuCode(menu.getCode(), Helper.getActiveApplicationCode());
							
							resultList.add(getMenuTreeMap(menuTree));
						}
					}
				}
				
			}
			
			return resultList;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private Map<String, Object> getMenuTreeMap(IDMMenuTreeModel menuTree){
		if(menuTree != null) {
			IDMMenuModel menu = menuTree.getMenu();
			IDMMenuModel parent = menuTree.getParentMenu();
			
			Map<String, Object> menuTreeModel = new HashMap<>();
			menuTreeModel.put(ApplicationConstants.STR_MENUCODE, menu.getCode());
			menuTreeModel.put("menuName", menu.getName());
			menuTreeModel.put("lvl", menuTree.getLvl());
			menuTreeModel.put("idx", menuTree.getIdx());
			
			if(parent != null){
				menuTreeModel.put("parentMenuCode", parent.getCode());
				menuTreeModel.put("parentMenuName", parent.getName());
			}
			
			return menuTreeModel;
		}
		
		
		return null;
	}
}
