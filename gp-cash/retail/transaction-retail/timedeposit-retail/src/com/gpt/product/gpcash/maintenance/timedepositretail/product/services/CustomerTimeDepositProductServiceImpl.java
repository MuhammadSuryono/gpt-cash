package com.gpt.product.gpcash.maintenance.timedepositretail.product.services;

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
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.maintenance.timedepositretail.product.model.CustomerTimeDepositProductModel;
import com.gpt.product.gpcash.maintenance.timedepositretail.product.model.CustomerTimeDepositTermModel;
import com.gpt.product.gpcash.maintenance.timedepositretail.product.repository.CustomerTimeDepositProductRepository;
import com.gpt.product.gpcash.maintenance.timedepositretail.product.repository.CustomerTimeDepositTermRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerTimeDepositProductServiceImpl implements CustomerTimeDepositProductService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustomerTimeDepositProductRepository productRepo;

	@Autowired
	private CustomerTimeDepositTermRepository termRepo;
	
	@Autowired
	private PendingTaskService pendingTaskService;
	
	private static final String DAYS = "days";
	
	private static final String MONTHS = "months";
	
	private static final String UPDATE_TERM = "UPDATE_TERM";

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerTimeDepositProductModel> result = productRepo.search(map, PagingUtils.createPageRequest(map));

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

	private List<Map<String, Object>> setModelToMap(List<CustomerTimeDepositProductModel> list, boolean isGetDetail) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerTimeDepositProductModel model : list) {
			resultList.add(setModelToMap(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerTimeDepositProductModel model, boolean isGetDetail) {
		Map<String, Object> map = new HashMap<>();
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("dscp", ValueUtils.getValue(model.getDscp()));
		
		
		
		if(isGetDetail){
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
			
			try {
				List<CustomerTimeDepositTermModel> termList = termRepo.searchTermByProductCode(model.getCode());
				map.put("termList", setModelToMapTerm(termList, true));
			} catch (Exception e) {
				e.printStackTrace();
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

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				checkCustomValidation(map);
				
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);
				checkUniqueRecord((String) map.get(ApplicationConstants.STR_CODE)); // cek ke table jika record already exist
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_UPDATE)) {
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);

				// check existing record exist or not
				CustomerTimeDepositProductModel productOld = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
				vo.setJsonObjectOld(setModelToMap(productOld, true));
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);

				// check existing record exist or not
				getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
			
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(UPDATE_TERM)) {
				checkCustomValidation(map);
				vo.setAction(ApplicationConstants.WF_ACTION_UPDATE);
				
				List<Map<String, Object>> termList = (ArrayList<Map<String, Object>>) map.get("termList");
				List<Map<String, Object>> termListOld = new ArrayList<>();
				CustomerTimeDepositProductModel productOld = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
				
				for (Map<String, Object> termMap : termList) {
					CustomerTimeDepositTermModel termOld = getExistingTermRecord((String) termMap.get(ApplicationConstants.STR_CODE), productOld.getCode(), false);
					if (termOld !=null) {
						termListOld.add(setModelToMapTerm(termOld, true));						
					}
				}
				Map<String, Object> productMap = new HashMap<>();
				productMap.put("product", setModelToMap(productOld, true));
				productMap.put("termList", termListOld);
				
				vo.setJsonObjectOld(productMap);
				
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

	private CustomerTimeDepositProductModel getExistingRecord(String code, boolean isThrowError) throws BusinessException {
		CustomerTimeDepositProductModel model = productRepo.findOne(code);

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
	
	private CustomerTimeDepositTermModel getExistingTermRecord(String code, String productCode, boolean isThrowError) throws Exception {
		CustomerTimeDepositTermModel model = termRepo.findByCodeAndProductType_code(code, productCode);

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
		vo.setService("CustomerTimeDepositProductSC");

		return vo;
	}

	private CustomerTimeDepositProductModel setMapToModel(Map<String, Object> map) {
		CustomerTimeDepositProductModel product = new CustomerTimeDepositProductModel();
		product.setCode((String) map.get(ApplicationConstants.STR_CODE));
		product.setName((String) map.get(ApplicationConstants.STR_NAME));

		if (ValueUtils.hasValue(map.get("description")))
			product.setDscp((String) map.get("description"));

		return product;
	}

	private void checkUniqueRecord(String productCode) throws Exception {
		CustomerTimeDepositProductModel product = productRepo.findOne(productCode);

		if (product != null && ApplicationConstants.NO.equals(product.getDeleteFlag())) {
			throw new BusinessException("GPT-0100004");
		}
	}

	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();
			
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				CustomerTimeDepositProductModel product = setMapToModel(map);
				CustomerTimeDepositProductModel productExisting = productRepo.findOne(product.getCode());

				// cek jika record replacement
				if (productExisting != null && ApplicationConstants.YES.equals(productExisting.getDeleteFlag())) {
					// set value yg bisa di ganti
					productExisting.setName(product.getName());
					productExisting.setDscp(product.getDscp());

					// set default value
					productExisting.setDeleteFlag(ApplicationConstants.NO);
					productExisting.setCreatedDate(DateUtils.getCurrentTimestamp());
					productExisting.setCreatedBy(vo.getCreatedBy());

					// set reset value
					productExisting.setUpdatedDate(null);
					productExisting.setUpdatedBy(null);

					productRepo.save(productExisting);
				} else {
					// set default value
					product.setDeleteFlag(ApplicationConstants.NO);
					product.setCreatedDate(DateUtils.getCurrentTimestamp());
					product.setCreatedBy(vo.getCreatedBy());

					productRepo.persist(product);
				}

			} else if (ApplicationConstants.WF_ACTION_UPDATE.equals(vo.getAction())) {
				
				List<Map<String, Object>> termList = (ArrayList<Map<String, Object>>) map.get("termList");
				ArrayList<CustomerTimeDepositTermModel> terms = new ArrayList<>();
				CustomerTimeDepositProductModel productExisting = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
								
				if (termList !=null) {
					
					termRepo.deleteTermByProduct(productExisting.getCode());
					
					for (Map<String, Object> termMap : termList) {
						CustomerTimeDepositTermModel termNew = setMapToModelTerm(termMap);
						termNew.setProductType(productExisting);
						termNew.setUpdatedBy(vo.getCreatedBy());
						termNew.setUpdatedDate(DateUtils.getCurrentTimestamp());
						terms.add(termNew);
						
					}
					
					termRepo.save(terms);
				} else {
					
					CustomerTimeDepositProductModel productNew = setMapToModel(map);
					productExisting.setName(productNew.getName());
					productExisting.setDscp(productNew.getDscp());
					productExisting.setUpdatedDate(DateUtils.getCurrentTimestamp());
					productExisting.setUpdatedBy(vo.getCreatedBy());

					productRepo.save(productExisting);
				}

			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				CustomerTimeDepositProductModel product = getExistingRecord((String) map.get(ApplicationConstants.STR_CODE), true);
				product.setDeleteFlag(ApplicationConstants.YES);
				product.setUpdatedDate(DateUtils.getCurrentTimestamp());
				product.setUpdatedBy(vo.getCreatedBy());

				productRepo.save(product);
				
				termRepo.deleteTermByProduct(product.getCode());
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return vo;
	}

	private CustomerTimeDepositTermModel setMapToModelTerm(Map<String, Object> termMap) {
		
		CustomerTimeDepositTermModel term = new CustomerTimeDepositTermModel();
		term.setCode((String) termMap.get(ApplicationConstants.STR_CODE));
		term.setTermType((String) termMap.get("termType"));
		term.setTermParam((Integer) termMap.get("termParam"));
		
		return term;
	}

	@Override
	public PendingTaskVO reject(PendingTaskVO vo) throws ApplicationException, BusinessException {
		// if any spesific business function for reject, applied here

		return vo;
	}
	
	private void checkCustomValidation(Map<String, Object> map) throws BusinessException, Exception {
		try{
			
			/*if(ValueUtils.hasValue(map.get("parentBranchCode")))
				maintenanceRepo.isParentBranchValid((String) map.get("parentBranchCode"));
		} catch (BusinessException e) {
			throw e;*/
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public Map<String, Object> searchTermByProduct(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			List<CustomerTimeDepositTermModel> result = termRepo.searchTermByProductCode((String) map.get("productCode"));
//			Page<TimeDepositTermModel> result = termRepo.findByProductType_CodeAndDeleteFlagOrderByTermParamAsc((String) map.get("productCode"), ApplicationConstants.NO, PagingUtils.createPageRequest(map));
			if(ApplicationConstants.WF_ACTION_SEARCH.equals(map.get(ApplicationConstants.WF_ACTION))){
//				resultMap.put("result", setModelToMapTerm(result.getContent(), false));
				resultMap.put("result", setModelToMapTerm(result, false));
			} else {
//				resultMap.put("result", setModelToMapTerm(result.getContent(), true));
				resultMap.put("result", setModelToMapTerm(result, true));
			}
			
//			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		return resultMap;
	}
	
	private List<Map<String, Object>> setModelToMapTerm(List<CustomerTimeDepositTermModel> list, boolean isGetDetail) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerTimeDepositTermModel model : list) {
			resultList.add(setModelToMapTerm(model, isGetDetail));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMapTerm(CustomerTimeDepositTermModel model, boolean isGetDetail) throws Exception {
		Map<String, Object> map = new HashMap<>();
		
		//untuk kebutuhan automatic di screen
		map.put(ApplicationConstants.STR_CODE, model.getCode());
		map.put(ApplicationConstants.STR_NAME, ValueUtils.getValue(model.getName()));
		map.put("typeCode", model.getTermType());
		if (model.getTermType().equals("D")) {
			map.put("typeName", DAYS);
		} else if (model.getTermType().equals("M")) {
			map.put("typeName", MONTHS);
		}
		map.put("termParam", ValueUtils.getValue(model.getTermParam()));
		
		
		if(isGetDetail){
			map.put("productCode", ValueUtils.getValue(model.getProductType().getCode()));
			map.put("productName", ValueUtils.getValue(model.getProductType().getName()));
			map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
			map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
			map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
			map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));
		}

		return map;
	}
}
