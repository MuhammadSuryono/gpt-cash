package com.gpt.product.gpcash.retail.transaction.purchasepayee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.transaction.purchasepayee.model.CustomerPurchasePayeeModel;

@Repository
public interface CustomerPurchasePayeeRepository extends JpaRepository<CustomerPurchasePayeeModel, String>, CashRepository<CustomerPurchasePayeeModel>{

	List<CustomerPurchasePayeeModel> findByPayeeNameAndCustomerId(String payeeName, String customerId) throws Exception;

}
