package com.gpt.component.scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.scheduler.model.SchedulerTriggerModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface SchedulerTriggerRepository extends JpaRepository<SchedulerTriggerModel, String>, CashRepository<SchedulerTriggerModel>{

}
