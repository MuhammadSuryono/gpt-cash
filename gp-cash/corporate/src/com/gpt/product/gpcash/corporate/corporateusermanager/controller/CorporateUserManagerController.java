package com.gpt.product.gpcash.corporate.corporateusermanager.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateAdminBaseController;

@RestController
public class CorporateUserManagerController extends CorporateAdminBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_USER_MANAGER";

	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CorporateUserManagerSC", method, param);
	}
}
