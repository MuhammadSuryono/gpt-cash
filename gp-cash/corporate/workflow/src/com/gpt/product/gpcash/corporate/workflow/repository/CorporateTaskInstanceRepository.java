package com.gpt.product.gpcash.corporate.workflow.repository;

import java.util.List;

import javax.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.corporate.workflow.model.CorporateTaskInstance;

@Repository
public interface CorporateTaskInstanceRepository extends JpaRepository<CorporateTaskInstance, String>, CorporateTaskInstanceCustomRepository {
	
	@Modifying
	@Query("update CorporateTaskInstance ti set ti.endDate = CURRENT_TIMESTAMP, ti.canceled = true where ti.endDate is null and ti.processInstance.id = ?1")
	void cancelUnfinishedTasks(String processInstanceId);

	@Query("select user.code as userCode, user.userId as userId, user.name as userName, user.profileImgUrl as imageUrl, ti.startDate as startDate, ti.userApprovalLvName as approvalLvName, ti.userApprovalLvAlias as approvalLvAlias from CorporateTaskInstance ti, IDMUserModel user where ti.user = user.code and ti.processInstance.id = ?1 and ti.endDate is null and ti.canceled = false order by userName")
	List<Tuple> findActiveTasksByProcessInstanceId(String processInstanceId) throws BusinessException, ApplicationException;
	
	@Query("select ti.user as user from CorporateTaskInstance ti where ti.processInstance.id = ?1 and ti.canceled = false")
	List<String> findAssignedUserByProcessInstanceId(String processInstanceId) throws BusinessException, ApplicationException;
	
	CorporateTaskInstance findByIdAndUserAndProcessInstanceId(String id, String user, String processInstanceId);
	
	@Query("select user.code as userCode, user.userId as userId, user.name as userName, user.profileImgUrl as imageUrl, ti.userApprovalLvCode as approvalLvCode, ti.userApprovalLvName as approvalLvName, ti.userApprovalLvAlias as approvalLvAlias, ti.status as status, ti.amount as amount, ti.amountCcyCd as amountCcyCd, ti.startDate as startDate, ti.endDate as endDate, ti.currApprLv as currApprLv, ti.stageId as stageId from CorporateTaskInstance ti, IDMUserModel user where ti.user = user.code and ti.processInstance.id = ?1 and ti.endDate is not null and ti.canceled = false order by endDate, userName")
	List<Tuple> findTaskHistoryByProcessInstanceId(String id);
	
	@Query("select count(ti) from CorporateTaskInstance ti join ti.processInstance pi where pi.endDate is null and ti.endDate is null and ti.canceled = false and ti.user = ?1")
	int countActiveTasksByUser(String user);
}
