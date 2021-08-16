package com.gpt.component.idm.user.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.passwordhistory.model.IDMPasswordHistoryModel;
import com.gpt.component.idm.passwordhistory.repository.IDMPasswordHistoryRepository;
import com.gpt.component.idm.role.model.IDMRoleModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.idm.userapp.model.IDMUserAppModel;
import com.gpt.component.idm.userapp.repository.IDMUserAppRepository;
import com.gpt.component.idm.userrole.model.IDMUserRoleModel;
import com.gpt.component.idm.userrole.repository.IDMUserRoleRepository;
import com.gpt.component.idm.userrole.services.IDMUserRoleService;
import com.gpt.component.idm.utils.IDMRepository;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.password.services.IPasswordUtils;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMUserServiceImpl implements IDMUserService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.avatar.upload.path}")
	private String pathUpload;
	
	@Value("${gpcash.avatar.domain.images.path}")
	private String avatarDomainPath;

	@Autowired
	private IDMUserRepository idmUserRepo;

	@Autowired
	private IDMRepository idmRepo;
	
	@Autowired
	private IDMUserRoleRepository idmUserRoleRepo;

	@Autowired
	private IDMUserRoleService idmUserRoleService;

	@Autowired
	private IDMUserAppRepository idmUserAppRepo;

	@Autowired
	private MaintenanceRepository maintenanceRepo;

	// @Autowired
	// private TokenUserRepository tokenUserRepo;

	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private IDMPasswordHistoryRepository idmPasswordHistoryRepo;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private IPasswordUtils passwordUtils;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<IDMUserModel> result = idmUserRepo.search(map, PagingUtils.createPageRequest(map));

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

	@Override
	public Map<String, Object> searchIDMUserForBackOffice(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String userCode = Helper.getSearchWildcardValue((String) map.get(ApplicationConstants.STR_CODE));
			String userName = Helper.getSearchWildcardValue((String) map.get(ApplicationConstants.STR_NAME));
			String branchCode = Helper.getSearchWildcardValue((String) map.get("branchCode"));

			List<String> applicationCodeList = new ArrayList<>();
			applicationCodeList.add(ApplicationConstants.APP_GPCASHBO);
			Page<IDMUserModel> result = idmUserRepo.findIDMUser(userCode, userName, applicationCodeList, branchCode,
					new PageRequest((Integer) map.get("currentPage") - 1, (Integer) map.get("pageSize")));

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

	private List<Map<String, Object>> setModelToMap(List<IDMUserModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (IDMUserModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(IDMUserModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>(17, 1);
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, model.getName());
		map.put("status", model.getStatus());

		BranchModel branch = model.getBranch();

		if (branch != null) {
			map.put("branchCode", branch.getCode());
			map.put("branchName", branch.getName());
		}

		if (isGetDetail) {
			map.put("email", ValueUtils.getValue(model.getEmail()));
			map.put("activeFrom", ValueUtils.getValue(model.getActiveFrom()));
			map.put("activeTo", ValueUtils.getValue(model.getActiveTo()));
			map.put("isPwdNeverExpired", model.getIsPwdNeverExpired());

			Map<String, Object> searchMap = new HashMap<>();
			searchMap.put("userCode", model.getCode());
			Page<IDMUserRoleModel> userRolePage = idmUserRoleRepo.search(searchMap, null);
			List<IDMUserRoleModel> userRoleList = userRolePage.getContent();

			List<Map<String, Object>> userRoleMapList = new ArrayList<>();
			for (IDMUserRoleModel userRole : userRoleList) {
				Map<String, Object> userRoleMap = new HashMap<>();

				IDMRoleModel role = userRole.getRole();
				userRoleMap.put("roleCode", role.getCode());
				userRoleMap.put("roleName", role.getName());
				userRoleMap.put("roleDscp", role.getDscp());

				userRoleMapList.add(userRoleMap);
			}

			map.put("roleCodeList", userRoleMapList);
			map.put("stillLoginFlag", model.getStillLoginFlag());
			map.put("createdBy", model.getCreatedBy());
			map.put("createdDate", model.getCreatedDate());
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
				IDMUserModel idmUserOld = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(idmUserOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
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

	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try {
			maintenanceRepo.isBranchValid((String) map.get("branchCode"));
			
			if(map.get("activeFrom") != null && map.get("activeTo") != null) {
				Date activeFrom = (Date) map.get("activeFrom");
				Date activeTo = (Date) map.get("activeTo");
				
				if(activeFrom.compareTo(activeTo) > 0){
					throw new BusinessException("GPT-0100166");
				}
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private IDMUserModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		IDMUserModel model = idmUserRepo.findOne(code);

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
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.STR_CODE)).concat(ApplicationConstants.DELIMITER_DASH)
				.concat((String) map.get(ApplicationConstants.STR_NAME)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("IDMUserSC");

		return vo;
	}

	private IDMUserModel setMapToModel(Map<String, Object> map) throws Exception {
		IDMUserModel idmUser = new IDMUserModel();
		idmUser.setCode((String) map.get(ApplicationConstants.STR_CODE));
		idmUser.setUserId((String) map.get(ApplicationConstants.STR_CODE));
		idmUser.setName((String) map.get(ApplicationConstants.STR_NAME));

		if (ValueUtils.hasValue(map.get("email")))
			idmUser.setEmail((String) map.get("email"));

		if (ValueUtils.hasValue(map.get("branchCode"))) {
			BranchModel branch = new BranchModel();
			branch.setCode((String) map.get("branchCode"));
			idmUser.setBranch(branch);
		}

		idmUser.setActiveFrom(Helper.parseStringToTimestamp((String) map.get("activeFrom")));

		if (map.get("activeTo") != null)
			idmUser.setActiveTo(Helper.parseStringToTimestamp((String) map.get("activeTo")));

		if (ValueUtils.hasValue(map.get("email")))
			idmUser.setEmail((String) map.get("email"));

		if (ValueUtils.hasValue(map.get("isPwdNeverExpired")))
			idmUser.setIsPwdNeverExpired((String) map.get("isPwdNeverExpired"));

		return idmUser;
	}

	private void checkUniqueRecord(String userCode) throws Exception {
		IDMUserModel idmUser = idmUserRepo.findOne(userCode);

		if (idmUser != null && ApplicationConstants.NO.equals(idmUser.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				IDMUserModel idmUser = setMapToModel(map);
				IDMUserModel idmUserExisting = idmUserRepo.findOne(idmUser.getCode());

				// cek jika record replacement
				if (idmUserExisting != null && ApplicationConstants.YES.equals(idmUserExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					idmUserExisting.setName(idmUser.getName());
					idmUserExisting.setEmail(idmUser.getEmail());
					idmUserExisting.setBranch(idmUser.getBranch());
					idmUserExisting.setActiveFrom(idmUser.getActiveFrom());
					idmUserExisting.setActiveTo(idmUser.getActiveTo());
					idmUserExisting.setIsPwdNeverExpired(idmUser.getIsPwdNeverExpired());

					// set default value
					idmUserExisting.setStatus(ApplicationConstants.IDM_USER_STATUS_ACTIVE);
					idmUserExisting.setStillLoginFlag(ApplicationConstants.NO);
					idmUserExisting.setDeleteFlag(ApplicationConstants.NO);
					idmUserExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					idmUserExisting.setCreatedBy(vo.getCreatedBy());
					
					generateNewPasswdAndEmail(idmUserExisting);

					// set reset value
					idmUserExisting.setUpdatedDate(null);
					idmUserExisting.setUpdatedBy(null);

					idmUserRepo.save(idmUserExisting);

					// save user role
					List<Map<String, Object>> roleCodeList = (ArrayList<Map<String, Object>>) map.get("roleCodeList");
					idmUserRoleService.updateUserRole(idmUserExisting, roleCodeList, vo.getCreatedBy());
				} else {
					// set default value

					// first time create password, status = RESET
					idmUser.setStatus(ApplicationConstants.IDM_USER_STATUS_RESET);
					idmUser.setStillLoginFlag(ApplicationConstants.NO);
					idmUser.setDeleteFlag(ApplicationConstants.NO);
					idmUser.setCreatedDate(DateUtils.getCurrentTimestamp());
					idmUser.setCreatedBy(vo.getCreatedBy());

					generateNewPasswdAndEmail(idmUser);

					idmUserRepo.saveAndFlush(idmUser);

					// save user app
					IDMUserAppModel userApp = new IDMUserAppModel();
					userApp.setUser(idmUser);
					IDMApplicationModel application = new IDMApplicationModel();
					application.setCode(ApplicationConstants.APP_GPCASHBO);
					userApp.setApplication(application);
					idmUserAppRepo.persist(userApp);

					// save user role
					List<Map<String, Object>> roleCodeList = (ArrayList<Map<String, Object>>) map.get("roleCodeList");
					idmUserRoleService.updateUserRole(idmUser, roleCodeList, vo.getCreatedBy());
				}

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				IDMUserModel idmUserNew = setMapToModel(map);

				IDMUserModel idmUserExisting = getExistingRecord(idmUserNew.getCode(), true);

				// set value yg boleh di edit
				idmUserExisting.setName(idmUserNew.getName());
				idmUserExisting.setEmail(idmUserNew.getEmail());
				idmUserExisting.setBranch(idmUserNew.getBranch());
				idmUserExisting.setActiveTo(idmUserNew.getActiveTo());
				idmUserExisting.setIsPwdNeverExpired(idmUserNew.getIsPwdNeverExpired());

				idmUserExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				idmUserExisting.setUpdatedBy(vo.getCreatedBy());

				idmUserRepo.save(idmUserExisting);

				// save user role
				List<Map<String, Object>> roleCodeList = (ArrayList<Map<String, Object>>) map.get("roleCodeList");
				idmUserRoleService.updateUserRole(idmUserExisting, roleCodeList, vo.getCreatedBy());
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				IDMUserModel idmUser = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
				idmUser.setDeleteFlag(ApplicationConstants.YES);
				idmUser.setUpdatedDate(DateUtils.getCurrentTimestamp());
				idmUser.setUpdatedBy(vo.getCreatedBy());

				idmUserRepo.save(idmUser);
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
	public void saveNewUser(IDMUserModel idmUser, String createdBy, boolean isNeedFlush)
			throws Exception {

		// set default value
		idmUser.setStatus(ApplicationConstants.IDM_USER_STATUS_RESET);
		idmUser.setStillLoginFlag(ApplicationConstants.NO);
		idmUser.setDeleteFlag(ApplicationConstants.NO);
		idmUser.setCreatedDate(DateUtils.getCurrentTimestamp());
		idmUser.setCreatedBy(createdBy);
		
		generateNewPasswdAndEmail(idmUser);
		
		if (isNeedFlush) {
			idmUserRepo.saveAndFlush(idmUser);
		} else {
			idmUserRepo.save(idmUser);
		}
	}

	@Override
	public IDMUserModel saveIDMUser(String id, String userId, String userName, String email, String createdBy)
			throws Exception {
		IDMUserModel idmUser = new IDMUserModel();
		idmUser.setCode(id);
		idmUser.setUserId(userId);
		idmUser.setName(userName);
		idmUser.setActiveFrom(DateUtils.getCurrentTimestamp());
		idmUser.setEmail(email);
		idmUser.setIsPwdNeverExpired(ApplicationConstants.YES);
		saveNewUser(idmUser, createdBy, true);

		return idmUser;
	}

	@Override
	public IDMUserModel updateIDMUser(IDMUserModel idmUser, String userName, String email, String createdBy)
			throws ApplicationException, BusinessException {
		idmUser.setName(userName);
		idmUser.setActiveFrom(DateUtils.getCurrentTimestamp());
		idmUser.setEmail(email);
		idmUser.setUpdatedDate(DateUtils.getCurrentTimestamp());
		idmUser.setUpdatedBy(createdBy);

		idmUserRepo.save(idmUser);

		return idmUser;
	}
	
	@Override
	public void unlockUser(String userCode, String createdBy) throws ApplicationException, BusinessException {
		try{
			IDMUserModel idmUser = idmRepo.isIDMUserValid(userCode);
			if(idmUser.getLastLoginDate()==null) {
				updateUserStatus(userCode, ApplicationConstants.IDM_USER_STATUS_RESET, createdBy);
			}else {
				updateUserStatus(userCode, ApplicationConstants.IDM_USER_STATUS_ACTIVE, createdBy);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void lockUser(String userCode, String createdBy) throws ApplicationException, BusinessException {
		try{
			updateUserStatus(userCode, ApplicationConstants.IDM_USER_STATUS_LOCKED, createdBy);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void resetUser(String userCode, String createdBy) throws ApplicationException, BusinessException {
		try{
			IDMUserModel idmUser = idmRepo.isIDMUserValid(userCode);
			
			resetUser(idmUser, createdBy);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void resetUser(IDMUserModel idmUser, String createdBy) throws ApplicationException, BusinessException {
		try{
			idmUser.setStatus(ApplicationConstants.IDM_USER_STATUS_RESET);
			idmUser.setLoginCount(0);
			idmUser.setUpdatedBy(createdBy);
			idmUser.setUpdatedDate(DateUtils.getCurrentTimestamp());
			
			generateNewPasswdAndEmail(idmUser);
			
			idmUserRepo.save(idmUser);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void inactivateUser(String userCode, String createdBy) throws ApplicationException, BusinessException {
		try{
			updateUserStatus(userCode, ApplicationConstants.IDM_USER_STATUS_INACTIVE, createdBy);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void activateUser(String userCode, String createdBy) throws ApplicationException, BusinessException {
		try{
			resetUser(userCode, createdBy);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	private void updateUserStatus(String userCode, String updateStatus, String createdBy)
			throws Exception {
		IDMUserModel idmUser = idmRepo.isIDMUserValid(userCode);
		idmUser.setStatus(updateStatus);
		
		if(updateStatus.equals(ApplicationConstants.IDM_USER_STATUS_ACTIVE) 
				|| updateStatus.equals(ApplicationConstants.IDM_USER_STATUS_RESET)) {
			idmUser.setLoginCount(0);
		}
		
		idmUser.setUpdatedDate(DateUtils.getCurrentTimestamp());
		idmUser.setUpdatedBy(createdBy);

		idmUserRepo.save(idmUser);
	}
	
	@Override
	public void changePassword(String userCode, String oldPassword, String newPassword, String newPassword2) throws BusinessException, ApplicationException{
		try{
			IDMUserModel user = idmUserRepo.findOne(userCode);
			
			if(user == null || user.getDeleteFlag().equals(ApplicationConstants.YES)) {
				throw new BusinessException("GPT-0100009");
			}
			
			String oldPasswdFromDB = new String(passwordUtils.decrypt(user.getPasswd().getBytes(ApplicationConstants.CHARSET)));
			//validate current password
			if(!oldPassword.equals(oldPasswdFromDB)){
				throw new BusinessException("GPT-0100115");
			}
			
			//validate new password must same with new password2
			if(!newPassword.equals(newPassword2)) {
				throw new BusinessException("GPT-0100136");
			}
			
			//validate old password cannot same with new password
			if(oldPassword.equals(newPassword2)) {
				throw new BusinessException("GPT-0100137");
			}
			
			//validate new password cannot same with userid
			//TODO user id harus di hash dl untuk di compare
			if(newPassword.equalsIgnoreCase(user.getCode())) {
				throw new BusinessException("GPT-0100138");
			}
			
			String newPasswdEncrypted = new String(passwordUtils.encrypt(	newPassword.getBytes(ApplicationConstants.CHARSET)));
			
			List<IDMPasswordHistoryModel> passwordHistoryList = idmPasswordHistoryRepo.findByUser(user.getCode());
			int historyCheck = Integer.parseInt(
					maintenanceRepo.isSysParamValid(SysParamConstants.PASS_HISTORY_VALIDATION).getValue());
			
			int i = 0;
			for(IDMPasswordHistoryModel passwdHistory : passwordHistoryList) {
				if(i == historyCheck)
					break;
				
				String passwdHistoryStr = new String(passwordUtils.decrypt(passwdHistory.getPasswd().getBytes(ApplicationConstants.CHARSET)));
				
				if(passwdHistoryStr.equals(newPassword)) {
					throw new BusinessException("GPT-0100139", new String[] {String.valueOf(historyCheck)});
				}
				
				i++;
			}
			user.setPasswd(newPasswdEncrypted);
			user.setStatus(ApplicationConstants.IDM_USER_STATUS_ACTIVE);
			user.setLastChangePasswordDate(DateUtils.getCurrentTimestamp());
			idmUserRepo.save(user);
			
			//save ke password history
			IDMPasswordHistoryModel passwdHistory = new IDMPasswordHistoryModel();
			passwdHistory.setUser(user);
			passwdHistory.setPasswd(newPasswdEncrypted);
			passwdHistory.setCreatedBy(userCode);
			passwdHistory.setCreatedDate(DateUtils.getCurrentTimestamp());
			idmPasswordHistoryRepo.save(passwdHistory);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void changePassword2(String userCode, String heartBeat, String newPassword, String newPassword2) throws BusinessException, ApplicationException{
		try{
			IDMUserModel user = idmUserRepo.findOne(userCode);
			
			if(user == null || user.getDeleteFlag().equals(ApplicationConstants.YES)) {
				throw new BusinessException("GPT-0100009");
			}
			
			String oldPasswdFromDB = new String(passwordUtils.decrypt(user.getPasswd().getBytes(ApplicationConstants.CHARSET)));
			byte[] key = Helper.generateAESKey(oldPasswdFromDB, heartBeat);
			
			try {
				byte[] newPasswordByte = passwordUtils.decryptCBC(newPassword.getBytes(ApplicationConstants.CHARSET), key); 
				newPassword = new String(newPasswordByte, ApplicationConstants.CHARSET);
			}catch(Exception e) {
				// if decrypt is failed, it means the old password is wrong
				throw new BusinessException("GPT-0100115");
			}
			
			//validate new password must be the same as new password2
			if(!Helper.passwordHashEquals(oldPasswdFromDB + newPassword, heartBeat, newPassword2)) {
				throw new BusinessException("GPT-0100136");
			}
			
			//validate old password cannot same with new password
			if(oldPasswdFromDB.equals(newPassword)) {
				throw new BusinessException("GPT-0100137");
			}
			
			//validate new password cannot same with userid
			//TODO user id harus di hash dl untuk di compare
			if(newPassword.equalsIgnoreCase(user.getCode())) {
				throw new BusinessException("GPT-0100138");
			}
			
			List<IDMPasswordHistoryModel> passwordHistoryList = idmPasswordHistoryRepo.findByUser(user.getCode());
			int historyCheck = Integer.parseInt(
					maintenanceRepo.isSysParamValid(SysParamConstants.PASS_HISTORY_VALIDATION).getValue());
			
			int i = 0;
			for(IDMPasswordHistoryModel passwdHistory : passwordHistoryList) {
				if(i == historyCheck)
					break;
				
				String passwdHistoryStr = new String(passwordUtils.decrypt(passwdHistory.getPasswd().getBytes(ApplicationConstants.CHARSET)));
				
				if(passwdHistoryStr.equals(newPassword)) {
					throw new BusinessException("GPT-0100139", new String[] {String.valueOf(historyCheck)});
				}
				
				i++;
			}
			
			String newPasswdEncrypted = new String(passwordUtils.encrypt(	newPassword.getBytes(ApplicationConstants.CHARSET)));
			user.setPasswd(newPasswdEncrypted);
			user.setStatus(ApplicationConstants.IDM_USER_STATUS_ACTIVE);
			user.setLastChangePasswordDate(DateUtils.getCurrentTimestamp());
			idmUserRepo.save(user);
			
			//save ke password history
			IDMPasswordHistoryModel passwdHistory = new IDMPasswordHistoryModel();
			passwdHistory.setUser(user);
			passwdHistory.setPasswd(newPasswdEncrypted);
			passwdHistory.setCreatedBy(userCode);
			passwdHistory.setCreatedDate(DateUtils.getCurrentTimestamp());
			idmPasswordHistoryRepo.save(passwdHistory);
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void updateLoginCount(String userCode)
			throws ApplicationException {
		try {
			IDMUserModel idmUser = idmRepo.isIDMUserValid(userCode);
			
			int loginCounter = idmUser.getLoginCount() + 1;
			
			if(loginCounter >= 3) {
				idmUser.setStatus(ApplicationConstants.IDM_USER_STATUS_LOCKED);
			}
			
			idmUser.setLoginCount(loginCounter);
			idmUser.setUpdatedDate(DateUtils.getCurrentTimestamp());
			idmUser.setUpdatedBy(userCode);
	
			idmUserRepo.save(idmUser);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void updateUserToInactive(String status, String userCode) throws ApplicationException, BusinessException {
		try{
			//requested by Susi (02 October 2017) update to LOCKED because there is no button to activate in corporate user and corporate admin
			idmUserRepo.updateUserToInactive(status, userCode, DateUtils.getCurrentTimestamp());
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	protected Map<String, Object> getRandomPasswd() throws Exception {
		int minPasswdLen = Integer.parseInt(maintenanceRepo.isSysParamValid(SysParamConstants.MIN_PWD_LENGTH).getValue());
		String passwd = Helper.getRandomPassword(minPasswdLen);
		String encryptedPasswd = new String(passwordUtils.encrypt(passwd.getBytes()));
		
		Map<String, Object> passwdMap = new HashMap<>();
		passwdMap.put("passwd", passwd);
		passwdMap.put("encryptedPasswd", encryptedPasswd);
		
		return passwdMap;
	}
	
	protected void generateNewPasswdAndEmail(IDMUserModel idmUser) throws Exception {
		Map<String, Object> passwdMap = getRandomPasswd();
		String passwd = (String) passwdMap.get("passwd");
		String encryptedPasswd = (String) passwdMap.get("encryptedPasswd");

		idmUser.setPasswd(encryptedPasswd);
		
		List<String> emailList = new ArrayList<>();
		emailList.add(idmUser.getEmail());
		
		Map<String, Object> inputs = new HashMap<>();
		inputs.put("emails", emailList);
		inputs.put("subject", "User Password Notification");
		
		String userId = idmUser.getUserId(); //idmUser.getUserId() = userId untuk corporate
		if(userId == null) {//jika null maka user bankline
			userId = idmUser.getCode(); 
		}
		
		inputs.put("userId", userId);
		inputs.put("username", idmUser.getName());
		inputs.put("password", passwd);
		inputs.put("productName", maintenanceRepo.isSysParamValid(SysParamConstants.PRODUCT_NAME).getValue());
		inputs.put("bankName", maintenanceRepo.isSysParamValid(SysParamConstants.BANK_NAME).getValue());
		
		eaiAdapter.invokeService(EAIConstants.USER_PASSWORD_NOTIFICATION, inputs);
		
	}
	
	@Override
	public Map<String, Object> uploadAvatar(byte[] rawData, String filename) throws ApplicationException, BusinessException {
		if (rawData == null)
			throw new BusinessException("GPT-0100113"); // Please select a file to upload

        try {
        	String id = Helper.generateHibernateUUIDGenerator();
        	
        	String profileImgUrl = pathUpload + File.separator + filename + "_" + id + ".jpeg";
            Path path = Paths.get(profileImgUrl);
            Files.write(path, rawData);
            
            Map<String,Object> result = new HashMap<>();
			result.put("fileId", id);
			result.put(ApplicationConstants.FILENAME, filename);
			
            return result;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
	}
	
	@Override
	public void saveAvatar(String userCode, String filename, String fileId) throws ApplicationException, BusinessException {
    	IDMUserModel idmUser = idmUserRepo.findOne(userCode);
    	
    	//delete old file
    	try{
    		 String oldImageUrl = pathUpload + File.separator + idmUser.getProfileImgFileName();
    		 File file = new File(oldImageUrl);
    	     file.delete();
    	}catch(Exception e){
    		logger.error("Unable to delete file : " + idmUser.getProfileImgFileName());
    	}
       
    	
        //save new file
        String fileNameValue = filename + "_" + fileId + ".jpeg";
    	String profileImgUrl = avatarDomainPath + File.separator + fileNameValue;
        idmUser.setProfileImgUrl(profileImgUrl);
        idmUser.setProfileImgFileName(fileNameValue);
        idmUserRepo.save(idmUser);
	}
	
	@Override
	public Map<String, Object> findBankUsers() throws ApplicationException {
		try {
			Map<String, Object> resultMap = new HashMap<>();
			
			List<IDMUserModel> userList = idmUserRepo.findBankUsers(ApplicationConstants.APP_GPCASHBO);

			List<Map<String, Object>> listUser = new ArrayList<>();
			for(IDMUserModel idmUser : userList) {
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
}