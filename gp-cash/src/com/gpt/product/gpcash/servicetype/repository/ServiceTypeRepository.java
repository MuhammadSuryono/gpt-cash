package com.gpt.product.gpcash.servicetype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.servicetype.model.ServiceTypeModel;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceTypeModel, String>, CashRepository<ServiceTypeModel>{

}
