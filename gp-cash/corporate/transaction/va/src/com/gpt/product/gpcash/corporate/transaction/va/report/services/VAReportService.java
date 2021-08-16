package com.gpt.product.gpcash.corporate.transaction.va.report.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;

@AutoDiscoveryImpl
public interface VAReportService {

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;
	
	Map<String, Object> downloadVAReportPDF(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchProductForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException;

}
