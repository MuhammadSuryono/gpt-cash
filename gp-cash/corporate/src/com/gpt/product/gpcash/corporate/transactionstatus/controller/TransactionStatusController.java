package com.gpt.product.gpcash.corporate.transactionstatus.controller;

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

import com.gpt.component.base.controller.CorporateUserBaseController;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;

@RestController
public class TransactionStatusController extends CorporateUserBaseController {
	protected static final String menuCode = "MNU_GPCASH_F_TRX_STATUS";

	@Autowired
	private MessageSource message;
	
	private Map<Locale, List<Map<String, String>>> trxStatus = new ConcurrentHashMap<>();

	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("TransactionStatusSC", method, param);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/getTransactionStatus")
	public List<Map<String, String>> getTransactionStatus(Locale locale) {
		List<Map<String, String>> result = trxStatus.get(locale);
		if(result == null) {
			TransactionStatus[] values = TransactionStatus.values();
			result = new ArrayList<>(values.length);
			
			for(TransactionStatus value : values) {
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
