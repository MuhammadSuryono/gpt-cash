package com.gpt.product.gpcash.menupackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.menupackage.model.MenuPackageModel;

@Repository
public interface MenuPackageRepository extends JpaRepository<MenuPackageModel, String>, CashRepository<MenuPackageModel>{

}
