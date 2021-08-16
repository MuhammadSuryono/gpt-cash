package com.gpt.product.gpcash.retail.mobile.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.product.gpcash.retail.customer.model.CustomerModel;
import com.gpt.product.gpcash.retail.mobile.model.CustomerMobileDevice;

@Repository
public interface CustomerMobileDeviceRepository extends JpaRepository<CustomerMobileDevice, String> {
	
	CustomerMobileDevice findFirstByDeviceId(String deviceId);
	
	CustomerMobileDevice findByDeviceIdAndUserId(String deviceId, String userId);

	@Query("from CustomerMobileDevice m join fetch m.user u join fetch u.user where u in ?1 and m.pushNotificationId is not null")
	List<CustomerMobileDevice> findPushIdByCustUserCode(List<CustomerModel> users);
	
}
