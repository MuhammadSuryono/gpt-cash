package com.gpt.product.gpcash.corporate.transaction.tax.deposittype.services;

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
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.transaction.tax.deposittype.model.DepositTypeModel;
import com.gpt.product.gpcash.corporate.transaction.tax.deposittype.repository.DepositTypeRepository;
import com.gpt.product.gpcash.corporate.transaction.tax.taxtype.model.TaxTypeModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class DepositTypeServiceImpl implements DepositTypeService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private DepositTypeRepository depositTypeRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<DepositTypeModel> result = depositTypeRepo.search(map, PagingUtils.createPageRequest(map));
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
	
	private List<Map<String, Object>> setModelToMap(List<DepositTypeModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (DepositTypeModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(DepositTypeModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("taxDepositCode", ValueUtils.getValue(model.getTaxDepositCode()));
		
		
		if(isGetDetail){
			map.put("dscp", ValueUtils.getValue(model.getDscp()));
			map.put("taxTypeCode", ValueUtils.getValue(model.getTaxType().getCode()));
			map.put("taxTypeName", ValueUtils.getValue(model.getTaxType().getName()));
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
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				DepositTypeModel depositTypeOld = getExistingModel((String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(depositTypeOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				
				//check existing record exist or not
				getExistingModel((String) map.get(ApplicationConstants.STR_CODE), true);

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
	
	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.STR_CODE));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("DepositTypeSC");

		return vo;
	}
	
	@SuppressWarnings("unchecked")
	private void checkUniqueRecord(PendingTaskVO vo) throws Exception {
		Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
		List<DepositTypeModel> list = depositTypeRepo.search(map, null).getContent();

		if (!list.isEmpty()) {
			throw new BusinessException("GPT-0100004");
		}
	}
	
	private DepositTypeModel getExistingModel(String code, boolean isThrowError) throws BusinessException {
		DepositTypeModel model = depositTypeRepo.findOne(code);

		if (model == null) {
			if (isThrowError)
				throw new BusinessException("GPT-0100001");
		} else {
			if (ApplicationConstants.YES.equals(model.getDeleteFlag())) {
				if (isThrowError) {
					throw new BusinessException("GPT-0100001");
				} else {
					return null;
				}
			}
		}

		return model;
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				DepositTypeModel depositType = setMapToModel(map);
				DepositTypeModel depositTypeExisting = depositTypeRepo.findOne(depositType.getCode());
				
				// cek jika record replacement
				if (depositTypeExisting != null && ApplicationConstants.YES.equals(depositTypeExisting.getDeleteFlag())) {
					
					depositTypeExisting.setName(depositType.getName());
					depositTypeExisting.setDscp(depositType.getDscp());
					depositTypeExisting.setTaxDepositCode(depositType.getTaxDepositCode());
					depositTypeExisting.setTaxType(depositType.getTaxType());				
					
					// set default value
					depositTypeExisting.setIdx(ApplicationConstants.IDX);
					depositTypeExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					depositTypeExisting.setCreatedBy(vo.getCreatedBy());
					depositTypeExisting.setDeleteFlag(ApplicationConstants.NO);
					depositTypeExisting.setInactiveFlag(ApplicationConstants.NO);
					
					// set reset value
					depositTypeExisting.setUpdatedDate(null);
					depositTypeExisting.setUpdatedBy(null);
					
					depositTypeRepo.save(depositTypeExisting);
					
				} else {
					
					// set default value
					depositType.setIdx(ApplicationConstants.IDX);
					depositType.setCreatedDate(DateUtils.getCurrentTimestamp());
					depositType.setCreatedBy(vo.getCreatedBy());
					depositType.setDeleteFlag(ApplicationConstants.NO);
					depositType.setInactiveFlag(ApplicationConstants.NO);
					
					depositTypeRepo.persist(depositType);
					
				}
				

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				DepositTypeModel depositTypeNew = setMapToModel(map);

				DepositTypeModel depositTypeExisting = getExistingModel(depositTypeNew.getCode(), true);

				// set value yg boleh di edit
				depositTypeExisting.setName(depositTypeNew.getName());
				depositTypeExisting.setDscp(depositTypeNew.getDscp());
				depositTypeExisting.setTaxDepositCode(depositTypeNew.getTaxDepositCode());
				depositTypeExisting.setTaxType(depositTypeNew.getTaxType());
				
				depositTypeExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				depositTypeExisting.setUpdatedBy(vo.getCreatedBy());

				depositTypeRepo.save(depositTypeExisting);
				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				
				DepositTypeModel depositType = getExistingModel((String) map.get(ApplicationConstants.STR_CODE), true);
				
				depositType.setDeleteFlag(ApplicationConstants.YES);
				depositType.setUpdatedDate(DateUtils.getCurrentTimestamp());
				depositType.setUpdatedBy(vo.getCreatedBy());
				
				depositTypeRepo.save(depositType);
				
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
		return vo;
	}

	private DepositTypeModel setMapToModel(Map<String, Object> map) {
		DepositTypeModel depositType = new DepositTypeModel();
		depositType.setCode((String) map.get(ApplicationConstants.STR_CODE));
		depositType.setName((String) map.get(ApplicationConstants.STR_NAME));
		
		if (ValueUtils.hasValue(map.get("dscp")))
			depositType.setDscp((String) map.get("dscp"));
		
		if (ValueUtils.hasValue(map.get("taxDepositCode")))
			depositType.setTaxDepositCode((String) map.get("taxDepositCode"));
		
		if (ValueUtils.hasValue(map.get("taxTypeCode"))) {
			TaxTypeModel taxType = new TaxTypeModel();
			taxType.setCode((String)map.get("taxTypeCode"));
			depositType.setTaxType(taxType);
		}
		
		return depositType;
	}

	@Override
	public Map<String, Object> searchByTaxType(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<DepositTypeModel> result = depositTypeRepo.findByTaxType_CodeAndDeleteFlagOrderByTaxDepositCodeAsc((String) map.get("taxType"), ApplicationConstants.NO, PagingUtils.createPageRequest(map));
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
	
}
