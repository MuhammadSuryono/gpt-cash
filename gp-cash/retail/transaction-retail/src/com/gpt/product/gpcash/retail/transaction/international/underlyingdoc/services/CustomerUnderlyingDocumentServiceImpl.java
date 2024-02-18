package com.gpt.product.gpcash.retail.transaction.international.underlyingdoc.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.maintenance.branch.model.BranchModel;
import com.gpt.component.maintenance.parametermt.model.DocumentTypeModel;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.transaction.international.underlyingdoc.model.CustomerUnderlyingDocumentModel;
import com.gpt.product.gpcash.retail.transaction.international.underlyingdoc.repository.CustomerUnderlyingDocumentRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerUnderlyingDocumentServiceImpl implements CustomerUnderlyingDocumentService {
	@Autowired
	private CustomerUnderlyingDocumentRepository underlyingRepo;

	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		
		String customerId = (String) map.get(ApplicationConstants.CUST_ID);
		String loginUser = (String) map.get(ApplicationConstants.LOGIN_USERID);
		
		try {
			String processReferenceNo = Helper.generateBackOfficeReferenceNo();
			List<Map<String, Object>> documentList = (ArrayList<Map<String, Object>>) map.get("documentList");
			
			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				
				for (Map<String, Object> documentMap : documentList) {
					
					CustomerUnderlyingDocumentModel underlying = setMapToModel(documentMap, customerId);
					underlying.setCreatedDate(DateUtils.getCurrentTimestamp());
					underlying.setCreatedBy(loginUser);
					underlying.setReferenceNo(processReferenceNo);
					underlying.setDeleteFlag(ApplicationConstants.NO);
					
					underlyingRepo.persist(underlying);
				}
				
			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				
				for (Map<String, Object> documentMap : documentList) {
					
					String id = (String) documentMap.get("id");
					
					CustomerUnderlyingDocumentModel underlying = underlyingRepo.findOne(id);
					underlying.setUpdatedDate(DateUtils.getCurrentTimestamp());
					underlying.setUpdatedBy(loginUser);
					underlying.setDeleteReferenceNo(processReferenceNo);
					underlying.setDeleteFlag(ApplicationConstants.YES);
					
					underlyingRepo.save(underlying);
//					underlyingRepo.delete(id);
				}
				
			} else {
				throw new BusinessException("GPT-0100003");
			}

			resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, processReferenceNo);
			resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200009");
			resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + Helper.DATE_TIME_FORMATTER.format(DateUtils.getCurrentTimestamp()));

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<CustomerUnderlyingDocumentModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CustomerUnderlyingDocumentModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(CustomerUnderlyingDocumentModel model ) {
		Map<String, Object> map = new HashMap<>();

		map.put("id", model.getId());
		map.put("documentTypeCode", model.getDocumentType().getCode());
		map.put("documentTypeName", model.getDocumentType().getName());
		map.put("underlyingAmount", model.getUnderlyingAmount());
		map.put("remark", model.getRemark1());
		map.put("expiryDate", model.getExpiryDate());

		map.put("branchCode", model.getBranch().getCode());
		map.put("branchName", model.getBranch().getName());
		
		map.put("underlyingCode", model.getUnderlyingCode());
		map.put("currency", model.getUnderlyingCurrency());
		
		map.put(ApplicationConstants.CUST_ID, model.getCustomer().getId());
		map.put("customerName", model.getCustomer().getName());
		
		map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
		map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
		map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
		map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));

		return map;
	}

	private CustomerUnderlyingDocumentModel setMapToModel(Map<String, Object> map, String customerId) throws Exception {
		CustomerUnderlyingDocumentModel underlying = new CustomerUnderlyingDocumentModel();

		CustomerModel customer = new CustomerModel();
		customer.setId(customerId);
		underlying.setCustomer(customer);

		DocumentTypeModel document = new DocumentTypeModel();
		document.setCode((String)map.get("documentTypeCode"));
		underlying.setDocumentType(document);
		
		if (document.getCode().equalsIgnoreCase("UNLY")) {
			String code = Helper.generateBackOfficeReferenceNo();
			underlying.setUnderlyingCode(code.substring(1));
		}
		
		BranchModel branch = new BranchModel();
		branch.setCode((String)map.get("branchCode"));
		underlying.setBranch(branch);
		
		underlying.setUnderlyingAmount(new BigDecimal(map.get("underlyingAmount").toString()));
		underlying.setUnderlyingCurrency((String)map.get("underlyingCcy"));
		underlying.setRemark1((String) map.get("remark"));
		underlying.setExpiryDate(new Timestamp(((Date) map.get("expiryDate")).getTime()));

		return underlying;
	}

	private void checkUniqueRecord(String docTypeCode, BigDecimal underlyingAmount) throws Exception {
		if (!underlyingRepo.findByDocumentTypeCodeAndUnderlyingAmount(docTypeCode, underlyingAmount).isEmpty()) {
			throw new BusinessException("GPT-0100004");
		}
	}
	
	@Override
	public void validateDetail(String docTypeCode, String underlyingAmount) throws ApplicationException, BusinessException {
		try {
			
			checkUniqueRecord(docTypeCode, new BigDecimal(underlyingAmount));
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<CustomerUnderlyingDocumentModel> result = underlyingRepo.findByCustomerIdAndDeleteFlagOrderByUnderlyingAmountAsc((String) map.get(ApplicationConstants.CUST_ID), ApplicationConstants.NO, PagingUtils.createPageRequest(map));
			
			resultMap.put("result", setModelToMap(result.getContent()));
			PagingUtils.setPagingInfo(resultMap, result);

			IDMUserModel userLogin = idmUserRepo.findOne((String) map.get(ApplicationConstants.LOGIN_USERID));
			resultMap.put("branchCode", userLogin.getBranch().getCode());
			resultMap.put("branchName", userLogin.getBranch().getName());
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
}
