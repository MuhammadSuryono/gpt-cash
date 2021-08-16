package com.gpt.product.gpcash.corporate.transaction.sipdAPI.services;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.eai.EAIEngine;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.idm.menu.model.IDMMenuModel;
import com.gpt.platform.cash.constants.EAIConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.account.model.AccountModel;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.transaction.sipd.model.SipdModel;
import com.gpt.product.gpcash.corporate.transaction.sipd.repository.SipdRepository;
import com.gpt.product.gpcash.service.model.ServiceModel;

//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Transactional(rollbackFor = Exception.class)
public class SIPDAPIServiceImpl implements SIPDAPIService {
	
	
	@Value("${spidapi.client_id}")
	private String clientID;
	
	@Value("${spidapi.client_secret}")
	private String clientSecret;
	
	@Value("${spidapi.tokenapi.expired}")
	private int tokenExp;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private SipdRepository sipdRepo;
	
	@Autowired
	private EAIEngine eaiAdapter;
	
	@Override
	public Map<String, Object> getTokenAPI(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		
		if(clientID.equals(map.get("client_id").toString()) && clientSecret.equals(map.get("client_secret").toString())) {
			
			Map<String, Object> returnMap = new HashMap<>();
			returnMap.put("access_token", getJWTToken(map.get("client_id").toString(),map.get("client_secret").toString()));
			returnMap.put("expired_in", tokenExp);
			
			return returnMap;
		}else {
			throw new BusinessException("Autentication Failed");
		}
		
		
	}
	
	private String getJWTToken(String cliId, String cliSecret) {
		
		String encodedAuth = Base64.getEncoder().encodeToString((cliId + ":" + cliSecret).getBytes());
		
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList("ROLE_USER");
		
//		String token = Jwts
//				.builder()
//				.setSubject(cliId)		
//				.claim("authorities",
//						grantedAuthorities.stream()
//								.map(GrantedAuthority::getAuthority)
//								.collect(Collectors.toList()))
//				.setIssuedAt(new Date(System.currentTimeMillis()))
//				.setExpiration(new Date(System.currentTimeMillis() + tokenExp*1000))
//				.signWith(SignatureAlgorithm.HS256,
//						encodedAuth.getBytes()).compact();
		
//		return token;
		return null;
	}

	@Override
	public Map<String, Object> postDataSIPD(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");
			
			SipdModel sipdModel = new SipdModel();
			sipdModel= setMapToModel(sipdModel, map);
			sipdModel = sipdRepo.persist(sipdModel);
			
			Map<String, Object> returnMap = new HashMap<>();
			returnMap.put("tx_partner_id", sipdModel.getTxPartnerId());
			returnMap.put("tx_id", sipdModel.getBillId());
			returnMap.put("created", sdf.format(sipdModel.getCreatedDate()));
			
			Map<String, Object> status = new HashMap<>();
			status.put("code", "000");
			status.put("message", "Success");			
			returnMap.put("status", status);
			
			Map<String, Object> additionalData = new HashMap<>();
			returnMap.put("addtionalData", additionalData);
			
			
			return returnMap;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private SipdModel setMapToModel(SipdModel sipdModel, Map<String, Object> map) throws Exception {
		// TODO Auto-generated method stub
		
		String txPartnerId = (String) map.get("tx_partner_id");
		String note = (String) map.get("note");
		String txType = (String) map.get("tx_type");
		BigDecimal amount = (BigDecimal) map.get("amount");
		
		if (map.get("sender_info") != null) {
			Map <String, Object> senderInfoMap = (Map<String, Object>) map.get("sender_info");
			sipdModel.setSenderInfoStr(objectMapper.writeValueAsString(map.get("sender_info")));
		
			AccountModel account = new AccountModel();
			account.setAccountNo((String) senderInfoMap.get("account_number"));
			sipdModel.setSourceAccount(account);
		}
		
		if (map.get("recipient_info") != null) {
			Map <String, Object> recipientInfoMap = (Map<String, Object>) map.get("recipient_info");
			sipdModel.setRecipientInfoStr(objectMapper.writeValueAsString(map.get("recipient_info")));
			
			sipdModel.setBenAccountNo((String) recipientInfoMap.get("account_number"));
			sipdModel.setBenAccountName((String) recipientInfoMap.get("account_bank_name"));
			sipdModel.setBenAccountCurrency((String) recipientInfoMap.get("account_number"));
		}
		
		if (map.get("tx_additional_data") != null) {
			sipdModel.setAdditionalDataStr(objectMapper.writeValueAsString(map.get("tx_additional_data")));			
		}
		
		String userIdSipd = (String) map.get("user_id");
		
		sipdModel.setTxPartnerId(txPartnerId);
		sipdModel.setRemark1(note);
		sipdModel.setTransactionAmount(amount);
		sipdModel.setTrxType(txType);
		sipdModel.setUserIdSipd(userIdSipd);
		
		//constant
		sipdModel.setBenAccountCurrency("IDR");
		sipdModel.setTransactionCurrency("IDR");
		sipdModel.setIsFinalPayment("N");
		sipdModel.setIsProcessed("N");
		
		CorporateModel corporate = new CorporateModel();
		corporate.setId("GDBANK");
		sipdModel.setCorporate(corporate);
		
		sipdModel.setCreatedDate(DateUtils.getCurrentTimestamp());
		sipdModel.setCreatedBy("GDBANK|GDUSERMAKER");
		sipdModel.setStatus("PENDING");
		
		IDMMenuModel menu = new IDMMenuModel();
		menu.setCode("MNU_GPCASH_F_PEMDA_SIPD");
		sipdModel.setMenu(menu);
		
		ServiceModel service = new ServiceModel();
		service.setCode("GPT_FTR_PEMDA_SIPD_IH_OWN");
		sipdModel.setService(service);
		// end constant
		
		sipdModel.setBillId(Helper.generateShortTransactionReferenceNo());
		
		return sipdModel;
	}

	@Override
	public Map<String, Object> inquirySIPD(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		
		Map<String, Object> inputs = new HashMap<>();
		inputs.put("bankCode", map.get("bank_code"));
		inputs.put("accountNo", map.get("bank_account"));
		
		Map<String, Object> returnMap = new HashMap<>();
		
		try {
			Map<String, Object> outputs = eaiAdapter.invokeService(EAIConstants.ACCOUNT_INQUIRY, inputs);
			
			
			returnMap.put("inquiryId", Helper.generateHibernateUUIDGenerator());
			returnMap.put("bank_account_name", outputs.get("accountName"));
			
			Map<String, Object> status = new HashMap<>();
			status.put("code", "900");
			status.put("message", "Inquiry Success");			
			returnMap.put("status", status);
			
			
			
		} catch (BusinessException e) {
			throw e;
		} catch (ApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		
		return returnMap;
	}

	@Override
	public Map<String, Object> checkStatusSIPD(Map<String, Object> map) throws ApplicationException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
