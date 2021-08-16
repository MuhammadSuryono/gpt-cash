package com.gpt.product.gpcash.corporate.transaction.directdebit.grantdebit.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.PagingUtils;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.pendingtask.services.PendingTaskService;
import com.gpt.component.pendingtask.valueobject.PendingTaskVO;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.transaction.directdebit.grantdebit.model.GrantDebitModel;
import com.gpt.product.gpcash.corporate.transaction.directdebit.grantdebit.repository.GrantDebitRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class GrantDebitServiceImpl implements GrantDebitService {
	@Autowired
	private GrantDebitRepository grantDebitRepo;

	@Autowired
	private PendingTaskService pendingTaskService;

    @Autowired
    private EAIEngine eaiAdapter;	

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			PendingTaskVO vo = setPendingTaskVO(map);
			vo.setJsonObject(map); // set model yg mau di simpan ke pending task

			// check unique pending task
			pendingTaskService.checkUniquePendingTask(vo);

			if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_CREATE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_CREATE);

				// check unique
				List<Map<String, Object>> accountList = (ArrayList<Map<String, Object>>) map.get("accountList");
				for (Map<String, Object> accountMap : accountList) {
					String corporateId = (String) map.get(ApplicationConstants.CORP_ID);
					String accountNo = (String) accountMap.get("accountNo");
					
					Date expiryDate = null;
					if(accountMap.get("expiryDate") != null) {
						expiryDate = (Date) accountMap.get("expiryDate");
					}
					
					
					validateDetail(accountNo, expiryDate);
					

					checkUniqueRecord(corporateId, accountNo);
				}

			} else if (map.get(ApplicationConstants.WF_ACTION).equals(ApplicationConstants.WF_ACTION_DELETE)) {
				vo.setAction(ApplicationConstants.WF_ACTION_DELETE);
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

	private List<Map<String, Object>> setModelToMap(List<GrantDebitModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (GrantDebitModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}

	private Map<String, Object> setModelToMap(GrantDebitModel model ) {
		Map<String, Object> map = new HashMap<>();

		map.put("id", model.getId());
		map.put("accountNo", model.getAccountNo());
		map.put("accountName", model.getAccountName());
		map.put("accountCurrencyCode", model.getAccountCurrencyCode());
		map.put("expiryDate", model.getExpiryDate());
		map.put("maxDebitLimit", model.getMaxDebitLimit());
		
		CorporateModel corporate = model.getCorporate();
		map.put(ApplicationConstants.CORP_ID, corporate.getId());
		map.put("corporateName", corporate.getName());
		
		map.put("createdBy", ValueUtils.getValue(model.getCreatedBy()));
		map.put("createdDate", ValueUtils.getValue(model.getCreatedDate()));
		map.put("updatedBy", ValueUtils.getValue(model.getUpdatedBy()));
		map.put("updatedDate", ValueUtils.getValue(model.getUpdatedDate()));

		return map;
	}

	private PendingTaskVO setPendingTaskVO(Map<String, Object> map) {
		PendingTaskVO vo = new PendingTaskVO();
		vo.setUniqueKey((String) map.get(ApplicationConstants.CORP_ID));
		vo.setUniqueKeyDisplay(((String) map.get(ApplicationConstants.CORP_ID)));
		vo.setCreatedBy((String) map.get(ApplicationConstants.LOGIN_USERID));
		vo.setMenuCode((String) map.get(ApplicationConstants.STR_MENUCODE));
		vo.setService("GrantDebitSC");

		return vo;
	}

	private GrantDebitModel setMapToModel(Map<String, Object> map, String corporateId) throws Exception {
		GrantDebitModel grantDebit = new GrantDebitModel();

		CorporateModel corporate = new CorporateModel();
		corporate.setId(corporateId);
		grantDebit.setCorporate(corporate);

		grantDebit.setAccountNo((String) map.get("accountNo"));
		grantDebit.setAccountName((String) map.get("accountName"));
		grantDebit.setAccountCurrencyCode((String) map.get("accountCurrencyCode"));
		
		grantDebit.setMaxDebitLimit(new BigDecimal((Integer) map.get("maxDebitLimit")));

		if(map.get("expiryDate") != null) {
			grantDebit.setExpiryDate(new Timestamp(FastDateFormat.getInstance("dd/MM/yyyy")
					.parse((String) map.get("expiryDate")).getTime()));
		}

		return grantDebit;
	}

	private void checkUniqueRecord(String corporateId, String accountNo) throws Exception {
		if (grantDebitRepo.findByCorporateIdAndAccountNo(corporateId, accountNo, null).hasContent()) {
			throw new BusinessException("GPT-0100047", new String[] { accountNo });
		}
	}
	
	@Override
	public Map<String, Object> validateDetail(String accountNo, Date expiryDate) 
			throws ApplicationException, BusinessException {
		try {
			if(expiryDate != null) {
				//validate expiryDate
				Date todayDate = DateUtils.getCurrentDate();
				
				if(todayDate.compareTo(expiryDate) >= 0){
					throw new BusinessException("GPT-0100209");
				}
			}
			
			//validate account
			Map<String, Object> inputs = new HashMap<>();
			inputs.put("accountNo", accountNo);
			
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.ACCOUNT_INQUIRY, inputs);
			
			Map<String, Object> result = new HashMap<>(3,1);
			result.put("accountNo", accountNo);
			result.put("accountName", outputs.get("accountName"));
			result.put("accountCurrencyCode", outputs.get("accountCurrencyCode"));
			
			return result;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PendingTaskVO approve(PendingTaskVO vo) throws ApplicationException, BusinessException {
		try {
			Map<String, Object> map = (Map<String, Object>) vo.getJsonObject();

			String corporateId = (String) map.get(ApplicationConstants.CORP_ID);

			List<Map<String, Object>> accountList = (List<Map<String, Object>>) map.get("accountList");
			if (ApplicationConstants.WF_ACTION_CREATE.equals(vo.getAction())) {
				for (Map<String, Object> accountMap : accountList) {
					GrantDebitModel grantDebit = setMapToModel(accountMap, corporateId);
					saveGrantDebit(grantDebit, vo.getCreatedBy());
				}
			} else if (ApplicationConstants.WF_ACTION_DELETE.equals(vo.getAction())) {
				for (Map<String, Object> accountMap : accountList) {
					String id = (String) accountMap.get("id");
					deleteGrantDebit(id);
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
		return null;
	}

	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Page<GrantDebitModel> result = grantDebitRepo.findByCorporateId(
					(String) map.get(ApplicationConstants.CORP_ID), PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	@Override
	public Map<String, Object> searchByCorporateIdAndAccount(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String accountNo = (String) map.get("accountNo");
			Page<GrantDebitModel> result = grantDebitRepo.findByCorporateIdAndAccountNo(
							(String) map.get(ApplicationConstants.LOGIN_CORP_ID), accountNo,
							PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}
	
	@Override
	public List<GrantDebitModel> searchByCorporateId(String corporateId) throws ApplicationException, BusinessException {
		try {
			Page<GrantDebitModel> result = grantDebitRepo.findByCorporateId(corporateId, null);
			return result.getContent();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	private void saveGrantDebit(GrantDebitModel grantDebit, String createdBy)
			throws ApplicationException, BusinessException {
		// set default value
		grantDebit.setCreatedDate(DateUtils.getCurrentTimestamp());
		grantDebit.setCreatedBy(createdBy);

		grantDebitRepo.persist(grantDebit);
	}

	private void deleteGrantDebit(String id)
			throws ApplicationException, BusinessException {
		grantDebitRepo.delete(id);
	}
}
