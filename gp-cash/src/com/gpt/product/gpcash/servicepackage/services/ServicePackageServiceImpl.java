package com.gpt.product.gpcash.servicepackage.services;

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
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;
import com.gpt.product.gpcash.menupackage.model.MenuPackageModel;
import com.gpt.product.gpcash.servicepackage.model.ServicePackageModel;
import com.gpt.product.gpcash.servicepackage.repository.ServicePackageRepository;
import com.gpt.product.gpcash.utils.ProductRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class ServicePackageServiceImpl implements ServicePackageService {

	@Autowired
	private ServicePackageRepository servicePackageRepo;
	
	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<ServicePackageModel> result = servicePackageRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<ServicePackageModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (ServicePackageModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(ServicePackageModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		
		if(isGetDetail){
			ChargePackageModel chargePackage = model.getChargePackage();
			map.put("chargePackageCode", ValueUtils.getValue(chargePackage.getCode()));
			map.put("chargePackageName", ValueUtils.getValue(chargePackage.getName()));
			
			LimitPackageModel limitPackage = model.getLimitPackage();
			map.put("limitPackageCode", ValueUtils.getValue(limitPackage.getCode()));
			map.put("limitPackageName", ValueUtils.getValue(limitPackage.getName()));
			
			MenuPackageModel menuPackage = model.getMenuPackage();
			map.put("menuPackageCode", ValueUtils.getValue(menuPackage.getCode()));
			map.put("menuPackageName", ValueUtils.getValue(menuPackage.getName()));
			
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
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String) map.get(ApplicationConstants.STR_CODE)); // cek ke table jika record already exist
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				ServicePackageModel servicePackageOld = getExistingRecord(
						(String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(servicePackageOld, true));
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
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try{
			productRepo.isLimitPackageValid((String) map.get("limitPackageCode"));
			productRepo.isChargePackageValid((String) map.get("chargePackageCode"));
			productRepo.isMenuPackageValid((String) map.get("menuPackageCode"));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private ServicePackageModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		ServicePackageModel model = servicePackageRepo.findOne(code);

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
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.STR_CODE)).concat(ApplicationConstants.DELIMITER_DASH)
				.concat((String) map.get(ApplicationConstants.STR_NAME)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("ServicePackageSC");

		return vo;
	}

	private ServicePackageModel setMapToModel(Map<String, Object> map) {
		ServicePackageModel servicePackage = new ServicePackageModel();
		servicePackage.setCode((String) map.get(ApplicationConstants.STR_CODE));
		servicePackage.setName((String) map.get(ApplicationConstants.STR_NAME));

		if (ValueUtils.hasValue(map.get("chargePackageCode"))) {
			ChargePackageModel chargePackage = new ChargePackageModel();
			chargePackage.setCode((String) map.get("chargePackageCode"));
			servicePackage.setChargePackage(chargePackage);
		}

		if (ValueUtils.hasValue(map.get("limitPackageCode"))) {
			LimitPackageModel limitPackage = new LimitPackageModel();
			limitPackage.setCode((String) map.get("limitPackageCode"));
			servicePackage.setLimitPackage(limitPackage);
		}

		if (ValueUtils.hasValue(map.get("menuPackageCode"))) {
			MenuPackageModel menuPackage = new MenuPackageModel();
			menuPackage.setCode((String) map.get("menuPackageCode"));
			servicePackage.setMenuPackage(menuPackage);
		}

		if (ValueUtils.hasValue(map.get(ApplicationConstants.APP_CODE))) {
			IDMApplicationModel application = new IDMApplicationModel();
			application.setCode((String) map.get(ApplicationConstants.APP_CODE));
			servicePackage.setApplication(application);
		}

		return servicePackage;
	}

	private void checkUniqueRecord(String code) throws Exception {
		ServicePackageModel servicePackage = servicePackageRepo.findOne(code);

		if (servicePackage != null && ApplicationConstants.NO.equals(servicePackage.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (HashMap<String, Object>) vo.getJsonObject();
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				ServicePackageModel servicePackage = setMapToModel(map);
				ServicePackageModel servicePackageExisting = servicePackageRepo.findOne(servicePackage.getCode());

				// cek jika record replacement
				if (servicePackageExisting != null
						&& ApplicationConstants.YES.equals(servicePackageExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					servicePackageExisting.setName(servicePackage.getName());
					servicePackageExisting.setChargePackage(servicePackage.getChargePackage());
					servicePackageExisting.setLimitPackage(servicePackage.getLimitPackage());
					servicePackageExisting.setMenuPackage(servicePackage.getMenuPackage());

					// set default value
					servicePackageExisting.setIdx(ApplicationConstants.IDX);
					servicePackageExisting.setDeleteFlag(ApplicationConstants.NO);
					servicePackageExisting.setInactiveFlag(ApplicationConstants.NO);
					servicePackageExisting.setSystemFlag(ApplicationConstants.NO);
					servicePackageExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					servicePackageExisting.setCreatedBy(vo.getCreatedBy());

					// set reset value
					servicePackageExisting.setUpdatedDate(null);
					servicePackageExisting.setUpdatedBy(null);

					servicePackageRepo.save(servicePackageExisting);
				} else {
					// set default value
					servicePackage.setIdx(ApplicationConstants.IDX);
					servicePackage.setDeleteFlag(ApplicationConstants.NO);
					servicePackage.setInactiveFlag(ApplicationConstants.NO);
					servicePackage.setSystemFlag(ApplicationConstants.NO);
					servicePackage.setCreatedDate(DateUtils.getCurrentTimestamp());
					servicePackage.setCreatedBy(vo.getCreatedBy());

					servicePackageRepo.persist(servicePackage);
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				ServicePackageModel servicePackageNew = setMapToModel(map);

				ServicePackageModel servicePackageExisting = getExistingRecord(servicePackageNew.getCode(), true);

				// set value yg boleh di edit
				servicePackageExisting.setName(servicePackageNew.getName());
				servicePackageExisting.setChargePackage(servicePackageNew.getChargePackage());
				servicePackageExisting.setLimitPackage(servicePackageNew.getLimitPackage());
				servicePackageExisting.setMenuPackage(servicePackageNew.getMenuPackage());
				servicePackageExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				servicePackageExisting.setUpdatedBy(vo.getCreatedBy());

				servicePackageRepo.save(servicePackageExisting);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				ServicePackageModel servicePackage = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
				servicePackage.setDeleteFlag(ApplicationConstants.YES);
				servicePackage.setUpdatedDate(DateUtils.getCurrentTimestamp());
				servicePackage.setUpdatedBy(vo.getCreatedBy());

				servicePackageRepo.save(servicePackage);
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
