package com.gpt.product.gpcash.limitpackage.spi;

import java.math.BigDecimal;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;

public interface LimitPackageListener {
	
	void updateALLLimitByLimitPackageCode(BigDecimal maxAmountLimit, int maxOccurrenceLimit,
			String limitPackageCode, String currencyCode, String serviceCode, String serviceCurrencyMatrixId,
			String isUpdateCorporateWithSpecialLimit) throws Exception;

	void updateALLLimit(BigDecimal maxAmountLimit, int maxTrxPerDay, String currencyCode, String serviceCurrencyMatrixId,
			String isUpdateCorporateWithSpecialCharge);

	void saveNewLimitByLimitPackageCode(BigDecimal maxAmountLimit, int maxTrxPerDay, String limitPackageCode,
			String currencyCode, String serviceCurrencyMatrixId, String createdBy) throws ApplicationException, BusinessException;

	void updateByLimitPackageCode(BigDecimal maxAmountLimit, int maxTrxPerDay, String currencyCode, 
			String serviceCode, String serviceCurrencyMatrixId, String limitPackageCode);
}
