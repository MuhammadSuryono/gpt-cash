package com.gpt.product.gpcash.corporate.transaction.payee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.payee.model.PayeeModel;

@Repository
public interface PayeeRepository extends JpaRepository<PayeeModel, String>, CashRepository<PayeeModel>{

	List<PayeeModel> findByPayeeNameAndCorporateUserGroupId(String payeeName, String userGroupId) throws Exception;

}
