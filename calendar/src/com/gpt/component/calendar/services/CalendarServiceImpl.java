package com.gpt.component.calendar.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.gpt.component.calendar.CalendarConstants;
import com.gpt.component.calendar.model.CalendarModel;
import com.gpt.component.calendar.repository.CalendarRepository;
import com.gpt.component.common.broadcast.Broadcaster;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;
import com.gpt.component.common.spring.Util;
import com.gpt.component.common.utils.ValueUtils;
import com.gpt.platform.cash.constants.ApplicationConstants;
import com.gpt.platform.cash.utils.DateUtils;

@Service
public class CalendarServiceImpl implements CalendarService {
	
	@Autowired
	private CalendarRepository calendarRepo;
	
	@Autowired
	private Broadcaster broadcaster;
	
	@Autowired
	private ApplicationContext appCtx;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String month = (String) map.get("month");
			String year = (String) map.get("year");
			
			Calendar startHolidayDate = Calendar.getInstance();
			startHolidayDate.set(Calendar.MONTH, Integer.parseInt(month) - 1);
			startHolidayDate.set(Calendar.YEAR, Integer.parseInt(year));
			startHolidayDate.set(Calendar.HOUR_OF_DAY, 0);
			startHolidayDate.set(Calendar.MINUTE, 0);
			startHolidayDate.set(Calendar.SECOND, 0);
			startHolidayDate.set(Calendar.MILLISECOND, 0);
			startHolidayDate.set(Calendar.DAY_OF_MONTH,
					startHolidayDate.getActualMinimum(Calendar.DAY_OF_MONTH));
			
			
			Calendar endHolidayDate = Calendar.getInstance();
			endHolidayDate.set(Calendar.MONTH, Integer.parseInt(month) - 1);
			endHolidayDate.set(Calendar.YEAR, Integer.parseInt(year));
			endHolidayDate.set(Calendar.HOUR_OF_DAY, 0);
			endHolidayDate.set(Calendar.MINUTE, 0);
			endHolidayDate.set(Calendar.SECOND, 0);
			endHolidayDate.set(Calendar.MILLISECOND, 0);
			endHolidayDate.set(Calendar.DAY_OF_MONTH,
					endHolidayDate.getActualMaximum(Calendar.DAY_OF_MONTH));
			
			List<CalendarModel> result = calendarRepo.findByHolidayDateBetween(new java.sql.Date(startHolidayDate.getTimeInMillis()), 
					new java.sql.Date(endHolidayDate.getTimeInMillis()));

			resultMap.put("result", setModelToMap(result));

		} catch (Exception e) {
			throw new ApplicationException(e);
		}

		return resultMap;
	}

	private List<Map<String, Object>> setModelToMap(List<CalendarModel> list) {
		List<Map<String, Object>> resultList = new ArrayList<>();

		for (CalendarModel model : list) {
			resultList.add(setModelToMap(model));
		}

		return resultList;
	}
	
	private Map<String, Object> setModelToMap(CalendarModel model) {
		Map<String, Object> map = new HashMap<>();
		
		map.put("holidayDate", model.getHolidayDate());
		map.put("holidayDscp", model.getDscp());
		map.put("type", model.getType());
		map.put("currencyCode", ValueUtils.getValue(model.getCurrency()));
		
		return map;
	}
	
	private void resetScheduler() {
		// only load/reload scheduler if commit is successfull
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter(){
			@Override
			public void afterCommit() {
				// broadcast to all server to reload all trigger
				broadcaster.broadcast(CalendarService.class.getSimpleName(), "");
			}
		});
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void submitRange(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Date holidayDateFrom = (Date) map.get("holidayDateFrom");
			Date holidayDateTo = (Date) map.get("holidayDateTo");
			String dscp = (String) map.get("dscp");
			String type = (String) map.get("type");
			String currencyCode = (String) map.get("currencyCode");
			String createdBy = (String) map.get(ApplicationConstants.LOGIN_USERID);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(holidayDateFrom);
			saveHoliday(dscp, holidayDateFrom, type, createdBy, currencyCode);
			
			//call Service untuk ambil semua domestic transaction future yang sudah di release dan ins datenya menjadi holiday
			   //POC ban lampung tc007
				try{
					Util.invokeSpringBean(appCtx, "DomesticTransferSC", "saveTransactionHoliday", cal.getTime());
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
				//
				
			while (cal.getTime().before(holidayDateTo)) {
			    cal.add(Calendar.DATE, 1);
			    saveHoliday(dscp, cal.getTime(), type, createdBy, currencyCode);
			    
			  //call Service untuk ambil semua domestic transaction future yang sudah di release dan ins datenya menjadi holiday
			   //POC ban lampung tc007
				try{
					Util.invokeSpringBean(appCtx, "DomesticTransferSC", "saveTransactionHoliday", cal.getTime());
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
				//
			}
	
			
			resetScheduler();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	private void saveHoliday(String dscp, Date holidayDate, String type, String createdBy, String currencyCode) throws Exception{
		CalendarModel calendar = new CalendarModel();
		calendar.setDscp(dscp);
		calendar.setHolidayDate(new java.sql.Date(holidayDate.getTime()));
		calendar.setType(type);
		calendar.setCurrency(currencyCode);
		calendar.setCreatedDate(DateUtils.getCurrentTimestamp());
		calendar.setCreatedBy(createdBy);
		calendarRepo.persist(calendar);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void submitList(List<CalendarModel> calendarList) throws ApplicationException, BusinessException {
		calendarRepo.save(calendarList);
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException {
		try {
			Date holidayDate = (Date) map.get("holidayDate");
			calendarRepo.deleteByHolidayDate(new java.sql.Date(holidayDate.getTime()));
			
			resetScheduler();
		} catch (Exception e) {
			throw new ApplicationException(e);
		}
	}
	
	@Override
	public boolean isHoliday(Date holidayDate) {
		Calendar calc = Calendar.getInstance();
		calc.setTime(holidayDate);
		calc.set(Calendar.HOUR_OF_DAY, 0);
		calc.set(Calendar.MINUTE, 0);
		calc.set(Calendar.SECOND, 0);
		calc.set(Calendar.MILLISECOND, 0);
		
		List<CalendarModel> calendar = calendarRepo.findByHolidayDate(new java.sql.Date(calc.getTimeInMillis()), CalendarConstants.TYPE_HOLIDAY);
		if(calendar.size() > 0){
			return true;
		} else {
			return false;
		}
		
	}

	@Override
	public boolean isCurrencyHoliday(Date holidayDate, String transactionCurrency) {
		Calendar calc = Calendar.getInstance();
		calc.setTime(holidayDate);
		calc.set(Calendar.HOUR_OF_DAY, 0);
		calc.set(Calendar.MINUTE, 0);
		calc.set(Calendar.SECOND, 0);
		calc.set(Calendar.MILLISECOND, 0);
		
		List<CalendarModel> calendar = calendarRepo.findByHolidayDate(new java.sql.Date(calc.getTimeInMillis()), CalendarConstants.TYPE_CURRENCY);
		if(calendar.size() > 0){
			for (CalendarModel calendarModel : calendar) {
				if (calendarModel.getCurrency().equals(transactionCurrency)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
}
