package com.gpt.component.maintenance.sysparam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.sysparam.model.SysParamModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface SysParamRepository extends JpaRepository<SysParamModel, String>, CashRepository<SysParamModel> {

	@Query("from SysParamModel sysParam where sysParam.code = (?1) and sysParam.deleteFlag = 'N'")
	SysParamModel findByCode(String code) throws Exception;

}