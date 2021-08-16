package com.gpt.product.gpcash.corporate.mobile.services;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.otp.OTPEngine;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.component.idm.login.services.IDMLoginService;
import com.gpt.component.idm.user.model.IDMUserModel;
import com.gpt.component.idm.user.repository.IDMUserRepository;
import com.gpt.component.password.services.IPasswordUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.corporate.corporate.model.CorporateModel;
import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.corporateuser.repository.CorporateUserRepository;
import com.gpt.product.gpcash.corporate.mobile.MobileConstants;
import com.gpt.product.gpcash.corporate.mobile.Status;
import com.gpt.product.gpcash.corporate.mobile.model.MobileDevice;
import com.gpt.product.gpcash.corporate.mobile.repository.MobileDeviceRepository;
import com.gpt.product.gpcash.corporate.utils.CorporateUtilsRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class MobileServiceImpl implements MobileService {

	@Autowired
	private IDMLoginService idmLoginService;
	
	@Autowired
	private CorporateUtilsRepository corporateUtilsRepo;
	
	@Autowired
	private CorporateUserRepository corpUserRepo;
	
	@Autowired
	private MobileDeviceRepository mobileDeviceRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private IPasswordUtils passwordUtils;
	
	@Autowired
	private OTPEngine otpEngine;

	@SuppressWarnings("unchecked")
	private String getDeviceId(Map<String, Object> map) {
		Map<String, String> deviceInfo = (Map<String, String>)map.get("device");
		if(deviceInfo != null)
			return deviceInfo.get("id");
		return null;
	}
	
	private MobileDevice validateMobileDevice(Map<String, Object> map) throws BusinessException {
		boolean deviceOk = false;
		MobileDevice md = null;
		String id = getDeviceId(map);
		if(id != null) {
			String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			MobileDevice thisUserMd = mobileDeviceRepo.findByDeviceIdAndUserId(id, userCode);
			if(thisUserMd != null)
				md = thisUserMd;
			else
				md = mobileDeviceRepo.findFirstByDeviceId(id);
			if(md == null) {
				// new device, still ok with provisioning
				deviceOk = true;
			} else if(md.getStatus() == Status.BLOCKED) {
				// this device has been blocked, when a device is blocked, all user using the same device is also blocked
				deviceOk = false;
			} else if(thisUserMd != null){
				deviceOk = true;
				if(md.getStatus() == Status.INACTIVE) {
					// known device, but still need provisioning
					md = null;
				}
			} else {
				// known device but not registered for this user, so still need provisioning
				deviceOk = true;
				md = null;
			}
		}
		if(!deviceOk) {
			throw new BusinessException(MobileConstants.ERROR_DEVICE_BLOCKED);
		}
		return md;
	}
	
	@Override
	public Map<String, Object> authenticate(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			validateMobileDevice(map);

			String passwd = (String) map.get("passwd");
			String key = (String)map.get("key");
			String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);
			String corpId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);

			//remark if stress test
			CorporateModel corporate = corporateUtilsRepo.isCorporateValid(corpId);
			if(ApplicationConstants.YES.equals(corporate.getInactiveFlag())) {
				throw new BusinessException("GPT-0100160");
			}
			//---------------------------------------------------
			
			Map<String, Object> returnMap = idmLoginService.authenticate(userCode, passwd, key, ipAddress);
			
			returnMap.put(ApplicationConstants.LOGIN_USERCODE, userCode);
			returnMap.put(ApplicationConstants.LOGIN_CORP_ID, corpId);
			
			otpEngine.generateOTP(userCode, 6);
			
			returnMap.put("otp", true);
			returnMap.put("phoneNo", getMaskedPhoneNo(userCode));
			
			return returnMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}	
	
	private String getMaskedPhoneNo(String userCode) {
		CorporateUserModel corpUser = corpUserRepo.findOne(userCode);
		String phoneNo = corpUser.getMobilePhoneNo();
		if(ValueUtils.hasValue(phoneNo)) {
			StringBuilder sb = new StringBuilder(phoneNo);
			for(int i=0;i<sb.length() - 4;i++) {
				sb.setCharAt(i, 'X');
			}
			return sb.toString();
		}
		return "XXXX";
	}
	
	private void validateOTP(Map<String, Object> map) throws BusinessException {
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		String otp = (String)map.get("otp");
		
		if(!otpEngine.validateOTP(userCode, otp)) {
			throw new BusinessException(MobileConstants.ERROR_INVALID_OTP);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void finalizeSetup(Map<String, Object> map) throws ApplicationException, BusinessException {
		validateOTP(map);
		
		String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
		Map<String, String> deviceInfo = (Map<String, String>)map.get("device");

		MobileDevice md = mobileDeviceRepo.findByDeviceIdAndUserId(deviceInfo.get("id"), userCode);
		if(md == null) {
			md = new MobileDevice();
			md.setDeviceId(deviceInfo.get("id"));
			md.setUser(corpUserRepo.findOne((String)map.get(ApplicationConstants.LOGIN_USERCODE)));
		}
		
		md.setDeviceName(deviceInfo.get("name"));
		md.setModel(deviceInfo.get("model"));
		md.setPlatform(deviceInfo.get("osType"));
		md.setOsVersion(deviceInfo.get("osVer"));
		md.setAppVersion(deviceInfo.get("appVer"));
		md.setLastActivity(DateUtils.getCurrentTimestamp());
		md.setPushNotificationId(deviceInfo.get("pushId"));
		
		md.setStatus(Status.ACTIVE);
		
		mobileDeviceRepo.save(md);
	}

	@Override
	public void registerFP(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			IDMUserModel user = idmUserRepo.findOne(userCode);
			
			//validate password
			String passwordFromDB = new String(passwordUtils.decrypt(user.getPasswd().getBytes(ApplicationConstants.CHARSET)));

			String heartBeat = (String)map.get("key");
			byte[] key = Helper.generateAESKey(passwordFromDB, heartBeat);
			
			String pk = (String)map.get("pk");
	        byte[] publicKey = null;
	        try {
	        	publicKey = passwordUtils.decryptCBC(pk.getBytes(ApplicationConstants.CHARSET), key); 
	        }catch(Exception e) {
				throw new BusinessException(MobileConstants.ERROR_INVALID_FP_REGISTRATION);
	        }
	        String publicKeyContent = new String(publicKey).replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
			byte[] publicKeyRaw = Base64.decodeBase64(publicKeyContent);
			
			verifyPublicKey(publicKeyRaw);			

			MobileDevice md = validateMobileDevice(map);
			
			if(md != null) {
				// for security purpose we must reset all other FP registered to this device first, so only single user can login using 
				// FP for the same device
				mobileDeviceRepo.resetRegisteredFP(md.getDeviceId());
				
				md.setFingerPrintPublicKey(new String(passwordUtils.encrypt(publicKeyRaw)));
				mobileDeviceRepo.save(md);
			} else {
				throw new BusinessException(MobileConstants.ERROR_INVALID_FP_REGISTRATION);
			}

		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private PublicKey verifyPublicKey(byte[] publicKey) throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(publicKey);
     
        return kf.generatePublic(keySpecX509);
	}
	
	private boolean verifySign(byte[] publicKey, byte[] data, byte[] sign) throws Exception {
		Signature sn = Signature.getInstance("SHA256withRSA");
		sn.initVerify(verifyPublicKey(publicKey));
		sn.update(data);
		
		return sn.verify(sign);
	}
	
	/**
	 * This method assumes that validateMobileDevice is ok
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	private void updateDeviceInfo(MobileDevice md, Map<String, Object> map) {
		String userCode = (String)map.get(ApplicationConstants.LOGIN_USERCODE);
		Map<String, String> deviceInfo = (Map<String, String>)map.get("device");
		String deviceId = deviceInfo.get("id");
		if(md == null)
			md = mobileDeviceRepo.findByDeviceIdAndUserId(deviceId, userCode);
		
		if(md == null) {
			md = new MobileDevice();
			md.setDeviceId(deviceId);
			md.setUser(corpUserRepo.findOne(userCode));
		}
		
		md.setDeviceName(deviceInfo.get("name"));
		md.setModel(deviceInfo.get("model"));
		md.setPlatform(deviceInfo.get("osType"));
		md.setOsVersion(deviceInfo.get("osVer"));
		md.setAppVersion(deviceInfo.get("appVer"));
		md.setLastActivity(DateUtils.getCurrentTimestamp());
		md.setPushNotificationId(deviceInfo.get("pushId"));
		
		md.setStatus(Status.ACTIVE);
		
		mobileDeviceRepo.save(md);
	}
	
	public void activateDevice(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			validateOTP(map);
			updateDeviceInfo(validateMobileDevice(map), map);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> mobileCorpLogin(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			MobileDevice md = validateMobileDevice(map);
			
			String passwd = (String) map.get("passwd");
			String key = (String)map.get("key");
			String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);
			String corpId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			
			//remark if stress test
			CorporateModel corporate = corporateUtilsRepo.isCorporateValid(corpId);
			if(ApplicationConstants.YES.equals(corporate.getInactiveFlag())) {
				throw new BusinessException("GPT-0100160");
			}
			//---------------------------------------------------
			
			Map<String, Object> returnMap = idmLoginService.login(userCode, passwd, key, ipAddress);
			
			returnMap.put(ApplicationConstants.LOGIN_USERCODE, userCode);
			returnMap.put(ApplicationConstants.LOGIN_CORP_ID, corpId);
			
			if(md == null) {
				// need provisioning
				otpEngine.generateOTP(userCode, 6);

				returnMap.put("otp", true);
				returnMap.put("phoneNo", getMaskedPhoneNo(userCode));
			} else {
				// need to update device info
				updateDeviceInfo(md, map);
			}
			
			return returnMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public Map<String, Object> mobileCorpFPLogin(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			MobileDevice md = validateMobileDevice(map);
			if(md == null) {
				Map<String, Object> returnMap = new HashMap<>();
				returnMap.put("notAllowed", true);
				
				return returnMap;
			}
			String userCode = (String) map.get(ApplicationConstants.LOGIN_USERCODE);
			String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);
			String corpId = (String) map.get(ApplicationConstants.LOGIN_CORP_ID);
			String key = (String)map.get("key");
			
			boolean somethingIsWrong = false;
			if(md.getFingerPrintPublicKey() == null) {
				somethingIsWrong = true;
			} else {
				
				//remark if stress test
				CorporateModel corporate = corporateUtilsRepo.isCorporateValid(corpId);
				if(ApplicationConstants.YES.equals(corporate.getInactiveFlag())) {
					throw new BusinessException("GPT-0100160");
				}
				//---------------------------------------------------
				
				byte[] sign = Base64.decodeBase64((String)map.get("sign"));
				byte[] nonce = key.getBytes(ApplicationConstants.CHARSET);
		        try {
					byte[] publicKey = passwordUtils.decrypt(md.getFingerPrintPublicKey().getBytes(ApplicationConstants.CHARSET));
					if(!verifySign(publicKey, nonce, sign)) {
						somethingIsWrong = true;
					}
		        }catch(Exception e) {
		        		somethingIsWrong = true;
		        }
			}
			
			if(somethingIsWrong) {
				/**
				 * This flow should never get executed unless the user is messing with the mobile app or somebody is
				 * trying to invoke this api directly, thus login with FP is no longer allowed on the mobile. 
				 */
				md.setFingerPrintPublicKey(null);
				mobileDeviceRepo.save(md);

				Map<String, Object> returnMap = new HashMap<>();
				returnMap.put("notAllowed", true);

				return returnMap;
			} else {
				// we'll do normal login from here
				IDMUserModel user = idmUserRepo.findOne(userCode);
				String passwordFromDB = new String(passwordUtils.decrypt(user.getPasswd().getBytes(ApplicationConstants.CHARSET)));
				String passwd = Helper.generateHash(passwordFromDB + key);
				
				Map<String, Object> returnMap = idmLoginService.login(userCode, passwd, key, ipAddress);
				returnMap.put(ApplicationConstants.LOGIN_USERCODE, userCode);
				returnMap.put(ApplicationConstants.LOGIN_CORP_ID, corpId);
				
				// need to update device info
				updateDeviceInfo(md, map);
				
				return returnMap;
			}
			
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
}
