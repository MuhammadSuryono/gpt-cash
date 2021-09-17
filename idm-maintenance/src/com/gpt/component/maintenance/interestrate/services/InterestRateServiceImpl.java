package com.gpt.component.maintenance.interestrate.services;

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
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.interestrate.model.InterestRateDetailModel;
import com.gpt.component.maintenance.interestrate.model.InterestRateModel;
import com.gpt.component.maintenance.interestrate.repository.InterestRateRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class InterestRateServiceImpl implements InterestRateService {

	@Autowired
	private InterestRateRepository interestRateRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<InterestRateModel> result = interestRateRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<InterestRateModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (InterestRateModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}
		return resultList;
	}
	
	private Map<String, Object> setModelToMap(InterestRateModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("id", ValueUtils.getValue(model.getId()));
		map.put("productCode", ValueUtils.getValue(model.getProductCode()));
		map.put("productName", ValueUtils.getValue(model.getProductName()));
		
		if(isGetDetail){
			
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
			
			if (model.getInterestDetail().size() > 0) {
				map.put("details", setModelToMapDetail(model.getInterestDetail()));
			}
			
		}
		
		return map;
	}

	@SuppressWarnings("unchecked")
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
				String productCode = (String) map.get("productCode");
				
				validateUniqueCode(productCode);
				
				List<Map<String, Object>> rateList = (ArrayList<Map<String, Object>>) map.get("rateList");
				for (Map<String, Object> rateMap : rateList) {
					
					String balance = (String) rateMap.get("balance");
					String period = (String) rateMap.get("period");
					checkUniqueRecord(productCode, balance, period); // cek ke table jika record already exist
					
				}
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);
				
				List<Map<String, Object>> rateList = (ArrayList<Map<String, Object>>) map.get("rateList");
				List<Map<String, Object>> rateListOld = new ArrayList<>();
				
				InterestRateModel interestRateOld = getExistingRate((String)map.get("id"), true);
				
				for (Map<String, Object> rateMap : rateList) {
					
					String productCode = interestRateOld.getProductCode();
					String balance = (String) rateMap.get("balance");
					String period = (String) rateMap.get("period");
					InterestRateDetailModel interestRateDetailOld = getExistingDetail(productCode, balance, period, false);
					if(interestRateDetailOld!= null) {
						rateListOld.add(setModelToMapDetail(interestRateDetailOld));
					}
				}
				Map<String, Object> rateMap = new HashMap<>();
				rateMap.put("interestRate", setModelToMap(interestRateOld, true));
				rateMap.put("rateList", rateListOld);
				vo.setJsonObjectOld(rateMap);
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				getExistingRate((String)map.get("id"), true);
				
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

	private void validateUniqueCode(String productCode) throws Exception {
		
		List<InterestRateModel> interestList = null;
		interestList = interestRateRepo.findByProductCode(productCode);
		
		if (interestList.size() > 0) {
			throw new BusinessException("GPT-0100004");
		}
	}

	private InterestRateModel getExistingRate(String id, boolean isThrowError) throws BusinessException {
		InterestRateModel interestRate = interestRateRepo.findOne(id);
		
		if (interestRate == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		} else {
			if (ApplicationConstants.YES.equals(interestRate.getDeleteFlag())) {
				if (isThrowError) {
					throw new BusinessException("GPT-0100001");
				} else {
					return null;
				}
			}
		}
		
		return interestRate;
	}

	private InterestRateDetailModel getExistingDetail(String productCode, String balance, String period, boolean isThrowError) throws BusinessException, Exception {
		
		List<InterestRateDetailModel> interestList = null;
		InterestRateDetailModel interestDetail = null;
		
		if (ValueUtils.hasValue(period)) {
			interestList = interestRateRepo.findByProductCodeBalanceAndPeriod(productCode, balance, period);
		} else {
			interestList = interestRateRepo.findByProductCodeBalanceAndPeriodIsNull(productCode, balance);
		}

		if(!ValueUtils.hasValue(interestList)){
			if(isThrowError)
				throw new BusinessException("GPT-0100001");
		} else {
			interestDetail = interestList.get(0);
		}
		
		return interestDetail;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get("productCode"));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("InterestRateSC");

		return vo;
	}

	@SuppressWarnings("unchecked")
	private InterestRateModel setMapToModel(Map<String, Object> map, String createdBy) {
		InterestRateModel interestRate = new InterestRateModel();
		interestRate.setId((String)map.get("id"));
		interestRate.setProductCode((String)map.get("productCode"));
		interestRate.setProductName((String)map.get("productName"));
		
		List<Map<String, Object>> rateList = (List<Map<String, Object>>) map.get("rateList");
		List<InterestRateDetailModel> listDetail = new ArrayList<>();
		for (Map<String, Object> mapList : rateList) {
			
			InterestRateDetailModel interestDetail = new InterestRateDetailModel();
			interestDetail.setBalance((String)mapList.get("balance"));
			interestDetail.setPeriod((String)mapList.get("period"));
			interestDetail.setInterestRate((String)mapList.get("interestRate"));
			interestDetail.setDeleteFlag(ApplicationConstants.NO);
			interestDetail.setIdx((Integer) mapList.get("idx"));
			interestDetail.setInterest(interestRate);
			listDetail.add(interestDetail);
		}
		
		interestRate.setInterestDetail(listDetail);

		return interestRate;
	}

	private void checkUniqueRecord(String productCode, String balance, String period) throws Exception {
		
		List<InterestRateDetailModel> interestList = null;
		
		if (ValueUtils.hasValue(period)) {
			interestList = interestRateRepo.findByProductCodeBalanceAndPeriod(productCode, balance, period);
		} else {
			interestList = interestRateRepo.findByProductCodeBalanceAndPeriodIsNull(productCode, balance);
		}
		
		if (interestList.size() > 0) {
			throw new BusinessException("GPT-0100004");
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				
				InterestRateModel interestRate = setMapToModel(map, vo.getCreatedBy());
				
				interestRate.setCreatedDate(DateUtils.getCurrentTimestamp());
				interestRate.setCreatedBy(vo.getCreatedBy());
				interestRate.setDeleteFlag(ApplicationConstants.NO);
				
				interestRateRepo.persist(interestRate);				

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				
				InterestRateModel interestRateNew = setMapToModel(map, vo.getCreatedBy());
				
				InterestRateModel interestRateExisting = interestRateRepo.findOne((String)map.get("id"));
				
				//delete existing detail
				interestRateRepo.deleteByInterestRateId(interestRateExisting.getId());
				
				interestRateExisting.setProductName(interestRateNew.getProductName());
				interestRateExisting.setUpdatedBy(vo.getCreatedBy());
				interestRateExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				
				//add new detail
				interestRateExisting.setInterestDetail(interestRateNew.getInterestDetail());
				
				interestRateRepo.save(interestRateExisting);

				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				
				InterestRateModel interestRate = interestRateRepo.findOne((String)map.get("id"));
				
				if (interestRate == null) {
					throw new BusinessException("GPT-0100001");
				}
				
				interestRate.setDeleteFlag(ApplicationConstants.YES);
				interestRate.setUpdatedDate(DateUtils.getCurrentTimestamp());
				interestRate.setUpdatedBy(vo.getCreatedBy());
				
				//delete Detail
//				interestRateRepo.deleteByInterestRateId(interestRate.getId());
				
//				interestRateRepo.delete(interestRate);
				interestRateRepo.save(interestRate);
				
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

	@Override
	public void validateDetail(String productCode, String balance, String period) throws ApplicationException, BusinessException {
		try {
			this.checkUniqueRecord(productCode, balance, period);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
	}

	@Override
	public Map<String, Object> searchInterestDetailById(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<InterestRateDetailModel> result = interestRateRepo.findDetailByInterestId((String)map.get("id"), PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMapDetail(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMapDetail(List<InterestRateDetailModel> list) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (InterestRateDetailModel model : list) {
			resultList.add(setModelToMapDetail(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMapDetail(InterestRateDetailModel model) {
		Map<String, Object> map = new HashMap<>();
		
		map.put("id", model.getId());
		map.put("balance", model.getBalance());
		map.put("period", ValueUtils.getValue(model.getPeriod()));
		map.put("interestRate", model.getInterestRate());
		map.put("idx", model.getIdx());
		
		return map;
	}

}
