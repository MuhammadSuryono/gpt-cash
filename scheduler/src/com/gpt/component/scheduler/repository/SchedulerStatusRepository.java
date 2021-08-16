package com.gpt.component.scheduler.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.scheduler.model.SchedulerStatusModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface SchedulerStatusRepository extends JpaRepository<SchedulerStatusModel, String>, CashRepository<SchedulerStatusModel>{

	@Query("from SchedulerStatusModel status "
			+ "where status.schedulerTask.code = ?1")
	Page<SchedulerStatusModel> findByTaskCode(String taskCode, Pageable pageInfo);
}
