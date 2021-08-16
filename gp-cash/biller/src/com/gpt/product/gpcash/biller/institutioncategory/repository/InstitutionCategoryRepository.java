package com.gpt.product.gpcash.biller.institutioncategory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.biller.institutioncategory.model.InstitutionCategoryModel;

@Repository
public interface InstitutionCategoryRepository extends JpaRepository<InstitutionCategoryModel, String>, CashRepository<InstitutionCategoryModel>{

}
