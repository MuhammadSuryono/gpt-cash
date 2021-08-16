package com.gpt.product.gpcash.pendingdownload.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.pendingdownload.model.PendingDownloadModel;

@Repository
public interface PendingDownloadRepository extends JpaRepository<PendingDownloadModel, String>, CashRepository<PendingDownloadModel> {
	@Query("from PendingDownloadModel pd "
			+ "where pd.createdBy = ?1 and menuCode = ?2 order by pd.createdDate desc")	
	Page<PendingDownloadModel> searchPendingDownloadByUser(String userId, String menuCode, Pageable pageInfo) throws Exception;
	
	@Query("from PendingDownloadModel pd "
			+ "where pd.status = ?1")	
	List<PendingDownloadModel> searchAllNew(String newStatus) throws Exception;
}