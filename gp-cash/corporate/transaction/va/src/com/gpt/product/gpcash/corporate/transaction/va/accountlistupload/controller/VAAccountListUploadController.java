package com.gpt.product.gpcash.corporate.transaction.va.accountlistupload.controller;

import java.io.File;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import com.gpt.component.base.controller.CorporateUserBaseController;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.transaction.va.accountlistupload.constants.VAAccountListUploadConstants;


@RestController
public class VAAccountListUploadController extends CorporateUserBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_CORP_VA_UPLOAD";
	
	@Value("${gpcash.vaaccountlistupload.download.sample.path}")
	private String pathDownloadSampleFile;
	
	@Value("${gpcash.vaaccountlistupload.download.sample.filename}")
	private String downloadSampleFileName;	
	
	@Value("${gpcash.vaaccountlistupload.upload.path}")
	private String pathUpload;
	
	@Autowired
	private MessageSource message;
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("VAAccountListUploadSC", method, param);
	}	

	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/upload")
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
		
		return invoke("VAAccountListUploadSC", "upload", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			
			String fileName = (String) map.get(ApplicationConstants.FILENAME);
			
			Map<String, Object> result = new HashMap<>();
			result.put("fileId", map.get("fileId"));
			result.put(ApplicationConstants.FILENAME, fileName);
			result.put("uploadDateTime", DateUtils.getCurrentTimestamp());
			
			HttpSession session = request.getSession(false);
			session.setAttribute(ApplicationConstants.FILENAME, fileName);
			
			deferredResult.setResult(result);
		}, this::defaultOnException, param);
	}
	
	@RequestMapping(path = baseCorpUserUrl + "/" + menuCode + "/downloadSample", method = RequestMethod.GET, produces = {"text/csv", 
			"text/plain", "application/octet-stream"} )
	public byte[] downloadSample(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("fileFormat") String fileFormat) throws IOException {
		String filename = downloadSampleFileName + "." + fileFormat.toLowerCase();
		Path path = Paths.get(pathDownloadSampleFile + File.separator + filename);
			
		String mimeType = request.getServletContext().getMimeType(filename);
		if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
		
        response.setContentType(mimeType);
		response.addHeader("Content-Disposition","attachment;filename="+ filename);
		
		return Files.readAllBytes(path);
	}
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/submitBucket")
	public DeferredResult<Map<String, Object>> submit(HttpServletRequest request, @RequestBody Map<String, Object> param) {
		param.put("uploadDateTime", DateUtils.getCurrentTimestamp());
		
		String filename = (String) request.getSession().getAttribute(ApplicationConstants.FILENAME);		
		param.put(ApplicationConstants.FILENAME, filename);
		param.put(ApplicationConstants.EAI_SERVICE_NAME, VAAccountListUploadConstants.EAI_PARSE_CUSTOMER_VA_ACCOUNT_LIST_UPLOAD);
		param.put(ApplicationConstants.PATH_UPLOAD, pathUpload);
		return invoke("VAAccountListUploadSC", "submitBucket", param);
	}

	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/searchPendingUpload")
	public DeferredResult<Map<String, Object>> searchPendingUpload(HttpServletRequest request, @RequestBody Map<String, Object> param) {		
		return invoke("PendingUploadSC", "searchPendingUpload", param);
	}
	
	/*@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/getVATypeForDroplist")
	public Map<String, Object> getVATypeForDroplist(@RequestBody Map<String, Object> param) {
		Map<String, Object> resultMap = new HashMap<>(2,1);
		
		Locale locale = LocaleContextHolder.getLocale();
		
		List<Map<String, Object>> vaTypeList = new ArrayList<>();
		
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("typeCode", "O");
		dataMap.put("typeName", message.getMessage(VAAccountListUploadConstants.VA_TYPE_OPEN, null, VAAccountListUploadConstants.VA_TYPE_OPEN, locale));
		vaTypeList.add(dataMap);
		
		dataMap = new HashMap<>();
		dataMap.put("typeCode", "F");
		dataMap.put("typeName", message.getMessage(VAAccountListUploadConstants.VA_TYPE_FIX, null, VAAccountListUploadConstants.VA_TYPE_FIX, locale));
		vaTypeList.add(dataMap);
		
		resultMap.put("result", vaTypeList);
		return resultMap;
	}*/
	
}
