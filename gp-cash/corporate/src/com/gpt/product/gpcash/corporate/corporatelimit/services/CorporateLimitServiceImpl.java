package com.gpt.product.gpcash.corporate.corporatelimit.services;

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
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporate.repository.CorporateRepository;
import com.gpt.product.gpcash.corporate.corporatelimit.model.CorporateLimitModel;
import com.gpt.product.gpcash.corporate.corporatelimit.repository.CorporateLimitRepository;
import com.gpt.product.gpcash.corporate.corporateusergroup.repository.CorporateUserGroupRepository;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;
import com.gpt.product.gpcash.limitpackage.spi.LimitPackageListener;
import com.gpt.product.gpcash.servicecurrencymatrix.model.ServiceCurrencyMatrixModel;
import com.gpt.product.gpcash.servicecurrencymatrix.repository.ServiceCurrencyMatrixRepository;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateLimitServiceImpl implements CorporateLimitService, LimitPackageListener {	
	@Autowired
	private CorporateLimitRepository corporateLimitRepo;
	
	@Autowired
	private MaintenanceRepository maintenanceRepo;
	
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private CorporateRepository corporateRepo;
	
	@Autowired
	private ServiceCurrencyMatrixRepository serviceCurrencyMatrixRepo;
	
	@Autowired
	private CorporateUserGroupRepository corporateUserGroupRepo;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = corporateLimitRepo.findLimit((String)map.get(ApplicationConstants.APP_CODE), ApplicationConstants.SERVICE_TYPE_TRX, (String)map.get(ApplicationConstants.CORP_ID) ,null);
			resultMap.put("result", setModelToMap(result.getContent()));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	public Map<String, Object> searchCorporateLimitList(String corporateId, String applicationCode) throws ApplicationException, BusinessException {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.APP_CODE, applicationCode);
		map.put(ApplicationConstants.CORP_ID, corporateId);
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
				Map<String, Object> corporateLimitOld = new HashMap<>();
				
				List<Map<String, Object>> corporateLimitList = (ArrayList<Map<String,Object>>)map.get("corporateLimitList");

				List<String> corporateLimitListOld = new ArrayList<>(); 
				for(Map<String, Object> corporateLimitMap : corporateLimitList){
					corporateLimitListOld.add((String)corporateLimitMap.get("id"));
				}
				
				Page<Object[]> result = corporateLimitRepo.findLimit((String)map.get(ApplicationConstants.APP_CODE), corporateLimitListOld , null);
				corporateLimitOld.put("corporateLimitList", setModelToMap(result.getContent()));
				vo.setJsonObjectOld(corporateLimitOld);
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
			vo.setService("CorporateLimitSC");
			
			List<Map<String, Object>> corporateLimitList = (ArrayList<Map<String,Object>>)map.get("corporateLimitList");
	
			String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
			
			//check pending task using like for each bank trx limit id
			for(Map<String, Object> corporateLimitMap : corporateLimitList){
				String corporateLimitId = (String)corporateLimitMap.get("id");
				uniqueKeyAppend = uniqueKeyAppend + corporateLimitId + ApplicationConstants.DELIMITER_PIPE;
				
				//check unique pending task
				vo.setUniqueKey(corporateLimitId);
				pendingTaskService.checkUniquePendingTaskLike(vo);
				
				getExistingRecord(corporateLimitId, true);
				
				maintenanceRepo.isCurrencyValid((String)corporateLimitMap.get("currencyCode"));
				productRepo.isServiceValid((String)corporateLimitMap.get("serviceCode"));
			}
			
			//set unique key
			vo.setUniqueKey(uniqueKeyAppend);
			vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.CORP_ID)).concat(ApplicationConstants.DELIMITER_DASH)
					.concat((String) map.get(ApplicationConstants.STR_NAME)));
			
			return vo;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private CorporateLimitModel getExistingRecord(String code, boolean isThrowError) throws BusinessException{
		CorporateLimitModel model = corporateLimitRepo.findOne(code);
		
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
					
					List<Map<String, Object>> list = (ArrayList<Map<String,Object>>)map.get("corporateLimitList");
					
					for(Map<String, Object> corporateLimitMap : list){
						String corporateLimitId = (String)corporateLimitMap.get("id");
						int maxOccurrenceLimit = Integer.parseInt((String) corporateLimitMap.get("maxOccurrenceLimit"));
						BigDecimal maxAmountLimit = new BigDecimal((String) corporateLimitMap.get("maxAmountLimit"));
						String currencyCode = (String)corporateLimitMap.get("currencyCode");
						
						CorporateLimitModel corporateLimit = getExistingRecord(corporateLimitId, true);
						corporateLimit.setMaxAmountLimit(maxAmountLimit);
						corporateLimit.setMaxOccurrenceLimit(maxOccurrenceLimit);
						
						CurrencyModel currency = new CurrencyModel();
						currency.setCode(currencyCode);
						corporateLimit.setCurrency(currency);
						
						corporateLimit.setUpdatedDate(DateUtils.getCurrentTimestamp());
						corporateLimit.setUpdatedBy(vo.getCreatedBy());
						
						//TODO cek ke bank limit
						
						corporateLimitRepo.save(corporateLimit);
					}			
					
					CorporateModel corporate = corporateRepo.findOne((String) map.get(ApplicationConstants.CORP_ID));
					corporate.setSpecialLimitFlag(ApplicationConstants.YES);
					corporateRepo.save(corporate);
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
			corporateLimitRepo.resetLimit();
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void updateALLLimitByLimitPackageCode(BigDecimal maxAmountLimit, int maxOccurrenceLimit, 
			String limitPackageCode, String currencyCode, String serviceCode, String serviceCurrencyMatrixId,
			String isUpdateCorporateWithSpecialLimit) throws Exception {
		if(ApplicationConstants.NO.equals(isUpdateCorporateWithSpecialLimit)) {
			//only update corp without special charge
			corporateLimitRepo.updateLimitsByLimitPackageCode(maxAmountLimit, maxOccurrenceLimit, limitPackageCode, 
					currencyCode, serviceCode, serviceCurrencyMatrixId, ApplicationConstants.NO);
		} else {
			//update all (corp with special limit and without special limit)
			corporateLimitRepo.updateLimitsByLimitPackageCode(maxAmountLimit, maxOccurrenceLimit, limitPackageCode, 
					currencyCode, serviceCode, serviceCurrencyMatrixId);
		}
		
		
//		updateCorporateUserGroupLimit(isUpdateCorporateWithSpecialLimit, maxAmountLimit, serviceCode, serviceCurrencyMatrixId, currencyCode, limitPackageCode);
		
	}
	
	//di pikir2 dl sama susi, karena udah ada pengecekan user group limit dan corporate limit sewaktu transaksi
	private void updateCorporateUserGroupLimit(String specialLimitFlag, BigDecimal maxAmountLimit, String serviceCode, String serviceCurrencyMatrixId,
			String currencyCode, String limitPackageCode) throws Exception {
		//requesed by Susi (12 September 2017)
		//jika isUpdateCorporateWithSpecialLimit = Y maka update ke corporate user group detail limit
		if(ApplicationConstants.YES.equals(specialLimitFlag)) {
			//cari corporate user group yg memakai limitPackageCode
			List<String> corporateUserGroupIdList = corporateUserGroupRepo.findUserGroupIdByLimitPackageCode(limitPackageCode);
			
			//update amount limit berdasarkan corporateUserGroupId
			corporateUserGroupRepo.updateAmountLimit(maxAmountLimit, serviceCode, serviceCurrencyMatrixId, currencyCode, corporateUserGroupIdList);
		}
	}

	@Override
	public void saveNewLimitByLimitPackageCode(BigDecimal maxAmountLimit, int maxTrxPerDay, String limitPackageCode,
			String currencyCode, String serviceCurrencyMatrixId, String createdBy) throws ApplicationException, BusinessException {
		try {
			//find corporate by charge package code
			List<String> corporates = corporateLimitRepo.findCorporatesByLimitPackage(limitPackageCode);
			
			for(String corporateStr : corporates) {
				//insert new service limit to corporate
				CorporateLimitModel corporateLimit = new CorporateLimitModel();
				
				CorporateModel corporate = new CorporateModel();
				corporate.setId(corporateStr);
				corporateLimit.setCorporate(corporate);
				
				ServiceCurrencyMatrixModel serviceCurrencyMatrix = serviceCurrencyMatrixRepo.findOne(serviceCurrencyMatrixId);
				
				corporateLimit.setMaxAmountLimit(maxAmountLimit);
				corporateLimit.setMaxOccurrenceLimit(maxTrxPerDay);
				corporateLimit.setAmountLimitUsage(BigDecimal.ZERO);
				corporateLimit.setOccurrenceLimitUsage(0);
				
				corporateLimit.setService(serviceCurrencyMatrix.getService());
				corporateLimit.setServiceCurrencyMatrix(serviceCurrencyMatrix);
				
				LimitPackageModel limitPackage = new LimitPackageModel();
				limitPackage.setCode(limitPackageCode);
				corporateLimit.setLimitPackage(limitPackage);
				
				IDMApplicationModel application = new IDMApplicationModel();
				application.setCode(ApplicationConstants.APP_GPCASHIB);
				corporateLimit.setApplication(application);
				
				CurrencyModel currency = new CurrencyModel();
				currency.setCode(currencyCode);
				corporateLimit.setCurrency(currency);
				
				corporateLimit.setCreatedBy(createdBy);
				corporateLimit.setCreatedDate(DateUtils.getCurrentTimestamp());
				
				corporateLimitRepo.persist(corporateLimit);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public void updateALLLimit(BigDecimal maxAmountLimit, int maxTrxPerDay, 
			String currencyCode, String serviceCurrencyMatrixId, String isUpdateCorporateWithSpecialCharge) {
		
		if(ApplicationConstants.NO.equals(isUpdateCorporateWithSpecialCharge)) {
			//only update corp without special charge
			corporateLimitRepo.updateAllLimits(maxAmountLimit, maxTrxPerDay, currencyCode, serviceCurrencyMatrixId, ApplicationConstants.NO);
		} else {
			//update all (corp with special charge and without special charge)
			corporateLimitRepo.updateAllLimits(maxAmountLimit, maxTrxPerDay, currencyCode, serviceCurrencyMatrixId);
		}
	}
	
	@Override
	public void updateByLimitPackageCode(BigDecimal maxAmountLimit, int maxTrxPerDay, 
			String currencyCode, String serviceCode, String serviceCurrencyMatrixId, String limitPackageCode) {
		
		//update spesific corporate charge
		corporateLimitRepo.updateLimitsByLimitPackageCode(maxAmountLimit, maxTrxPerDay, limitPackageCode, 
				currencyCode, serviceCode, serviceCurrencyMatrixId);
	}
}
