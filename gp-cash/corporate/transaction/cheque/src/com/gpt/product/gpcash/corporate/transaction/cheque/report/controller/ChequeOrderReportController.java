package com.gpt.product.gpcash.corporate.transaction.cheque.report.controller;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.corporate.transaction.cheque.constants.ChequeConstants;

@RestController
public class ChequeOrderReportController extends BankBaseController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected static final String menuCode = "MNU_GPCASH_BO_RPT_CHQ_ORDER";
	
	@Autowired
	private MessageSource message;
	
	private Map<Locale, List<Map<String, String>>> status = new ConcurrentHashMap<>();
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("ChequeOrderReportSC", method, param);
	}
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/fileFormat")
	public Map<String, Object> fileFormatToday(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(3,1);
		
		List<Map<String, Object>> supportedFileFormat = new ArrayList<>();
		Map<String, Object> fileFormat = new HashMap<>();
		fileFormat.put("ext", getDownloadFileExtention(ApplicationConstants.FILE_FORMAT_PDF));
		fileFormat.put("name", ApplicationConstants.FILE_FORMAT_PDF);
		supportedFileFormat.add(fileFormat);
		
		fileFormat = new HashMap<>();
		fileFormat.put("ext", getDownloadFileExtention(ApplicationConstants.FILE_FORMAT_EXCEL));
		fileFormat.put("name", ApplicationConstants.FILE_FORMAT_EXCEL);
		supportedFileFormat.add(fileFormat);
		
		resultMap.put("fileFormats", supportedFileFormat);
		
		return resultMap;
	}
	
	private String getDownloadFileExtention(String fileFormat) {
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF)) {
			return "pdf";
		} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			return "xls";
		}
		
		return "txt";
	}
	
	@RequestMapping(path = baseBankUrl + "/" + menuCode + "/downloadReport", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadReport(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("ChequeOrderReportSC", "downloadReport", 
				(DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
					HttpSession session = request.getSession(false);
					if(logger.isDebugEnabled()) {
						logger.debug("fileName : " + map.get(ApplicationConstants.FILENAME));
					}
					session.setAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE, (String) map.get(ApplicationConstants.FILENAME));
					
					deferredResult.setResult(param);
				}, this::defaultOnException, param);		
	}
	
	@RequestMapping(path = baseBankUrl + "/" + menuCode + "/download", method = RequestMethod.GET)
	public byte[] download(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String fileName = (String) request.getSession().getAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE);
		if(logger.isDebugEnabled()) {
			logger.debug("fileName : " + fileName);
		}
		Path filePath = Paths.get(fileName);
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
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/getStatus")
	public List<Map<String, String>> getStatusStatus(Locale locale) {
		List<Map<String, String>> result = status.get(locale);
		if(result == null) {
			result = new ArrayList<>(5);
			
			Map<String, String> vals = new HashMap<>(2, 1);
			vals.put("key", ChequeConstants.CHQ_STS_NEW_REQUEST);
			vals.put("value", message.getMessage(ChequeConstants.CHQ_STS_NEW_REQUEST, null, locale));
			result.add(vals);
			
			vals = new HashMap<>(2, 1);
			vals.put("key", ChequeConstants.CHQ_STS_READY);
			vals.put("value", message.getMessage(ChequeConstants.CHQ_STS_READY, null, locale));
			result.add(vals);
			
			vals = new HashMap<>(2, 1);
			vals.put("key", ChequeConstants.CHQ_STS_PICKED_UP);
			vals.put("value", message.getMessage(ChequeConstants.CHQ_STS_PICKED_UP, null, locale));
			result.add(vals);
			
			vals = new HashMap<>(2, 1);
			vals.put("key", ChequeConstants.CHQ_STS_DECLINED);
			vals.put("value", message.getMessage(ChequeConstants.CHQ_STS_DECLINED, null, locale));
			result.add(vals);
			
			vals = new HashMap<>(2, 1);
			vals.put("key", ChequeConstants.CHQ_STS_EXPIRED);
			vals.put("value", message.getMessage(ChequeConstants.CHQ_STS_EXPIRED, null, locale));
			result.add(vals);
			
			status.put(locale, result);
		}
		
		return result;
	}
}