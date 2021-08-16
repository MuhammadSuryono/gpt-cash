package com.gpt.component.maintenance.denomprepaid.services;

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
import com.gpt.component.maintenance.denomprepaid.model.PrepaidDenominationModel;
import com.gpt.component.maintenance.denomprepaid.repository.PrepaidDenominationRepository;
import com.gpt.component.maintenance.parametermt.model.PrepaidDenominationTypeModel;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class PrepaidDenominationServiceImpl implements PrepaidDenominationService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private PrepaidDenominationRepository denomRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<PrepaidDenominationModel> result = denomRepo.search(map, PagingUtils.createPageRequest(map));
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
	
	private List<Map<String, Object>> setModelToMap(List<PrepaidDenominationModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PrepaidDenominationModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(PrepaidDenominationModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("denomination", ValueUtils.getValue(model.getDenomination()));
		map.put("denomTypeName", ValueUtils.getValue(model.getPrepaidType().getName()));
		
		if(isGetDetail){
			map.put("dscp", ValueUtils.getValue(model.getDscp()));
			map.put("denomTypeCode", ValueUtils.getValue(model.getPrepaidType().getCode()));
//			map.put("denomTypeName", ValueUtils.getValue(model.getPrepaidType().getName()));
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}

		return map;
	}
	
	private List<Map<String, Object>> setModelToMapDroplist(List<PrepaidDenominationModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PrepaidDenominationModel model : list) {
			resultList.add(setModelToMapDroplist(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMapDroplist(PrepaidDenominationModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		
		//untuk kebutuhan automatic di screen
		map.put(ApplicationConstants.STR_CODE, model.getDenomination());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("packageCode", ValueUtils.getValue(model.getCode()));
		
		
		if(isGetDetail){
			map.put("dscp", ValueUtils.getValue(model.getDscp()));
			map.put("denomTypeCode", ValueUtils.getValue(model.getPrepaidType().getCode()));
			map.put("denomTypeName", ValueUtils.getValue(model.getPrepaidType().getName()));
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
				checkUniqueRecord((String) map.get(ApplicationConstants.STR_CODE)); // cek ke table jika record already exist
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				PrepaidDenominationModel prepaidDenomOld = getExistingModel((String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(prepaidDenomOld, true));
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
		vo.setService("PrepaidDenominationSC");

		return vo;
	}
	
	private void checkUniqueRecord(String code) throws Exception {
		PrepaidDenominationModel denom = denomRepo.findOne(code);

		if (denom != null && denom.getDeleteFlag().equals(ApplicationConstants.NO)) {
			throw new BusinessException("GPT-0100004");
		}
	}
	
	private PrepaidDenominationModel getExistingModel(String code, boolean isThrowError) throws BusinessException {
		PrepaidDenominationModel model = denomRepo.findOne(code);

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
				PrepaidDenominationModel prepaidDenom = setMapToModel(map);
				PrepaidDenominationModel prepaidDenomExisting = denomRepo.findOne(prepaidDenom.getCode());
				
				// cek jika record replacement
				if (prepaidDenomExisting != null && ApplicationConstants.YES.equals(prepaidDenomExisting.getDeleteFlag())) {
					
					prepaidDenomExisting.setName(prepaidDenom.getName());
					prepaidDenomExisting.setDscp(prepaidDenom.getDscp());
					prepaidDenomExisting.setDenomination(prepaidDenom.getDenomination());
					prepaidDenomExisting.setPrepaidType(prepaidDenom.getPrepaidType());				
					
					// set default value
					prepaidDenomExisting.setIdx(ApplicationConstants.IDX);
					prepaidDenomExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					prepaidDenomExisting.setCreatedBy(vo.getCreatedBy());
					prepaidDenomExisting.setDeleteFlag(ApplicationConstants.NO);
					prepaidDenomExisting.setInactiveFlag(ApplicationConstants.NO);
					
					// set reset value
					prepaidDenomExisting.setUpdatedDate(null);
					prepaidDenomExisting.setUpdatedBy(null);
					
					denomRepo.save(prepaidDenomExisting);
					
				} else {
					
					// set default value
					prepaidDenom.setIdx(ApplicationConstants.IDX);
					prepaidDenom.setCreatedDate(DateUtils.getCurrentTimestamp());
					prepaidDenom.setCreatedBy(vo.getCreatedBy());
					prepaidDenom.setDeleteFlag(ApplicationConstants.NO);
					prepaidDenom.setInactiveFlag(ApplicationConstants.NO);
					
					denomRepo.persist(prepaidDenom);
					
				}
				

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				PrepaidDenominationModel prepaidDenomNew = setMapToModel(map);

				PrepaidDenominationModel prepaidDenomExisting = getExistingModel(prepaidDenomNew.getCode(), true);

				// set value yg boleh di edit
				prepaidDenomExisting.setName(prepaidDenomNew.getName());
				prepaidDenomExisting.setDscp(prepaidDenomNew.getDscp());
				prepaidDenomExisting.setDenomination(prepaidDenomNew.getDenomination());
				prepaidDenomExisting.setPrepaidType(prepaidDenomNew.getPrepaidType());
				
				prepaidDenomExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				prepaidDenomExisting.setUpdatedBy(vo.getCreatedBy());

				denomRepo.save(prepaidDenomExisting);
				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				
				PrepaidDenominationModel prepaidDenom = getExistingModel((String) map.get(ApplicationConstants.STR_CODE), true);
				
				prepaidDenom.setDeleteFlag(ApplicationConstants.YES);
				prepaidDenom.setUpdatedDate(DateUtils.getCurrentTimestamp());
				prepaidDenom.setUpdatedBy(vo.getCreatedBy());
				
				denomRepo.save(prepaidDenom);
				
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

	private PrepaidDenominationModel setMapToModel(Map<String, Object> map) {
		PrepaidDenominationModel prepaidDenom = new PrepaidDenominationModel();
		prepaidDenom.setCode((String) map.get(ApplicationConstants.STR_CODE));
		prepaidDenom.setName((String) map.get(ApplicationConstants.STR_NAME));
		
		if (ValueUtils.hasValue(map.get("dscp")))
			prepaidDenom.setDscp((String) map.get("dscp"));
		
		if (ValueUtils.hasValue(map.get("denomination")))
			prepaidDenom.setDenomination((String) map.get("denomination"));
		
		if (ValueUtils.hasValue(map.get("denomTypeCode"))) {
			PrepaidDenominationTypeModel prepaidType = new PrepaidDenominationTypeModel();
			prepaidType.setCode((String)map.get("denomTypeCode"));
			prepaidDenom.setPrepaidType(prepaidType);
		}
		
		return prepaidDenom;
	}

	@Override
	public Map<String, Object> searchByDenomType(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<PrepaidDenominationModel> result = denomRepo.findByPrepaidType_CodeAndDeleteFlagOrderByDenominationAsc((String) map.get("modelCode"), ApplicationConstants.NO, PagingUtils.createPageRequest(map));
			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
				resultMap.put("result", setModelToMapDroplist(result.getContent(), false));
			} else {
				resultMap.put("result", setModelToMapDroplist(result.getContent(), true));
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
