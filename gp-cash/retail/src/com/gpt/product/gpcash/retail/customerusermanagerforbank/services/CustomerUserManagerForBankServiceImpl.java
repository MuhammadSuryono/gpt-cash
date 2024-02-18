package com.gpt.product.gpcash.retail.customerusermanagerforbank.services;

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
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerUserManagerForBankServiceImpl implements CustomerUserManagerForBankService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private IDMUserRepository idmUserRepo;

	@Autowired
	private CustomerRepository customerRepo;

	@SuppressWarnings("unchecked")
	@Override
	public void updateStillLoginFlag(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			List<String> userList = (List<String>) map.get("userList");
			if (ValueUtils.hasValue(userList)) {
				idmUserRepo.updateStillLoginFlagByCodes(userList);
			} else {
				throw new BusinessException("GPT-0100010");
			}

		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public void updateStillLoginFlagALL(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			idmUserRepo.updateStillLoginFlagByApplicationCodes(
					(List<String>) map.get(ApplicationConstants.APP_CODE));

		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> findByStillLogin(Map<String, Object> map)
			throws ApplicationException, BusinessException {

		Map<String, Object> resultMap = new HashMap<>();
		try {
			String stillLoginFlag = Helper.getSearchWildcardValue((String) map.get("stillLoginFlag"));
			String userId = Helper.getSearchWildcardValue((String) map.get("userId"));
			String customerId = Helper.getSearchWildcardValue((String) map.get(ApplicationConstants.CUST_ID));

			Page<Object[]> result = customerRepo.findStillLoginUsers(customerId, userId, stillLoginFlag,
					PagingUtils.createPageRequest(map));

			resultMap.put("result", setModelToMap(result.getContent()));

			PagingUtils.setPagingInfo(resultMap, result);
		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<Object[]> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (Object[] model : list) {
			Map<String, Object> map = new HashMap<>();
			map.put(ApplicationConstants.CUST_ID, model[0]);
			map.put("userId", ValueUtils.hasValue(model[1]) ? model[1] : ApplicationConstants.EMPTY_STRING);
			map.put("userName", ValueUtils.hasValue(model[2]) ? model[2] : ApplicationConstants.EMPTY_STRING);
			map.put("stillLoginFlag", ValueUtils.hasValue(model[3]) ? model[3] : ApplicationConstants.EMPTY_STRING);
			map.put("userCode", ValueUtils.hasValue(model[4]) ? model[4] : ApplicationConstants.EMPTY_STRING);

			resultList.add(map);

		}

		return resultList;
	}

}
