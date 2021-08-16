package com.gpt.product.gpcash.corporate.approvalmatrix.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menutype.model.IDMMenuTypeModel;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.approvalmatrix.model.CorporateApprovalMatrixDetailModel;
import com.gpt.product.gpcash.corporate.approvalmatrix.model.CorporateApprovalMatrixMasterModel;
import com.gpt.product.gpcash.corporate.approvalmatrix.model.CorporateApprovalMatrixSubModel;
import com.gpt.product.gpcash.corporate.approvalmatrix.repository.CorporateApprovalMatrixRepository;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateApprovalMatrixServiceImpl implements CorporateApprovalMatrixService {

	@Autowired
	private CorporateApprovalMatrixRepository corporateApprovalMatrixRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private IDMRepository idmRepo;

	@Autowired
	private CorporateAdminPendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			
			CorporateModel corporate = corporateUtilsRepo.isCorporateValid(corporateId);
			
			String roleCode = corporate.getServicePackage().getMenuPackage().getRole().getCode();
			List<IDMMenuModel> menuList = idmRepo.getMenuRepo().findTransactionAndMaintenanceMenuByRoleCode(roleCode);
			List<Map<String, Object>> approvalMatrixListResult = new ArrayList<>();
			
			for(IDMMenuModel menu : menuList) {
				CorporateApprovalMatrixMasterModel master = corporateApprovalMatrixRepo.
						findByCorporateIdAndMenuCode(corporateId, menu.getCode());
				
				if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
					approvalMatrixListResult.add(setModelToMap(master, false, menu));
				} else {
					approvalMatrixListResult.add(setModelToMap(master, true, menu));
				}
			}
			
			resultMap.put("result", approvalMatrixListResult);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private Map<String, Object> setModelToMap(CorporateApprovalMatrixMasterModel model, boolean isGetDetail,
			IDMMenuModel menu) {
		Map<String, Object> map = new HashMap<>();
		
		if(model != null) {
			CurrencyModel currency = model.getCurrency();
			
			if(currency != null){
				map.put("currencyCode", currency.getCode());
				map.put("currencyName", currency.getName());
			} else {
				map.put("currencyCode", ApplicationConstants.EMPTY_STRING);
				map.put("currencyName", ApplicationConstants.EMPTY_STRING);
			}

			if(isGetDetail){
				List<CorporateApprovalMatrixSubModel> subList = model.getCorporateApprovalMatrixSub();
				List<Map<String, Object>> approvalMatrixList = new ArrayList<>();
				for (CorporateApprovalMatrixSubModel sub : subList) {
					Map<String, Object> approvalMatrixMap = new HashMap<>();
					approvalMatrixMap.put("rangeLimit", ValueUtils.getValue(sub.getLongAmountLimit().toPlainString()));
					approvalMatrixMap.put("noOfApproval", String.valueOf(sub.getNoApprover()));

					List<CorporateApprovalMatrixDetailModel> detailList = sub.getCorporateApprovalMatrixDetail();
					List<Map<String, Object>> approvalList = new ArrayList<>();
					for (CorporateApprovalMatrixDetailModel detail : detailList) {
						Map<String, Object> detailMap = new HashMap<>();
						detailMap.put("sequenceNo", String.valueOf(detail.getSequenceNo()));
						detailMap.put("noOfUser", String.valueOf(detail.getNoUser()));

						AuthorizedLimitSchemeModel als = detail.getAuthorizedLimitScheme();
						if(als != null) {
							detailMap.put("authorizedLimitId", als.getId());
						} else {
							detailMap.put("authorizedLimitId", ApplicationConstants.EMPTY_STRING);
						}
						
						detailMap.put("userGroupOptionCode", detail.getUserGroupOption());

						CorporateUserGroupModel userGroup = detail.getCorporateUserGroup();
						if (ApplicationConstants.USER_GROUP_OPTION_SPECIFY_GROUP.equals(detail.getUserGroupOption())) {
							detailMap.put("userGroupCode", userGroup.getCode());
							detailMap.put("userGroupName", userGroup.getName());
						} else {
							detailMap.put("userGroupCode", ApplicationConstants.EMPTY_STRING);
							detailMap.put("userGroupName", ApplicationConstants.EMPTY_STRING);
						}

						approvalList.add(detailMap);
					}

					approvalMatrixMap.put("approvalList", approvalList);
					approvalMatrixList.add(approvalMatrixMap);
				}

				map.put("approvalMatrixListing", approvalMatrixList);
			}
		}
		
		map.put("searchMenuCode", menu.getCode());
		map.put("searchMenuName", menu.getName());
		
		IDMMenuTypeModel menuType = menu.getMenuType();
		
		if(ApplicationConstants.MENU_TYPE_TRANSACTION.equals(menuType.getCode())) {
			map.put("searchMenuType", "Transaction");
		} else {
			map.put("searchMenuType", "Non Transaction");
		}

		
		return map;

	}

	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String searchMenuCode = (String) map.get("searchMenuCode");

		try {
			CorporateAdminPendingTaskVO vo = setCorporateAdminPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);

				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				IDMMenuModel menu = idmRepo.getMenuRepo().findOne(searchMenuCode);
				CorporateApprovalMatrixMasterModel corporateApprovalMatrixOld = getExistingRecord(corporateId,
						searchMenuCode, false);
				vo.setJsonObjectOld(setModelToMap(corporateApprovalMatrixOld, true, menu));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				getExistingRecord(corporateId, searchMenuCode, true);
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
			String searchMenuCode = (String) map.get("searchMenuCode");
			String currencyCode = (String) map.get("currencyCode");

			IDMMenuModel menu = idmRepo.isIDMMenuValid(searchMenuCode);
			String menuType = menu.getMenuType().getCode();

			List<Map<String, Object>> approvalMatrixListing = (ArrayList<Map<String, Object>>) map.get("approvalMatrixListing");
			
			if(approvalMatrixListing.size() == 0) {
				throw new BusinessException("GPT-0100161");
			}
			
			boolean isTransaction = false;
			if(ApplicationConstants.MENU_TYPE_TRANSACTION.equals(menuType)){
				isTransaction = true;
			}

			List<String> rangeLimitList = new ArrayList<>();
			for (Map<String, Object> approvalMatrixMap : approvalMatrixListing) {
				if(isTransaction){
					String rangeLimit = (String) approvalMatrixMap.get("rangeLimit");
					
					if(rangeLimitList.contains(rangeLimit)){
						throw new BusinessException("GPT-0100073");
					}
					rangeLimitList.add(rangeLimit);
				}
				
				int noOfApproval = Integer.parseInt(((String) approvalMatrixMap.get("noOfApproval")));
				List<Map<String, Object>> approvalList = (ArrayList<Map<String, Object>>) approvalMatrixMap.get("approvalList");
				int totalNoUser = 0;
				
				List<Integer> sequenceList = new ArrayList<>();
				for (Map<String, Object> detailMap : approvalList) {
					int sequenceNo = (int) detailMap.get("sequenceNo");
					
					if(sequenceList.contains(sequenceNo)){
						throw new BusinessException("GPT-0100074");
					}
					sequenceList.add(sequenceNo);
					
					int noOfUser = (int) detailMap.get("noOfUser");
					
					if(noOfUser == 0) {
						throw new BusinessException("GPT-0100158");
					}
					
					String authorizedLimitId = (String) detailMap.get("authorizedLimitId");
					if(ValueUtils.hasValue(authorizedLimitId)){
						corporateUtilsRepo.isAuthorizedLimitSchemeValid(corporateId, authorizedLimitId);
					}
					
					String userGroupOptionCode = (String) detailMap.get("userGroupOptionCode");
					String userGroupCode = (String) detailMap.get("userGroupCode");
					
					
					if(ApplicationConstants.USER_GROUP_OPTION_SPECIFY_GROUP.equals(userGroupOptionCode)){
						if(!ValueUtils.hasValue(userGroupCode)){
							throw new BusinessException("GPT-0100075");
						}
						
						corporateUtilsRepo.isCorporateUserGroupValid(corporateId, userGroupCode);
					} else {
						if(ValueUtils.hasValue(userGroupCode)){
							throw new BusinessException("GPT-0100076");
						}
					}
					
					totalNoUser = totalNoUser + noOfUser;
				}
				
				if(noOfApproval != totalNoUser){
					throw new BusinessException("GPT-0100072");
				}
			}
			
			if(isTransaction){
				maintenanceRepo.isCurrencyValid(currencyCode);
			}

			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private CorporateApprovalMatrixMasterModel getExistingRecord(String corporateId, String menuCode, boolean isThrowError) throws Exception {
		CorporateApprovalMatrixMasterModel model = corporateApprovalMatrixRepo
				.findByCorporateIdAndMenuCode(corporateId, menuCode);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		}

		return model;
	}

	@Override
	public List<CorporateApprovalMatrixDetailModel> getApprovalMatrixDetailForWorkflow(String menuCode, String corporateId)
			throws ApplicationException, BusinessException {
		try {
			List<CorporateApprovalMatrixDetailModel> result = corporateApprovalMatrixRepo.findDetailByMenuCodeAndCorporateId(menuCode, corporateId);

			return result;

		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private CorporateAdminPendingTaskVO setCorporateAdminPendingTaskVO(Map<String, Object> map) {
		CorporateAdminPendingTaskVO vo = new CorporateAdminPendingTaskVO();
		vo.setUniqueKey((String) map.get("searchMenuCode"));
		vo.setUniqueKeyDisplay((String) map.get("searchMenuName"));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CorporateApprovalMatrixSC");
		vo.setCorporateId((String) map.get(ApplicationConstants.LOGIN_CORP_ID));

		return vo;
	}

	private void checkUniqueRecord(String corporateId, String menuCode) throws Exception {
		CorporateApprovalMatrixMasterModel model = corporateApprovalMatrixRepo.findByCorporateIdAndMenuCode(corporateId, menuCode);

		if (model != null) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateAdminPendingTaskVO approve(CorporateAdminPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String searchMenuCode = (String) map.get("searchMenuCode");

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				checkCustomValidation(map);
				
				saveApprovalMatrix(map, vo.getCreatedBy());
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				checkCustomValidation(map);
				
				CorporateApprovalMatrixMasterModel approvalMatrix = getExistingRecord(corporateId, searchMenuCode, false);
				
				if(approvalMatrix == null) {
					saveApprovalMatrix(map, vo.getCreatedBy());
				} else {
					corporateApprovalMatrixRepo.delete(approvalMatrix);

					saveApprovalMatrix(map, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				CorporateApprovalMatrixMasterModel approvalMatrix = getExistingRecord(corporateId, searchMenuCode, true);

				corporateApprovalMatrixRepo.delete(approvalMatrix);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	@SuppressWarnings("unchecked")
	private void saveApprovalMatrix(Map<String, Object> map, String createdBy) throws Exception {
		String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String searchMenuCode = (String) map.get("searchMenuCode");
		String currencyCode = (String) map.get("currencyCode");

		CorporateApprovalMatrixMasterModel master = new CorporateApprovalMatrixMasterModel();

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		master.setCorporate(corporate);

		IDMMenuModel menu = idmRepo.isIDMMenuValid(searchMenuCode);
		String menuType = menu.getMenuType().getCode();
		
		boolean isTransaction = false;
		if(ApplicationConstants.MENU_TYPE_TRANSACTION.equals(menuType)){
			isTransaction = true;
			
			CurrencyModel currency = new CurrencyModel();
			currency.setCode(currencyCode);
			master.setCurrency(currency);
		}

		master.setMenu(menu);

		master.setCreatedBy(createdBy);
		master.setCreatedDate(DateUtils.getCurrentTimestamp());

		List<Map<String, Object>> approvalMatrixListing = (ArrayList<Map<String, Object>>) map.get("approvalMatrixListing");

		
		List<CorporateApprovalMatrixSubModel> subList = new ArrayList<>();
		for (Map<String, Object> approvalMatrixMap : approvalMatrixListing) {
			int noOfApproval = Integer.parseInt(((String) approvalMatrixMap.get("noOfApproval")));

			CorporateApprovalMatrixSubModel sub = new CorporateApprovalMatrixSubModel();
			sub.setNoApprover(noOfApproval);
			sub.setCreatedBy(createdBy);
			sub.setCreatedDate(DateUtils.getCurrentTimestamp());

			if(isTransaction){
				String rangeLimitStr = (String) approvalMatrixMap.get("rangeLimit");
				BigDecimal rangeLimit = new BigDecimal(rangeLimitStr);
				sub.setLongAmountLimit(rangeLimit);
			} else {
				sub.setLongAmountLimit(new BigDecimal(ApplicationConstants.APPROVAL_MATRIX_NON_TRANSACTION));
			}
			
			List<Map<String, Object>> approvalList = (ArrayList<Map<String,Object>>) approvalMatrixMap.get("approvalList");

			List<CorporateApprovalMatrixDetailModel> detailList = new ArrayList<>();
			for (Map<String, Object> detailMap : approvalList) {
				int sequenceNo = (int) detailMap.get("sequenceNo");
				int noOfUser = (int) detailMap.get("noOfUser");
				String authorizedLimitId = (String) detailMap.get("authorizedLimitId");
				String userGroupOptionCode = (String) detailMap.get("userGroupOptionCode");
				String userGroupCode = (String) detailMap.get("userGroupCode");

				CorporateApprovalMatrixDetailModel detail = new CorporateApprovalMatrixDetailModel();
				detail.setNoUser(noOfUser);
				detail.setSequenceNo(sequenceNo);
				detail.setUserGroupOption(userGroupOptionCode);

				if(ValueUtils.hasValue(authorizedLimitId)){
					AuthorizedLimitSchemeModel als = new AuthorizedLimitSchemeModel();
					als.setId(authorizedLimitId);
					detail.setAuthorizedLimitScheme(als);
				}
				

				if (userGroupCode != null
						&& ApplicationConstants.USER_GROUP_OPTION_SPECIFY_GROUP.equals(userGroupOptionCode)) {
					CorporateUserGroupModel userGroup = corporateUtilsRepo.isCorporateUserGroupValid(corporateId, userGroupCode);
					detail.setCorporateUserGroup(userGroup);
				}

				detail.setCorporateApprovalMatrixSub(sub);

				detail.setCreatedBy(createdBy);
				detail.setCreatedDate(DateUtils.getCurrentTimestamp());

				detailList.add(detail);
			}

			sub.setCorporateApprovalMatrixMaster(master);
			sub.setCorporateApprovalMatrixDetail(detailList);
			subList.add(sub);
		}

		master.setCorporateApprovalMatrixSub(subList);
		corporateApprovalMatrixRepo.save(master);
	}

	@Override
	public CorporateAdminPendingTaskVO reject(CorporateAdminPendingTaskVO vo)
			throws ApplicationException, BusinessException {

		// if any spesific business function for reject, applied here

		return vo;
	}

	@Override
	public Map<String, Object> getCurrency() throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			
			String localCurrency = maintenanceRepo.isSysParamValid(SysParamConstants.LOCAL_CURRENCY_CODE).getValue();
			
			//add to list for future use when implement forex
			List<Map<String, Object>> currencyList = new ArrayList<>();
			Map<String, Object> currencyMap = new HashMap<>();
			currencyMap.put("code", localCurrency);
			currencyList.add(currencyMap);
			resultMap.put("currencyList", currencyList);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
}