package com.gpt.product.gpcash.corporate.corporatecharge.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class CorporateChargeController extends BankBaseController {

	protected static final String menuCode = "MNU_GPCASH_CORP_CH_PC_DTL";
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param) {
		param.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHIB);
		return invoke("CorporateChargeSC", method, param);
	}
	
}

