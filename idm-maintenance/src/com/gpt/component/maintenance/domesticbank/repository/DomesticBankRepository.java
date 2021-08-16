package com.gpt.component.maintenance.domesticbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.domesticbank.model.DomesticBankModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface DomesticBankRepository extends JpaRepository<DomesticBankModel, String>, CashRepository<DomesticBankModel> {

}
