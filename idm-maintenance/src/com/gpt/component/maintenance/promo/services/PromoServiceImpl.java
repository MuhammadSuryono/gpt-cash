package com.gpt.component.maintenance.promo.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
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

	@Value("${gpcash.helpdesk.upload.path}")
	private String pathUpload;
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
//		map.put(ApplicationConstants.CORP_ID, map.get(ApplicationConstants.LOGIN_CORP_ID));
		try {
			Page<PromoModel> result = promoRepo.search(map, PagingUtils.createPageRequest(map));
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
	
	private List<Map<String, Object>> setModelToMap(List<PromoModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (PromoModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(PromoModel model, boolean isGetDetail) {
		
			Map<String, Object> map = new HashMap<>();
			map.put("id", model.getId());
			map.put("fileName", ValueUtils.getValue(model.getFileName()));
			map.put("infoType", ValueUtils.getValue(model.getInfoType()));
			map.put("title", ValueUtils.getValue(model.getTitle()));
			map.put("uploadId", ValueUtils.getValue(model.getUploadId()));
			map.put("description", ValueUtils.getValue(model.getDscp()));
			
			try {
				
				String fileUrl = pathUpload + File.separator + model.getFileName() + "_" + model.getUploadId();
				File file = new File(fileUrl);
				byte[] fileContent = Files.readAllBytes(file.toPath());
				
				map.put("fileBytes", fileContent);
			} catch (Exception e) {
//				logger.error("No Image Uploaded : " + e.getMessage(), e);
			}
			
			if (isGetDetail) {
				map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
				map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
				map.put("fileUrl", ValueUtils.getValue(model.getFileUrl()));
				map.put("fileFormat", ValueUtils.getValue(model.getFileFormat()));
				map.put("uploadDate", ValueUtils.getValue(model.getUploadDate()));
				map.put("corpId", ValueUtils.getValue(model.getCorpId()));
				
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
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
//				checkUniqueRecord(map); 
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);
				List<PromoModel> existingList = new ArrayList<>();
				existingList.add(getExistingModel((String) map.get("fileId")));
				vo.setJsonObjectOld(setModelToMap(existingList, true).get(0));
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
				
				// check existing record exist or not
				getExistingModel((String) map.get("fileId"));
				
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

	private void checkUniqueRecord(Map<String, Object> map) throws Exception{
		
		List<PromoModel> list = promoRepo.search(map, null).getContent();

		if (!list.isEmpty()) {
			throw new BusinessException("GPT-0100004");
		}
		
	}
	
	private PromoModel getExistingModel(String uploadId) throws BusinessException {
		
		PromoModel existModel = promoRepo.findByUploadIdAndDeleteFlag(uploadId, ApplicationConstants.NO);
		
		if (existModel == null)
				throw new BusinessException("GPT-0100001");
		
		return existModel;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey(new StringBuffer((String) map.get("fileId")).append(ApplicationConstants.DELIMITER_PIPE)
				.append((String) map.get(ApplicationConstants.STR_CODE)).toString());
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("PromoSC");

		return vo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {

		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				
				PromoModel model = setMapToModel(map);
				
				model.setDeleteFlag(ApplicationConstants.NO);
				model.setCreatedBy(vo.getCreatedBy());
				model.setCreatedDate(new Timestamp(System.currentTimeMillis()));
				
				promoRepo.persist(model);
				
			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				
				PromoModel oldModel = getExistingModel((String)map.get("fileId"));
				PromoModel newModel = setMapToModel(map);
				
				// set value yg boleh di edit
				oldModel.setTitle(newModel.getTitle());
				oldModel.setDscp(newModel.getDscp());
				
				oldModel.setUpdatedBy(vo.getCreatedBy());
				oldModel.setUpdatedDate(DateUtils.getCurrentTimestamp());
				
				promoRepo.save(oldModel);
				
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				
				PromoModel model = getExistingModel((String)map.get("fileId"));
				model.setDeleteFlag(ApplicationConstants.YES);
				model.setUpdatedBy(vo.getCreatedBy());
				model.setUpdatedDate(DateUtils.getCurrentTimestamp());
				
				promoRepo.save(model);
				
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return vo;
	}

	private PromoModel setMapToModel(Map<String, Object> map) throws Exception {
		
		PromoModel model = new PromoModel();
		model.setUploadId((String)map.get("fileId"));
		model.setFileName((String)map.get("fileName"));
		model.setTitle((String)map.get("title"));
		model.setDscp((String)map.get("description"));
		model.setFileFormat((String)map.get("fileFormat"));
		model.setInfoType((String)map.get("infoType"));
		model.setUploadDate((Timestamp) Helper.parseStringToTimestamp((String) map.get("uploadedDate")));
		model.setCorpId((String)map.get("corpId"));
		
		return model;
	}
	
	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {

		return null;
	}

	@Override
	public Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException {
		byte[] rawdata = (byte[])map.get("rawdata");
		String filename = (String)map.get(ApplicationConstants.FILENAME);

		if (rawdata == null)
			throw new BusinessException("GPT-0100113"); // Please select a file to upload

        try {
        	String id = Helper.generateHibernateUUIDGenerator();
            Path path = Paths.get(pathUpload + File.separator + filename + "_" + id);
            Files.write(path, rawdata);
            
			Map<String,Object> result = new HashMap<>();
			result.put("fileId", id);
			result.put(ApplicationConstants.FILENAME, filename);
			result.put("uploadDateTime", DateUtils.getCurrentTimestamp());
			
			return result;
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
	}

	@Override
	public Map<String, Object> searchNewsInfo(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> returnMap = new HashMap<>();
		
		try {
			List<PromoModel> listAll = promoRepo.findAllNewsAndInfo();
			List<Map<String, Object>> newsList = new ArrayList<>();
			List<Map<String, Object>> infoList = new ArrayList<>();
			for (PromoModel model : listAll) {
				
				if (model.getInfoType().equals("NEWS")) {
					newsList.add(setModelToMap(model, true));
				} else if (model.getInfoType().equals("INFO")) {
					infoList.add(setModelToMap(model, true));
				}
			}
			returnMap.put("newsList", newsList);
			returnMap.put("infoList", infoList);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		return returnMap;
	}
}
