package com.gpt.product.gpcash.banktransactionlimit.services;

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
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.banktransactionlimit.model.BankTransactionLimitModel;
import com.gpt.product.gpcash.banktransactionlimit.repository.BankTransactionLimitRepository;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class BankTransactionLimitServiceImpl implements BankTransactionLimitService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private BankTransactionLimitRepository bankTransactionLimitRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = bankTransactionLimitRepo.findDetailByServiceTypeCode((String)map.get(ApplicationConstants.APP_CODE), ApplicationConstants.SERVICE_TYPE_TRX ,null);
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
			
			resultList.add(setModelToMap(model));			
		}
		
		return resultList;
	}
	
	private Map<String, Object> setModelToMap(Object[] model) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", model[0]);
		map.put("serviceCode", model[1]);
		map.put("serviceName", model[2]);
		map.put("minAmountLimit", ((BigDecimal) model[3]).toPlainString());
		map.put("maxAmountLimit", ((BigDecimal) model[4]).toPlainString());
		map.put("currencyCode", model[5]);
		map.put("currencyName", model[6]);
		map.put("currencyMatrixName", model[8]);
		
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
				List<Map<String, Object>> bankTransactionLimitList = (ArrayList<Map<String,Object>>)map.get("bankTransactionLimitList");

				List<String> bankTransactionLimitListOld = new ArrayList<>(); 
				for(Map<String, Object> bankTrxLimitMap : bankTransactionLimitList){
					bankTransactionLimitListOld.add((String)bankTrxLimitMap.get("id"));
				}
				
				Page<Object[]> result = bankTransactionLimitRepo.findDetailByIds((String)map.get(ApplicationConstants.APP_CODE), bankTransactionLimitListOld ,null);
				
				Map<String, Object> bankTransactionLimitOld = new HashMap<>();
				bankTransactionLimitOld.put("bankTransactionLimitList", setModelToMap(result.getContent()));
				vo.setJsonObjectOld(bankTransactionLimitOld);
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
	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) throws Exception {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setCreatedBy((String)map.get(ApplicationConstants.LOGIN_USERID)); 
		vo.setMenuCode((String)map.get(ApplicationConstants.STR_MENUCODE)); 
		vo.setService("BankTransactionLimitSC");
		
		List<Map<String, Object>> bankTransactionLimitList = (ArrayList<Map<String,Object>>)map.get("bankTransactionLimitList");

		String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
		
		//check pending task using like for each bank trx limit id
		for(Map<String, Object> bankTrxLimitMap : bankTransactionLimitList){
			String bankTransactionLimitId = (String)bankTrxLimitMap.get("id");
			uniqueKeyAppend = uniqueKeyAppend + bankTransactionLimitId + ApplicationConstants.DELIMITER_PIPE;
			
			vo.setUniqueKey(bankTransactionLimitId);
			
			//check unique pending task
			pendingTaskService.checkUniquePendingTaskLike(vo);
			
			getExistingRecord(bankTransactionLimitId, true);
			
			maintenanceRepo.isCurrencyValid((String)bankTrxLimitMap.get("currencyCode"));
			productRepo.isServiceValid((String)bankTrxLimitMap.get("serviceCode"));
		}
		
		//set unique key
		vo.setUniqueKey(uniqueKeyAppend);
		
		return vo;
	}
	
	private BankTransactionLimitModel getExistingRecord(String code, boolean isThrowError) throws BusinessException{
		BankTransactionLimitModel model = bankTransactionLimitRepo.findOne(code);
		
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
					
					List<Map<String, Object>> list = (ArrayList<Map<String,Object>>)map.get("bankTransactionLimitList");
					
					for(Map<String, Object> bankTransactionLimitMap : list){
						String bankTransactionLimitId = (String)bankTransactionLimitMap.get("id");
						BigDecimal minAmountLimit = new BigDecimal((String) bankTransactionLimitMap.get("minAmountLimit"));
						BigDecimal maxAmountLimit = new BigDecimal((String) bankTransactionLimitMap.get("maxAmountLimit"));
						String currencyCode = (String)bankTransactionLimitMap.get("currencyCode");
						
						BankTransactionLimitModel bankTransactionLimit = getExistingRecord(bankTransactionLimitId, true);
						bankTransactionLimit.setMinAmountLimit(minAmountLimit);
						bankTransactionLimit.setMaxAmountLimit(maxAmountLimit);
						
						CurrencyModel currency = new CurrencyModel();
						currency.setCode(currencyCode);
						bankTransactionLimit.setCurrency(currency);
						
						bankTransactionLimit.setUpdatedDate(DateUtils.getCurrentTimestamp());
						bankTransactionLimit.setUpdatedBy(vo.getCreatedBy());
						
						bankTransactionLimitRepo.save(bankTransactionLimit);
					}				
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
}
