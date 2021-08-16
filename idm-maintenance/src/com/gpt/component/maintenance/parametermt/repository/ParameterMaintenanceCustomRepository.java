package com.gpt.component.maintenance.parametermt.repository;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gpt.component.maintenance.parametermt.model.ParameterMaintenanceModel;
import com.gpt.platform.cash.repository.CashRepository;

public interface ParameterMaintenanceCustomRepository extends CashRepository<ParameterMaintenanceModel> {

	Page<Object> searchModel(Map<String, Object> paramMap, Pageable pageInfo) throws Exception;
	
	Object findAnyOne(Class<?> clazz, String code);
	void saveAnyOne(Object model);
	void updateAnyOne(Object model);
	public Object getParameterMaintenanceByModelAndName(String modelCode, String name) throws ClassNotFoundException;
}
