package com.gpt.product.gpcash.corporate.transaction.domestic.repository;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.gpt.platform.cash.repository.CashRepository;
import com.gpt.product.gpcash.corporate.pendingtaskuser.model.CorporateUserPendingTaskModel;
import com.gpt.product.gpcash.corporate.transaction.domestic.model.DomesticTransferModel;

@Repository
public interface DomesticTransferCustomRepository extends CashRepository<DomesticTransferModel>{

	public List<CorporateUserPendingTaskModel> findDomesticReport(Map<String, Object> map);

}
