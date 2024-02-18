package com.gpt.component.idm.login.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.loginhistory.model.IDMLoginHistoryModel;
import com.gpt.component.idm.loginhistory.services.IDMLoginHistoryService;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.component.idm.menu.repository.IDMMenuRepository;
import com.gpt.component.idm.menutree.model.IDMMenuTreeModel;
import com.gpt.component.idm.menutree.repository.IDMMenuTreeRepository;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.idm.userapp.repository.IDMUserAppRepository;
import com.gpt.component.idm.userrole.model.IDMUserRoleModel;
import com.gpt.component.idm.userrole.repository.IDMUserRoleRepository;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.password.services.IPasswordUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMLoginServiceImpl implements IDMLoginService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private IDMLoginHistoryService idmLoginHistoryService;

	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private IDMUserService idmUserService;

	@Autowired
	private IDMUserAppRepository idmUserAppRepo;

	@Autowired
	private IDMUserRoleRepository idmUserRoleRepo;

	@Autowired
	private IDMMenuTreeRepository idmMenuTreeRepo;

	@Autowired
	private IDMMenuRepository idmMenuRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private IPasswordUtils passwordUtils;

	@Override
	public Map<String, Object> login(Map<String, Object> map) throws ApplicationException, BusinessException {
		String loginId = (String) map.get(ApplicationConstants.LOGIN_USERID);
		String passwd = (String) map.get("passwd");
		String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);

		try {
			return login(loginId, passwd, ipAddress);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

	}
	
	@Override
	public String getPlainPasswd(String passwd, String key) {
		String plainPasswd = ApplicationConstants.EMPTY_STRING;
		
		if(key == null) 
			key = ApplicationConstants.EMPTY_STRING;
		
		try {
			plainPasswd = new String(passwordUtils.decryptCBC(passwd.getBytes(ApplicationConstants.CHARSET), key.getBytes(ApplicationConstants.CHARSET)));
		} catch (Exception e) {
			//sengaja di telan, dr depan tidak perlu tau.
			logger.debug("Invalid during Decrypt");
		}
		
		return plainPasswd;
	}

	/**
	 * @deprecated use {@link #login(String, String, String, String)} instead
	 */
	@Deprecated
	@Override
	public Map<String, Object> login(String userCode, String passwd, String ipAddress) throws BusinessException, ApplicationException {
		try {
			Map<String, Object> resultMap = new HashMap<>();
	
			IDMUserModel user = idmUserRepo.findOne(userCode);
			validateUserAndPassword(user, passwd);
			
			//START remark if stress test
			validateUserStillLogin(user);
	
			//gunanya adalah agar user bisa akses detailPendingTask dan detailExecutedTransaction (untuk trx sts detail)
			//meskipun tidak memiliki menu tersebut sewaktu getProfiles.
			List<String> menuList = idmMenuRepo.findMenuByUserCodeForAP(user.getCode());
			if(menuList.isEmpty()) {
				validateUserRole(userCode);
			}
			
			List<String> appListStr = getUserApplication(userCode);
			
			// get last login
			resultMap.put("lastLoginDate", user.getLastLoginDate());
	
			// save login history
			List<String> loginHistoryList = new ArrayList<>();
			for (String applicationCode : appListStr) {
				IDMLoginHistoryModel loginHistory = idmLoginHistoryService.saveLoginHistory(user.getCode(), applicationCode, ipAddress);
				loginHistoryList.add(loginHistory.getId());
			}
			
			updateLoginFlag(user);
			resultMap.put(ApplicationConstants.LOGIN_MENULIST, menuList);
			resultMap.put(ApplicationConstants.LOGIN_HISTORY_ID, loginHistoryList);
			//END remark if stress test
	
			resultMap.put(ApplicationConstants.LOGIN_DATE, user.getLastLoginDate());
			resultMap.put(ApplicationConstants.LOGIN_USERID, user.getCode());
			resultMap.put(ApplicationConstants.LOGIN_USERNAME, user.getName());
			resultMap.put(ApplicationConstants.IDM_USER_STATUS, user.getStatus());
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	
	@Override
	public Map<String, Object> loginOutsource(String userCode, String ipAddress) throws BusinessException, ApplicationException {
		try {
			Map<String, Object> resultMap = new HashMap<>();
	
			IDMUserModel user = idmUserRepo.findOne(userCode);

			//gunanya adalah agar user bisa akses detailPendingTask dan detailExecutedTransaction (untuk trx sts detail)
			//meskipun tidak memiliki menu tersebut sewaktu getProfiles.
			List<String> menuList = idmMenuRepo.findMenuByUserCodeForAP(user.getCode());
			if(menuList.isEmpty()) {
				validateUserRole(userCode);
			}
			
			List<String> appListStr = getUserApplication(userCode);
			
			// get last login
			resultMap.put("lastLoginDate", user.getLastLoginDate());
	
			// save login history
			List<String> loginHistoryList = new ArrayList<>();
			for (String applicationCode : appListStr) {
				IDMLoginHistoryModel loginHistory = idmLoginHistoryService.saveLoginHistory(user.getCode(), applicationCode, ipAddress);
				loginHistoryList.add(loginHistory.getId());
			}
			
			resultMap.put(ApplicationConstants.LOGIN_MENULIST, menuList);
			resultMap.put(ApplicationConstants.LOGIN_HISTORY_ID, loginHistoryList);
			//END remark if stress test
	
			resultMap.put(ApplicationConstants.LOGIN_DATE, user.getLastLoginDate());
			resultMap.put(ApplicationConstants.LOGIN_USERID, user.getCode());
			resultMap.put(ApplicationConstants.LOGIN_USERNAME, user.getName());
			resultMap.put(ApplicationConstants.IDM_USER_STATUS, user.getStatus());
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> login(String userCode, String passwd, String heartBeat, String ipAddress) throws BusinessException, ApplicationException {
		try {
			Map<String, Object> resultMap = new HashMap<>();
	
			IDMUserModel user = idmUserRepo.findOne(userCode);
			validateUserAndPassword(user, passwd, heartBeat);
			
			//START remark if stress test
			validateUserStillLogin(user);
	
			//gunanya adalah agar user bisa akses detailPendingTask dan detailExecutedTransaction (untuk trx sts detail)
			//meskipun tidak memiliki menu tersebut sewaktu getProfiles.
			List<String> menuList = idmMenuRepo.findMenuByUserCodeForAP(user.getCode());
			if(menuList.isEmpty()) {
				/**
				 * only user that has not been configured properly will get into this
				 * do we really need to clarify if this user actually doesn't have any role yet ???
				 */
				validateUserRole(userCode);
				
				/**
				 * ok so the role exists but still the menusList is empty, shouldn't we throw some exception here ???
				 */
			}
			
			List<String> appListStr = getUserApplication(userCode);
			
			// get last login
			resultMap.put("lastLoginDate", user.getLastLoginDate());
	
			// save login history
			List<String> loginHistoryList = new ArrayList<>();
			for (String applicationCode : appListStr) {
				IDMLoginHistoryModel loginHistory = idmLoginHistoryService.saveLoginHistory(user.getCode(), applicationCode, ipAddress);
				loginHistoryList.add(loginHistory.getId());
			}
			
			updateLoginFlag(user);
			resultMap.put(ApplicationConstants.LOGIN_MENULIST, menuList);
			resultMap.put(ApplicationConstants.LOGIN_HISTORY_ID, loginHistoryList);
			//END remark if stress test
	
			resultMap.put(ApplicationConstants.LOGIN_DATE, user.getLastLoginDate());
			resultMap.put(ApplicationConstants.LOGIN_USERID, user.getCode());
			resultMap.put(ApplicationConstants.LOGIN_USERNAME, user.getName());
			resultMap.put(ApplicationConstants.IDM_USER_STATUS, user.getStatus());
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}	
	
	private List<String> getUserApplication(String userCode) throws BusinessException {
		// get user application
		List<String> appList = idmUserAppRepo.findApplicationsCodeByUserCode(userCode);
		if (appList.isEmpty()) {
			throw new BusinessException("GPT-0100033");
		}
		
		return appList;
	}
	
	/**
	 * 
	 * @param user
	 * @param passwd
	 * @return
	 * @throws BusinessException
	 * @throws Exception
	 * 
	 * @deprecated use {@link #validateUserAndPassword(IDMUserModel, String, String)} instead
	 */
	@Deprecated
	private IDMUserModel validateUserAndPassword(IDMUserModel user, String passwd)
			throws BusinessException, Exception {
		
		Date todayDate = DateUtils.getCurrentDate();

		//validate user exist or not
		if(user == null) {
			throw new BusinessException("GPT-0100032");
		}
		
		if(user.getPasswd() == null) {
			throw new BusinessException("GPT-0100032");
		}
		
		//validate user locked or not
		if(ApplicationConstants.IDM_USER_STATUS_LOCKED.equals(user.getStatus())) {
			throw new BusinessException("GPT-0100135", new String[] {ApplicationConstants.IDM_USER_STATUS_LOCKED});
		}
		
		//validate user active or inactive
		if(ApplicationConstants.IDM_USER_STATUS_INACTIVE.equals(user.getStatus())) {
			throw new BusinessException("GPT-0100135", new String[] {ApplicationConstants.IDM_USER_STATUS_INACTIVE});
		}
		
		//validate user deleted or not
		if(ApplicationConstants.YES.equals(user.getDeleteFlag())) {
			throw new BusinessException("GPT-0100032");
		}
		
		//validate user idle days
		validateUserIdle(user);
		
		//validate password
		String passwordFromDB = new String(passwordUtils.decrypt(user.getPasswd().getBytes(ApplicationConstants.CHARSET)));
		if(!passwordFromDB.equals(passwd)) {
			idmUserService.updateLoginCount(user.getCode());
			throw new BusinessException("GPT-0100032");
		}
		
		//validate password has been reset or not (force change password)
		if(ApplicationConstants.IDM_USER_STATUS_RESET.equals(user.getStatus())) {
			throw new BusinessException("GPT-0100141");
		}
		
		//validate user active period
		Date activeFromDate = DateUtils.getSQLDate(user.getActiveFrom());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(activeFromDate);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
		
		if(calendar.getTime().compareTo(todayDate) > 0) {
			throw new BusinessException("GPT-0100164");
		}
		
		if(user.getActiveTo() != null) {
			Date activeToDate = DateUtils.getSQLDate(user.getActiveTo());
			if(activeToDate.compareTo(todayDate) < 0) {
				throw new BusinessException("GPT-0100165");
			}
		}
		//-------------------------------
		
		
		//validate password already expired
		if(ApplicationConstants.NO.equals(user.getIsPwdNeverExpired())) {
		validateUserPasswordExpired(user);
		}
		
		return user;
	}
	

	/**
	 * 
	 * @param user
	 * @param key
	 * @param passwdHash
	 * @return
	 * @throws BusinessException
	 * @throws Exception
	 */
	private IDMUserModel validateUserAndPassword(IDMUserModel user, String passwdHash, String heartBeat)
			throws BusinessException, Exception {
		
		Date todayDate = DateUtils.getCurrentDate();

		//validate user exist or not
		if(user == null) {
			throw new BusinessException("GPT-0100032");
		}
		
		if(user.getPasswd() == null) {
			throw new BusinessException("GPT-0100032");
		}
		
		//validate user locked or not
		if(ApplicationConstants.IDM_USER_STATUS_LOCKED.equals(user.getStatus())) {
			throw new BusinessException("GPT-0100135", new String[] {ApplicationConstants.IDM_USER_STATUS_LOCKED});
		}
		
		//validate user active or inactive
		if(ApplicationConstants.IDM_USER_STATUS_INACTIVE.equals(user.getStatus())) {
			throw new BusinessException("GPT-0100135", new String[] {ApplicationConstants.IDM_USER_STATUS_INACTIVE});
		}
		
		//validate user deleted or not
		if(ApplicationConstants.YES.equals(user.getDeleteFlag())) {
			throw new BusinessException("GPT-0100032");
		}
		
		//validate user idle days
		validateUserIdle(user);
		
		//validate password
		String passwordFromDB = new String(passwordUtils.decrypt(user.getPasswd().getBytes(ApplicationConstants.CHARSET)));
		
		if(!Helper.passwordHashEquals(passwordFromDB, heartBeat, passwdHash)) {
			idmUserService.updateLoginCount(user.getCode());
			throw new BusinessException("GPT-0100032");
		}
		
		//validate user active period
		Date activeFromDate = DateUtils.getSQLDate(user.getActiveFrom());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(activeFromDate);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);
	    calendar.set(Calendar.MILLISECOND, 0);
		
		if(calendar.getTime().compareTo(todayDate) > 0) {
			throw new BusinessException("GPT-0100164");
		}
		
		if(user.getActiveTo() != null) {
			Date activeToDate = DateUtils.getSQLDate(user.getActiveTo());
			if(activeToDate.compareTo(todayDate) < 0) {
				throw new BusinessException("GPT-0100165");
			}
		}
		//-------------------------------
		
		//validate password already expired
		if(ApplicationConstants.NO.equals(user.getIsPwdNeverExpired()) && user.getLastChangePasswordDate() != null) {
			validateUserPasswordExpired(user);
			}
		
		return user;
	}
	
	private void validateUserPasswordExpired(IDMUserModel user) throws Exception {
		if(user.getLastLoginDate() != null) {
	        long diff = DateUtils.getCurrentTimestamp().getTime() - user.getLastChangePasswordDate().getTime(); 
	        
	        long diffDays =  diff / (24 * 60 * 60 * 1000);
	        
	        if(logger.isDebugEnabled()) {
	        	logger.debug("getLastChangePasswordDate : " + user.getLastChangePasswordDate());
	        	logger.debug("validateUserPasswordExpired diffDays : " + diffDays);
	        }
	        
	        int passwordValidityDays = Integer.parseInt(
	        		maintenanceRepo.isSysParamValid(SysParamConstants.PWD_VALIDITY).getValue());
			if(diffDays > passwordValidityDays) {
				throw new BusinessException("GPT-0100140");
			}
		}
	}
	
	private void validateUserIdle(IDMUserModel user) throws Exception {
		if(user.getLastLoginDate() != null) {
	        long diff = DateUtils.getCurrentTimestamp().getTime() - user.getLastLoginDate().getTime(); 
	        
	        long diffDays =  diff / (24 * 60 * 60 * 1000);
	        
	        if(logger.isDebugEnabled()) {
	        	logger.debug("getLastLoginDate : " + user.getLastLoginDate());
	        	logger.debug("validateUserIdle diffDays : " + diffDays);
	        }
	        	
	        
	        int maxUserIdleDays = Integer.parseInt(maintenanceRepo.isSysParamValid(SysParamConstants.MAX_USER_IDLE).getValue());
	        
			if(diffDays > maxUserIdleDays && user.getLastLockedFromIdleDate() == null) {
				idmUserService.updateUserToInactive(ApplicationConstants.IDM_USER_STATUS_LOCKED, user.getCode());
				throw new BusinessException("GPT-0100135", new String[] {ApplicationConstants.IDM_USER_STATUS_LOCKED});
			}
		}
	}

//	private IDMUserModel validateUserApplication(String userCode, List<String> userApplicationList)
//			throws BusinessException, Exception {
//		List<IDMUserModel> userList = idmUserRepo.findUserApplication(userCode, userApplicationList, null).getContent();
//		
//		if (userList.isEmpty()) {
//			throw new BusinessException("GPT-0100032");
//		}
//		IDMUserModel user = userList.get(0);
//
//		return user;
//	}
	
	private void validateUserStillLogin(IDMUserModel user)
			throws BusinessException {
		if (ApplicationConstants.YES.equals(user.getStillLoginFlag())) {
			throw new BusinessException("GPT-0100134");
		}
	}

	private List<IDMUserRoleModel> validateUserRole(String userCode) throws BusinessException {
		// validate user role
		List<IDMUserRoleModel> userRoleList = idmUserRoleRepo.findByUserCodeAndRoleTypeCode(userCode,
				ApplicationConstants.ROLE_TYPE_AP);
		if (userRoleList.isEmpty()) {
			throw new BusinessException("GPT-0100034");
		}

		return userRoleList;
	}

	private List<Map<String, Object>> setModelMenuTreeToMap(List<IDMMenuTreeModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (IDMMenuTreeModel model : list) {
			resultList.add(setModelMenuTreeToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelMenuTreeToMap(IDMMenuTreeModel model) {
		Map<String, Object> map = new HashMap<>();

		IDMMenuModel menu = model.getMenu();
		map.put(ApplicationConstants.STR_MENUCODE, menu.getCode());
		map.put("menuName", menu.getName());
		map.put("icon", ValueUtils.getValue(menu.getIcon()));
		map.put("lvl", model.getLvl());
		map.put("idx", model.getIdx());

		IDMMenuModel parent = model.getParentMenu();

		if (parent != null) {
			map.put("parentMenuCode", parent.getCode());
			map.put("parentMenuName", parent.getName());
		}

		return map;
	}

	@Override
	public void logout(List<String> loginHistoryList, String userId, Timestamp loginDate) throws ApplicationException, BusinessException {
		try {
			if(loginHistoryList != null)
				idmLoginHistoryService.updateLogoutLoginHistories(loginHistoryList);
			idmUserRepo.releaseLoginSession(userId, loginDate);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> getProfiles(String userCode, String passwd, boolean isJoinWithWF) throws ApplicationException, BusinessException {
		try {
			List<String> appListStr = getUserApplication(userCode);

			// get user menu
			List<String> menuAPList = idmMenuRepo.findMenuByUserCodeForAP(userCode);
			
			// get menu tree by menuCodeList
			Map<String, Object> resultMap = new HashMap<>();
			
			if(isJoinWithWF) {
				List<String> menuList = idmMenuRepo.findMenuByUserCodeJoinWithWF(userCode, menuAPList);
				
				if(menuList.size() > 0 && menuAPList.size() > 0) {
					List<IDMMenuTreeModel> menuTreeList = idmMenuTreeRepo.findByMenuCodesAndApplicationCodes(menuList, appListStr);
					resultMap.put("loginMenuTree", setModelMenuTreeToMap(menuTreeList));
				} else {
					resultMap.put("loginMenuTree", new ArrayList<>());
				}
			} else {
				if(menuAPList.size() > 0) {
					List<IDMMenuTreeModel> menuTreeList = idmMenuTreeRepo.findByMenuCodesAndApplicationCodes(menuAPList, appListStr);
					resultMap.put("loginMenuTree", setModelMenuTreeToMap(menuTreeList));
				} else {
					resultMap.put("loginMenuTree", new ArrayList<>());
				}
			}
			
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void updateLoginFlag(IDMUserModel user){
		user.setLoginCount(0);
		user.setLastLockedFromIdleDate(null);//this is to avoid user got locked again for idle days validation
		user.setStillLoginFlag(ApplicationConstants.YES);
		user.setLastLoginDate(DateUtils.getCurrentTimestamp());
		idmUserRepo.save(user);
	}
	
	
	
	/**
	 * Used for mobile
	 */
	
	@Override
	public Map<String, Object> authenticate(String userCode, String passwd, String heartBeat, String ipAddress) throws BusinessException, ApplicationException {
		try {
			IDMUserModel user = idmUserRepo.findOne(userCode);
			validateUserAndPassword(user, passwd, heartBeat);
			
			//Do we need to check for this ???
			validateUserStillLogin(user);
	
			validateUserRole(userCode);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(ApplicationConstants.LOGIN_DATE, user.getLastLoginDate());
			resultMap.put(ApplicationConstants.LOGIN_USERID, user.getCode());
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}	
	
}
