package com.gpt.product.gpcash.limitpackage.services;

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
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.maintenance.parametermt.model.CurrencyModel;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.repository.SysParamRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageDetailModel;
import com.gpt.product.gpcash.limitpackage.model.LimitPackageModel;
import com.gpt.product.gpcash.limitpackage.repository.LimitPackageRepository;
import com.gpt.product.gpcash.limitpackage.spi.LimitPackageListener;
import com.gpt.product.gpcash.service.model.ServiceModel;
import com.gpt.product.gpcash.servicecurrencymatrix.model.ServiceCurrencyMatrixModel;
import com.gpt.product.gpcash.servicecurrencymatrix.repository.ServiceCurrencyMatrixRepository;
import com.gpt.product.gpcash.servicepackage.model.ServicePackageModel;
import com.gpt.product.gpcash.servicepackage.repository.ServicePackageRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class LimitPackageServiceImpl implements LimitPackageService {
	private List<LimitPackageListener> limitPackageListener;

	@Autowired
	private LimitPackageRepository limitPackageRepo;

	@Autowired
	private ServiceCurrencyMatrixRepository serviceCurrencyMatrixRepo;

	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private ServicePackageRepository servicePackageRepo;
	
	@Autowired
	private SysParamRepository sysParamRepo;
	
	@Autowired(required=false)
	public void setlimitPackageListener(List<LimitPackageListener> limitPackageListener) {
		this.limitPackageListener = limitPackageListener;
	}

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<LimitPackageModel> result = limitPackageRepo.search(map, PagingUtils.createPageRequest(map));

			if (ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))) {
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

	private List<Map<String, Object>> setModelToMap(List<LimitPackageModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (LimitPackageModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(LimitPackageModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
		map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
		map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
		map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));

		if (isGetDetail) {
			if (model.getLimitPackageDetail() != null) {
				List<Map<String, String>> listDetail = new ArrayList<>();
				for (LimitPackageDetailModel detailModel : model.getLimitPackageDetail()) {
					Map<String, String> detail = new HashMap<>();

					ServiceCurrencyMatrixModel serviceCurrencyMatrix = detailModel.getServiceCurrencyMatrix();
					ServiceModel service = serviceCurrencyMatrix.getService();
					CurrencyModel currency = detailModel.getCurrency();

					detail.put("serviceCode", service.getCode());
					detail.put("serviceName", service.getName());
					detail.put("serviceCurrencyMatrixId", serviceCurrencyMatrix.getId());
					detail.put("currencyMatrixName", serviceCurrencyMatrix.getCurrencyMatrix().getName());
					detail.put("maxTrxPerDay", String.valueOf(detailModel.getMaxOccurrenceLimit()));
					detail.put("currencyCode", currency.getCode());
					detail.put("maxTrxAmountPerDay", detailModel.getMaxAmountLimit().toPlainString());

					listDetail.add(detail);
				}
				map.put("transactionLimitList", listDetail);
			}
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

			String code = (String)map.get(ApplicationConstants.STR_CODE);
			
			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord(code); 
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				LimitPackageModel limitPackageOld = getExistingRecord(code,
						true);

				vo.setJsonObjectOld(setModelToMap(limitPackageOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				getExistingRecord(code, true);
				
				checkIsLimitPackageStillInUsed(code);
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
	
	private void checkIsLimitPackageStillInUsed(String limitPackageCode) throws Exception{
		try {
			List<ServicePackageModel> servicePackageList = servicePackageRepo.findByLimitPackageCode(limitPackageCode);
			if(servicePackageList.size() > 0) {
				throw new BusinessException("GPT-0100132");
			}
		} catch (BusinessException e) {
			throw e;
		}
	}

	private LimitPackageModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		LimitPackageModel model = limitPackageRepo.findOne(code);

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
		vo.setService("LimitPackageSC");

		return vo;
	}

	@SuppressWarnings("unchecked")
	private LimitPackageModel setMapToModel(Map<String, Object> map) {
		LimitPackageModel limitPackage = new LimitPackageModel();
		limitPackage.setCode((String) map.get(ApplicationConstants.STR_CODE));
		limitPackage.setName((String) map.get(ApplicationConstants.STR_NAME));

		IDMApplicationModel application = new IDMApplicationModel();
		application.setCode(Helper.getActiveApplicationCode());
		limitPackage.setApplication(application);

		List<Map<String, Object>> transactionLimitList = (ArrayList<Map<String, Object>>) map
				.get("transactionLimitList");

		List<LimitPackageDetailModel> limitPackageDetailList = new ArrayList<>();
		for (Map<String, Object> transactionLimitMap : transactionLimitList) {
			String serviceCode = (String) transactionLimitMap.get("serviceCode");
			String serviceCurrencyMatrixId = (String) transactionLimitMap.get("serviceCurrencyMatrixId");
			int maxTrxPerDay = Integer.parseInt((String) transactionLimitMap.get("maxTrxPerDay"));
			String currencyCode = (String) transactionLimitMap.get("currencyCode");
			BigDecimal maxTrxAmountPerDay = new BigDecimal((String) transactionLimitMap.get("maxTrxAmountPerDay"));

			LimitPackageDetailModel limitPackageDetail = new LimitPackageDetailModel();

			CurrencyModel currency = new CurrencyModel();
			currency.setCode(currencyCode);
			limitPackageDetail.setCurrency(currency);

			ServiceModel service = new ServiceModel();
			service.setCode(serviceCode);
			limitPackageDetail.setService(service);

			ServiceCurrencyMatrixModel serviceCurrencyMatrix = new ServiceCurrencyMatrixModel();
			serviceCurrencyMatrix.setId(serviceCurrencyMatrixId);
			limitPackageDetail.setServiceCurrencyMatrix(serviceCurrencyMatrix);

			limitPackageDetail.setMaxOccurrenceLimit(maxTrxPerDay);
			limitPackageDetail.setMaxAmountLimit(maxTrxAmountPerDay);

			LimitPackageModel limitPackageParent = new LimitPackageModel();
			limitPackageParent.setCode((String) map.get(ApplicationConstants.STR_CODE));
			limitPackageDetail.setLimitPackage(limitPackageParent);
			limitPackageDetailList.add(limitPackageDetail);
		}
		limitPackage.setLimitPackageDetail(limitPackageDetailList);

		return limitPackage;
	}

	private void checkUniqueRecord(String code) throws Exception {
		LimitPackageModel limitPackage = limitPackageRepo.findOne(code);

		if (limitPackage != null && ApplicationConstants.NO.equals(limitPackage.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				LimitPackageModel limitPackage = setMapToModel(map);
				LimitPackageModel limitPackageExisting = limitPackageRepo.findOne(limitPackage.getCode());

				// cek jika record replacement
				if (limitPackageExisting != null
						&& ApplicationConstants.YES.equals(limitPackageExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					limitPackageExisting.setName(limitPackage.getName());

					// set default value
					limitPackageExisting.setIdx(ApplicationConstants.IDX);
					limitPackageExisting.setDeleteFlag(ApplicationConstants.NO);
					limitPackageExisting.setInactiveFlag(ApplicationConstants.NO);
					limitPackageExisting.setSystemFlag(ApplicationConstants.NO);
					limitPackageExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					limitPackageExisting.setCreatedBy(vo.getCreatedBy());

					// set reset value
					limitPackageExisting.setUpdatedDate(null);
					limitPackageExisting.setUpdatedBy(null);

					// delete existing, replace with new
					limitPackageRepo.deleteLimitPackageDetail(limitPackageExisting.getLimitPackageDetail());

					// set new detail
					List<LimitPackageDetailModel> childList = (ArrayList<LimitPackageDetailModel>) limitPackage
							.getLimitPackageDetail();
					for (LimitPackageDetailModel limitPackageDetail : childList) {
						// set again because using @jsonignore to avoid jackson
						// rekurisif
						LimitPackageModel limitPackageParent = new LimitPackageModel();
						limitPackageParent.setCode(limitPackage.getCode());
						limitPackageDetail.setLimitPackage(limitPackageParent);

						limitPackageDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
						limitPackageDetail.setCreatedBy(vo.getCreatedBy());
					}
					limitPackageExisting.setLimitPackageDetail(childList);

					limitPackageRepo.save(limitPackageExisting);
				} else {
					// set default value
					limitPackage.setIdx(ApplicationConstants.IDX);
					limitPackage.setDeleteFlag(ApplicationConstants.NO);
					limitPackage.setInactiveFlag(ApplicationConstants.NO);
					limitPackage.setSystemFlag(ApplicationConstants.NO);
					limitPackage.setCreatedDate(DateUtils.getCurrentTimestamp());
					limitPackage.setCreatedBy(vo.getCreatedBy());

					// set new detail
					List<LimitPackageDetailModel> childList = (ArrayList<LimitPackageDetailModel>) limitPackage
							.getLimitPackageDetail();
					for (LimitPackageDetailModel limitPackageDetail : childList) {
						LimitPackageModel limitPackageParent = new LimitPackageModel();
						limitPackageParent.setCode(limitPackage.getCode());
						limitPackageDetail.setLimitPackage(limitPackageParent);
						
						limitPackageDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
						limitPackageDetail.setCreatedBy(vo.getCreatedBy());
					}
					limitPackage.setLimitPackageDetail(childList);

					limitPackageRepo.save(limitPackage);
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				LimitPackageModel limitPackageNew = setMapToModel(map);

				LimitPackageModel limitPackageExisting = getExistingRecord(limitPackageNew.getCode(), true);

				// set value yg boleh di edit
				limitPackageExisting.setName(limitPackageNew.getName());
				limitPackageExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				limitPackageExisting.setUpdatedBy(vo.getCreatedBy());

				List<LimitPackageDetailModel> existingDetailList = limitPackageExisting.getLimitPackageDetail();
				Map<String, LimitPackageDetailModel> existingDetailMap = new HashMap<>();
				for(LimitPackageDetailModel existingDetail : existingDetailList) {
					existingDetailMap.put(existingDetail.getService().getCode(), existingDetail);
				}

				// set new detail
				List<LimitPackageDetailModel> childList = (ArrayList<LimitPackageDetailModel>) limitPackageNew
						.getLimitPackageDetail();
				for (LimitPackageDetailModel limitPackageDetail : childList) {
					LimitPackageModel limitPackageParent = new LimitPackageModel();
					limitPackageParent.setCode(limitPackageNew.getCode());
					limitPackageDetail.setLimitPackage(limitPackageParent);
					
					ServiceModel serviceNew = limitPackageDetail.getService();
					BigDecimal maxAmountLimit = limitPackageDetail.getMaxAmountLimit();
					int maxOccurenceLimit = limitPackageDetail.getMaxOccurrenceLimit();
					CurrencyModel currency = limitPackageDetail.getCurrency();
					
					//update to corporate limit
					if(limitPackageListener != null) {
						for(LimitPackageListener listener : limitPackageListener) {
							listener.updateALLLimitByLimitPackageCode(maxAmountLimit,
									maxOccurenceLimit, 
									limitPackageNew.getCode(), limitPackageDetail.getCurrency().getCode(), 
									serviceNew.getCode(), 
									limitPackageDetail.getServiceCurrencyMatrix().getId(), 
									(String) map.get("isUpdateCorporateWithSpecialLimit"));
						}
					}
					
					//update limit package detail value
					if(existingDetailMap.get(serviceNew.getCode()) != null) {
						LimitPackageDetailModel existingDetail = existingDetailMap.get(serviceNew.getCode());
						existingDetail.setMaxAmountLimit(maxAmountLimit);
						existingDetail.setMaxOccurrenceLimit(maxOccurenceLimit);
						existingDetail.setCurrency(currency);
					}

					limitPackageDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
					limitPackageDetail.setCreatedBy(vo.getCreatedBy());
				}
				limitPackageExisting.setLimitPackageDetail(existingDetailList);
				
				limitPackageRepo.save(limitPackageExisting);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				String code = (String)map.get(ApplicationConstants.STR_CODE);
				
				LimitPackageModel limitPackage = getExistingRecord(code, true);
				limitPackage.setDeleteFlag(ApplicationConstants.YES);
				limitPackage.setLimitPackageDetail(null);
				limitPackage.setUpdatedDate(DateUtils.getCurrentTimestamp());
				limitPackage.setUpdatedBy(vo.getCreatedBy());

				checkIsLimitPackageStillInUsed(code);
				
				limitPackageRepo.save(limitPackage);
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
	public Map<String, Object> searchServiceCurrencyMatrixTRX(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = serviceCurrencyMatrixRepo.findDetailByServiceTypeCode(
					(String) map.get(ApplicationConstants.APP_CODE), ApplicationConstants.SERVICE_TYPE_TRX, null);

			List<Map<String, Object>> resultList = new ArrayList<>();
			for (Object[] model : result.getContent()) {
				Map<String, Object> serviceCurrencyMatrixMap = new HashMap<>();
				serviceCurrencyMatrixMap.put("serviceCode", model[0]);
				serviceCurrencyMatrixMap.put("serviceName", model[1]);
				serviceCurrencyMatrixMap.put("serviceCurrencyMatrixId", model[2]);
				serviceCurrencyMatrixMap.put("currencyMatrixName", model[3]);
				resultList.add(serviceCurrencyMatrixMap);
			}

			resultMap.put("result", resultList);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> searchLimitPackageDetailByCode(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = limitPackageRepo.findDetailByServiceTypeCodeAndLimitPackageCode(
					(String) map.get(ApplicationConstants.APP_CODE), ApplicationConstants.SERVICE_TYPE_TRX,
					(String) map.get(ApplicationConstants.STR_CODE), null);

			List<Map<String, Object>> resultList = new ArrayList<>();
			for (Object[] model : result.getContent()) {
				Map<String, Object> serviceCurrencyMatrixMap = new HashMap<>();
				serviceCurrencyMatrixMap.put("serviceCode", model[0]);
				serviceCurrencyMatrixMap.put("serviceName", model[1]);
				serviceCurrencyMatrixMap.put("serviceCurrencyMatrixId", model[2]);
				serviceCurrencyMatrixMap.put("currencyMatrixName", model[3]);
				serviceCurrencyMatrixMap.put("currencyCode", model[4]);
				serviceCurrencyMatrixMap.put("maxTrxPerDay", model[5]);
				serviceCurrencyMatrixMap.put("maxTrxAmountPerDay", model[6]);
				resultList.add(serviceCurrencyMatrixMap);
			}

			resultMap.put("result", resultList);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchSpesific(String code, String applicationCode) throws ApplicationException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = serviceCurrencyMatrixRepo.findDetailByServiceTypeCode(applicationCode, ApplicationConstants.SERVICE_TYPE_TRX, null);

			List<Map<String, Object>> resultList = new ArrayList<>();
			for (Object[] model : result.getContent()) {
				Map<String, Object> serviceCurrencyMatrixMap = new HashMap<>();
				String serviceCode = (String) model[0];
				String serviceCurrencyMatrixId = (String) model[2];
				
				serviceCurrencyMatrixMap.put("serviceCode", serviceCode);
				serviceCurrencyMatrixMap.put("serviceName", model[1]);
				serviceCurrencyMatrixMap.put("serviceCurrencyMatrixId", serviceCurrencyMatrixId);
				serviceCurrencyMatrixMap.put("currencyMatrixName", model[3]);
				
				LimitPackageDetailModel limitDetail = limitPackageRepo.findDetailByLimitPackageCode(serviceCode, serviceCurrencyMatrixId, code);
				if(limitDetail != null) {
					serviceCurrencyMatrixMap.put("currencyCode", limitDetail.getCurrency().getCode());
					serviceCurrencyMatrixMap.put("maxTrxPerDay", limitDetail.getMaxOccurrenceLimit());
					serviceCurrencyMatrixMap.put("maxTrxAmountPerDay", limitDetail.getMaxAmountLimit());
				} else {
					serviceCurrencyMatrixMap.put("currencyCode", sysParamRepo.findOne(SysParamConstants.LOCAL_CURRENCY_CODE).getValue());
					serviceCurrencyMatrixMap.put("maxTrxPerDay", "0");
					serviceCurrencyMatrixMap.put("maxTrxAmountPerDay", BigDecimal.ZERO);
				}
				
				resultList.add(serviceCurrencyMatrixMap);
			}

			resultMap.put("result", resultList);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}

	@Override
	public Map<String, Object> searchForDroplist(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<LimitPackageModel> result = limitPackageRepo.search(map, PagingUtils.createPageRequest(map));
			resultMap.put("result", setModelToMap(result.getContent(), false));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
}
