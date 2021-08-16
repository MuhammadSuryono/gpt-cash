package com.gpt.product.gpcash.corporate.transaction.tax.ntpninquiry.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.transaction.tax.mpn.model.MPNModel;
import com.gpt.product.gpcash.corporate.transaction.tax.mpn.repository.MPNRepository;
import com.gpt.product.gpcash.corporate.transaction.tax.ntpninquiry.constants.NTPNInquiryConstants;
import com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.model.TaxPaymentModel;
import com.gpt.product.gpcash.corporate.transaction.tax.taxpayment.repository.TaxPaymentRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class NTPNInquiryServiceImpl implements NTPNInquiryService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Autowired
	private TaxPaymentRepository taxPaymentRepo;
	
	@Autowired
	private MPNRepository MPNRepo;


	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<TaxPaymentModel> result = taxPaymentRepo.findPendingNTP(PagingUtils.createPageRequest(map));
			Page<MPNModel> resultMPN = MPNRepo.findPendingNTP(PagingUtils.createPageRequest(map));
			resultMap.put("result", setModelToMap(result.getContent(), resultMPN.getContent()));
			
			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<TaxPaymentModel> list, List<MPNModel> listMPN) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (TaxPaymentModel model : list) {
			resultList.add(setModelToMap(model));
		}
		
		for (MPNModel model : listMPN) {
			resultList.add(setMPNModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(TaxPaymentModel model) throws Exception {
		Map<String, Object> map = new HashMap<>();
		
		map.put("taxPaymentId", model.getId());
		
		CorporateModel corporate = model.getCorporate();
		map.put("corporateId", corporate.getId());
		map.put("corporateName", corporate.getName());
		
		map.put("paymentDate", model.getPaymentDate());
		map.put("referenceNo", model.getReferenceNo());
		map.put("npwp", model.getNpwpNo());
		map.put("benName", model.getBenName());
		
		AccountModel account = model.getSourceAccount();
		map.put("accountNo", account.getAccountNo());
		map.put("accountName", account.getAccountName());
		map.put("accountCurrencyCode", account.getCurrency().getCode());
		
		map.put("ntb", model.getNtb());

		return map;
	}

	@Override
	public Map<String, Object> NTPNInquiry(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {			
			String taxPaymentId = (String) map.get("taxPaymentId");
			
			Object taxPayment = new Object();
			taxPayment = taxPaymentRepo.findOne(taxPaymentId);
			if(taxPayment == null) {
				taxPayment = MPNRepo.findOne(taxPaymentId);
			}
			String className = taxPayment.getClass().getName();
			
			Map<String, Object> inputs = prepareInputsForEAI(taxPayment, 
					(String)map.get(ApplicationConstants.APP_CODE));
			Map<String, Object> result = eaiAdapter.invokeService(NTPNInquiryConstants.EAI_NTPN_INQUIRY, inputs);
			
			if(ValueUtils.hasValue(result.get("ntp"))) {
				//update ntp from host to table
				if(TaxPaymentModel.class.getName().equals(className)) {
					TaxPaymentModel taxPaymentModel = (TaxPaymentModel) taxPayment;
					taxPaymentModel.setNtp((String) result.get("ntp"));
					taxPaymentRepo.save(taxPaymentModel);
				} else if (MPNModel.class.getName().equals(className)) {
					MPNModel model = (MPNModel) taxPayment;
					model.setNtpn((String) result.get("ntp"));
					MPNRepo.save(model);
				}
			}
			
			map.put(ApplicationConstants.WF_FIELD_MESSAGE, ApplicationConstants.SUCCESS_STS);
			
			return map;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> NTPNInquiryAll(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Page<TaxPaymentModel> taxPaymentList = taxPaymentRepo.findPendingNTP(null);
			Page<MPNModel> resultMPNList = MPNRepo.findPendingNTP(null);
			
			for(TaxPaymentModel taxPayment : taxPaymentList) {
				Map<String, Object> inputs = prepareInputsForEAI(taxPayment, 
						(String)map.get(ApplicationConstants.APP_CODE));
				
				Map<String, Object> result = eaiAdapter.invokeService(NTPNInquiryConstants.EAI_NTPN_INQUIRY, inputs);
				if(ValueUtils.hasValue(result.get("ntp"))) {
					//update ntp from host to table
					taxPayment.setNtp((String) result.get("ntp"));
					taxPaymentRepo.save(taxPayment);
				}
			}
			
			for(MPNModel mpn : resultMPNList) {
				Map<String, Object> inputs = prepareInputsForEAI(mpn, 
						(String)map.get(ApplicationConstants.APP_CODE));
				
				Map<String, Object> result = eaiAdapter.invokeService(NTPNInquiryConstants.EAI_NTPN_INQUIRY, inputs);
				if(ValueUtils.hasValue(result.get("ntp"))) {
					//update ntp from host to table
					mpn.setNtpn((String) result.get("ntp"));
					MPNRepo.save(mpn);
				}
			}
			
			map.put(ApplicationConstants.WF_FIELD_MESSAGE, ApplicationConstants.SUCCESS_STS);
			
			return map;
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private Map<String, Object> prepareInputsForEAI(Object model, String appCode) {
		Map<String, Object> inputs = new HashMap<>();
		
		AccountModel sourceAccountModel = new AccountModel();
		String billingId = ApplicationConstants.EMPTY_STRING;
		String ntb = ApplicationConstants.EMPTY_STRING;
		
		if (TaxPaymentModel.class.equals(model.getClass())){
			TaxPaymentModel taxPaymentModel = (TaxPaymentModel) model;
			sourceAccountModel = taxPaymentModel.getSourceAccount();
			billingId = taxPaymentModel.getBillingId();
			ntb = taxPaymentModel.getNtb();
		} else if (MPNModel.class.equals(model.getClass())) {
			MPNModel taxPaymentModel = (MPNModel) model;
			sourceAccountModel = taxPaymentModel.getSourceAccount();
			billingId = taxPaymentModel.getBillingId();
			ntb = taxPaymentModel.getNtb();
		}
		
		inputs.put("billingId", billingId);
		inputs.put("ntb", ntb);
		inputs.put("sourceAccount", sourceAccountModel.getAccountNo());
		
		if(logger.isDebugEnabled()) {
			logger.debug("\n\n\n\n\n");
			logger.debug("inputs : " + inputs);
			logger.debug("\n\n\n\n\n");
		}
		
		return inputs;
		
	}
	
	private Map<String, Object> setMPNModelToMap(MPNModel model) throws Exception {
		Map<String, Object> map = new HashMap<>();
		
		map.put("taxPaymentId", model.getId());
		
		CorporateModel corporate = model.getCorporate();
		map.put("corporateId", corporate.getId());
		map.put("corporateName", corporate.getName());
		
		map.put("paymentDate", model.getPaymentDate());
		map.put("referenceNo", model.getReferenceNo());
		map.put("npwp", model.getNpwpNo());
		map.put("benName", model.getBenName());
		
		AccountModel account = model.getSourceAccount();
		map.put("accountNo", account.getAccountNo());
		map.put("accountName", account.getAccountName());
		map.put("accountCurrencyCode", account.getCurrency().getCode());
		
		map.put("ntb", model.getNtb());
		
		return map;
	}

}
