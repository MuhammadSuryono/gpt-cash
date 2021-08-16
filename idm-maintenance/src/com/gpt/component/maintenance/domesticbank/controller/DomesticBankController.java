package com.gpt.component.maintenance.domesticbank.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.BankBaseController;

@RestController
public class DomesticBankController extends BankBaseController {
	
	protected static final String menuCode = "MNU_GPCASH_MT_DOM_BANK";

	@RequestMapping(baseBankUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(@PathVariable String method, @RequestBody Map<String, Object> param, HttpServletRequest req, HttpServletResponse res) {
		return invoke("DomesticBankSC", method, param);
	}
	
}
