package com.gpt.product.gpcash.corporate.transaction.cheque.order.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.model.ChequeOrderDetailModel;
import com.gpt.product.gpcash.corporate.transaction.cheque.order.model.ChequeOrderModel;

@Repository
public interface ChequeOrderRepository extends JpaRepository<ChequeOrderModel, String>, CashRepository<ChequeOrderModel>{
	List<ChequeOrderModel> findByOrderNo(String orderNo);
	
	@Query("from ChequeOrderDetailModel detail "
			+ "where detail.chequeOrder.id = ?1 ")
	Page<ChequeOrderDetailModel> findDetailByOrderId(String chequeOrderId, Pageable pageInfo) throws Exception;

	@Query("from ChequeOrderModel chq "
			+ "where chq.id in (?1) and chq.isProcessed = 'Y' "
			+ "and chq.corporateUserGroup.id = ?2 ")
	Page<ChequeOrderModel> findByChequeOrderId(List<String> chequeOrderId, String userGroupId, Pageable pageInfo) throws Exception;
	
	@Query("select distinct detail.chequeOrder.id from ChequeOrderDetailModel detail "
			+ "where detail.chequeNo like ?1 ")
	List<String> findByChequeNo(String chequeNo) throws Exception;
	
	@Query("select distinct header.branch.code from ChequeOrderModel header "
			+ "where header.branch.code = ?1 "
			+ "and header.status in (?2) "
			+ "and header.createdDate between ?3 and ?4 order by header.branch.code")
	List<String> findChequeBranch(String branches, List<String> statusList, Date startDate, Date endDate) throws Exception;
	
	@Query("select distinct header.branch.code from ChequeOrderModel header "
			+ "where header.status in (?1) "
			+ "and header.createdDate between ?2 and ?3 order by header.branch.code")
	List<String> findChequeAllBranch(List<String> statusList, Date startDate, Date endDate) throws Exception;
	
	@Query("from ChequeOrderModel header "
			+ "where header.branch.code = ?1 "
			+ "and header.status in (?2) "
			+ "and header.createdDate between ?3 and ?4 order by header.branch.code")
	List<ChequeOrderModel> findChequeOrderByBranch(String branch, List<String> statusList, Date startDate, Date endDate) throws Exception;
	
	@Query("from ChequeOrderModel header "
			+ "where header.branch.code = ?1 "
			+ "and header.status in (?2) "
			+ "and header.createdDate between ?3 and ?4 "
			+ "and header.instructionDate < ?5 order by header.branch.code")
	List<ChequeOrderModel> findChequeOrderByBranchForExpired(String branch, List<String> statusList, Date startDate, Date endDate, Date currentDate) throws Exception;
}
