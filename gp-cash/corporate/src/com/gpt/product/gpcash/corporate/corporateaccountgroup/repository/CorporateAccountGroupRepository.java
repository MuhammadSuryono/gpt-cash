package com.gpt.product.gpcash.corporate.corporateaccountgroup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupDetailModel;
import com.gpt.product.gpcash.corporate.corporateaccountgroup.model.CorporateAccountGroupModel;

@Repository
public interface CorporateAccountGroupRepository extends JpaRepository<CorporateAccountGroupModel, String>, CashRepository<CorporateAccountGroupModel>{
	@Modifying
	@Query("delete from CorporateAccountGroupDetailModel detail where detail.corporateAccount.id = ?1")
	void deleteDetailByCorporateAccountId(String corporateAccountId);
	
	@Modifying
	@Query("delete from CorporateAccountGroupDetailModel detail where detail.corporateAccountGroup.id= ?1")
	void deleteDetailByAccountGroupId(String id);
	
	@Modifying
	@Query("delete from CorporateAccountGroupDetailModel detail where detail.id = ?1")
	void deleteDetailById(String id);
	
	@Modifying
	@Query("delete from CorporateAccountGroupDetailModel detail where detail.id in ?1")
	void deleteDetailById(List<String> idList);
		
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "where detail.corporateAccount.corporate.id = ?1 "
			+ "and detail.corporateAccount.account.accountNo = ?2 "
			+ "and detail.corporateAccountGroup.id = ?3")
	List<CorporateAccountGroupDetailModel> findDetailByAccountNoAndAccountGroupId(String corporateId, String accountNo, String corporateAccountGroupId) throws Exception;
	
	@Query("select detail.id from CorporateAccountGroupDetailModel detail "
			+ "where detail.corporateAccount.corporate.id = ?1 "
			+ "and detail.corporateAccountGroup.id = ?2")
	List<String> findDetailByAccountGroupId(String corporateId, String corporateAccountGroupId) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "where detail.corporateAccount.corporate.id = ?1 "
			+ "and detail.corporateAccount.account.accountNo = ?2")
	List<CorporateAccountGroupDetailModel> findDetailByAccountNo(String corporateId, String accountNo) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "where detail.corporateAccount.corporate.id = ?1 "
			+ "and detail.corporateAccount.account.accountNo = ?2 "
			+ "and detail.isInquiry = 'Y' "
			+ "order by detail.corporateAccount.account.accountNo")
	List<CorporateAccountGroupDetailModel> findDetailByAccountNoAndIsInquiry(String corporateId, String accountNo) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join detail.corporateAccount corpAcct "
			+ "join fetch corpAcct.account "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and detail.id = ?2")
	CorporateAccountGroupDetailModel findDetailById(String corporateId, String accountGroupDetailId) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join fetch detail.corporateAccount as corpAcct "
			+ "join fetch corpAcct.account as acct "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and acct.accountNo = ?2 "
			+ "and detail.corporateAccountGroup.id = ?3 "
			+ "and detail.isInquiry = 'Y' "
			+ "order by acct.accountNo")
	List<CorporateAccountGroupDetailModel> findDetailByAccountNoAndAccountGroupIdAndIsInquiry(String corporateId, String accountNo, String accountGroupId) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join fetch detail.corporateAccount as corpAcct "
			+ "join fetch corpAcct.account as acct "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and acct.branch.code = ?2 "
			+ "and detail.corporateAccountGroup.id = ?3 "
			+ "and detail.isInquiry = 'Y' "
			+ "order by acct.accountNo")
	List<CorporateAccountGroupDetailModel> findDetailByBranchCodeAndAccountGroupIdAndIsInquiry(String corporateId, String branchCode, String accountGroupId) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join fetch detail.corporateAccount as corpAcct "
			+ "join fetch corpAcct.account as acct "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and acct.accountType.code in ?2 "
			+ "and detail.corporateAccountGroup.id = ?3 "
			+ "and detail.isInquiry = 'Y' "
			+ "order by acct.accountNo")
	List<CorporateAccountGroupDetailModel> findDetailByAccountTypeAndAccountGroupIdAndIsInquiry(String corporateId, List<String> accountTypeList, String accountGroupId) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join fetch detail.corporateAccount as corpAcct "
			+ "join fetch corpAcct.account as acct "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and detail.corporateAccountGroup.id = ?2 "
			+ "and detail.isInquiry = 'Y' "
			+ "order by acct.accountNo")
	List<CorporateAccountGroupDetailModel> findDetailByAccountGroupIdAndIsInquiry(String corporateId, String accountGroupId) throws Exception;
	
	CorporateAccountGroupModel findByCorporateIdAndCode(String corporateId, String accountGroupCode) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "where detail.corporateAccount.corporate.id = ?1 "
			+ "and detail.corporateAccount.account.accountNo in (?2)")
	List<CorporateAccountGroupDetailModel> findDetailByAccountNoList(String corporateId, List<String> accountNo) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join fetch detail.corporateAccount corpAcct "
			+ "join fetch corpAcct.account as acct "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and detail.corporateAccountGroup.id = ?2 "
			+ "and detail.isDebit = 'Y' and acct.accountType.code in (?3) "
			+ "and acct.currency.code = ?4 "
			+ "order by acct.accountNo")
	List<CorporateAccountGroupDetailModel> findDetailByAccountGroupIdAndIsDebit(String corporateId, String accountGroupId,
			List<String> accountTypeList, String localCurrency) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join fetch detail.corporateAccount corpAcct "
			+ "join fetch corpAcct.account as acct "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and detail.corporateAccountGroup.id = ?2 "
			+ "and detail.isDebit = 'Y' and acct.accountType.code in (?3) "
			+ "order by acct.accountNo")
	List<CorporateAccountGroupDetailModel> findDetailByAccountGroupIdAndIsDebitMultiCurrency(String corporateId, String accountGroupId,
			List<String> accountTypeList) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join fetch detail.corporateAccount corpAcct "
			+ "join fetch corpAcct.account as acct "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and detail.isDebit = 'Y' and acct.accountType.code in (?2) "
			+ "and acct.currency.code = ?3 "
			+ "order by acct.accountNo")
	List<CorporateAccountGroupDetailModel> findDetailByAccountGroupIdAndIsDebitForBank(String corporateId,
			List<String> accountTypeList, String localCurrency) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join fetch detail.corporateAccount corpAcct "
			+ "join fetch corpAcct.account as acct "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and detail.corporateAccountGroup.id = ?2 "
			+ "and detail.isCredit = 'Y' and acct.accountType.code in (?3) "
			+ "order by acct.accountNo")
	List<CorporateAccountGroupDetailModel> findDetailByAccountGroupIdAndIsCredit(String corporateId, String accountGroupId, 
			List<String> accountTypeList) throws Exception;
	
	@Query("select count(*) as count from CorporateAccountGroupDetailModel detail "
			+ "where detail.corporateAccount.corporate.id = ?1 "
			+ "and detail.corporateAccountGroup.id = ?2")
	Object getCountByCorporateIdAndAccountGroupId(String corporateId, String accountGroupId) throws Exception;
	
	@Modifying
	@Query("update CorporateAccountGroupDetailModel detail set isInquiry = ?2 , isCredit = ?3, isDebit=?4 "
			+ "where detail.corporateAccount.id = ?1")
	void updateDetailByCorporateAccountId(String corporateAccountId, String isAllowInquiry, 
			String isAllowCredit, String isAllowDebit);
	
	@Query("select detail.corporateAccount.account.accountNo from CorporateAccountGroupDetailModel detail "
			+ "where detail.corporateAccount.corporate.id = ?1 "
			+ "and detail.id = ?2")
	String findAccountNoByAccountGroupDetailId(String corporateId, String corporateAccountGroupDetailId) throws Exception;
	
	@Query("from CorporateAccountGroupDetailModel detail "
			+ "join fetch detail.corporateAccount corpAcct "
			+ "join fetch corpAcct.account as acct "
			+ "where corpAcct.corporate.id = ?1 "
			+ "and detail.corporateAccountGroup.id = ?2 "
			+ "and acct.accountNo = ?3 "
			+ "and detail.isCredit = 'Y' and acct.accountType.code in (?4) "
			+ "and acct.currency.code = ?5 ")
	List<CorporateAccountGroupDetailModel> findDetailByAccountGroupIdCorpAccAndIsCredit(String corporateId, String accountGroupId, String acctNo, 
			List<String> accountTypeList, String localCurrency) throws Exception;
}
