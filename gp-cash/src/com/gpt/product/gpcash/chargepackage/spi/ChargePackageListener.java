package com.gpt.product.gpcash.chargepackage.spi;

import java.math.BigDecimal;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;

public interface ChargePackageListener {
	void updateALLChargeByChargePackageCode(BigDecimal chargeAmount, String chargePackageCode,
			String currencyCode, String serviceChargeId, String isUpdateCorporateWithSpecialCharge);

	void updateALLCharge(BigDecimal chargeAmount, String currencyCode, String serviceChargeId,
			String isUpdateCorporateWithSpecialCharge);

	void saveNewChargeByChargePackageCode(BigDecimal chargeAmount, String chargePackageCode,
			String currencyCode, String serviceChargeId, String createdBy) throws ApplicationException, BusinessException;

	void updateByChargePackageCode(BigDecimal chargeAmount, String currencyCode, String serviceChargeId,
			String chargePackageCode);
}
