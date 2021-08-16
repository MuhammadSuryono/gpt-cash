package com.gpt.product.gpcash.menupackage.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.gpt.component.idm.role.services.IDMRoleService;
import com.gpt.component.idm.rolemenu.model.IDMRoleMenuModel;
import com.gpt.component.idm.rolemenu.services.IDMRoleMenuService;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.menupackage.model.MenuPackageModel;
import com.gpt.product.gpcash.menupackage.repository.MenuPackageRepository;
import com.gpt.product.gpcash.menupackage.spi.MenuPackageListener;
import com.gpt.product.gpcash.servicepackage.model.ServicePackageModel;
import com.gpt.product.gpcash.servicepackage.repository.ServicePackageRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class MenuPackageServiceImpl implements MenuPackageService{
	
	@Autowired
	private MenuPackageRepository menuPackageRepo;
	
	@Autowired
	private IDMRoleMenuService roleMenuService;
	
	@Autowired
	private IDMRoleService roleService;
	
	@Autowired
	private IDMMenuTreeService menuTreeService;
	
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private ServicePackageRepository servicePackageRepo;
	
	private List<MenuPackageListener> menuPackageListener;
	
	@Autowired(required=false)
	public void setmenuPackageListener(List<MenuPackageListener> menuPackageListener) {
		this.menuPackageListener = menuPackageListener;
	}

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<MenuPackageModel> result = menuPackageRepo.search(map, PagingUtils.createPageRequest(map));
			
			resultMap.put("result", setModelToMap(result.getContent(), false));
			
			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<MenuPackageModel> list, boolean isGetDetail) throws ApplicationException, BusinessException{
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		for (MenuPackageModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));		
		}
		
		return resultList;
	}
	
	private Map<String, Object> setModelToMap(MenuPackageModel model, boolean isGetDetail) throws ApplicationException, BusinessException {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));

		if(isGetDetail){
			String roleCode = model.getRole().getCode();
			map.put("roleCode", ValueUtils.getValue(roleCode));
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
			
			if (model.getRole() != null) {
				List<Map<String, String>> listDetail = new ArrayList<>();
				
				List<IDMRoleMenuModel> roleMenuList = roleMenuService.searchRoleMenu(roleCode);
				for (IDMRoleMenuModel detailModel : roleMenuList) {
					Map<String, String> detail = new HashMap<>();

					IDMMenuModel menu = detailModel.getMenu();
					if(menu != null) {
						detail.put(ApplicationConstants.STR_MENUCODE, detailModel.getMenu().getCode());
					}

					listDetail.add(detail);
				}
				map.put("menuList", listDetail);
			}
		}

		return map;
	}
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			PendingTaskVO vo = setPendingTaskVO(map); 
			vo.setJsonObject(map); 
			
			//check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);
			
			String code = (String)map.get(ApplicationConstants.STR_CODE);
			
			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String)map.get(ApplicationConstants.STR_CODE)); //cek ke table jika record already exist
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				//check existing record exist or not
				MenuPackageModel menuPackageOld = getExistingRecord(code, true);
				vo.setJsonObjectOld(setModelToMap(menuPackageOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				
				//check existing record exist or not
				getExistingRecord(code, true);
				
				checkIsMenuPackageStillInUsed(code);
			}else{
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
	
	private void checkIsMenuPackageStillInUsed(String menuPackageCode) throws Exception{
		try {
			List<ServicePackageModel> servicePackageList = servicePackageRepo.findByMenuPackageCode(menuPackageCode);
			if(servicePackageList.size() > 0) {
				throw new BusinessException("GPT-0100131");
			}
		} catch (BusinessException e) {
			throw e;
		}
	}
	
	private MenuPackageModel getExistingRecord(String code, boolean isThrowError) throws BusinessException{
		MenuPackageModel model = menuPackageRepo.findOne(code);
		
		if(model == null){
			if(isThrowError)
				throw new BusinessException("GPT-0100001");
		}else{
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				if(isThrowError){
					throw new BusinessException("GPT-0100001");
				}else{
					return null;
				}
			}
		}
		
		return model;
	}
	
	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.STR_CODE));
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.STR_CODE)).concat(ApplicationConstants.DELIMITER_DASH)
				.concat((String) map.get(ApplicationConstants.STR_NAME)));
		vo.setCreatedBy((String)map.get(ApplicationConstants.LOGIN_USERID)); 
		vo.setMenuCode((String)map.get(ApplicationConstants.STR_MENUCODE)); 
		vo.setService("MenuPackageSC");
		
		return vo;
	}
	
	private MenuPackageModel setMapToModel(Map<String, Object> map) {
		MenuPackageModel menuPackage = new MenuPackageModel();
		menuPackage.setCode((String)map.get(ApplicationConstants.STR_CODE));
		menuPackage.setName((String)map.get(ApplicationConstants.STR_NAME));
		
		IDMApplicationModel application = new IDMApplicationModel();
		application.setCode(Helper.getActiveApplicationCode());
		menuPackage.setApplication(application);
		
		return menuPackage;
	}
	
	private void checkUniqueRecord(String code) throws Exception {
			MenuPackageModel menuPackage = menuPackageRepo.findOne(code);
			
			if(menuPackage != null && ApplicationConstants.NO.equals(menuPackage.getDeleteFlag())){
				throw new BusinessException("GPT-0100004");
			}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (HashMap<String, Object>)vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				MenuPackageModel menuPackage = setMapToModel(map);
				MenuPackageModel menuPackageExisting = menuPackageRepo.findOne(menuPackage.getCode());
				
				//cek jika record replacement
				if(menuPackageExisting != null && ApplicationConstants.YES.equals(menuPackageExisting.getDeleteFlag())){										
					//set value yg bisa di ganti
					menuPackageExisting.setName(menuPackage.getName());
					
					//set default value
					menuPackageExisting.setIdx(ApplicationConstants.IDX);
					menuPackageExisting.setDeleteFlag(ApplicationConstants.NO);
					menuPackageExisting.setInactiveFlag(ApplicationConstants.NO);
					menuPackageExisting.setSystemFlag(ApplicationConstants.NO);
					menuPackageExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					menuPackageExisting.setCreatedBy(vo.getCreatedBy());
					
					//set reset value
					menuPackageExisting.setUpdatedDate(null);
					menuPackageExisting.setUpdatedBy(null);
					
					//set new detail
					List<IDMMenuModel> menuList = getParentMenuModel((ArrayList<Map<String,Object>>) map.get("menuList"));
					
					//update role
					IDMRoleModel idmRole = menuPackageExisting.getRole();
					idmRole.setDeleteFlag(ApplicationConstants.NO);
					roleService.updateRole(idmRole, vo.getCreatedBy());
					
					//save role menu
					roleMenuService.saveRoleMenu(menuPackageExisting.getRole(), menuList, vo.getCreatedBy());
					
					menuPackageRepo.save(menuPackageExisting);
				} else {
					//set default value
					menuPackage.setIdx(ApplicationConstants.IDX);
					menuPackage.setDeleteFlag(ApplicationConstants.NO);
					menuPackage.setInactiveFlag(ApplicationConstants.NO);
					menuPackage.setSystemFlag(ApplicationConstants.NO);
					menuPackage.setCreatedDate(DateUtils.getCurrentTimestamp());
					menuPackage.setCreatedBy(vo.getCreatedBy());
					
					//set new detail
					List<IDMMenuModel> menuList = getParentMenuModel((ArrayList<Map<String,Object>>) map.get("menuList"));
					
					//save role
					String roleCode = UUID.randomUUID().toString().toUpperCase();
					IDMRoleModel idmRole = new IDMRoleModel();
					idmRole.setCode(roleCode);
					idmRole.setName(menuPackage.getCode());
					idmRole.setApplication(menuPackage.getApplication());
					roleService.saveRole(idmRole, vo.getCreatedBy());
					
					menuPackage.setRole(idmRole);
					
					//save role menu
					roleMenuService.saveRoleMenu(menuPackage.getRole(), menuList, vo.getCreatedBy());
					
					menuPackageRepo.persist(menuPackage);
				}
			} else if(ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				MenuPackageModel menuPackageNew = setMapToModel(map);
				MenuPackageModel menuPackageExisting = getExistingRecord(menuPackageNew.getCode(), true);
				
				//set value yg boleh di edit
				menuPackageExisting.setName(menuPackageNew.getName());
				menuPackageExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				menuPackageExisting.setUpdatedBy(vo.getCreatedBy());
				
				//set new detail
				List<IDMMenuModel> menuList = getParentMenuModel((ArrayList<Map<String,Object>>) map.get("menuList"));
				
				IDMRoleModel existingRole = menuPackageExisting.getRole();
				
				//find delete list and delete from corporate
				List<String> menuListString = new ArrayList<>();
				for(IDMMenuModel menu : menuList) {
					if(menu != null) {
						menuListString.add(menu.getCode());
					}
				}
				
				//harus delete ke listener dl baru save role menu, jika tidak maka tidak ada menu yg terdelete
				if(menuPackageListener != null) {
					for(MenuPackageListener listener : menuPackageListener) {
						listener.deleteALLCorporateMenuByMenuList(existingRole.getCode() , menuListString);
					}
				}
				
				//save role menu
				roleMenuService.updateRoleMenu(existingRole, menuList, vo.getCreatedBy());
				
				menuPackageRepo.save(menuPackageExisting); 
			} else if(ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				String code = (String)map.get(ApplicationConstants.STR_CODE);
				
				MenuPackageModel menuPackage = getExistingRecord(code, true);
				menuPackage.setDeleteFlag(ApplicationConstants.YES);
				menuPackage.setUpdatedDate(DateUtils.getCurrentTimestamp());
				menuPackage.setUpdatedBy(vo.getCreatedBy());
				
				checkIsMenuPackageStillInUsed(code);
				
				//delete role
				roleService.deleteRole(menuPackage.getRole(), vo.getCreatedBy());
				
				//delete role menu
				roleMenuService.deleteRoleMenu(menuPackage.getRole());
				
				menuPackageRepo.save(menuPackage);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return vo;
	}
	
	private List<IDMMenuModel> getParentMenuModel(List<Map<String, Object>> menuMapList) throws ApplicationException, BusinessException{
		List<IDMMenuModel> menuList = new ArrayList<>();
		for(Map<String, Object> menuMap : menuMapList){
			String menuCode = (String)menuMap.get(ApplicationConstants.STR_MENUCODE);
			
			if(menuCode != null) {
				IDMMenuTreeModel menuTree = menuTreeService.searchByMenuCode(menuCode, Helper.getActiveApplicationCode());
				//hanya insert parent menu yg blm pernah di insert
				if(menuTree != null) {
					if(!menuList.contains(menuTree.getParentMenu())){
						menuList.add(menuTree.getParentMenu());
					}
				}
				
				
				IDMMenuModel menu = new IDMMenuModel();
				menu.setCode(menuCode);
				menuList.add(menu);
			}
		}
		
		return menuList;
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		//if any spesific business function for reject, applied here
		
		return vo;
	}

	@Override
	public Map<String, Object> searchMenuPackageDetailByCode(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			MenuPackageModel menuPackage = getExistingRecord((String)map.get(ApplicationConstants.STR_CODE), true);
			
			resultMap.put("menuList", roleMenuService.searchRoleMenuGetMap(menuPackage.getRole().getCode(), false));
			resultMap.put(ApplicationConstants.STR_CODE, menuPackage.getCode());
			resultMap.put(ApplicationConstants.STR_NAME, menuPackage.getName());
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
}