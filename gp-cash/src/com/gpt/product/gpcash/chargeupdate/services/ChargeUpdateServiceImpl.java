package com.gpt.product.gpcash.chargeupdate.services;

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
import com.gpt.product.gpcash.chargepackage.model.ChargePackageDetailModel;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;
import com.gpt.product.gpcash.chargepackage.repository.ChargePackageRepository;
import com.gpt.product.gpcash.chargepackage.spi.ChargePackageListener;
import com.gpt.product.gpcash.servicecharge.model.ServiceChargeModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class ChargeUpdateServiceImpl implements ChargeUpdateService {
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private ChargePackageRepository chargePackageRepo;
	
	private List<ChargePackageListener> chargePackageListener;
	
	@Autowired(required=false)
	public void setUpdateListeners(List<ChargePackageListener> chargePackageListener) {
		this.chargePackageListener = chargePackageListener;
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
		vo.setService("ChargeUpdateSC");

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
				String isUpdateCorporateWithSpecialCharge = (String) map.get("isUpdateCorporateWithSpecialCharge");
				
				for(Map<String, Object> chargeMap : list) {
					String serviceChargeId = (String) chargeMap.get("serviceChargeId");
					String currencyCode = (String) chargeMap.get("currencyCode");
					BigDecimal value = new BigDecimal(String.valueOf(chargeMap.get("value")));
					
					//insert new
					List<ChargePackageModel> chargePackageNotHaveList = chargePackageRepo.findChargePackageNotHaveServiceCharge(serviceChargeId);
					for(ChargePackageModel chargePackage : chargePackageNotHaveList) {
						saveNewServiceChargeToChargePackageAndCorporateCharges(chargePackage, currencyCode, serviceChargeId, value, createdBy);
					}
					
					//update 
					updateServiceChargeToChargePackageAndCorporateCharges(currencyCode, serviceChargeId, value, createdBy, isUpdateCorporateWithSpecialCharge);
				}
			} else {
				//update spesific
				String chargePackageCode = (String) map.get("code");
				ChargePackageModel chargePackage = chargePackageRepo.findOne(chargePackageCode);
				
				for(Map<String, Object> chargeMap : list) {
					String serviceChargeId = (String) chargeMap.get("serviceChargeId");
					String currencyCode = (String) chargeMap.get("currencyCode");
					BigDecimal value = new BigDecimal(String.valueOf(chargeMap.get("value")));
					
					//check if service charge already exist or not
					ChargePackageDetailModel detail = chargePackageRepo.findDetailByServiceChargeAndChargePackage(serviceChargeId, chargePackage.getCode());
					
					if(detail == null) {
						//insert new
						saveNewServiceChargeToChargePackageAndCorporateCharges(chargePackage, currencyCode, serviceChargeId, value, createdBy);
					} else {
						//update
						updateServiceChargeToChargePackageAndCorporateChargesByChargePackage(currencyCode, serviceChargeId, value, createdBy, chargePackageCode);
					}
					
					
				}
			}
			
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return vo;
	}
		
	private void saveNewServiceChargeToChargePackageAndCorporateCharges(ChargePackageModel chargePackage, String currencyCode, String serviceChargeId, BigDecimal value,
			String createdBy) throws ApplicationException, BusinessException {
		
		ChargePackageDetailModel chargePackageDetail = new ChargePackageDetailModel();

		CurrencyModel currency = new CurrencyModel();
		currency.setCode(currencyCode);
		chargePackageDetail.setCurrency(currency);

		ServiceChargeModel serviceCharge = new ServiceChargeModel();
		serviceCharge.setId(serviceChargeId);
		chargePackageDetail.setServiceCharge(serviceCharge);

		chargePackageDetail.setValue(value);
		chargePackageDetail.setValueType(ApplicationConstants.CHARGE_VAL_TYPE_FIX);
		chargePackageDetail.setChargePackage(chargePackage);
		chargePackage.getChargePackageDetail().add(chargePackageDetail);
		
		chargePackageDetail.setCreatedBy(createdBy);
		chargePackageDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
		chargePackageRepo.save(chargePackage);
		
		//insert new corporate charges by charge package code
		if(chargePackageListener != null) {
			for(ChargePackageListener listener : chargePackageListener) {
				listener.saveNewChargeByChargePackageCode(value,
						chargePackage.getCode(),
						currencyCode, 
						serviceChargeId, createdBy);
			}
		}
	}
	
	private void updateServiceChargeToChargePackageAndCorporateCharges(String currencyCode, String serviceChargeId, BigDecimal value,
			String createdBy, String isUpdateCorporateWithSpecialCharge) throws ApplicationException, BusinessException {
		
		//update existing
		chargePackageRepo.updateAllChargesByServiceCharge(value, currencyCode, createdBy, DateUtils.getCurrentDate(),serviceChargeId);
		//end update charge package

		//update corporate charges
		if(chargePackageListener != null) {
			for(ChargePackageListener listener : chargePackageListener) {
				listener.updateALLCharge(value,
						currencyCode, 
						serviceChargeId, 
						isUpdateCorporateWithSpecialCharge);
			}
		}
	}
	
	private void updateServiceChargeToChargePackageAndCorporateChargesByChargePackage(String currencyCode, String serviceChargeId, BigDecimal value,
			String createdBy, String chargePackageCode) throws ApplicationException, BusinessException {
		
		//update existing
		chargePackageRepo.updateChargesByServiceCharge(value, currencyCode, createdBy, DateUtils.getCurrentDate(),serviceChargeId, chargePackageCode);
		//end update charge package

		//update corporate charges
		if(chargePackageListener != null) {
			for(ChargePackageListener listener : chargePackageListener) {
				listener.updateByChargePackageCode(value,
						currencyCode, 
						serviceChargeId, 
						chargePackageCode);
			}
		}
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}

}
