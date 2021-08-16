package com.gpt.product.gpcash.accounthistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.accounthistory.model.AccountHistoryModel;

@Repository
public interface AccountHistoryRepository extends JpaRepository<AccountHistoryModel, String>, CashRepository<AccountHistoryModel>{

}
