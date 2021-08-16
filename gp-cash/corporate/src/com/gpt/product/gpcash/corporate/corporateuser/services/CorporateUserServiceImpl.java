package com.gpt.product.gpcash.corporate.corporateuser.services;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.idm.userapp.services.IDMUserAppService;
import com.gpt.component.idm.userrole.repository.IDMUserRoleRepository;
import com.gpt.component.idm.userrole.services.IDMUserRoleService;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.maintenance.wfrole.model.WorkflowRoleModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.approvalmap.model.ApprovalMapModel;
import com.gpt.product.gpcash.approvalmap.repository.ApprovalMapRepository;
import com.gpt.product.gpcash.corporate.authorizedlimitscheme.model.AuthorizedLimitSchemeModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.services.CorporateUserGroupService;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.valueobject.CorporateAdminPendingTaskVO;
import com.gpt.product.gpcash.corporate.token.tokentype.model.TokenTypeModel;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;
import com.gpt.product.gpcash.corporate.token.tokenuser.services.TokenUserService;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateUserServiceImpl implements CorporateUserService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CorporateUserRepository corporateUserRepo;

	@Autowired
	private CorporateAdminPendingTaskService pendingTaskService;

	@Autowired
	private IDMUserService idmUserService;

	@Autowired
	private IDMUserRoleService idmUserRoleService;

	@Autowired
	private IDMUserAppService idmUserAppService;

	@Autowired
	private IDMUserRepository idmUserRepo;

	@Autowired
	private CorporateUserGroupService corporateUserGroupService;

	@Autowired
	private ApprovalMapRepository approvalMapRepo;

	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;

	@Autowired
	private TokenUserService tokenUserService;
	
	@Autowired
	private IDMUserRoleRepository userRoleRepo;
	
	@Value("${gpcash.avatar.upload.path}")
	private String pathUpload;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
			String userId = Helper.getSearchWildcardValue((String) map.get("userId"));
			String userName = Helper.getSearchWildcardValue((String) map.get("userName"));
			String userGroupCode = Helper.getSearchWildcardValue((String) map.get("userGroupCode"));

			Page<CorporateUserModel> result = corporateUserRepo.findByUserIdNameAndGroupCode(corporateId, userId, userName,
					userGroupCode, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<CorporateUserModel> list, boolean isGetDetail)
			throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CorporateUserModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CorporateUserModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("userId", model.getUserId());

		IDMUserModel idmUser = model.getUser();
		if (idmUser != null) {
			map.put("userName", idmUser.getName());
			map.put("status", idmUser.getStatus());
		}

		CorporateUserGroupModel userGroup = model.getCorporateUserGroup();
		if (userGroup != null) {
			map.put("userGroupCode", userGroup.getCode());
			map.put("userGroupName", userGroup.getName());
			
			CorporateAccountGroupModel accountGroup = userGroup.getCorporateAccountGroup();
			map.put("accountGroupCode", accountGroup.getCode());
			map.put("accountGroupName", accountGroup.getName());
		}
		
		ApprovalMapModel approvalMap = model.getApprovalMap();
		if (approvalMap != null) {
			WorkflowRoleModel wfRole = approvalMap.getWorkflowRole();
			map.put("wfRoleCode", wfRole.getCode());
			map.put("wfRoleName", wfRole.getName());
		}

		AuthorizedLimitSchemeModel authorizedLimitScheme = model.getAuthorizedLimit();
		if (authorizedLimitScheme != null) {
			map.put("authorizedLimitId", authorizedLimitScheme.getId());
			map.put("approvalLevelName", authorizedLimitScheme.getApprovalLevel().getName());
			map.put("authorizedLimitAlias", authorizedLimitScheme.getApprovalLevelAlias());
		}

		if (isGetDetail) {
			if (idmUser != null) {
				map.put("email", ValueUtils.getValue(idmUser.getEmail()));

				TokenUserModel tokenUserModel = corporateUtilsRepo.getTokenUserRepo().findByAssignedUserCode(idmUser.getCode());
				if (tokenUserModel != null) {
					map.put("tokenType", ValueUtils.getValue(tokenUserModel.getTokenType().getCode()));
					map.put("tokenNo", ValueUtils.getValue(tokenUserModel.getTokenNo()));
				}
			}

			map.put("isNotifyMyTask", ValueUtils.getValue(model.getIsNotifyMyTask()));
			map.put("isNotifyMyTrx", ValueUtils.getValue(model.getIsNotifyMyTrx()));
			map.put("mobileNo", ValueUtils.getValue(model.getMobilePhoneNo()));
			map.put("isGrantViewDetail", ValueUtils.getValue(model.getIsGrantViewDetail()));

			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			CorporateAdminPendingTaskVO vo = setCorporateAdminPendingTaskVO(map);
			vo.setJsonObject(map);

			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String userId = (String) map.get("userId");

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				
				String userCode = Helper.getCorporateUserCode(corporateId, userId);
				checkUniqueRecord(userCode);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				String userCode = Helper.getCorporateUserCode(corporateId, userId);
				CorporateUserModel corporateUserOld = getExistingRecord(userCode, true);
				vo.setJsonObjectOld(setModelToMap(corporateUserOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				String userCode = Helper.getCorporateUserCode(corporateId, userId);
				getExistingRecord(userCode, true);
			} else if (map.get(ApplicationConstants.WF_ACTION).equals("DELETE_LIST")) {
				vo.setAction("DELETE_LIST");

				List<Map<String, Object>> userIdList = (ArrayList<Map<String,Object>>)map.get("userIdList");
				for(Map<String, Object> userIdMap : userIdList) {
					String userCode = Helper.getCorporateUserCode(corporateId, (String) userIdMap.get("userId"));
					
					// check existing record exist or not
					getExistingRecord(userCode, true);
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

	private CorporateUserModel getExistingRecord(String userCode, boolean isThrowError) throws BusinessException {
		CorporateUserModel model = corporateUserRepo.findOne(userCode);

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

	@SuppressWarnings("unchecked")
	private CorporateAdminPendingTaskVO setCorporateAdminPendingTaskVO(Map<String, Object> map) throws ApplicationException, BusinessException {
		CorporateAdminPendingTaskVO vo = new CorporateAdminPendingTaskVO();
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERCODE));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CorporateUserSC");
		vo.setCorporateId((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
		
		if(map.get(ApplicationConstants.WF_ACTION).equals("DELETE_LIST")) {
			String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
			
			//check pending task using like for each userId
			List<Map<String, Object>> userIdList = (ArrayList<Map<String,Object>>) map.get("userIdList");
			for(Map<String, Object> userIdMap : userIdList){
				String userCode = Helper.getCorporateUserCode((String) map.get(ApplicationConstants.LOGIN_CORP_ID),
						(String) userIdMap.get("userId"));
				uniqueKeyAppend = uniqueKeyAppend + userCode + ApplicationConstants.DELIMITER_PIPE;
				
				//check unique pending task
				vo.setUniqueKey(userCode);
				pendingTaskService.checkUniquePendingTaskLike(vo);
			}
			
			vo.setUniqueKey(uniqueKeyAppend);
			vo.setUniqueKeyDisplay((String) map.get(ApplicationConstants.LOGIN_CORP_ID));
		} else if(map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
			vo.setUniqueKey(Helper.getCorporateUserCode((String) map.get(ApplicationConstants.LOGIN_CORP_ID),
					(String) map.get("userId")));
			vo.setUniqueKeyDisplay(((String) map.get("userId")).concat(ApplicationConstants.DELIMITER_DASH)
					.concat((String) map.get("userName")));
			
			// check unique pending task
			pendingTaskService.checkUniquePendingTaskLike(vo);
		} else {
			vo.setUniqueKey(Helper.getCorporateUserCode((String) map.get(ApplicationConstants.LOGIN_CORP_ID),
					(String) map.get("userId")));
			vo.setUniqueKeyDisplay(((String) map.get("userId")).concat(ApplicationConstants.DELIMITER_DASH)
					.concat((String) map.get("userName")));
			
			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);
		}

		return vo;
	}

	private void checkUniqueRecord(String userCode) throws Exception {
		CorporateUserModel model = corporateUserRepo.findOne(userCode);

		if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
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
			String userId = (String) map.get("userId");
			String userName = (String) map.get("userName");
			String email = (String) map.get("email");
			String isNotifyMyTask = (String) map.get("isNotifyMyTask");
			String isNotifyMyTrx = (String) map.get("isNotifyMyTrx");
			String mobileNo = (String) map.get("mobileNo");
			String wfRoleCode = (String) map.get("wfRoleCode");
			String authorizedLimitId = (String) map.get("authorizedLimitId");
			String userGroupCode = (String) map.get("userGroupCode");
			String isGrantViewDetail = (String) map.get("isGrantViewDetail");
			String tokenType = (String) map.get("tokenType");
			String tokenNo = (String) map.get("tokenNo");

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				checkCustomValidation(map);

				String userCode = Helper.getCorporateUserCode(corporateId, userId);
				CorporateUserModel corporateUserExisting = corporateUserRepo.findOne(userCode);

				// cek jika record replacement
				if (corporateUserExisting != null
						&& ApplicationConstants.YES.equals(corporateUserExisting.getDeleteFlag())) {
					updateCorporateUser(corporateId, userId, userGroupCode, mobileNo, userName, email,
							isGrantViewDetail, authorizedLimitId, isNotifyMyTask, isNotifyMyTrx, tokenType, tokenNo,
							vo.getCreatedBy(), wfRoleCode);
				} else {
					saveCorporateUserAndAssignToken(corporateId, userId, userGroupCode, mobileNo, userName, email, isGrantViewDetail,
							authorizedLimitId, isNotifyMyTask, isNotifyMyTrx, tokenType, tokenNo, vo.getCreatedBy(),
							wfRoleCode);
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				checkCustomValidation(map);
				
				updateCorporateUser(corporateId, userId, userGroupCode, mobileNo, userName, email, isGrantViewDetail,
						authorizedLimitId, isNotifyMyTask, isNotifyMyTrx, tokenType, tokenNo, vo.getCreatedBy(),
						wfRoleCode);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				String userCode = Helper.getCorporateUserCode(corporateId, userId);
				CorporateUserModel corporateUser = getExistingRecord(userCode, true);
				
				TokenUserModel tokenUserModel = corporateUtilsRepo.getTokenUserRepo().findByAssignedUserCode(userCode);

				//unassign if user has token
				if(tokenUserModel == null) {
					deleteCorporateUser(corporateUser, vo.getCreatedBy());
				} else {
					deleteCorporateUserAndUnassign(corporateUser, vo.getCreatedBy(), tokenUserModel.getTokenNo(), corporateId, userCode);
				}
				
				
			} else if ("DELETE_LIST".equals(vo.getAction())) {
				List<Map<String, Object>> userIdList = (ArrayList<Map<String,Object>>)map.get("userIdList");
				for(Map<String, Object> userIdMap : userIdList) {
					String userCode = Helper.getCorporateUserCode(corporateId, (String) userIdMap.get("userId"));
					
					// check existing record exist or not
					CorporateUserModel corporateUser = getExistingRecord(userCode, true);

					TokenUserModel tokenUserModel = corporateUtilsRepo.getTokenUserRepo().findByAssignedUserCode(userCode);

					//unassign if user has token
					if(tokenUserModel == null) {
						deleteCorporateUser(corporateUser, vo.getCreatedBy());
					} else {
						deleteCorporateUserAndUnassign(corporateUser, vo.getCreatedBy(), tokenUserModel.getTokenNo(), corporateId, userCode);
					}
				}
				
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}

	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			String corporateId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String wfRoleCode = (String) map.get("wfRoleCode");
			String authorizedLimitId = (String) map.get("authorizedLimitId");
			String userGroupCode = (String) map.get("userGroupCode");
			String tokenType = (String) map.get("tokenType");
			String tokenNo = (String) map.get("tokenNo");
			String userId = (String) map.get("userId");

			// check wfRoleCode
			maintenanceRepo.isWorkflowRoleValid(wfRoleCode);

			// check authorized limit
			corporateUtilsRepo.isAuthorizedLimitSchemeValid(corporateId, authorizedLimitId);

			// check user group
			corporateUtilsRepo.isCorporateUserGroupValid(corporateId, userGroupCode);
			
			//check jika maker tidak perlu tokenNo dan token type,
			// sebaliknya yg lain perlu
			if(ApplicationConstants.ROLE_WF_CRP_USR_MAKER.equals(wfRoleCode)){
				if(ValueUtils.hasValue(tokenNo)){
					throw new BusinessException("GPT-0100066");
				}
				
				if(ValueUtils.hasValue(tokenType)){
					throw new BusinessException("GPT-0100067");
				}
			} else {
				if(!ValueUtils.hasValue(tokenNo)){
					throw new BusinessException("GPT-0100068");
				}
				
				if(!ValueUtils.hasValue(tokenType)){
					throw new BusinessException("GPT-0100069");
				}
			}
			
			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				//check maximum user
				CorporateModel corp = corporateUtilsRepo.getCorporateRepo().findOne(corporateId);
				
				//select active user from corporate
				List<CorporateUserModel> corporateUserList = corporateUtilsRepo.getCorporateUserRepo().
						findByCorporateIdAndDeleteFlagExcludeAdmin(corporateId, ApplicationConstants.NO);
				
				if((corporateUserList.size() + 1) > corp.getMaxCorporateUser()) {
					throw new BusinessException("GPT-0100183");
				}
				
				//hanya cek jika user nya adalah bukan maker
				if(!ApplicationConstants.ROLE_WF_CRP_USR_MAKER.equals(wfRoleCode)){
					//check if deviceNo has been used by another user
					corporateUtilsRepo.isTokenNoAlreadyAssigned(corporateId, tokenNo);
				}
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				TokenUserModel tokenUser = tokenUserService.findByAssignedUserCode(Helper.getCorporateUserCode(corporateId, userId), false);
				
				//hanya cek jika ada ganti tokenNo dan sebelum nya blm ada token
				if(tokenUser != null) {
					String existingToken = tokenUser.getTokenNo();
					if(!existingToken.equals(tokenNo)) {
						if (logger.isDebugEnabled()) {
							logger.debug("\n\n\n\n\n\n");
							logger.debug("existingToken = " + existingToken);
							logger.debug("tokenNo = " + tokenNo);
							logger.debug("\n\n\n\n\n");
						}
						
						//hanya cek jika user nya adalah bukan maker
						if(!ApplicationConstants.ROLE_WF_CRP_USR_MAKER.equals(wfRoleCode)){
							//check if deviceNo has been used by another user
							corporateUtilsRepo.isTokenNoAlreadyAssigned(corporateId, tokenNo);
						}
					}
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public CorporateAdminPendingTaskVO reject(CorporateAdminPendingTaskVO vo)
			throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

	@Override
	public void saveCorporateUser(CorporateUserModel corporateUser, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		corporateUser.setDeleteFlag(ApplicationConstants.NO);
		corporateUser.setCreatedDate(DateUtils.getCurrentTimestamp());
		corporateUser.setCreatedBy(createdBy);
		corporateUser.setUpdatedDate(null);
		corporateUser.setUpdatedBy(null);

		corporateUserRepo.save(corporateUser);
	}

	private void updateCorporateUser(CorporateUserModel corporateUser, String updatedBy, String newTokenNo
			, IDMUserModel idmUser) throws Exception {
		
		corporateUser.setUpdatedDate(DateUtils.getCurrentTimestamp());
		corporateUser.setUpdatedBy(updatedBy);
		corporateUserRepo.save(corporateUser);
		
		//update token if changed and reassign token
		String oldTokenNo = ApplicationConstants.EMPTY_STRING;
		
		TokenUserModel tokenUserModel = corporateUtilsRepo.getTokenUserRepo().findByAssignedUserCode(idmUser.getCode());
		
		//user sebelum nya ada token
		if(tokenUserModel != null) {
			oldTokenNo = tokenUserModel.getTokenNo();
		}
		
		boolean unassigned = false;
		boolean assigned = false;
		
		 
		if(!oldTokenNo.equals(newTokenNo)) {
			//reassign token jika token berubah
			
			CorporateModel corporate = corporateUser.getCorporate();
			
			//jika sebelum nya telah memiliki token maka perlu di unassign dahulu
			if(!oldTokenNo.equals(ApplicationConstants.EMPTY_STRING)) {
				unassigned = true;
			}
			
			//jika user adalah approver / releaser maka perlu di assign token
			if(ValueUtils.hasValue(newTokenNo)) {
				assigned = true;
				
			} else {
				//jika user telah berubah menjadi maker dr approver / releaser maka perlu unassign token sebelum nya
				unassigned = true;
			}
			
			if(unassigned) {
				tokenUserService.unassignToken(idmUser.getCode(), oldTokenNo, corporate.getId());
			}
			
			if(assigned) {
				tokenUserService.assignToken(idmUser.getCode(), newTokenNo, corporate.getId(), updatedBy);
			}
		}
	}

	@Override
	public void deleteCorporateUser(CorporateUserModel corporateUser, String deletedBy)
			throws ApplicationException, BusinessException {
		corporateUser.setDeleteFlag(ApplicationConstants.YES);
		corporateUser.setUpdatedDate(DateUtils.getCurrentTimestamp());
		corporateUser.setUpdatedBy(deletedBy);

		corporateUserRepo.save(corporateUser);

		IDMUserModel idmUser = corporateUser.getUser();
		idmUser.setDeleteFlag(ApplicationConstants.YES);
		idmUser.setUpdatedDate(DateUtils.getCurrentTimestamp());
		idmUser.setUpdatedBy(deletedBy);

		idmUserRepo.save(idmUser);
	}
	
	private void deleteCorporateUserAndUnassign(CorporateUserModel corporateUser, String deletedBy, String tokenNo, String corporateId, String userCode)
			throws ApplicationException, BusinessException {
		deleteCorporateUser(corporateUser, deletedBy);
		
		//unassignToken
		tokenUserService.unassignToken(userCode, tokenNo, corporateId);
	}

	@Override
	public void saveCorporateUser(String userId, String mobileNo, String phoneNo, String isGrantViewDetail,
			ApprovalMapModel approvalMap, CorporateModel corporate, CorporateUserGroupModel corpUsrGroup,
			IDMUserModel idmUser, String createdBy) throws ApplicationException, BusinessException {

		String userCode = Helper.getCorporateUserCode(corporate.getId(), userId);

		CorporateUserModel corpUser = new CorporateUserModel();
		corpUser.setId(userCode);
		corpUser.setIsGrantViewDetail(isGrantViewDetail);
		corpUser.setMobilePhoneNo(mobileNo);
		corpUser.setUserId(userId);
		corpUser.setApprovalMap(approvalMap);
		corpUser.setCorporate(corporate);
		corpUser.setCorporateUserGroup(corpUsrGroup);
		corpUser.setUser(idmUser);

		saveCorporateUser(corpUser, createdBy);
	}

	@Override
	public void saveCorporateUser(String userId, String mobileNo, String isGrantViewDetail,
			ApprovalMapModel approvalMap, CorporateModel corporate, CorporateUserGroupModel corpUsrGroup,
			IDMUserModel idmUser, String createdBy, String authorizedLimitId, String isNotifyMyTask,
			String isNotifyMyTrx) throws ApplicationException, BusinessException {

		String userCode = Helper.getCorporateUserCode(corporate.getId(), userId);

		CorporateUserModel corpUser = new CorporateUserModel();
		corpUser.setId(userCode);
		corpUser.setIsGrantViewDetail(isGrantViewDetail);
		corpUser.setMobilePhoneNo(mobileNo);
		corpUser.setUserId(userId);
		corpUser.setApprovalMap(approvalMap);
		corpUser.setCorporate(corporate);
		corpUser.setCorporateUserGroup(corpUsrGroup);
		corpUser.setUser(idmUser);
		corpUser.setIsNotifyMyTask(isNotifyMyTask);
		corpUser.setIsNotifyMyTrx(isNotifyMyTrx);

		AuthorizedLimitSchemeModel authorizedLimit = new AuthorizedLimitSchemeModel();
		authorizedLimit.setId(authorizedLimitId);
		corpUser.setAuthorizedLimit(authorizedLimit);

		saveCorporateUser(corpUser, createdBy);
	}

	@Override
	public void saveCorporateUserAndAssignToken(String corporateId, String userId, String userGroupCode, String mobileNo,
			String userName, String email, String isGrantViewDetail, String authorizedLimitId, String isNotifyMyTask,
			String isNotifyMyTrx, String tokenType, String tokenNo, String createdBy, String roleCodeWF)
			throws Exception {

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);

		// get corporate user group
		CorporateUserGroupModel corpUsrGroup = corporateUtilsRepo.getCorporateUserGroupRepo()
				.findByCorporateIdAndCode(corporateId, userGroupCode);
		
		// generate user id for corporate
		String userCode = Helper.getCorporateUserCode(corporate.getId(), userId);

		// validation user exist
		IDMUserModel idmUser = idmUserRepo.findOne(userCode);
		if (idmUser != null) {
			throw new BusinessException("GPT-0100050", new String[] { userCode });
		}

		// create idm user
		idmUser = idmUserService.saveIDMUser(userCode, userId, userName, email, createdBy);

		//hanya jika bukan maker maka assignToken
		if(!ApplicationConstants.ROLE_WF_CRP_USR_MAKER.equals(roleCodeWF)) {
			//assignToken
			tokenUserService.assignToken(userCode, tokenNo, corporateId, createdBy);
		}

		AuthorizedLimitSchemeModel authorizedLimit = corporateUtilsRepo.getAuthorizedLimitSchemeRepo()
				.findOne(authorizedLimitId);

		// get approval map
		List<ApprovalMapModel> approvalMapList = approvalMapRepo.findByWorkflowRoleCodeAndApprovalLevelCode(roleCodeWF,
				authorizedLimit.getApprovalLevel().getCode());
		ApprovalMapModel approvalMap = approvalMapList.get(0);

		// create corporate user for admin user
		saveCorporateUser(userId, mobileNo, isGrantViewDetail, approvalMap, corporate, corpUsrGroup, idmUser, createdBy,
				authorizedLimitId, isNotifyMyTask, isNotifyMyTrx);

		// create user role AP
		String roleCodeAP = corpUsrGroup.getRole().getCode();
		idmUserRoleService.saveUserRole(idmUser, roleCodeAP, createdBy);

		// create user role WF
		roleCodeWF = roleCodeWF.concat(authorizedLimit.getApprovalLevel().getCode());
		idmUserRoleService.saveUserRole(idmUser, roleCodeWF, createdBy);

		// add user application
		idmUserAppService.saveUserApplication(idmUser, ApplicationConstants.APP_GPCASHIB, createdBy);

	}

	@Override
	public void saveCorporateAdminUser(CorporateModel corporate, String userId, String userName, String mobileNo,
			String email, String createdBy, String wfRoleCode) throws Exception {
		// insert corporate user group dengan role CRPADM
		CorporateUserGroupModel corpUsrGroup = corporateUserGroupService.saveCorporateUserGroupAdmin(corporate,
				ApplicationConstants.ROLE_AP_ADMIN, createdBy);

		String userCode = Helper.getCorporateUserCode(corporate.getId(), userId);

		// validation user exist
		IDMUserModel idmUser = idmUserRepo.findOne(userCode);
		if (idmUser != null) {
			throw new BusinessException("GPT-0100050", new String[] { userId });
		}

		// create idm user
		idmUser = idmUserService.saveIDMUser(userCode, userId, userName, email, createdBy);

		// get approval map
		List<ApprovalMapModel> approvalMapList = approvalMapRepo.findByWorkflowRoleCodeAndApprovalLevelCode(wfRoleCode,
				"1");
		ApprovalMapModel approvalMap = approvalMapList.get(0);

		// create corporate user for admin user
		saveCorporateUser(userId, mobileNo, mobileNo, ApplicationConstants.NO, approvalMap, corporate, corpUsrGroup,
				idmUser, createdBy);

		// create user role AP for CRPADM
		idmUserRoleService.saveUserRole(idmUser, ApplicationConstants.ROLE_AP_ADMIN, createdBy);

		// create user role WF for CRP_ADM_MAKER or CRP_ADM_CHECKER
		idmUserRoleService.saveUserRole(idmUser, wfRoleCode, createdBy);

		// add user application
		idmUserAppService.saveUserApplication(idmUser, ApplicationConstants.APP_GPCASHIB_ADMIN, createdBy);

	}

	@Override
	public void updateCorporateAdminUser(CorporateModel corporate, String userId, String userName, String mobileNo,
			String email, String tokenNo, String updatedBy) throws Exception {
		String userCode = Helper.getCorporateUserCode(corporate.getId(), userId);
		IDMUserModel idmUser = idmUserRepo.findOne(userCode);

		if (idmUser == null) {
			throw new BusinessException("GPT-0100009", new String[] { userId });
		}

		idmUser.setDeleteFlag(ApplicationConstants.NO);
		idmUserService.updateIDMUser(idmUser, userName, email, updatedBy);

		// update corporate user for admin user
		CorporateUserModel corporateUser = corporateUserRepo.findOne(userCode);
		corporateUser.setMobilePhoneNo(mobileNo);
		corporateUser.setDeleteFlag(ApplicationConstants.NO);
		updateCorporateUser(corporateUser, updatedBy, tokenNo, idmUser);
	}

	@Override
	public void updateCorporateUser(String corporateId, String userId, String userGroupCode, String mobileNo,
			String userName, String email, String isGrantViewDetail, String authorizedLimitId, String isNotifyMyTask,
			String isNotifyMyTrx, String tokenType, String tokenNo, String updatedBy, String roleCodeWF)
			throws Exception {
		String userCode = Helper.getCorporateUserCode(corporateId, userId);
		IDMUserModel idmUser = idmUserRepo.findOne(userCode);

		if (idmUser == null) {
			throw new BusinessException("GPT-0100009", new String[] { userId });
		}

		idmUser.setDeleteFlag(ApplicationConstants.NO);
		idmUserService.updateIDMUser(idmUser, userName, email, updatedBy);

		// update corporate user for admin user
		CorporateUserModel corporateUser = corporateUserRepo.findOne(userCode);
		corporateUser.setDeleteFlag(ApplicationConstants.NO);
		corporateUser.setMobilePhoneNo(mobileNo);
		corporateUser.setIsGrantViewDetail(isGrantViewDetail);
		corporateUser.setIsNotifyMyTask(isNotifyMyTask);
		corporateUser.setIsNotifyMyTrx(isNotifyMyTrx);

		AuthorizedLimitSchemeModel authorizedLimit = corporateUtilsRepo.getAuthorizedLimitSchemeRepo()
				.findOne(authorizedLimitId);
		corporateUser.setAuthorizedLimit(authorizedLimit);

		// get approval map
		List<ApprovalMapModel> approvalMapList = approvalMapRepo.findByWorkflowRoleCodeAndApprovalLevelCode(roleCodeWF,
				authorizedLimit.getApprovalLevel().getCode());
		ApprovalMapModel approvalMap = approvalMapList.get(0);

		
		//update corporate user group jika berubah
		CorporateUserGroupModel corpUsrGroup = corporateUtilsRepo.getCorporateUserGroupRepo()
				.findByCorporateIdAndCode(corporateId, userGroupCode);
		
		if(!corpUsrGroup.getCode().equals(corporateUser.getCorporateUserGroup().getCode()) ||
				!approvalMap.getId().equals(corporateUser.getApprovalMap().getId())) {
			// delete old user role
			userRoleRepo.deleteByUserCode(idmUser.getCode());
			userRoleRepo.flush();
			
			String roleCodeAP = corpUsrGroup.getRole().getCode();
			idmUserRoleService.saveUserRole(idmUser, roleCodeAP, updatedBy);
			
			// create user role WF
			roleCodeWF = roleCodeWF.concat(authorizedLimit.getApprovalLevel().getCode());
			idmUserRoleService.saveUserRole(idmUser, roleCodeWF, updatedBy);
			
			//set new user group
			corporateUser.setCorporateUserGroup(corpUsrGroup);
			
			corporateUser.setApprovalMap(approvalMap);
		}
		//--------------------------

		updateCorporateUser(corporateUser, updatedBy, tokenNo, idmUser);

	}

	@Override
	public Map<String, Object> searchWorkflowRoleByApplicationCode(String applicationCode)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<WorkflowRoleModel> wfRoleList = maintenanceRepo.getWorkflowRoleRepo()
					.findByApplicationCode(applicationCode);

			List<Map<String, Object>> wfRoleResult = new ArrayList<>();
			for (WorkflowRoleModel wfRole : wfRoleList) {
				Map<String, Object> wfRoleMap = new HashMap<>();
				wfRoleMap.put(ApplicationConstants.STR_CODE, wfRole.getCode());
				wfRoleMap.put(ApplicationConstants.STR_NAME, wfRole.getName());
				wfRoleResult.add(wfRoleMap);
			}
			resultMap.put("result", wfRoleResult);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> searchTokenUserByCorporateId(String corporateId, String tokenType)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<TokenUserModel> tokenUserList = corporateUtilsRepo.getTokenUserRepo().findByCorporateIdAndTokenTypeCodeOrderByTokenNo(corporateId, tokenType);

			List<Map<String, Object>> tokenUserResult = new ArrayList<>();
			for (TokenUserModel model : tokenUserList) {
				Map<String, Object> modelMap = new HashMap<>();
				modelMap.put("tokenNo", model.getTokenNo());
				tokenUserResult.add(modelMap);
			}
			resultMap.put("result", tokenUserResult);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public List<String> searchUnassignTokenUserByCorporateId(String corporateId, String tokenType)
			throws ApplicationException, BusinessException {
		List<String> tokenUserResult = new ArrayList<>();
		try {
			List<TokenUserModel> tokenUserList = corporateUtilsRepo.getTokenUserRepo().findUnassignTokenByCorporateIdAndTokenTypeCodeOrderByTokenNo(corporateId, tokenType);
			for (TokenUserModel model : tokenUserList) {
				tokenUserResult.add(model.getTokenNo());
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return tokenUserResult;
	}
	
	@Override
	public Map<String, Object> getUserProfiles(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			CorporateUserModel corporateUser = corporateUserRepo.findOneFetchUser(userCode);
			CorporateModel corporate = corporateUser.getCorporate();
			WorkflowRoleModel wfRole = corporateUser.getApprovalMap().getWorkflowRole();
			IDMUserModel idmUser = corporateUser.getUser();
			
			resultMap.put(ApplicationConstants.CORP_ID, corporate.getId());
			resultMap.put("corporateName", corporate.getName());
			
			resultMap.put("isNotifyMyTask", corporateUser.getIsNotifyMyTask());
			resultMap.put("isNotifyMyTrx", corporateUser.getIsNotifyMyTrx());
			resultMap.put("email", ValueUtils.getValue(idmUser.getEmail()));
			
			
			BranchModel branch = corporate.getBranch();
			
			if(branch != null) {
				resultMap.put("corporateBranchCode", branch.getCode());
				resultMap.put("corporateBranchName", branch.getName());
			} else {
				resultMap.put("corporateBranchCode", ApplicationConstants.EMPTY_STRING);
				resultMap.put("corporateBranchName", ApplicationConstants.EMPTY_STRING);
			}
			
			resultMap.put("userRoleCode", wfRole.getCode());
			resultMap.put("userRole", wfRole.getName());
			resultMap.put("lastLoginDate", ValueUtils.getValue(idmUser.getLastLoginDate()));
			resultMap.put("imageUrl", ValueUtils.getValue(idmUser.getProfileImgUrl()));
			
			try {
				
				String imageUrl = pathUpload + File.separator + idmUser.getProfileImgFileName();
				File image = new File(imageUrl);
				byte[] imageContent = Files.readAllBytes(image.toPath());
				
				resultMap.put("imageBytes", imageContent);
			} catch (Exception e) {
				logger.error("No Image Uploaded : " + e.getMessage(), e);
			}
			
			TokenUserModel tokenUser = corporateUtilsRepo.getTokenUserRepo().findByAssignedUserCode(userCode);
			
			if(tokenUser != null) {
				TokenTypeModel tokenType = tokenUser.getTokenType();
				resultMap.put("tokenTypeCode", tokenType.getCode());
				resultMap.put("tokenTypeName", tokenType.getName());
				
				resultMap.put(ApplicationConstants.LOGIN_TOKEN_NO, tokenUser.getTokenNo());
			}
			
			//get widget list for UI (requested)
			getWidget(resultMap, wfRole.getCode());
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private void getWidget(Map<String, Object> map, String wfRoleCode) {
		List<Map<String, Object>> widgetList = new ArrayList<>();
		if(wfRoleCode.contains("CRP_USR")) {
			Map<String, Object> widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_USER_DASHBOARD/searchCountPendingTaskByUser");
			widgetData.put("widget", "pending_task");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_USER_DASHBOARD/searchCorporateUserGroupAccount");
			widgetData.put("widget", "user_group");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_USER_DASHBOARD/countTotalCreatedTrx");
			widgetData.put("widget", "created_transaction");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_USER_DASHBOARD/countTotalExecutedTrx");
			widgetData.put("widget", "executed_transaction");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_USER_DASHBOARD/findLimitUsage");
			widgetData.put("widget", "limit_usage");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_USER_DASHBOARD/findCOT");
			widgetData.put("widget", "cot");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_USER_DASHBOARD/getPromo");
			widgetData.put("widget", "promo");
			widgetList.add(widgetData);
			
		} else if(wfRoleCode.contains("CRP_ADM")) {
			Map<String, Object> widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_ADMIN_DASHBOARD/searchCorporateAccountGroup");
			widgetData.put("widget", "account_group");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_ADMIN_DASHBOARD/searchCorporateUserGroup");
			widgetData.put("widget", "user_group_admin");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_ADMIN_DASHBOARD/searchCorporateAccount");
			widgetData.put("widget", "account");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_ADMIN_DASHBOARD/searchCorporateUser");
			widgetData.put("widget", "user");
			widgetList.add(widgetData);
			
			widgetData = new HashMap<>();
			widgetData.put("service", "MNU_GPCASH_F_ADMIN_DASHBOARD/getActivityByUser");
			widgetData.put("widget", "activity");
			widgetList.add(widgetData);
		}
		
		map.put("widgetList", widgetList);
	}
	
	@Override
	public void forgotPassword(String corporateId, String userCode, String email) throws ApplicationException, BusinessException {
		try {
			CorporateUserModel corporateUser = corporateUserRepo.findOne(userCode);
			if(corporateUser != null) {
				CorporateModel corporate = corporateUser.getCorporate();
				
				if(corporate.getId().equals(corporateId)) {
					IDMUserModel idmUser = corporateUser.getUser();
					
					if(email.equalsIgnoreCase(idmUser.getEmail())) {
						//sent email
						idmUserService.resetUser(idmUser, ApplicationConstants.CREATED_BY_SYSTEM);
					} else
						throw new BusinessException("GPT-0100151");
				} else
					throw new BusinessException("GPT-0100151");
			} else
				throw new BusinessException("GPT-0100151");
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void updateUserNotificationFlag(String userCode, String notifyMyTask, String notifyMyTrx) throws ApplicationException, BusinessException {
		try {
			CorporateUserModel corporateUser = corporateUserRepo.findOne(userCode);
			corporateUser.setIsNotifyMyTask(notifyMyTask);
			corporateUser.setIsNotifyMyTrx(notifyMyTrx);
			corporateUserRepo.save(corporateUser);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> findUserMakerByCorporateIdAndDeleteFlag(String corporateId, String deleteFlag) throws ApplicationException {
		try {
			Map<String, Object> resultMap = new HashMap<>();
			
			List<CorporateUserModel> corpUserList = corporateUserRepo.findUserMakerByCorporateIdAndDeleteFlag(corporateId, ApplicationConstants.NO);

			List<Map<String, Object>> listUser = new ArrayList<>();
			for(CorporateUserModel corpUser : corpUserList) {
				IDMUserModel idmUser = corpUser.getUser();
				
				Map<String, Object> userMap = new HashMap<>();
				userMap.put("makerUserId", idmUser.getUserId());
				userMap.put("makerUserName", idmUser.getName());
				listUser.add(userMap);
			}
			
			resultMap.put("result", listUser);
			
			return resultMap;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> findUserByCorporate(String corporateId) throws ApplicationException {
		try {
			Map<String, Object> resultMap = new HashMap<>();
			
			List<CorporateUserModel> corpUserList = corporateUserRepo.findUserByCorporate(corporateId);

			List<Map<String, Object>> listUser = new ArrayList<>();
			for(CorporateUserModel corpUser : corpUserList) {
				IDMUserModel idmUser = corpUser.getUser();
				
				Map<String, Object> userMap = new HashMap<>();
				userMap.put("actionByUserId", idmUser.getUserId());
				userMap.put("actionByUserName", idmUser.getName());
				listUser.add(userMap);
			}
			
			resultMap.put("result", listUser);
			
			return resultMap;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public CorporateUserModel getCorpUserByUserCd(String userCode) {
		return corporateUserRepo.findOneFetchUser(userCode);
	}
}
