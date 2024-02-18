package com.gpt.product.gpcash.retail;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;

public interface IGenerateReport {
	void doGenerateReport(Map<String, Object> map, String requestBy)
			throws ApplicationException, BusinessException;
}
