package com.gpt.product.gpcash.corporate.inquiry.balance.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateUserBaseController;

@RestController
public class BalanceSummaryController extends CorporateUserBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_BAL_SUMMARY";
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("BalanceSummarySC", method, param);
	}	

}
