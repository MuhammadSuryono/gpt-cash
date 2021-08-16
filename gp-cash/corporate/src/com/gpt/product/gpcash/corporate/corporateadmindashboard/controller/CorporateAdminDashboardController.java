package com.gpt.product.gpcash.corporate.corporateadmindashboard.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import com.gpt.component.base.controller.CorporateAdminBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;

@RestController
public class CorporateAdminDashboardController extends CorporateAdminBaseController {

	protected static final String menuCode = "MNU_GPCASH_F_ADMIN_DASHBOARD";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CorporateAdminDashboardSC", method, param);
	}

	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/uploadAvatar")
	public DeferredResult<Map<String, Object>> upload(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
		
		Map<String, Object> param = new HashMap<>();
		
		if (!file.isEmpty()) {
	        try {
		        param.put("rawdata", file.getBytes());
				param.put(ApplicationConstants.FILENAME, file.getOriginalFilename());
	        } catch (IOException e) {
	        	logger.error("Failed to upload "+e.getMessage(),e);
	        }			
		}
		
		return invoke("CorporateUserDashboardSC", "uploadAvatar", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			Map<String, Object> result = new HashMap<>();
			result.put("fileId", map.get("fileId"));
			result.put(ApplicationConstants.FILENAME, map.get(ApplicationConstants.FILENAME));
			
			deferredResult.setResult(result);
		}, this::defaultOnException, param);
	}
}