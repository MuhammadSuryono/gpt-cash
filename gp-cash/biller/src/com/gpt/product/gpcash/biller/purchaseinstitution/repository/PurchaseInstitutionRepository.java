package com.gpt.product.gpcash.biller.purchaseinstitution.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.biller.purchaseinstitution.model.PurchaseInstitutionModel;

@Repository
public interface PurchaseInstitutionRepository extends JpaRepository<PurchaseInstitutionModel, String>, CashRepository<PurchaseInstitutionModel>{

}
