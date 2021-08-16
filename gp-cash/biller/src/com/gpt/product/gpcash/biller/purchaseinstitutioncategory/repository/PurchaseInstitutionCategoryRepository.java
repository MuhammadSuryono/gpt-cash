package com.gpt.product.gpcash.biller.purchaseinstitutioncategory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.biller.purchaseinstitutioncategory.model.PurchaseInstitutionCategoryModel;

@Repository
public interface PurchaseInstitutionCategoryRepository extends JpaRepository<PurchaseInstitutionCategoryModel, String>, CashRepository<PurchaseInstitutionCategoryModel>{

}
