package com.gpt.product.gpcash.retail.transaction.domestic.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CustomerUserBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;

@RestController
public class CustomerDomesticTransferController extends CustomerUserBaseController {

	protected static final String menuCode = "MNU_R_GPCASH_F_FUND_DOMESTIC";
	
	@RequestMapping(baseCustUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CustomerDomesticTransferSC", method, param);
	}
	
	@RequestMapping(baseCustUserUrl + "/" + menuCode + "/confirm")
	public DeferredResult<Map<String, Object>> confirm(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		
		HttpSession session = request.getSession(false);
		session.setAttribute(ApplicationConstants.CONFIRMATION_DATA, param);
		
		logger.debug("param : " + param);
		
		return invoke("CustomerDomesticTransferSC", "confirm", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			Map<String, Object> confirmDataMap = (Map<String, Object>)session.getAttribute(ApplicationConstants.CONFIRMATION_DATA);
			confirmDataMap.putAll(map);
			confirmDataMap.putAll((Map<String, Object>) confirmDataMap.get("benAccountInfo"));
			
			//remove unused data
			confirmDataMap.remove("benAccountInfo");
			confirmDataMap.remove(ApplicationConstants.WF_ACTION);
			
			//put confirmationData for submit
			session.setAttribute(ApplicationConstants.CONFIRMATION_DATA, confirmDataMap);
			
			logger.debug("confirmDataMap : " + confirmDataMap);
			deferredResult.setResult(map);
		}, this::defaultOnException, param);
	}
	
	@RequestMapping(baseCustUserUrl + "/" + menuCode + "/submit")
	public DeferredResult<Map<String, Object>> approve(HttpServletRequest request,
			@RequestBody Map<String, Object> param) {
		HttpSession session = request.getSession(false);
		
		Map<String, Object> confirmationDataMap = (Map<String, Object>) session.getAttribute(ApplicationConstants.CONFIRMATION_DATA);
		param.putAll(confirmationDataMap);
		
		//START count totalDebitedAmount
		BigDecimal totalCharge = BigDecimal.ZERO;
		BigDecimal transactionAmount = BigDecimal.ZERO;

		if(confirmationDataMap.get(ApplicationConstants.TRANS_TOTAL_CHARGE) != null) {
			totalCharge = Helper.getBigDecimalValue(confirmationDataMap.get(ApplicationConstants.TRANS_TOTAL_CHARGE), BigDecimal.ZERO);
		}
		
		if(confirmationDataMap.get(ApplicationConstants.TRANS_AMOUNT) != null) {
			transactionAmount = Helper.getBigDecimalValue(confirmationDataMap.get(ApplicationConstants.TRANS_AMOUNT), BigDecimal.ZERO);
		}
		param.put(ApplicationConstants.TRANS_TOTAL_DEBIT_AMOUNT, transactionAmount.add(totalCharge));
		//END count totalDebitedAmount
		
		//default value
		param.put("chargeInstruction", ApplicationConstants.REMIITER_CHARGES_INSTRUCTION);
		
		return invoke("CustomerDomesticTransferSC", "submit", param);
	}

	@RequestMapping(path = baseCustUserUrl + "/" + menuCode + "/downloadTransactionStatus", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadPeriodicTransaction(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("CustomerDomesticTransferSC", "downloadTransactionStatus", 
				(DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
					
					HttpSession session = request.getSession(false);
					session.setAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE, (String) map.get(ApplicationConstants.FILENAME));
					
					deferredResult.setResult(param);
				}, this::defaultOnException, param);		
	}
	
	@RequestMapping(path = baseCustUserUrl + "/" + menuCode + "/download", method = RequestMethod.GET)
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
