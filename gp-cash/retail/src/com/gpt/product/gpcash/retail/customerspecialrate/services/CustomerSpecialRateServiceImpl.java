package com.gpt.product.gpcash.retail.customerspecialrate.services;

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
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.services.SysParamService;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customerspecialrate.model.CustomerSpecialRateModel;
import com.gpt.product.gpcash.retail.customerspecialrate.repository.CustomerSpecialRateRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerSpecialRateServiceImpl implements CustomerSpecialRateService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustomerSpecialRateRepository specialRateRepo;

	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private SysParamService sysParamService;
	
	@Autowired
	private ExchangeRateRepository exchangeRateRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerSpecialRateModel> result = specialRateRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<CustomerSpecialRateModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerSpecialRateModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}
		return resultList;
	}
	
	private Map<String, Object> setModelToMap(CustomerSpecialRateModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("id", ValueUtils.getValue(model.getId()));	
		map.put("refNoSpecialRate", ValueUtils.getValue(model.getRefNoSpecialRate()));	
		map.put("customerId", ValueUtils.getValue(model.getCustomer().getId()));
		map.put("customerName", ValueUtils.getValue(model.getCustomer().getName()));		
		map.put("foreignCurrency1", ValueUtils.getValue(model.getForeignCurrency1().getCode()));
		if(model.getForeignCurrency2()!=null){
			map.put("foreignCurrency2", ValueUtils.getValue(model.getForeignCurrency2().getCode()));
		}
		map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
		map.put("type", ValueUtils.getValue(model.getType()));
		map.put("expiryDate", ValueUtils.getValue(model.getExpiryDate()));
		map.put("transactionAmountRate", ValueUtils.getValue(model.getTransactionAmountRate()));		
		map.put("status", ValueUtils.getValue(model.getStatus()));
		
		
		if(isGetDetail){
			map.put("customer", ValueUtils.getValue(model.getCustomer().getId()).concat(" - ").concat(ValueUtils.getValue(model.getCustomer().getName())));
			map.put("unit", ValueUtils.getValue(model.getCurrencyQuantity1()));
			map.put("unit2", ValueUtils.getValue(model.getCurrencyQuantity2()));
			
			String specialRate = "";
			if(model.getTransactionBuyRate()!=null) {
				specialRate = "Buy Rate : ".concat(ValueUtils.getValue(model.getTransactionBuyRate().toPlainString()));
				map.put("buySellAmountRate", ValueUtils.getValue(model.getBuyAmountRate()));
				map.put("transactionBuyRate", ValueUtils.getValue(model.getTransactionBuyRate()));
				map.put("transactionAmountRate",  "IDR " + ValueUtils.getValue(model.getTransactionAmountRate()));	
			}
			
			if(model.getTransactionSellRate()!=null) {
				specialRate = "Sell Rate : ".concat((String) ValueUtils.getValue(model.getTransactionSellRate().toPlainString()));
				map.put("buySellAmountRate", ValueUtils.getValue(model.getSellAmountRate()));
				map.put("transactionSellRate", ValueUtils.getValue(model.getTransactionSellRate()));
				map.put("transactionAmountRate",  ValueUtils.getValue(model.getForeignCurrency2().getCode()) + " " +  ValueUtils.getValue(model.getTransactionAmountRate()));
			}
			map.put("specialRate", specialRate);
			
			map.put("remark", ValueUtils.getValue(model.getRemark()));
			
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
				BigDecimal deviation = new BigDecimal(sysParamService.getValueByCode(SysParamConstants.SPECIAL_RATE_DEVIATION));
				deviation = deviation.divide(new BigDecimal(100));
				Map<String, Object> rateMap = getLatestExchangeRate();
				ExchangeRateModel rate1 = new ExchangeRateModel();
				ExchangeRateModel rate2 = new ExchangeRateModel();
				
				String rateType = (String)map.get("rateType");
				if(rateType.equals("LOCAL")) {
					rate1 = (ExchangeRateModel) rateMap.get((String)map.get("foreignCurrency"));
					
					BigDecimal buySellRate = new BigDecimal((String) map.get("buySellRate"));
					if(((String)map.get("buySellRateOpt")).equals("Buy Rate")) {						
						BigDecimal minRate = rate1.getTransactionBuyRate().subtract(deviation.multiply(rate1.getTransactionBuyRate()));
						BigDecimal maxRate = rate1.getTransactionBuyRate().add(deviation.multiply(rate1.getTransactionBuyRate()));
						if(buySellRate.compareTo(minRate)<0 || buySellRate.compareTo(maxRate)>0) {
							throw new BusinessException("GPT-131192"); //invalid deviation rate
						}
					}else {
						BigDecimal minRate = rate1.getTransactionSellRate().subtract(deviation.multiply(rate1.getTransactionSellRate()));
						BigDecimal maxRate = rate1.getTransactionSellRate().add(deviation.multiply(rate1.getTransactionSellRate()));
						if(buySellRate.compareTo(minRate)<0 || buySellRate.compareTo(maxRate)>0) {
							throw new BusinessException("GPT-131192"); //invalid deviation rate
						}
					}
				}else {
					rate1 = (ExchangeRateModel) rateMap.get((String)map.get("foreignCurrency1"));
					rate2 = (ExchangeRateModel) rateMap.get((String)map.get("foreignCurrency2"));
					
					BigDecimal buyRate = new BigDecimal((String) map.get("buyRate"));
					BigDecimal minRate1 = rate1.getTransactionBuyRate().subtract(deviation.multiply(rate1.getTransactionBuyRate()));
					BigDecimal maxRate1 = rate1.getTransactionBuyRate().add(deviation.multiply(rate1.getTransactionBuyRate()));
					if(buyRate.compareTo(minRate1)<0 || buyRate.compareTo(maxRate1)>0) {
						throw new BusinessException("GPT-131192"); //invalid deviation rate
					}
					
					BigDecimal sellRate = new BigDecimal((String) map.get("sellRate"));
					BigDecimal minRate2 = rate2.getTransactionSellRate().subtract(deviation.multiply(rate2.getTransactionSellRate()));
					BigDecimal maxRate2 = rate2.getTransactionSellRate().add(deviation.multiply(rate2.getTransactionSellRate()));
					if(sellRate.compareTo(minRate2)<0 || sellRate.compareTo(maxRate2)>0) {
						throw new BusinessException("GPT-131192"); //invalid deviation rate
					}
				}
				
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				CustomerSpecialRateModel specialRateOld = getExistingModel((String) map.get("refNoSpecialRate"), true);
				vo.setJsonObjectOld(setModelToMap(specialRateOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				// get pending task by reference
				//CorporateUserPendingTaskModel pendingTask = pendingTaskRepo.findByRefNoSpecialRateAndStatusIsPending((String) map.get("refNoSpecialRate"));
				//if (pendingTask != null) {
					//throw new BusinessException("GPT-131193"); //Treasury Code is already in use in pending transaction
				//}

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

	private CustomerSpecialRateModel getExistingModel(String refNoSpecialRate, boolean isThrowError) throws BusinessException, Exception {
		CustomerSpecialRateModel specialRate = null;

		// preparing input
		Map<String, Object> map = new HashMap<>();
		map.put("refNoSpecialRate", refNoSpecialRate);

		List<CustomerSpecialRateModel> list = specialRateRepo.search(map, null).getContent();

		if(!ValueUtils.hasValue(list)){
			if(isThrowError)
				throw new BusinessException("GPT-0100001");
		}else{
			specialRate = list.get(0);
		}
		
		return specialRate;
	}
	
	public CustomerSpecialRateModel updateStatus(String refNoSpecialRate, String status, String updatedBy) throws BusinessException, Exception {
		CustomerSpecialRateModel specialRate = getExistingModel(refNoSpecialRate, true);
		
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
		vo.setService("CustomerSpecialRateSC");

		return vo;
	}

	private CustomerSpecialRateModel setMapToModel(Map<String, Object> map) throws ParseException {
		CustomerSpecialRateModel specialRate = new CustomerSpecialRateModel();
		specialRate.setRefNoSpecialRate((String)map.get("refNoSpecialRate"));
		
		CustomerModel customer = new CustomerModel();
		customer.setId((String) map.get("customerId"));
		specialRate.setCustomer(customer);
		
		specialRate.setCurrencyQuantity1(Integer.parseInt(((String)map.get("unit"))));
		specialRate.setTransactionAmountRate(new BigDecimal((String) map.get("trxAmount")));
		specialRate.setType((String)map.get("typeRate"));
		specialRate.setRateType((String)map.get("rateType"));
		specialRate.setRemark((String)map.get("remark"));
		
		String expDate = (String)map.get("expiryDate");
		String expHour = (String)map.get("expHour");
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
		Date expiryDate = sdf.parse(expDate + " " + expHour); 
		
		specialRate.setExpiryDate(new Timestamp (expiryDate.getTime()));
		
		specialRate.setStatus(ApplicationConstants.SPECIAL_RATE_OPEN);
		String rateType = (String)map.get("rateType");
		if(rateType.equals("LOCAL")) {
			CurrencyModel foreignCurrency = new CurrencyModel();
			foreignCurrency.setCode((String)map.get("foreignCurrency"));
			specialRate.setForeignCurrency1(foreignCurrency);
			
			if(((String)map.get("buySellRateOpt")).equals("Buy Rate")) {
				specialRate.setTransactionBuyRate(new BigDecimal((String) map.get("buySellRate")));
				specialRate.setBuyAmountRate(new BigDecimal((String) map.get("buySellAmountRate")));
			}else {
				specialRate.setTransactionSellRate(new BigDecimal((String) map.get("buySellRate")));
				specialRate.setSellAmountRate(new BigDecimal((String) map.get("buySellAmountRate")));
			}
			
		}else {
			CurrencyModel foreignCurrency1 = new CurrencyModel();
			foreignCurrency1.setCode((String)map.get("foreignCurrency1"));
			specialRate.setForeignCurrency1(foreignCurrency1);
			
			CurrencyModel foreignCurrency2 = new CurrencyModel();
			foreignCurrency2.setCode((String)map.get("foreignCurrency2"));
			specialRate.setForeignCurrency2(foreignCurrency2);
			
			specialRate.setCurrencyQuantity2(Integer.parseInt(((String)map.get("unit2"))));
			specialRate.setTransactionBuyRate(new BigDecimal((String) map.get("buyRate")));
			specialRate.setTransactionSellRate(new BigDecimal((String) map.get("sellRate")));
		}


		CurrencyModel currency = new CurrencyModel();
		currency.setCode((String) map.get("currencyCode"));

		return specialRate;
	}

	private void checkUniqueRecord(PendingTaskVO vo) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
		
		Map<String, Object> mapInput = new HashMap<>();
		mapInput.put("refNoSpecialRate", map.get("refNoSpecialRate"));

		List<CustomerSpecialRateModel> list = specialRateRepo.search(mapInput, null).getContent();

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
				CustomerSpecialRateModel specialRate = setMapToModel(map);

				// set default value
				specialRate.setIdx(ApplicationConstants.IDX);
				specialRate.setCreatedDate(DateUtils.getCurrentTimestamp());
				specialRate.setCreatedBy(vo.getCreatedBy());
				specialRate.setDeleteFlag(ApplicationConstants.NO);

				specialRateRepo.persist(specialRate);

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				CustomerSpecialRateModel specialRateNew = setMapToModel(map);

				CustomerSpecialRateModel specialRateExisting = getExistingModel(specialRateNew.getRefNoSpecialRate(), true);

				if (specialRateExisting == null) {
					throw new BusinessException("GPT-0100001");
				}

				// set value yg boleh di edit
				specialRateExisting.setTransactionSellRate(specialRateNew.getTransactionSellRate());
				specialRateExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				specialRateExisting.setUpdatedBy(vo.getCreatedBy());

				specialRateRepo.save(specialRateExisting);
				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				
				CustomerSpecialRateModel specialRate = getExistingModel((String) map.get("refNoSpecialRate"), true);
				
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
