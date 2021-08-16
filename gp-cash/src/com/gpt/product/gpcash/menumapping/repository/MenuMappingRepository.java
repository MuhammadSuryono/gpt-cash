package com.gpt.product.gpcash.menumapping.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.menumapping.model.MenuMappingModel;

@Repository
public interface MenuMappingRepository extends JpaRepository<MenuMappingModel, String>, CashRepository<MenuMappingModel>  {
	
}