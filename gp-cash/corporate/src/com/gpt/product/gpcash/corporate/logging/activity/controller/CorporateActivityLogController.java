package com.gpt.product.gpcash.corporate.logging.activity.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateAdminBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.logging.CorporateActivityLogActivityType;

@RestController
public class CorporateActivityLogController extends CorporateAdminBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_NON_FIN";
	
	@Autowired
	private MessageSource message;

	private Map<Locale, List<Map<String, String>>> menuType = new ConcurrentHashMap<>();

	private Map<Locale, List<Map<String, String>>> activityType = new ConcurrentHashMap<>();

	private Map<Locale, List<Map<String, String>>> status = new ConcurrentHashMap<>();
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CorporateActivityLogSC", method, param);
	}
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/getMenuType")
	public List<Map<String, String>> getMenuType(Locale locale) {
		List<Map<String, String>> result = menuType.get(locale);

		if (result == null) {
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

	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/getActivityType")
	public List<Map<String, String>> getActivityType(Locale locale) {
		List<Map<String, String>> result = activityType.get(locale);
		if (result == null) {
			CorporateActivityLogActivityType[] values = CorporateActivityLogActivityType.values();
			result = new ArrayList<>(values.length);

			for (CorporateActivityLogActivityType value : values) {
				Map<String, String> vals = new HashMap<>(2, 1);
				vals.put("key", value.name());
				vals.put("value", message.getMessage(value.name(), null, locale));
				result.add(vals);
			}
			activityType.put(locale, result);
		}

		return result;
	}

	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/getStatus")
	public List<Map<String, String>> getStatus(Locale locale) {
		List<Map<String, String>> result = status.get(locale);

		if (result == null) {
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
	
	@RequestMapping(path = baseCorpAdminUrl + "/" + menuCode + "/downloadActivity", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadPeriodicTransaction(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("CorporateActivityLogSC", "downloadActivity", 
				(DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
					
					HttpSession session = request.getSession(false);
					session.setAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE, (String) map.get(ApplicationConstants.FILENAME));
					
					deferredResult.setResult(param);
				}, this::defaultOnException, param);		
	}
	
	@RequestMapping(path = baseCorpAdminUrl + "/" + menuCode + "/download", method = RequestMethod.GET)
	public byte[] download(HttpServletRequest request, HttpServletResponse response) throws IOException {	
		Path filePath = Paths.get((String) request.getSession().getAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE));
		String filename = filePath.getFileName().toString();
			
		String mimeType = request.getServletContext().getMimeType(filename);
		if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
		
        response.setContentType(mimeType);
		response.addHeader("Content-Disposition","attachment;filename="+ filename);
		
		return Files.readAllBytes(filePath);
	}
}