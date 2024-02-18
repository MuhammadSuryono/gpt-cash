package com.gpt.product.gpcash.retail.transaction.international.status.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;
import com.gpt.product.gpcash.retail.transaction.international.constants.CustomerInternationalConstants;

@RestController
public class CustomerInternationalTransferStatusController extends BankBaseController {
	
	@Autowired
	private MessageSource message;
	
	protected static final String menuCode = "MNU_R_GPCASH_INT_STS";

	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param, HttpServletRequest req, HttpServletResponse res) {
		return invoke("CustomerInternationalTransferStatusSC", method, param);
	}
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/getStatusForDroplist")
	public Map<String, Object> getStatusForDroplist(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(3,1);
		
		Locale locale = LocaleContextHolder.getLocale();
		
		List<Map<String, Object>> statusList = new ArrayList<>();
		
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("statusCode", CustomerInternationalConstants.INT_STS_NEW_REQUEST);
		dataMap.put("statusName", message.getMessage(CustomerInternationalConstants.INT_STS_NEW_REQUEST, null, CustomerInternationalConstants.INT_STS_NEW_REQUEST, locale));
		statusList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("statusCode", CustomerInternationalConstants.INT_STS_DECLINED);
		dataMap.put("statusName", message.getMessage(CustomerInternationalConstants.INT_STS_DECLINED, null, CustomerInternationalConstants.INT_STS_DECLINED, locale));
		statusList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("statusCode", CustomerInternationalConstants.INT_STS_PROCESSED);
		dataMap.put("statusName", message.getMessage(CustomerInternationalConstants.INT_STS_PROCESSED, null, CustomerInternationalConstants.INT_STS_PROCESSED, locale));
		statusList.add(dataMap);
		
		resultMap.put("result", statusList);
		return resultMap;
	}
		
}
