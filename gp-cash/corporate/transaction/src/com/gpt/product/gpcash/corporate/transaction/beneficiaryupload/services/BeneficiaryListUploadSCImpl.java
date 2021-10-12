package com.gpt.product.gpcash.corporate.transaction.beneficiaryupload.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.common.validation.annotation.Input;
import com.gpt.component.common.validation.annotation.Option;
import com.gpt.component.common.validation.annotation.Output;
import com.gpt.component.common.validation.annotation.SubVariable;
import com.gpt.component.common.validation.annotation.Validate;
import com.gpt.component.common.validation.annotation.Variable;
import com.gpt.component.common.validation.converter.Format;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.platform.cash.workflow.CorporateWFEngine;
import com.gpt.product.gpcash.corporate.logging.annotation.EnableCorporateActivityLog;
import com.gpt.product.gpcash.corporate.pendingtaskuser.services.CorporateUserPendingTaskService;
import com.gpt.product.gpcash.corporate.pendingtaskuser.valueobject.CorporateUserPendingTaskVO;
import com.gpt.product.gpcash.corporate.token.validation.services.TokenValidationService;
import com.gpt.product.gpcash.corporate.transaction.pendingupload.services.PendingUploadService;
import com.gpt.product.gpcash.corporate.transaction.validation.services.TransactionValidationService;

@Validate
@Service
public class BeneficiaryListUploadSCImpl implements BeneficiaryListUploadSC {
	
	@Autowired
	private BeneficiaryListUploadService beneUploadService;
	
	@Autowired
	private PendingUploadService pendingUploadService;
	
	@Autowired
	private TransactionValidationService transactionValidationService;		
	
	@Autowired
	private TokenValidationService tokenValidationService;
	
	@Autowired
	private CorporateUserPendingTaskService pendingTaskService;
	
	@Autowired
	private CorporateWFEngine wfEngine;

	
	@Validate
	@Output({
		@Variable(name = "fileId"),
		@Variable(name = ApplicationConstants.FILENAME),
		@Variable(name = "uploadDateTime", type = Date.class, format = Format.DATE_TIME)
	})
	@Override
	public Map<String, Object> upload(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneUploadService.upload(map);
	}
	
	@Validate
	@Input({
		@Variable(name = "fileFormat", options = {
			ApplicationConstants.FILE_FORMAT_CSV,
			ApplicationConstants.FILE_FORMAT_TXT
		}),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode )
	})	
	@Override
	public Map<String, Object> downloadSample(Map<String, Object> map) throws ApplicationException, BusinessException {
		return beneUploadService.downloadSample(map);
	}

	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = "pendingUploadId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DETAIL}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode )
	})	
	@Override
	public Map<String, Object> searchPendingUploadById(Map<String, Object> map) throws ApplicationException, BusinessException {		
		return pendingUploadService.searchBeneficiaryUploadById(map);
	}

	@EnableCorporateActivityLog
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({
		@Variable(name = "pendingUploadId"),	
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_CONFIRM}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode )
	})	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> confirm(Map<String, Object> map) throws ApplicationException, BusinessException {
		
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		Map<String, Object> pendingUploadInfo = pendingUploadService.searchBeneficiaryUploadByIdValidOnly(map);				
		map.putAll(pendingUploadInfo);
		
		transactionValidationService.validateMaximumRecordBulkTransaction(((Long)map.get("totalRecord")).intValue());
		
		String corpId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String tokenNo = (String) map.get(ApplicationConstants.LOGIN_TOKEN_NO);
		
		map = beneUploadService.confirm(map);

		map.put(ApplicationConstants.IS_ONE_SIGNER, isOneSigner);
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			//pengecekan jika loginTokenNo tidak ada maka mungkin saja telah di unassign, maka harus di assign dl token nya.
			if(!ValueUtils.hasValue(tokenNo)) {
				throw new BusinessException("GPT-0100153");
			}
			map.put(ApplicationConstants.CHALLENGE_NO, tokenValidationService.getChallenge(corpId,userCode,tokenNo));				
		}
		
		return map; 
	}	
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "pendingUploadId"),	
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { 
					"CREATE_OVERBOOKING",
					"CREATE_DOMESTIC",
					"CREATE_DOMESTIC_ONLINE",
					"CREATE_INTERNATIONAL"
				}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode )
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_REFERENCE_NO),
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N),
		@Variable(name = ApplicationConstants.WF_FIELD_DATE_TIME_INFO, format = Format.I18N)
	})	
	@Transactional(rollbackFor = Exception.class)
	@Override
	public Map<String, Object> submit(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> pendingUploadInfo = pendingUploadService.searchBeneficiaryUploadByIdValidOnly(map);		
		map.putAll(pendingUploadInfo);
		
		String isOneSigner = map.get(ApplicationConstants.IS_ONE_SIGNER)!=null?(String) map.get(ApplicationConstants.IS_ONE_SIGNER):ApplicationConstants.NO;
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			tokenValidationService.authenticate((String) map.get(ApplicationConstants.LOGIN_CORP_ID), 
					(String) map.get(ApplicationConstants.LOGIN_USERCODE), 
					(String) map.get(ApplicationConstants.LOGIN_TOKEN_NO), (String) map.get(ApplicationConstants.CHALLENGE_NO), (String) map.get(ApplicationConstants.RESPONSE_NO));
		}
		
		Map<String, Object> resultMap = beneUploadService.submit(map);
		
		
		if(ApplicationConstants.YES.equals(isOneSigner)) {
			CorporateUserPendingTaskVO vo = (CorporateUserPendingTaskVO) resultMap.get(ApplicationConstants.PENDINGTASK_VO);
			String pendingTaskId = vo.getId();
			vo = pendingTaskService.approve(pendingTaskId, (String) map.get(ApplicationConstants.LOGIN_USERCODE));
			
			if(ApplicationConstants.YES.equals(vo.getIsError())) {
				throw new BusinessException(vo.getErrorCode());
			} else {				
				resultMap = new HashMap<>();
				String strDateTime = Helper.DATE_TIME_FORMATTER.format(vo.getCreatedDate());
				resultMap.put(ApplicationConstants.WF_FIELD_REFERENCE_NO, vo.getReferenceNo());
				resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0200005");
				resultMap.put(ApplicationConstants.WF_FIELD_DATE_TIME_INFO, "GPT-0200008|" + strDateTime);
				resultMap.put("dateTime", strDateTime);
			}
			
			//end taskInstance
			wfEngine.endInstance(pendingTaskId);			
		}
		
		return resultMap;
		
	}

	@EnableCorporateActivityLog
	@Transactional(rollbackFor = Exception.class)
	@Override
	public CorporateUserPendingTaskVO approve(CorporateUserPendingTaskVO vo) throws ApplicationException, BusinessException {
		transactionValidationService.validateCOT((String) vo.getInstructionMode(), 
				vo.getTransactionServiceCode(), 
				vo.getTransactionCurrency(), ApplicationConstants.APP_GPCASHIB,
				vo.getSessionTime(), true, true);
		
		vo = beneUploadService.approve(vo);
		
//		globalTransactionService.updateExecutedTransactionByUserCode(vo.getCreatedBy());
		
		return vo;
	}

	@Override
	public CorporateUserPendingTaskVO reject(CorporateUserPendingTaskVO vo)	throws ApplicationException, BusinessException {
		return null;
	}
	
	@Validate
	@Input({
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),		
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE), 
		@Variable(name = ApplicationConstants.WF_ACTION, options = {ApplicationConstants.WF_ACTION_SEARCH}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode)
	})	
	@Output({
		@Variable(name = "fileFormats", type = List.class)
	})
	@Override
	public Map<String, Object> getFileFormats(Map<String, Object> map) throws ApplicationException, BusinessException {
		List<String> supportedFileFormat = new ArrayList<>();
		supportedFileFormat.add(ApplicationConstants.FILE_FORMAT_CSV);
		supportedFileFormat.add(ApplicationConstants.FILE_FORMAT_TXT);		
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("fileFormats", supportedFileFormat);
		
		return resultMap;
	}	
	
	@Validate(paging = Option.REQUIRED, sorting = Option.OPTIONAL)
	@Input({ 
		@Variable(name = "pendingUploadId"),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DETAIL }),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode) 
	})
	@Output({ 
		@Variable(name = "details", type = List.class, subVariables = {
			@SubVariable(name = "benAccountNo"),
			@SubVariable(name = "benAccountName")
		})
	})	
	@Override
	public Map<String, Object> searchPendingUploadDetail(Map<String, Object> map) throws ApplicationException, BusinessException {
		return pendingUploadService.searchPendingUploadDetail(map);
	}
	
	@EnableCorporateActivityLog
	@Validate
	@Input({
		@Variable(name = "pendingUploadId", type = List.class),
		@Variable(name = ApplicationConstants.LOGIN_USERID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.LOGIN_CORP_ID, format = Format.UPPER_CASE),
		@Variable(name = ApplicationConstants.WF_ACTION, options = { ApplicationConstants.WF_ACTION_DELETE}),
		@Variable(name = ApplicationConstants.STR_MENUCODE, options = menuCode )
	})
	@Output({
		@Variable(name = ApplicationConstants.WF_FIELD_MESSAGE, format = Format.I18N)
	})	
	@Override
	public Map<String, Object> deletePendingUpload(Map<String, Object> map) throws ApplicationException, BusinessException {		
		return pendingUploadService.deleteBeneficiaryUpload(map);
	}	
}