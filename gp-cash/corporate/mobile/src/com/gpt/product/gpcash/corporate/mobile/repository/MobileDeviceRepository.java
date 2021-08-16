package com.gpt.product.gpcash.corporate.mobile.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.product.gpcash.corporate.corporateuser.model.CorporateUserModel;
import com.gpt.product.gpcash.corporate.mobile.model.MobileDevice;

@Repository
public interface MobileDeviceRepository extends JpaRepository<MobileDevice, String> {
	
	MobileDevice findFirstByDeviceId(String deviceId);
	
	MobileDevice findByDeviceIdAndUserId(String deviceId, String userId);

	@Query("from MobileDevice m join fetch m.user u join fetch u.user where u in ?1 and m.pushNotificationId is not null")
	List<MobileDevice> findPushIdByCorpUserCode(List<CorporateUserModel> corpUsers);
	
	@Modifying
	@Query("UPDATE MobileDevice SET fingerPrintPublicKey = null WHERE deviceId = ?1")
	void resetRegisteredFP(String deviceId);
	
}
