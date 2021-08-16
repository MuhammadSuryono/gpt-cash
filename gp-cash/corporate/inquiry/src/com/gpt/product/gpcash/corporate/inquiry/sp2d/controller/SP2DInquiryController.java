package com.gpt.product.gpcash.corporate.inquiry.sp2d.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateUserBaseController;

@RestController
public class SP2DInquiryController extends CorporateUserBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_SP2D_INQ";
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("SP2DInquirySC", method, param);
	}	

}
