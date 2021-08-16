package com.gpt.component.maintenance.parametermt.services;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.parametermt.model.ParameterMaintenanceModel;
import com.gpt.component.maintenance.parametermt.repository.ParameterMaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;

@Service
@Transactional(rollbackFor = Exception.class)
public class ParameterMaintenanceServiceImpl implements ParameterMaintenanceService {

	@Autowired
	private ParameterMaintenanceRepository parameterMaintenanceRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			resultMap.put("result", setModelToMap(parameterMaintenanceRepo.search(map, null).getContent()));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<ParameterMaintenanceModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (ParameterMaintenanceModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.STR_CODE, model.getCode());
			map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
			map.put("parentLabel", ValueUtils.getValue(model.getParentName()));
			map.put("parentModelCode", ValueUtils.getValue(model.getParentModelCode()));
			resultList.add(map);

		}

		return resultList;
	}

	@Override
	public Map<String, Object> searchModel(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object> result = parameterMaintenanceRepo.searchModel(map, PagingUtils.createPageRequest(map));

			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
				resultMap.put("result", setModelToMapForModel(result.getContent(), map, false));
			} else {
				resultMap.put("result", setModelToMapForModel(result.getContent(), map, true));
			}

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMapForModel(List<Object> list, Map<String, Object> map, boolean isGetDetail)
			throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (Object obj : list) {
			resultList.add(setModelToMap(obj, map, isGetDetail));
		}
		return resultList;
	}

	private Map<String, Object> setModelToMap(Object obj, Map<String, Object> map, boolean isGetDetail) throws Exception {
		Class<?> clazz = obj.getClass();

		Map<String, Object> data = new HashMap<>();

		data.put(ApplicationConstants.STR_CODE, (String) clazz.getDeclaredField(ApplicationConstants.STR_CODE).get(obj));
		data.put(ApplicationConstants.STR_NAME, ValueUtils.getValue((String) clazz.getDeclaredField(ApplicationConstants.STR_NAME).get(obj)));
		data.put("modelCode", (String) map.get("modelCode")); //balikin lg untuk ui
		
		if(String.valueOf(map.get("modelCode")).equalsIgnoreCase("COM_MT_PEMDA_CODE")) {
			data.put("pemdaName", ValueUtils.getValue((String) clazz.getDeclaredField("dscp").get(obj)));
			data.put("bphtbId", ValueUtils.getValue((String) clazz.getDeclaredField("bphtbId").get(obj)));
		}
		
		if(isGetDetail){
			data.put("dscp", ValueUtils.getValue((String) clazz.getDeclaredField("dscp").get(obj)));
			data.put("inactiveFlag", ValueUtils.getValue((String) clazz.getDeclaredField("inactiveFlag").get(obj)));
			data.put("systemFlag", ValueUtils.getValue((String) clazz.getDeclaredField("systemFlag").get(obj)));
			data.put("createdBy", ValueUtils.getValue((String) clazz.getDeclaredField("createdBy").get(obj)));
			data.put("createdDate", ValueUtils.getValue((Timestamp) clazz.getDeclaredField("createdDate").get(obj)));
			data.put("updatedBy", ValueUtils.getValue((String) clazz.getDeclaredField("updatedBy").get(obj)));
			data.put("updatedDate", ValueUtils.getValue((Timestamp) clazz.getDeclaredField("updatedDate").get(obj)));
		
			// cek jika ada parent property atau tidak
			ParameterMaintenanceModel parameter = parameterMaintenanceRepo.findOne((String) map.get("modelCode"));
			
			if (ValueUtils.hasValue(parameter.getParentModelName())) {
				String parentProperty = parameter.getParentProperty();
				Method m = clazz.getMethod("get" + WordUtils.capitalize(parentProperty), new Class[] {});
				Object parentObject = m.invoke(obj, new Object[] {});
				parentObject = Helper.loadHibernateObjectFromProxy(parentObject);
				
				Class<?> parentClazz = Class.forName(parameter.getParentModelName());
				data.put("parentCode",
						ValueUtils.hasValue((String) parentClazz.getDeclaredField(ApplicationConstants.STR_CODE).get(parentObject))
								? (String) parentClazz.getDeclaredField(ApplicationConstants.STR_CODE).get(parentObject)
								: ApplicationConstants.EMPTY_STRING);
				data.put("parentName",
						ValueUtils.hasValue((String) parentClazz.getDeclaredField(ApplicationConstants.STR_NAME).get(parentObject))
								? (String) parentClazz.getDeclaredField(ApplicationConstants.STR_NAME).get(parentObject)
								: ApplicationConstants.EMPTY_STRING);
				
				data.put("parentLabel", parameter.getParentName());
				data.put("parentModelCode", parameter.getParentModelCode());
				data.put("parentProperty", parameter.getParentProperty());
			}
		}
		
		return data;
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
				checkUniqueRecord(vo, map); 
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);
				vo.setJsonObjectOld(setModelToMap(getModelRecord(map), map, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
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
		vo.setUniqueKey(new StringBuffer((String) map.get("modelCode")).append(ApplicationConstants.DELIMITER_PIPE)
				.append((String) map.get(ApplicationConstants.STR_CODE)).toString());
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("ParameterMaintenanceSC");

		return vo;
	}

	private Object setMapToModel(Map<String, Object> map, ParameterMaintenanceModel parameterModel) throws Exception {
		Class<?> clazz, clazzParent;
		Object myObject, myObjectParent;

		// set model class
		clazz = Class.forName(parameterModel.getModelName());
		myObject = clazz.newInstance();

		if (ValueUtils.hasValue(map.get(ApplicationConstants.STR_CODE))) {
			clazz.getDeclaredField(ApplicationConstants.STR_CODE).set(myObject, map.get(ApplicationConstants.STR_CODE));
		}
		if (ValueUtils.hasValue(map.get(ApplicationConstants.STR_NAME))) {
			clazz.getDeclaredField(ApplicationConstants.STR_NAME).set(myObject, map.get(ApplicationConstants.STR_NAME));
		}
		if (ValueUtils.hasValue(map.get("dscp"))) {
			clazz.getDeclaredField("dscp").set(myObject, map.get("dscp"));
		}

		// cek jika ada parent property atau tidak
		if (ValueUtils.hasValue(parameterModel.getParentModelName())) {
			clazzParent = Class.forName(parameterModel.getParentModelName());
			myObjectParent = clazzParent.newInstance();
			clazzParent.getDeclaredField(ApplicationConstants.STR_CODE).set(myObjectParent, map.get("parentCode"));

			// set parent model to model
			clazz.getDeclaredField(parameterModel.getParentProperty()).set(myObject, myObjectParent);
		}

		return myObject;
	}

	private void checkUniqueRecord(PendingTaskVO vo, Map<String, Object> map) throws Exception {
		ParameterMaintenanceModel parameterModel = parameterMaintenanceRepo.findOne((String) map.get("modelCode"));
		Class<?> clazz = Class.forName(parameterModel.getModelName());
		Object modelRecord = parameterMaintenanceRepo.findAnyOne(clazz,
				(String) map.get(ApplicationConstants.STR_CODE));

		if (modelRecord != null && ApplicationConstants.NO
				.equals(ValueUtils.getValue((String) clazz.getDeclaredField("deleteFlag").get(modelRecord)))) {
			throw new BusinessException("GPT-0100004");
		}
	}

	private Object getModelRecord(Map<String, Object> map) throws Exception {
		ParameterMaintenanceModel parameterModel = parameterMaintenanceRepo.findOne((String) map.get("modelCode"));
		Class<?> clazz = Class.forName(parameterModel.getModelName());

		return parameterMaintenanceRepo.findAnyOne(clazz, (String) map.get(ApplicationConstants.STR_CODE));
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		Class<?> clazz;
		Object modelDB, modelDBExisting;

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			ParameterMaintenanceModel parameterModel = parameterMaintenanceRepo.findOne((String) map.get("modelCode"));
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				clazz = Class.forName(parameterModel.getModelName());
				
				// set model class
				modelDB = clazz.newInstance();

				modelDB = setMapToModel(map, parameterModel);
				modelDBExisting = (Object) parameterMaintenanceRepo.findAnyOne(modelDB.getClass(),
						(String) clazz.getDeclaredField(ApplicationConstants.STR_CODE).get(modelDB));

				// cek jika record replacement
				if (modelDBExisting != null && ApplicationConstants.YES.equals(
						ValueUtils.getValue((String) clazz.getDeclaredField("deleteFlag").get(modelDBExisting)))) {
					// set value yg bisa di ganti
					clazz.getDeclaredField(ApplicationConstants.STR_CODE).set(modelDBExisting,
							(String) clazz.getDeclaredField(ApplicationConstants.STR_CODE).get(modelDB));
					clazz.getDeclaredField(ApplicationConstants.STR_NAME).set(modelDBExisting,
							(String) clazz.getDeclaredField(ApplicationConstants.STR_NAME).get(modelDB));
					clazz.getDeclaredField("dscp").set(modelDBExisting,
							(String) clazz.getDeclaredField("dscp").get(modelDB));

					// set default value
					clazz.getDeclaredField("idx").set(modelDBExisting, ApplicationConstants.IDX);
					clazz.getDeclaredField("deleteFlag").set(modelDBExisting, ApplicationConstants.NO);
					clazz.getDeclaredField("inactiveFlag").set(modelDBExisting, ApplicationConstants.NO);
					clazz.getDeclaredField("systemFlag").set(modelDBExisting, ApplicationConstants.NO);
					clazz.getDeclaredField("createdDate").set(modelDBExisting, DateUtils.getCurrentTimestamp());
					clazz.getDeclaredField("createdBy").set(modelDBExisting, vo.getCreatedBy());

					// set reset value
					clazz.getDeclaredField("updatedDate").set(modelDBExisting, null);
					clazz.getDeclaredField("updatedBy").set(modelDBExisting, null);

					// cek jika ada parent property atau tidak
					if (ValueUtils.hasValue(parameterModel.getParentModelName())) {
						clazz.getDeclaredField(parameterModel.getParentProperty()).set(modelDBExisting,
								(Object) clazz.getDeclaredField(parameterModel.getParentProperty()).get(modelDB));
					}

					parameterMaintenanceRepo.updateAnyOne(modelDBExisting);
				} else {
					// set default value
					clazz.getDeclaredField("idx").set(modelDB, ApplicationConstants.IDX);
					clazz.getDeclaredField("deleteFlag").set(modelDB, ApplicationConstants.NO);
					clazz.getDeclaredField("inactiveFlag").set(modelDB, ApplicationConstants.NO);
					clazz.getDeclaredField("systemFlag").set(modelDB, ApplicationConstants.NO);
					clazz.getDeclaredField("createdDate").set(modelDB, DateUtils.getCurrentTimestamp());
					clazz.getDeclaredField("createdBy").set(modelDB, vo.getCreatedBy());

					parameterMaintenanceRepo.saveAnyOne(modelDB);
				}

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				clazz = Class.forName(parameterModel.getModelName());
				modelDB = clazz.newInstance();
				modelDBExisting = clazz.newInstance();

				modelDB = setMapToModel(map, parameterModel);
				
				modelDBExisting = (Object) parameterMaintenanceRepo.findAnyOne(modelDB.getClass(),
						(String) clazz.getDeclaredField(ApplicationConstants.STR_CODE).get(modelDB));

				if (modelDBExisting == null) {
					throw new BusinessException("GPT-0100001");
				}

				// set value yg boleh di edit
				clazz.getDeclaredField(ApplicationConstants.STR_NAME).set(modelDBExisting,
						(String) clazz.getDeclaredField(ApplicationConstants.STR_NAME).get(modelDB));
				clazz.getDeclaredField("dscp").set(modelDBExisting,
						(String) clazz.getDeclaredField("dscp").get(modelDB));
				clazz.getDeclaredField("updatedDate").set(modelDBExisting, DateUtils.getCurrentTimestamp());
				clazz.getDeclaredField("updatedBy").set(modelDBExisting, vo.getCreatedBy());

				// cek jika ada parent property atau tidak
				if (ValueUtils.hasValue(parameterModel.getParentModelName())) {
					clazz.getDeclaredField(parameterModel.getParentProperty()).set(modelDBExisting,
							(Object) clazz.getDeclaredField(parameterModel.getParentProperty()).get(modelDB));
				}

				parameterMaintenanceRepo.updateAnyOne(modelDBExisting);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				clazz = Class.forName(parameterModel.getModelName());
				modelDB = clazz.newInstance();
				modelDBExisting = clazz.newInstance();

				modelDB = setMapToModel(map, parameterModel);
				modelDBExisting = (Object) parameterMaintenanceRepo.findAnyOne(modelDB.getClass(),
						(String) clazz.getDeclaredField(ApplicationConstants.STR_CODE).get(modelDB));

				clazz.getDeclaredField("deleteFlag").set(modelDBExisting, ApplicationConstants.YES);
				clazz.getDeclaredField("updatedDate").set(modelDBExisting, DateUtils.getCurrentTimestamp());
				clazz.getDeclaredField("updatedBy").set(modelDBExisting, vo.getCreatedBy());

				parameterMaintenanceRepo.updateAnyOne(modelDBExisting);
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

		return null;
	}
	
	@Override
	public Object getParameterMaintenanceByModelAndName(String modelCode, String name) throws ClassNotFoundException {
		return parameterMaintenanceRepo.getParameterMaintenanceByModelAndName(modelCode, name);
	}
}
