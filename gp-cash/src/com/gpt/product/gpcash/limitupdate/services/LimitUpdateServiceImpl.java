package com.gpt.product.gpcash.limitupdate.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageDetailModel;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;
import com.gpt.product.gpcash.limitpackage.repository.LimitPackageRepository;
import com.gpt.product.gpcash.limitpackage.spi.LimitPackageListener;
import com.gpt.product.gpcash.servicecurrencymatrix.model.ServiceCurrencyMatrixModel;
import com.gpt.product.gpcash.servicecurrencymatrix.repository.ServiceCurrencyMatrixRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class LimitUpdateServiceImpl implements LimitUpdateService {
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private LimitPackageRepository limitPackageRepo;
	
	@Autowired
	private ServiceCurrencyMatrixRepository serviceCurrencyMatrixRepo;
	
	private List<LimitPackageListener> limitPackageListener;
	
	@Autowired(required=false)
	public void setUpdateListeners(List<LimitPackageListener> limitPackageListener) {
		this.limitPackageListener = limitPackageListener;
	}
	
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);
			
			vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);
			resultMap.putAll(pendingTaskService.savePendingTask(vo)); 
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("LimitUpdateSC");

		return vo;
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String isUpdateAll = (String) map.get("isUpdateAll");
			String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERID);
			
			List<Map<String, Object>> list = (List<Map<String,Object>>) map.get("list");
			
			if (ApplicationConstants.YES.equals(isUpdateAll)) {
				String isUpdateCorporateWithSpecialLimit = (String) map.get("isUpdateCorporateWithSpecialLimit");
				
				for(Map<String, Object> limitMap : list) {
					String serviceCurrencyMatrixId = (String) limitMap.get("serviceCurrencyMatrixId");
					String currencyCode = (String) limitMap.get("currencyCode");
					int maxTrxPerDay = (Integer) limitMap.get("maxTrxPerDay");
					BigDecimal maxTrxAmountPerDay = new BigDecimal(String.valueOf(limitMap.get("maxTrxAmountPerDay")));
					
					//insert new
					List<LimitPackageModel> limitPackageNotHaveList = limitPackageRepo.findLimitPackageNotHaveServiceCurrency(serviceCurrencyMatrixId);
					for(LimitPackageModel limitPackage : limitPackageNotHaveList) {
						saveNewServiceLimitToLimitPackageAndCorporateLimits(limitPackage, currencyCode, serviceCurrencyMatrixId, maxTrxAmountPerDay, maxTrxPerDay, createdBy);
					}
					
					//update 
					updateServiceLimitToLimitPackageAndCorporateLimits(currencyCode, serviceCurrencyMatrixId, maxTrxAmountPerDay, maxTrxPerDay, createdBy, isUpdateCorporateWithSpecialLimit);
				}
			} else {
				//update spesific
				String limitPackageCode = (String) map.get("code");
				LimitPackageModel limitPackage = limitPackageRepo.findOne(limitPackageCode);
				
				for(Map<String, Object> limitMap : list) {
					String serviceCurrencyMatrixId = (String) limitMap.get("serviceCurrencyMatrixId");
					String currencyCode = (String) limitMap.get("currencyCode");
					int maxTrxPerDay = (Integer) limitMap.get("maxTrxPerDay");
					BigDecimal maxTrxAmountPerDay = new BigDecimal(String.valueOf(limitMap.get("maxTrxAmountPerDay")));
					
					ServiceCurrencyMatrixModel serviceCurrencyMatrix = serviceCurrencyMatrixRepo.findOne(serviceCurrencyMatrixId);
					String serviceCode = serviceCurrencyMatrix.getService().getCode();
					
					//check if service limit already exist or not
					LimitPackageDetailModel detail = limitPackageRepo.findDetailByServiceCurrencyMatrixAndLimitPackage(serviceCurrencyMatrixId, limitPackage.getCode());
					
					if(detail == null) {
						//insert new
						saveNewServiceLimitToLimitPackageAndCorporateLimits(limitPackage, currencyCode, serviceCurrencyMatrixId, maxTrxAmountPerDay, maxTrxPerDay, createdBy);
					} else {
						//update
						updateServiceLimitToLimitPackageAndCorporateLimitsByLimitPackage(currencyCode, serviceCode, serviceCurrencyMatrixId, maxTrxAmountPerDay, maxTrxPerDay, 
								createdBy, limitPackageCode);
					}
					
					
				}
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}
		
	private void saveNewServiceLimitToLimitPackageAndCorporateLimits(LimitPackageModel limitPackage, String currencyCode, String serviceCurrencyMatrixId,
			BigDecimal maxTrxAmountPerDay, int maxTrxPerDay, String createdBy) throws ApplicationException, BusinessException {
		
		LimitPackageDetailModel limitPackageDetail = new LimitPackageDetailModel();

		//currency
		CurrencyModel currency = new CurrencyModel();
		currency.setCode(currencyCode);
		limitPackageDetail.setCurrency(currency);

		//service currency matrix
		ServiceCurrencyMatrixModel serviceCurrency = serviceCurrencyMatrixRepo.findOne(serviceCurrencyMatrixId);
		limitPackageDetail.setServiceCurrencyMatrix(serviceCurrency);
		
		//service
		limitPackageDetail.setService(serviceCurrency.getService());
		
		//occurence
		limitPackageDetail.setMaxOccurrenceLimit(maxTrxPerDay);
		
		//max amount limit
		limitPackageDetail.setMaxAmountLimit(maxTrxAmountPerDay);
		
		limitPackageDetail.setLimitPackage(limitPackage);
		limitPackageDetail.setCreatedBy(createdBy);
		limitPackageDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
		
		limitPackage.getLimitPackageDetail().add(limitPackageDetail);
		limitPackageRepo.save(limitPackage);
		
		//insert new corporate limits by limit package code
		if(limitPackageListener != null) {
			for(LimitPackageListener listener : limitPackageListener) {
				listener.saveNewLimitByLimitPackageCode(maxTrxAmountPerDay, maxTrxPerDay,
						limitPackage.getCode(),
						currencyCode, 
						serviceCurrencyMatrixId, createdBy);
			}
		}
	}
	
	private void updateServiceLimitToLimitPackageAndCorporateLimits(String currencyCode, String serviceLimitId, BigDecimal maxTrxAmountPerDay, int maxTrxPerDay,
			String createdBy, String isUpdateCorporateWithSpecialLimit) throws ApplicationException, BusinessException {
		
		//update existing
		limitPackageRepo.updateAllLimitsByServiceCurrencyMatrix(maxTrxAmountPerDay, maxTrxPerDay, currencyCode, createdBy, DateUtils.getCurrentDate(),serviceLimitId);
		//end update limit package

		//update corporate limits
		if(limitPackageListener != null) {
			for(LimitPackageListener listener : limitPackageListener) {
				listener.updateALLLimit(maxTrxAmountPerDay, maxTrxPerDay,
						currencyCode, 
						serviceLimitId, 
						isUpdateCorporateWithSpecialLimit);
			}
		}
	}
	
	private void updateServiceLimitToLimitPackageAndCorporateLimitsByLimitPackage(String currencyCode, String serviceCode, String serviceLimitId, BigDecimal maxTrxAmountPerDay, int maxTrxPerDay,
			String createdBy, String limitPackageCode) throws ApplicationException, BusinessException {
		
		//update existing
		limitPackageRepo.updateLimitsByServiceCurrencyMatrix(maxTrxAmountPerDay, maxTrxPerDay, currencyCode, createdBy, 
				DateUtils.getCurrentDate(),serviceLimitId, limitPackageCode);
		//end update limit package

		//update corporate limits
		if(limitPackageListener != null) {
			for(LimitPackageListener listener : limitPackageListener) {
				listener.updateByLimitPackageCode(maxTrxAmountPerDay, maxTrxPerDay,
						currencyCode,
						serviceCode,
						serviceLimitId, 
						limitPackageCode);
			}
		}
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

}