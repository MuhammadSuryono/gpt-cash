package com.gpt.product.gpcash.corporate.inquiry.accountstatement.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.maintenance.sysparam.SysParamConstants;
import com.gpt.component.maintenance.sysparam.model.SysParamModel;
import com.gpt.component.maintenance.sysparam.repository.SysParamRepository;
import com.gpt.product.gpcash.corporate.inquiry.transaction.services.TransactionHistoryService;

@Service
@Transactional(rollbackFor = Exception.class)
public class AccountStatementServiceImpl implements AccountStatementService {
	
	@Autowired
	private TransactionHistoryService transactionHistoryService;
	
	@Autowired
	private SysParamRepository sysParamRepo;
	
	@Override
	public Map<String, Object> getPeriods(Map<String, Object> map) throws ApplicationException, BusinessException {
		List<Map<String,Integer>> periods = new ArrayList<>();	
		try {
			SysParamModel model = sysParamRepo.findByCode(SysParamConstants.ACCOUNT_STATEMENT_PERIODS);
			if (model!=null) {
				int val = Integer.parseInt(model.getValue());
				if (val>0) {
					Calendar cal = Calendar.getInstance();
					int currentMonth = cal.get(Calendar.MONTH);
					for (int i=0; i<val; i++) {
						cal.set(Calendar.MONTH, currentMonth-i);

						Map<String,Integer> period = new HashMap<>();
						period.put("month", cal.get(Calendar.MONTH));
						period.put("year", cal.get(Calendar.YEAR));
						periods.add(period);
					}
				}
			}
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
		
		Map<String,Object> result = new HashMap<>();
		result.put("periods", periods);
		
		return result;
	}	
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, ((Integer)map.get("month") - 1));
    	cal.set(Calendar.YEAR, (Integer)map.get("year"));        
        
    	cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        map.put("fromDate", cal.getTime());
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        map.put("toDate", cal.getTime());
        
		return transactionHistoryService.periodicTransaction(map);
	}
	
	@Override
	public Map<String, Object> download(Map<String, Object> map) throws ApplicationException, BusinessException {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, ((Integer)map.get("month") - 1));
		cal.set(Calendar.YEAR, (Integer)map.get("year"));        
		
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		map.put("fromDate", cal.getTime());
		
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		map.put("toDate", cal.getTime());
		
//		transactionHistoryService.periodicTransaction(map);
		
		return null;		
	}	
    
}
