package com.gpt.component.idm.menutree.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menutree.model.IDMMenuTreeModel;
import com.gpt.component.idm.menutree.repository.IDMMenuTreeRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMMenuTreeServiceImpl implements IDMMenuTreeService{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IDMMenuTreeRepository menuTreeRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String appCode = (String)map.get(ApplicationConstants.APP_CODE);
			
			//query to get parent menu
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put("lvl", "1");
			inputMap.put(ApplicationConstants.APP_CODE,appCode);
			Page<IDMMenuTreeModel> parentMenuList = menuTreeRepo.search(inputMap, 
					new PageRequest(0, 999, new Sort(Sort.Direction.ASC , "idx")));
			
			List<Map<String, Object>> resultMenuList = new ArrayList<>();
			for(IDMMenuTreeModel parentMenu : parentMenuList.getContent()){
				logger.debug("parentmenu " + parentMenu.getMenu().getCode());
				
				//get child menu based on parent menu
				inputMap = new HashMap<>();
				inputMap.put(ApplicationConstants.APP_CODE, appCode);
				inputMap.put("parentMenuCode", parentMenu.getMenu().getCode());
				
				Page<IDMMenuTreeModel> childMenuList = menuTreeRepo.search(inputMap, 
						new PageRequest(0, 999, new Sort(Sort.Direction.ASC , "idx")));
				
				//add menu
				if(childMenuList.getContent().size() > 0) {
					resultMenuList.addAll(setModelToMap(childMenuList.getContent()));
				} else {
					Map<String, Object> menuMap = new HashMap<>();
					IDMMenuModel menuParent = parentMenu.getMenu();
					menuMap.put(ApplicationConstants.STR_MENUCODE, menuParent.getCode());
					menuMap.put("menuName", menuParent.getName());
					menuMap.put("parentMenuCode", ApplicationConstants.EMPTY_STRING);
					menuMap.put("parentMenuName", ApplicationConstants.EMPTY_STRING);
					resultMenuList.add(menuMap);
				}
				
			}
			
			resultMap.put("result", resultMenuList);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<IDMMenuTreeModel> list){
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		for (IDMMenuTreeModel model : list) {
			Map<String, Object> map = new HashMap<>();
			IDMMenuModel menu = model.getMenu();
			map.put(ApplicationConstants.STR_MENUCODE, menu.getCode());
			map.put("menuName", menu.getName());
			
			IDMMenuModel parentMenu = model.getParentMenu();
			if(parentMenu != null){
				map.put("parentMenuCode", parentMenu.getCode());
				map.put("parentMenuName", parentMenu.getName());
			}else{
				map.put("parentMenuCode", ApplicationConstants.EMPTY_STRING);
				map.put("parentMenuName", ApplicationConstants.EMPTY_STRING);
			}
			
			
			resultList.add(map);			
		}
		
		return resultList;
	}

	@Override
	public IDMMenuTreeModel searchByMenuCode(String menuCode, String applicationCode)
			throws ApplicationException, BusinessException {
		try{
			Map<String, Object> inputMap = new HashMap<>();
			inputMap.put(ApplicationConstants.APP_CODE,applicationCode);
			inputMap.put("searchMenuCode", menuCode);
			Page<IDMMenuTreeModel> parentMenuList = menuTreeRepo.search(inputMap, null);
			
			if(parentMenuList.getContent().size() > 0){
				return (IDMMenuTreeModel)parentMenuList.getContent().get(0);
			}else{
				return null;
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

}
