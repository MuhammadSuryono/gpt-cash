package com.gpt.component.maintenance.promo.services;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.promo.model.PromoModel;
import com.gpt.component.maintenance.promo.repository.PromoRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;

@Service
@Transactional(rollbackFor = Exception.class)
public class PromoServiceImpl implements PromoService {

	@Autowired
	private PromoRepository promoRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Value("${gpcash.avatar.upload.path}")
	private String pathUpload;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		try {
			resultMap.put("result", setModelToMap(promoRepo.search(map, null).getContent()));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<PromoModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PromoModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.STR_CODE, model.getCode());
			map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
			map.put("imgUrl", ValueUtils.getValue(model.getPromoImgUrl()));
			map.put("imgFileName", ValueUtils.getValue(model.getPromoImgFileName()));
			
			try {
				
				String imageUrl = pathUpload + File.separator + model.getPromoImgFileName();
				File image = new File(imageUrl);
				byte[] imageContent = Files.readAllBytes(image.toPath());
				
				map.put("imageBytes", imageContent);
			} catch (Exception e) {
//				logger.error("No Image Uploaded : " + e.getMessage(), e);
			}
			
			resultList.add(map);

		}

		return resultList;
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
//				checkUniqueRecord(vo, map); 
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);
//				vo.setJsonObjectOld(setModelToMap(getModelRecord(map), map, true));
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

	/*private Object setMapToModel(Map<String, Object> map, PromoModel parameterModel) throws Exception {
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
		PromoModel parameterModel = promoRepo.findOne((String) map.get("modelCode"));
		Class<?> clazz = Class.forName(parameterModel.getModelName());
		Object modelRecord = promoRepo.findAnyOne(clazz,
				(String) map.get(ApplicationConstants.STR_CODE));

		if (modelRecord != null && ApplicationConstants.NO
				.equals(ValueUtils.getValue((String) clazz.getDeclaredField("deleteFlag").get(modelRecord)))) {
			throw new BusinessException("GPT-0100004");
		}
	}

	private Object getModelRecord(Map<String, Object> map) throws Exception {
		PromoModel parameterModel = promoRepo.findOne((String) map.get("modelCode"));
		Class<?> clazz = Class.forName(parameterModel.getModelName());

		return promoRepo.findAnyOne(clazz, (String) map.get(ApplicationConstants.STR_CODE));
	}*/

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		Class<?> clazz;
		Object modelDB, modelDBExisting;

		try {
			/*Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			PromoModel parameterModel = promoRepo.findOne((String) map.get("modelCode"));
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				clazz = Class.forName(parameterModel.getModelName());
				
				// set model class
				modelDB = clazz.newInstance();

				modelDB = setMapToModel(map, parameterModel);
				modelDBExisting = (Object) promoRepo.findAnyOne(modelDB.getClass(),
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

					promoRepo.updateAnyOne(modelDBExisting);
				} else {
					// set default value
					clazz.getDeclaredField("idx").set(modelDB, ApplicationConstants.IDX);
					clazz.getDeclaredField("deleteFlag").set(modelDB, ApplicationConstants.NO);
					clazz.getDeclaredField("inactiveFlag").set(modelDB, ApplicationConstants.NO);
					clazz.getDeclaredField("systemFlag").set(modelDB, ApplicationConstants.NO);
					clazz.getDeclaredField("createdDate").set(modelDB, DateUtils.getCurrentTimestamp());
					clazz.getDeclaredField("createdBy").set(modelDB, vo.getCreatedBy());

					promoRepo.saveAnyOne(modelDB);
				}

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				clazz = Class.forName(parameterModel.getModelName());
				modelDB = clazz.newInstance();
				modelDBExisting = clazz.newInstance();

				modelDB = setMapToModel(map, parameterModel);
				
				modelDBExisting = (Object) promoRepo.findAnyOne(modelDB.getClass(),
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

				promoRepo.updateAnyOne(modelDBExisting);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				clazz = Class.forName(parameterModel.getModelName());
				modelDB = clazz.newInstance();
				modelDBExisting = clazz.newInstance();

				modelDB = setMapToModel(map, parameterModel);
				modelDBExisting = (Object) promoRepo.findAnyOne(modelDB.getClass(),
						(String) clazz.getDeclaredField(ApplicationConstants.STR_CODE).get(modelDB));

				clazz.getDeclaredField("deleteFlag").set(modelDBExisting, ApplicationConstants.YES);
				clazz.getDeclaredField("updatedDate").set(modelDBExisting, DateUtils.getCurrentTimestamp());
				clazz.getDeclaredField("updatedBy").set(modelDBExisting, vo.getCreatedBy());

				promoRepo.updateAnyOne(modelDBExisting);
			}
		} catch (BusinessException e) {
			throw e;
		*/} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return vo;
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {

		return null;
	}
}
