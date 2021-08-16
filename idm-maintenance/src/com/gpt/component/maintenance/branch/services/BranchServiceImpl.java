package com.gpt.component.maintenance.branch.services;

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

import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.CityModel;
import com.gpt.component.maintenance.parametermt.model.LevelModel;
import com.gpt.component.maintenance.utils.MaintenanceRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class BranchServiceImpl implements BranchService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MaintenanceRepository maintenanceRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<BranchModel> result = maintenanceRepo.getBranchRepo().search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<BranchModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (BranchModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(BranchModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("dscp", ValueUtils.getValue(model.getDscp()));
		map.put("address1", ValueUtils.getValue(model.getAddress1()));
		map.put("address2", ValueUtils.getValue(model.getAddress2()));
		map.put("address3", ValueUtils.getValue(model.getAddress3()));
		map.put("cityCode", model.getCity() != null ? model.getCity().getCode() : ApplicationConstants.EMPTY_STRING);
		map.put("cityName", model.getCity() != null ? model.getCity().getName() : ApplicationConstants.EMPTY_STRING);
		
		if(isGetDetail){
			map.put("levelCode", model.getLevel() != null ? model.getLevel().getCode() : ApplicationConstants.EMPTY_STRING);
			map.put("levelName", model.getLevel() != null ? model.getLevel().getName() : ApplicationConstants.EMPTY_STRING);
			map.put("parentBranchCode", model.getParentBranch() != null ? model.getParentBranch().getCode()
					: ApplicationConstants.EMPTY_STRING);
			map.put("parentBranchName", model.getParentBranch() != null ? model.getParentBranch().getName()
					: ApplicationConstants.EMPTY_STRING);
			map.put("email", ValueUtils.getValue(model.getEmail()));
			map.put("contactName", ValueUtils.getValue(model.getContactName()));
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
				BranchModel branchOld = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(branchOld, true));
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

	private BranchModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		BranchModel model = maintenanceRepo.getBranchRepo().findOne(code);

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
		vo.setService("BranchSC");

		return vo;
	}

	private BranchModel setMapToModel(Map<String, Object> map) {
		BranchModel branch = new BranchModel();
		branch.setCode((String) map.get(ApplicationConstants.STR_CODE));
		branch.setName((String) map.get(ApplicationConstants.STR_NAME));

		if (ValueUtils.hasValue(map.get("dscp")))
			branch.setDscp((String) map.get("dscp"));

		if (ValueUtils.hasValue(map.get("address1")))
			branch.setAddress1((String) map.get("address1"));

		if (ValueUtils.hasValue(map.get("address2")))
			branch.setAddress2((String) map.get("address2"));

		if (ValueUtils.hasValue(map.get("address3")))
			branch.setAddress3((String) map.get("address3"));

		if (ValueUtils.hasValue(map.get("cityCode"))) {
			CityModel city = new CityModel();
			city.setCode((String) map.get("cityCode"));
			branch.setCity(city);
		}

		if (ValueUtils.hasValue(map.get("levelCode"))) {
			LevelModel level = new LevelModel();
			level.setCode((String) map.get("levelCode"));
			branch.setLevel(level);
		}

		if (ValueUtils.hasValue(map.get("parentBranchCode"))) {
			BranchModel parentBranch = new BranchModel();
			parentBranch.setCode((String) map.get("parentBranchCode"));
			branch.setParentBranch(parentBranch);
		}

		if (ValueUtils.hasValue(map.get("email")))
			branch.setEmail((String) map.get("email"));

		if (ValueUtils.hasValue(map.get("contactName")))
			branch.setContactName((String) map.get("contactName"));

		return branch;
	}

	private void checkUniqueRecord(String branchCode) throws Exception {
		BranchModel branch = maintenanceRepo.getBranchRepo().findOne(branchCode);

		if (branch != null && ApplicationConstants.NO.equals(branch.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				BranchModel branch = setMapToModel(map);
				BranchModel branchExisting = maintenanceRepo.getBranchRepo().findOne(branch.getCode());

				// cek jika record replacement
				if (branchExisting != null && ApplicationConstants.YES.equals(branchExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					branchExisting.setName(branch.getName());
					branchExisting.setDscp(branch.getDscp());
					branchExisting.setAddress1(branch.getAddress1());
					branchExisting.setAddress2(branch.getAddress2());
					branchExisting.setAddress3(branch.getAddress3());
					branchExisting.setCity(branch.getCity());
					branchExisting.setLevel(branch.getLevel());
					branchExisting.setParentBranch(branch.getParentBranch());
					branchExisting.setEmail(branch.getEmail());
					branchExisting.setContactName(branch.getContactName());

					// set default value
					branchExisting.setIdx(ApplicationConstants.IDX);
					branchExisting.setDeleteFlag(ApplicationConstants.NO);
					branchExisting.setInactiveFlag(ApplicationConstants.NO);
					branchExisting.setSystemFlag(ApplicationConstants.NO);
					branchExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					branchExisting.setCreatedBy(vo.getCreatedBy());

					// set reset value
					branchExisting.setUpdatedDate(null);
					branchExisting.setUpdatedBy(null);

					maintenanceRepo.getBranchRepo().save(branchExisting);
				} else {
					// set default value
					branch.setIdx(ApplicationConstants.IDX);
					branch.setDeleteFlag(ApplicationConstants.NO);
					branch.setInactiveFlag(ApplicationConstants.NO);
					branch.setSystemFlag(ApplicationConstants.NO);
					branch.setCreatedDate(DateUtils.getCurrentTimestamp());
					branch.setCreatedBy(vo.getCreatedBy());

					maintenanceRepo.getBranchRepo().persist(branch);
				}

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				BranchModel branchNew = setMapToModel(map);

				BranchModel branchExisting = getExistingRecord(branchNew.getCode(), true);

				// set value yg boleh di edit
				branchExisting.setName(branchNew.getName());
				branchExisting.setDscp(branchNew.getDscp());
				branchExisting.setAddress1(branchNew.getAddress1());
				branchExisting.setAddress2(branchNew.getAddress2());
				branchExisting.setAddress3(branchNew.getAddress3());
				branchExisting.setCity(branchNew.getCity());
				branchExisting.setLevel(branchNew.getLevel());
				branchExisting.setParentBranch(branchNew.getParentBranch());
				branchExisting.setEmail(branchNew.getEmail());
				branchExisting.setContactName(branchNew.getContactName());

				branchExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
				branchExisting.setUpdatedBy(vo.getCreatedBy());

				maintenanceRepo.getBranchRepo().save(branchExisting);
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				BranchModel branch = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
				branch.setDeleteFlag(ApplicationConstants.YES);
				branch.setUpdatedDate(DateUtils.getCurrentTimestamp());
				branch.setUpdatedBy(vo.getCreatedBy());

				maintenanceRepo.getBranchRepo().save(branch);
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
		try{
			maintenanceRepo.isCityValid((String) map.get("cityCode"));
			maintenanceRepo.isLevelValid((String) map.get("levelCode"));
			
			if(ValueUtils.hasValue(map.get("parentBranchCode")))
				maintenanceRepo.isParentBranchValid((String) map.get("parentBranchCode"));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
}
