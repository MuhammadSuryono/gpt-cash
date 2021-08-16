package com.gpt.product.gpcash.corporate.corporateusergroup.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.gpt.component.idm.rolemenu.repository.IDMRoleMenuRepository;
import com.gpt.component.idm.rolemenu.services.IDMRoleMenuService;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.repository.CorporateRepository;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.repository.CorporateAccountGroupRepository;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.services.CorporateAccountGroupService;
import com.gpt.product.gpcash.corporate.corporatelimit.model.CorporateLimitModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.repository.CorporateUserGroupRepository;
import com.gpt.product.gpcash.corporate.corporateusergroup.spi.CorporateUserGroupListener;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;
import com.gpt.product.gpcash.currencymatrix.model.CurrencyMatrixModel;
import com.gpt.product.gpcash.menupackage.spi.MenuPackageListener;
import com.gpt.product.gpcash.service.model.ServiceModel;
import com.gpt.product.gpcash.servicecurrencymatrix.model.ServiceCurrencyMatrixModel;
import com.gpt.product.gpcash.servicecurrencymatrix.repository.ServiceCurrencyMatrixRepository;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateUserGroupServiceImpl implements CorporateUserGroupService, MenuPackageListener{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CorporateUserGroupRepository corporateUserGroupRepo;

	@Autowired
	private ServiceCurrencyMatrixRepository serviceCurrencyMatrixRepo;

	@Autowired
	private IDMRoleMenuRepository roleMenuRepo;

	@Autowired
	private CorporateAccountGroupRepository corporateAccountGroupRepo;
	
	@Autowired
	private CorporateAccountGroupService corporateAccountGroupService;
	
	@Autowired
	private CorporateAdminPendingTaskService pendingTaskService;

	@Autowired
	private IDMRoleMenuService roleMenuService;

	@Autowired
	private IDMRoleService roleService;

	@Autowired
	private IDMMenuTreeService menuTreeService;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private CorporateRepository corporateRepo;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired(required=false)
	private CorporateUserGroupListener corporateUserGroupListener;
	
	public void setCorporateUserGroupListener(CorporateUserGroupListener corporateUserGroupListener) {
		this.corporateUserGroupListener = corporateUserGroupListener;
	}

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CorporateUserGroupModel> result = corporateUserGroupRepo.searchUserGroupNotAdmin(map,
					PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<CorporateUserGroupModel> list, boolean isGetDetail)
			throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CorporateUserGroupModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CorporateUserGroupModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("beneficiaryScope", ValueUtils.getValue(model.getBenScope()));
		
		if(model.getCorporateAccountGroup() != null){
			CorporateAccountGroupModel corporateAccountGroup = model.getCorporateAccountGroup();
			map.put("accountGroupCode", ValueUtils.getValue(corporateAccountGroup.getCode()));
			map.put("accountGroupName", ValueUtils.getValue(corporateAccountGroup.getName()));
		}
		
		if(model.getCorporate() != null){
			CorporateModel corporate = model.getCorporate();
			map.put(ApplicationConstants.CORP_ID, corporate.getId());
			map.put("corporateName", corporate.getName());
		}

		if (isGetDetail) {

			List<Map<String, Object>> limitList = new ArrayList<>();
			if (model.getCorporateUserGroupDetail() != null) {
				for (CorporateUserGroupDetailModel detailModel : model.getCorporateUserGroupDetail()) {
					Map<String, Object> limit = new HashMap<>();

					CurrencyModel limitCurrency = detailModel.getCurrency();
					ServiceModel service = detailModel.getService();

					limit.put("maxAmountLimit", detailModel.getMaxAmountLimit().toPlainString());
					limit.put("currencyCode", limitCurrency.getCode());
					limit.put("currencyName", limitCurrency.getName());
					limit.put("serviceCode", service.getCode());
					limit.put("serviceName", service.getName());
					
					CurrencyMatrixModel currencyMatrix = detailModel.getServiceCurrencyMatrix().getCurrencyMatrix();
					limit.put("currencyMatrixCode", currencyMatrix.getCode());
					limit.put("currencyMatrixName", currencyMatrix.getName());

					limitList.add(limit);
				}
				map.put("limitList", limitList);
			}

			List<Map<String, Object>> menuList = new ArrayList<>();
			if (model.getRole() != null) {
				IDMRoleModel role = model.getRole();
				List<IDMRoleMenuModel> roleMenu = roleMenuRepo.searchRoleMenu(role.getCode());
				for (IDMRoleMenuModel roleMenuModel : roleMenu) {
					Map<String, Object> menuMap = new HashMap<>();
					IDMMenuModel menu = roleMenuModel.getMenu();

					menuMap.put(ApplicationConstants.STR_MENUCODE, menu.getCode());
					menuMap.put("menuName", menu.getName());
					
					IDMMenuTreeModel menuTree = menuTreeService.searchByMenuCode(menu.getCode(), ApplicationConstants.APP_GPCASHIB);
					
					if(menuTree != null) {
						IDMMenuModel parentMenu = menuTree.getParentMenu();
						if(parentMenu != null) {
							menuMap.put("parentMenuCode", parentMenu.getCode());
							menuMap.put("parentMenuName", parentMenu.getName());
						}
					}
					
					
					menuList.add(menuMap);
				}
				map.put("menuList", menuList);
			}

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
			CorporateAdminPendingTaskVO vo = setCorporateAdminPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				checkCustomValidation(map);

				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);

				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				CorporateUserGroupModel corporateUserGroupOld = getExistingRecord(
						(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(corporateUserGroupOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				
				String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
				String userGroupCode = (String) map.get(ApplicationConstants.STR_CODE);
				
				// check existing record exist or not
				getExistingRecord(corporateId, userGroupCode, true);
				
				//validasi jika masih terdapat user yg memakai user group ini
				List<CorporateUserModel> userList = corporateUtilsRepo.getCorporateUserRepo().
							findCorporateUserByUserGroupCode(corporateId, userGroupCode);

				if(userList.size() > 0) {
					throw new BusinessException("GPT-0100162");
				}
				
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

	@SuppressWarnings("unchecked")
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);

			//delete tidak perlu cek account group code
			if (!map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				String accountGroupCode = (String) map.get("accountGroupCode");
				
				// check account group exist atau gak
				corporateUtilsRepo.isCorporateAccountGroupValid(corporateId, accountGroupCode);
			}
			//-----------------------------------------------------
			
			
			List<Map<String, Object>> menuList = (ArrayList<Map<String,Object>>) map.get("menuList");
			for(Map<String, Object> menuMap : menuList){
				//check menu
				idmRepo.isIDMMenuValid((String) menuMap.get(ApplicationConstants.STR_MENUCODE));
			}

			List<Map<String, Object>> limitList = (ArrayList<Map<String,Object>>) map.get("limitList");
			for(Map<String, Object> limitMap : limitList){
				// check currency matrix exist or not
				BigDecimal userGroupMaxAmountLimit = new BigDecimal((String) limitMap.get("maxAmountLimit"));
				String currencyCode = (String) limitMap.get("currencyCode");
				String serviceCode = (String) limitMap.get("serviceCode");
				String currencyMatrixCode = (String) limitMap.get("currencyMatrixCode");
				
				ServiceModel service = productRepo.isServiceValid(serviceCode);
				
				//check ke corporate limit
				CorporateLimitModel corporateLimit = corporateUtilsRepo.getCorporateLimitRepo().findByServiceAndCurrencyMatrix(
						ApplicationConstants.APP_GPCASHIB, serviceCode, 
						currencyMatrixCode, currencyCode, corporateId);
				
				//corporate user group limit cannot bigger than corporate limit
				if(userGroupMaxAmountLimit.compareTo(corporateLimit.getMaxAmountLimit()) > 0) {
					throw new BusinessException("GPT-0100156", new String[] {service.getName(), corporateLimit.getMaxAmountLimit().toPlainString()});
				}
			}

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private CorporateUserGroupModel getExistingRecord(String corporateId, String code, boolean isThrowError)
			throws Exception {
		CorporateUserGroupModel model = corporateUserGroupRepo.findByCorporateIdAndCode(corporateId, code);
		
		if (model==null) {
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

	private CorporateAdminPendingTaskVO setCorporateAdminPendingTaskVO(Map<String, Object> map) {
		CorporateAdminPendingTaskVO vo = new CorporateAdminPendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.STR_CODE));
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.STR_CODE)).concat(ApplicationConstants.DELIMITER_DASH)
				.concat((String) map.get(ApplicationConstants.STR_NAME)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CorporateUserGroupSC");
		vo.setCorporateId((String) map.get(ApplicationConstants.LOGIN_CORP_ID));

		return vo;
	}

	@SuppressWarnings("unchecked")
	private CorporateUserGroupModel setMapToModel(CorporateUserGroupModel corporateUserGroup, Map<String, Object> map,
			boolean isNew, String createdBy) throws Exception {
		corporateUserGroup.setCode((String) map.get(ApplicationConstants.STR_CODE));
		corporateUserGroup.setName((String) map.get(ApplicationConstants.STR_NAME));
		corporateUserGroup.setBenScope((String) map.get("beneficiaryScope"));
		if (isNew)
			corporateUserGroup.setApprovalLimit(BigDecimal.ZERO);
		
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		corporateUserGroup.setCorporate(corporate);
		
		CorporateAccountGroupModel corporateAccountGroup = corporateAccountGroupRepo.findByCorporateIdAndCode(corporateId, (String) map.get("accountGroupCode"));
		corporateAccountGroup.setId(corporateAccountGroup.getId());
		corporateUserGroup.setCorporateAccountGroup(corporateAccountGroup);

		List<Map<String, Object>> limitList = (ArrayList<Map<String, Object>>) map.get("limitList");

		corporateUserGroup.setCorporateUserGroupDetail(
				getCorporateUserGroupDetailList(limitList, isNew, corporateUserGroup, corporateId, createdBy));

		return corporateUserGroup;
	}

	private List<CorporateUserGroupDetailModel> getCorporateUserGroupDetailList(List<Map<String, Object>> limitList,
			boolean isNew, CorporateUserGroupModel corporateUserGroup, String corporateId, String createdBy) throws Exception {
		List<CorporateUserGroupDetailModel> corporateUserGroupDetailList = new ArrayList<>();
		for (Map<String, Object> limitMap : limitList) {
			BigDecimal maxAmountLimit = new BigDecimal((String) limitMap.get("maxAmountLimit"));
			String currencyCode = (String) limitMap.get("currencyCode");
			String serviceCode = (String) limitMap.get("serviceCode");
			String currencyMatrixCode = (String) limitMap.get("currencyMatrixCode");
			

			CorporateUserGroupDetailModel detail = null;

			if (isNew) {
				// if new for sure only add new
				detail = createNewDataCorporateUserGroupDetail(corporateUserGroup, maxAmountLimit, currencyCode,
						serviceCode, currencyMatrixCode, createdBy);
			} else {

				ServiceCurrencyMatrixModel serviceCurrencyMatrix = serviceCurrencyMatrixRepo
						.findByServiceCode(ApplicationConstants.APP_GPCASHIB, serviceCode, currencyMatrixCode);

				// check currency matrix exist or not
				detail = corporateUserGroupRepo.findDetailByServiceCurrencyMatrixIdAndUserGroupId(corporateId,
								serviceCurrencyMatrix.getId(), corporateUserGroup.getId());

				if (detail==null) { // add new data
					detail = createNewDataCorporateUserGroupDetail(corporateUserGroup, maxAmountLimit, currencyCode, 
							serviceCode, currencyMatrixCode, createdBy);
				} else { // if exist then update data
					detail.setMaxAmountLimit(maxAmountLimit);
					detail.setUpdatedBy(createdBy);
					detail.setUpdatedDate(DateUtils.getCurrentTimestamp());
				}
			}

			corporateUserGroupDetailList.add(detail);
		}
		return corporateUserGroupDetailList;
	}

	private List<IDMMenuModel> getParentMenuModel(List<Map<String, Object>> menuMapList)
			throws ApplicationException, BusinessException {
		List<IDMMenuModel> menuList = new ArrayList<>();
		for (Map<String, Object> menuMap : menuMapList) {
			String menuCode = (String) menuMap.get(ApplicationConstants.STR_MENUCODE);

			IDMMenuTreeModel menuTree = menuTreeService.searchByMenuCode(menuCode, ApplicationConstants.APP_GPCASHIB);
			// hanya insert parent menu yg blm pernah di insert
			if (!menuList.contains(menuTree.getParentMenu())) {
				menuList.add(menuTree.getParentMenu());
			}

			IDMMenuModel menu = new IDMMenuModel();
			menu.setCode(menuCode);
			menuList.add(menu);
		}

		return menuList;
	}

	private CorporateUserGroupDetailModel createNewDataCorporateUserGroupDetail(
			CorporateUserGroupModel corporateUserGroup, BigDecimal maxAmountLimit, String currencyCode,
			String serviceCode, String currencyMatrixCode, String createdBy) throws Exception {
		CorporateUserGroupDetailModel detail = new CorporateUserGroupDetailModel();

		detail.setServiceCurrencyMatrix(serviceCurrencyMatrixRepo.findByServiceCode(ApplicationConstants.APP_GPCASHIB, serviceCode, currencyMatrixCode));

		detail.setCorporateUserGroup(corporateUserGroup);

		detail.setAmountLimitUsage(BigDecimal.ZERO);
		detail.setMaxAmountLimit(maxAmountLimit);

		CurrencyModel currency = new CurrencyModel();
		currency.setCode(currencyCode);
		detail.setCurrency(currency);

		ServiceModel service = new ServiceModel();
		service.setCode(serviceCode);
		detail.setService(service);

		detail.setCreatedBy(createdBy);
		detail.setCreatedDate(DateUtils.getCurrentTimestamp());

		return detail;
	}

	private void checkUniqueRecord(String corporateId, String code) throws Exception {
		CorporateUserGroupModel model = corporateUserGroupRepo.findByCorporateIdAndCode(corporateId, code);

		if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag()))
			throw new BusinessException("GPT-0100004");
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateAdminPendingTaskVO approve(CorporateAdminPendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			checkCustomValidation(map);

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				CorporateUserGroupModel corporateUserGroupExisting = corporateUserGroupRepo.findByCorporateIdAndCode(
						(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE));

				// cek jika record replacement
				if (corporateUserGroupExisting != null	&& ApplicationConstants.YES.equals(corporateUserGroupExisting.getDeleteFlag())) {
					setMapToModel(corporateUserGroupExisting, map, true, vo.getCreatedBy());

					IDMRoleModel role = saveRoleMenu((ArrayList<Map<String, Object>>) map.get("menuList"),
							corporateUserGroupExisting.getName(), vo.getCreatedBy());

					corporateUserGroupExisting.setRole(role);
					saveCorporateUserGroup(corporateUserGroupExisting, vo.getCreatedBy());
				} else {
					CorporateUserGroupModel corporateUserGroup = new CorporateUserGroupModel();
					setMapToModel(corporateUserGroup, map, true, vo.getCreatedBy());

					IDMRoleModel role = saveRoleMenu((ArrayList<Map<String, Object>>) map.get("menuList"),
							corporateUserGroup.getName(), vo.getCreatedBy());

					corporateUserGroup.setRole(role);
					saveCorporateUserGroup(corporateUserGroup, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				CorporateUserGroupModel corporateUserGroupExisting = getExistingRecord(
						(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE), true);

				setMapToModel(corporateUserGroupExisting, map, false, vo.getCreatedBy());
				
				//delete old role menu and add new role menu
				updateRoleMenu((ArrayList<Map<String, Object>>) map.get("menuList"),
						corporateUserGroupExisting, vo.getCreatedBy());

				updateCorporateUserGroup(corporateUserGroupExisting, vo.getCreatedBy());
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				CorporateUserGroupModel corporateUserGroup = getExistingRecord(
						(String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) map.get(ApplicationConstants.STR_CODE), true);

				deleteCorporateUserGroup(corporateUserGroup, vo.getCreatedBy());
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	@Override
	public CorporateAdminPendingTaskVO reject(CorporateAdminPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	public IDMRoleModel saveRoleMenu(List<Map<String, Object>> menuListMap, String roleName, String createdBy)
			throws ApplicationException, BusinessException {
		List<IDMMenuModel> menuList = getParentMenuModel(menuListMap);

		// save role
		String roleCode = UUID.randomUUID().toString().toUpperCase();
		IDMRoleModel idmRole = new IDMRoleModel();
		idmRole.setCode(roleCode);
		idmRole.setName(roleName);

		IDMApplicationModel idmApp = new IDMApplicationModel();
		idmApp.setCode(ApplicationConstants.APP_GPCASHIB);
		idmRole.setApplication(idmApp);

		// save role
		roleService.saveRole(idmRole, createdBy);

		// save role menu
		roleMenuService.saveRoleMenu(idmRole, menuList, createdBy);

		return idmRole;
	}
	
	public IDMRoleModel updateRoleMenu(List<Map<String, Object>> menuListMap, CorporateUserGroupModel corporateUserGroup, String createdBy)
			throws Exception {
		List<IDMMenuModel> menuList = getParentMenuModel(menuListMap);

		IDMRoleModel oldRole = corporateUserGroup.getRole();
		
		//delete old role menu
		//perlu new propagation jika tidak kena error ORA-00001: unique constraint (IDM_ROLE_MENU_UNIQUE_INDEX) violated
		roleMenuService.deleteRoleMenuNewTrx(oldRole);
		
		// save new role menu
		roleMenuService.saveRoleMenu(oldRole, menuList, createdBy);

		return oldRole;
	}

	@Override
	public void saveCorporateUserGroup(CorporateUserGroupModel corporateUserGroup, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		boolean isNew = corporateUserGroup.getDeleteFlag() == null;
		corporateUserGroup.setDeleteFlag(ApplicationConstants.NO);
		corporateUserGroup.setCreatedDate(DateUtils.getCurrentTimestamp());
		corporateUserGroup.setCreatedBy(createdBy);
		corporateUserGroup.setUpdatedDate(null);
		corporateUserGroup.setUpdatedBy(null);

		if (isNew)
			corporateUserGroupRepo.persist(corporateUserGroup);
		else
			corporateUserGroupRepo.save(corporateUserGroup);
	}

	@Override
	public void updateCorporateUserGroup(CorporateUserGroupModel corporateUserGroup, String updatedBy)
			throws ApplicationException, BusinessException {
		corporateUserGroup.setUpdatedDate(DateUtils.getCurrentTimestamp());
		corporateUserGroup.setUpdatedBy(updatedBy);
		corporateUserGroupRepo.save(corporateUserGroup);
	}

	@Override
	public void deleteCorporateUserGroup(CorporateUserGroupModel corporateUserGroup, String deletedBy)
			throws ApplicationException, BusinessException {
		String roleCode = corporateUserGroup.getRole().getCode();

		// delete role menu
		roleMenuRepo.deleteByRoleCode(roleCode);

		// delete role
		roleService.deleteRole(corporateUserGroup.getRole(), deletedBy);

		// delete corporate user group detail
		corporateUserGroupRepo.deleteDetailByUserGroupId(corporateUserGroup.getId());

		corporateUserGroup.setDeleteFlag(ApplicationConstants.YES);
		corporateUserGroup.setUpdatedDate(DateUtils.getCurrentTimestamp());
		corporateUserGroup.setUpdatedBy(deletedBy);
		corporateUserGroupRepo.save(corporateUserGroup);
	}

	@Override
	public CorporateUserGroupModel saveCorporateUserGroupAdmin(CorporateModel corporate, String roleCode,
			String createdBy) throws ApplicationException, BusinessException {

		CorporateUserGroupModel corpUserGroup = new CorporateUserGroupModel();
		IDMRoleModel role = new IDMRoleModel();
		role.setCode(roleCode);
		corpUserGroup.setRole(role);
		corpUserGroup.setDeleteFlag(ApplicationConstants.NO);
		corpUserGroup.setCreatedBy(createdBy);
		corpUserGroup.setCreatedDate(DateUtils.getCurrentTimestamp());

		corpUserGroup.setCorporate(corporate);

		corporateUserGroupRepo.save(corpUserGroup);

		return corpUserGroup;
	}

	@Override
	public Map<String, Object> searchCorporateMenu(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		String corporateId = (String) map.get(ApplicationConstants.CORP_ID);

		CorporateModel corporate = corporateRepo.findOne(corporateId);
		
		if(corporate == null)
			throw new BusinessException("GPT-0100054");
		
		if(ApplicationConstants.YES.equals(corporate.getDeleteFlag()))
			throw new BusinessException("GPT-0100054");
		
		String roleCode = corporate.getServicePackage().getMenuPackage().getRole().getCode();
		
		resultMap.put("result", roleMenuService.searchRoleMenuGetMap(roleCode, true));
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchCorporateAccountGroup(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		resultMap.put("result", corporateAccountGroupService.searchByCorporateId((String) map.get(ApplicationConstants.CORP_ID), false));
		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchUserGroupByCorporateId(String corporateId)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<CorporateUserGroupModel> userGroupList = corporateUtilsRepo.getCorporateUserGroupRepo()
					.findByRoleTypeCodeAndApplicationCode(corporateId, ApplicationConstants.ROLE_TYPE_AP,
							ApplicationConstants.APP_GPCASHIB);

			List<Map<String, Object>> userGroupResult = new ArrayList<>();
			for (CorporateUserGroupModel model : userGroupList) {
				Map<String, Object> modelMap = new HashMap<>();
				modelMap.put("userGroupCode", model.getCode());
				modelMap.put("userGroupName", model.getName());
				
				CorporateAccountGroupModel accountGroup = model.getCorporateAccountGroup();
				modelMap.put("accountGroupCode", accountGroup.getCode());
				modelMap.put("accountGroupName", accountGroup.getName());
				
				userGroupResult.add(modelMap);
			}
			resultMap.put("result", userGroupResult);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> findDetailByUserGroupId(String corporateId, String userCode)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);
			
			String userGroupId = corporateUser.getCorporateUserGroup().getId();
			
			List<CorporateUserGroupDetailModel> userGroupDetailList = corporateUtilsRepo.getCorporateUserGroupRepo()
					.findDetailByUserGroupId(corporateId, userGroupId);

			List<Map<String, Object>> userGroupDetailResult = new ArrayList<>();
			for (CorporateUserGroupDetailModel model : userGroupDetailList) {
				Map<String, Object> modelMap = new HashMap<>();
				
				ServiceModel service = model.getService();
				CurrencyModel currency = model.getCurrency();
				
				modelMap.put("serviceName", service.getName());
				modelMap.put("currencyCode", currency.getCode());
				modelMap.put("currencyName", currency.getName());
				modelMap.put("maxAmountLimit", model.getMaxAmountLimit());
				
				BigDecimal totalPendingTaskDebitedAmount = new BigDecimal(0);
				if(corporateUserGroupListener != null) {
					Map<String, Object> trxInfoMap = corporateUserGroupListener.getTrxInfoFromPendingTask(service.getCode(), DateUtils.getCurrentTimestamp(), ApplicationConstants.SI_IMMEDIATE, 
							userGroupId);
					totalPendingTaskDebitedAmount = (BigDecimal) trxInfoMap.get("totalPendingTaskDebitedAmount");
				}
				modelMap.put("amountLimitUsage", totalPendingTaskDebitedAmount.add((model.getAmountLimitUsage() != null ? model.getAmountLimitUsage() : BigDecimal.ZERO)));
				userGroupDetailResult.add(modelMap);
			}
			resultMap.put("result", userGroupDetailResult);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public void resetLimit(String parameter) throws ApplicationException, BusinessException {
		try{
			corporateUserGroupRepo.resetLimit();
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void deleteALLCorporateMenuByMenuList(String roleCode, List<String> newMenuList) throws Exception {
		//find corporate by menuPackage.roleCode
		List<String> corporateList = corporateRepo.findCorporateByRoleCode(roleCode);

		
		//find delete menu list
		List<String> deleteMenuList = new ArrayList<>();
		List<IDMRoleMenuModel> menuByRoleCodeExisting = idmRepo.getRoleMenuRepo().searchRoleMenu(roleCode);
		for(IDMRoleMenuModel roleMenu : menuByRoleCodeExisting) {
			IDMMenuModel menuExisting = roleMenu.getMenu();
			if(menuExisting != null) {
				String oldMenu = menuExisting.getCode();
				if(!newMenuList.contains(oldMenu)) {
					deleteMenuList.add(oldMenu);
				}
			}
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("roleCode : " + roleCode);
			logger.debug("deleteMenuList : " + deleteMenuList);
		}
		
		if(deleteMenuList.size() > 0) {
			//iterate each corporate
			for(String corporateId : corporateList) {
				//find corporateUserGroup.roleCode list from each corporate
				List<String> roleList = corporateUserGroupRepo.findRoleCodeByCorporateId(corporateId);
				
				if(logger.isDebugEnabled()) {
					logger.debug("corporateId : " + corporateId);
				}
				
				//request by Susi and Diaz (11 Sept 2017)
				//delete all by role code and menu list
				idmRepo.getMenuRepo().deleteByRoleCodeListAndMenuList(roleList, deleteMenuList);
			}
		}
		
	}
	
	
	@Override
	public Map<String, Object> findMenuForPendingTask(String corporateId, String userCode)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			CorporateUserModel corporateUser = corporateUtilsRepo.isCorporateUserValid(userCode);

			String userGroupRole = corporateUser.getCorporateUserGroup().getRole().getCode();
			
			List<IDMMenuModel> menuList = idmRepo.getMenuRepo().findTransactionAndMaintenanceMenuByRoleCode(userGroupRole);
			
			List<Map<String, Object>> menus = new ArrayList<>();
			for (IDMMenuModel model : menuList) {
				Map<String, Object> modelMap = new HashMap<>();
				modelMap.put("pendingTaskCode", model.getCode());
				modelMap.put("pendingTaskName", model.getName());
				menus.add(modelMap);
			}
			resultMap.put("menus", menus);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
}