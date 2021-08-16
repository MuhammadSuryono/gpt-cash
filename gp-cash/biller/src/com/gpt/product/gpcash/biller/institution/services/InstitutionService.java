package com.gpt.product.gpcash.biller.institution.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.biller.institution.model.InstitutionModel;

@AutoDiscoveryImpl
public interface InstitutionService extends WorkflowService {
	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	InstitutionModel isInstitutionValid(String code) throws Exception;

	Map<String, Object> searchForBillPayment(Map<String, Object> map) throws ApplicationException, BusinessException;
}
