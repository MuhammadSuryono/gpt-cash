package com.gpt.product.gpcash.maintenance.timedepositretail.specialrate.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.exchangerate.model.ExchangeRateModel;
import com.gpt.component.maintenance.exchangerate.repository.ExchangeRateRepository;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.maintenance.timedepositretail.specialrate.model.CustomerTimeDepositSpecialRateModel;
import com.gpt.product.gpcash.maintenance.timedepositretail.specialrate.repository.CustomerTimeDepositSpecialRateRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTimeDepositSpecialRateServiceImpl implements CustomerTimeDepositSpecialRateService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustomerTimeDepositSpecialRateRepository specialRateRepo;

	@Autowired
	private PendingTaskService pendingTaskService;
	
	/*@Autowired
	private SysParamService sysParamService;*/
	
	@Autowired
	private ExchangeRateRepository exchangeRateRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerTimeDepositSpecialRateModel> result = specialRateRepo.search(map, PagingUtils.createPageRequest(map));

			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
				resultMap.put("result", setModelToMap(result.getContent(), false));
			} else {
				resultMap.put("result", setModelToMap(result.getContent(), true));
			}

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<CustomerTimeDepositSpecialRateModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerTimeDepositSpecialRateModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}
		return resultList;
	}
	
	private Map<String, Object> setModelToMap(CustomerTimeDepositSpecialRateModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("id", ValueUtils.getValue(model.getId()));	
		map.put("refNoSpecialRate", ValueUtils.getValue(model.getRefNoSpecialRate()));	
		map.put("customerId", ValueUtils.getValue(model.getCustomer().getId()));
		map.put("customerName", ValueUtils.getValue(model.getCustomer().getName()));		

		map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
		map.put("expiryDate", ValueUtils.getValue(model.getExpiryDate()));
		map.put("specialRate", ValueUtils.getValue(model.getSpecialRate()));		
		map.put("status", ValueUtils.getValue(model.getStatus()));
		
		
		if(isGetDetail){
			map.put("customer", ValueUtils.getValue(model.getCustomer().getId()).concat(" - ").concat(ValueUtils.getValue(model.getCustomer().getName())));
			
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}
		
		
		return map;
	}

	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord(vo); // cek ke table jika record already exist
				
				//check rate deviation
					
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				CustomerTimeDepositSpecialRateModel specialRateOld = getExistingModel((String) map.get("refNoSpecialRate"), true);
				vo.setJsonObjectOld(setModelToMap(specialRateOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				// get pending task by reference
//				CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findByRefNoSpecialRateAndStatusIsPending((String) map.get("refNoSpecialRate"));
//				if (pendingTask != null) {
//					throw new BusinessException("GPT-131193"); //Treasury Code is already in use in pending transaction
//				}

			} else {
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

	private CustomerTimeDepositSpecialRateModel getExistingModel(String refNoSpecialRate, boolean isThrowError) throws BusinessException, Exception {
		CustomerTimeDepositSpecialRateModel specialRate = null;

		// preparing input
		Map<String, Object> map = new HashMap<>();
		map.put("refNoSpecialRate", refNoSpecialRate);

		List<CustomerTimeDepositSpecialRateModel> list = specialRateRepo.search(map, null).getContent();

		if(!ValueUtils.hasValue(list)){
			if(isThrowError)
				throw new BusinessException("GPT-0100001");
		}else{
			specialRate = list.get(0);
		}
		
		return specialRate;
	}
	
	public CustomerTimeDepositSpecialRateModel updateStatus(String refNoSpecialRate, String status, String updatedBy) throws BusinessException, Exception {
		CustomerTimeDepositSpecialRateModel specialRate = getExistingModel(refNoSpecialRate, true);
		
		specialRate.setStatus(status);
		specialRate.setUpdatedDate(DateUtils.getCurrentTimestamp());
		specialRate.setUpdatedBy(updatedBy);
		
		specialRateRepo.save(specialRate);
		
		return specialRate;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get("refNoSpecialRate"));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("CustomerTimeDepositSpecialRateSC");

		return vo;
	}

	private CustomerTimeDepositSpecialRateModel setMapToModel(Map<String, Object> map) throws ParseException {
		CustomerTimeDepositSpecialRateModel specialRate = new CustomerTimeDepositSpecialRateModel();
		specialRate.setRefNoSpecialRate((String)map.get("refNoSpecialRate"));
		
		CustomerModel customer = new CustomerModel();
		customer.setId((String) map.get("customerId"));
		specialRate.setCustomer(customer);
		
		specialRate.setSpecialRate(new BigDecimal((String) map.get("specialRate")));
		
		String expDate = (String)map.get("expiryDate");
		String expHour = (String)map.get("expHour");
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
		Date expiryDate = sdf.parse(expDate + " " + expHour); 
		
		specialRate.setExpiryDate(new Timestamp (expiryDate.getTime()));
		
		specialRate.setStatus(ApplicationConstants.SPECIAL_RATE_OPEN);


		CurrencyModel currency = new CurrencyModel();
		currency.setCode((String) map.get("currencyCode"));

		return specialRate;
	}

	private void checkUniqueRecord(PendingTaskVO vo) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
		
		Map<String, Object> mapInput = new HashMap<>();
		mapInput.put("refNoSpecialRate", map.get("refNoSpecialRate"));

		List<CustomerTimeDepositSpecialRateModel> list = specialRateRepo.search(mapInput, null).getContent();

		if (!list.isEmpty()) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				CustomerTimeDepositSpecialRateModel specialRate = setMapToModel(map);

				// set default value
				specialRate.setIdx(ApplicationConstants.IDX);
				specialRate.setCreatedDate(DateUtils.getCurrentTimestamp());
				specialRate.setCreatedBy(vo.getCreatedBy());
				specialRate.setDeleteFlag(ApplicationConstants.NO);

				specialRateRepo.persist(specialRate);

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				CustomerTimeDepositSpecialRateModel specialRateNew = setMapToModel(map);

				CustomerTimeDepositSpecialRateModel specialRateExisting = getExistingModel(specialRateNew.getRefNoSpecialRate(), true);

				if (specialRateExisting == null) {
					throw new BusinessException("GPT-0100001");
				}


				specialRateExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				specialRateExisting.setUpdatedBy(vo.getCreatedBy());

				specialRateRepo.save(specialRateExisting);
				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				
				CustomerTimeDepositSpecialRateModel specialRate = getExistingModel((String) map.get("refNoSpecialRate"), true);
				
				specialRate.setDeleteFlag(ApplicationConstants.YES);
				specialRate.setUpdatedDate(DateUtils.getCurrentTimestamp());
				specialRate.setUpdatedBy(vo.getCreatedBy());
				
				specialRateRepo.save(specialRate);
				
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
		// if any spesific business function for reject, applied here

		return vo;
	}
	
	private Map<String, Object> getLatestExchangeRate() throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		ExchangeRateModel exchangeRate;
		List<ExchangeRateModel> rateList = exchangeRateRepo.findByDeleteFlag(ApplicationConstants.NO);
		
		for (int i = 0; i < rateList.size(); i++) {
			exchangeRate = rateList.get(i);
			returnMap.put(exchangeRate.getCurrency().getCode(), exchangeRate);
		}
		
		return returnMap;
	}

}
