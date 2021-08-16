package com.gpt.component.logging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.logging.model.ErrorLogModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLogModel, String>, CashRepository<ErrorLogModel>{

}
