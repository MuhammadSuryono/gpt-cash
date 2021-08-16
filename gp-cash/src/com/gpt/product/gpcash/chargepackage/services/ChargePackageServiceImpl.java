package com.gpt.product.gpcash.chargepackage.services;

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
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.repository.SysParamRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageDetailModel;
import com.gpt.product.gpcash.chargepackage.model.ChargePackageModel;
import com.gpt.product.gpcash.chargepackage.repository.ChargePackageRepository;
import com.gpt.product.gpcash.chargepackage.spi.ChargePackageListener;
import com.gpt.product.gpcash.service.model.ServiceModel;
import com.gpt.product.gpcash.servicecharge.model.ServiceChargeModel;
import com.gpt.product.gpcash.servicecharge.repository.ServiceChargeRepository;
import com.gpt.product.gpcash.servicepackage.model.ServicePackageModel;
import com.gpt.product.gpcash.servicepackage.repository.ServicePackageRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class ChargePackageServiceImpl implements ChargePackageService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ChargePackageRepository chargePackageRepo;

	@Autowired
	private ServiceChargeRepository serviceCharge;

	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Autowired
	private ServicePackageRepository servicePackageRepo;
	
	@Autowired
	private SysParamRepository sysParamRepo;
	
	private List<ChargePackageListener> chargePackageListener;
	
	@Autowired(required=false)
	public void setUpdateListeners(List<ChargePackageListener> chargePackageListener) {
		this.chargePackageListener = chargePackageListener;
	}

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<ChargePackageModel> result = chargePackageRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<ChargePackageModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (ChargePackageModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(ChargePackageModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
		map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
		map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
		map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));

		if(isGetDetail){
			if (model.getChargePackageDetail() != null) {
				List<Map<String, String>> listDetail = new ArrayList<>();
				for (ChargePackageDetailModel detailModel : model.getChargePackageDetail()) {
					Map<String, String> detail = new HashMap<>();

					ServiceChargeModel serviceCharge = detailModel.getServiceCharge();
					ServiceModel service = serviceCharge.getService();
					CurrencyModel currency = detailModel.getCurrency();

					detail.put("serviceCode", service.getCode());
					detail.put("serviceName", service.getName());
					detail.put("serviceChargeId", serviceCharge.getId());
					detail.put("serviceChargeName", serviceCharge.getName());
					detail.put("currencyCode", currency.getCode());
					detail.put("value", detailModel.getValue().toPlainString());

					listDetail.add(detail);
				}
				map.put("transactionChargeList", listDetail);
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
				checkUniqueRecord((String) map.get(ApplicationConstants.STR_CODE)); // cek ke table jika record already exist
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				ChargePackageModel chargePackageOld = getExistingRecord(code,
						true);

				chargePackageOld.getChargePackageDetail();
				vo.setJsonObjectOld(setModelToMap(chargePackageOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				getExistingRecord(code, true);
				
				checkIsChargePackageStillInUsed(code);
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
	
	private void checkIsChargePackageStillInUsed(String chargePackageCode) throws Exception{
		try {
			List<ServicePackageModel> servicePackageList = servicePackageRepo.findByChargePackageCode(chargePackageCode);
			if(servicePackageList.size() > 0) {
				throw new BusinessException("GPT-0100133");
			}
		} catch (BusinessException e) {
			throw e;
		}
	}

	private ChargePackageModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		ChargePackageModel model = chargePackageRepo.findOne(code);

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
		vo.setService("ChargePackageSC");

		return vo;
	}

	@SuppressWarnings("unchecked")
	private ChargePackageModel setMapToModel(Map<String, Object> map, boolean isGetDetail) {
		ChargePackageModel chargePackage = new ChargePackageModel();
		chargePackage.setCode((String) map.get(ApplicationConstants.STR_CODE));
		chargePackage.setName((String) map.get(ApplicationConstants.STR_NAME));

		if(isGetDetail){
			IDMApplicationModel application = new IDMApplicationModel();
			application.setCode(Helper.getActiveApplicationCode());
			chargePackage.setApplication(application);

			List<Map<String, Object>> transactionChargeList = (ArrayList<Map<String, Object>>) map
					.get("transactionChargeList");

			List<ChargePackageDetailModel> chargePackageDetailList = new ArrayList<>();
			for (Map<String, Object> transactionLimitMap : transactionChargeList) {
				String serviceChargeId = (String) transactionLimitMap.get("serviceChargeId");
				String currencyCode = (String) transactionLimitMap.get("currencyCode");
				BigDecimal chargeValue = new BigDecimal((String) transactionLimitMap.get("value"));

				ChargePackageDetailModel chargePackageDetail = new ChargePackageDetailModel();

				CurrencyModel currency = new CurrencyModel();
				currency.setCode(currencyCode);
				chargePackageDetail.setCurrency(currency);

				ServiceChargeModel serviceCharge = new ServiceChargeModel();
				serviceCharge.setId(serviceChargeId);
				chargePackageDetail.setServiceCharge(serviceCharge);

				chargePackageDetail.setValue(chargeValue);
				chargePackageDetail.setValueType(ApplicationConstants.CHARGE_VAL_TYPE_FIX);

				ChargePackageModel chargePackageParent = new ChargePackageModel();
				chargePackageParent.setCode((String) map.get(ApplicationConstants.STR_CODE));
				chargePackageDetail.setChargePackage(chargePackageParent);
				chargePackageDetailList.add(chargePackageDetail);
			}
			chargePackage.setChargePackageDetail(chargePackageDetailList);
		}

		return chargePackage;
	}

	private void checkUniqueRecord(String chargePackageCode) throws Exception {
		ChargePackageModel model = chargePackageRepo.findOne(chargePackageCode);

		if (model != null && ApplicationConstants.NO.equals(model.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				ChargePackageModel chargePackage = setMapToModel(map, true);
				ChargePackageModel chargePackageExisting = chargePackageRepo.findOne(chargePackage.getCode());

				// cek jika record replacement
				if (chargePackageExisting != null
						&& ApplicationConstants.YES.equals(chargePackageExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					chargePackageExisting.setName(chargePackage.getName());

					// set default value
					chargePackageExisting.setIdx(ApplicationConstants.IDX);
					chargePackageExisting.setDeleteFlag(ApplicationConstants.NO);
					chargePackageExisting.setInactiveFlag(ApplicationConstants.NO);
					chargePackageExisting.setSystemFlag(ApplicationConstants.NO);
					chargePackageExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					chargePackageExisting.setCreatedBy(vo.getCreatedBy());

					// set reset value
					chargePackageExisting.setUpdatedDate(null);
					chargePackageExisting.setUpdatedBy(null);

					// delete existing, replace with new
					chargePackageRepo.deleteChargePackageDetail(chargePackageExisting.getChargePackageDetail());

					// set new detail
					List<ChargePackageDetailModel> childList = (ArrayList<ChargePackageDetailModel>) chargePackage
							.getChargePackageDetail();
					for (ChargePackageDetailModel chargePackageDetail : childList) {
						// set again because using @jsonignore to avoid jackson
						// rekurisif
						ChargePackageModel chargePackageParent = new ChargePackageModel();
						chargePackageParent.setCode(chargePackage.getCode());
						chargePackageDetail.setChargePackage(chargePackageParent);

						chargePackageDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
						chargePackageDetail.setCreatedBy(vo.getCreatedBy());
					}
					chargePackageExisting.setChargePackageDetail(childList);

					chargePackageRepo.save(chargePackageExisting);
				} else {
					// set default value
					chargePackage.setIdx(ApplicationConstants.IDX);
					chargePackage.setDeleteFlag(ApplicationConstants.NO);
					chargePackage.setInactiveFlag(ApplicationConstants.NO);
					chargePackage.setSystemFlag(ApplicationConstants.NO);
					chargePackage.setCreatedDate(DateUtils.getCurrentTimestamp());
					chargePackage.setCreatedBy(vo.getCreatedBy());

					// set new detail
					List<ChargePackageDetailModel> childList = (ArrayList<ChargePackageDetailModel>) chargePackage
							.getChargePackageDetail();
					for (ChargePackageDetailModel chargePackageDetail : childList) {
						// set again because using @jsonignore to avoid jackson
						// rekurisif
						ChargePackageModel chargePackageParent = new ChargePackageModel();
						chargePackageParent.setCode(chargePackage.getCode());
						chargePackageDetail.setChargePackage(chargePackageParent);

						chargePackageDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
						chargePackageDetail.setCreatedBy(vo.getCreatedBy());
					}
					chargePackage.setChargePackageDetail(childList);

					chargePackageRepo.save(chargePackage);
				}
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				ChargePackageModel chargePackageNew = setMapToModel(map, true);

				ChargePackageModel chargePackageExisting = getExistingRecord(chargePackageNew.getCode(), true);

				// set value yg boleh di edit
				chargePackageExisting.setName(chargePackageNew.getName());
				chargePackageExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				chargePackageExisting.setUpdatedBy(vo.getCreatedBy());

				List<ChargePackageDetailModel> existingDetailList = chargePackageExisting.getChargePackageDetail();
				Map<String, ChargePackageDetailModel> existingDetailMap = new HashMap<>();
				for(ChargePackageDetailModel existingDetail : existingDetailList) {
					existingDetailMap.put(existingDetail.getServiceCharge().getId(), existingDetail);
				}

				// set new detail
				List<ChargePackageDetailModel> childList = (ArrayList<ChargePackageDetailModel>) chargePackageNew
						.getChargePackageDetail();
				for (ChargePackageDetailModel chargePackageDetail : childList) {
					// set again because using @jsonignore to avoid jackson
					// rekurisif
					ChargePackageModel chargePackageParent = new ChargePackageModel();
					chargePackageParent.setCode(chargePackageNew.getCode());
					chargePackageDetail.setChargePackage(chargePackageParent);
					
					ServiceChargeModel serviceChargeNew = chargePackageDetail.getServiceCharge();
					BigDecimal chargeValue = chargePackageDetail.getValue();
					CurrencyModel currency = chargePackageDetail.getCurrency();
					
					if(chargePackageListener != null) {
						for(ChargePackageListener listener : chargePackageListener) {
							listener.updateALLChargeByChargePackageCode(chargeValue, 
									chargePackageNew.getCode(), 
									chargePackageDetail.getCurrency().getCode(), 
									serviceChargeNew.getId(), 
									(String) map.get("isUpdateCorporateWithSpecialCharge"));
						}
					}
					
					//update charge package detail value
					if(existingDetailMap.get(serviceChargeNew.getId()) != null) {
						ChargePackageDetailModel existingDetail = existingDetailMap.get(serviceChargeNew.getId());
						existingDetail.setValue(chargeValue);
						existingDetail.setCurrency(currency);
					}

					chargePackageDetail.setCreatedDate(DateUtils.getCurrentTimestamp());
					chargePackageDetail.setCreatedBy(vo.getCreatedBy());
				}
				chargePackageExisting.setChargePackageDetail(existingDetailList);

				chargePackageRepo.save(chargePackageExisting);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				String code = (String)map.get(ApplicationConstants.STR_CODE);
				
				ChargePackageModel chargePackage = getExistingRecord(code, true);
				chargePackage.setDeleteFlag(ApplicationConstants.YES);
				chargePackage.setChargePackageDetail(null);
				chargePackage.setUpdatedDate(DateUtils.getCurrentTimestamp());
				chargePackage.setUpdatedBy(vo.getCreatedBy());

				checkIsChargePackageStillInUsed(code);
				
				chargePackageRepo.save(chargePackage);
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
	public Map<String, Object> searchServiceChargeTRX(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = serviceCharge.findDetailByServiceTypeCode((String) map.get(ApplicationConstants.APP_CODE),
					ApplicationConstants.SERVICE_TYPE_TRX, null);

			List<Map<String, Object>> resultList = new ArrayList<>();
			for (Object[] model : result.getContent()) {
				Map<String, Object> serviceCurrencyMatrixMap = new HashMap<>();
				serviceCurrencyMatrixMap.put("serviceCode", model[0]);
				serviceCurrencyMatrixMap.put("serviceName", model[1]);
				serviceCurrencyMatrixMap.put("serviceChargeId", model[2]);
				serviceCurrencyMatrixMap.put("serviceChargeName", model[3]);
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
	public Map<String, Object> searchChargePackageDetailByCode(Map<String, Object> map)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = chargePackageRepo.findDetailByServiceTypeCode(
					(String) map.get(ApplicationConstants.APP_CODE), ApplicationConstants.SERVICE_TYPE_TRX,
					(String) map.get(ApplicationConstants.STR_CODE), null);

			List<Map<String, Object>> resultList = new ArrayList<>();
			for (Object[] model : result.getContent()) {
				Map<String, Object> serviceCurrencyMatrixMap = new HashMap<>();
				serviceCurrencyMatrixMap.put("serviceCode", model[0]);
				serviceCurrencyMatrixMap.put("serviceName", model[1]);
				serviceCurrencyMatrixMap.put("serviceChargeId", model[2]);
				serviceCurrencyMatrixMap.put("serviceChargeName", model[3]);
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
	public Map<String, Object> searchByCode(String code) throws ApplicationException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			ChargePackageModel model = chargePackageRepo.findOne(code);
			
			if (model.getChargePackageDetail() != null) {
				List<Map<String, String>> listDetail = new ArrayList<>();
				for (ChargePackageDetailModel detailModel : model.getChargePackageDetail()) {
					Map<String, String> detail = new HashMap<>();

					ServiceChargeModel serviceCharge = detailModel.getServiceCharge();
					ServiceModel service = serviceCharge.getService();
					CurrencyModel currency = detailModel.getCurrency();

					detail.put("serviceCode", service.getCode());
					detail.put("serviceName", service.getName());
					detail.put("serviceChargeId", serviceCharge.getId());
					detail.put("serviceChargeName", serviceCharge.getName());
					detail.put("currencyCode", currency.getCode());
					detail.put("value", detailModel.getValue().toPlainString());

					listDetail.add(detail);
				}
				resultMap.put("result", listDetail);
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public Map<String, Object> searchSpesific(String code, String applicationCode) throws ApplicationException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<Object[]> result = serviceCharge.findDetailByServiceTypeCode(applicationCode,
					ApplicationConstants.SERVICE_TYPE_TRX, null);

			List<Map<String, Object>> resultList = new ArrayList<>();
			for (Object[] model : result.getContent()) {
				Map<String, Object> serviceCurrencyMatrixMap = new HashMap<>();
				String serviceChargeId = (String) model[2];
				serviceCurrencyMatrixMap.put("serviceCode", model[0]);
				serviceCurrencyMatrixMap.put("serviceName", model[1]);
				serviceCurrencyMatrixMap.put("serviceChargeId", serviceChargeId);
				serviceCurrencyMatrixMap.put("serviceChargeName", model[3]);
				
				ChargePackageDetailModel chargeDetail = chargePackageRepo.findDetailByServiceChargeAndChargePackage(serviceChargeId, code);
				if(chargeDetail != null) {
					serviceCurrencyMatrixMap.put("currencyCode", chargeDetail.getCurrency().getCode());
					serviceCurrencyMatrixMap.put("value", chargeDetail.getValue());
				} else {
					serviceCurrencyMatrixMap.put("currencyCode", sysParamRepo.findOne(SysParamConstants.LOCAL_CURRENCY_CODE).getValue());
					serviceCurrencyMatrixMap.put("value", BigDecimal.ZERO);
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
			Page<ChargePackageModel> result = chargePackageRepo.search(map, PagingUtils.createPageRequest(map));
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
