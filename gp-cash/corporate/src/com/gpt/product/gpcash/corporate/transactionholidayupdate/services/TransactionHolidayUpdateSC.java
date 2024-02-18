package com.gpt.product.gpcash.corporate.transactionholidayupdate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;

@AutoDiscoveryImpl
public interface TransactionHolidayUpdateSC extends WorkflowService{
	String menuCode = "MNU_GPCASH_BO_TRX_HOLIDAY_UPDATE";
	
	Map<String, Object> searchHolidayTransactionByReferenceNo(Map<String, Object> map)
			throws ApplicationException, BusinessException, Exception;
	
	Map<String, Object> detailPendingTask(Map<String, Object> map) throws ApplicationException, BusinessException;

}
