package com.gpt.product.gpcash.corporate.usertransactionmapping.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.corporate.usertransactionmapping.model.UserTransactionMappingModel;
import com.gpt.product.gpcash.corporate.usertransactionmapping.repository.UserTransactionMappingRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserTransactionMappingServiceImpl implements UserTransactionMappingService {

	@Autowired
	private UserTransactionMappingRepository userTransactionMappingRepo;
	
	@Override
	public Map<String, Object> countTotalCreatedTrx(String userCode)
			throws ApplicationException, BusinessException {
		UserTransactionMappingModel userTransaction = userTransactionMappingRepo.findOne(userCode);
		Map<String, Object> result = new HashMap<>();
		
		int total = 0;
		if(userTransaction != null) {
			total = userTransaction.getTotalCreatedTransaction();
		}
		result.put("total", total);
		return result;
	}
	
	@Override
	public Map<String, Object> countTotalExecutedTrx(String userCode)
			throws ApplicationException, BusinessException {
		UserTransactionMappingModel userTransaction = userTransactionMappingRepo.findOne(userCode);
		Map<String, Object> result = new HashMap<>();
		
		int total = 0;
		if(userTransaction != null) {
			total = userTransaction.getTotalExecutedTransaction();
		}
		result.put("total", total);
		return result;
	}
	
	@Override
	public void updateCreatedTransactionByUserCode(String userCode) throws ApplicationException, BusinessException {
		try {
			UserTransactionMappingModel userTrx = userTransactionMappingRepo.findOne(userCode);
			if(userTrx != null) {
				userTrx.setTotalCreatedTransaction(userTrx.getTotalCreatedTransaction() + 1);
				userTrx.setTotalExecutedTransaction(0);
				userTransactionMappingRepo.save(userTrx);
			} else {
				userTrx = new UserTransactionMappingModel();
				userTrx.setId(userCode);
				userTrx.setTotalCreatedTransaction(userTrx.getTotalCreatedTransaction() + 1);
				userTransactionMappingRepo.persist(userTrx);
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	@Override
	public void updateExecutedTransactionByUserCode(String userCode) throws ApplicationException, BusinessException {
		try {
			UserTransactionMappingModel userTrx = userTransactionMappingRepo.findOne(userCode);
			if(userTrx != null) {
				int totalCreated = userTrx.getTotalCreatedTransaction() - 1;
				if(totalCreated < 0)
					totalCreated = 0;
				
				userTrx.setTotalCreatedTransaction(totalCreated);
				userTrx.setTotalExecutedTransaction(userTrx.getTotalExecutedTransaction() + 1);
				userTransactionMappingRepo.save(userTrx);
			} else {
				userTrx = new UserTransactionMappingModel();
				userTrx.setId(userCode);
				userTrx.setTotalCreatedTransaction(0);
				userTrx.setTotalExecutedTransaction(userTrx.getTotalExecutedTransaction() + 1);
				
				userTransactionMappingRepo.persist(userTrx);
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}		
	}
	
	@Override
	public void resetTotalExecuted(String parameter) throws ApplicationException, BusinessException {
		try{
			userTransactionMappingRepo.resetTotalExecuted();
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
}
