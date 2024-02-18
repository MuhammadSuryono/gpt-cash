package com.gpt.product.gpcash.corporate.inquiry.newsinfo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateUserBaseController;

@RestController
public class NewsInformationController extends CorporateUserBaseController {

	protected static final String menuCode = "MNU_GPCASH_F_NEWS_INFO";
	
	@Value("${gpcash.helpdesk.upload.path}")
	private String pathUpload;

	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("NewsInformationSC", method, param);
	}
	
	@RequestMapping(path = baseCorpUserUrl + "/" + menuCode + "/downloadFile", method = RequestMethod.GET)
	public byte[] downloadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam("fileName") String fileName) throws IOException {	
//		String filename = fileName + "." + fileFormat.toLowerCase();
		Path path = Paths.get(pathUpload + File.separator + fileName);
			
		String mimeType = request.getServletContext().getMimeType(fileName);
		if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
		
        response.setContentType(mimeType);
		response.addHeader("Content-Disposition","attachment;filename="+ fileName);
		
		return Files.readAllBytes(path);
	}
	
}
