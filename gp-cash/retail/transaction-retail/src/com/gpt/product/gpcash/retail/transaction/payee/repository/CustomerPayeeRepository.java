package com.gpt.product.gpcash.retail.transaction.payee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transaction.payee.model.CustomerPayeeModel;

@Repository
public interface CustomerPayeeRepository extends JpaRepository<CustomerPayeeModel, String>, CashRepository<CustomerPayeeModel>{

	List<CustomerPayeeModel> findByPayeeNameAndCustomerId(String payeeName, String customerId) throws Exception;

}
