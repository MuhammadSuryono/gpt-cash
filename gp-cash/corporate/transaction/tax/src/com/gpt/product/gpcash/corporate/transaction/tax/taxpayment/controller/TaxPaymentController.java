package com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateUserBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.constants.TaxPaymentConstants;

@RestController
public class TaxPaymentController extends CorporateUserBaseController {
	
	@Autowired
	private MessageSource message;
	
	protected static final String menuCode = "MNU_GPCASH_F_TAX_PAYMENT";

	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("TaxPaymentSC", method, param);
	}
	
	@RequestMapping(path = baseCorpUserUrl + "/" + menuCode + "/downloadTransactionStatus", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadPeriodicTransaction(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("TaxPaymentSC", "downloadTransactionStatus", 
				(DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
					
					HttpSession session = request.getSession(false);
					session.setAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE, (String) map.get(ApplicationConstants.FILENAME));
					
					deferredResult.setResult(param);
				}, this::defaultOnException, param);		
	}
	
	@RequestMapping(path = baseCorpUserUrl + "/" + menuCode + "/download", method = RequestMethod.GET)
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
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/getPeriodMonthForDroplist")
	public Map<String, Object> getPeriodMonthForDroplist(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(3,1);
		
		Locale locale = LocaleContextHolder.getLocale();
		
		List<Map<String, Object>> returnList = new ArrayList<>(12);
		
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("code", "01");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.JANUARY, null, TaxPaymentConstants.JANUARY, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "02");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.FEBRUARY, null, TaxPaymentConstants.FEBRUARY, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "03");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.MARCH, null, TaxPaymentConstants.MARCH, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "04");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.APRIL, null, TaxPaymentConstants.APRIL, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "05");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.MAY, null, TaxPaymentConstants.MAY, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "06");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.JUNE, null, TaxPaymentConstants.JUNE, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "07");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.JULY, null, TaxPaymentConstants.JULY, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "08");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.AUGUST, null, TaxPaymentConstants.AUGUST, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "09");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.SEPTEMBER, null, TaxPaymentConstants.SEPTEMBER, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "10");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.OCTOBER, null, TaxPaymentConstants.OCTOBER, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "11");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.NOVEMBER, null, TaxPaymentConstants.NOVEMBER, locale));
		returnList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", "12");
		dataMap.put("name", message.getMessage(TaxPaymentConstants.DECEMBER, null, TaxPaymentConstants.DECEMBER, locale));
		returnList.add(dataMap);
		
		resultMap.put("result", returnList);
		return resultMap;
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/getPeriodYearForDroplist")
	public Map<String, Object> getPeriodYearForDroplist(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(3,1);
		
		
		List<Map<String, Object>> returnList = new ArrayList<>();
		
		/*//sementara				
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		for(int i = 0 ; i <= 1 ; i++){
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("code", currentYear-i);
			dataMap.put("name", currentYear-i);
			returnList.add(dataMap);
		}*/
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		for(int i = currentYear ; i <= currentYear+1 ; i++){
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put("code", i);
			dataMap.put("name", i);
			returnList.add(dataMap);
		}
		
		resultMap.put("result", returnList);
		return resultMap;
	}
	
	
}