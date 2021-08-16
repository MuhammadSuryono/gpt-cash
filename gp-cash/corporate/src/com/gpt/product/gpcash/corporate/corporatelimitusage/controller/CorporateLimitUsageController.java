package com.gpt.product.gpcash.corporate.corporatelimitusage.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateAdminBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class CorporateLimitUsageController extends CorporateAdminBaseController {

	protected static final String menuCode = "MNU_GPCASH_F_TRX_LMT_USG";
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param) {
		param.put(ApplicationConstants.APP_CODE, ApplicationConstants.APP_GPCASHIB);
		return invoke("CorporateLimitUsageSC", method, param);
	}
}