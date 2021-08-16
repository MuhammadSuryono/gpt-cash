package com.gpt.product.gpcash.corporate.outsourceadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.outsourceadmin.model.OutsourceAdminLoginModel;

@Repository
public interface OutsourceAdminRepository extends JpaRepository<OutsourceAdminLoginModel, String>, CashRepository<OutsourceAdminLoginModel>{
	
	@Modifying
	@Query("delete from OutsourceAdminLoginModel osAdmin where osAdmin.corporate.id = ?1")
	void deleteByCorpId(String corpId);

}
