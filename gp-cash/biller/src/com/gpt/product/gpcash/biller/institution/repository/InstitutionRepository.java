package com.gpt.product.gpcash.biller.institution.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.biller.institution.model.InstitutionModel;

@Repository
public interface InstitutionRepository extends JpaRepository<InstitutionModel, String>, CashRepository<InstitutionModel>{

}
