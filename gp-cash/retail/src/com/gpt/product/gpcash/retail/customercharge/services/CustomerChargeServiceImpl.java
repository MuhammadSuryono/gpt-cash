package com.gpt.product.gpcash.retail.customercharge.services;

import java.math.BigDecimal;
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

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;
import com.gpt.product.gpcash.chargepackage.spi.ChargePackageListener;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;
import com.gpt.product.gpcash.retail.customercharge.model.CustomerChargeModel;
import com.gpt.product.gpcash.retail.customercharge.repository.CustomerChargeRepository;
import com.gpt.product.gpcash.servicecharge.model.ServiceChargeModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerChargeServiceImpl implements CustomerChargeService, ChargePackageListener {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CustomerChargeRepository customerChargeRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private CustomerRepository customerRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = customerChargeRepo.findCharges((String)map.get(ApplicationConstants.APP_CODE), ApplicationConstants.SERVICE_TYPE_TRX, (String)map.get(ApplicationConstants.CUST_ID) ,null);
			resultMap.put("result", setModelToMap(result.getContent()));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<Object[]> list){
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		for (Object[] model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", model[0]);
			map.put("serviceCode", model[1]);
			map.put("serviceName", model[2]);
			map.put("serviceChargeId", model[3]);
			map.put("serviceChargeName", model[4]);
			map.put("currencyCode", model[5]);
			map.put("currencyName", model[6]);
			
			BigDecimal value = (BigDecimal) model[7];
			map.put("value", value.toPlainString());
			
			map.put("valueType", model[8]);
			resultList.add(map);			
		}
		
		return resultList;
	}
	
	@Override
	public Map<String, Object> getCustomerCharges(String applicationCode, String transactionServiceCode,
			String customerId) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = customerChargeRepo.findChargesByCustomer(applicationCode, transactionServiceCode, customerId ,null);
			
			List<Map<String, Object>> chargeList = new ArrayList<>();
			
			BigDecimal totalCharge = new BigDecimal(0);
			String totalChargeCurrency = "";
			for (Object[] model : result.getContent()) {
				Map<String, Object> chargeMap = new HashMap<>();
				chargeMap.put("id", model[0]);
				chargeMap.put("serviceChargeName", model[4]);
				chargeMap.put("currencyCode", model[5]);
				chargeMap.put("currencyName", model[6]);
				
				//TODO need to change logic if implement multi currency charges
				totalChargeCurrency = (String) model[5]; 
				
				BigDecimal value = (BigDecimal) model[7];
				chargeMap.put("value", value.toPlainString());
				chargeList.add(chargeMap);
				
				totalCharge = totalCharge.add(value);
			}
			
			resultMap.put(ApplicationConstants.TRANS_CHARGE_LIST, chargeList);
			resultMap.put(ApplicationConstants.TRANS_TOTAL_CHARGE, totalCharge.toPlainString());
			resultMap.put(ApplicationConstants.TRANS_TOTAL_CHARGE_CURRENCY, totalChargeCurrency);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	@Override
	public Map<String, Object> getCustomerChargesPerRecord(String applicationCode, String transactionServiceCode,
			String customerId, Long records) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = customerChargeRepo.findChargesByCustomer(applicationCode, transactionServiceCode, customerId ,null);
			
			List<Map<String, Object>> chargeList = new ArrayList<>();
			
			BigDecimal totalCharge = new BigDecimal(0);
			String totalChargeCurrency = "";
			for (Object[] model : result.getContent()) {
				Map<String, Object> chargeMap = new HashMap<>();
				chargeMap.put("id", model[0]);
				chargeMap.put("serviceChargeName", model[4]);
				chargeMap.put("currencyCode", model[5]);
				chargeMap.put("currencyName", model[6]);
				
				//TODO need to change logic if implement multi currency charges
				totalChargeCurrency = (String) model[5]; 
				
				BigDecimal value = (BigDecimal) model[7];
				chargeMap.put("value", value.toPlainString());
				
				BigDecimal totalPerType = value.multiply(new BigDecimal(records));
				chargeMap.put("total", totalPerType.toPlainString());
				chargeList.add(chargeMap);
				
				totalCharge = totalCharge.add(totalPerType);
			}
			
			resultMap.put(ApplicationConstants.TRANS_CHARGE_LIST, chargeList);
			resultMap.put(ApplicationConstants.TRANS_TOTAL_CHARGE, totalCharge.toPlainString());
			resultMap.put(ApplicationConstants.TRANS_TOTAL_CHARGE_CURRENCY, totalChargeCurrency);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			PendingTaskVO vo = setPendingTaskVO(map); 
			vo.setJsonObject(map); 
			
			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				//jika edit maka simpan pending task untuk old value. Old value diambil dari DB
				Map<String, Object> customerChargeOld = new HashMap<>();
				
				List<Map<String, Object>> customerChargeList = (ArrayList<Map<String,Object>>)map.get("customerChargeList");

				List<String> customerChargeListOld = new ArrayList<>(); 
				for(Map<String, Object> customerChargeMap : customerChargeList){
					customerChargeListOld.add((String)customerChargeMap.get("id"));
				}
				
				Page<Object[]> result = customerChargeRepo.findChargesById((String)map.get(ApplicationConstants.APP_CODE), customerChargeListOld , null);
				customerChargeOld.put("customerChargeList", setModelToMap(result.getContent()));
				vo.setJsonObjectOld(customerChargeOld);
			} else{
				throw new BusinessException("GPT-0100003");
			}

			resultMap.putAll(pendingTaskService.savePendingTask(vo)); 
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo()); 
						
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	@SuppressWarnings("unchecked")
	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) throws ApplicationException, BusinessException {
		try{
			PendingTaskVO vo = new PendingTaskVO();
			vo.setCreatedBy((String)map.get(ApplicationConstants.LOGIN_USERID)); 
			vo.setMenuCode((String)map.get(ApplicationConstants.STR_MENUCODE)); 
			vo.setService("CustomerChargeSC");
			
			List<Map<String, Object>> customerChargeList = (ArrayList<Map<String,Object>>)map.get("customerChargeList");
	
			String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
			
			//check pending task using like for each bank trx limit id
			for(Map<String, Object> customerChargeMap : customerChargeList){
				String customerChargeId = (String)customerChargeMap.get("id");
				uniqueKeyAppend = uniqueKeyAppend + customerChargeId + ApplicationConstants.DELIMITER_PIPE;
				
				//check unique pending task
				vo.setUniqueKey(customerChargeId);
				pendingTaskService.checkUniquePendingTaskLike(vo);
				
				getExistingRecord(customerChargeId, true);
				
				maintenanceRepo.isCurrencyValid((String)customerChargeMap.get("currencyCode"));
			}
			
			//set unique key
			vo.setUniqueKey(uniqueKeyAppend);
			vo.setUniqueKeyDisplay((String) map.get(ApplicationConstants.STR_NAME));
			
			return vo;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private CustomerChargeModel getExistingRecord(String code, boolean isThrowError) throws BusinessException{
		CustomerChargeModel model = customerChargeRepo.findOne(code);
		
		if(model == null){
			if(isThrowError)
				throw new BusinessException("GPT-0100001");
		}
		
		return model;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
				if(ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
					Map<String, Object> map = (Map<String, Object>)vo.getJsonObject();
					
					List<Map<String, Object>> list = (ArrayList<Map<String,Object>>)map.get("customerChargeList");
					
					for(Map<String, Object> customerChargeMap : list){
						String customerChargeId = (String)customerChargeMap.get("id");
						BigDecimal value = new BigDecimal((String) customerChargeMap.get("value"));
						String currencyCode = (String)customerChargeMap.get("currencyCode");
						
						CustomerChargeModel customerCharge = getExistingRecord(customerChargeId, true);
						customerCharge.setValue(value);
						
						CurrencyModel currency = new CurrencyModel();
						currency.setCode(currencyCode);
						customerCharge.setCurrency(currency);
						
						customerCharge.setUpdatedDate(DateUtils.getCurrentTimestamp());
						customerCharge.setUpdatedBy(vo.getCreatedBy());
						
						customerChargeRepo.save(customerCharge);
					}				
					
					CustomerModel customer = customerRepo.findOne((String) map.get(ApplicationConstants.CUST_ID));
					customer.setSpecialChargeFlag(ApplicationConstants.YES);
					customerRepo.save(customer);
				}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return vo;
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		//if any spesific business function for reject, applied here
		
		return vo;
	}

	@Override
	public void updateALLChargeByChargePackageCode(BigDecimal chargeAmount, String chargePackageCode,
			String currencyCode, String serviceChargeId, String isUpdateCustomerWithSpecialCharge) {
		
		if(ApplicationConstants.NO.equals(isUpdateCustomerWithSpecialCharge)) {
			//only update without special charge
			customerChargeRepo.updateChargesByChargePackageCode(chargeAmount, chargePackageCode, currencyCode, 
					serviceChargeId, ApplicationConstants.NO);
		} else {
			//update all (with special charge and without special charge)
			customerChargeRepo.updateChargesByChargePackageCode(chargeAmount, chargePackageCode, currencyCode, 
					serviceChargeId);
		}
	}
	
	@Override
	public void saveNewChargeByChargePackageCode(BigDecimal chargeAmount, String chargePackageCode,
			String currencyCode, String serviceChargeId, String createdBy) throws ApplicationException, BusinessException {
		try {
			//find customer by charge package code
			List<String> customers = customerChargeRepo.findCustomersByChargePackage(chargePackageCode);
			
			for(String customerStr : customers) {
				//insert new service charge to customer
				CustomerChargeModel customerCharge = new CustomerChargeModel();
				
				CustomerModel customer = new CustomerModel();
				customer.setId(customerStr);
				customerCharge.setCustomer(customer);
				
				customerCharge.setValue(chargeAmount);
				
				ChargePackageModel chargePackage = new ChargePackageModel();
				chargePackage.setCode(chargePackageCode);
				customerCharge.setChargePackage(chargePackage);
				
				customerCharge.setValueType(ApplicationConstants.CHARGE_VAL_TYPE_FIX);
				
				ServiceChargeModel serviceCharge = new ServiceChargeModel();
				serviceCharge.setId(serviceChargeId);
				customerCharge.setServiceCharge(serviceCharge);
				
				IDMApplicationModel application = new IDMApplicationModel();
				application.setCode(ApplicationConstants.APP_GPCASHIB);
				customerCharge.setApplication(application);
				
				CurrencyModel currency = new CurrencyModel();
				currency.setCode(currencyCode);
				customerCharge.setCurrency(currency);
				
				customerCharge.setCreatedBy(createdBy);
				customerCharge.setCreatedDate(DateUtils.getCurrentTimestamp());
				
				customerChargeRepo.persist(customerCharge);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void updateALLCharge(BigDecimal chargeAmount, 
			String currencyCode, String serviceChargeId, String isUpdateCustomerWithSpecialCharge) {
		
		if(ApplicationConstants.NO.equals(isUpdateCustomerWithSpecialCharge)) {
			//only update without special charge
			customerChargeRepo.updateAllCharges(chargeAmount, currencyCode, serviceChargeId, ApplicationConstants.NO);
		} else {
			//update all (with special charge and without special charge)
			customerChargeRepo.updateAllCharges(chargeAmount, currencyCode, serviceChargeId);
		}
	}
	
	@Override
	public void updateByChargePackageCode(BigDecimal chargeAmount, 
			String currencyCode, String serviceChargeId, String chargePackageCode) {
		
		//update spesific customer charge
		customerChargeRepo.updateChargesByChargePackageCode(chargeAmount, chargePackageCode, currencyCode, 
				serviceChargeId);
	}
}
