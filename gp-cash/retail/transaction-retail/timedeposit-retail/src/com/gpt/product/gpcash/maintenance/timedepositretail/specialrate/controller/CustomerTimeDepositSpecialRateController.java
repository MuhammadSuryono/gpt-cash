package com.gpt.product.gpcash.maintenance.timedepositretail.specialrate.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;

@RestController
public class CustomerTimeDepositSpecialRateController extends BankBaseController {
	
	protected static final String menuCode = "MNU_R_GPCASH_MT_TD_SPECIAL_RATE";

	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CustomerTimeDepositSpecialRateSC", method, param);
	}
		
}
