package com.gpt.component.calendar.services;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gpt.component.calendar.model.CalendarModel;
import com.gpt.component.common.exceptions.ApplicationException;
import com.gpt.component.common.exceptions.BusinessException;

public interface CalendarService {

	Map<String, Object> search(Map<String, Object> map) throws ApplicationException, BusinessException;

	void submitRange(Map<String, Object> map) throws ApplicationException, BusinessException;

	void submitList(List<CalendarModel> holidayList) throws ApplicationException, BusinessException;

	void submitDelete(Map<String, Object> map) throws ApplicationException, BusinessException;

	boolean isHoliday(Date holidayDate);
	
	boolean isCurrencyHoliday(Date holidayDate, String transactionCurrency);

}
