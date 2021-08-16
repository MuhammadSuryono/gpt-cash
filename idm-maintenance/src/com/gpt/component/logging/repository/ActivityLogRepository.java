package com.gpt.component.logging.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.logging.model.ActivityLogModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogModel, String>, CashRepository<ActivityLogModel>{
	Page<ActivityLogModel> searchActivityLog(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;
}
