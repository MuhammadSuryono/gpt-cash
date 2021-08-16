package com.gpt.product.gpcash.corporate.inquiry.accountstatement.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateUserBaseController;

@RestController
public class AccountStatementController extends CorporateUserBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_F_ACCT_STATEMENT";
	
	@RequestMapping(baseCorpUserUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("AccountStatementSC", method, param);
	}
	
	@RequestMapping(path = baseCorpUserUrl + "/" + menuCode + "/download", method = RequestMethod.GET)
	public byte[] download(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return null;	
	}

}
