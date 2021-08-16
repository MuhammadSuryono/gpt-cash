package com.gpt.product.gpcash.corporate.transaction.va.registration.controller;

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
import com.gpt.product.gpcash.corporate.corporate.VAStatus;

@RestController
public class VARegistrationController extends BankBaseController{
	protected static final String menuCode = "MNU_GPCASH_CORP_VA";
	
	@Autowired
	private MessageSource message;

	private Map<Locale, List<Map<String, String>>> status = new ConcurrentHashMap<>();
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("VARegistrationSC", method, param);
	}
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/getVAStatus")
	public List<Map<String, String>> getTransactionStatus(Locale locale) {
		List<Map<String, String>> result = status.get(locale);
		if(result == null) {
			VAStatus[] values = VAStatus.values();
			result = new ArrayList<>(values.length);
			
			for(VAStatus value : values) {
				Map<String, String> vals = new HashMap<>(2, 1);
				vals.put("key", value.name());
				vals.put("value", message.getMessage(value.name(), null, locale));
				result.add(vals);
			}
			status.put(locale, result);
		}
		
		return result;
	}
}
