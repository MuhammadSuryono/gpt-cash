package com.gpt.component.maintenance.promo.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import com.gpt.component.base.controller.BankBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class PromoController extends BankBaseController {
	protected static final String menuCode = "MNU_GPCASH_MT_HELPDESK";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${gpcash.helpdesk.upload.path}")
	private String pathUpload;

	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("PromoSC", method, param);
	}
	
	@RequestMapping(baseBankUrl + "/" + menuCode + "/upload")
	public DeferredResult<Map<String, Object>> upload(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
		Map<String, Object> param = new HashMap<>();
		
		if (!file.isEmpty()) {
	        try {
		        param.put("rawdata", file.getBytes());
				param.put(ApplicationConstants.FILENAME, file.getOriginalFilename());
	        } catch (IOException e) {
	        	logger.error("Failed to upload " + e.getMessage(),e);
	        }			
		}
		
		return invoke("PromoSC", "upload", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			// set session
//			HttpSession session = request.getSession(false);
//			session.setAttribute("uploadDateTime", map.get("uploadDateTime"));
			
			String fileName = (String) map.get(ApplicationConstants.FILENAME);
			
			Map<String, Object> result = new HashMap<>();
			result.put("fileId", map.get("fileId"));
			result.put(ApplicationConstants.FILENAME, fileName);
			result.put("uploadDateTime", (Timestamp) map.get("uploadDateTime"));
			
			HttpSession session = request.getSession(false);
			session.setAttribute(ApplicationConstants.FILENAME, fileName);
			
			deferredResult.setResult(result);
		}, this::defaultOnException, param);
	}
	
	
}
