package com.gpt.product.gpcash.corporate.transactionupdate.services;

import java.util.Map;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.invoker.discovery.AutoDiscoveryImpl;
import com.gpt.component.pendingtask.services.WorkflowService;
import com.gpt.product.gpcash.corporate.token.tokenuser.model.TokenUserModel;

@AutoDiscoveryImpl
public interface TransactionUpdateService extends WorkflowService {	
}
