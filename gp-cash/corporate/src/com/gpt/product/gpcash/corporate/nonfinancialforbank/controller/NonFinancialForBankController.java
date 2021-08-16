package com.gpt.product.gpcash.corporate.nonfinancialforbank.controller;

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
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.logging.CorporateActivityLogActivityType;
import com.gpt.product.gpcash.corporate.transactionstatus.TransactionStatus;

@RestController
public class NonFinancialForBankController extends BankBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_BO_RPT_CORP_NON_FIN";
	
	@Autowired
	private MessageSource message;
	
	private Map<Locale, List<Map<String, String>>> trxStatus = new ConcurrentHashMap<>();
	
	private Map<Locale, List<Map<String, String>>> menuType = new ConcurrentHashMap<>();
	
	private Map<Locale, List<Map<String, String>>> activityType = new ConcurrentHashMap<>();
	
	private Map<Locale, List<Map<String, String>>> status = new ConcurrentHashMap<>();

	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("NonFinancialForBankSC", method, param);
	}
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/getTransactionStatus")
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
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/getMenuType")
	public List<Map<String, String>> getMenuType(Locale locale) {
		List<Map<String, String>> result = menuType.get(locale);
		
		if(result == null) {
			result = new ArrayList<>(3);
			
			Map<String, String> vals = new HashMap<>(2, 1);
			vals.put("key", ApplicationConstants.NON_FIN_ALL_MENU);
			vals.put("value", message.getMessage(ApplicationConstants.NON_FIN_ALL_MENU, null, locale));
			result.add(vals);
			
			vals = new HashMap<>(2, 1);
			vals.put("key", ApplicationConstants.NON_FIN_ADMIN_MENU);
			vals.put("value", message.getMessage(ApplicationConstants.NON_FIN_ADMIN_MENU, null, locale));
			result.add(vals);
			
			vals = new HashMap<>(2, 1);
			vals.put("key", ApplicationConstants.NON_FIN_USER_MENU);
			vals.put("value", message.getMessage(ApplicationConstants.NON_FIN_USER_MENU, null, locale));
			result.add(vals);
			
			menuType.put(locale, result);
		}
		return result;
	}
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/getActivityType")
	public List<Map<String, String>> getActivityType(Locale locale) {
		List<Map<String, String>> result = activityType.get(locale);
		if(result == null) {
			CorporateActivityLogActivityType[] values = CorporateActivityLogActivityType.values();
			result = new ArrayList<>(values.length);
			
			for(CorporateActivityLogActivityType value : values) {
				Map<String, String> vals = new HashMap<>(2, 1);
				vals.put("key", value.name());
				vals.put("value", message.getMessage(value.name(), null, locale));
				result.add(vals);
			}
			activityType.put(locale, result);
		}
		
		return result;
	}
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/getStatus")
	public List<Map<String, String>> getStatus(Locale locale) {
		List<Map<String, String>> result = status.get(locale);
		
		if(result == null) {
			result = new ArrayList<>(3);
			
			Map<String, String> vals = new HashMap<>(2, 1);
			vals.put("key", ApplicationConstants.ALL_STS);
			vals.put("value", message.getMessage(ApplicationConstants.ALL_STS, null, locale));
			result.add(vals);
			
			vals = new HashMap<>(2, 1);
			vals.put("key", ApplicationConstants.FAILED_STS);
			vals.put("value", message.getMessage(ApplicationConstants.FAILED_STS, null, locale));
			result.add(vals);
			
			vals = new HashMap<>(2, 1);
			vals.put("key", ApplicationConstants.SUCCESS_STS);
			vals.put("value", message.getMessage(ApplicationConstants.SUCCESS_STS, null, locale));
			result.add(vals);
			
			status.put(locale, result);
		}
		return result;
	}
}
