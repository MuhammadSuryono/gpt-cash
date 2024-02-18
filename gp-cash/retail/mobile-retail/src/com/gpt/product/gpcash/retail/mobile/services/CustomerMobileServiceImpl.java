package com.gpt.product.gpcash.retail.mobile.services;

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
import com.gpt.component.idm.user.services.IDMUserService;
import com.gpt.component.password.services.IPasswordUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;
import com.gpt.platform.cash.utils.Helper;
import com.gpt.product.gpcash.retail.CustomerUtilsRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.customer.repository.CustomerRepository;
import com.gpt.product.gpcash.retail.mobile.CustomerMobileConstants;
import com.gpt.product.gpcash.retail.mobile.CustomerStatus;
import com.gpt.product.gpcash.retail.mobile.model.CustomerMobileDevice;
import com.gpt.product.gpcash.retail.mobile.repository.CustomerMobileDeviceRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerMobileServiceImpl implements CustomerMobileService {

	@Autowired
	private IDMLoginService idmLoginService;
	
	@Autowired
	private CustomerUtilsRepository customerUtilsRepo;
	
	@Autowired
	private CustomerRepository custUserRepo;
	
	@Autowired
	private CustomerMobileDeviceRepository mobileDeviceRepo;
	
	@Autowired
	private IDMUserRepository idmUserRepo;
	
	@Autowired
	private IPasswordUtils passwordUtils;
	
	@Autowired
	private OTPEngine otpEngine;
	
	@Autowired
	private IDMUserService idmUserService;

	@SuppressWarnings("unchecked")
	private CustomerMobileDevice validateCustomerMobileDevice(Map<String, Object> map) throws BusinessException {
		boolean deviceOk = false;
		CustomerMobileDevice md = null;
		Map<String, String> deviceInfo = (Map<String, String>)map.get("device");
		if(deviceInfo != null) {
			String id = deviceInfo.get("id");
			if(id != null) {
				String custId = (String) map.get(ApplicationConstants.CUST_ID); 
				CustomerMobileDevice thisUserMd = mobileDeviceRepo.findByDeviceIdAndUserId(id, custId);
				if(thisUserMd != null)
					md = thisUserMd;
				else
					md = mobileDeviceRepo.findFirstByDeviceId(id);
				if(md == null) {
					// new device, still ok with provisioning
					deviceOk = true;
				} else if(md.getCustomerStatus() == CustomerStatus.BLOCKED) {
					// this device has been blocked, when a device is blocked, all user using the same device is also blocked
					deviceOk = false;
				} else if(thisUserMd != null){
					deviceOk = true;
					if(md.getCustomerStatus() == CustomerStatus.INACTIVE) {
						// known device, but still need provisioning
						md = null;
					}
				} else {
					// known device but not registered for this user, so still need provisioning
					deviceOk = true;
					md = null;
				}
			}
		}
		if(!deviceOk) {
			throw new BusinessException(CustomerMobileConstants.ERROR_DEVICE_BLOCKED);
		}
		return md;
	}
	
	@Override
	public Map<String, Object> authenticate(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			validateCustomerMobileDevice(map);

			String passwd = (String) map.get("passwd");
			String key = (String)map.get("key");
			String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);
			String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERID);

			//remark if stress test
			CustomerModel customer = customerUtilsRepo.getCustomerRepo().findByUserIdContainingIgnoreCase(loginUserId);
			
			if(customer == null)
				throw new BusinessException("GPT-0100032");
			
			String custId = customer.getId();
			
			if(ApplicationConstants.YES.equals(customer.getInactiveFlag())) {
				throw new BusinessException("GPT-0100160");
			}
			//---------------------------------------------------
			
			Map<String, Object> returnMap = idmLoginService.authenticate(custId, passwd, key, ipAddress);
			
			returnMap.put(ApplicationConstants.CUST_ID, custId);
			
			otpEngine.generateOTP(custId, 6);
			
			returnMap.put("otp", true);
			returnMap.put("phoneNo", getMaskedPhoneNo(custId));
			
			return returnMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}	
	
	private String getMaskedPhoneNo(String userCode) {
		CustomerModel custUser = custUserRepo.findOne(userCode);
		String phoneNo = custUser.getMobileNo();
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
		String custId = (String) map.get(ApplicationConstants.CUST_ID);
		String otp = (String)map.get("otp");
		
		if(!otpEngine.validateOTP(custId, otp)) {
			throw new BusinessException(CustomerMobileConstants.ERROR_INVALID_OTP);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void finalizeSetup(Map<String, Object> map) throws ApplicationException, BusinessException {
		validateOTP(map);
		
		String custId = (String) map.get(ApplicationConstants.CUST_ID);
		Map<String, String> deviceInfo = (Map<String, String>)map.get("device");

		CustomerMobileDevice md = mobileDeviceRepo.findByDeviceIdAndUserId(deviceInfo.get("id"), custId);
		if(md == null) {
			md = new CustomerMobileDevice();
			md.setDeviceId(deviceInfo.get("id"));
			md.setUser(custUserRepo.findOne((String)map.get(ApplicationConstants.CUST_ID)));
		}
		
		md.setDeviceName(deviceInfo.get("name"));
		md.setModel(deviceInfo.get("model"));
		md.setPlatform(deviceInfo.get("osType"));
		md.setOsVersion(deviceInfo.get("osVer"));
		md.setAppVersion(deviceInfo.get("appVer"));
		md.setLastActivity(DateUtils.getCurrentTimestamp());
		md.setPushNotificationId(deviceInfo.get("pushId"));
		
		md.setCustomerStatus(CustomerStatus.ACTIVE);
		
		mobileDeviceRepo.save(md);
	}

	@Override
	public void registerFP(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String custId = (String) map.get(ApplicationConstants.CUST_ID);
			IDMUserModel user = idmUserRepo.findOne(custId);
			
			//validate password
			String passwordFromDB = new String(passwordUtils.decrypt(user.getPasswd().getBytes(ApplicationConstants.CHARSET)));

			String heartBeat = (String)map.get("key");
			byte[] key = Helper.generateAESKey(passwordFromDB, heartBeat);
			
			String pk = (String)map.get("pk");
	        byte[] publicKey = null;
	        try {
	        		publicKey = passwordUtils.decryptCBC(pk.getBytes(ApplicationConstants.CHARSET), key); 
	        }catch(Exception e) {
				throw new BusinessException(CustomerMobileConstants.ERROR_INVALID_FP_REGISTRATION);
	        }
	        String publicKeyContent = new String(publicKey).replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
			byte[] publicKeyRaw = Base64.decodeBase64(publicKeyContent);
			
			verifyPublicKey(publicKeyRaw);			
			
			CustomerMobileDevice md = validateCustomerMobileDevice(map);
			
			if(md != null) {
				md.setFingerPrintPublicKey(new String(passwordUtils.encrypt(publicKeyRaw)));
				mobileDeviceRepo.save(md);
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
	 * This method assumes that validateCustomerMobileDevice is ok
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	private void updateDeviceInfo(CustomerMobileDevice md, Map<String, Object> map) {
		String custId = (String)map.get(ApplicationConstants.CUST_ID);
		Map<String, String> deviceInfo = (Map<String, String>)map.get("device");
		String deviceId = deviceInfo.get("id");
		if(md == null)
			md = mobileDeviceRepo.findByDeviceIdAndUserId(deviceId, custId);
		
		if(md == null) {
			md = new CustomerMobileDevice();
			md.setDeviceId(deviceId);
			md.setUser(custUserRepo.findOne(custId));
		}
		
		md.setDeviceName(deviceInfo.get("name"));
		md.setModel(deviceInfo.get("model"));
		md.setPlatform(deviceInfo.get("osType"));
		md.setOsVersion(deviceInfo.get("osVer"));
		md.setAppVersion(deviceInfo.get("appVer"));
		md.setLastActivity(DateUtils.getCurrentTimestamp());
		md.setPushNotificationId(deviceInfo.get("pushId"));
		
		md.setCustomerStatus(CustomerStatus.ACTIVE);
		
		mobileDeviceRepo.save(md);
	}
	
	public void activateDevice(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			validateOTP(map);
			updateDeviceInfo(validateCustomerMobileDevice(map), map);
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}

	@Override
	public Map<String, Object> mobileCustLogin(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			
			
			String passwd = (String) map.get("passwd");
			String key = (String)map.get("key");
			String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);
			String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERID);
			
			//remark if stress test
			CustomerModel customer = customerUtilsRepo.getCustomerRepo().findByUserIdContainingIgnoreCase(loginUserId);
			
			if(customer == null)
				throw new BusinessException("GPT-0100032");
			
			if(ApplicationConstants.YES.equals(customer.getInactiveFlag()))
				throw new BusinessException("GPT-R-0100160");
			
			//---------------------------------------------------
			map.put(ApplicationConstants.CUST_ID, customer.getId());
			CustomerMobileDevice md = validateCustomerMobileDevice(map); //pindah ke bawah, supaya bisa dapat custIdnya
			

			String custId = customer.getId();
			
			Map<String, Object> returnMap = idmLoginService.login(custId, passwd, key, ipAddress);
			
			returnMap.put(ApplicationConstants.CUST_ID, custId);
			returnMap.put("forceChangePassword",returnMap.get(ApplicationConstants.IDM_USER_STATUS).equals(ApplicationConstants.IDM_USER_STATUS_RESET));
			
			if(md == null) {
				// need provisioning
				otpEngine.generateOTP(custId, 6);

				returnMap.put("otp", true);
				returnMap.put("phoneNo", getMaskedPhoneNo(custId));
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
	public Map<String, Object> mobileCustFPLogin(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String loginUserId = (String) map.get(ApplicationConstants.LOGIN_USERID);
			
			//remark if stress test
			CustomerModel customer = customerUtilsRepo.getCustomerRepo().findByUserIdContainingIgnoreCase(loginUserId);
			
			if(customer == null)
				throw new BusinessException("GPT-0100032");
			
			if(ApplicationConstants.YES.equals(customer.getInactiveFlag()))
				throw new BusinessException("GPT-R-0100160");
			//
			
			map.put(ApplicationConstants.CUST_ID, customer.getId());
			
			CustomerMobileDevice md = validateCustomerMobileDevice(map);
			if(md == null) {
				Map<String, Object> returnMap = new HashMap<>();
				returnMap.put("notAllowed", true);
				
				return returnMap;
			}
			String ipAddress = (String) map.get(ApplicationConstants.LOGIN_IPADDRESS);
			String custId = customer.getId();
			String key = (String)map.get("key");
			
			boolean somethingIsWrong = false;
			if(md.getFingerPrintPublicKey() == null) {
				somethingIsWrong = true;
			} else {
				

				
				byte[] sign = Base64.decodeBase64((String)map.get("sign"));
			
				//byte[] keyNonce = Helper.generateAESKey(key, key);
				//byte[] nonce = ((String)map.get("nonce")).getBytes(ApplicationConstants.CHARSET);
				
				//ikutin coprorate nya, tidk ada kirim nonce
				byte[] nonce = key.getBytes(ApplicationConstants.CHARSET); 
		        try {
		        		//nonce = passwordUtils.decryptCBC(nonce, keyNonce); 
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
				IDMUserModel user = idmUserRepo.findOne(custId);
				String passwordFromDB = new String(passwordUtils.decrypt(user.getPasswd().getBytes(ApplicationConstants.CHARSET)));
				String passwd = Helper.generateHash(passwordFromDB + key);
				
				Map<String, Object> returnMap = idmLoginService.login(custId, passwd, key, ipAddress);
				returnMap.put(ApplicationConstants.CUST_ID, custId);
				
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
	
	@Override
	public Map<String, Object> mobileForceChangePassword(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			String loginId = (String) map.get(ApplicationConstants.LOGIN_USERID);
			CustomerModel customer = customerUtilsRepo.getCustomerRepo().findByUserIdContainingIgnoreCase(loginId);
			
			String oldPassword = (String) map.get("oldPassword");
			String newPassword = (String) map.get("newPassword");
			String newPassword2 = (String) map.get("newPassword2");
			byte[] key = Helper.generateAESKey("", (String)map.get("key"));
			
			try {
				byte[] oldPasswordByte = passwordUtils.decryptCBC(oldPassword.getBytes(ApplicationConstants.CHARSET), key); 
				oldPassword = new String(oldPasswordByte, ApplicationConstants.CHARSET);
			}catch(Exception e) {
				//sengaja di telan, dr depan tidak perlu tau.
				e.printStackTrace();
			}
			
			try {
				byte[] newPasswordByte = passwordUtils.decryptCBC(newPassword.getBytes(ApplicationConstants.CHARSET), key); 
				newPassword = new String(newPasswordByte, ApplicationConstants.CHARSET);
			}catch(Exception e) {
				//sengaja di telan, dr depan tidak perlu tau.
				e.printStackTrace();
			}
			
			try {
				byte[] newPassword2Byte = passwordUtils.decryptCBC(newPassword2.getBytes(ApplicationConstants.CHARSET), key); 
				newPassword2 = new String(newPassword2Byte, ApplicationConstants.CHARSET);
			}catch(Exception e) {
				//sengaja di telan, dr depan tidak perlu tau.
				e.printStackTrace();
			}
			
		
			idmUserService.changePassword(customer.getId(), oldPassword, newPassword,
					newPassword2);
			
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(ApplicationConstants.WF_FIELD_MESSAGE, "GPT-0100116");
			return resultMap;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
}
