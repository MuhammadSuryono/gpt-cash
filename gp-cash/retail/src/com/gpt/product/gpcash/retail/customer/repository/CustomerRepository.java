package com.gpt.product.gpcash.retail.customer.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.retail.customer.model.CustomerModel;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerModel, String>, CashRepository<CustomerModel> {
	@Query("from CustomerModel cust where UPPER(userId) = UPPER(?1) and deleteFlag = 'N' ")
	CustomerModel findByUserIdContainingIgnoreCase(String userId) throws Exception;
	
	@Query("from CustomerModel cust where UPPER(hostCifId) = UPPER(?1) and deleteFlag = 'N' ")
	CustomerModel findByHostCifIdContainingIgnoreCase(String hostCifId) throws Exception;
	
	@Query("from CustomerModel cust where deleteFlag = 'N' ")
	List<CustomerModel> findCustomers() throws Exception;
	
	@Query("select custUser.id, custUser.userId, custUser.name, usr.stillLoginFlag, usr.code "
			 + "from CustomerModel custUser "
		     + "left join custUser.user usr "
		     + "where custUser.id like (?1) "
		     + "and custUser.userId like (?2) "
		     + "and usr.stillLoginFlag like (?3) "
		     + "and usr.deleteFlag = 'N' ")
	Page<Object[]> findStillLoginUsers(String customerId, String userId, String stillLoginFlag, Pageable pageInfo) throws Exception;
}
