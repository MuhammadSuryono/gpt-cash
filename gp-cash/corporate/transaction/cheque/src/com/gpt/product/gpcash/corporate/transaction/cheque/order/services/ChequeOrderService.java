package com.gpt.product.gpcash.corporate.transaction.cheque.order.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserWorkflowService;

@AutoDiscoveryImpl
public interface ChequeOrderService extends CorporateUserWorkflowService {
	Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> detailExecutedTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> cancelTransaction(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> downloadTransactionStatus(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> findDetailByOrderNo(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchForCorporate(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchForBank(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> process(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> submitProcess(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> decline(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> handover(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> confirmProcess(Map<String, Object> map) throws ApplicationException, BusinessException;

	Map<String, Object> searchOnlineForCorporate(Map<String, Object> map)
			throws ApplicationException, BusinessException;

	Map<String, Object> findOnlineBySerialNo(Map<String, Object> map) throws ApplicationException, BusinessException;
}