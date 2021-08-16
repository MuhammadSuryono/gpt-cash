package com.gpt.component.maintenance.errormapping.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.broadcast.Broadcaster;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.errormapping.model.ErrorMappingModel;
import com.gpt.component.maintenance.errormapping.repository.ErrorMappingRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class ErrorMappingServiceImpl implements ErrorMappingService {

	@Autowired
	private ErrorMappingRepository errorMappingRepo;

	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private Broadcaster broadcaster;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<ErrorMappingModel> result = errorMappingRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<ErrorMappingModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (ErrorMappingModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(ErrorMappingModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("nameId", ValueUtils.getValue(model.getNameId()));
		
		if(isGetDetail){
			map.put("errorFlag", ValueUtils.getValue(model.getErrorFlag()));
			map.put("rollbackFlag", ValueUtils.getValue(model.getRollbackFlag()));
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
				checkUniqueRecord((String) map.get(ApplicationConstants.STR_CODE)); 
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				ErrorMappingModel errorMappingOld = getExistingRecord(
						(String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(errorMappingOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
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

	private ErrorMappingModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		ErrorMappingModel model = errorMappingRepo.findOne(code);

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

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.STR_CODE));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("ErrorMappingSC");

		return vo;
	}

	private ErrorMappingModel setMapToModel(Map<String, Object> map) {
		ErrorMappingModel errorMapping = new ErrorMappingModel();
		errorMapping.setCode((String) map.get(ApplicationConstants.STR_CODE));
		errorMapping.setName((String) map.get(ApplicationConstants.STR_NAME));
		errorMapping.setNameId((String) map.get("nameId"));

		errorMapping.setRollbackFlag(ValueUtils.getValue((String) map.get("rollbackFlag"), ApplicationConstants.NO));
		errorMapping.setErrorFlag(ValueUtils.getValue((String) map.get("errorFlag"), ApplicationConstants.NO));
		
		return errorMapping;
	}
	
	private void checkUniqueRecord(String code) throws Exception {
		ErrorMappingModel model = errorMappingRepo.findOne(code);

		if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}	

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				ErrorMappingModel errorMapping = setMapToModel(map);
				ErrorMappingModel errorMappingExisting = errorMappingRepo
						.findOne(errorMapping.getCode());

				// cek jika record replacement
				if (errorMappingExisting != null
						&& ApplicationConstants.YES.equals(errorMappingExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					errorMappingExisting.setName(errorMapping.getName());
					errorMappingExisting.setNameId(errorMapping.getNameId());
					errorMappingExisting.setRollbackFlag(errorMapping.getRollbackFlag());
					errorMappingExisting.setErrorFlag(errorMapping.getErrorFlag());

					saveErrorMapping(errorMappingExisting, vo.getCreatedBy());
				} else {
					saveErrorMapping(errorMapping, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				ErrorMappingModel errorMappingNew = setMapToModel(map);

				ErrorMappingModel errorMappingExisting = getExistingRecord(errorMappingNew.getCode(), true);

				// set value yg boleh di edit
				errorMappingExisting.setName(errorMappingNew.getName());
				errorMappingExisting.setNameId(errorMappingNew.getNameId());
				errorMappingExisting.setRollbackFlag(errorMappingNew.getRollbackFlag());
				errorMappingExisting.setErrorFlag(errorMappingNew.getErrorFlag());

				updateErrorMapping(errorMappingExisting, vo.getCreatedBy());
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				// check existing record exist or not
				ErrorMappingModel errorMapping = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);

				deleteErrorMapping(errorMapping, vo.getCreatedBy());
			}
			
			broadcaster.broadcast(ApplicationConstants.BROADCAST_LOCALIZATION, (String) map.get(ApplicationConstants.STR_CODE));
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
	public void saveErrorMapping(ErrorMappingModel errorMapping, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		boolean isNew = errorMapping.getDeleteFlag() == null;
		errorMapping.setDeleteFlag(ApplicationConstants.NO);
		errorMapping.setInactiveFlag(ApplicationConstants.NO);
		errorMapping.setSystemFlag(ApplicationConstants.NO);
		errorMapping.setRollbackFlag(errorMapping.getRollbackFlag());
		errorMapping.setErrorFlag(errorMapping.getErrorFlag());
		errorMapping.setCreatedDate(DateUtils.getCurrentTimestamp());
		errorMapping.setCreatedBy(createdBy);
		errorMapping.setUpdatedDate(null);
		errorMapping.setUpdatedBy(null);

		if(isNew)
			errorMappingRepo.persist(errorMapping);
		else
			errorMappingRepo.save(errorMapping);
	}

	@Override
	public void updateErrorMapping(ErrorMappingModel errorMapping, String updatedBy)
			throws ApplicationException, BusinessException {
		errorMapping.setUpdatedDate(DateUtils.getCurrentTimestamp());
		errorMapping.setUpdatedBy(updatedBy);
		errorMappingRepo.save(errorMapping);
	}

	@Override
	public void deleteErrorMapping(ErrorMappingModel errorMapping, String deletedBy)
			throws ApplicationException, BusinessException {
		errorMapping.setDeleteFlag(ApplicationConstants.YES);
		errorMapping.setUpdatedDate(DateUtils.getCurrentTimestamp());
		errorMapping.setUpdatedBy(deletedBy);

		errorMappingRepo.save(errorMapping);
	}

}
