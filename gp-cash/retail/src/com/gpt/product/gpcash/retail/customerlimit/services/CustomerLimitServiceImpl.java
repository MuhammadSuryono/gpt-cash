package com.gpt.product.gpcash.retail.customerlimit.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;
import com.gpt.product.gpcash.limitpackage.spi.LimitPackageListener;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;
import com.gpt.product.gpcash.retail.customerlimit.model.CustomerLimitModel;
import com.gpt.product.gpcash.retail.customerlimit.repository.CustomerLimitRepository;
import com.gpt.product.gpcash.servicecurrencymatrix.model.ServiceCurrencyMatrixModel;
import com.gpt.product.gpcash.servicecurrencymatrix.repository.ServiceCurrencyMatrixRepository;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerLimitServiceImpl implements CustomerLimitService, LimitPackageListener {	
	@Autowired
	private CustomerLimitRepository customerLimitRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired
	private ServiceCurrencyMatrixRepository serviceCurrencyMatrixRepo;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = customerLimitRepo.findLimit((String)map.get(ApplicationConstants.APP_CODE), ApplicationConstants.SERVICE_TYPE_TRX, (String)map.get(ApplicationConstants.CUST_ID) ,null);
			resultMap.put("result", setModelToMap(result.getContent()));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	public Map<String, Object> searchCustomerLimitList(String customerId, String applicationCode) throws ApplicationException, BusinessException {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.APP_CODE, applicationCode);
		map.put(ApplicationConstants.CUST_ID, customerId);
		return search(map);
	}
	
	private List<Map<String, Object>> setModelToMap(List<Object[]> list){
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		for (Object[] model : list) {
			resultList.add(setModelToMap(model));			
		}
		
		return resultList;
	}
	
	private Map<String, Object> setModelToMap(Object[] model) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", model[0]);
		map.put("serviceCode", model[1]);
		map.put("serviceName", model[2]);
		map.put("maxAmountLimit", ((BigDecimal) model[3]).toPlainString());
		
		map.put("maxOccurrenceLimit", model[4].toString());
		map.put("amountLimitUsage", ((BigDecimal) model[5]).toPlainString());
		map.put("occurrenceLimitUsage", model[6].toString());
		map.put("currencyCode", model[7]);
		map.put("currencyName", model[8]);
		map.put("currencyMatrixCode", model[9]);
		map.put("currencyMatrixName", model[10]);
		
		BigDecimal remainingOccurenceLimit = new BigDecimal(model[4].toString()).subtract(new BigDecimal(model[6].toString()));
		map.put("remainingOccurenceLimit", remainingOccurenceLimit.toPlainString());

		BigDecimal remainingAmountLimit = new BigDecimal(model[3].toString()).subtract(new BigDecimal(model[5].toString()));
		map.put("remainingAmountLimit", remainingAmountLimit.toPlainString());
		
		return map;
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
				Map<String, Object> customerLimitOld = new HashMap<>();
				
				List<Map<String, Object>> customerLimitList = (ArrayList<Map<String,Object>>)map.get("customerLimitList");

				List<String> customerLimitListOld = new ArrayList<>(); 
				for(Map<String, Object> customerLimitMap : customerLimitList){
					customerLimitListOld.add((String)customerLimitMap.get("id"));
				}
				
				Page<Object[]> result = customerLimitRepo.findLimit((String)map.get(ApplicationConstants.APP_CODE), customerLimitListOld , null);
				customerLimitOld.put("customerLimitList", setModelToMap(result.getContent()));
				vo.setJsonObjectOld(customerLimitOld);
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
			vo.setService("CustomerLimitSC");
			
			List<Map<String, Object>> customerLimitList = (ArrayList<Map<String,Object>>)map.get("customerLimitList");
	
			String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
			
			//check pending task using like for each bank trx limit id
			for(Map<String, Object> customerLimitMap : customerLimitList){
				String customerLimitId = (String)customerLimitMap.get("id");
				uniqueKeyAppend = uniqueKeyAppend + customerLimitId + ApplicationConstants.DELIMITER_PIPE;
				
				//check unique pending task
				vo.setUniqueKey(customerLimitId);
				pendingTaskService.checkUniquePendingTaskLike(vo);
				
				getExistingRecord(customerLimitId, true);
				
				maintenanceRepo.isCurrencyValid((String)customerLimitMap.get("currencyCode"));
				productRepo.isServiceValid((String)customerLimitMap.get("serviceCode"));
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
	
	private CustomerLimitModel getExistingRecord(String code, boolean isThrowError) throws BusinessException{
		CustomerLimitModel model = customerLimitRepo.findOne(code);
		
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
					
					List<Map<String, Object>> list = (ArrayList<Map<String,Object>>)map.get("customerLimitList");
					
					for(Map<String, Object> customerLimitMap : list){
						String customerLimitId = (String)customerLimitMap.get("id");
						int maxOccurrenceLimit = Integer.parseInt((String) customerLimitMap.get("maxOccurrenceLimit"));
						BigDecimal maxAmountLimit = new BigDecimal((String) customerLimitMap.get("maxAmountLimit"));
						String currencyCode = (String)customerLimitMap.get("currencyCode");
						
						CustomerLimitModel customerLimit = getExistingRecord(customerLimitId, true);
						customerLimit.setMaxAmountLimit(maxAmountLimit);
						customerLimit.setMaxOccurrenceLimit(maxOccurrenceLimit);
						
						CurrencyModel currency = new CurrencyModel();
						currency.setCode(currencyCode);
						customerLimit.setCurrency(currency);
						
						customerLimit.setUpdatedDate(DateUtils.getCurrentTimestamp());
						customerLimit.setUpdatedBy(vo.getCreatedBy());
						
						//TODO cek ke bank limit
						
						customerLimitRepo.save(customerLimit);
					}			
					
					CustomerModel customer = customerRepo.findOne((String) map.get(ApplicationConstants.CUST_ID));
					customer.setSpecialLimitFlag(ApplicationConstants.YES);
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
	public void resetLimit(String parameter) throws ApplicationException, BusinessException {
		try{
			customerLimitRepo.resetLimit();
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void updateALLLimitByLimitPackageCode(BigDecimal maxAmountLimit, int maxOccurrenceLimit, 
			String limitPackageCode, String currencyCode, String serviceCode, String serviceCurrencyMatrixId,
			String isUpdateCustomerWithSpecialLimit) throws Exception {
		if(ApplicationConstants.NO.equals(isUpdateCustomerWithSpecialLimit)) {
			//only update without special charge
			customerLimitRepo.updateLimitsByLimitPackageCode(maxAmountLimit, maxOccurrenceLimit, limitPackageCode, 
					currencyCode, serviceCode, serviceCurrencyMatrixId, ApplicationConstants.NO);
		} else {
			//update all (with special limit and without special limit)
			customerLimitRepo.updateLimitsByLimitPackageCode(maxAmountLimit, maxOccurrenceLimit, limitPackageCode, 
					currencyCode, serviceCode, serviceCurrencyMatrixId);
		}
	}
	
	@Override
	public void saveNewLimitByLimitPackageCode(BigDecimal maxAmountLimit, int maxTrxPerDay, String limitPackageCode,
			String currencyCode, String serviceCurrencyMatrixId, String createdBy) throws ApplicationException, BusinessException {
		try {
			//find customer by charge package code
			List<String> customers = customerLimitRepo.findCustomersByLimitPackage(limitPackageCode);
			
			for(String customerStr : customers) {
				//insert new service limit to customer
				CustomerLimitModel customerLimit = new CustomerLimitModel();
				
				CustomerModel customer = new CustomerModel();
				customer.setId(customerStr);
				customerLimit.setCustomer(customer);
				
				ServiceCurrencyMatrixModel serviceCurrencyMatrix = serviceCurrencyMatrixRepo.findOne(serviceCurrencyMatrixId);
				
				customerLimit.setMaxAmountLimit(maxAmountLimit);
				customerLimit.setMaxOccurrenceLimit(maxTrxPerDay);
				customerLimit.setAmountLimitUsage(BigDecimal.ZERO);
				customerLimit.setOccurrenceLimitUsage(0);
				
				customerLimit.setService(serviceCurrencyMatrix.getService());
				customerLimit.setServiceCurrencyMatrix(serviceCurrencyMatrix);
				
				LimitPackageModel limitPackage = new LimitPackageModel();
				limitPackage.setCode(limitPackageCode);
				customerLimit.setLimitPackage(limitPackage);
				
				IDMApplicationModel application = new IDMApplicationModel();
				application.setCode(ApplicationConstants.APP_GPCASHIB);
				customerLimit.setApplication(application);
				
				CurrencyModel currency = new CurrencyModel();
				currency.setCode(currencyCode);
				customerLimit.setCurrency(currency);
				
				customerLimit.setCreatedBy(createdBy);
				customerLimit.setCreatedDate(DateUtils.getCurrentTimestamp());
				
				customerLimitRepo.persist(customerLimit);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void updateALLLimit(BigDecimal maxAmountLimit, int maxTrxPerDay, 
			String currencyCode, String serviceCurrencyMatrixId, String isUpdateCustomerWithSpecialCharge) {
		
		if(ApplicationConstants.NO.equals(isUpdateCustomerWithSpecialCharge)) {
			//only update without special charge
			customerLimitRepo.updateAllLimits(maxAmountLimit, maxTrxPerDay, currencyCode, serviceCurrencyMatrixId, ApplicationConstants.NO);
		} else {
			//update all (with special charge and without special charge)
			customerLimitRepo.updateAllLimits(maxAmountLimit, maxTrxPerDay, currencyCode, serviceCurrencyMatrixId);
		}
	}
	
	@Override
	public void updateByLimitPackageCode(BigDecimal maxAmountLimit, int maxTrxPerDay, 
			String currencyCode, String serviceCode, String serviceCurrencyMatrixId, String limitPackageCode) {
		
		//update spesific customer charge
		customerLimitRepo.updateLimitsByLimitPackageCode(maxAmountLimit, maxTrxPerDay, limitPackageCode, 
				currencyCode, serviceCode, serviceCurrencyMatrixId);
	}
}
