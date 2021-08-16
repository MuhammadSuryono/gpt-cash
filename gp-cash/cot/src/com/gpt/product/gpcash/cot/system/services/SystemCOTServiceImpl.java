package com.gpt.product.gpcash.cot.system.services;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.cot.system.model.SystemCOTModel;
import com.gpt.product.gpcash.cot.system.repository.SystemCOTRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class SystemCOTServiceImpl implements SystemCOTService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SystemCOTRepository systemCOTRepo;
	
	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<SystemCOTModel> result = systemCOTRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<SystemCOTModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (SystemCOTModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(SystemCOTModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		
		String[] startTime = model.getStartTime().split(":");
		map.put("startHour", startTime[0]);
		map.put("startMinutes", startTime[1]);
		
		String[] endTime = model.getEndTime().split(":");
		map.put("endHour", endTime[0]);
		map.put("endMinutes", endTime[1]);
		
		if(isGetDetail){
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}
		
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map); 

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				//jika edit maka simpan pending task untuk old value. Old value diambil dari DB
				List<Map<String, Object>> systemCOTList = (ArrayList<Map<String,Object>>)map.get("systemCOTList");

				List<SystemCOTModel> systemCOTListOld = new ArrayList<>(); 
				for(Map<String, Object> systemCOTMap : systemCOTList){
					SystemCOTModel systemCOTModelOld = systemCOTRepo.findByCodeAndApplicationCode((String) 
							systemCOTMap.get(ApplicationConstants.STR_CODE), (String) 
							systemCOTMap.get(ApplicationConstants.APP_CODE));
					systemCOTListOld.add(systemCOTModelOld);
				}

				Map<String, Object> systemCOTMapOld = new HashMap<>();
				systemCOTMapOld.put("systemCOTList", setModelToMap(systemCOTListOld, true));
				vo.setJsonObjectOld(systemCOTMapOld);
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

	private SystemCOTModel getExistingRecord(String code, String applicationCode, boolean isThrowError) throws Exception {
		SystemCOTModel model = systemCOTRepo.findByCodeAndApplicationCode(code, applicationCode);

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

	@SuppressWarnings("unchecked")
	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) throws BusinessException, Exception {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("SystemCOTSC");
		
		List<Map<String, Object>> sysCOTList = (ArrayList<Map<String,Object>>)map.get("systemCOTList");

		String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
		
		for(Map<String, Object> systemCOTMap : sysCOTList){
			String code = (String)systemCOTMap.get("code");
			String applicationCode = (String)systemCOTMap.get(ApplicationConstants.APP_CODE);
			uniqueKeyAppend = uniqueKeyAppend + code + ApplicationConstants.DELIMITER_PIPE;
			
			vo.setUniqueKey(code);
			
			//check unique pending task
			pendingTaskService.checkUniquePendingTaskLike(vo);
			
			getExistingRecord(code, applicationCode, true);
			
			checkCustomValidation(systemCOTMap);
			
		}
		
		//set unique key
		vo.setUniqueKey(uniqueKeyAppend);

		return vo;
	}

	private SystemCOTModel setMapToModel(Map<String, Object> map) {
		SystemCOTModel systemCOT = new SystemCOTModel();
		systemCOT.setCode((String) map.get(ApplicationConstants.STR_CODE));
		systemCOT.setName((String) map.get(ApplicationConstants.STR_NAME));
		
		String startHour = (String) map.get("startHour");
		String startMinutes = (String) map.get("startMinutes");
		systemCOT.setStartTime(startHour.concat(":").concat(startMinutes));
		
		String endHour = (String) map.get("endHour");
		String endMinutes = (String) map.get("endMinutes");
		systemCOT.setEndTime(endHour.concat(":").concat(endMinutes));
		
		IDMApplicationModel application = new IDMApplicationModel();
		application.setCode((String) map.get(ApplicationConstants.APP_CODE));
		systemCOT.setApplication(application);
		
		return systemCOT;
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException {
		String startHour = (String) map.get("startHour");
		String startMinutes = (String) map.get("startMinutes");
		String endHour = (String) map.get("endHour");
		String endMinutes = (String) map.get("endMinutes");
		
		if(Integer.parseInt(startHour) < 0 || Integer.parseInt(startHour) > 24){
			throw new BusinessException("GPT-0100078");
		}
		
		if(Integer.parseInt(startMinutes) < 0 || Integer.parseInt(startMinutes) > 59){
			throw new BusinessException("GPT-0100078");
		}
		
		if(Integer.parseInt(endHour) < 0 || Integer.parseInt(endHour) > 24){
			throw new BusinessException("GPT-0100078");
		}
		
		if(Integer.parseInt(endMinutes) < 0 || Integer.parseInt(endMinutes) > 59){
			throw new BusinessException("GPT-0100078");
		}
		
		if(Integer.parseInt(startHour) > Integer.parseInt(endHour)){
			throw new BusinessException("GPT-0100078");
		}
		
		if(Integer.parseInt(startHour) == Integer.parseInt(endHour)){
			if(Integer.parseInt(startMinutes) > Integer.parseInt(endMinutes)){
				throw new BusinessException("GPT-0100078");
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>)vo.getJsonObject();
			
			List<Map<String, Object>> list = (ArrayList<Map<String,Object>>)map.get("systemCOTList");

			if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				for(Map<String, Object> systemCOTMap : list){
					SystemCOTModel systemCOTExisting = getExistingRecord(
							(String) systemCOTMap.get(ApplicationConstants.STR_CODE),
							(String) systemCOTMap.get(ApplicationConstants.APP_CODE),
							true);

					SystemCOTModel systemCOTNew = setMapToModel(systemCOTMap);
					
					// set value yg boleh di edit
					systemCOTExisting.setName(systemCOTNew.getName());
					systemCOTExisting.setStartTime(systemCOTNew.getStartTime());
					systemCOTExisting.setEndTime(systemCOTNew.getEndTime());
					systemCOTExisting.setApplication(systemCOTNew.getApplication());

					updateSystemCOT(systemCOTExisting, vo.getCreatedBy());
				}
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
	public void updateSystemCOT(SystemCOTModel systemCOT, String updatedBy)
			throws ApplicationException, BusinessException {
		systemCOT.setUpdatedDate(DateUtils.getCurrentTimestamp());
		systemCOT.setUpdatedBy(updatedBy);
		systemCOTRepo.save(systemCOT);
	}

	@Override
	public void validateSystemCOT(String code, String applicationCode)
			throws Exception {
		SystemCOTModel systemCOT = systemCOTRepo.findByCodeAndApplicationCode(code, applicationCode);
		
		if(logger.isDebugEnabled()) {
			logger.info("COT Code : " + code);
			logger.info("COT Application Code : " + applicationCode);
		}
		
		if(systemCOT == null){
			throw new BusinessException("GPT-0100110");
		}
		
		String[] startTime = systemCOT.getStartTime().split(":");
		Calendar cotStart = Calendar.getInstance();
		cotStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
		cotStart.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
		cotStart.set(Calendar.SECOND, 0);
		cotStart.set(Calendar.MILLISECOND, 0);
		
		String[] endTime = systemCOT.getEndTime().split(":");
		Calendar cotEnd = Calendar.getInstance();
		cotEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
		cotEnd.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
		cotEnd.set(Calendar.SECOND, 0);
		cotEnd.set(Calendar.MILLISECOND, 0);
		
		Calendar instructionDateCal = Calendar.getInstance();
		
		if (logger.isDebugEnabled()){
			logger.debug("\n\n\n\n\n\n");
			logger.debug("code: " + code);
			logger.debug("applicationCode: " + applicationCode);
			logger.debug("COT start : " + cotStart.getTime());
			logger.debug("COT end : " + cotEnd.getTime());
			logger.debug("Today Date : " + instructionDateCal.getTime());
			logger.debug("\n\n\n\n\n\n");
		}
		
		if(!(instructionDateCal.compareTo(cotStart) >= 0 && instructionDateCal.compareTo(cotEnd) <= 0)){
			throw new BusinessException("GPT-0100111");
		}
	}
	
	@Override
	public void validateSystemCOTWithSessionTime(String code, String applicationCode, String sessionTime)
			throws Exception {
		SystemCOTModel systemCOT = systemCOTRepo.findByCodeAndApplicationCode(code, applicationCode);
		
		if(systemCOT == null){
			throw new BusinessException("GPT-0100110");
		}
		
		String[] startTime = systemCOT.getStartTime().split(":");
		Calendar cotStart = Calendar.getInstance();
		cotStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
		cotStart.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
		cotStart.set(Calendar.SECOND, 0);
		cotStart.set(Calendar.MILLISECOND, 0);
		
		String[] endTime = systemCOT.getEndTime().split(":");
		Calendar cotEnd = Calendar.getInstance();
		cotEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
		cotEnd.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
		cotEnd.set(Calendar.SECOND, 0);
		cotEnd.set(Calendar.MILLISECOND, 0);
		
		String[] sessionTimeArr = sessionTime.split("\\:");
		int sessionTimeHour = Integer.valueOf(sessionTimeArr[0]);
		int sessionTimeMinute = Integer.valueOf(sessionTimeArr[1]);
		Calendar instructionDateCal = Calendar.getInstance();
		instructionDateCal.set(Calendar.HOUR_OF_DAY, sessionTimeHour);
		instructionDateCal.set(Calendar.MINUTE, sessionTimeMinute);
		instructionDateCal.set(Calendar.SECOND, 0);
		instructionDateCal.set(Calendar.MILLISECOND, 0);
		
		if (logger.isDebugEnabled()){
			logger.debug("COT start : " + cotStart.getTime());
			logger.debug("COT end : " + cotEnd.getTime());
			logger.debug("Future / Recurring Date : " + instructionDateCal.getTime());
		}
		
		if(!(instructionDateCal.compareTo(cotStart) >= 0 && instructionDateCal.compareTo(cotEnd) <= 0)){
			throw new BusinessException("GPT-0100111");
		}
	}
	
	@Override
	public Map<String, Object> findByApplicationCodeSortByEndTime(String applicationCode)
			throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			
			List<SystemCOTModel> sysCOTList = systemCOTRepo.findByApplicationCodeSortByEndTime(applicationCode);

			List<Map<String, Object>> sysCOTResult = new ArrayList<>();
			for (SystemCOTModel model : sysCOTList) {
				Map<String, Object> modelMap = new HashMap<>();
				modelMap.put("name", model.getName());
				modelMap.put("startTime", model.getStartTime());
				modelMap.put("endTime", model.getEndTime());
				
				sysCOTResult.add(modelMap);
			}
			resultMap.put("result", sysCOTResult);

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
}