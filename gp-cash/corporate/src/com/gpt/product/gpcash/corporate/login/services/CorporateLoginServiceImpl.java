package com.gpt.product.gpcash.corporate.login.services;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.login.services.IDMLoginService;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.outsourceadmin.model.OutsourceAdminLoginModel;
import com.gpt.product.gpcash.corporate.outsourceadmin.repository.OutsourceAdminRepository;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CorporateLoginServiceImpl implements CorporateLoginService {
	
	@Autowired
	private IDMLoginService idmLoginService;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private OutsourceAdminRepository osRepository;
	
	@Override
	public Map<String, Object> corporateLogin(Map<String, Object> map) throws ApplicationException, BusinessException {
		String passwd = (String) map.get("passwd");
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);
		String corpId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);

		try {
			//remark if stress test
			CorporateModel corporate = corporateUtilsRepo.isCorporateValid(corpId);
			if(ApplicationConstants.YES.equals(corporate.getInactiveFlag())) {
				throw new BusinessException("GPT-0100160");
			}
			//---------------------------------------------------
			
			Map<String, Object> returnMap = idmLoginService.login(userCode, passwd, ipAddress);
			
			//rewrite special for corporate since corporate have userCode and corpId
			returnMap.put(ApplicationConstants.LOGIN_USERCODE, userCode);
			returnMap.put(ApplicationConstants.LOGIN_CORP_ID, corpId);
			
			return returnMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> corporateLoginOutsource(Map<String, Object> map) throws ApplicationException, BusinessException {
		

		String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);
		String sId = (String) map.get("sId");
		
		String userCode = "";
		String corpId = "";
		String userId = "";
		
		//get Data from tabel osAdmin 
		OutsourceAdminLoginModel osLoginModel = osRepository.getOne(sId);
		if(osLoginModel!=null) {
			Timestamp today = DateUtils.getCurrentTimestamp();
			long minutes = TimeUnit.MILLISECONDS.toMinutes(today.getTime() - osLoginModel.getCreatedDate().getTime());
			if(minutes < 30) {
				userId = osLoginModel.getUserId();
				corpId = osLoginModel.getCorporate().getId();
				userCode = Helper.getCorporateUserCode(corpId, userId);
				
			}else {
				throw new BusinessException("Link is expired");
			}
		}else {
			throw new BusinessException("Link is expired");
		}
		
		try {
			//remark if stress test
			CorporateModel corporate = corporateUtilsRepo.isCorporateValid(corpId);
			if(ApplicationConstants.YES.equals(corporate.getInactiveFlag())) {
				throw new BusinessException("GPT-0100160");
			}
			//---------------------------------------------------
			
			Map<String, Object> returnMap = idmLoginService.loginOutsource(userCode, ipAddress);
			
			//rewrite special for corporate since corporate have userCode and corpId
			returnMap.put(ApplicationConstants.LOGIN_USERCODE, userCode);
			returnMap.put(ApplicationConstants.LOGIN_CORP_ID, corpId);
			returnMap.put(ApplicationConstants.LOGIN_USERID, userId);
			
			return returnMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
}
