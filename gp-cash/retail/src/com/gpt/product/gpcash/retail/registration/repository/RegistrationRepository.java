package com.gpt.product.gpcash.retail.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.registration.model.RegistrationModel;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationModel, String>, CashRepository<RegistrationModel> {
	
	@Query("from RegistrationModel where UPPER(code) = UPPER(?1) and activeFlag = 'N'") 
	RegistrationModel findByRegistrationCode(String registrationCode) throws Exception;
}
