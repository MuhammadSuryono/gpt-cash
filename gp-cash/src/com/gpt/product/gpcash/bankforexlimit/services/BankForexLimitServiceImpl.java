package com.gpt.product.gpcash.bankforexlimit.services;

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
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.bankforexlimit.model.BankForexLimitModel;
import com.gpt.product.gpcash.bankforexlimit.repository.BankForexLimitRepository;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class BankForexLimitServiceImpl implements BankForexLimitService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private BankForexLimitRepository bankForexLimitRepo;
	
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
			Page<BankForexLimitModel> result = bankForexLimitRepo.search(map, PagingUtils.createPageRequest(map));
			resultMap.put("result", setModelToMap(result.getContent()));
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<BankForexLimitModel> list){
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		for (BankForexLimitModel model : list) {
			
			resultList.add(setModelToMap(model));			
		}
		
		return resultList;
	}
	
	private Map<String, Object> setModelToMap(BankForexLimitModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", ValueUtils.getValue(model.getId()));
		map.put("currencyCode", ValueUtils.getValue(model.getCurrency().getCode()));
		map.put("currencyName", ValueUtils.getValue(model.getCurrency().getName()));
		map.put("minBuyLimit", ValueUtils.getValue(model.getMinBuyLimit(), BigDecimal.ZERO));
		map.put("maxBuyLimit", ValueUtils.getValue(model.getMaxBuyLimit(), BigDecimal.ZERO));
		map.put("minSellLimit", ValueUtils.getValue(model.getMinSellLimit(), BigDecimal.ZERO));
		map.put("maxSellLimit", ValueUtils.getValue(model.getMaxSellLimit(), BigDecimal.ZERO));
		
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		try {
			PendingTaskVO vo = setPendingTaskVO(map); 
			vo.setJsonObject(map); 
			
			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				
				List<Map<String, Object>> bankForexLimitList = (ArrayList<Map<String,Object>>)map.get("bankForexLimitList");
				for(Map<String, Object> bankForexLimitMap : bankForexLimitList){
					String currency = (String)bankForexLimitMap.get("currencyCode");
					checkUniqueRecord(currency); // cek ke table jika record already exist					
				}
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				//jika edit maka simpan pending task untuk old value. Old value diambil dari DB
				List<Map<String, Object>> bankForexLimitList = (ArrayList<Map<String,Object>>)map.get("bankForexLimitList");
				
				List<Map<String, Object>> bankForexLimitListOld = new ArrayList<>(); 
				for(Map<String, Object> bankForexLimitMap : bankForexLimitList){
					BankForexLimitModel limitModel = getExistingRecord(String.valueOf(bankForexLimitMap.get("id")), true);
					bankForexLimitListOld.add(setModelToMap(limitModel));
				}
				Map<String, Object> bankForexLimitOld = new HashMap<>();
				bankForexLimitOld.put("bankForexLimitList", bankForexLimitListOld);
				vo.setJsonObjectOld(bankForexLimitOld);
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				
				List<Map<String, Object>> bankForexLimitList = (ArrayList<Map<String,Object>>)map.get("bankForexLimitList");
				for(Map<String, Object> bankForexLimitMap : bankForexLimitList){
					
					//check existing record exist or not
					getExistingRecord(String.valueOf(bankForexLimitMap.get("id")), true);
				}
				
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
	
	private void checkUniqueRecord(String currencyCode) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("currencyCode", currencyCode);
		
		List<BankForexLimitModel> limit = bankForexLimitRepo.search(map, null).getContent();

		if (!limit.isEmpty()) {
			throw new BusinessException("GPT-0100004");
		}
	}
	
	@SuppressWarnings("unchecked")
	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) throws Exception {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setCreatedBy((String)map.get(ApplicationConstants.LOGIN_USERID)); 
		vo.setMenuCode((String)map.get(ApplicationConstants.STR_MENUCODE)); 
		vo.setService("BankForexLimitSC");
		
		List<Map<String, Object>> bankForexLimitList = (ArrayList<Map<String,Object>>)map.get("bankForexLimitList");

		String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
		
		//check pending task using like for each bank forex limit id
		for(Map<String, Object> bankForexLimitMap : bankForexLimitList){
			String bankForexLimitId = (String)bankForexLimitMap.get("id");
			uniqueKeyAppend = uniqueKeyAppend + bankForexLimitId + ApplicationConstants.DELIMITER_PIPE;
			
			vo.setUniqueKey(bankForexLimitId);
			
			//check unique pending task
			pendingTaskService.checkUniquePendingTaskLike(vo);
			maintenanceRepo.isCurrencyValid((String)bankForexLimitMap.get("currencyCode"));
		}
		
		//set unique key
		vo.setUniqueKey(uniqueKeyAppend);
		
		return vo;
	}
	
	private BankForexLimitModel getExistingRecord(String code, boolean isThrowError) throws BusinessException{
		BankForexLimitModel model = bankForexLimitRepo.findOne(code);
		
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
			
			Map<String, Object> map = (Map<String, Object>)vo.getJsonObject();
			List<Map<String, Object>> limitList = (ArrayList<Map<String,Object>>)map.get("bankForexLimitList");
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				
				for(Map<String, Object> bankForexLimitMap : limitList){
					BankForexLimitModel limitModel = new BankForexLimitModel();
					limitModel.setMaxBuyLimit(new BigDecimal((String) bankForexLimitMap.get("maxBuyLimit")));
					limitModel.setMinBuyLimit(new BigDecimal((String) bankForexLimitMap.get("minBuyLimit")));
					limitModel.setMaxSellLimit(new BigDecimal((String) bankForexLimitMap.get("maxSellLimit")));
					limitModel.setMinSellLimit(new BigDecimal((String) bankForexLimitMap.get("minSellLimit")));
					CurrencyModel currencyForex = new CurrencyModel();
					currencyForex.setCode((String) bankForexLimitMap.get("currencyCode"));
					limitModel.setCurrency(currencyForex);
					
					// set default value
					limitModel.setCreatedDate(DateUtils.getCurrentTimestamp());
					limitModel.setCreatedBy(vo.getCreatedBy());
					limitModel.setDeleteFlag(ApplicationConstants.NO);
					IDMApplicationModel appModel = new IDMApplicationModel();
//					appModel.setCode(ApplicationConstants.APP_GPCASHIB);
					appModel.setCode(Helper.getActiveApplicationCode());
					limitModel.setApplication(appModel);

					bankForexLimitRepo.persist(limitModel);
				}
				
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				
				for(Map<String, Object> bankForexLimitMap : limitList){
					
					BankForexLimitModel limitModel = getExistingRecord((String)bankForexLimitMap.get("id"), true);
					limitModel.setMaxBuyLimit(new BigDecimal((String) bankForexLimitMap.get("maxBuyLimit")));
					limitModel.setMinBuyLimit(new BigDecimal((String) bankForexLimitMap.get("minBuyLimit")));
					limitModel.setMaxSellLimit(new BigDecimal((String) bankForexLimitMap.get("maxSellLimit")));
					limitModel.setMinSellLimit(new BigDecimal((String) bankForexLimitMap.get("minSellLimit")));
					// set default value
					limitModel.setUpdatedDate(DateUtils.getCurrentTimestamp());
					limitModel.setUpdatedBy(vo.getCreatedBy());
					
					bankForexLimitRepo.save(limitModel);
				}				
				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				
				for(Map<String, Object> bankForexLimitMap : limitList){
					BankForexLimitModel limitModel = getExistingRecord((String)bankForexLimitMap.get("id"), true);
					limitModel.setDeleteFlag(ApplicationConstants.YES);
					limitModel.setUpdatedDate(DateUtils.getCurrentTimestamp());
					limitModel.setUpdatedBy(vo.getCreatedBy());
					
					bankForexLimitRepo.save(limitModel);
					
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

	@Override
	public void resetLimit(String parameter) throws ApplicationException, BusinessException {
		try{
			bankForexLimitRepo.resetLimit();
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
	}
}
