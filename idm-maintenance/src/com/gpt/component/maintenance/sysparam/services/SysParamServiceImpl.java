package com.gpt.component.maintenance.sysparam.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.model.SysParamModel;
import com.gpt.component.maintenance.sysparam.repository.SysParamRepository;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;

@Service
@Transactional(rollbackFor = Exception.class)
public class SysParamServiceImpl implements SysParamService {	
	@Autowired
	private SysParamRepository sysParamRepo;
	
	@Autowired
	private PendingTaskService pendingTaskService;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<SysParamModel> result = sysParamRepo.search(map, PagingUtils.createPageRequest(map));
			
			resultMap.put("result", setModelToMap(result.getContent()));
			
			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMap(List<SysParamModel> list){
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		for (SysParamModel model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.STR_CODE, model.getCode());
			map.put(ApplicationConstants.STR_NAME, model.getName());
			map.put("value", ValueUtils.getValue(model.getValue()));
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
			
			resultList.add(map);
		}
		
		return resultList;
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
				List<Map<String, Object>> sysParamList = (ArrayList<Map<String,Object>>)map.get("sysParamList");

				List<SysParamModel> sysParamListOld = new ArrayList<>(); 
				for(Map<String, Object> sysParamMap : sysParamList){
					SysParamModel sysParamModelOld = sysParamRepo.findByCode((String) 
							sysParamMap.get(ApplicationConstants.STR_CODE));
					sysParamListOld.add(sysParamModelOld);
				}

				Map<String, Object> sysParamMapOld = new HashMap<>();
				sysParamMapOld.put("sysParamList", setModelToMap(sysParamListOld));
				vo.setJsonObjectOld(sysParamMapOld);
				
			} else
				throw new BusinessException("GPT-0100003");
			
			resultMap.putAll(pendingTaskService.savePendingTask(vo)); 
			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo()); 
						
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return resultMap;
	}
	
	private SysParamModel getExistingRecord(String code, boolean isThrowError) throws BusinessException, ApplicationException {
		try {
			SysParamModel model = sysParamRepo.findByCode(code);
			if(model == null){
				if(isThrowError)
					throw new BusinessException("GPT-0100001");
			}
			
			return model;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
	}
	
	@Override
	public String getValueByCode(String code) throws BusinessException, ApplicationException {
		try {
			SysParamModel model = getExistingRecord(code, true);
			
			return model.getValue();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected PendingTaskVO setPendingTaskVO(Map<String, Object> map) throws ApplicationException, BusinessException {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setId(Helper.generateHibernateUUIDGenerator());
		vo.setCreatedBy((String)map.get(ApplicationConstants.LOGIN_USERID)); 
		vo.setMenuCode((String)map.get(ApplicationConstants.STR_MENUCODE)); 
		vo.setService("SysParamSC");
		
		List<Map<String, Object>> sysParamList = (ArrayList<Map<String,Object>>)map.get("sysParamList");

		String uniqueKeyAppend = ApplicationConstants.EMPTY_STRING;
		
		for(Map<String, Object> sysParamMap : sysParamList){
			String code = (String)sysParamMap.get(ApplicationConstants.STR_CODE);
			uniqueKeyAppend = uniqueKeyAppend + code + ApplicationConstants.DELIMITER_PIPE;
			
			vo.setUniqueKey(code);
			
			//check unique pending task
			pendingTaskService.checkUniquePendingTaskLike(vo);
			
			SysParamModel sysParam = getExistingRecord(code, true);
			
			//check value dgn regex
			Pattern r = Pattern.compile(sysParam.getRegex());
			Matcher m = r.matcher((String)sysParamMap.get("value"));

			if(!m.find()){
				throw new BusinessException("GPT-0100021");
			}
		}
		
		//set unique key
		vo.setUniqueKey(uniqueKeyAppend);
		
		return vo;
	}
	
	protected SysParamModel setMapToModel(Map<String, Object> map){
		SysParamModel model = new SysParamModel();
		model.setCode((String)map.get(ApplicationConstants.STR_CODE));
		model.setValue((String)map.get("value"));
		
		return model;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if(ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				Map<String, Object> map = (Map<String, Object>)vo.getJsonObject();
				
				List<Map<String, Object>> list = (ArrayList<Map<String,Object>>)map.get("sysParamList");
				
				for(Map<String, Object> sysParamMap : list){
					SysParamModel modelDB = getExistingRecord((String) sysParamMap.get(ApplicationConstants.STR_CODE), true);
					
					//set value yg boleh di edit
					modelDB.setValue((String) sysParamMap.get("value"));
					modelDB.setUpdatedDate(DateUtils.getCurrentTimestamp());
					modelDB.setUpdatedBy(vo.getCreatedBy());
						
					this.sysParamRepo.save(modelDB); 
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
		
		return vo;
	}
	
	@Override
	public List<String> getTransactionSessionTime() throws ApplicationException, BusinessException {
		try {
			SysParamModel model = sysParamRepo.findByCode(SysParamConstants.TRX_SCHEDULER_SESSION_TIME);
			
			return  Arrays.asList(model.getValue().split("\\|"));
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public String getProductName() throws ApplicationException, BusinessException {
		try {
			SysParamModel model = sysParamRepo.findByCode(SysParamConstants.PRODUCT_NAME);
			
			return  model.getValue();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public String getLocalCurrency() throws ApplicationException, BusinessException {
		try {
			SysParamModel model = sysParamRepo.findByCode(SysParamConstants.LOCAL_CURRENCY_CODE);
			
			return  model.getValue();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
}
