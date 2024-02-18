package com.gpt.component.maintenance.promo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gpt.component.maintenance.promo.model.PromoModel;
import com.gpt.platform.cash.repository.CashRepository;

@Repository
public interface PromoRepository extends JpaRepository<PromoModel, String>, CashRepository<PromoModel> {
	
	PromoModel findByUploadIdAndDeleteFlag(String uploadId, String isDelete);
	
	@Query("FROM PromoModel p "
			+ "WHERE p.deleteFlag = 'N'"
			+ "AND p.infoType IN ('INFO', 'NEWS')"
			+ "ORDER BY p.infoType, p.createdDate DESC")	
	List<PromoModel> findAllNewsAndInfo(); 
	
}
