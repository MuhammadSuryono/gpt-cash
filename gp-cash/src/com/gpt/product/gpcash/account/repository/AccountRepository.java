package com.gpt.product.gpcash.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.account.model.AccountModel;

@Repository
public interface AccountRepository extends JpaRepository<AccountModel, String>, CashRepository<AccountModel>{

}
