package com.gpt.component.maintenance.domesticbank.services;

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
import com.gpt.component.maintenance.domesticbank.model.DomesticBankModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
@Transactional(rollbackFor = Exception.class)
public class DomesticBankServiceImpl implements DomesticBankService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<DomesticBankModel> result = maintenanceRepo.getDomesticBankRepo().search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<DomesticBankModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (DomesticBankModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(DomesticBankModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put("bankCode", model.getCode());
		map.put("onlineBankCode", ValueUtils.getValue(model.getOnlineBankCode()));
		map.put("memberCode", ValueUtils.getValue(model.getMemberCode()));
		map.put("name", ValueUtils.getValue(model.getName()));
		
		//saat ini masih kosong karena blm di implement
		map.put("branchCode", ApplicationConstants.EMPTY_STRING);
		map.put("branchName", ApplicationConstants.EMPTY_STRING);
		map.put("cityCode", ApplicationConstants.EMPTY_STRING);
		map.put("cityName", ApplicationConstants.EMPTY_STRING);
		
		if(isGetDetail){
			map.put("organizationUnitCode", ValueUtils.getValue(model.getOrganizationUnitCode()));
			map.put("organizationUnitName", ValueUtils.getValue(model.getOrganizationUnitName()));
			map.put("address1", ValueUtils.getValue(model.getAddress1()));
			map.put("address2", ValueUtils.getValue(model.getAddress2()));
			map.put("address3", ValueUtils.getValue(model.getAddress3()));
			map.put("onilneBankConnectionStatus", ValueUtils.getValue(model.getOnlineBankConnectionStatus()));
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

				//check existing record exist or not
				DomesticBankModel domesticBankOld = getExistingRecord((String)map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(domesticBankOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				
				//check existing record exist or not
				getExistingRecord((String)map.get(ApplicationConstants.STR_CODE), true);
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
	
	private DomesticBankModel getExistingRecord(String code, boolean isThrowError) throws BusinessException{
		DomesticBankModel model = maintenanceRepo.getDomesticBankRepo().findOne(code);
		
		if(model == null){
			if(isThrowError)
				throw new BusinessException("GPT-0100001");
		}else{
			if(ApplicationConstants.YES.equals(model.getDeleteFlag())){
				if(isThrowError){
					throw new BusinessException("GPT-0100001");
				}else{
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
		vo.setService("DomesticBankSC");

		return vo;
	}

	private DomesticBankModel setMapToModel(Map<String, Object> map) {
		DomesticBankModel domesticBank = new DomesticBankModel();
		domesticBank.setCode((String) map.get(ApplicationConstants.STR_CODE));
		domesticBank.setName((String) map.get(ApplicationConstants.STR_NAME));

		if (ValueUtils.hasValue(map.get("organizationUnitCode")))
			domesticBank.setOrganizationUnitCode((String) map.get("organizationUnitCode"));

		if (ValueUtils.hasValue(map.get("organizationUnitName")))
			domesticBank.setOrganizationUnitName((String) map.get("organizationUnitName"));

		if (ValueUtils.hasValue(map.get("address1")))
			domesticBank.setAddress1((String) map.get("address1"));

		if (ValueUtils.hasValue(map.get("address2")))
			domesticBank.setAddress2((String) map.get("address2"));

		if (ValueUtils.hasValue(map.get("address3")))
			domesticBank.setAddress3((String) map.get("address3"));

		if (ValueUtils.hasValue(map.get("memberCode")))
			domesticBank.setMemberCode((String) map.get("memberCode"));

		if (ValueUtils.hasValue(map.get("interBankCode"))) {
			domesticBank.setOnlineBankCode((String) map.get("interBankCode"));
			domesticBank.setOnlineBankConnectionStatus(ApplicationConstants.YES);
		} else {
			domesticBank.setOnlineBankConnectionStatus(ApplicationConstants.NO);
		}

		return domesticBank;
	}

	private void checkUniqueRecord(String code) throws Exception {
		DomesticBankModel domesticBank = maintenanceRepo.getDomesticBankRepo().findOne(code);

		if (domesticBank != null && ApplicationConstants.NO.equals(domesticBank.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				DomesticBankModel domesticBank = setMapToModel(map);
				DomesticBankModel domesticBankExisting = maintenanceRepo.getDomesticBankRepo().findOne(domesticBank.getCode());

				// cek jika record replacement
				if (domesticBankExisting != null
						&& ApplicationConstants.YES.equals(domesticBankExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					domesticBankExisting.setName(domesticBank.getName());
					domesticBankExisting.setOrganizationUnitCode(domesticBank.getOrganizationUnitCode());
					domesticBankExisting.setOrganizationUnitName(domesticBank.getOrganizationUnitName());
					domesticBankExisting.setAddress1(domesticBank.getAddress1());
					domesticBankExisting.setAddress2(domesticBank.getAddress2());
					domesticBankExisting.setAddress3(domesticBank.getAddress3());
					domesticBankExisting.setMemberCode(domesticBank.getMemberCode());
					domesticBankExisting.setOnlineBankConnectionStatus(domesticBank.getOnlineBankConnectionStatus());
					domesticBankExisting.setOnlineBankCode(domesticBank.getOnlineBankCode());

					// set default value
					domesticBankExisting.setIdx(ApplicationConstants.IDX);
					domesticBankExisting.setDeleteFlag(ApplicationConstants.NO);
					domesticBankExisting.setInactiveFlag(ApplicationConstants.NO);
					domesticBankExisting.setSystemFlag(ApplicationConstants.NO);
					domesticBankExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					domesticBankExisting.setCreatedBy(vo.getCreatedBy());

					// set reset value
					domesticBankExisting.setUpdatedDate(null);
					domesticBankExisting.setUpdatedBy(null);

					maintenanceRepo.getDomesticBankRepo().save(domesticBankExisting);
				} else {
					// set default value
					domesticBank.setIdx(ApplicationConstants.IDX);
					domesticBank.setDeleteFlag(ApplicationConstants.NO);
					domesticBank.setInactiveFlag(ApplicationConstants.NO);
					domesticBank.setSystemFlag(ApplicationConstants.NO);
					domesticBank.setCreatedDate(DateUtils.getCurrentTimestamp());
					domesticBank.setCreatedBy(vo.getCreatedBy());

					maintenanceRepo.getDomesticBankRepo().persist(domesticBank);
				}

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				DomesticBankModel domesticBankNew = setMapToModel(map);

				DomesticBankModel domesticBankExisting = getExistingRecord(domesticBankNew.getCode(), true);

				// set value yg boleh di edit
				domesticBankExisting.setName(domesticBankNew.getName());
				domesticBankExisting.setOrganizationUnitCode(domesticBankNew.getOrganizationUnitCode());
				domesticBankExisting.setOrganizationUnitName(domesticBankNew.getOrganizationUnitName());
				domesticBankExisting.setAddress1(domesticBankNew.getAddress1());
				domesticBankExisting.setAddress2(domesticBankNew.getAddress2());
				domesticBankExisting.setAddress3(domesticBankNew.getAddress3());
				domesticBankExisting.setMemberCode(domesticBankNew.getMemberCode());
				domesticBankExisting.setOnlineBankConnectionStatus(domesticBankNew.getOnlineBankConnectionStatus());
				domesticBankExisting.setOnlineBankCode(domesticBankNew.getOnlineBankCode());

				domesticBankExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				domesticBankExisting.setUpdatedBy(vo.getCreatedBy());

				maintenanceRepo.getDomesticBankRepo().save(domesticBankExisting);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				//check existing record exist or not
				DomesticBankModel domesticBank = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);

				domesticBank.setDeleteFlag(ApplicationConstants.YES);
				domesticBank.setUpdatedDate(DateUtils.getCurrentTimestamp());
				domesticBank.setUpdatedBy(vo.getCreatedBy());

				maintenanceRepo.getDomesticBankRepo().save(domesticBank);
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

	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {

	}
}
