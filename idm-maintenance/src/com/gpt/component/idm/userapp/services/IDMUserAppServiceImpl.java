package com.gpt.component.idm.userapp.services;

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
import com.gpt.component.idm.app.model.IDMApplicationModel;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.userapp.model.IDMUserAppModel;
import com.gpt.component.idm.userapp.repository.IDMUserAppRepository;
import com.gpt.component.idm.utils.IDMRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class IDMUserAppServiceImpl implements IDMUserAppService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private IDMUserAppRepository userAppRepo;

	@Autowired
	private IDMRepository idmRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<IDMUserAppModel> result = userAppRepo.search(map, PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<IDMUserAppModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (IDMUserAppModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(IDMUserAppModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("appCode", ValueUtils.hasValue(model.getUser()) ? model.getApplication().getCode()
				: ApplicationConstants.EMPTY_STRING);
		map.put("appName", ValueUtils.hasValue(model.getUser()) ? model.getApplication().getName()
				: ApplicationConstants.EMPTY_STRING);
		
		return map;
	}
	
	private List<Map<String, Object>> setModelIDMApplicationToMap(List<IDMApplicationModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (IDMApplicationModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(IDMApplicationModel model) {
		Map<String, Object> map = new HashMap<>();
		map.put("appCode", model.getCode());
		map.put("appName", model.getName());
		
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map);

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			idmRepo.isIDMUserValid((String) map.get("userCode"));

			List<Map<String, Object>> appCodeList = (List<Map<String, Object>>) map.get("appCodeList");
			List<String> appCodes = new ArrayList<>(appCodeList.size());
			for (Map<String, Object> idmAppCodeMap : appCodeList) {
				appCodes.add((String) idmAppCodeMap.get("appCode"));
				
				idmRepo.isIDMAppValid((String) idmAppCodeMap.get("appCode"));
			}

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				Map<String, Object> idmUserAppOld = new HashMap<>();
				idmUserAppOld.put("userCode", (String) map.get("userCode"));

				idmUserAppOld.put("appCodeList", setModelIDMApplicationToMap(userAppRepo.findApplicationsByUserCode((String) map.get("userCode"))));
				vo.setJsonObjectOld(idmUserAppOld);
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
		vo.setUniqueKey((String) map.get("userCode"));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("IDMUserAppSC");

		return vo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

				String userCode = (String) map.get("userCode");

				// delete all by user code
				userAppRepo.deleteByUserCode(userCode);
				userAppRepo.flush();

				List<Map<String, Object>> list = (ArrayList<Map<String, Object>>) map.get("appCodeList");

				for (Map<String, Object> roleMap : list) {
					// insert new user role
					IDMUserAppModel userApp = new IDMUserAppModel();

					IDMApplicationModel application = new IDMApplicationModel();
					application.setCode((String) roleMap.get("appCode"));
					userApp.setApplication(application);

					IDMUserModel user = new IDMUserModel();
					user.setCode(userCode);
					userApp.setUser(user);

					userApp.setCreatedDate(DateUtils.getCurrentTimestamp());
					userApp.setCreatedBy(vo.getCreatedBy());

					userAppRepo.persist(userApp);
				}
			}
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
	public void saveUserApplication(IDMUserModel idmUser, String applicationCode, String createdBy){
		IDMUserAppModel userApp = new IDMUserAppModel();
		userApp.setUser(idmUser);
		
		IDMApplicationModel application = new IDMApplicationModel();
		application.setCode(applicationCode);
		userApp.setApplication(application);
		userApp.setCreatedBy(createdBy);
		userApp.setCreatedDate(DateUtils.getCurrentTimestamp());
		userAppRepo.save(userApp);
	}
}