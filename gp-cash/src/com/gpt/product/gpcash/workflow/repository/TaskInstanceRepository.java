package com.gpt.product.gpcash.workflow.repository;

import java.util.List;

import javax.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.workflow.model.TaskInstance;

@Repository
public interface TaskInstanceRepository extends JpaRepository<TaskInstance, String>, TaskInstanceCustomRepository {
	
	@Modifying
	@Query("update TaskInstance ti set ti.endDate = CURRENT_TIMESTAMP, ti.canceled = true where ti.endDate is null and ti.processInstance.id = ?1")
	void cancelUnfinishedTasks(String processInstanceId);
	
	@Query("select user.code as userCode, user.userId as userId, user.name as userName, user.profileImgUrl as imageUrl, ti.startDate as startDate from TaskInstance ti, IDMUserModel user where ti.user = user.code and ti.processInstance.id = ?1 and ti.endDate is null and ti.canceled = false order by userName")
	List<Tuple> findActiveTasksByProcessInstanceId(String processInstanceId) throws BusinessException, ApplicationException;
	
	@Query("select ti.user as user from TaskInstance ti where ti.processInstance.id = ?1 and ti.canceled = false")
	List<String> findAssignedUserByProcessInstanceId(String processInstanceId) throws BusinessException, ApplicationException;
	
	TaskInstance findByIdAndUserAndProcessInstanceId(String id, String user, String processInstanceId);
	
	@Query("select user.code as userCode, user.userId as userId, user.name as userName, user.profileImgUrl as imageUrl, ti.status as status, ti.startDate as startDate, ti.endDate as endDate, ti.currApprLv as currApprLv, ti.stageId as stageId from TaskInstance ti, IDMUserModel user where ti.user = user.code and ti.processInstance.id = ?1 and ti.endDate is not null and ti.canceled = false order by endDate, userName")
	List<Tuple> findTaskHistoryByProcessInstanceId(String id);
	
	@Query("select count(ti) from TaskInstance ti join ti.processInstance pi where pi.endDate is null and ti.endDate is null and ti.canceled = false and ti.user = ?1")
	int countActiveTasksByUser(String user);
}
