package com.gpt.product.gpcash.corporate.corporateuser.services;

import java.util.List;
import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateusergroup.model.CorporateUserGroupModel;
import com.gpt.product.gpcash.corporate.pendingtaskadmin.services.CorporateAdminWorkflowService;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.product.gpcash.approvalmap.model.ApprovalMapModel;

@AutoDiscoveryImpl
public interface CorporateUserService extends CorporateAdminWorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	void saveCorporateUser(CorporateUserModel corporateUser, String createdBy)
			throws ApplicationException, BusinessException;

	void deleteCorporateUser(CorporateUserModel corporateUser, String deletedBy)
			throws ApplicationException, BusinessException;
	
	void saveCorporateUserAndAssignToken(String corporateId, String userId, String userGroupCode, String mobileNo,
			String userName, String email, String isGrantViewDetail, String authorizedLimitId, String isNotifyMyTask,
			String isNotifyMyTrx, String tokenType, String tokenNo, String createdBy, String roleCodeWF,String isApproverReleaser, String isOneSigner) throws Exception;

	void saveCorporateUser(String userId, String mobileNo, String phoneNo, String isGrantViewDetail,
			ApprovalMapModel approvalMap, CorporateModel corporate, CorporateUserGroupModel corpUsrGroup,
			IDMUserModel idmUser, String createdBy) throws ApplicationException, BusinessException;

	void saveCorporateUser(String userId, String mobileNo, String isGrantViewDetail,
			ApprovalMapModel approvalMap, CorporateModel corporate, CorporateUserGroupModel corpUsrGroup,
			IDMUserModel idmUser, String createdBy, String authorizedLimitId, String isNotifyMyTask, String isNotifyMyTrx,String isApproverReleaser, String isOneSigner) throws ApplicationException, BusinessException;

	void updateCorporateUser(String corporateId, String userId, String userGroupCode, String mobileNo,
			String userName, String email, String isGrantViewDetail, String authorizedLimitId, String isNotifyMyTask,
			String isNotifyMyTrx, String tokenType, String tokenNo, String updatedBy, String roleCodeWF,String isApproverReleaser, String isOneSigner) throws Exception;

	void saveCorporateAdminUser(CorporateModel corporate, String userId, String userName, String mobileNo, String email,
			String createdBy, String wfRoleCode) throws Exception;

	Map<String, Object> searchWorkflowRoleByApplicationCode(String applicationCode) throws ApplicationException, BusinessException;
	
	Map<String, Object> searchTokenUserByCorporateId(String corporateId, String tokenType) throws ApplicationException, BusinessException;

	Map<String, Object> getUserProfiles(Map<String, Object> map) throws ApplicationException, BusinessException;

	List<String> searchUnassignTokenUserByCorporateId(String corporateId, String tokenType)
			throws ApplicationException, BusinessException;

	void updateCorporateAdminUser(CorporateModel corporate, String userId, String userName, String mobileNo,
			String email, String tokenNo, String updatedBy) throws Exception;

	void forgotPassword(String corporateId, String userCode, String email)
			throws ApplicationException, BusinessException;

	void updateUserNotificationFlag(String userCode, String notifyMyTask, String notifyMyTrx)
			throws ApplicationException, BusinessException;

	Map<String, Object> findUserMakerByCorporateIdAndDeleteFlag(String corporateId, String deleteFlag) throws ApplicationException;

	Map<String, Object> findUserByCorporate(String corporateId) throws ApplicationException;
	
	public CorporateUserModel getCorpUserByUserCd(String userCode);
}
