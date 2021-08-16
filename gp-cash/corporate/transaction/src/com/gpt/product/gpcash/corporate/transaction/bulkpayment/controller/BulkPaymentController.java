package com.gpt.product.gpcash.corporate.transaction.bulkpayment.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
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
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;

@RestController
public class BulkPaymentController extends CorporateUserBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_MASS_FUND_BULK_PAYMENT";
	
	@Value("${gpcash.bulkpayment.download.sample.path}")
	private String pathDownloadSampleFile;
	
	@Value("${gpcash.bulkpayment.download.sample.filename}")
	private String downloadSampleFileName;
	
	@Value("${gpcash.bulkpayment.upload.path}")
	private String pathUpload;
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {	
		return invoke("BulkPaymentSC", method, param);
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
		
		return invoke("BulkPaymentSC", "upload", (DeferredResult<Map<String, Object>> deferredResult, Map<String, Object> map) -> {
			// set session
//			HttpSession session = request.getSession(false);
//			session.setAttribute("uploadDateTime", map.get("uploadDateTime"));
			
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
//		HttpSession session = request.getSession(false);
//		param.put("uploadDateTime", session.getAttribute("uploadDateTime"));
		
		String filename = (String) request.getSession().getAttribute(ApplicationConstants.FILENAME);
		param.put(ApplicationConstants.FILENAME, filename);
		
		param.put("uploadDateTime", DateUtils.getCurrentTimestamp());
		param.put(ApplicationConstants.EAI_SERVICE_NAME, EAIConstants.PARSE_CUSTOMER_BULK);
		param.put(ApplicationConstants.PATH_UPLOAD, pathUpload);
		return invoke("PendingUploadSC", "submitBucket", param);
	}

	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/searchPendingUpload")
	public DeferredResult<Map<String, Object>> searchPendingUpload(HttpServletRequest request, @RequestBody Map<String, Object> param) {		
		return invoke("PendingUploadSC", "searchPendingUpload", param);
	}
	
	@RequestMapping(path = baseCorpUserUrl + "/" + menuCode + "/downloadTransactionStatus", method = RequestMethod.POST)
	public DeferredResult<Map<String, Object>> downloadPeriodicTransaction(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
		return invoke("BulkPaymentSC", "downloadTransactionStatus", 
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
	
}
