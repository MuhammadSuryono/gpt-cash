package com.gpt.product.gpcash.cot.currency.services;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.product.gpcash.cot.currency.model.CurrencyCOTModel;
import com.gpt.product.gpcash.cot.currency.repository.CurrencyCOTRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class CurrencyCOTServiceImpl implements CurrencyCOTService{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CurrencyCOTRepository currencyCOTRepo;
	
	@Override
	public void validateSystemCOT(String code, String applicationCode)
			throws Exception {
		CurrencyCOTModel currencyCOT = currencyCOTRepo.findByCodeAndApplicationCode(code, applicationCode);
		
		if(currencyCOT == null){
			throw new BusinessException("GPT-0100110");
		}
		
		String[] startTime = currencyCOT.getStartTime().split(":");
		Calendar cotStart = Calendar.getInstance();
		cotStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
		cotStart.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
		cotStart.set(Calendar.SECOND, 0);
		cotStart.set(Calendar.MILLISECOND, 0);
		
		String[] endTime = currencyCOT.getEndTime().split(":");
		Calendar cotEnd = Calendar.getInstance();
		cotEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
		cotEnd.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
		cotEnd.set(Calendar.SECOND, 0);
		cotEnd.set(Calendar.MILLISECOND, 0);
		
		Calendar instructionDateCal = Calendar.getInstance();
		
		if (logger.isDebugEnabled()){
			logger.debug("COT start : " + cotStart.getTime());
			logger.debug("COT end : " + cotEnd.getTime());
			logger.debug("Today Date : " + instructionDateCal.getTime());
		}
		
		if(!(instructionDateCal.compareTo(cotStart) >= 0 && instructionDateCal.compareTo(cotEnd) <= 0)){
			throw new BusinessException("GPT-0100112");
		}
	}
	
	@Override
	public void validateSystemCOT(String code, String applicationCode, String sessionTime)
			throws Exception {
		CurrencyCOTModel currencyCOT = currencyCOTRepo.findByCodeAndApplicationCode(code, applicationCode);
		
		if(currencyCOT == null){
			throw new BusinessException("GPT-0100110");
		}
		
		String[] startTime = currencyCOT.getStartTime().split(":");
		Calendar cotStart = Calendar.getInstance();
		cotStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
		cotStart.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
		cotStart.set(Calendar.SECOND, 0);
		cotStart.set(Calendar.MILLISECOND, 0);
		
		String[] endTime = currencyCOT.getEndTime().split(":");
		Calendar cotEnd = Calendar.getInstance();
		cotEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
		cotEnd.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
		cotEnd.set(Calendar.SECOND, 0);
		cotEnd.set(Calendar.MILLISECOND, 0);
		
		String[] sessionTimeArr = sessionTime.split("\\:");
		int sessionTimeHour = Integer.valueOf(sessionTimeArr[0]);
		int sessionTimeMinute = Integer.valueOf(sessionTimeArr[1]);
		Calendar instructionDateCal = Calendar.getInstance();
		instructionDateCal.set(Calendar.HOUR_OF_DAY, sessionTimeHour);
		instructionDateCal.set(Calendar.MINUTE, sessionTimeMinute);
		instructionDateCal.set(Calendar.SECOND, 0);
		instructionDateCal.set(Calendar.MILLISECOND, 0);
		
		if (logger.isDebugEnabled()){
			logger.debug("COT start : " + cotStart.getTime());
			logger.debug("COT end : " + cotEnd.getTime());
			logger.debug("Future / Recurring Date : " + instructionDateCal.getTime());
		}
		
		if(!(instructionDateCal.compareTo(cotStart) >= 0 && instructionDateCal.compareTo(cotEnd) <= 0)){
			throw new BusinessException("GPT-0100112");
		}
	}
}
