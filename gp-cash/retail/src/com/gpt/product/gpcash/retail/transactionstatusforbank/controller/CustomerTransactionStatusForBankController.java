package com.gpt.product.gpcash.retail.transactionstatusforbank.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;
import com.gpt.product.gpcash.retail.transactionstatus.CustomerTransactionStatus;

@RestController
public class CustomerTransactionStatusForBankController extends BankBaseController {
	protected static final String menuCode = "MNU_R_GPCASH_BO_RPT_TRX_STS";

	@Autowired
	private MessageSource message;
	
	private Map<Locale, List<Map<String, String>>> trxStatus = new ConcurrentHashMap<>();

	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CustomerTransactionStatusForBankSC", method, param);
	}
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/getTransactionStatus")
	public List<Map<String, String>> getTransactionStatus(Locale locale) {
		List<Map<String, String>> result = trxStatus.get(locale);
		if(result == null) {
			CustomerTransactionStatus[] values = CustomerTransactionStatus.values();
			result = new ArrayList<>(values.length);
			
			for(CustomerTransactionStatus value : values) {
				Map<String, String> vals = new HashMap<>(2, 1);
				vals.put("key", value.name());
				vals.put("value", message.getMessage(value.name(), null, locale));
				result.add(vals);
			}
			trxStatus.put(locale, result);
		}
		
		return result;
	}
	
}