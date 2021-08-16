package com.gpt.product.gpcash.corporate.authorizedlimitscheme.services;

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
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.approvallevel.model.ApprovalLevelModel;
import com.gpt.product.gpcash.approvallevel.repository.ApprovalLevelRepository;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.repository.AuthorizedLimitSchemeRepository;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class AuthorizedLimitSchemeServiceImpl implements AuthorizedLimitSchemeService {

	@Autowired
	private AuthorizedLimitSchemeRepository authorizedLimitSchemeRepo;

	@Autowired
	private CorporateAdminPendingTaskService pendingTaskService;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ApprovalLevelRepository approvalLevelRepo;
	
	@Autowired
	private CorporateUserRepository corporateUserRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			
			List<ApprovalLevelModel> approvalLevelList = approvalLevelRepo.findApprovalLevelAndSort();
			List<Map<String, Object>> approvalLevelListResult = new ArrayList<>();
			for(ApprovalLevelModel approvalLevel : approvalLevelList) {
				AuthorizedLimitSchemeModel authLimit = authorizedLimitSchemeRepo.
						findByCorporateIdAndApprovalLevelCode(corporateId, approvalLevel.getCode());

				if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
					approvalLevelListResult.add(setModelToMap(authLimit, false, approvalLevel));
				} else {
					approvalLevelListResult.add(setModelToMap(authLimit, true, approvalLevel));
				}
			}
			
			resultMap.put("result", approvalLevelListResult);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private Map<String, Object> setModelToMap(AuthorizedLimitSchemeModel model, boolean isGetDetail, ApprovalLevelModel approvalLevel) {
		Map<String, Object> map = new HashMap<>();

		if(model != null) {
			ApprovalLevelModel authLimitApprovalLevel = model.getApprovalLevel();
			map.put(ApplicationConstants.STR_CODE, authLimitApprovalLevel.getCode());
			map.put(ApplicationConstants.STR_NAME, authLimitApprovalLevel.getName());
			map.put("alias", model.getApprovalLevelAlias());

			CurrencyModel currency = model.getLimitCurrency();
			map.put("currencyCode", currency.getCode());
			map.put("currencyName", currency.getName());

			if (isGetDetail) {
				map.put("makerLimit", ValueUtils.getValue(model.getMakerLimit()));
				map.put("singleApprovalLimit", ValueUtils.getValue(model.getSingleApprovalLimit()));
				map.put("intraGroupLimit", ValueUtils.getValue(model.getIntraGroupApprovalLimit()));
				map.put("crossGroupLimit", ValueUtils.getValue(model.getCrossGroupApprovalLimit()));

				map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
				map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
				map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
				map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
			}
		} else {
			map.put(ApplicationConstants.STR_CODE, approvalLevel.getCode());
			map.put(ApplicationConstants.STR_NAME, approvalLevel.getName());
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

			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String approvalLevelCode = (String) map.get("approvalLevelCode");

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);
				
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				AuthorizedLimitSchemeModel authorizedLimitSchemeOld = getExistingRecord(corporateId, approvalLevelCode,
						false);
				
				ApprovalLevelModel approvalLevel = approvalLevelRepo.findOne(approvalLevelCode);
				
				vo.setJsonObjectOld(setModelToMap(authorizedLimitSchemeOld, true, approvalLevel));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				AuthorizedLimitSchemeModel als = getExistingRecord(corporateId, approvalLevelCode, true);
				
				//check ada atau tidak user yg lagi memakai user auth limit ini atau tidak
				List<CorporateUserModel> userList = corporateUserRepo.findCorporateUserByAuthorizedLimitId(als.getId());
				if(userList.size() > 0) {
					throw new BusinessException("GPT-0100163");
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

	private AuthorizedLimitSchemeModel getExistingRecord(String corporateId, String approvalLevelCode,
			boolean isThrowError) throws Exception {
		AuthorizedLimitSchemeModel model = authorizedLimitSchemeRepo
				.findByCorporateIdAndApprovalLevelCode(corporateId, approvalLevelCode);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		}

		return model;
	}

	private CorporateAdminPendingTaskVO setCorporateAdminPendingTaskVO(Map<String, Object> map) {
		CorporateAdminPendingTaskVO vo = new CorporateAdminPendingTaskVO();
		vo.setUniqueKey((String) map.get("approvalLevelCode"));
		vo.setUniqueKeyDisplay(((String) map.get("approvalLevelName")).concat(ApplicationConstants.DELIMITER_DASH)
				.concat((String) map.get("alias")));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("AuthorizedLimitSchemeSC");
		vo.setCorporateId((String) map.get(ApplicationConstants.LOGIN_CORP_ID));

		return vo;
	}

	private AuthorizedLimitSchemeModel setMapToModel(String corporateId, Map<String, Object> map) {
		AuthorizedLimitSchemeModel authorizedLimitScheme = new AuthorizedLimitSchemeModel();
		
		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		authorizedLimitScheme.setCorporate(corporate);
		
		authorizedLimitScheme.setApprovalLevelAlias((String) map.get("alias"));

		ApprovalLevelModel approvalLevel = new ApprovalLevelModel();
		approvalLevel.setCode((String) map.get("approvalLevelCode"));
		authorizedLimitScheme.setApprovalLevel(approvalLevel);

		CurrencyModel limitCurrency = new CurrencyModel();
		limitCurrency.setCode((String) map.get("currencyCode"));
		authorizedLimitScheme.setLimitCurrency(limitCurrency);

		authorizedLimitScheme.setMakerLimit(new BigDecimal((String) map.get("makerLimit")));
		authorizedLimitScheme.setSingleApprovalLimit(new BigDecimal((String) map.get("singleApprovalLimit")));
		authorizedLimitScheme.setIntraGroupApprovalLimit(new BigDecimal((String) map.get("intraGroupLimit")));
		authorizedLimitScheme.setCrossGroupApprovalLimit(new BigDecimal((String) map.get("crossGroupLimit")));

		return authorizedLimitScheme;
	}

//	private void checkUniqueRecord(String corporateId, String approvalLevelCode) throws Exception {
//		AuthorizedLimitSchemeModel model = authorizedLimitSchemeRepo
//				.findByCorporateIdAndApprovalLevelCode(corporateId, approvalLevelCode);
//
//		if (model != null) {
//			throw new BusinessException("GPT-0100004");
//		}
//	}

	@SuppressWarnings("unchecked")
	@Override
	public CorporateAdminPendingTaskVO approve(CorporateAdminPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String approvalLevelCode = (String) map.get("approvalLevelCode");
			if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				AuthorizedLimitSchemeModel authorizedLimitSchemeExisting = authorizedLimitSchemeRepo
						.findByCorporateIdAndApprovalLevelCode(corporateId, approvalLevelCode);
				
				AuthorizedLimitSchemeModel authorizedLimitSchemeNew = setMapToModel(corporateId, map);
				
				if(authorizedLimitSchemeExisting == null) {
					saveAuthorizedLimitScheme(authorizedLimitSchemeNew, vo.getCreatedBy());
				} else {
					// set value yg boleh di edit untuk update
					authorizedLimitSchemeExisting.setApprovalLevelAlias(authorizedLimitSchemeNew.getApprovalLevelAlias());
					authorizedLimitSchemeExisting.setLimitCurrency(authorizedLimitSchemeNew.getLimitCurrency());
					authorizedLimitSchemeExisting.setMakerLimit(authorizedLimitSchemeNew.getMakerLimit());
					authorizedLimitSchemeExisting.setSingleApprovalLimit(authorizedLimitSchemeNew.getSingleApprovalLimit());
					authorizedLimitSchemeExisting.setIntraGroupApprovalLimit(authorizedLimitSchemeNew.getIntraGroupApprovalLimit());
					authorizedLimitSchemeExisting.setCrossGroupApprovalLimit(authorizedLimitSchemeNew.getCrossGroupApprovalLimit());

					updateAuthorizedLimitScheme(authorizedLimitSchemeExisting, vo.getCreatedBy());
				}
				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				AuthorizedLimitSchemeModel authorizedLimitScheme = getExistingRecord(corporateId, approvalLevelCode,
						true);

				deleteAuthorizedLimitScheme(authorizedLimitScheme, vo.getCreatedBy());
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

	@Override
	public void saveAuthorizedLimitScheme(AuthorizedLimitSchemeModel authorizedLimitScheme, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		authorizedLimitScheme.setCreatedDate(DateUtils.getCurrentTimestamp());
		authorizedLimitScheme.setCreatedBy(createdBy);
		authorizedLimitScheme.setUpdatedDate(null);
		authorizedLimitScheme.setUpdatedBy(null);

		authorizedLimitSchemeRepo.persist(authorizedLimitScheme);
	}

	@Override
	public void updateAuthorizedLimitScheme(AuthorizedLimitSchemeModel authorizedLimitScheme, String updatedBy)
			throws ApplicationException, BusinessException {
		authorizedLimitScheme.setUpdatedDate(DateUtils.getCurrentTimestamp());
		authorizedLimitScheme.setUpdatedBy(updatedBy);
		authorizedLimitSchemeRepo.save(authorizedLimitScheme);
	}

	@Override
	public void deleteAuthorizedLimitScheme(AuthorizedLimitSchemeModel authorizedLimitScheme, String deletedBy)
			throws ApplicationException, BusinessException {
		authorizedLimitSchemeRepo.delete(authorizedLimitScheme);
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try{
			maintenanceRepo.isCurrencyValid((String) map.get("currencyCode"));
			
			productRepo.isApprovalLevelValid((String) map.get("approvalLevelCode"));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@Override
	public Map<String, Object> searchAuthorizedLimitSchemeByCorporateId(String corporateId)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<AuthorizedLimitSchemeModel> authorizedLimitList = authorizedLimitSchemeRepo
					.findByCorporateIdOrderByApprovalLevelCode(corporateId);

			List<Map<String, Object>> authorizedLimitResult = new ArrayList<>();
			for (AuthorizedLimitSchemeModel model : authorizedLimitList) {
				Map<String, Object> modelMap = new HashMap<>();
				modelMap.put("authorizedLimitId", model.getId());
				modelMap.put("approvalLevelAlias", model.getApprovalLevelAlias());
				
				ApprovalLevelModel level = model.getApprovalLevel();
				modelMap.put("approvalLevelCode", level.getCode());
				modelMap.put("approvalLevelName", level.getName());
				authorizedLimitResult.add(modelMap);
			}
			resultMap.put("result", authorizedLimitResult);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
}