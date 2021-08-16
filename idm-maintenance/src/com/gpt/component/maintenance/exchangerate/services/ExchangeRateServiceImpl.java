package com.gpt.component.maintenance.exchangerate.services;

import java.math.BigDecimal;
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

import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.exchangerate.model.ExchangeRateModel;
import com.gpt.component.maintenance.exchangerate.repository.ExchangeRateRepository;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExchangeRateServiceImpl implements ExchangeRateService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ExchangeRateRepository exchangeRateRepo;

	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<ExchangeRateModel> result = exchangeRateRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<ExchangeRateModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (ExchangeRateModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}
		return resultList;
	}
	
	private Map<String, Object> setModelToMap(ExchangeRateModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("id", ValueUtils.getValue(model.getId()));
		map.put("effectiveDate", ValueUtils.getValue(model.getEffectiveDate()));
		map.put("currencyQuantity", ValueUtils.getValue(model.getCurrencyQuantity()));
		map.put("currencyCode", model.getCurrency() != null ? model.getCurrency().getCode() : ApplicationConstants.EMPTY_STRING);
		map.put("currencyName", model.getCurrency() != null ? model.getCurrency().getName() : ApplicationConstants.EMPTY_STRING);
		
		if(isGetDetail){
			map.put("trxBuyRate", ValueUtils.getValue(model.getTransactionBuyRate()));
			map.put("trxSellRate", ValueUtils.getValue(model.getTransactionSellRate()));
			map.put("trxMidRate", ValueUtils.getValue(model.getTransactionMidRate()));
			map.put("bankBuyRate", ValueUtils.getValue(model.getBankNoteBuyRate()));
			map.put("bankSellRate", ValueUtils.getValue(model.getBankNoteSellRate()));
			map.put("tellerBuyRate", ValueUtils.getValue(model.getTellerBuyRate()));
			map.put("tellerSellRate", ValueUtils.getValue(model.getTellerSellRate()));
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

			maintenanceRepo.isCurrencyValid((String) map.get("currencyCode")); // cek jika curreny valid atau tidak

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord(vo); // cek ke table jika record already exist
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				ExchangeRateModel exchangeRateOld = getExistingModel((String) map.get("currencyCode"), true);
				vo.setJsonObjectOld(setModelToMap(exchangeRateOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				
				//check existing record exist or not
				getExistingModel((String) map.get("currencyCode"), true);

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

	private ExchangeRateModel getExistingModel(String currencyCode, boolean isThrowError) throws BusinessException, Exception {
		ExchangeRateModel exchangeRate = null;

		// preparing input
		Map<String, Object> map = new HashMap<>();
		map.put("currencyCode", currencyCode);

		List<ExchangeRateModel> list = exchangeRateRepo.search(map, null).getContent();

		if(!ValueUtils.hasValue(list)){
			if(isThrowError)
				throw new BusinessException("GPT-0100001");
		}else{
			exchangeRate = list.get(0);
		}
		
		return exchangeRate;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get("currencyCode"));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("ExchangeRateSC");

		return vo;
	}

	private ExchangeRateModel setMapToModel(Map<String, Object> map) {
		ExchangeRateModel exchangeRate = new ExchangeRateModel();
//		exchangeRate.setCurrencyQuantity(Integer.parseInt((String) map.get("currencyQuantity")));
		exchangeRate.setEffectiveDate(DateUtils.getCurrentTimestamp());
		exchangeRate.setTransactionBuyRate(new BigDecimal((String) map.get("trxBuyRate")));
		exchangeRate.setTransactionMidRate(new BigDecimal((String) map.get("trxMidRate")));
		exchangeRate.setTransactionSellRate(new BigDecimal((String) map.get("trxSellRate")));
		exchangeRate.setBankNoteBuyRate(new BigDecimal((String) map.get("bankBuyRate")));
		exchangeRate.setBankNoteSellRate(new BigDecimal((String) map.get("bankSellRate")));
		exchangeRate.setTellerBuyRate(new BigDecimal((String) map.get("tellerBuyRate")));
		exchangeRate.setTellerSellRate(new BigDecimal((String) map.get("tellerSellRate")));

		CurrencyModel currency = new CurrencyModel();
		currency.setCode((String) map.get("currencyCode"));
		exchangeRate.setCurrency(currency);

		return exchangeRate;
	}

	private void checkUniqueRecord(PendingTaskVO vo) throws Exception {
		Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
		List<ExchangeRateModel> list = exchangeRateRepo.search(map, null).getContent();

		if (!list.isEmpty()) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				ExchangeRateModel exchangeRate = setMapToModel(map);

				// set default value
				exchangeRate.setIdx(ApplicationConstants.IDX);
				exchangeRate.setCreatedDate(DateUtils.getCurrentTimestamp());
				exchangeRate.setCreatedBy(vo.getCreatedBy());
				exchangeRate.setDeleteFlag(ApplicationConstants.NO);

				exchangeRateRepo.persist(exchangeRate);

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				ExchangeRateModel exchangeRateNew = setMapToModel(map);

				ExchangeRateModel exchangeRateExisting = getExistingModel(exchangeRateNew.getCurrency().getCode(), true);

				if (exchangeRateExisting == null) {
					throw new BusinessException("GPT-0100001");
				}

				// set value yg boleh di edit
//				exchangeRateExisting.setCurrencyQuantity(exchangeRateNew.getCurrencyQuantity());
				exchangeRateExisting.setEffectiveDate(exchangeRateNew.getEffectiveDate());
				exchangeRateExisting.setTransactionBuyRate(exchangeRateNew.getTransactionBuyRate());
				exchangeRateExisting.setTransactionMidRate(exchangeRateNew.getTransactionMidRate());
				exchangeRateExisting.setTransactionSellRate(exchangeRateNew.getTransactionSellRate());
				exchangeRateExisting.setBankNoteBuyRate(exchangeRateNew.getBankNoteBuyRate());
				exchangeRateExisting.setBankNoteSellRate(exchangeRateNew.getBankNoteSellRate());
				exchangeRateExisting.setTellerBuyRate(exchangeRateNew.getTellerBuyRate());
				exchangeRateExisting.setTellerSellRate(exchangeRateNew.getTellerSellRate());
				exchangeRateExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				exchangeRateExisting.setUpdatedBy(vo.getCreatedBy());

				exchangeRateRepo.save(exchangeRateExisting);
				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				
				ExchangeRateModel exchangeRate = getExistingModel((String) map.get("currencyCode"), true);
				
				exchangeRate.setDeleteFlag(ApplicationConstants.YES);
				exchangeRate.setUpdatedDate(DateUtils.getCurrentTimestamp());
				exchangeRate.setUpdatedBy(vo.getCreatedBy());
				
				exchangeRateRepo.save(exchangeRate);
				
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

}
