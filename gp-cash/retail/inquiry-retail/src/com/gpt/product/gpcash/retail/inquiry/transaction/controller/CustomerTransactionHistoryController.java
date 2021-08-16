package com.gpt.product.gpcash.retail.inquiry.transaction.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import com.gpt.component.base.controller.CustomerUserBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class CustomerTransactionHistoryController extends CustomerUserBaseController {
	
	protected static final String menuCode = "MNU_R_GPCASH_F_TRX_HISTORY";
	
	@Autowired
	private MessageSource message;
	
	@RequestMapping(baseCustUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CustomerTransactionHistorySC", method, param);
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
	
	@RequestMapping(path = baseCustUserUrl + "/" + menuCode + "/downloadPeriodicTransaction", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadPeriodicTransaction(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("CustomerTransactionHistorySC", "downloadPeriodicTransaction", 
				(DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
					
					HttpSession session = request.getSession(false);
					session.setAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE, (String) map.get(ApplicationConstants.FILENAME));
					
					deferredResult.setResult(param);
				}, this::defaultOnException, param);		
	}	
	
	@RequestMapping(path = baseCustUserUrl + "/" + menuCode + "/downloadPeriodicTransactionMultiAccount", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadPeriodicTransactionMultiAccount(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("CustomerTransactionHistorySC", "downloadPeriodicTransactionMultiAccount", 
				(DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
					
					HttpSession session = request.getSession(false);
					session.setAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE, (String) map.get(ApplicationConstants.FILENAME));
					
					deferredResult.setResult(param);
				}, this::defaultOnException, param);		
	}	
	
	@RequestMapping(path = baseCustUserUrl + "/" + menuCode + "/downloadTodayTransaction", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadTodayTransaction(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("CustomerTransactionHistorySC", "downloadTodayTransaction", 
				(DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
					HttpSession session = request.getSession(false);
					session.setAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE, (String) map.get(ApplicationConstants.FILENAME));
					
					deferredResult.setResult(param);
				}, this::defaultOnException, param);		
	}	
	
	@RequestMapping(path = baseCustUserUrl + "/" + menuCode + "/downloadTodayTransactionMultiAccount", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadTodayTransactionMultiAccount(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("CustomerTransactionHistorySC", "downloadTodayTransactionMultiAccount", 
				(DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
					
					HttpSession session = request.getSession(false);
					session.setAttribute(ApplicationConstants.KEY_DOWNLOAD_FILE, (String) map.get(ApplicationConstants.FILENAME));
					
					deferredResult.setResult(param);
				}, this::defaultOnException, param);		
	}
	
	@RequestMapping(baseCustUserUrl + "/" + menuCode + "/fileFormatToday")
	public Map<String, Object> fileFormatToday(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(3,1);
		
		List<Map<String, Object>> supportedFileFormat = new ArrayList<>();
		Map<String, Object> fileFormat = new HashMap<>();
		fileFormat.put("ext", getDownloadFileExtention(ApplicationConstants.FILE_FORMAT_CSV));
		fileFormat.put("name", ApplicationConstants.FILE_FORMAT_CSV);
		supportedFileFormat.add(fileFormat);
		
		fileFormat = new HashMap<>();
		fileFormat.put("ext", getDownloadFileExtention(ApplicationConstants.FILE_FORMAT_TXT));
		fileFormat.put("name", ApplicationConstants.FILE_FORMAT_TXT);
		supportedFileFormat.add(fileFormat);
		
		fileFormat = new HashMap<>();
		fileFormat.put("ext", getDownloadFileExtention(ApplicationConstants.FILE_FORMAT_MT_942));
		fileFormat.put("name", ApplicationConstants.FILE_FORMAT_MT_942);
		supportedFileFormat.add(fileFormat);
		
		fileFormat = new HashMap<>();
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
	
	@RequestMapping(baseCustUserUrl + "/" + menuCode + "/fileFormatPeriodical")
	public Map<String, Object> fileFormatPeriodical(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(3,1);
		
		List<Map<String, Object>> supportedFileFormat = new ArrayList<>();
		Map<String, Object> fileFormat = new HashMap<>();
		fileFormat.put("ext", getDownloadFileExtention(ApplicationConstants.FILE_FORMAT_CSV));
		fileFormat.put("name", ApplicationConstants.FILE_FORMAT_CSV);
		supportedFileFormat.add(fileFormat);
		
		fileFormat = new HashMap<>();
		fileFormat.put("ext", getDownloadFileExtention(ApplicationConstants.FILE_FORMAT_TXT));
		fileFormat.put("name", ApplicationConstants.FILE_FORMAT_TXT);
		supportedFileFormat.add(fileFormat);
		
		fileFormat = new HashMap<>();
		fileFormat.put("ext", getDownloadFileExtention(ApplicationConstants.FILE_FORMAT_MT_940));
		fileFormat.put("name", ApplicationConstants.FILE_FORMAT_MT_940);
		supportedFileFormat.add(fileFormat);
		
		fileFormat = new HashMap<>();
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
		if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_CSV)) {
			return "csv";
		} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_PDF)) {
			return "pdf";
		} else if(fileFormat.equals(ApplicationConstants.FILE_FORMAT_EXCEL)) {
			return "xls";
		}
		
		return "txt";
	}
	
	@RequestMapping(baseCustUserUrl + "/" + menuCode + "/fileSetting")
	public Map<String, Object> fileSetting(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(3,1);
		
		Locale locale = LocaleContextHolder.getLocale();
		
		List<Map<String, Object>> fileSettingList = new ArrayList<>();
		
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("code", ApplicationConstants.FILE_SETTING_SEPARATED);
		dataMap.put("name", message.getMessage(ApplicationConstants.FILE_SETTING_SEPARATED, null, ApplicationConstants.FILE_SETTING_SEPARATED, locale));
		fileSettingList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("code", ApplicationConstants.FILE_SETTING_CONSOLIDATED);
		dataMap.put("name", message.getMessage(ApplicationConstants.FILE_SETTING_CONSOLIDATED, null, ApplicationConstants.FILE_SETTING_CONSOLIDATED, locale));
		fileSettingList.add(dataMap);
		
		resultMap.put("result", fileSettingList);
		return resultMap;
	}
}
