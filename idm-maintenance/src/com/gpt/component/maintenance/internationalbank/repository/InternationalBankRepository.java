package com.gpt.component.maintenance.internationalbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.internationalbank.model.InternationalBankModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface InternationalBankRepository extends JpaRepository<InternationalBankModel, String>, CashRepository<InternationalBankModel> {

}
