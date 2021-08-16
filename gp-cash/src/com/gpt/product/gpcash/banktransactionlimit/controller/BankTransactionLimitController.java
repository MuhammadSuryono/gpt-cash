package com.gpt.product.gpcash.banktransactionlimit.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;

@RestController
public class BankTransactionLimitController extends BankBaseController {

	protected static final String menuCode = "MNU_GPCASH_BANK_TRX_LMT";
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param) {
		param.put(ApplicationConstants.APP_CODE, Helper.getActiveApplicationCode());
		return invoke("BankTransactionLimitSC", method, param);
	}
}
