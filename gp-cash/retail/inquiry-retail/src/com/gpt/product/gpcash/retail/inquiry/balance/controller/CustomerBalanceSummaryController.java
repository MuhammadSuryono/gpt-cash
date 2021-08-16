package com.gpt.product.gpcash.retail.inquiry.balance.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CustomerUserBaseController;

@RestController
public class CustomerBalanceSummaryController extends CustomerUserBaseController {
	
	protected static final String menuCode = "MNU_R_GPCASH_F_BAL_SUMMARY";
	
	@RequestMapping(baseCustUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CustomerBalanceSummarySC", method, param);
	}	

}
