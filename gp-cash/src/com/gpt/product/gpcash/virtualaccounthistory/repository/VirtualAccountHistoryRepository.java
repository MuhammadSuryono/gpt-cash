package com.gpt.product.gpcash.virtualaccounthistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.virtualaccounthistory.model.VirtualAccountHistoryModel;

@Repository
public interface VirtualAccountHistoryRepository extends JpaRepository<VirtualAccountHistoryModel, String>, CashRepository<VirtualAccountHistoryModel>{

}
