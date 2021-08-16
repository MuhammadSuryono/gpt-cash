package com.gpt.product.gpcash.corporate.report.log.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class DownloadLogController extends BankBaseController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected static final String menuCode = "MNU_GPCASH_DOWNLOAD_LOG";
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("DownloadLogSC", method, param);
	}
	
	
	@RequestMapping(path = baseBankUrl + "/" + menuCode + "/downloadReport", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadReport(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("DownloadLogSC", "downloadReport", 
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
}
