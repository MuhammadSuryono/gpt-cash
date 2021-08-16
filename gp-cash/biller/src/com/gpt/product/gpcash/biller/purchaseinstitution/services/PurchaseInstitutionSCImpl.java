package com.gpt.product.gpcash.biller.purchaseinstitution.services;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.component.logging.annotation.EnableActivityLog;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;

@Validate
@Service
public class PurchaseInstitutionSCImpl implements PurchaseInstitutionSC {

	@Autowired
	private PurchaseInstitutionService purchaseInstitutionService;

	@EnableActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = ApplicationConstants.STR_CODE, required = false, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_NAME, required = false),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_SEARCH,
			ApplicationConstants.WF_ACTION_DETAIL 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		return purchaseInstitutionService.search(map);
	}

	@EnableActivityLog
	@Validate
	@Input({ 
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.STR_NAME), 
		@Variable(name = "dscp"),
		@Variable(name = "institutionType", options = { 
				ApplicationConstants.INSTITUTION_TYPE_FIX,
				ApplicationConstants.INSTITUTION_TYPE_ANY
		}),
		@Variable(name = "amountType", options = { 
			ApplicationConstants.INSTITUTION_AMOUNT_TYPE_FIX_FULL_ALL,
			ApplicationConstants.INSTITUTION_AMOUNT_TYPE_FIX_FULL_PER,
			ApplicationConstants.INSTITUTION_AMOUNT_TYPE_USER_INPUT
		}),
		@Variable(name = "billOnlineFlag", options = { ApplicationConstants.YES, ApplicationConstants.NO }),
		@Variable(name = "charges", type = BigDecimal.class),
		@Variable(name = "purchaseInstitutionType"),
		@Variable(name = "name1"),
		@Variable(name = "name2", required = false),
		@Variable(name = "name3", required = false),
		@Variable(name = "name4", required = false),
		@Variable(name = "name5", required = false),
		@Variable(name = "nameEng1"),
		@Variable(name = "nameEng2", required = false),
		@Variable(name = "nameEng3", required = false),
		@Variable(name = "nameEng4", required = false),
		@Variable(name = "nameEng5", required = false),
		@Variable(name = "regex1"),
		@Variable(name = "regex2", required = false),
		@Variable(name = "regex3", required = false),
		@Variable(name = "regex4", required = false),
		@Variable(name = "regex5", required = false),
		@Variable(name = "purchaseInstitutionCategoryCode"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
			ApplicationConstants.WF_ACTION_CREATE,
			ApplicationConstants.WF_ACTION_UPDATE 
		}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		return purchaseInstitutionService.submit(map);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return purchaseInstitutionService.approve(vo);
	}

	@EnableActivityLog
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		return purchaseInstitutionService.reject(vo);
	}

	@EnableActivityLog
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.STR_CODE, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DELETE }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})
	@Override
	public Map<String, Object> submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		return purchaseInstitutionService.submit(map);
	}

}