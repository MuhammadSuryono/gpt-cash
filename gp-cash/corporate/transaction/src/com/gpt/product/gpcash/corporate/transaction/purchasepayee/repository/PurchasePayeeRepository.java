package com.gpt.product.gpcash.corporate.transaction.purchasepayee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.purchasepayee.model.PurchasePayeeModel;

@Repository
public interface PurchasePayeeRepository extends JpaRepository<PurchasePayeeModel, String>, CashRepository<PurchasePayeeModel>{

	List<PurchasePayeeModel> findByPayeeNameAndCorporateUserGroupId(String payeeName, String userGroupId) throws Exception;

}
