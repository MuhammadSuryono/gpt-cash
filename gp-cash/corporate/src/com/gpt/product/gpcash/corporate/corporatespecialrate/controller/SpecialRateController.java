package com.gpt.product.gpcash.corporate.corporatespecialrate.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;

@RestController
public class SpecialRateController extends BankBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_MT_SPECIAL_RATE";

	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("SpecialRateSC", method, param);
	}
		
}
