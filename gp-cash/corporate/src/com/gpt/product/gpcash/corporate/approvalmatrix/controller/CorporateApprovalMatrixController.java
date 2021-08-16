package com.gpt.product.gpcash.corporate.approvalmatrix.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.gpt.component.base.controller.CorporateAdminBaseController;

@RestController
public class CorporateApprovalMatrixController extends CorporateAdminBaseController {

	protected static final String menuCode = "MNU_GPCASH_F_APRV_MTRX";
	
	@RequestMapping(baseCorpAdminUrl + "/" + menuCode + "/{method}")
	public DeferredResult<Map<String, Object>> genericHandler(HttpServletRequest request, @PathVariable String method, @RequestBody Map<String, Object> param) {
		return invoke("CorporateApprovalMatrixSC", method, param);
	}
	
}